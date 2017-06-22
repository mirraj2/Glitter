function GameLoop(canvas) {
  this.canvas = canvas;
}

GameLoop.prototype.start = function() {
  var lastTime = Date.now(), timeSinceLastFrame = 0;
  var lastFPSUpdate = 0;
  var fps = 0;
  function gameLoop() {
    requestAnimationFrame(gameLoop);
    var now = Date.now();
    if (now == lastTime) {
      return;
    }

    timeSinceLastFrame = now - lastTime;
    lastTime = now;

    if (now - lastFPSUpdate >= 1000) {
      this.canvas.setFPS(fps);
      lastFPSUpdate = now;
    }

    var currentFPS = 1000 / timeSinceLastFrame;
    fps = Math.round(currentFPS * .1 + fps * .9);

    this.canvas.render();
  }

  gameLoop();
}
