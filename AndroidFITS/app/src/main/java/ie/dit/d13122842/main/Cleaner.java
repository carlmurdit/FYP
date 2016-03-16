package ie.dit.d13122842.main;

import android.os.Handler;
import android.util.Log;

import com.rabbitmq.client.Channel;

import java.util.ArrayList;

import ie.dit.d13122842.config.Config;
import ie.dit.d13122842.messages.ControlMessage;
import ie.dit.d13122842.messages.WorkMessage;
import ie.dit.d13122842.posting.FormPoster;
import ie.dit.d13122842.utils.Utils;

public class Cleaner {
    private final Handler handler;

    public Cleaner(Handler handler) {
        this.handler = handler;
    }

    public void doWork(ControlMessage ctlMsg, ArrayList<Star> stars, String rawMessageWRK, Channel channelRLT) throws Exception {

        WorkMessage wrkMsg = new WorkMessage(rawMessageWRK);
        Log.d("fyp", "PARSED WORK MESSAGE:\n" + wrkMsg.toString());
        Utils.tellUI(handler, "RECEIVED WORK:\n" + wrkMsg.toString() + "\n");

        for (Star star : stars) {
            // for each star, clean its box in all planes in the FITS

            // Attempt to process this work unit
            String resultMessage ="SENDING RESULT...";
            try {
                double[][][] resultPixels = cleanBox(ctlMsg, wrkMsg, star);

                String sResultPixels = PixelBox.arrayToString(resultPixels);
                longLogv("fypr", sResultPixels);

                // Send processed pixels to API Server 2
                FormPoster poster = new FormPoster(ctlMsg.getResult_Server_URL());
                poster.add("action", "upload"); // add POST variables
                poster.add("fitsFilename", wrkMsg.getFilename());
                poster.add("starNum", Integer.toString(star.getStarNum()));
                poster.add("planeCount", Integer.toString(wrkMsg.getPlanes()));
                poster.add("images", sResultPixels);
                String postResponse = poster.post();
                Log.d("fyp", "RESULTS UPLOADED." + postResponse);
                Utils.tellUI(handler, "RESULTS UPLOADED." + postResponse + "\n");

                // Send a message to the result queue
                resultMessage = String.format(
                        "Cleaned %s, %d Planes, Star %d, X %d, Y %d, Box width %d, %s.",
                        wrkMsg.getFilename(), wrkMsg.getPlanes(), star.getStarNum(), star.getX(), star.getY(),
                        star.getBoxwidth(), star.getBox());
                channelRLT.basicPublish("", Config.MQ.QUEUE_NAME_RLT, null, resultMessage.getBytes());
                Log.d("fyp", "RESULT MESSAGE SENT.");
                Utils.tellUI(handler, "RESULT MESSAGE SENT.\n");
            } catch (Exception e) {
                resultMessage = String.format(
                        "FAIL:\n" +
                                "%s, %d Planes, Star %d, X %d, Y %d, Box width %d, %s\nERR: %s.\n",
                        wrkMsg.getFilename(), wrkMsg.getPlanes(), star.getStarNum(), star.getX(), star.getY(),
                        star.getBoxwidth(), star.getBox(), e.getMessage());
                channelRLT.basicPublish("", Config.MQ.QUEUE_NAME_RLT, null, resultMessage.getBytes());
                Log.d("fyp", "FAIL SENT.\nReason" + e.getMessage());
                Utils.tellUI(handler, "FAIL SENT.\nReason:" + e.getMessage() + "\n");
            }
        }
    }



    // For a work message, download and clean a FITS box, using stored Star data
    private double[][][] cleanBox(ControlMessage ctlMsg, WorkMessage wrkMsg, Star star) throws Exception {
        String sMsg; // used for debug and info messages
        double[][][] resultPixels = new double[wrkMsg.getPlanes()][star.getBoxwidth()][star.getBoxwidth()];

        for (int plane=1; plane<=wrkMsg.getPlanes(); plane++) {

            // download the FITS box for this plane
            sMsg = String.format("GETTING %s, STAR %d, PLANE %d...\nX %d, Y %d, Box width %d, %s",
                    wrkMsg.getFilename(), star.getStarNum(), plane, star.getX(), star.getY(), star.getBoxwidth(), star.getBox());
            Log.d("fyp", sMsg);
            Utils.tellUI(handler, sMsg);
            FormPoster poster = new FormPoster(ctlMsg.getAPI_Server_URL());
            poster.add("action", "getbox"); // add POST variables
            poster.add("box", star.getBox());
            poster.add("filename", wrkMsg.getFilename());
            poster.add("plane", Integer.toString(plane));
            String fitsResponse = poster.post();

            // populate an array from the returned Fits data
            double[][][] fitsPixels = PixelBox.stringToArray(star.getBoxwidth(), fitsResponse);
            Utils.tellUI(handler, "FITS RECEIVED.\n");
            Log.d("fyp", "FITS RECEIVED.");
            longLogv("fyp", PixelBox.arrayToString(fitsPixels, Integer.toString(plane)));

            // Perform Calculation on the data
            sMsg = String.format("CLEANING %s, STAR %d, PLANE %d...\nX %d, Y %d, Box width %d, %s",
                    wrkMsg.getFilename(), star.getStarNum(), plane, star.getX(), star.getY(), star.getBoxwidth(), star.getBox());
            Log.d("fyp", sMsg);
            Utils.tellUI(handler, sMsg);

            // insert the processed pixels into the results array.
            // The position in the 1st dimension corresponds to the plane number.
            resultPixels[plane-1] = crunchPixels(fitsPixels, star.getBiasPixels(), star.getFlatPixels(), star.getBoxwidth());
            Utils.tellUI(handler, "FITS CLEANED.\n");
            Log.d("fyp", "FITS CLEANED.");
            longLogv("fyp", PixelBox.arrayToString(resultPixels, Integer.toString(plane)));

        }

        return resultPixels;

    }

    private double[][] crunchPixels(double[][][] fitsPixels, double[][][] biasPixels, double[][][] flatPixels, int boxWidth) throws Exception {
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
                    // Log.d("fyp", String.format("Crunching: p%d, x%d, y%d, %.10f", p,x,y,resultPixels[p][x][y]));
                }
            }
            return resultPixels;

        } catch (Exception e) {
            throw new Exception(String.format("Error cleaning pixels at pixel x%d y%d: %s",
                    x, y, e.getMessage()));
        }

    }

    public void longLogv(String tag, String str) {
        if(true) return; // disable
        // avoid max length of logcat messages
        if(str.length() > 4000) {
            Log.v(tag, str.substring(0, 4000));
            longLogv(tag, str.substring(4000));
        } else
            Log.v(tag, str);
    }


}
