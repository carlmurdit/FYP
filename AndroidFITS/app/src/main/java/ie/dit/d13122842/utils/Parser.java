package ie.dit.d13122842.utils;

//import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.StringReader;

public class Parser {







    public void parsePixels(String postResponse, double[][][] pixelArray) throws Exception {

        int p=0, x=0, y =0;
        int boxSide = pixelArray[0].length;
        String line = "";
        BufferedReader reader = new BufferedReader(new StringReader(postResponse));

        try {

            // a line looks like 'p1 r11 c3 0.9914176097'
            for (p = 0; p < pixelArray.length; p++) {
                for (x = 0; x < boxSide; x++) {
                    for (y = 0; y < boxSide; y++) {
                        pixelArray[p][x][y] = Double.parseDouble(reader.readLine().split(" ")[3]);
                    }
                }
            }

        } catch (Exception e) {
            throw new Exception(String.format("Error in parsePixels at pixel p%d x%d y%d from string: %s",
                    p, x, y, e.getMessage()));
        }
    }

}
