package ie.dit.d13122842;

public class Config {

	public static final String RESULTSDIR = 
			"/Users/carl/Documents/git/fyp/FITSAPIResultServer/Results/";
	
	// AWS S3 destination for cleaned FITS files
	public static class AWS_Cleaned {
		public static final String ENDPOINT = "https://s3-us-west-2.amazonaws.com/";
		public static final String ACCESS_KEY_ID = "AKIAIDSX4ACXMUJFYCGQ";
		public static final String SECRET_ACCESS_KEY = "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";
		//for cleanedfits.s3-website-us-west-2.amazonaws.com
		public static final String BUCKET = "cleanedfits";
		public static final String BUCKET_PREFIX = "cleaned";
	}
	
	// Rabbit Message Queue Broker
	public static class MQ {
		// Management UI = http://192.168.3.21:15672; 
		public static final String HOST = "192.168.3.21";
		public static final String QUEUE_URL =  "amqp://test:test@"+HOST+":5672";
		public static final String ACTIVATION_QUEUE = "activation_queue";
		public static final String CLEANING_WORK_QUEUE = "cleaning_work_queue";
		public static final String CLEANING_RESULT_QUEUE = "cleaning_result_queue";
		public static final String MAGNITUDE_WORK_QUEUE = "magnitude_work_queue";
		public static final String MAGNITUDE_RESULT_QUEUE = "magnitude_result_queue";
	}
	
	// API Server
	public static class API {
		// used for posting magnitude jobs
		public static final String SERVER_URL = "http://192.168.3.18:8080/FITSAPIServer/MainServlet";
	}
	
	// Result Server
	public static class Result {
		// used for posting magnitude jobs
		public static final String SERVER_URL = "http://192.168.3.18:8080/FITSAPIResultServer/MainServlet";
	}
}
