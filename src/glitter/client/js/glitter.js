$(function() {
  var socket = new Socket("$$(websocketIP)", "$$(websocketPort)", false);
  socket.open();

  canvas = new Canvas();
  loop = new GameLoop();

  loop.start();
});
