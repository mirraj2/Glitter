function Background(particleSystem) {
  var emitter = particleSystem.createAndRegister(canvas.stage, "starfield");
  emitter.container.displayGroup = new PIXI.DisplayGroup(-1);
  this.emitter = emitter.emitter;
  
  this.emitter.customEase = function(t) {
    if (t < .1) {
      return t / .1;
    }
    if (t > .9) {
      return (1 - t) / .1;
    }
    return 1;
  }

  this.lastUpdate = 0;

  var self = this;
  $(window).resize(function() {
    self.onWindowResize();
  });
  this.onWindowResize();
}

Background.prototype.onWindowResize = function() {
  var emitter = this.emitter;

  var win = $(window);
  var w = win.width();
  var h = win.height();
  emitter.spawnRect.width = win.width();
  emitter.spawnRect.height = win.height();
  emitter.maxParticles = w * h / 5000;
}
