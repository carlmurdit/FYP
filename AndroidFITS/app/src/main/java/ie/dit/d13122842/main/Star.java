package ie.dit.d13122842.main;

public class Star {
    private int starNum = - 1; // 1-based position from the Config
	private int x;
	private int y;
    private int radius;
    private int annulus;
    private int dannulus;
	private int boxwidth;
    private int threshold;
	private String box;
    private double[][][] flatPixels = null;
    private double[][][] biasPixels = null;

	public Star(int starNum, String configLine) throws Exception {

        try {
            this.starNum = starNum;
            String[] parts = configLine.split(" ");
            this.x = Integer.parseInt(parts[0]);
            this.y = Integer.parseInt(parts[1]);
            this.radius = Integer.parseInt(parts[2]);
            this.annulus = Integer.parseInt(parts[3]);
            this.dannulus = Integer.parseInt(parts[4]);
            this.boxwidth = Integer.parseInt(parts[5]);
            this.threshold = Integer.parseInt(parts[6]);
            this.box = makeBox();
        } catch (Exception e) {
            String format = "Error parsing config line '%s' into a Star: %s";
            throw new Exception(String.format(format, configLine, e.getMessage()), e);
        }
	}

    private String makeBox() {
        int x1 = getX() - getBoxwidth() /2;
        int y1 = getY() - getBoxwidth() /2;
        int x2 = getX() + getBoxwidth() /2 -1;
        int y2 = getY() + getBoxwidth() /2 -1;
        // see 5.2 FITS File Access Routines, p 39 of 186 in cfitsio user ref guide
        return String.format("[%d:%d,%d:%d]", x1, x2, y1, y2);
    }

    public int getStarNum() {
        return starNum;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getBoxwidth() {
        return boxwidth;
    }

    public String getBox() {
        return box;
    }

    public double[][][] getFlatPixels() throws Exception {
        if (flatPixels == null)
            throw new Exception("Error: Flat Pixels requested for Star "+starNum+" are null.");
        return flatPixels;
    }

    public void setFlatPixels(double[][][] flatPixels) {
        this.flatPixels = flatPixels;
    }

    public double[][][] getBiasPixels() throws Exception {
        if (biasPixels == null)
            throw new Exception("Error: Bias Pixels requested for Star "+starNum+" are null.");
        return biasPixels;
    }

    public void setBiasPixels(double[][][] biasPixels) {
        this.biasPixels = biasPixels;
    }
}
