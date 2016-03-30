package ie.dit.d13122842;

import java.util.ArrayList;
import java.util.List;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;

public class MessageQueueManager {

	public void postCleaningJob(CleaningJob job) throws Exception {
		// Create Control and Work messages
		
		try {
			
			System.out.println(job.toString());

			// Set up RabbitMQ Client
			System.out.println("Creating messages for cleaning job..");
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(Config.MQ.HOST);
			factory.setUsername(Config.MQ.USER);
			factory.setPassword(Config.MQ.PASS);
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
	
			channel.queueDeclare(Config.MQ.ACTIVATION_QUEUE, true, false, false, null);
			channel.queueDeclare(Config.MQ.CLEANING_WORK_QUEUE, true, false, false, null);
			
			// Build the control message
			/* e.g.:
			{ 
				"CID":"1",
				"Desc":"FITS Cleaning",
				"Work Q URL":"amqp://test:test@192.168.3.21:5672",
				"Work Q Name":"work_queue",
				"Result Q URL":"amqp://test:test@192.168.3.21:5672",
				"Result Q Name":"result_queue",
				"API Server URL":"http://192.168.3.13:8080/FITSAPIServer/MainServlet",
				"Result Server URL":"http://192.168.3.13:8080/FITSAPIResultServer/MainServlet",
				"Flat Filename":"Final-MasterFlat.fits",
				"Bias Filename":"Final-MasterBias-subrect.fits",
				"Config Filename":"config"
			}
			*/
			String actMsg = String.format("{\n" +
					" \"ActID\":\"%s\"" + 
					" \"Desc\":\"%s\"" + 
					" \"Work Q URL\":\"%s\"" + 
					" \"Work Q Name\":\"%s\"" + 
					" \"Result Q URL\":\"%s\"" + 
					" \"Result Q Name\":\"%s\"" + 
					" \"API Server URL\":\"%s\"" + 
					" \"Result Server URL\":\"%s\"" + 
					" \"Flat Filename\":\"%s\"" + 
					" \"Bias Filename\":\"%s\"" + 
					" \"Config Filename\":\"%s\"" +
					"}",				
					job.getActID(),
					job.getDesc(),
					job.getWork_Q_URL(),
					job.getWork_Q_Name(),
					job.getResult_Q_URL(),
					job.getResult_Q_Name(),
					job.getAPI_Server_URL(),
					job.getResult_Server_URL(),
					job.getFlat_Filename(),
					job.getBias_Filename(),
					job.getConfig_Filename());
			
			System.out.println("actMsg:\n"+actMsg);
			byte[] actBytes = actMsg.getBytes();
			
			// Build the work message 
			// that will be populated inside the loop:
			/* e.g.:
			 {
				"CID":"1",
				"WID":"0000",
				"FITS Filename":"0000001.fits",
				"Planes":2
			}
			 */
			String wrkMsgFmt = "{" +
					" \"ActID\":\"1\"" + 
					" \"WorkID\":\"0000\"" + 
					" \"FITS Filename\":\"%s\"" + 
					" \"Planes\":%d" + 
					"}";
			
			System.out.println("About to publish messages...");

			// Send a control message and a work message for each FITS file
			for (String filename : job.getFITS_Filenames()) {
				
				// Clients should request the uncompressed version
				if (filename.endsWith(".fz")) {
					filename = filename.substring(0,filename.lastIndexOf('.'));
				}
				
				// Publish a control message
				channel.basicPublish(
						"", 				// default exchange so routing key == queue name
						Config.MQ.ACTIVATION_QUEUE,
						MessageProperties.PERSISTENT_TEXT_PLAIN,
						actBytes);
				System.out.println("-> Sent '" + new String(actBytes, "UTF-8") + "'");
				
				// publish a work message to contain this filename
				byte[] wrkBytes = String.format(wrkMsgFmt, 
						filename,
						job.getPlanes_per_fits()).getBytes();
				
				channel.basicPublish(
						"", 				// default exchange so routing key == queue name
						Config.MQ.CLEANING_WORK_QUEUE,
						MessageProperties.PERSISTENT_TEXT_PLAIN,
						wrkBytes);
				System.out.println("-> Sent '" + new String(wrkBytes, "UTF-8") + "'");			
				
			}
			 
			channel.close();
			connection.close();
			
			System.out.println("Messages published.");
			
		} catch (Exception e) {
			
			throw new Exception("Failed to send message to the message queue: "+e.getMessage());
			
		}
		
	}
	
	public List<ResultMessage> getResultMessages(String queueName) throws Exception {
	
		ArrayList<ResultMessage> receivedMessages = new ArrayList<ResultMessage>();
		
		try {
			
			System.out.println("getResultMessages()");	

			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(Config.MQ.HOST);
			factory.setUsername(Config.MQ.USER);
			factory.setPassword(Config.MQ.PASS);
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			
			final boolean AUTOACK_ON = true;
			GetResponse response = channel.basicGet(queueName, AUTOACK_ON);
			while (response != null) {
				String jsonString = new String(response.getBody());
				System.out.println("Msg read: "+jsonString);
				receivedMessages.add(new ResultMessage(jsonString));
			    response = channel.basicGet(queueName, AUTOACK_ON);
			}
			System.out.println("getResultMessages() count = "+receivedMessages.size());
			return receivedMessages;
			
		} catch (Exception e) {
			throw new Exception("Error reading results queue contents. "+e.getMessage(),e);
		}
			
	}
}
