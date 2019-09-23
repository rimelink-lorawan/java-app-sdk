/*
 * The MIT License
 *
 * Copyright (c) 2017 Rimelink
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
package com.rimelink.data.common;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.rimelink.data.common.messages.UplinkMessage;
import com.rimelink.data.mqtt.Client;

/**
 * loraserver mqtt 客户端抽象类 
 */
public abstract class AbstractClient {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER
                .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 启动
     *
     * @return the Client instance
     * @throws Exception in case something goes wrong
     */
    public abstract AbstractClient start() throws Exception;

    /**
     * 停止 超时时间 5000 ms
     *
     * @return the Client instance
     * @throws Exception in case something goes wrong
     */
    public abstract AbstractClient end() throws Exception;

    /**
     * 停止，指定超时时间
     *
     * @param _timeout 超时时间
     * @return the Client instance
     * @throws Exception in case something goes wrong
     */
    public abstract AbstractClient end(long _timeout) throws Exception;

    /**
     * 立即停止
     *
     * @return the Client instance
     * @throws Exception in case something goes wrong
     */
    public abstract AbstractClient endNow() throws Exception;

    /**
     * 下发
     * 
     * @param _devEUI
     * @param _data
     * @param _port
     * @throws Exception
     */
    public abstract void send(String _devEUI, byte[] _data, int _port) throws Exception;
    
    /**
     * 下发
     * 
     * @param _devEUI
     * @param _data
     * @param _port
     * @param _confirmed
     * @throws Exception
     */
    public abstract void send(String _devEUI, byte[] _data, int _port, boolean _confirmed) throws Exception;

    /**
     * 连接建立事件
     *
     * @param _handler  
     * @return the Connection instance
     * @throws Exception in case something goes wrong
     */
    public abstract AbstractClient onConnected(Consumer<Connection> _handler) throws Exception;

    /**
     * 错误时
     *
     * @param _handler  
     * @return the Client instance
     * @throws Exception in case something goes wrong
     */
    public abstract AbstractClient onError(Consumer<Throwable> _handler) throws Exception;

    /**
     * 上行数据消息时
     * 
     * @param _handler
     * @return
     */
    public abstract Client onMessage(BiConsumer<String, UplinkMessage> _handler);
 

}
