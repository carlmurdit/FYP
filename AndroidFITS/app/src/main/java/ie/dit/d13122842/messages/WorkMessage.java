package ie.dit.d13122842.messages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WorkMessage {
    private String cID;         // Control ID
    private String wID;         // Work Unit ID
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
                    obj.get("CID"), obj.get("WID"), obj.get("FITS Filename"), obj.get("Planes"));
        } catch (Exception e) {
            throw new Exception("Error creating object from JSON of the work message.\n"+e.getMessage()+"\n"+json);
        }


    }

    private void init(Object cID, Object wID, Object filename, Object planes) {
        this.cID = (String) cID;
        this.wID = (String) wID;
        this.filename = (String) filename;
        this.planes = (int)(long) planes;
    }

    public String getcID() {
        return cID;
    }

    public void setcID(String cID) {
        this.cID = cID;
    }

    public String getwID() {
        return wID;
    }

    public void setwID(String wID) {
        this.wID = wID;
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
        return String.format("cID=%s\nwID=%s\nfilename=%s\nplanes=%d",
                cID , wID, filename, planes);
    }


}