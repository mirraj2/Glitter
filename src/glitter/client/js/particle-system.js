function ParticleSystem() {
  glitter.register(this);
  this.emitters = [];
  this.configs = this.getConfigs();
}

ParticleSystem.prototype.update = function(millis) {
  for (var i = this.emitters.length - 1; i >= 0; i--) {
    var emitter = this.emitters[i];
    if (!emitter.emit && emitter.particleCount == 0) {
      this.emitters.splice(i, 1);

      emitter.parent.removeChild(emitter.container);
      emitter.container.destroy({
        child : true
      });
      emitter.destroy();

      continue;
    }
    if (emitter.spawnPos && emitter.vx) {
      emitter.updateSpawnPos(emitter.spawnPos.x + emitter.vx * millis, emitter.spawnPos.y + emitter.vy * millis);
    } else if (emitter.entityLink) {
      emitter.updateSpawnPos(emitter.entityLink.centerX(), emitter.entityLink.centerY());
    }
    emitter.update(millis / 1000);
  }
}

ParticleSystem.prototype.createEmitter = function(parent, configName) {
  var config = this.configs[configName];

  var container = new PIXI.Container();
  container.displayGroup = new PIXI.DisplayGroup(-1);
  container.displayFlag = PIXI.DISPLAY_FLAG.MANUAL_CONTAINER;
  parent.addChild(container);

  var emitter = new PIXI.particles.Emitter(container, [ PIXI.Texture.fromImage("particle.png") ], config);
  emitter.emit = true;
  emitter.container = container;
  emitter.parent = parent;

  this.emitters.push(emitter);

  return emitter;
}

ParticleSystem.prototype.createProjectile = function(parent, configName, spell, locs) {
  var config = this.configs[configName];

  var container = new PIXI.Container();
  // container.displayGroup = new PIXI.DisplayGroup(1);
  container.displayFlag = PIXI.DISPLAY_FLAG.MANUAL_CONTAINER;
  parent.addChild(container);

  var speed = spell.speed * Tile.SIZE;

  config.emitterLifetime = spell.range / spell.speed;
  config.pos.x = locs.fromX;
  config.pos.y = locs.fromY;

  var emitter = new PIXI.particles.Emitter(container, [ PIXI.Texture.fromImage("particle.png") ], config);
  emitter.vx = locs.dx * speed / 1000;
  emitter.vy = locs.dy * speed / 1000;
  emitter.emit = true;
  emitter.container = container;
  emitter.parent = parent;

  this.emitters.push(emitter);

  return {
    emitter : emitter,
    update : function(millis) {
      emitter.update(millis / 1000);
    },
    destroy : function() {
      emitter.emit = false;
    }
  };
}

ParticleSystem.prototype.getConfigs = function() {
  return {
    fireball : {
      "alpha" : {
        "start" : 1,
        "end" : 0
      },
      "scale" : {
        "start" : 1,
        "end" : 1,
        "minimumScaleMultiplier" : 0.01
      },
      "color" : {
        "start" : "#ff3838",
        "end" : "#ffd900"
      },
      "speed" : {
        "start" : 2,
        "end" : 10,
        "minimumSpeedMultiplier" : 10
      },
      "acceleration" : {
        "x" : 0,
        "y" : 0
      },
      "maxSpeed" : 100,
      "startRotation" : {
        "min" : 0,
        "max" : 360
      },
      "noRotation" : false,
      "rotationSpeed" : {
        "min" : -1,
        "max" : -1
      },
      "lifetime" : {
        "min" : 0.1,
        "max" : .4
      },
      "blendMode" : "add",
      "frequency" : 0.001,
      "emitterLifetime" : -1,
      "maxParticles" : 1000,
      "pos" : {
        "x" : 0,
        "y" : 0
      },
      "addAtBack" : false,
      "spawnType" : "point"
    },
    frostbolt : {
      "alpha" : {
        "start" : 1,
        "end" : 0
      },
      "scale" : {
        "start" : 1,
        "end" : 1,
        "minimumScaleMultiplier" : 0.01
      },
      "color" : {
        "start" : "#38deff",
        "end" : "#00a2ff"
      },
      "speed" : {
        "start" : 1,
        "end" : 1,
        "minimumSpeedMultiplier" : 10
      },
      "acceleration" : {
        "x" : 0,
        "y" : 0
      },
      "maxSpeed" : 100,
      "startRotation" : {
        "min" : 0,
        "max" : 360
      },
      "noRotation" : false,
      "rotationSpeed" : {
        "min" : -1,
        "max" : -1
      },
      "lifetime" : {
        "min" : .25,
        "max" : .5
      },
      "blendMode" : "add",
      "frequency" : 0.004,
      "emitterLifetime" : -1,
      "maxParticles" : 1000,
      "pos" : {
        "x" : 0,
        "y" : 0
      },
      "addAtBack" : false,
      "spawnType" : "point"
    },
    heal : {
      "alpha" : {
        "start" : 1,
        "end" : 0
      },
      "scale" : {
        "start" : 0.07,
        "end" : 0.01,
        "minimumScaleMultiplier" : 10
      },
      "color" : {
        "start" : "#7aff14",
        "end" : "#05f549"
      },
      "speed" : {
        "start" : 10,
        "end" : 50,
        "minimumSpeedMultiplier" : 10
      },
      "acceleration" : {
        "x" : 0,
        "y" : -500
      },
      "maxSpeed" : 0,
      "startRotation" : {
        "min" : 0,
        "max" : 360
      },
      "noRotation" : false,
      "rotationSpeed" : {
        "min" : 0,
        "max" : 0
      },
      "lifetime" : {
        "min" : 1,
        "max" : 1
      },
      "blendMode" : "screen",
      "frequency" : 0.001,
      "emitterLifetime" : 0.25,
      "maxParticles" : 100,
      "pos" : {
        "x" : 0,
        "y" : 0
      },
      "addAtBack" : false,
      "spawnType" : "rect",
      "spawnRect" : {
        "x" : -24,
        "y" : -32,
        "w" : 48,
        "h" : 64
      }
    },
    starfield : {
      "alpha" : {
        "start" : 0,
        "end" : 1
      },
      "scale" : {
        "start" : 0.04,
        "end" : 0.04,
        "minimumScaleMultiplier" : 5
      },
      "color" : {
        "start" : "#adf4fa",
        "end" : "#adf4fa"
      },
      "speed" : {
        "start" : .25,
        "end" : .25,
        "minimumSpeedMultiplier" : 30
      },
      "acceleration" : {
        "x" : 0,
        "y" : 0
      },
      "maxSpeed" : 100,
      "startRotation" : {
        "min" : 0,
        "max" : 360
      },
      "noRotation" : false,
      "rotationSpeed" : {
        "min" : -1,
        "max" : -1
      },
      "lifetime" : {
        "min" : 5,
        "max" : 10
      },
      "blendMode" : "add",
      "frequency" : 0.00001,
      "emitterLifetime" : -1,
      "maxParticles" : 500,
      "pos" : {
        "x" : 0,
        "y" : 0
      },
      "addAtBack" : false,
      "spawnType" : "rect",
      "spawnRect" : {
        "x" : 0,
        "y" : 0,
        "w" : 100,
        "h" : 100
      }
    }
  };
}
