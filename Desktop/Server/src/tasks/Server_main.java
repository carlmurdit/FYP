package tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class Server_main {
	
	public static void main(String[] argv) throws Exception {
		System.out.println("Server_main started.");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("192.168.3.21");
		//factory.setHost("147.252.141.32");
		factory.setUsername("test");
		factory.setPassword("test");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare("control_queue", true, false, false, null);
		channel.queueDeclare("work_queue", true, false, false, null);

		publishFile("messages/Control.txt", channel, "control_queue");
		publishFile("messages/Work1.txt", channel, "work_queue"); 
		publishFile("messages/Work2.txt", channel, "work_queue"); 

		channel.close();
		connection.close();
	}

	private static void publishFile(String fileName, Channel channel,
			String routingKey) {

		try {
			
			byte[] allBytes = Files.readAllBytes(Paths.get(fileName));
			channel.basicPublish(
					"", 				// default exchange so routing key == queue name
					routingKey,
					MessageProperties.PERSISTENT_TEXT_PLAIN,
					allBytes);
			System.out.println("-> Sent '" + new String(allBytes, "UTF-8") + "'");

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

}
