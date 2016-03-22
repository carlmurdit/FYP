package ie.dit.d13122842.messages;

import org.json.simple.JSONObject;

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

    public ResultMessage(boolean success, String activity, String filename,
                         int planes, int starNumber, String box, long processingTime,
                         String androidId, String errorMessage) {
        this.success = success;
        this.activity = activity;
        this.filename = filename;
        this.planes = planes;
        this.starNumber = starNumber;
        this.box = box;
        this.processingTime = processingTime;
        this.androidId = androidId;
        this.errorMessage = errorMessage;
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

    public String toJSON() {
        JSONObject obj=new JSONObject();
        obj.put("success",success);
        obj.put("activity",activity);
        obj.put("filename",filename);
        obj.put("planes",planes);
        obj.put("starNumber",starNumber);
        obj.put("box", box);
        obj.put("processingTime",processingTime);
        obj.put("androidId", androidId);
        obj.put("errorMessage",errorMessage);
        System.out.print(obj);
        return obj.toString();
    }
}