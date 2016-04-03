package ie.dit.d13122842.messages;

import org.json.simple.JSONObject;

public class ResultMessage {
    private boolean success;
    private String activity;
    private String sourceFileName;
    private int planes;
    private int starNumber;
    private String box;
    private long processingTime;
    private String androidId;
    private String errorMessage;
    private String s3url; // e.g. https://s3-us-west-2.amazonaws.com/cleanedfits/cleaned/0000001_2.fits
    private String results;
    private String followingJob;

    public ResultMessage(boolean success, String activity, String sourceFileName,
                         int planes, int starNumber, String box, long processingTime,
                         String androidId, String errorMessage, String s3url,
                         String results, String followingJob) {
        this.success = success;
        this.activity = activity;
        this.sourceFileName = sourceFileName;
        this.planes = planes;
        this.starNumber = starNumber;
        this.box = box;
        this.processingTime = processingTime;
        this.androidId = androidId;
        this.errorMessage = errorMessage;
        this.s3url = s3url;
        this.results = results;
        this.followingJob = followingJob;
    }

    public String toJSON() {
        JSONObject obj=new JSONObject();
        obj.put("success",success);
        obj.put("activity",activity);
        obj.put("sourceFileName",sourceFileName);
        obj.put("planes",planes);
        obj.put("starNumber",starNumber);
        obj.put("box", box);
        obj.put("processingTime",processingTime);
        obj.put("androidId", androidId);
        obj.put("errorMessage",errorMessage);
        obj.put("s3url",s3url);
        obj.put("results",results);
        obj.put("followingJob",followingJob);
        System.out.print(obj);
        return obj.toString();
    }
}