package ie.dit.d13122842.main;

import android.os.Handler;
import android.util.Log;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.ArrayList;

import ie.dit.d13122842.messages.ControlMessage;
import ie.dit.d13122842.messages.ResultMessage;
import ie.dit.d13122842.messages.WorkMessage;
import ie.dit.d13122842.posting.FormPoster;
import ie.dit.d13122842.utils.Timer;
import ie.dit.d13122842.utils.Utils;

public class Cleaner {
    private final Handler handler;

    public Cleaner(Handler handler) {
        this.handler = handler;
    }

    public void doWork(ControlMessage ctlMsg, ArrayList<Star> stars,
                       String rawMessageWRK, Channel channelResult, String androidId) throws Exception {

        WorkMessage wrkMsg = new WorkMessage(rawMessageWRK);
        Log.d("fyp", "PARSED WORK MESSAGE:\n" + wrkMsg.toString());
        Utils.tellUI(handler, Enums.UITarget.WRK_HEAD, wrkMsg.getFilename());

        // prepare UI progress bar
        // Number of steps is: (star-count * plane-count * 2) + (star-count)
        // Each plane for each star requires 2 operations: download and clean.
        // Each star also requires an upload operation.
        int progressSteps = stars.size() * wrkMsg.getPlanes() * 2 + stars.size();
        Utils.tellUI(handler, Enums.UITarget.WRK_PROGRESS_MAX, progressSteps);
        Utils.tellUI(handler, Enums.UITarget.WRK_PROGRESS_RESET);

        for (int i = 1; i <= stars.size(); i++) {
            // for each star, clean its box in all planes in the FITS
            Star star = stars.get(i-1);
            Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_1, "Star " + i + " of " + stars.size());

            Timer timer = new Timer();
            timer.start();

            String s3URL = ""; // POSTing an upload returns the S3 destination url

            // Attempt to process this work unit
            try {
                double[][][] resultPixels = cleanBox(ctlMsg, wrkMsg, star);

                Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, "Parsing...");
                String sResultPixels = PixelBox.arrayToString(resultPixels);
                longLogv("fypr", sResultPixels);

                // Send processed pixels to the API Results Server
                Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, "Uploading...");
                FormPoster poster = new FormPoster(ctlMsg.getResult_Server_URL());
                poster.add("action", "uploadCleaned"); // add POST variables
                poster.add("fitsFilename", wrkMsg.getFilename());
                poster.add("starNum", Integer.toString(star.getStarNum()));
                poster.add("planeCount", Integer.toString(wrkMsg.getPlanes()));
                poster.add("images", sResultPixels);
                s3URL = poster.post();
                Log.d("fyp", "RESULTS UPLOADED." + s3URL);

                // Send a message to the result queue
                Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, "Publishing (Success)...");
                ResultMessage msg = new ResultMessage(true,
                        ctlMsg.getDesc(), wrkMsg.getFilename(), wrkMsg.getPlanes(),
                        star.getStarNum(), star.getBox(), timer.stop(), androidId, "", s3URL);
                channelResult.basicPublish("", ctlMsg.getResult_Q_Name(), null, msg.toJSON().getBytes());
                Log.d("fyp", "RESULT MESSAGE SENT.");

                // Star is processed. Clear the UI except for star number.
                Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_2, "");  // clear plane n of n
                Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, "");  // clear current activity
                Utils.tellUI(handler, Enums.UITarget.WRK_PROGRESS_NEXT); // increment progress bar

            } catch (Exception e) {
                String errorMessage = e.getMessage();
                ResultMessage msg;
                try {
                    Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, "Publishing (Fail)...");
                    msg = new ResultMessage(false,
                            ctlMsg.getDesc(), wrkMsg.getFilename(), wrkMsg.getPlanes(),
                            star.getStarNum(), star.getBox(), timer.stop(), androidId, errorMessage, s3URL);
                } catch (Exception e1) {
                    errorMessage += " Also: ResultMessage() Error: "+e1.getMessage();
                    msg = new ResultMessage(false, "", "", 0, 0, "", 0, "", errorMessage, "");
                }
                // send message to Result Queue
                try {
                    channelResult.basicPublish("", ctlMsg.getResult_Q_Name(), null, msg.toJSON().getBytes());
                } catch (IOException e1) {
                    errorMessage += " Also: basicPublish() Error: "+e1.getMessage();
                }

                Log.d("fyp", "FAIL SENT.\nReason: " + errorMessage);
                Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_1, ""); // clear star n of n
                Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_2, ""); // clear plane n of n
                Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, ""); // clear "cleaning..." etc
                Utils.tellUI(handler, Enums.UITarget.ERROR,
                        "FAIL sent for " + wrkMsg.getFilename() + "\nReason:" + errorMessage);

            }
        }
        Utils.tellUI(handler, Enums.UITarget.WRK_HEAD, "");
        Utils.tellUI(handler, Enums.UITarget.WRK_PROGRESS_RESET); // reset progress bar
        Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_1, ""); // clear star n of n
    }



    // For a work message, download and clean a FITS box, using stored Star data
    private double[][][] cleanBox(ControlMessage ctlMsg, WorkMessage wrkMsg, Star star) throws Exception {
        String sMsg; // used for debug and info messages
        double[][][] resultPixels = new double[wrkMsg.getPlanes()][star.getBoxwidth()][star.getBoxwidth()];

        for (int plane=1; plane<=wrkMsg.getPlanes(); plane++) {

            Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_2, "Plane "+plane+" of "+wrkMsg.getPlanes());

            // download the FITS box for this plane
            sMsg = String.format("GETTING %s, STAR %d, PLANE %d...\nX %d, Y %d, Box width %d, %s",
                    wrkMsg.getFilename(), star.getStarNum(), plane, star.getX(), star.getY(), star.getBoxwidth(), star.getBox());
            Log.d("fyp", sMsg);
            Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, "Downloading...");
            FormPoster poster = new FormPoster(ctlMsg.getAPI_Server_URL());
            poster.add("action", "getbox"); // add POST variables
            poster.add("box", star.getBox());
            poster.add("filename", wrkMsg.getFilename());
            poster.add("plane", Integer.toString(plane));
            String fitsResponse = poster.post();

            // populate an array from the returned Fits data
            double[][][] fitsPixels = PixelBox.stringToArray(star.getBoxwidth(), fitsResponse);
            Log.d("fyp", "FITS RECEIVED.");
            longLogv("fyp", PixelBox.arrayToString(fitsPixels, Integer.toString(plane)));

            // update the UI's progress bar
            Utils.tellUI(handler, Enums.UITarget.WRK_PROGRESS_NEXT, null);

            // Perform Calculation on the data
            sMsg = String.format("CLEANING %s, STAR %d, PLANE %d...\nX %d, Y %d, Box width %d, %s",
                    wrkMsg.getFilename(), star.getStarNum(), plane, star.getX(), star.getY(), star.getBoxwidth(), star.getBox());
            Log.d("fyp", sMsg);
            Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, "Cleaning...");

            // insert the processed pixels into the results array.
            // The position in the 1st dimension corresponds to the plane number.
            resultPixels[plane-1] = cleanBoxPlane(fitsPixels, star.getBiasPixels(), star.getFlatPixels(), star.getBoxwidth());
            Log.d("fyp", "FITS CLEANED.");
            longLogv("fyp", PixelBox.arrayToString(resultPixels, Integer.toString(plane)));
            Utils.tellUI(handler, Enums.UITarget.WRK_PROGRESS_NEXT, null);

        }

        Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_2, ""); // clear plane n of n
        Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, ""); // clear current activity

        return resultPixels;

    }

    private double[][] cleanBoxPlane(double[][][] fitsPixels, double[][][] biasPixels, double[][][] flatPixels, int boxWidth) throws Exception {
        // Apply cleaning calculation to a single plane.
        // Ignore 1st dimension of inputs with the source plane
        // because we are called with only one plane at a time.
        // New pixel = (RAW - Bias) / Flat = new pixel

        int x=0, y =0;
        double[][] resultPixels = new double[boxWidth][boxWidth];

        try {

            for (x = 0; x < boxWidth; x++) {
                for (y = 0; y < boxWidth; y++) {
                    resultPixels[x][y] = (fitsPixels[0][x][y] - biasPixels[0][x][y]) / flatPixels[0][x][y];
               }
            }
            return resultPixels;

        } catch (Exception e) {
            throw new Exception(String.format("Error cleaning pixels at pixel x%d y%d: %s",
                    x, y, e.getMessage()));
        }

    }

    private void longLogv(String tag, String str) {
        if(true) return; // disable
        // avoid max length of logcat messages
        if(str.length() > 4000) {
            Log.v(tag, str.substring(0, 4000));
            longLogv(tag, str.substring(4000));
        } else
            Log.v(tag, str);
    }


}
