package tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;



public class Client_main {

	private final String QUEUE_NAME_CTL = "control_queue";
	private final String QUEUE_NAME_WRK = "work_queue";
	private final boolean AUTOACK_OFF = false;
	private final int WORK_PER_CTRL = 2; // work messages per control message
	
	public static void main(String[] argv) throws Exception {
		System.out.println("Client_main started.");
		Client_main main = new Client_main();
	}
	
	public Client_main() throws IOException {
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("192.168.3.21");
		// factory.setHost("147.252.141.32");
		factory.setUsername("test");
		factory.setPassword("test");
		
		Connection connection;
		try {
			connection = factory.newConnection();
		} catch (IOException | TimeoutException e) {
			System.out.println("Connection failed, "+e.getMessage());
			return;
		}
		
		final Channel channelWRK = connection.createChannel(); // channels not thread-safe
		channelWRK.queueDeclare(QUEUE_NAME_WRK, true, false, false, null);
		channelWRK.basicQos(0); // prefetch count unlimited		
		
		final Channel channelCTL = connection.createChannel(); 
		channelCTL.queueDeclare(QUEUE_NAME_CTL, true, false, false, null);
		channelCTL.basicQos(1); // prefetch count
		channelCTL.basicConsume(
				QUEUE_NAME_CTL, 
				AUTOACK_OFF, 
				new DefaultConsumer(channelCTL) {
					@Override
					public void handleDelivery(String consumerTag,
							Envelope envelope, AMQP.BasicProperties properties,
							byte[] bodyCTL) throws IOException {
						
						ControlMessage ctlMsg = null;
						ArrayList<WorkMessage> wrkMsgs = new ArrayList<WorkMessage>();
						try {
							// accept the CONTROL message and create an object
							String messageCTL = new String(bodyCTL, "UTF-8");
							ctlMsg = parseControlMessage(messageCTL, envelope.getDeliveryTag());
							System.out.println("-> ControlMessage object created:\n"+ctlMsg.toString());	

							// get the WORK messages
							for (int i=1; i<=WORK_PER_CTRL; i++) {				
								GetResponse response = channelWRK.basicGet(QUEUE_NAME_WRK, AUTOACK_OFF);
								if (response == null) {
									System.out.println("\t-> No WRK msg found.");
								} else {
								    String messageWRK = new String(response.getBody(), "UTF-8");
								    WorkMessage wm = parseWorkMessage(messageWRK, response.getEnvelope().getDeliveryTag()); 
								    wrkMsgs.add(wm);
									System.out.println("-> WorkMessage object created:\n"+wm.toString());
								}
							}
							
							// do work on the messages
							doWork(ctlMsg, wrkMsgs);
							
							//ack all messages
							ack(ctlMsg, channelCTL, wrkMsgs, channelWRK);
							
						} catch (Exception e) {
							System.out.println("The work failed. "+e.getMessage());
						} finally {
							System.out.println("-> Job Done");
						}
					}
				});
	}
	
	private JSONObject parseJSON(String json) throws ParseException {
		// https://code.google.com/archive/p/json-simple
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(json);		
	}
	
	private ControlMessage parseControlMessage(String json, Long deliveryTag) throws Exception  {
		
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
	
	private WorkMessage parseWorkMessage(String json, Long deliveryTag) throws Exception  {
		
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

	/*
	 * private static void close() { // todo - graceful stop 
	 * channel.close();
	 * connection.close(); }
	 */
	
	private void doWork(ControlMessage ctlMsg, ArrayList<WorkMessage> wrkMsgs) {
		System.out.println("-> doWork()...");
		// Download the config and read the stars
		ArrayList<Star> stars = new ArrayList<Star>();
		
		
		// For each star in the config, download boxes from flat and bias
				
		// For each Work message
			// For each config star (dummy values initially)For each work message (ie FITS file)
				// For each plane in the FITS 
					// Download FITS pixels for the star's X, Y, box 
					// Pixel clean()
					// Store result for the message (50 blocks) on AWS
					// Write to result queue with uploaded URL
			// ackWork()
	}

	private void ack(ControlMessage ctlMsg, Channel channelCTL, ArrayList<WorkMessage> wrkMsgs, Channel channelWRK) {
		try {
			channelCTL.basicAck(ctlMsg.getDeliveryTag(), false);
			for (WorkMessage wm : wrkMsgs) {
				channelWRK.basicAck(wm.getDeliveryTag(), false);
			}
		} catch (IOException e) {
			System.out.println("Error acking: "+e.getMessage());
		}
	}
}

