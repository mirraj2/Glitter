function Socket(ip, port, encrypted) {
  if (!window.WebSocket) {
    console.log("websockets not supported!");
    return;
  }

  var connected = false;
  var onOpen = function() {
  };
  var onClose = function() {
  };
  var onMessage = function() {
  };

  var socket;
  var waitTime = 1;
  var autoReconnect = true;

  var ret = {
    send : function(data) {
      socket.send(JSON.stringify(data));
      return this;
    },
    open : function() {
      socket = new WebSocket((encrypted ? "wss" : "ws") + "://" + ip + ":" + port + "/");
      socket.onopen = function() {
        connected = true;
        waitTime = 1;
        onOpen();
      };
      socket.onclose = function() {
        connected = false;
        if (autoReconnect) {
          reconnect(waitTime);
        }
        waitTime *= 2;
        onClose();
      };
      socket.onmessage = function(msg) {
        var data = JSON.parse(msg.data);
        if (Array.isArray(data)) {
          for (var i = 0; i < data.length; i++) {
            onMessage(data[i]);
          }
        } else {
          onMessage(data);
        }
      };
      return this;
    },
    onOpen : function(e) {
      onOpen = e;
      return this;
    },
    onClose : function(e) {
      onClose = e;
      return this;
    },
    onMessage : function(e) {
      onMessage = e;
      return this;
    },
    isConnected : function() {
      return connected;
    },
    close : function() {
      autoReconnect = false;
      socket.close();
      return this;
    }
  };

  function reconnect(t) {
    console.log("Attempting to reconnect in " + t + " seconds.");
    setTimeout(function() {
      ret.open();
    }, t * 1000);
  }

  return ret;
}
