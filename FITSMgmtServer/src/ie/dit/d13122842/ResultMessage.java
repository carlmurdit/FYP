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
        	
        	System.out.println();
        	init(obj.get("success"),        obj.get("activity"),   obj.get("filename"),
        		 obj.get("planes"),         obj.get("starNumber"), obj.get("box"),
        		 obj.get("processingTime"), obj.get("androidId"),  obj.get("errorMessage"));
                  
        } catch (Exception e) {
            throw new Exception("Error creating ResultMessage from JSON.\n" + e.getMessage() + "\n" + jsonString);
        } 	
    }

	private void init(Object success, Object activity, Object filename,
			Object planes, Object starNumber, Object box, Object processingTime,
			Object androidId, Object errorMessage) {
        this.success = (boolean) success;
        this.activity = (String) activity;
        this.filename = (String) filename;
        this.planes = (int)(long)planes;
        this.starNumber = (int)(long) starNumber;
        this.box = (String) box;
        this.processingTime = (long) processingTime;
        this.androidId = (String) androidId;
        this.errorMessage = (String) errorMessage;
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
}