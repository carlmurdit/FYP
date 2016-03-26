package ie.dit.d13122842.messages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WorkMessage {
    private String actID;         // Control ID
    private String workID;         // Work Unit ID
    private String filename;    // FITS file to be cleaned
    private int planes;         // Number of planes in the FITS file

    public WorkMessage(String json) throws Exception  {

        // https://code.google.com/archive/p/json-simple
        JSONParser parser = new JSONParser();
        JSONObject obj;
        try {
            obj = (JSONObject) parser.parse(json);
        } catch (ParseException e) {
            throw new Exception("Error parsing JSON of the work message.\n" + e.getMessage() + "\n" + json);
        }

        //System.out.println(" -> JSON object: '" + obj.toJSONString() + "'");
        WorkMessage wm = null;
        try {
            init(
                    obj.get("actID"), obj.get("workID"), obj.get("FITS Filename"), obj.get("Planes"));
        } catch (Exception e) {
            throw new Exception("Error creating object from JSON of the work message.\n"+e.getMessage()+"\n"+json);
        }


    }

    private void init(Object actID, Object workID, Object filename, Object planes) {
        this.actID = (String) actID;
        this.workID = (String) workID;
        this.filename = (String) filename;
        this.planes = (int)(long) planes;
    }

    public String getActID() {
        return actID;
    }

    public void setActID(String cID) {
        this.actID = actID;
    }

    public String getWorkID() {
        return workID;
    }

    public void setWorkID(String wID) {
        this.workID = wID;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getPlanes() {
        return planes;
    }

    public void setPlanes(int planes) {
        this.planes = planes;
    }

    public String toString() {
        return String.format("actID=%s\nworkID=%s\nfilename=%s\nplanes=%d",
                actID , workID, filename, planes);
    }


}