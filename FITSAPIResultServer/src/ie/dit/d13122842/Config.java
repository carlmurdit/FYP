package ie.dit.d13122842;

public class Config {

	public static final String RESULTSDIR = 
			"/Users/carl/Documents/git/fyp/FITSAPIResultServer/Results/";
	
	// AWS S3 destination for cleaned FITS files
	public static class AWS_Cleaned {
		public static final String ACCESS_KEY_ID = "AKIAIDSX4ACXMUJFYCGQ";
		public static final String SECRET_ACCESS_KEY = "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";
		//for cleanedfits.s3-website-us-west-2.amazonaws.com
		public static final String BUCKET = "cleanedfits";
		public static final String SOURCE_BUCKET_PREFIX = "";
	}
	
	// Rabbit Message Queue Broker
	public static class MQ {
		// Management UI = "http://192.168.3.21:15672"; 
		public static final String QUEUE_URI = "amqp://test:test@192.168.3.21:5672";
		public static final String CONTROL_QUEUE = "control_queue";
		public static final String MAGNITUDE_WORK_QUEUE = "magnitude_work";
	}
	
	// API Server
	public static class API {
		public static final String SERVER_URL = "http://192.168.3.13:8080/FITSAPIServer/MainServlet";
	}
	
	// Result Server
	public static class Result {
		public static final String SERVER_URL = "http://192.168.3.13:8080/FITSAPIResultServer/MainServlet";
	}
}
