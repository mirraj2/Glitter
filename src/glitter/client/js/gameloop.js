function GameLoop(canvas) {
  checkNotNull(canvas);
  this.canvas = canvas;
}

GameLoop.prototype.start = function() {
  var lastTime = performance.now(), t = 0;
  var lastFPSUpdate = 0;
  var fps = 0;
  var MAX_UPDATE_TIME = 100;
  
  function gameLoop() {
    var now = performance.now();
    if (now == lastTime) {
      requestAnimationFrame(gameLoop);
      return;
    }

    t = now - lastTime;
    lastTime = now;

    if (now - lastFPSUpdate >= 1000) {
      canvas.setFPS(fps);
      lastFPSUpdate = now;
    }

    var currentFPS = 1000 / t;
    fps = Math.round(currentFPS * .1 + fps * .9);

    while (t > 0) {
      var tickTime = Math.min(t, MAX_UPDATE_TIME);
      input.update(tickTime);
      camera.update(tickTime);
      t -= MAX_UPDATE_TIME;
    }
    canvas.render();
    requestAnimationFrame(gameLoop);
  }

  gameLoop();
}
