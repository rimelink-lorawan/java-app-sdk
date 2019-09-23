引用程序集
```
import com.rimelink.data.mqtt.Client;
```

## 类: Client
创建一个 client 实例:
```
Client client = new Client(broker, appId, appAccessKey);
```
*   `broker [String]`: MQTT服务器地址（如`lorawan.timeddd.com`）.
*   `appId [String]`: loraserver 中的 applicationId，同时也是MQTT服务器的授权用户名 (如 `9`).
*   `appAccessKey [String]`: 相应的访问密码(如 `DHhgiTB0Fsshel1HaMJgveh-PJX2yXEs5by-E0UdE9s`). 

## 回调: connect
连接服务器成功回调
```
client.onConnected((Connection _client) -> System.out.println("connected !"));
```

## 回调: error
无法连接，或连接异常断开时回调
```
client.onError((Throwable _error) -> System.err.println("error: " + _error.getMessage()));

```
*   `c`: 代表 client 实例本身。

## 回调: message
当接收到上行消息时回调
```
client.onMessage((String devEUI, UplinkMessage data) =>
{
    System.out.println("devEUI:" + devEUI);
    byte[] received = data.getData();
    String json = data.getPayloadRaw();
});
```
*   `devEUI [String]`: 节点的 devEUI, 如: `32343647015b003a`.
*   `message [Message]`: 上行消息对象，其成员：
*   `message.Data`:  上行数据的字节数组，节点发送的实际数据
*   `message.JSON`: 接收的原始 JSON 格式数据，ToString()后为：
```
{    
    "applicationID": "123",    
    "applicationName": "...",    
    "nodeName": "...",    
    "devEUI": "0202020202020202",    
    "rxInfo": [
        {            
            "mac": "0303030303030303",                 // MAC of the receiving gateway            
            "name": "rooftop-gateway",                 // name of the receiving gateway            
            "latitude": 52.3740364,                    // latitude of the receiving gateway            
            "longitude": 4.9144401,                    // longitude of the receiving gateway           
            "altitude": 10.5,                          // altitude of the receiving gateway            
            "time": "2016-11-25T16:24:37.295915988Z",  // time when the package was received (GPS time of gateway, only set when available)            
            "rssi": -57,                               // signal strength (dBm)            
            "loRaSNR": 10                              // signal to noise ratio
        }
    ],    
    "txInfo": {        
        "frequency": 868100000,    // frequency used for transmission        
        "dataRate": {            
            "modulation": "LORA",  // modulation (LORA or FSK)            
            "bandwidth": 250,      // used bandwidth            
            "spreadFactor": 5      // used SF (LORA)
            // "bitrate": 50000    // used bitrate (FSK)
        },        
        "adr": false,        
        "codeRate": "4/6"
    },    
    "fCnt": 10,                    // frame-counter    
    "fPort": 5,                    // FPort    
    "data": "...",                 // base64 encoded payload (decrypted)    
    "object": {                    // decoded object (when application coded has been configured)        
        "temperatureSensor": {"1": 25},        
        "humiditySensor": {"1": 32}
    }
}
```

## 方法: Start
启动 client 连接
```
client.start();
```
实例化 Client 之后，调用起方法进行连接。

## 方法: Send
向指定节点发送下行数据
```
send(String devEUI, byte[] data, int port)
```
*   `devEUI [String]`: 节点的 devEUI, 如: `32343647015b003a`.
*   `payload [byte[]]`: 字节数组，如 new byte[]{ 1, 2, 3, 4 }.
*   `port [int]`：整数，下行端口，如 10.

## 方法: End
关闭 client 连接
```
client.end();
```
在应用退出时，应调用此方法关闭客户端连接。