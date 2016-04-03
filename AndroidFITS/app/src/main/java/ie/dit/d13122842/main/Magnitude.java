package ie.dit.d13122842.main;

import android.os.Handler;
import android.util.Log;

import com.rabbitmq.client.Channel;

import java.util.Arrays;

import ie.dit.d13122842.exception.ResultPublicationException;
import ie.dit.d13122842.exception.WorkException;
import ie.dit.d13122842.messages.ControlMessage;
import ie.dit.d13122842.messages.ResultMessage;
import ie.dit.d13122842.messages.WorkMessage;
import ie.dit.d13122842.posting.FormPoster;
import ie.dit.d13122842.utils.Timer;
import ie.dit.d13122842.utils.Utils;

public class Magnitude {
    private final Handler handler;

    public Magnitude(Handler handler) {
        this.handler = handler;
    }

    public void doWork(ControlMessage ctlMsg, Stars stars,
                       String rawMessageWRK, Channel channelResult, String androidId)
            throws WorkException, ResultPublicationException {

        WorkMessage wrkMsg;
        try {
            wrkMsg = new WorkMessage(rawMessageWRK);
        } catch (Exception e) {
            throw new WorkException(e);
        }
        Log.d("fyp", "PARSED WORK MESSAGE:\n" + wrkMsg.toString());
        Utils.tellUI(handler, Enums.UITarget.WRK_HEAD, wrkMsg.getFilename());

        // prepare UI progress bar
        // Number of steps is a download, 1 step per planes
        // Each star also requires an upload operation.
        int progressSteps = 1 + wrkMsg.getPlanes();
        Utils.tellUI(handler, Enums.UITarget.WRK_PROGRESS_MAX, progressSteps);
        Utils.tellUI(handler, Enums.UITarget.WRK_PROGRESS_RESET);

        // For the work filename, get the corresponding star (from the config file)
        int starNum; Star star;
        try {
            if (stars == null ) {
                Log.d("fyp", "stars == null");
            }
            String[] parts = wrkMsg.getFilename().split("_");
            Log.d("fyp", "File Parts: " + parts[0] + " " + parts[1]);
            Log.d("fyp", "File StarPart: "+parts[1].substring(0,1));
            starNum = Integer.parseInt(parts[1].substring(0,1));

            // Log.d("fyp", "starNum = "+starNum+", wrkMsg.getFilename() = "+wrkMsg.getFilename());
            star = stars.get(starNum - 1);
            Log.d("fyp", "starNum = "+star.getStarNum());
        } catch (Exception e) {
            throw new WorkException
                    ("Error looking up star for filename "+wrkMsg.getFilename()+" "+e.getMessage());
        }

        Timer timer = new Timer();
        timer.start();

        // Attempt to process this work unit
        try {

            double[] magnitudes = getMagnitudes(ctlMsg, wrkMsg, star);

            // Send the magnitudes to the Result Queue
            Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, "Sending Result (Success)...");
            ResultMessage msg = new ResultMessage(true,
                    ctlMsg.getDesc(), wrkMsg.getFilename(), wrkMsg.getPlanes(),
                    star.getStarNum(), star.getBox(), timer.stop(), androidId, "", "",
                    Arrays.toString(magnitudes), ctlMsg.getFollowingJob());
            try {
                channelResult.basicPublish("", ctlMsg.getResult_Q_Name(), null, msg.toJSON().getBytes());
            } catch (Exception e) {
                throw new ResultPublicationException(e); // tell calling class to reset the channel
            }

            Log.d("fyp", "SUCCESS RESULT SENT.");

            Utils.tellUI(handler, Enums.UITarget.WRK_PROGRESS_RESET); // reset progress bar
            Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, ""); // Clear Sending Result...

        } catch (Exception e) {

            String errorMessage = e.getMessage();

            Utils.tellUI(handler, Enums.UITarget.WRK_PROGRESS_RESET); // reset progress bar
            Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, "Sending result (Fail)...");

            ResultMessage msg;
            try {
                msg = new ResultMessage(false,
                        ctlMsg.getDesc(), wrkMsg.getFilename(), wrkMsg.getPlanes(),
                        star.getStarNum(), star.getBox(), timer.stop(), androidId,
                        errorMessage, "", "", ctlMsg.getFollowingJob());
            } catch (Exception e1) {
                // something was null or invalid
                errorMessage += " Also: ResultMessage() Error: "+e1.getMessage();
                msg = new ResultMessage(false, "", "", 0, 0, "", 0, "", errorMessage, "", "", "");
            }
            // send message to Result Queue
            try {
                channelResult.basicPublish("", ctlMsg.getResult_Q_Name(), null, msg.toJSON().getBytes());
                Log.d("fyp", "FAIL SENT.\nReason: " + errorMessage);
            } catch (Exception e1) {
                throw new ResultPublicationException(e1); // tell calling class to reset the channel
            }

            Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, ""); // Clear Sending Result...

            // tell calling class to reject the message
            throw new WorkException
                    ("FAIL sent for " + wrkMsg.getFilename() + "\nReason:" + errorMessage);

        }

    }

    // For a work message, download the file (e.g. 0000005_2.fits is the second star from FITS 0000005.fits)
    // Calculate the magnitude for each plane in the file
    private double[] getMagnitudes(ControlMessage ctlMsg, WorkMessage wrkMsg, Star star) throws Exception {
        String sMsg; // used for debug and info messages
        double[] resultMags = new double[wrkMsg.getPlanes()];

        // download the cleaned FITS file (all planes)
        sMsg = String.format("MAG: GETTING %s, STAR %d...\n\tX %d, Y %d, Box width %d, %s",
                wrkMsg.getFilename(), star.getStarNum(), star.getX(), star.getY(), star.getBoxwidth(), star.getBox());
        Log.d("fyp", sMsg);
        Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, "Downloading...");
        FormPoster poster = new FormPoster(ctlMsg.getAPI_Server_URL());
        poster.add("action", "getClean"); // add POST variables
        poster.add("filename", wrkMsg.getFilename());
        String cleanPixelsString = poster.post();

        // populate an array from the returned Fits data
        double[][][]cleanPixels = PixelBox.stringToArray(star.getBoxwidth(), cleanPixelsString);
        Log.d("fyp", "CLEAN PIXELS RECEIVED.");

        Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, "Calculating Magnitude...");
        for (int plane=1; plane<=wrkMsg.getPlanes(); plane++) {

            Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_2, "Plane " + plane + " of " + wrkMsg.getPlanes());

            // update the UI's progress bar
            Utils.tellUI(handler, Enums.UITarget.WRK_PROGRESS_NEXT, null);

            // insert the magnitude results into the results array.
            // The position in the array corresponds to the plane number.
            resultMags[plane-1] = getMagnitude(cleanPixels, star.getBoxwidth(), star.getThreshold());
            Utils.tellUI(handler, Enums.UITarget.WRK_PROGRESS_NEXT, null);

        }

        Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_2, ""); // clear plane n of n
        Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_3, ""); // clear current activity

        return resultMags;

    }

    private double getMagnitude(
            double[][][] pixels, int boxWidth, double threshold) throws Exception {
        // Apply magnitude calculation to a single plane.
        // Ignore 1st dimension because we are called with one plane at a time.

        double result = 0.0d;

        int x=0, y =0;
        double sumStar = 0.0d;
        double sumNonStar = 0.0d;
        int countStar = 0;
        int countNonStar = 0;
        double v = 0.0d;

        /*
        1.	The star luminosity is calculated. This is the sum of the pixels that are part of
        the star (that exceed the threshold).
        2.	The average sky background luminosity is calculated. This is the average of the
        pixels that are not part of the star (are short of the threshold).
        3.	Star luminosity that is due to sky background is removed. This is the count of the
        pixels that are part of the star multiplied by the average sky background luminosity.
        4.	The remaining star luminosity is the magnitude.
        */

        try {

            for (x = 0; x < boxWidth; x++) {
                for (y = 0; y < boxWidth; y++) {
                    v = pixels[0][x][y];
                    if (v >= threshold) {
                        sumStar += v;
                        countStar ++;
                    } else {
                        sumNonStar += v;
                        countNonStar ++;
                    }
                }
            }

            double avgNonStar = sumNonStar / countNonStar;
            double starBackground = countStar * avgNonStar;
            return sumStar - starBackground;

        } catch (Exception e) {
            throw new Exception(String.format("Error cleaning pixels at pixel x%d y%d: %s",
                    x, y, e.getMessage()));
        }

    }
}