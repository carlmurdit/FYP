package tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class Server_main {
	
	public static void main(String[] argv) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("192.168.3.21");
		//factory.setHost("147.252.141.32");
		factory.setUsername("test");
		factory.setPassword("test");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		// sample code
		channel.queueDeclare("task_queue", true, false, false, null);
		publishString("First message.", channel, "task_queue");
		publishString("Second message..", channel, "task_queue");
		publishString("Third message...", channel, "task_queue");
		publishString("Fourth message....", channel, "task_queue");
		publishString("Fifth message.....", channel, "task_queue");
		publishString("Sixth message......", channel, "task_queue");
		// end sample code

	/*	channel.queueDeclare("control_queue", true, false, false, null);
		channel.queueDeclare("work_queue", true, false, false, null);

		publishFile("messages/Control.txt", channel, "control_queue");
		publishFile("messages/Work.txt", channel, "work_queue"); */

		channel.close();
		connection.close();
	}

	private static void publishString(String message, Channel channel,
			String routingKey) {
		try {
			channel.basicPublish("", routingKey,
					MessageProperties.PERSISTENT_TEXT_PLAIN,
					message.getBytes("UTF-8"));
			System.out.println(" [x] Sent '" + message + "'");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	private static void publishFile(String fileName, Channel channel,
			String routingKey) {

		try {

			File file = new File(fileName);
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			String line = null;
			while ((line = br.readLine()) != null) {
				String message = line;
				// System.out.println(message);
				channel.basicPublish("", routingKey,
						MessageProperties.PERSISTENT_TEXT_PLAIN,
						message.getBytes("UTF-8"));
				System.out.println(" --> Sent '" + message + "'");
			}
			br.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

}
