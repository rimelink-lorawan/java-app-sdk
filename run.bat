set path=%path%;D:\Java\jdk1.8.0_144\bin
javac -Djava.ext.dirs=. Test.java
java -cp .;com.rimelink.data.mqtt.sdk-1.0.jar;org.eclipse.paho.client.mqttv3-1.1.1.jar;jackson-core-2.8.7.jar;jackson-databind-2.8.7.jar;jackson-annotations-2.8.7.jar Test
