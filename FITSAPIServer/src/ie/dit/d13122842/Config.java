package ie.dit.d13122842;

import com.amazonaws.regions.Regions;

public class Config {
	
	public static final String FITSDIR = 
			"/Users/carl/Documents/git/fyp/FITSAPIServer/WebContent/fitsdir/";
	
	// FITS files (raw and processed) on Amazon Web Services
	public static class aws {
		public static class raw {
			public static String URL() {
				return ENDPOINT+BUCKET+"/"+BUCKET_PREFIX;
			}
			public static final Regions REGIONS = Regions.EU_WEST_1;
			public static final String ENDPOINT = "https://s3-eu-west-1.amazonaws.com/";
			public static final String ACCESS_KEY_ID = "AKIAJQ4SHAXAUY7VLEGQ";
			public static final String SECRET_ACCESS_KEY = "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";
			public static final String BUCKET = "astronomydata";
			public static final String BUCKET_PREFIX = "AstronomyData/compressedRAW/";
		}
		public static class clean {
			public static final Regions REGIONS = Regions.US_WEST_2;
			public static final String ENDPOINT = "https://s3-us-west-2.amazonaws.com/";
			public static final String ACCESS_KEY_ID = "AKIAIDSX4ACXMUJFYCGQ";
			public static final String SECRET_ACCESS_KEY = "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";
			public static final String BUCKET = "cleanedfits";
			public static final String BUCKET_PREFIX = "cleaned/";			
		}
	}
	
	public static class app {
		public static final String FITS_SUBSET = "/Users/carl/Documents/git/fyp/FITS_C/src/bin/fits_subset";
		public static final String FUNPACK = "/Users/carl/Documents/git/fyp/FITS_C/src/bin/funpack";
	}
	
}
