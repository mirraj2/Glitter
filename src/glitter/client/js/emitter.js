function Emitter(config, parent) {
  this.parent = checkNotNull(parent);

  var container = this.container = new PIXI.Container();
  container.displayGroup = new PIXI.DisplayGroup(7);
  container.displayFlag = PIXI.DISPLAY_FLAG.MANUAL_CONTAINER;
  parent.addChild(container);

  var particles = config.particles;
  if (!particles) {
    particles = [ "particle.png" ];
  }

  particles = $.map(particles, function(p) {
    return PIXI.Texture.fromImage(p);
  });

  var emitter = this.emitter = new PIXI.particles.Emitter(container, particles, config);
  emitter.emit = true;

  this.callback = null;
}

Emitter.prototype.update = function(millis) {
  if (this.callback) {
    this.callback(millis);
  }

  var emitter = this.emitter;
  emitter.update(millis / 1000);

  if (!emitter.emit && emitter.particleCount == 0) {
    this.parent.removeChild(this.container);
    this.container.destroy({
      child : true
    });
    emitter.destroy();
    return false;
  }

  return true;
}

Emitter.prototype.translate = function(dx, dy) {
  var p = this.emitter.spawnPos;
  if (p) {
    this.emitter.updateSpawnPos(p.x + dx, p.y + dy);
  }
}

Emitter.prototype.position = function(x, y) {
  this.emitter.updateSpawnPos(x, y);
}

Emitter.prototype.finishUp = function() {
  this.emitter.emit = false;
}