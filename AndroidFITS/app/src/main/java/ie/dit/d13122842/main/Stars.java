package ie.dit.d13122842.main;


import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import ie.dit.d13122842.messages.ControlMessage;
import ie.dit.d13122842.posting.FormPoster;
import ie.dit.d13122842.utils.Utils;

public class Stars {
    private ArrayList<Star> stars = new ArrayList<>();

    public void populateFromConfig(Handler handler, ControlMessage ctlMsg) throws Exception {
        // Populate with the lines in Config

        String sMsg; // used for debug and info messages

        // Download the config (star list) file
        sMsg = "Downloading " + ctlMsg.getConfig_Filename() + "...";
        Log.d("fyp", sMsg);
        Utils.tellUI(handler, Enums.UITarget.ACT_STATUS, sMsg);

        FormPoster poster = new FormPoster(ctlMsg.getAPI_Server_URL());
        poster.add("action", "getfile");  // set POST variables
        poster.add("filename", ctlMsg.getConfig_Filename());
        String configContents = poster.post();

        // parse the contents into stars
        this.stars = new ArrayList<Star>();
        String[] lines = configContents.split("\n");
        if (lines.length == 0)
            throw new Exception("The Config does not contain any stars: " + configContents);
        int lineNum = 1;
        for (String line : lines) {
            if (!line.startsWith("!") && line.trim().length() > 0) {
                stars.add(new Star(lineNum, line));
                lineNum++;
            }
        }

        // show debug message
        sMsg = "CONFIG Received:\n";
        for (int i = 0; i < stars.size(); i++) {
            Star star = stars.get(i);
            sMsg += String.format("\tStar %d: x%d, y%d, boxwidth %d\n",
                    i, star.getX(), star.getY(), star.getBoxwidth());
        }
        Log.d("fyp", sMsg);

        /*
        // Compare previously downloaded Config (if found) to this download
        if (this.stars != null && this.stars.size() == stars.size()) {
            boolean differs = false;
            for (int i=0; i<stars.size(); i++) {
                Star star = stars.get(i);
                Star oldStar = this.stars.get(i);
                if (star.getBox().compareTo(oldStar.getBox()) != 0) {
                    // difference found, so re-download flat and bias
                    differs = true;
                    break;
                }
            }
            // all same so no need to re-download
            if (!differs) return this.stars;
        }
        */

    }

    public void populateWithBiasAndFlat(Handler handler, ControlMessage ctlMsg) throws Exception {
        String sMsg;

        if (stars == null || stars.size() == 0) {
            Log.e("fyp", "Error in populateWithBiasAndFlat() - stars was empty.");
            populateFromConfig(handler, ctlMsg);
        }

        // Update each Star with flat and bias boxes from the server
        for (int i = 0; i < stars.size(); i++) {
            Star star = stars.get(i);

            // Get the box around this star from the Flat file
            sMsg = String.format("GETTING FLAT, STAR %d...\nX %d, Y %d, Box width %d, %s from %s...",
                    i + 1, star.getX(), star.getY(), star.getBoxwidth(), star.getBox(), ctlMsg.getFlat_Filename());
            Log.d("fyp", sMsg);
            Utils.tellUI(handler, Enums.UITarget.ACT_STATUS, "Downloading " + ctlMsg.getFlat_Filename() + "...");
            FormPoster poster = new FormPoster(ctlMsg.getAPI_Server_URL());
            poster.add("action", "getbox");  // add POST variables
            poster.add("box", star.getBox());
            poster.add("filename", ctlMsg.getFlat_Filename());
            poster.add("plane", "1");
            String flatResponse = poster.post();

            // populate star's Flat array from the returned data
            star.setFlatPixels(PixelBox.stringToArray(star.getBoxwidth(), flatResponse));
            Log.d("fyp", "FLAT RECEIVED.");
            Utils.longLogV("fyp", PixelBox.arrayToString(star.getFlatPixels(), "-"), false);

            // repeat for Bias
            sMsg = String.format("GETTING BIAS, STAR %d:\nX %d, Y %d, Box width %d, %s from %s...",
                    i + 1, star.getX(), star.getY(), star.getBoxwidth(), star.getBox(), ctlMsg.getBias_Filename());
            Log.d("fyp", sMsg);
            Utils.tellUI(handler, Enums.UITarget.ACT_STATUS, "Downloading " + ctlMsg.getBias_Filename() + "...");
            poster = new FormPoster(ctlMsg.getAPI_Server_URL());
            poster.add("action", "getbox");
            poster.add("box", star.getBox());
            poster.add("filename", ctlMsg.getBias_Filename()); // add POST variables
            poster.add("plane", "1");
            String biasResponse = poster.post();

            // populate star's Bias array from the returned data
            star.setBiasPixels(PixelBox.stringToArray(star.getBoxwidth(), biasResponse));
            Log.d("fyp", "BIAS RECEIVED.");
            Utils.longLogV("fyp", PixelBox.arrayToString(star.getBiasPixels(), "-"), false);

        }
    }

    public Star get(int i) {
        return this.stars.get(i);
    }

    public int size() {
        return this.stars.size();
    }
}
