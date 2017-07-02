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
      input.update(tickTime);
      camera.update(tickTime);
      quickbar.update(tickTime);
      Dust.update(tickTime);
      minimap.update();
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
