# What is this?
Vertx provides a clustered session store and a local session store for web sessions.
Vertx also provides support for web sockets via SockJS.

If you use the local session store, any web socket "PING" will extend the web session timeout.
If you use the clustered session store, a different mechanism for the web session timeout 
is used and the mechanism to extend the timeout does not work.

# How to use this
1. Start _de.apwolf.Main#main_ with _useClusteredSessionStore=true_
2. After the backend has started, perform a REST request: http://localhost:8086/test
   1. You will see this log line: Received REST request on /test with session _someId_
3. Open [the sockjs client](src/test/resources/sockjsclient.html)
   4. A web socket session will start, you will see this log line every 5 seconds: 
Received WS request SOCKET_PING for session _someId_
5. Wait 2 minutes (the timeout for the session)
6. Perform the same REST request again: http://localhost:8086/test  
&rarr; You will see a different session id

To see that it works with the local session store, redo all the steps with _useClusteredSessionStore=false_

# Details
The web session timeout for a websocket ping is extended in _io.vertx.ext.web.handler.sockjs.impl.
EventBusBridgeImpl#internalHandlePing_ by calling _io.vertx.ext.web.Session#setAccessed_.

_io.vertx.ext.web.sstore.impl.LocalSessionStoreImpl#handle_ checks _Session#lastAccessed_ to evaluate
whether a session was timed out, so everything works fine.

_io.vertx.ext.web.sstore.impl.ClusteredSessionStoreImpl_ does not use _Session#lastAccessed_,
it uses _io.vertx.core.shareddata.AsyncMap_ and its internal TTL mechanism.  
So the timeout extension performed by the event bus bridge is not honored and the session runs out
although the websocket session is still open.