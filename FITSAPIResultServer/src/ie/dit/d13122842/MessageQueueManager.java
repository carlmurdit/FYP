package ie.dit.d13122842;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class MessageQueueManager {

	public void postMagnitudeJob(String actID, String desc, String fileName, int planeCount) throws Exception {
		
		// The result of a Cleaning Job is the basis for a new Magnitude Job.
		//
		// Publish a successful cleaning result to "cleaned_queue" with a 
		// corresponding control queue message so a node will take it to do Magnitude on it.
		// Publish a failed cleaning result to "clean_failed_queue"
		
		try {
			
			System.out.println("in postCleaningResult()");

			// Set up RabbitMQ Client
			System.out.println("Creating messages for cleaning job..");
			ConnectionFactory factory = new ConnectionFactory();
			factory.setUri(Config.MQ.QUEUE_URL);
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
	
			channel.queueDeclare(Config.MQ.ACTIVATION_QUEUE, true, false, false, null);
			channel.queueDeclare(Config.MQ.MAGNITUDE_WORK_QUEUE, true, false, false, null);
			
			// Build the control message. 
			// Leave n/a fields empty but don't remove them, they are expected.
			/* e.g.:
			{ 
				"CID":"2",
				"Desc":"Magnitude",
				"Work Q URL":"amqp://test:test@192.168.3.21:5672",
				"Work Q Name":"work_queue",
				"API Server URL":"http://192.168.3.13:8080/FITSAPIServer/MainServlet",
				"Result Server URL":"http://192.168.3.13:8080/FITSAPIResultServer/MainServlet",
				"Flat Filename":"",
				"Bias Filename":"",
				"Config Filename":""
			}
			*/
			// ToDo: Remove hard coding for Magnitude Job
			ActivationMessage actMsg = new ActivationMessage( 
					actID, desc,
					Config.MQ.QUEUE_URL, Config.MQ.MAGNITUDE_WORK_QUEUE,
					Config.MQ.QUEUE_URL, Config.MQ.MAGNITUDE_RESULT_QUEUE,
					Config.API.SERVER_URL, "", 
					"", "", 
					"Config", "0");
			String actJSON = actMsg.toJSON();
			
			/*
		String ctlMsg = String.format("{\n" +
					" \"ActID\":\"%s\"" + 
					" \"Desc\":\"%s\"" + 
					" \"Work Q URL\":\"%s\"" + 
					" \"Work Q Name\":\"%s\"" + 
					" \"API Server URL\":\"%s\"" + 
					" \"Result Server URL\":\"%s\"" + 
					" \"Flat Filename\":\"%s\"" + 
					" \"Bias Filename\":\"%s\"" + 
					" \"Config Filename\":\"%s\"" +
					"}",				
					cID,
					desc,
					Config.MQ.QUEUE_URI,
					Config.MQ.MAGNITUDE_WORK_QUEUE,
					Config.API.SERVER_URL,
					Config.Result.SERVER_URL,
					"","","config");
					*/
			
			System.out.println("actJSON:\n"+actJSON);
			byte[] actBytes = actJSON.getBytes();
			
			// Build the work message 
			byte[] wrkBytes = String.format(
					"{" +
					" \"%s\":\"%s\"" + 
					" \"%s\":\"%s\"" + 
					" \"%s\":\"%s\"" + 
					" \"%s\":%d" + 
					"}", 
					WorkMessage.Fields.ACT_ID, actID,
					WorkMessage.Fields.WORK_ID, "0000",
					WorkMessage.Fields.SOURCE_FILE, fileName,
					WorkMessage.Fields.PLANES, planeCount
					).getBytes();
					
			// Publish a control message
			channel.basicPublish(
					"", 				// default exchange so routing key == queue name
					Config.MQ.ACTIVATION_QUEUE,
					MessageProperties.PERSISTENT_TEXT_PLAIN,
					actBytes);
			System.out.println("-> Sent '" + new String(actBytes, "UTF-8") + "'");
			
			channel.basicPublish(
					"", 				// default exchange so routing key == queue name
					Config.MQ.MAGNITUDE_WORK_QUEUE,
					MessageProperties.PERSISTENT_TEXT_PLAIN,
					wrkBytes);
			System.out.println("-> Sent SUCCESS\n'" + new String(wrkBytes, "UTF-8") + "'");
			 
			channel.close();
			connection.close();
			
			System.out.println("Messages published.");
			
		} catch (Exception e) {
			
			throw new Exception("Failed to send message to the message queue: "+e.getMessage());
			
		}
		
	}
}
