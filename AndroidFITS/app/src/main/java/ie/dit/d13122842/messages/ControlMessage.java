package ie.dit.d13122842.messages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ControlMessage {

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
        try {
            init(
                    obj.get("ActID"), obj.get("Desc"), obj.get("Work Q URL"), obj.get("Work Q Name"),
                    obj.get("Result Q URL"), obj.get("Result Q Name"), obj.get("API Server URL"),
                    obj.get("Result Server URL"),obj.get("Flat Filename"), obj.get("Bias Filename"),
                    obj.get("Config Filename"));
        } catch (Exception e) {
            throw new Exception("Error creating object from JSON of the control message.\n" + e.getMessage() + "\n" + json);
        }
    }

    private void init(Object actID, Object Desc, Object Work_Q_URL,
                          Object Work_Q_Name, Object Result_Q_URL, Object Result_Q_Name,
                          Object API_Server_URL, Object Result_Server_URL, Object Flat_Filename, Object Bias_Filename,
                          Object Config_Filename) {

        this.actID = (String) actID;
        this.desc = (String) Desc;
        this.work_Q_URL = (String) Work_Q_URL;
        this.work_Q_Name = (String) Work_Q_Name;
        this.result_Q_URL = (String) Result_Q_URL;
        this.result_Q_Name = (String) Result_Q_Name;
        this.api_Server_URL = (String) API_Server_URL;
        this.result_Server_URL = (String) Result_Server_URL;
        this.flat_Filename = (String) Flat_Filename;
        this.bias_Filename = (String) Bias_Filename;
        this.config_Filename = (String) Config_Filename;
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

    public String toString() {
        return String.format("actID:%s\ndesc:%s\nWork_Q_URL:%s\n"
                        + "Work_Q_Name:%s\nResult_Q_URL:%s\nResult_Q_Name:%s\n"
                        + "API_Server_URL:%s\nFlat_Filename:%s\nBias_Filename:%s\n"
                        + "Config_Filename:%s",
                actID, desc, work_Q_URL, work_Q_Name, result_Q_URL, result_Q_Name,
                api_Server_URL, flat_Filename, bias_Filename,
                config_Filename);
    }

}