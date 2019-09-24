# java-app-sdk
锐米 loraserver 应用 MQTT 通信数据SDK

### 环境说明
JDK 1.8 及以上

### 使用示例
#### SDK 及其依赖包
- com.rimelink.data.mqtt.sdk-1.0.jar
- jackson-annotations-2.8.7.jar
- jackson-core-2.8.7.jar
- jackson-databind-2.8.7.jar
- org.eclipse.paho.client.mqttv3-1.1.1.jar

可从这里下载：[https://github.com/rimelink-lorawan/java-app-sdk/releases](https://github.com/rimelink-lorawan/java-app-sdk/releases)

#### 创建测试使用类 Test.java
在依赖包的同一目录下创建 Test.java
```
import com.rimelink.data.common.Connection;
import com.rimelink.data.common.messages.UplinkMessage;
import com.rimelink.data.mqtt.Client;

public class Test {

	public static void main(String[] args) throws Exception{
		Client client = new Client("lorawan.timeddd.com", "15", "151515#");

		client.onMessage((String devEUI, UplinkMessage data) -> {
			try {
				byte[] received = data.getData();
				System.out.println("received: devEUI=" + devEUI + " \r\n data: " + bytesToHex(received));

				System.out.println("send led ");
				client.send(devEUI, "led".getBytes(), 1);
			} catch (Exception ex) {
				System.out.println("exception: " + ex.getMessage());
			}
		});

		client.onError((Throwable _error) -> System.err.println("error: " + _error.getMessage()));

		client.onConnected((Connection _client) -> System.out.println("connected !"));

		client.start();
	}
 
	public static String bytesToHex(byte[] bytes) {  
	    StringBuffer sb = new StringBuffer();  
	    for(int i = 0; i < bytes.length; i++) {  
	        String hex = Integer.toHexString(bytes[i] & 0xFF);  
	        if(hex.length() < 2) sb.append(0);  
            
	        sb.append(hex);
	        sb.append(" ");  
	    }  
	    return sb.toString();  
	} 
}
```

#### 编译与运行
在 windows 下可创建 .bat 批处理文件，如取名 run.bat 放于同一目录下。
SDK 要求 java 1.8 及以上，批处理中，D:\Java\jdk1.8.0_144\bin 表示jdk命令安装的位置，请修改为实际安装位置。
```
set path=%path%;D:\Java\jdk1.8.0_144\bin
javac -Djava.ext.dirs=. Test.java
java -cp .;com.rimelink.data.mqtt.sdk-1.0.jar;org.eclipse.paho.client.mqttv3-1.1.1.jar;jackson-core-2.8.7.jar;jackson-databind-2.8.7.jar;jackson-annotations-2.8.7.jar Test
```
运行结果：
```text
connected !
received: devEUI=32343647144d004b
 data: 41 cb 85 20 42 59 89 77 41 78 fe ac ff ff 05
send led
received: devEUI=32343647144d002d
 data: 41 cc a3 d8 42 58 9c 02 41 7a 01 19 ff ff 06
send led
received: devEUI=32343647144b0031
 data: 41 ca e1 48 42 54 73 17 41 71 e7 99 ff ff 05
send led
```

