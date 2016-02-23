package ie.dit.d13122842.main;

public class WorkingData {

    public double[][][] flatPixels;
    public double[][][] biasPixels;
    public double[][][] fitsPixels;
    public double[][][] result;

    public WorkingData(int boxSize) {
        flatPixels = new double[1][boxSize][boxSize];
        biasPixels = new double[1][boxSize][boxSize];
        fitsPixels = new double[1][boxSize][boxSize];
        result = new double[1][boxSize][boxSize];
    }

    public static String toString(double[][][] pixelArray) {

        StringBuilder sb = new StringBuilder();
        for (int p = 0; p < pixelArray.length; p++) {
            for (int x = 0; x < pixelArray[0].length; x++) {
                for (int y = 0; y < pixelArray[0].length; y++) {
                    sb.append(String.format("p%d, x%d, y%d, %.10f\n", p, x, y, pixelArray[p][x][y]));
                }
            }
        }
        return sb.toString();

    }

}