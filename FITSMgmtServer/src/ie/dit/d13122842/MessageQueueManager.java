package ie.dit.d13122842;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
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
				"Following Job":"config",
				"Is Source":"N"
			}
			*/
			ActivationMessage actMsg = new ActivationMessage( 
					job.getActID(), job.getDesc(),
					job.getWork_Q_URL(), job.getWork_Q_Name(),
					job.getResult_Q_URL(), job.getResult_Q_Name(),
					job.getAPI_Server_URL(), job.getResult_Server_URL(), 
					job.getFlat_Filename(), job.getBias_Filename(), 
					job.getConfig_Filename(), job.getFollowingJob());
			String actJSON = actMsg.toJSON();
			
			/*
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
					" \"Following Job\":\"%s\"" +
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
					job.getConfig_Filename(),
					job.getFollowingJob());
					*/
			
			System.out.println("actJSON:\n"+actJSON);
			byte[] actBytes = actJSON.getBytes();
			
			// Build the work message 

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
				String wrkJSON = String.format(
						"{" +
						" \"%s\":\"%s\"" + 
						" \"%s\":\"%s\"" + 
						" \"%s\":\"%s\"" + 
						" \"%s\":%d" + 
						"}", 
						WorkMessage.Fields.ACT_ID, job.getActID(),
						WorkMessage.Fields.WORK_ID, "0000",
						WorkMessage.Fields.SOURCE_FILE, filename,
						WorkMessage.Fields.PLANES, job.getPlanes_per_fits()
						);

				System.out.println("wrkJSON: "+wrkJSON);
				byte[] wrkBytes = wrkJSON.getBytes();
				
				channel.basicPublish(
						"", 				// default exchange so routing key == queue name
						job.getWork_Q_Name(),
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
		
		// ToDo: Use a database to handle simultaneous access
	
		ArrayList<ResultMessage> resultsOld = new ArrayList<ResultMessage>();
		ArrayList<ResultMessage> resultsNew = new ArrayList<ResultMessage>();
		
		String messageHistory = Config.Dirs.RESULT_HISTORY+queueName;
		
		// get any results that were previously saved
		if (new File(messageHistory).exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(messageHistory))) {
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					resultsOld.add(new ResultMessage(line));
				}
			} catch (IOException e) {
				System.out.println("Error reading previous results from "+messageHistory);
			}
		}	

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
				resultsNew.add(new ResultMessage(jsonString));
			    response = channel.basicGet(queueName, AUTOACK_ON);
			}
			System.out.println("resultsNew() count = "+resultsNew.size());		
			
		} catch (Exception e) {
			throw new Exception("Error reading results queue contents. "+e.getMessage(),e);
		}
		
		// save the new results
		try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(messageHistory, true)))) {
			for (ResultMessage rm : resultsNew) {
				pw.println(rm.toString());
			}
		} catch (IOException e) {
			System.out.println("Error persisting new results to "+messageHistory);
		}
		
		resultsOld.addAll(resultsNew);
		return resultsOld;
			
	}
}
