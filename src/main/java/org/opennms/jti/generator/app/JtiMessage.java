package org.opennms.jti.generator.app;


import java.util.Date;

import org.opennms.jti.generator.proto.Port;
import org.opennms.jti.generator.proto.TelemetryTop;
import org.opennms.jti.generator.proto.Port.InterfaceInfos;

public class JtiMessage {

    public static TelemetryTop.TelemetryStream buildJtiMessage(String ipAddress, long numOfInterfaces, long ifInOctets, long ifOutOctets) {
    	String interfaceName = "eth0-";
    	Port.GPort.Builder builder = Port.GPort.newBuilder();

    	for(long i=0; i<= numOfInterfaces; i++) {
    		InterfaceInfos interfaceInfos = Port.InterfaceInfos.newBuilder()
    		.setIfName(interfaceName + i)
            .setInitTime(1457647123)
            .setSnmpIfIndex(517)
            .setParentAeName("ae0")
            .setIngressStats(Port.InterfaceStats.newBuilder()
                    .setIfOctets(ifInOctets)
                    .setIfPkts(1)
                    .setIf1SecPkts(1)
                    .setIf1SecOctets(1)
                    .setIfUcPkts(1)
                    .setIfMcPkts(1)
                    .setIfBcPkts(1)
                    .build())
            .setEgressStats(Port.InterfaceStats.newBuilder()
                    .setIfOctets(ifOutOctets)
                    .setIfPkts(1)
                    .setIf1SecPkts(1)
                    .setIf1SecOctets(1)
                    .setIfUcPkts(1)
                    .setIfMcPkts(1)
                    .setIfBcPkts(1)
                    .build())
            .build();
    		
    		builder.addInterfaceStats(interfaceInfos);
    	}
    	
    	final Port.GPort port = builder.build();
    	
        final TelemetryTop.JuniperNetworksSensors juniperNetworksSensors = TelemetryTop.JuniperNetworksSensors.newBuilder()
                .setExtension(Port.jnprInterfaceExt, port)
                .build();

        final TelemetryTop.EnterpriseSensors sensors = TelemetryTop.EnterpriseSensors.newBuilder()
                .setExtension(TelemetryTop.juniperNetworks, juniperNetworksSensors)
                .build();

        final TelemetryTop.TelemetryStream jtiMsg = TelemetryTop.TelemetryStream.newBuilder()
                .setSystemId(ipAddress)
                .setComponentId(0)
                .setSensorName("intf-stats")
                .setSequenceNumber(49103)
                .setTimestamp(new Date().getTime())
                .setEnterprise(sensors)
                .build();

        return jtiMsg;

    }

   /* @Ignore
    @Test
    public void canSendJtiMessage() throws IOException {
        TelemetryTop.TelemetryStream jtiMsg = buildJtiMessage("192.168.2.1", "eth0", 100, 100);
        byte[] jtiMsgBytes = jtiMsg.toByteArray();

        InetAddress address = InetAddressUtils.getLocalHostAddress();
        DatagramPacket packet = new DatagramPacket(jtiMsgBytes, jtiMsgBytes.length, address, 50000);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
    }*/
}