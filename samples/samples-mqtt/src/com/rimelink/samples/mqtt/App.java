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
package com.rimelink.samples.mqtt;

import com.rimelink.data.common.Connection;
import com.rimelink.data.common.messages.UplinkMessage;
import com.rimelink.data.mqtt.Client;

public class App {

	public static void main(String[] args) throws Exception{
		Client client = new Client("localhost", "1", "");

		client.onMessage((String devEUI, UplinkMessage data) -> {
			try {
				byte[] received = data.getData();
				System.out.println("收到:devEUI=" + " 数据: " + bytesToHex(received));

				// 下发
				System.out.println("下发 led ");
				client.send(devEUI, "led".getBytes(), 1);
			} catch (Exception ex) {
				System.out.println("异常: " + ex.getMessage());
			}
		});

		client.onError((Throwable _error) -> System.err.println("error: " + _error.getMessage()));

		client.onConnected((Connection _client) -> System.out.println("connected !"));

		client.start();
	}

	/**
	 * 字节转 16 进制字符
	 * @param bytes
	 * @return
	 */
	public static String bytesToHex(byte[] bytes) {  
	    StringBuffer sb = new StringBuffer();  
	    for(int i = 0; i < bytes.length; i++) {  
	        String hex = Integer.toHexString(bytes[i] & 0xFF);  
	        if(hex.length() < 2){  
	            sb.append(0);  
	        }  
	        sb.append(hex);
	        sb.append(" ");  
	    }  
	    return sb.toString();  
	}

}
