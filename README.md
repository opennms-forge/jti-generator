# jti-generator

This is an app to generate JTI (Juniper Telemetry Interface) messages.


## compile:
```
mvn install
```

sample command:
```
java -jar jti-generator.jar --nodes 1000 --interfaces 50 --rate 30 127.0.0.1 50000
```

#### nodes :      
These are local IPs starts at 192.168.1.1, need to specify number of nodes, Default is 1000.

#### interfaces : 
need to specify number of interfaces. Interfaces named as eth0, eth1, eth2 etc. Default is 50.

### rate :       
need to specify the interval at which messages are generated, Default is 30 secs.

#### destination, port :  
Destination IP address and port at which messages needs to be sent.


