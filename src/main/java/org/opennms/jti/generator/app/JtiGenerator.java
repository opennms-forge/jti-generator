package org.opennms.jti.generator.app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.opennms.jti.generator.proto.TelemetryTop;

public class JtiGenerator {

	JtiGenerator() {

	}
    static int sequenceNumber = 16;
    
	public static void main(String[] args) {

		Options options = new Options();
		Option nodes = new Option("n", "nodes", true, "number of nodes");
		nodes.setRequired(true);
		options.addOption(nodes);

		Option interfaces = new Option("i", "interfaces", true, "number of interfaces");
		interfaces.setRequired(true);
		options.addOption(interfaces);

		Option rate = new Option("r", "rate", true, "number of interfaces");
		interfaces.setRequired(true);
		options.addOption(rate);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("utility-name", options);

			System.exit(1);
			return;
		}

		final String numberOfNodes = cmd.getOptionValue("nodes");
		final String numberOfInterfaces = cmd.getOptionValue("interfaces");
		final String numberOfSecs = cmd.getOptionValue("rate");
		final String destination = args[args.length - 2];
		final String port = args[args.length - 1];

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		Runnable generateJti = new Runnable() {
			public void run() {
				JtiGenerator jti = new JtiGenerator();
				try {
					sequenceNumber++;
					jti.generateMessages(Integer.valueOf(numberOfNodes), Integer.valueOf(numberOfInterfaces), destination,
							Integer.valueOf(port), sequenceNumber);
				} catch (NumberFormatException | SocketException | UnknownHostException e) {
					e.printStackTrace();
				}
			}
		};

		ScheduledFuture<?> job = scheduler.scheduleAtFixedRate(generateJti, 0, Long.valueOf(numberOfSecs),
				TimeUnit.SECONDS);

	}

	public void generateMessages(int numOfNodes, int numOfInterfaces, String destination, int port, int sequenceNumber)
			throws UnknownHostException, SocketException {

		InetAddress address = InetAddress.getByName("192.168.1.1");
		IPAddress ipaddress = new IPAddress(address);
		InetAddress destinationAddr = InetAddress.getByName(destination);
		DatagramSocket socket = new DatagramSocket();
		for (int i = 0; i < numOfNodes; i++) {
			TelemetryTop.TelemetryStream jtiMsg = JtiMessage.buildJtiMessage(ipaddress.toString(), numOfInterfaces, 100,
					100, sequenceNumber);

			byte[] jtiMsgBytes = jtiMsg.toByteArray();

			DatagramPacket packet = new DatagramPacket(jtiMsgBytes, jtiMsgBytes.length, destinationAddr, port);

			try {
				socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}

			ipaddress = ipaddress.incr();
		}
		socket.close();

	}

}
