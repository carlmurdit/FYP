package ie.dit.d13122842;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class MessageQueueManager {

	public void postCleaningJob(CleaningJob job) throws Exception {
		
		try {
			
			System.out.println(job.toString());
						
			// Set up RabbitMQ Client
			System.out.println("Creating messages for cleaning job..");
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(Config.RabbitMQ.HOST);
			factory.setUsername(Config.RabbitMQ.USER);
			factory.setPassword(Config.RabbitMQ.PASS);
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
	
			channel.queueDeclare(Config.RabbitMQ.CONTROL_QUEUE, true, false, false, null);
			channel.queueDeclare(Config.RabbitMQ.WORK_QUEUE, true, false, false, null);
			
			// Build the control message
			/* e.g.:
			{ 
				"CID":"1",
				"Desc":"FITS Pixel Calculation",
				"Work Q URL":"amqp://test:test@192.168.3.21:5672",
				"Work Q Name":"work_queue",
				"Result Q URL":"amqp://test:test@192.168.3.21:5672",
				"Result Q Name":"result_queue",
				"API Server URL":"http://192.168.3.13:8080/FITSAPIServer/MainServlet",
				"Flat Filename":"Final-MasterFlat.fits",
				"Bias Filename":"Final-MasterBias-subrect.fits",
				"Config Filename":"config"
			}
			*/
			String ctlMsg = String.format("{\n" +
					" \"CID\":\"%s\"" + 
					" \"Desc\":\"%s\"" + 
					" \"Work Q URL\":\"%s\"" + 
					" \"Work Q Name\":\"%s\"" + 
					" \"Result Q URL\":\"%s\"" + 
					" \"Result Q Name\":\"%s\"" + 
					" \"API Server URL\":\"%s\"" + 
					" \"Flat Filename\":\"%s\"" + 
					" \"Bias Filename\":\"%s\"" + 
					" \"Config Filename\":\"%s\"" +
					"}",				
					job.getCID(),
					job.getDesc(),
					job.getWork_Q_URL(),
					job.getWork_Q_Name(),
					job.getResult_Q_URL(),
					job.getResult_Q_Name(),
					job.getAPI_Server_URL(),
					job.getFlat_Filename(),
					job.getBias_Filename(),
					job.getConfig_Filename());
			
			System.out.println("ctlMsg:\n"+ctlMsg);
			byte[] ctlBytes = ctlMsg.getBytes();
			
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
					" \"CID\":\"1\"" + 
					" \"CID\":\"0000\"" + 
					" \"FITS Filename\":\"%s\"" + 
					" \"Planes\":%d" + 
					"}";
			
			System.out.println("About to publish messages...");

			for (String filename : job.getFITS_Filenames()) {
				
				// Send a control message and a work message for each FITS file
				
				channel.basicPublish(
						"", 				// default exchange so routing key == queue name
						Config.RabbitMQ.CONTROL_QUEUE,
						MessageProperties.PERSISTENT_TEXT_PLAIN,
						ctlBytes);
				System.out.println("-> Sent '" + new String(ctlBytes, "UTF-8") + "'");
				
				byte[] wrkBytes = String.format(wrkMsgFmt, 
						filename,
						job.getPlanes_per_fits()).getBytes();
				
				channel.basicPublish(
						"", 				// default exchange so routing key == queue name
						Config.RabbitMQ.WORK_QUEUE,
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
}
