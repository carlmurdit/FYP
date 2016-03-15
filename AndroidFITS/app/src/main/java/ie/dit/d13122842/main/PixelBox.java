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
                        returnArray[p][x][y] = Double.parseDouble(reader.readLine().split(" ")[3]);
                        lineCount++;
                    }
                }
            }
//            Log.d("fyp", "stringToArray():\n" + postResponse +"\nlineCount "+lineCount);
//            Log.d("fyp", "\nEnd of stringToArray()");
            return returnArray;

        } catch (Exception e) {
            String sMsg = String.format("Error in stringToArray at pixel p%d x%d y%d: %s. String:\n'%s'",
                    p, x, y, e.getMessage(), postResponse);
            Log.e("fyp", sMsg, e);
            throw new Exception(sMsg, e);

        }
    }

    public static String arrayToString(double[][][] pixelArray, String planeLabel) {

        StringBuilder sb = new StringBuilder();
        for (int p = 0; p < pixelArray.length; p++) {
            for (int x = 0; x < pixelArray[0].length; x++) {
                for (int y = 0; y < pixelArray[0].length; y++) {
                    sb.append(String.format("p%s, x%02d, y%02d, %16.10f\n", planeLabel, x, y, pixelArray[p][x][y]));
                }
            }
        }
        return sb.toString();

    }

    public static String arrayToString(double[][][] pixelArray) {

        StringBuilder sb = new StringBuilder();
        for (int p = 0; p < pixelArray.length; p++) {
            for (int x = 0; x < pixelArray[0].length; x++) {
                for (int y = 0; y < pixelArray[0].length; y++) {
                    sb.append(String.format("p%s, x%02d, y%02d, %16.10f\n", p, x, y, pixelArray[p][x][y]));
                }
            }
        }
        return sb.toString();

    }


}
