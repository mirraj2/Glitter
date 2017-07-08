function GameLoop(canvas) {
  checkNotNull(canvas);
  this.canvas = canvas;

  this.start();
}

GameLoop.prototype.start = function() {
  var lastTime = performance.now(), t = 0;
  var lastFPSUpdate = 0;
  var fps = 0;
  var MAX_UPDATE_TIME = 100;

  function gameLoop() {
    var now = performance.now();
    if (now == lastTime || window.noLoop) {
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

    // each tick should be no more than MAX_UPDATE_TIME millis
    // unless the number of ticks would be greater than 10
    var maxTime = Math.max(MAX_UPDATE_TIME, t / 10);

    while (t > 0) {
      var tickTime = Math.min(t, maxTime);
      for (var i = glitter.callbacks.length - 1; i >= 0; i--) {
        var callback = glitter.callbacks[i];
        if (callback.update(tickTime)) {
          //we should unregister this callback
          glitter.callbacks.splice(i, 1);
        }
      }
      t -= maxTime;
    }

    canvas.render();

    // update the countdown timer
    if (window.gameStartTime) {
      var timeLeft = window.gameStartTime - Date.now();
      if (timeLeft < 0) {
        timeLeft = 0;
      }
      $(".countdown .num").text(Math.ceil(timeLeft / 1000.0));
    }

    requestAnimationFrame(gameLoop);
  }

  gameLoop();
}
