package org.opennms.jti.generator.app;


import java.util.Date;
import java.util.Random;

import org.opennms.jti.generator.proto.Port;
import org.opennms.jti.generator.proto.TelemetryTop;
import org.opennms.jti.generator.proto.Port.InterfaceInfos;

public class JtiMessage {

    public static TelemetryTop.TelemetryStream buildJtiMessage(String ipAddress, int numOfInterfaces, long ifInOctets, long ifOutOctets, int sequenceNumber) {
    	String interfaceName = "eth";
    	Port.GPort.Builder builder = Port.GPort.newBuilder();
    	sequenceNumber++;
    	Random rnd = new Random();
    	int random = rnd.nextInt(32);
    	for(int i=0; i< numOfInterfaces; i++) {
    		InterfaceInfos interfaceInfos = Port.InterfaceInfos.newBuilder()
    		.setIfName(interfaceName + i)
            .setInitTime(1457647123)
            .setSnmpIfIndex(512+i)
            .setParentAeName("ae0")
            .setIngressStats(Port.InterfaceStats.newBuilder()
                    .setIfOctets(ifInOctets)
                    .setIfPkts(48 + 8 * i)
                    .setIf1SecPkts(i)
                    .setIf1SecOctets(random)
                    .setIfUcPkts(16 + i * 2)
                    .setIfMcPkts(24 + i * 4)
                    .setIfBcPkts(8 + i * 2)
                    .build())
            .setEgressStats(Port.InterfaceStats.newBuilder()
                    .setIfOctets(ifOutOctets)
                    .setIfPkts(24 + 12 * i)
                    .setIf1SecPkts(i)
                    .setIf1SecOctets(random)
                    .setIfUcPkts(4 + 2 * i)
                    .setIfMcPkts(12 + 6 * i)
                    .setIfBcPkts(8 + 4 * i)
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
                .setSequenceNumber(49103 + sequenceNumber)
                .setTimestamp(new Date().getTime())
                .setEnterprise(sensors)
                .build();

        return jtiMsg;

    }

}