package ie.dit.d13122842.main;

import android.util.Log;

import java.io.BufferedReader;
import java.io.StringReader;

public class PixelBox {

    public static double[][][] stringToArray(int boxWidth, String postResponse) throws Exception {
        int p=0, x=0, y =0;
        String line = "";
        BufferedReader reader = new BufferedReader(new StringReader(postResponse));
        double[][][] returnArray;
        int lineCount = 0;

        if (postResponse == null || postResponse == "")
            throw new Exception("stringToArray() called with an empty string.");

        try {
            returnArray = new double[1][boxWidth][boxWidth];

            // a line looks like 'p1 r11 c3 0.9914176097'
            for (p = 0; p < returnArray.length; p++) {
                for (x = 0; x < boxWidth; x++) {
                    for (y = 0; y < boxWidth; y++) {
                        lineCount++;
                        line = reader.readLine();
                        returnArray[p][x][y] = Double.parseDouble(line.split("\\s+")[3]);
                    }
                }
            }
//            Log.d("fyp", "stringToArray():\n" + postResponse +"\nlineCount "+lineCount);
//            Log.d("fyp", "\nEnd of stringToArray()");
            return returnArray;

        } catch (Exception e) {
            String sMsg = String.format("Error in stringToArray at " +
                            "line No. %d, pixel p%d x%d y%d, %s: %s\n'%s'",
                        lineCount, p, x, y, line, e.getMessage());
            Log.e("fyp", sMsg, e);
            throw new Exception(sMsg, e);

        }
    }

}
