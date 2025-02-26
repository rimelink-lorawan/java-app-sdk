/*
 * The MIT License
 *
 * Copyright (c) 2019 Rimelink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.rimelink.data.mqtt;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.rimelink.data.common.AbstractClient;
import com.rimelink.data.common.Connection;
import com.rimelink.data.common.Subscribable;
import com.rimelink.data.common.events.ConnectHandler;
import com.rimelink.data.common.events.ErrorHandler;
import com.rimelink.data.common.events.EventHandler;
import com.rimelink.data.common.events.UplinkHandler;
import com.rimelink.data.common.messages.UplinkMessage;

/**
 * Mqtt 客户端类
 */
public class Client extends AbstractClient {

    /**
     * 连接相关信息
     */
    private final String broker;
    private final String appId;
    private MqttClientPersistence persistence = new MemoryPersistence();
    private final MqttConnectOptions connOpts;

    /**
     * 事件
     */
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<Class, List<EventHandler>> handlers = new HashMap<>();

    /**
     * 连接实例 
     */
    private MqttClient mqttClient;

    /**
     * 构建
     *
     * @param _broker MQTT服务器
     * @param _appId loraserver 中applicationId 同时是 mqtt 用户名
     * @param _appAccessKey 密码
     * @throws java.net.URISyntaxException 
     */
    public Client(String _broker, String _appId, String _appAccessKey) throws URISyntaxException {
        this(_broker, _appId, _appAccessKey, null);
    }

    /**
     * 构建
     *
     * @param _broker MQTT服务器
     * @param _appId loraserver 中applicationId 同时是 mqtt 用户名
     * @param _appAccessKey 密码
     * @param _connOpts MQTT 连接选项
     * @throws java.net.URISyntaxException  
     */
    public Client(String _broker, String _appId, String _appAccessKey, MqttConnectOptions _connOpts) throws URISyntaxException {
        broker = validateBroker(_broker);
        appId = _appId;
        if (_connOpts != null) {
            connOpts = _connOpts;
        } else {
            connOpts = new MqttConnectOptions();
        }
        connOpts.setUserName(_appId);
        connOpts.setPassword(_appAccessKey.toCharArray());
    }

    private String validateBroker(String _source) throws URISyntaxException {

        URI tempBroker = new URI(_source);

        if ("tcp".equals(tempBroker.getScheme())) {
            if (tempBroker.getPort() == -1) {
                return tempBroker.toString() + ":1883";
            } else {
                return tempBroker.toString();
            }
        } else if ("ssl".equals(tempBroker.getScheme())) {
            if (tempBroker.getPort() == -1) {
                return tempBroker.toString() + ":8883";
            } else {
                return tempBroker.toString();
            }
        } else if (tempBroker.getPort() != -1) {
            return "tcp://" + tempBroker.toString();
        } else {
            return "tcp://" + tempBroker.toString() + ":1883";
        }
    }
 
    @Override
    public Client start() throws MqttException, Exception {
        if (mqttClient != null) {
            throw new RuntimeException("Already connected");
        }
        mqttClient = new MqttClient(broker, MqttClient.generateClientId(), persistence);
        mqttClient.connect(connOpts);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                mqttClient = null;
                if (handlers.containsKey(ErrorHandler.class)) {
                    handlers.get(ErrorHandler.class).stream().forEach((handler) -> {
                        executor.submit(() -> {
                            ((ErrorHandler) handler).safelyHandle(cause);
                        });
                    });
                }
            }
 
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception { 
                String[] tokens = topic.split("\\/");
                if (tokens.length < 4) return;  
                	 
                String type = tokens[tokens.length - 1];
                if ("rx".equals(type) || "up".equals(type)) {
                    version = "up".equals(type) ? 3 : 0;				 
	                Map kvmap = MAPPER.readValue(message.getPayload(), Map.class);
	                
	                String devEUI = kvmap.get("devEUI").toString();    
	                String payload = kvmap.get("data").toString();    
	                byte[] data = Base64.getDecoder().decode(payload);   
					
					if (handlers.containsKey(UplinkHandler.class)) {
						handlers.get(UplinkHandler.class).stream().forEach((handler) -> {
							executor.submit(() -> {
								try {
									UplinkHandler uh = (UplinkHandler) handler;
									uh.handle(devEUI, new UplinkMessage(payload, data)); 
								} catch (Exception ex) {
									if (handlers.containsKey(ErrorHandler.class)) {
										handlers.get(ErrorHandler.class).stream().forEach((handler1) -> {
											executor.submit(() -> {
												((ErrorHandler) handler1).safelyHandle(ex);
											});
										});
									}
								}
							});
						});
					}
				}
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                /**
                 * Not supported for now
                 */
            }
        });

        for (List<EventHandler> ehl : handlers.values()) {
            for (EventHandler eh : ehl) {
                eh.subscribe(new Subscribable() {

                    private static final String WILDCARD_WORD = "+";
                    private static final String WILDCARD_PATH = "#";

                    @Override
                    public void subscribe(String[] _key) throws Exception {
                        StringJoiner sj = new StringJoiner("/");
                        for (String key : _key) {
                            sj.add(key);
                        }
                        mqttClient.subscribe(sj.toString());
                    }

                    @Override
                    public String getWordWildcard() {
                        return WILDCARD_WORD;
                    }

                    @Override
                    public String getPathWildcard() {
                        return WILDCARD_PATH;
                    }
                });
            }
        }

        if (handlers.containsKey(ConnectHandler.class)) {
            handlers.get(ConnectHandler.class).stream().forEach((handler) -> {
                executor.submit(() -> {
                    try {
                        ((ConnectHandler) handler).handle(() -> mqttClient);
                    } catch (Exception ex) {
                        if (handlers.containsKey(ErrorHandler.class)) {
                            handlers.get(ErrorHandler.class).stream().forEach((handler1) -> {
                                executor.submit(() -> {
                                    ((ErrorHandler) handler1).safelyHandle(ex);
                                });
                            });
                        }
                    }
                });
            });
        }
        return this;
    } 

    @Override
    public Client end() throws MqttException, InterruptedException {
        if (mqttClient == null) {
            throw new RuntimeException("Not connected");
        }
        return end(5000);
    }

    @Override
    public Client end(long _timeout) throws MqttException, InterruptedException {
        if (mqttClient == null) {
            throw new RuntimeException("Not connected");
        }
        executor.awaitTermination(_timeout, TimeUnit.MILLISECONDS);
        mqttClient.disconnect(_timeout);
        if (!mqttClient.isConnected()) {
            mqttClient = null;
        }
        return this;
    }

    @Override
    public Client endNow() throws MqttException {
        if (mqttClient == null) {
            throw new RuntimeException("Not connected");
        }
        mqttClient.disconnectForcibly(0, 0);
        mqttClient = null;
        return this;
    }

    @Override
    public void send(String _devEUI, byte[] _data, int _port) throws Exception{
    	send(_devEUI, _data, _port, false);
    }
    
    private int version = 0;
    
    @Override
    public void send(String _devEUI, byte[] _data, int _port, boolean _confirmed) throws Exception {
    	Map<String, Object> downData = new HashMap<String, Object>();
        downData.put("dev_eui", _devEUI);
        downData.put("confirmed", _confirmed);
        downData.put("fPort", _port);
        downData.put("data", Base64.getEncoder().encode(_data));
        
        byte[] payload = MAPPER.writeValueAsBytes(downData);
        if(version < 3) {
            mqttClient.publish("application/" + appId + "/node/" + _devEUI + "/tx", payload, 0, false);
            mqttClient.publish("application/" + appId + "/device/" + _devEUI + "/tx", payload, 0, false);
        }else {
            mqttClient.publish("application/" + appId + "/device/" + _devEUI + "/command/down", payload, 0, false);
        }
    }

    @Override
    public Client onConnected(Consumer<Connection> _handler) {
        if (mqttClient != null) {
            throw new RuntimeException("Already connected");
        }
        if (!handlers.containsKey(ConnectHandler.class)) {
            handlers.put(ConnectHandler.class, new LinkedList<>());
        }
        handlers.get(ConnectHandler.class).add(new ConnectHandler() {
            @Override
            public void handle(Connection _client) {
                _handler.accept(_client);
            }
        });
        return this;
    }

    @Override
    public Client onError(Consumer<Throwable> _handler) {
        if (mqttClient != null) {
            throw new RuntimeException("Already connected");
        }
        if (!handlers.containsKey(ErrorHandler.class)) {
            handlers.put(ErrorHandler.class, new LinkedList<>());
        }
        handlers.get(ErrorHandler.class).add(new ErrorHandler() {
            @Override
            public void handle(Throwable _error) {
                _handler.accept(_error);
            }
        });
        return this;
    }

    @Override
    public Client onMessage(BiConsumer<String, UplinkMessage> _handler) { 
        if (mqttClient != null) {
            throw new RuntimeException("Already connected");
        }
        if (!handlers.containsKey(UplinkHandler.class)) {
            handlers.put(UplinkHandler.class, new LinkedList<>());
        }
        handlers.get(UplinkHandler.class).add(new UplinkHandler(appId) {
            @Override
            public void handle(String _devEUI, UplinkMessage _data) {
                _handler.accept(_devEUI, _data);
            } 
        });
        return this;
    }   
}
