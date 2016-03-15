package ie.dit.d13122842.messages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ControlMessage {

    private String CID;
    private String Desc;
    private String Work_Q_URL;
    private String Work_Q_Name;
    private String Result_Q_URL;
    private String Result_Q_Name;
    private String API_Server_URL;
    private String Flat_Filename;
    private String Bias_Filename;
    private String Config_Filename;
    private Long deliveryTag; //message tag, used to ack

    // Constructor to take the JSON in the MQ message
    public ControlMessage (String json, Long deliveryTag) throws Exception {

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
                    obj.get("CID"), obj.get("Desc"), obj.get("Work Q URL"), obj.get("Work Q Name"),
                    obj.get("Result Q URL"), obj.get("Result Q Name"), obj.get("API Server URL"),
                    obj.get("Flat Filename"), obj.get("Bias Filename"), obj.get("Config Filename"),
                    deliveryTag);
        } catch (Exception e) {
            throw new Exception("Error creating object from JSON of the control message.\n" + e.getMessage() + "\n" + json);
        }
    }

    private void init(Object CID, Object Desc, Object Work_Q_URL,
                          Object Work_Q_Name, Object Result_Q_URL, Object Result_Q_Name,
                          Object API_Server_URL, Object Flat_Filename, Object Bias_Filename,
                          Object Config_Filename, Long deliveryTag) {

        this.CID = (String) CID;
        this.Desc = (String) Desc;
        this.Work_Q_URL = (String) Work_Q_URL;
        this.Work_Q_Name = (String) Work_Q_Name;
        this.Result_Q_URL = (String) Result_Q_URL;
        this.Result_Q_Name = (String) Result_Q_Name;
        this.API_Server_URL = (String) API_Server_URL;
        this.Flat_Filename = (String) Flat_Filename;
        this.Bias_Filename = (String) Bias_Filename;
        this.Config_Filename = (String) Config_Filename;
        this.deliveryTag = deliveryTag;
    }

    public String getCID() {
        return CID;
    }

    public void setCID(String cID) {
        CID = cID;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getWork_Q_URL() {
        return Work_Q_URL;
    }

    public void setWork_Q_URL(String work_Q_URL) {
        Work_Q_URL = work_Q_URL;
    }

    public String getWork_Q_Name() {
        return Work_Q_Name;
    }

    public void setWork_Q_Name(String work_Q_Name) {
        Work_Q_Name = work_Q_Name;
    }

    public String getResult_Q_URL() {
        return Result_Q_URL;
    }

    public void setResult_Q_URL(String result_Q_URL) {
        Result_Q_URL = result_Q_URL;
    }

    public String getResult_Q_Name() {
        return Result_Q_Name;
    }

    public void setResult_Q_Name(String result_Q_Name) {
        Result_Q_Name = result_Q_Name;
    }

    public String getAPI_Server_URL() {
        return API_Server_URL;
    }

    public void setAPI_Server_URL(String aPI_Server_URL) {
        API_Server_URL = aPI_Server_URL;
    }

    public String getFlat_Filename() {
        return Flat_Filename;
    }

    public void setFlat_Filename(String flat_Filename) {
        Flat_Filename = flat_Filename;
    }

    public String getBias_Filename() {
        return Bias_Filename;
    }

    public void setBias_Filename(String bias_Filename) {
        Bias_Filename = bias_Filename;
    }

    public String getConfig_Filename() {
        return Config_Filename;
    }

    public void setConfig_Filename(String config_Filename) {
        Config_Filename = config_Filename;
    }

    public Long getDeliveryTag() {
        return deliveryTag;
    }

    public void setDeliveryTag(Long deliveryTag) {
        this.deliveryTag = deliveryTag;
    }

    public String toString() {
        return String.format("CID:%s\nDesc:%s\nWork_Q_URL:%s\n"
                        + "Work_Q_Name:%s\nResult_Q_URL:%s\nResult_Q_Name:%s\n"
                        + "API_Server_URL:%s\nFlat_Filename:%s\nBias_Filename:%s\n"
                        + "Config_Filename:%s\ndeliveryTag=%s",
                CID, Desc, Work_Q_URL, Work_Q_Name, Result_Q_URL, Result_Q_Name,
                API_Server_URL, Flat_Filename, Bias_Filename,
                Config_Filename, deliveryTag);
    }

}