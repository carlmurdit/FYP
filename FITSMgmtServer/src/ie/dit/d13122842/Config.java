package ie.dit.d13122842;

public class Config {
	
	public static class AWS {
		public static final String ACCESS_KEY_ID = "AKIAJQ4SHAXAUY7VLEGQ";
		public static final String SECRET_ACCESS_KEY = "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";
		//for https://s3-eu-west-1.amazonaws.com/astronomydata/AstronomyData/compressedRAW/0000801.fits.fz:
		public static final String SOURCE_BUCKET = "astronomydata";
		public static final String SOURCE_BUCKET_PREFIX = "AstronomyData/compressedRAW/";
	}
	
	public static class RabbitMQ {
		// public static final String HOST = "192.168.3.21";
		public static final String HOST = "192.168.43.82";
		public static final String USER = "test";
		public static final String PASS = "test";
		public static final String CONTROL_QUEUE = "control_queue";
		public static final String WORK_QUEUE = "work_queue";
		public static final String MANAGEMENT_UI = "http://192.168.3.21:15672"; // FYI
	}
	
}
