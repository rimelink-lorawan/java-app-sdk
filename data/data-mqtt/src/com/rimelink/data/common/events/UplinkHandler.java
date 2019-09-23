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
package com.rimelink.data.common.events;

import com.rimelink.data.common.Subscribable;
import com.rimelink.data.common.messages.DataMessage;
import com.rimelink.data.common.messages.UplinkMessage;

/**
 * 上行数据消息处理
 */
public abstract class UplinkHandler implements EventHandler {
	private String _appId;
	
	public UplinkHandler(String appId) {
		_appId = appId;
	}

    public abstract void handle(String _devEUI, UplinkMessage _data); 

    @Override
    public void subscribe(Subscribable _client) throws Exception { 
    	
    	_client.subscribe(new String[]{
                "application",
                _appId,
                "node",
                _client.getWordWildcard(),
                "rx"
            });
    	
    	_client.subscribe(new String[]{
                "application",
                _appId,
                "device",
                _client.getWordWildcard(),
                "rx"
            });
    }
}
