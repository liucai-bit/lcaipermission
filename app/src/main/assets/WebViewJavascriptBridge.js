window.__LCAI_BRIDGE_LOADED__ = true;

(function (global) {
    'use strict';

    /* 版本号，供原生 JS_CHECK_BRIDGE_VERSION 检测 */
    var VERSION = '1.1.0';

    /* 已存在则不覆盖（双保险，Android 端也有检测） */
    if (global.WebViewJavascriptBridge) {
        return;
    }

    /* ======================== 常量 ======================== */
    var YY_OVERRIDE_SCHEMA = 'yy://';
    var YY_RETURN_DATA = YY_OVERRIDE_SCHEMA + 'return/';
    var YY_FETCH_QUEUE = YY_RETURN_DATA + '_fetchQueue/';
    var CALLBACK_ID_FORMAT = 'JS_CB_%s';
    var CALLBACK_TIMEOUT_MS = 30000;

    /* ======================== 内部状态 ======================== */
    var messageHandlers = {};
    var responseCallbacks = {};
    var uniqueId = 1;
    var sendMessageQueue = [];
    var messageFlushTimer = null;

    /* ======================== 工具函数 ======================== */
    function isFunction(obj) {
        return typeof obj === 'function';
    }

    function safeStringify(obj) {
        if (obj === undefined || obj === null) return '';
        if (typeof obj === 'string') return obj;
        try {
            return JSON.stringify(obj);
        } catch (e) {
            return String(obj);
        }
    }

    function genCallbackId() {
        return CALLBACK_ID_FORMAT.replace('%s', String(uniqueId++));
    }

    /* ======================== 发送消息到原生 ======================== */
    function _doSend(message, responseCallback) {
        if (responseCallback) {
            var callbackId = genCallbackId();
            responseCallbacks[callbackId] = {
                callback: responseCallback,
                timer: setTimeout(function () {
                    var pending = responseCallbacks[callbackId];
                    if (pending) {
                        delete responseCallbacks[callbackId];
                        try {
                            pending.callback(null, 'timeout');
                        } catch (e) {
                            console.error('[JSBridge] timeout callback error', e);
                        }
                    }
                }, CALLBACK_TIMEOUT_MS)
            };
            message['callbackId'] = callbackId;
        }
        sendMessageQueue.push(message);
        _scheduleFlush();
    }

    function _scheduleFlush() {
        if (messageFlushTimer) return;
        messageFlushTimer = setTimeout(function () {
            messageFlushTimer = null;
            _dispatchMessageToNative();
        }, 0);
    }

    function _dispatchMessageToNative() {
        _triggerNative(YY_OVERRIDE_SCHEMA);
    }

    function _triggerNative(url) {
        var iframe = null;
        try {
            iframe = document.createElement('iframe');
            iframe.style.display = 'none';
            iframe.style.width = '0';
            iframe.style.height = '0';
            iframe.style.border = 'none';
            iframe.src = url;
            document.documentElement.appendChild(iframe);
            setTimeout(function () {
                try {
                    if (iframe && iframe.parentNode) {
                        iframe.parentNode.removeChild(iframe);
                    }
                } catch (e) { /* ignore */ }
                iframe = null;
            }, 200);
        } catch (e) {
            try {
                window.location.href = url;
            } catch (e2) {
                console.error('[JSBridge] trigger native failed', e2);
            }
        }
    }

    /* ======================== 原生调用 H5 ======================== */
    function _handleMessageFromNative(messageJSON) {
        var message;
        try {
            message = typeof messageJSON === 'string' ? JSON.parse(messageJSON) : messageJSON;
        } catch (e) {
            console.error('[JSBridge] parse native message failed', e);
            return;
        }

        if (!message) return;

        if (message.responseId) {
            var pending = responseCallbacks[message.responseId];
            if (pending) {
                if (pending.timer) clearTimeout(pending.timer);
                delete responseCallbacks[message.responseId];
                try {
                    pending.callback(message.responseData, null);
                } catch (e) {
                    console.error('[JSBridge] response callback error', e);
                }
            }
            return;
        }

        var handler = messageHandlers[message.handlerName];
        if (!handler) {
            console.warn('[JSBridge] handler not registered: ' + message.handlerName);
            if (message.callbackId) {
                _doSend({
                    responseId: message.callbackId,
                    responseData: safeStringify({ error: 'handler not found: ' + message.handlerName })
                });
            }
            return;
        }

        var responseCallback = (function (callbackId) {
            var called = false;
            return function (responseData) {
                if (called) return;
                called = true;
                _doSend({
                    responseId: callbackId,
                    responseData: safeStringify(responseData)
                });
            };
        })(message.callbackId);

        try {
            handler(message.data, responseCallback);
        } catch (e) {
            console.error('[JSBridge] handler error: ' + message.handlerName, e);
            if (message.callbackId) {
                responseCallback(safeStringify({ error: String(e && e.message || e) }));
            }
        }
    }

    function _fetchQueue() {
        var queue = sendMessageQueue;
        sendMessageQueue = [];
        return JSON.stringify(queue);
    }

    /* ======================== H5 调用原生 ======================== */
    function callHandler(handlerName, data, callback) {
        if (arguments.length === 2 && isFunction(data)) {
            callback = data;
            data = null;
        }
        var message = {
            handlerName: handlerName,
            data: safeStringify(data)
        };
        _doSend(message, function (responseData, error) {
            if (isFunction(callback)) {
                callback(responseData, error);
            }
        });
    }

    function send(data, callback) {
        _doSend({
            data: safeStringify(data)
        }, function (responseData, error) {
            if (isFunction(callback)) {
                callback(responseData, error);
            }
        });
    }

    /* ======================== H5 注册方法 ======================== */
    function registerHandler(handlerName, handler) {
        messageHandlers[handlerName] = handler;
    }

    function unregisterHandler(handlerName) {
        delete messageHandlers[handlerName];
    }

    /* ======================== 初始化 ======================== */
    function _initBridge() {
        var bridge = {
            __version__: VERSION,
            callHandler: callHandler,
            send: send,
            registerHandler: registerHandler,
            unregisterHandler: unregisterHandler,
            _handleMessageFromNative: _handleMessageFromNative,
            _fetchQueue: _fetchQueue
        };

        global.WebViewJavascriptBridge = bridge;

        _dispatchReadyEvent();

        /* 延迟通知原生，等原生 WebViewClient 就绪 */
        setTimeout(function () {
            _dispatchMessageToNative();
        }, 50);

        bridge.__debug = function () {
            return {
                queueLen: sendMessageQueue.length,
                pendingCallbacks: Object.keys(responseCallbacks).length,
                registeredHandlers: Object.keys(messageHandlers)
            };
        };
    }

    function _dispatchReadyEvent() {
        var ok = false;
        try {
            var evt = document.createEvent('Events');
            evt.initEvent('WebViewJavascriptBridgeReady', true, true);
            document.dispatchEvent(evt);
            ok = true;
        } catch (e) {
            /* ignore */
        }
        if (!ok) {
            try {
                var evt2 = document.createEvent('Event');
                evt2.initEvent('WebViewJavascriptBridgeReady', true, true);
                document.dispatchEvent(evt2);
                ok = true;
            } catch (e2) {
                console.warn('[JSBridge] dispatch ready event failed', e2);
            }
        }
    }

    _initBridge();

    if (typeof module !== 'undefined' && module.exports) {
        module.exports = global.WebViewJavascriptBridge;
    }

})(typeof window !== 'undefined' ? window : this);

window.__LCAI_BRIDGE_SCRIPT_END__ = true;