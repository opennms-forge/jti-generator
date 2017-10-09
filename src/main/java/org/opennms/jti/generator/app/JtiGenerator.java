package org.opennms.jti.generator.app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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

    JtiGenerator() throws SocketException {
        socket = new DatagramSocket();
    }

    static int sequenceNumber = 1;

    private DatagramSocket socket;

    public static void main(String[] args) throws SocketException {

        Options options = new Options();

        Option nodes = new Option("n", "nodes", true, "number of nodes");
        options.addOption(nodes);

        Option interfaces = new Option("i", "interfaces", true, "number of interfaces");
        options.addOption(interfaces);

        Option rate = new Option("r", "rate", true, "rate at which messages get generated in seconds");
        options.addOption(rate);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        List<String> argsList = new ArrayList<>();
        String cmdLineSyntax = "jti-generator --nodes <count> --interfaces <count> --rate <seconds>  <ip-address> <port-number>";
        String footer = " <ip-address> \n <port-number>";

        try {
            cmd = parser.parse(options, args, true);
            argsList = cmd.getArgList();
        } catch (ParseException e) {
            System.out.println("Error in parsing arguments");
            formatter.printHelp(cmdLineSyntax, "", options, footer);
            System.exit(1);
            return;
        }
        if (argsList.size() < 2) {
            System.out.println("Incomplete arguments, specify IPAddress and Port number");
            formatter.printHelp(cmdLineSyntax, "", options, footer);
            System.exit(1);
            return;
        }
        final String numberOfNodes = cmd.getOptionValue("nodes", "1000");
        final String numberOfInterfaces = cmd.getOptionValue("interfaces", "50");
        final String numberOfSecs = cmd.getOptionValue("rate", "30");
        final String destination = argsList.get(0);
        final String port = argsList.get(1);
        System.out.printf("Sending JTI messages to %s with the interval of %s secs \n", destination, numberOfSecs);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable generateJti = new Runnable() {
            public void run() {

                try {
                    sequenceNumber++;
                    JtiGenerator jti = new JtiGenerator();
                    jti.generateMessages(Integer.valueOf(numberOfNodes), Integer.valueOf(numberOfInterfaces),
                            destination, Integer.valueOf(port), sequenceNumber);
                } catch (NumberFormatException e) {
                    System.out.println(" Error, invalid arguments");
                    System.exit(1);
                } catch (SocketException e1) {
                    System.out.println("Error, connecting to socket");
                    System.exit(1);
                } catch (UnknownHostException e2) {
                    System.out.println("Error, invalid IPAddress");
                    System.exit(1);
                } 
            }
        };

        ScheduledFuture<?> job = scheduler.scheduleAtFixedRate(generateJti, 0, Long.valueOf(numberOfSecs),
                TimeUnit.SECONDS);

    }

    public void generateMessages(int numOfNodes, int numOfInterfaces, String destination, int port, int sequenceNumber)
            throws UnknownHostException {

        InetAddress address = InetAddress.getByName("192.168.1.1");
        IPAddress ipaddress = new IPAddress(address);
        InetAddress destinationAddr = InetAddress.getByName(destination);

        for (int i = 0; i < numOfNodes; i++) {
            TelemetryTop.TelemetryStream jtiMsg = JtiMessage.buildJtiMessage(ipaddress.toString(), numOfInterfaces, 100,
                    100, sequenceNumber);
            byte[] jtiMsgBytes = jtiMsg.toByteArray();

            DatagramPacket packet = new DatagramPacket(jtiMsgBytes, jtiMsgBytes.length, destinationAddr, port);

            try {
                socket.send(packet);
                System.out.print(".");
            } catch (IOException e) {
                System.out.println("Error while sending packet");
            }
            ipaddress = ipaddress.incr();
        }
        System.out.printf("\n sent jti messages from %d nodes and %d interfaces \n", numOfNodes, numOfInterfaces);

    }

}
