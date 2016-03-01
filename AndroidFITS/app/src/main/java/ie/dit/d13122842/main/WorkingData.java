package ie.dit.d13122842.main;

public class WorkingData {

    public double[][][] fitsPixels;
    public double[][][] result;
    public int boxsize;

    public WorkingData(int boxSize) {
        this.boxsize = boxSize;
        fitsPixels = new double[1][boxSize][boxSize];
        result = new double[1][boxSize][boxSize];
    }

}
