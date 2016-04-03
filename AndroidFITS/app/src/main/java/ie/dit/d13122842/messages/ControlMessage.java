package ie.dit.d13122842.messages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ControlMessage {

    private static class Fields {
        public static final String ACT_ID = "Act ID";
        public static final String DESC = "Desc";
        public static final String WORK_Q_URL = "Work Q URL";
        public static final String WORK_Q_NAME = "Work Q Name";
        public static final String RESULT_Q_URL = "Result Q URL";
        public static final String RESULT_Q_NAME = "Result Q Name";
        public static final String API_SERVER_URL = "API Server URL";
        public static final String RESULT_SERVER_URL = "Result Server URL";
        public static final String FLAT_FILENAME = "Flat Filename";
        public static final String BIAS_FILENAME = "Bias Filename";
        public static final String CONFIG_FILENAME = "Config Filename";
        public static final String FOLLOW_ON_JOB = "Follow On Job";
    }

    private String actID;
    private String desc;
    private String work_Q_URL;
    private String work_Q_Name;
    private String result_Q_URL;
    private String result_Q_Name;
    private String api_Server_URL;
    private String result_Server_URL;
    private String flat_Filename;
    private String bias_Filename;
    private String config_Filename;
    private String followingJob;

    // Constructor to take the JSON in the MQ message
    public ControlMessage (String json) throws Exception {

        // https://code.google.com/archive/p/json-simple
        JSONParser parser = new JSONParser();
        JSONObject obj;
        try {
            obj = (JSONObject) parser.parse(json);
        } catch (ParseException e) {
            throw new Exception("Error parsing JSON of the control message.\n" + e.getMessage() + "\n" + json);
        }

        //System.out.println(" -> JSON object: '" + obj.toJSONString() + "'");

        this.actID = (String) obj.get(Fields.ACT_ID);
        this.desc = (String) obj.get(Fields.DESC);
        this.work_Q_URL = (String) obj.get(Fields.WORK_Q_URL);
        this.work_Q_Name = (String) obj.get(Fields.WORK_Q_NAME);
        this.result_Q_URL = (String) obj.get(Fields.RESULT_Q_URL);
        this.result_Q_Name = (String) obj.get(Fields.RESULT_Q_NAME);
        this.api_Server_URL = (String) obj.get(Fields.API_SERVER_URL);
        this.result_Server_URL = (String) obj.get(Fields.RESULT_SERVER_URL);
        this.flat_Filename = (String) obj.get(Fields.FLAT_FILENAME);
        this.bias_Filename = (String) obj.get(Fields.BIAS_FILENAME);
        this.config_Filename = (String) obj.get(Fields.CONFIG_FILENAME);
        this.followingJob = (String) obj.get(Fields.FOLLOW_ON_JOB);
    }

    public String getActID() {
        return actID;
    }

    public void setCID(String actID) {
        this.actID = actID;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getWork_Q_URL() {
        return work_Q_URL;
    }

    public void setWork_Q_URL(String work_Q_URL) {
        this.work_Q_URL = work_Q_URL;
    }

    public String getWork_Q_Name() {
        return work_Q_Name;
    }

    public void setWork_Q_Name(String work_Q_Name) {
        this.work_Q_Name = work_Q_Name;
    }

    public String getResult_Q_URL() {
        return result_Q_URL;
    }

    public void setResult_Q_URL(String result_Q_URL) {
        this.result_Q_URL = result_Q_URL;
    }

    public String getResult_Q_Name() {
        return result_Q_Name;
    }

    public void setResult_Q_Name(String result_Q_Name) {
        this.result_Q_Name = result_Q_Name;
    }

    public String getAPI_Server_URL() {
        return api_Server_URL;
    }

    public void setAPI_Server_URL(String aPI_Server_URL) {
        api_Server_URL = aPI_Server_URL;
    }

    public String getResult_Server_URL() {
        return result_Server_URL;
    }

    public void setResult_Server_URL(String result_Server_URL) {
        result_Server_URL = result_Server_URL;
    }

    public String getFlat_Filename() {
        return flat_Filename;
    }

    public void setFlat_Filename(String flat_Filename) {
        this.flat_Filename = flat_Filename;
    }

    public String getBias_Filename() {
        return bias_Filename;
    }

    public void setBias_Filename(String bias_Filename) {
        this.bias_Filename = bias_Filename;
    }

    public String getConfig_Filename() {
        return config_Filename;
    }

    public void setConfig_Filename(String config_Filename) {
        this.config_Filename = config_Filename;
    }

    public String getFollowingJob() {
        return followingJob;
    }

    public void setFollowingJob(String followingJob) {
        this.followingJob = followingJob;
    }

    public String toString() {
        return String.format("actID:%s\ndesc:%s\nWork_Q_URL:%s\n"
                        + "Work_Q_Name:%s\nResult_Q_URL:%s\nResult_Q_Name:%s\n"
                        + "API_Server_URL:%s\nFlat_Filename:%s\nBias_Filename:%s\n"
                        + "Config_Filename:%s\nfollowingJob:%s",
                actID, desc, work_Q_URL, work_Q_Name, result_Q_URL, result_Q_Name,
                api_Server_URL, flat_Filename, bias_Filename,
                config_Filename, followingJob);
    }

}