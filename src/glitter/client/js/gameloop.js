function GameLoop(canvas) {
  checkNotNull(canvas);
  this.canvas = canvas;
}

GameLoop.prototype.start = function() {
  var self = this;
  
  var lastTime = Date.now(), timeSinceLastFrame = 0;
  var lastFPSUpdate = 0;
  var fps = 0;
  function gameLoop() {
    var now = Date.now();
    if (now == lastTime) {
      requestAnimationFrame(gameLoop);
      return;
    }

    timeSinceLastFrame = now - lastTime;
    lastTime = now;

    if (now - lastFPSUpdate >= 1000) {
      self.canvas.setFPS(fps);
      lastFPSUpdate = now;
    }

    var currentFPS = 1000 / timeSinceLastFrame;
    fps = Math.round(currentFPS * .1 + fps * .9);

    self.canvas.render();
    requestAnimationFrame(gameLoop);
  }

  gameLoop();
}
