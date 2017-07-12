var app;
var width, height;
var emitter;

$(document).mousemove(function(e) {
  if (emitter) {
    emitter.updateSpawnPos(e.pageX, e.pageY);
  }
});

$(function() {
  app = new PIXI.Application({});

  $("body").append(app.view);
  app.renderer.autoResize = true;

  width = $("body").width(), height = $("body").height();
  function onResize() {
    width = $("body").width();
    height = $("body").height();
    app.renderer.resize(width, height);
  }
  $(window).resize(onResize);
  onResize();

  emitter = new PIXI.particles.Emitter(

  // The PIXI.Container to put the emitter in
  // if using blend modes, it's important to put this
  // on top of a bitmap, and not use the root stage Container
  app.stage,

  [ PIXI.Texture.fromImage("particle.png") ],

  {
    "alpha": {
      "start": 1,
      "end": 0
    },
    "scale": {
      "start": 0.4,
      "end": 0.4,
      "minimumScaleMultiplier": 10
    },
    "color": {
      "start": "#3be5ff",
      "end": "#3be5ff"
    },
    "speed": {
      "start": 10,
      "end": 10,
      "minimumSpeedMultiplier": 20
    },
    "acceleration": {
      "x": 0,
      "y": 0
    },
    "maxSpeed": 0,
    "startRotation": {
      "min": 0,
      "max": 360
    },
    "noRotation": false,
    "rotationSpeed": {
      "min": -1,
      "max": -1
    },
    "lifetime": {
      "min": 1,
      "max": 1
    },
    "blendMode": "screen",
    "frequency": 0.001,
    "emitterLifetime": -1,
    "maxParticles": 10000,
    "pos": {
      "x": 0,
      "y": 0
    },
    "addAtBack": false,
    "spawnType": "point"
  });
  emitter.emit = true;

  var last = performance.now();
  function render() {
    requestAnimationFrame(render);
    var now = performance.now();
    var t = now - last;
    last = now;
    emitter.update(t / 1000);
    app.renderer.render(app.stage);
  }
  requestAnimationFrame(render);
});
