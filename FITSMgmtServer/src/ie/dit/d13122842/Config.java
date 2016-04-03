package ie.dit.d13122842;
import com.amazonaws.regions.Regions;

public class Config {
	
	// FITS files (raw and processed) on Amazon Web Services
	public static class aws {
		public static class raw {
			public static final Regions REGIONS = Regions.EU_WEST_1; 
			public static final String ENDPOINT = "https://s3-eu-west-1.amazonaws.com/";
			// Credentials for 'carl' user, created by Paul
			public static final String ACCESS_KEY_ID = "AKIAJQ4SHAXAUY7VLEGQ";
			public static final String SECRET_ACCESS_KEY = "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";
			public static final String BUCKET = "astronomydata";
			public static final String BUCKET_PREFIX = "AstronomyData/compressedRAW/";
		}
		public static class clean {
			public static final Regions REGIONS = Regions.US_WEST_2;
			public static final String ENDPOINT = "https://s3-us-west-2.amazonaws.com/";
			// Credentials for 'fitsuser' user, created by Carl
			public static final String ACCESS_KEY_ID = "AKIAIDSX4ACXMUJFYCGQ";
			public static final String SECRET_ACCESS_KEY = "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";
			public static final String BUCKET = "cleanedfits";
			public static final String BUCKET_PREFIX = "cleaned/";			
		}
	}
	
	// Rabbit Message Queue Broker
	public static class MQ {
		// Management UI: http://192.168.3.21:15672
		public static final String HOST = "192.168.3.21";
		public static final String USER = "test";
		public static final String PASS = "test";
		public static final String QUEUE_URL =  "amqp://test:test@<c:out value="+HOST+"/>:5672";
		public static final String ACTIVATION_QUEUE = "activation_queue";
		public static final String CLEANING_WORK_QUEUE = "cleaning_work_queue";
		public static final String CLEANING_RESULT_QUEUE = "cleaning_result_queue";
		public static final String MAGNITUDE_WORK_QUEUE = "magnitude_work_queue";
		public static final String MAGNITUDE_RESULT_QUEUE = "magnitude_result_queue";
	}
	
	// API Server
	public static class API {
		public static final String HOST = "192.168.3.18";
	}
	
	// Result Server
	public static class Result {
		public static final String HOST = "192.168.3.18";
	}
	
	public static class Dirs {
		public static final String RESULT_HISTORY = "/Users/carl/Documents/git/fyp/FITSMgmtServer/temp/";
	}
}
