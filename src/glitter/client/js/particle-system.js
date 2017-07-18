function ParticleSystem() {
  this.configs = this.getConfigs();
  this.emitters = [];

  glitter.register(this);
}

ParticleSystem.prototype.update = function(millis) {
  for (var i = this.emitters.length - 1; i >= 0; i--) {
    var emitter = this.emitters[i];
    if (!emitter.update(millis)) {
      this.emitters.splice(i, 1);
    }
  }
}

ParticleSystem.prototype.createEmitter = function(parent, configName) {
  return new Emitter(this.configs[configName], parent);
}

ParticleSystem.prototype.createAndRegister = function(parent, configName) {
  var ret = this.createEmitter(parent, configName);
  this.emitters.push(ret);
  return ret;
}

ParticleSystem.prototype.createProjectile = function(parent, configName, spell, locs) {
  var config = this.configs[configName];
  config.emitterLifetime = spell.range / spell.speed;
  config.pos.x = locs.fromX;
  config.pos.y = locs.fromY;

  var emitter = new Emitter(config, parent);

  var projectile = new Projectile(emitter);
  projectile.vx = locs.dx * spell.speed * Tile.SIZE;
  projectile.vy = locs.dy * spell.speed * Tile.SIZE;
  return projectile;
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
      "maxParticles" : 300,
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
        "min" : .15,
        "max" : .25
      },
      "blendMode" : "add",
      "frequency" : 0.001,
      "emitterLifetime" : -1,
      "maxParticles" : 200,
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
    },
    toxicCloudProjectile : {
      "alpha" : {
        "start" : 1,
        "end" : 0
      },
      "scale" : {
        "start" : 0.4,
        "end" : 1.2,
        "minimumScaleMultiplier" : 2
      },
      "color" : {
        "start" : "#4ad143",
        "end" : "#000000"
      },
      "speed" : {
        "start" : 10,
        "end" : 10,
        "minimumSpeedMultiplier" : 1
      },
      "acceleration" : {
        "x" : 0,
        "y" : 0
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
        "min" : 0.5,
        "max" : 1
      },
      "blendMode" : "add",
      "frequency" : 0.007,
      "emitterLifetime" : -1,
      "maxParticles" : 1000,
      "pos" : {
        "x" : 0,
        "y" : 0
      },
      "addAtBack" : true,
      "spawnType" : "point"
    },
    toxicCloud : {
      "particles" : [ "smokeparticle.png" ],
      "alpha" : {
        "start" : 0.52,
        "end" : 0
      },
      "scale" : {
        "start" : 0.5,
        "end" : 1,
        "minimumScaleMultiplier" : 0.1
      },
      "color" : {
        "start" : "#6bff61",
        "end" : "#d8ff4a"
      },
      "speed" : {
        "start" : 1,
        "end" : 1,
        "minimumSpeedMultiplier" : 1
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
      "noRotation" : true,
      "rotationSpeed" : {
        "min" : 0,
        "max" : 0
      },
      "lifetime" : {
        "min" : 2,
        "max" : 2
      },
      "blendMode" : "normal",
      "frequency" : 0.01,
      "emitterLifetime" : -1,
      "maxParticles" : 1000,
      "pos" : {
        "x" : 0,
        "y" : 0
      },
      "addAtBack" : true,
      "spawnType" : "circle",
      "spawnCircle" : {
        "x" : 0,
        "y" : 0,
        "r" : 40
      }
    }
  };
}
