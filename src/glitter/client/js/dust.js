var Dust = (function() {
  var emitters = [];
  return {
    emitters : emitters,
    update : function(millis) {
      for (var i = emitters.length - 1; i >= 0; i--) {
        var emitter = emitters[i];
        if (!emitter.update(millis)) {
          emitter.parent.removeChild(this.container);
          emitter.particles = null;
          emitters.splice(i, 1);
        }
      }
    }
  };
})();

function Emitter(parent) {
  this.x = 0;
  this.y = 0;
  this.vx = 0;
  this.vy = 0;
  this.life = 5000;
  this.particleCount = 100;
  this.particleLife = 500;
  this.scaleAmount = 1;
  this.imageName = "particle.png";
  this.parent = parent;
  Dust.emitters.push(this);
}

Emitter.prototype.position = function(x, y) {
  this.x = x;
  this.y = y;
  return this;
}

Emitter.prototype.velocity = function(vx, vy) {
  this.vx = vx;
  this.vy = vy;
  return this;
}

Emitter.prototype.setLife = function(life) {
  this.life = life;
  return this;
}

Emitter.prototype.scale = function(scale) {
  this.scaleAmount = scale;
  return this;
}

Emitter.prototype.numParticles = function(numParticles) {
  this.particleCount = numParticles;
  return this;
}

Emitter.prototype.update = function(millis) {
  if (!this.container) {
    this.init();
  }

  this.life -= millis;
  this.x += this.vx * millis / 1000;
  this.y += this.vy * millis / 1000;

  return this.tickParticles(millis);
}

Emitter.prototype.tickParticles = function(millis) {
  var ret = false;
  for (var i = 0; i < this.particles.length; i++) {
    var particle = this.particles[i];
    particle.life -= millis;
    if (particle.life <= 0) {
      if (this.life > 0) {
        this.resetParticle(particle);
        ret = true;
      } else {
        particle.alpha = 0;
      }
    } else {
      ret = true;
      particle.alpha = Math.max(particle.life / this.particleLife, 0);
      particle.x += particle.vx * millis;
      particle.y += particle.vy * millis;
    }
  }
  return ret;
}

Emitter.prototype.resetParticle = function(particle) {
  particle.x = this.x + this.offsetX;
  particle.y = this.y + this.offsetY;
  particle.vx = (Math.random() - .5) * 100 / 1000;
  particle.vy = (Math.random() - .5) * 100 / 1000;
  particle.life = .9 * this.particleLife + Math.random() * .2 * this.particleLife;
}

Emitter.prototype.destroy = function() {
  this.life = 0;
}

Emitter.prototype.init = function() {
  this.container = new PIXI.Container(this.particleCount * 2, {
    alpha : true
  });
  this.parent.addChild(this.container);

  var texture = PIXI.Texture.fromImage(this.imageName);
  this.offsetX = -this.scaleAmount * texture.width / 2;
  this.offsetY = -this.scaleAmount * texture.height / 2;
  this.particles = new Array(this.particleCount);
  for (var i = 0; i < this.particleCount; i++) {
    var particle = new PIXI.Sprite(texture);
    // particle.blendMode = PIXI.BLEND_MODES.SCREEN;
    particle.scale.x = this.scaleAmount;
    particle.scale.y = this.scaleAmount;
    particle.tint = 0xf23026;
    this.resetParticle(particle);
    particle.life = Math.random() * this.particleLife;
    this.particles[i] = particle;
    this.container.addChild(particle);
  }
  this.started = true;

  for (var i = 0; i < 10; i++) {
    this.tickParticles(100);
  }
}