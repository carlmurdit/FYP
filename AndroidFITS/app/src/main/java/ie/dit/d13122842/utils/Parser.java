package ie.dit.d13122842.utils;

//import org.json.JSONObject;

import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

import ie.dit.d13122842.main.Star;
import ie.dit.d13122842.messages.ControlMessage;
import ie.dit.d13122842.messages.WorkMessage;

public class Parser {

    private JSONObject parseJSON(String json) throws ParseException {
        // https://code.google.com/archive/p/json-simple
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(json);
    }

    public ControlMessage parseControlMessage(String json, Long deliveryTag) throws Exception  {

        JSONObject obj;
        try {
            obj = parseJSON(json);
        } catch (ParseException e) {
            throw new Exception("Error parsing JSON of the control message.\n"+e.getMessage()+"\n"+json);
        }

        //System.out.println(" -> JSON object: '" + obj.toJSONString() + "'");
        ControlMessage cm = null;
        try {
            cm = new ControlMessage(
                    obj.get("CID"), obj.get("Desc"), obj.get("Work Q URL"), obj.get("Work Q Name"),
                    obj.get("Result Q URL"), obj.get("Result Q Name"), obj.get("API Server URL"),
                    obj.get("Flat Filename"), obj.get("Bias Filename"), obj.get("Config Filename"),
                    deliveryTag);
        } catch (Exception e) {
            throw new Exception("Error creating object from JSON of the control message.\n"+e.getMessage()+"\n"+json);
        }
        return cm;
    }

    public WorkMessage parseWorkMessage(String json, Long deliveryTag) throws Exception  {

        JSONObject obj;
        try {
            obj = parseJSON(json);
        } catch (ParseException e) {
            throw new Exception("Error parsing JSON of the work message.\n"+e.getMessage()+"\n"+json);
        }

        //System.out.println(" -> JSON object: '" + obj.toJSONString() + "'");
        WorkMessage wm = null;
        try {
            wm = new WorkMessage(
                    obj.get("CID"), obj.get("WID"), obj.get("FITS Filename"), obj.get("Planes"), deliveryTag);
        } catch (Exception e) {
            throw new Exception("Error creating object from JSON of the work message.\n"+e.getMessage()+"\n"+json);
        }
        return wm;

/*		JSONReader jr = new JSONReader();
		Object o = jr.read(json);
		ArrayList<Object> works = new ArrayList<Object>();
		works = (ArrayList<Object>) o;
		for(Object w : works){
			// System.out.println(w);
		}
		System.out.println("result is " + o + ", class " + o.getClass()); */

    }

    public void parseConfig(String configContents, ArrayList<Star> stars) throws Exception {
        Log.d("", "parseConfig configContents = "+configContents);
        try {
            stars.clear();
            String[] lines = configContents.split("\n"); //todo handle \r\n endings?
            for (int l=0; l<lines.length; l++) {
                String line = lines[l];
                if (!line.startsWith("!")) {
                    String[] parts = line.split(" ");
                    stars.add(new Star(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]),
                            Integer.parseInt(parts[4]),
                            Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6])));
                }
            }

        } catch (Exception e) {
            throw new Exception("Error parsing Config file contents into stars: \n"+configContents+"\n"+e.getMessage());
        }

    }
}
