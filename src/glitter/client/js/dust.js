var Dust = (function() {
  var emitters = [];
  return {
    emitters : emitters,
    update : function(millis) {
      for (var i = 0; i < this.emitters.length; i++) {
        emitters[i].update(millis);
      }
    }
  };
})();

function Emitter(parent) {
  this.x = 0;
  this.y = 0;
  this.vx = 0;
  this.vy = 0;
  this.particleCount = 100;
  this.particleLife = 1000;
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
  this.x += this.vx * millis / 1000;
  this.y += this.vy * millis / 1000;
  this.tickParticles(millis);
}

Emitter.prototype.tickParticles = function(millis) {
  for (var i = 0; i < this.particles.length; i++) {
    var particle = this.particles[i];
    particle.life -= millis;
    if (particle.life <= 0) {
      this.resetParticle(particle);
    } else {
      particle.alpha = Math.max(particle.life / this.particleLife, 0);
      particle.x += particle.vx * millis;
      particle.y += particle.vy * millis;
    }
  }
}

Emitter.prototype.resetParticle = function(particle) {
  particle.x = this.x;
  particle.y = this.y;
  particle.vx = (Math.random() - .5) * 100 / 1000;
  particle.vy = (Math.random() - .5) * 100 / 1000;
  particle.life = .9 * this.particleLife + Math.random() * .2 * this.particleLife;
}

Emitter.prototype.init = function() {
  this.container = new PIXI.particles.ParticleContainer(this.particleCount, {
    alpha : true
  });
  this.parent.addChild(this.container);

  this.particles = new Array(this.particleCount);
  for (var i = 0; i < this.particleCount; i++) {
    var particle = PIXI.Sprite.fromImage(this.imageName);
    particle.scale.x = this.scaleAmount;
    particle.scale.y = this.scaleAmount;
    // particle.tint = 0xFF0000;
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