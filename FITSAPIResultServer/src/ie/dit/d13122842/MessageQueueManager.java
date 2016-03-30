package ie.dit.d13122842;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class MessageQueueManager {

	public void postMagnitudeJob(String cID, String desc, String s3KeyName) throws Exception {
		
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
			factory.setUri(Config.MQ.QUEUE_URI);
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
			String ctlMsg = String.format("{\n" +
					" \"CID\":\"%s\"" + 
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
					"","","");
			
			System.out.println("ctlMsg:\n"+ctlMsg);
			byte[] ctlBytes = ctlMsg.getBytes();
			
			// Build the work message 
			// that will be populated inside the loop:
			/* e.g.:
			 {
				"CID":"2",
				"WID":"0000",
				"FITS Filename":"0000001.fits",
				"Planes":2
			}
			 */
			String wrkMsg = String.format("{" +
					" \"CID\":\"2\"" + 
					" \"CID\":\"0000\"" + 
					" \"FITS Filename\":\"%s\"" + 
					"}", s3KeyName);
					
			// Publish a control message
			channel.basicPublish(
					"", 				// default exchange so routing key == queue name
					Config.MQ.ACTIVATION_QUEUE,
					MessageProperties.PERSISTENT_TEXT_PLAIN,
					ctlBytes);
			System.out.println("-> Sent '" + new String(ctlBytes, "UTF-8") + "'");
			
			// publish a work message to contain this filename
			byte[] wrkBytes = wrkMsg.getBytes();
			
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
