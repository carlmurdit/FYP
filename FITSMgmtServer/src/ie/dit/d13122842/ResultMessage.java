package ie.dit.d13122842;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ResultMessage {
    private boolean success;
    private String activity;
    private String filename;
    private int planes;
    private int starNumber;
    private String box;
    private long processingTime;
    private String androidId;
    private String errorMessage;
    private String s3url; // e.g. https://s3-us-west-2.amazonaws.com/cleanedfits/cleaned/0000001_2.fits
    
    public ResultMessage(String jsonString) throws Exception {
    	
    	System.out.println("ResultMessage... 1");
    	
        // https://code.google.com/archive/p/json-simple
        JSONParser parser = new JSONParser();
        JSONObject obj;
        try {
            obj = (JSONObject) parser.parse(jsonString);
            System.out.println("ResultMessage jsonString: "+ obj.toJSONString() + "'");
        } catch (ParseException e) {
            throw new Exception("Error parsing JSON of the result message.\n" + e.getMessage() + "\n" + jsonString);
        }   

        try {        	
	        this.success = (boolean) obj.get("success");
	        this.activity = (String) obj.get("activity");
	        this.filename = (String) obj.get("filename");
	        this.planes = (int)(long) obj.get("planes");
	        this.starNumber = (int)(long) obj.get("starNumber");
	        this.box = (String) obj.get("box");
	        this.processingTime = (long) obj.get("processingTime");
	        this.androidId = (String) obj.get("androidId");
	        this.errorMessage = (String) obj.get("errorMessage");
	        this.s3url = "<a href=\"" +(String) obj.get("s3url")+"\">"+obj.get("s3url")+"</a>";	        
	    } catch (Exception e) {
	        throw new Exception("Error creating ResultMessage from JSON.\n" + e.getMessage() + "\n" + jsonString);
	    } 	
        
    }

    public boolean isSuccess() {
        return success;
    }

    public String getActivity() {
        return activity;
    }

    public String getFilename() {
        return filename;
    }

    public int getPlanes() {
        return planes;
    }

    public int getStarNumber() {
        return starNumber;
    }

    public String getBox() {
        return box;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public String getAndroidId() {
        return androidId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getTooltip() {
    	return getFilename()+", star "+getStarNumber();
    }

	public String getS3url() {
		return s3url;
	}

	public void setS3url(String s3url) {
		this.s3url = s3url;
	}
}
