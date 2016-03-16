package ie.dit.d13122842;

public class Config {
	
	// Amazon Web Services
	public static class AWS {
		public static final String ACCESS_KEY_ID = "AKIAJQ4SHAXAUY7VLEGQ";
		public static final String SECRET_ACCESS_KEY = "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";
		//for https://s3-eu-west-1.amazonaws.com/astronomydata/AstronomyData/compressedRAW/0000801.fits.fz:
		public static final String SOURCE_BUCKET = "astronomydata";
		public static final String SOURCE_BUCKET_PREFIX = "AstronomyData/compressedRAW/";
	}
	
	// Rabbit Message Queue Broker
	public static class MQ {
		// public static final String HOST = "192.168.3.21";
		public static final String HOST = "147.252.142.204";
		public static final String USER = "test";
		public static final String PASS = "test";
		public static final String CONTROL_QUEUE = "control_queue";
		public static final String WORK_QUEUE = "work_queue";
		public static final String MANAGEMENT_UI = "http://147.252.142.204:15672"; // FYI
	}
	
	// API Server
	public static class API {
		public static final String HOST = "147.252.142.202";
	}
	
	// Result Server
	public static class Result {
		public static final String HOST = "147.252.142.202";
	}
}
