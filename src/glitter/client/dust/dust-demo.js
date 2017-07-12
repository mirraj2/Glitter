var stage;
var width, height;

$(function() {
  var renderer = PIXI.autoDetectRenderer($("body").width(), $("body").height(), {
    antialias : true
  });
  renderer.autoResize = true;

  $("body").append(renderer.view);

  width = $("body").width(), height = $("body").height();
  function onResize() {
    width = $("body").width();
    height = $("body").height();
    renderer.resize(width, height);
  }
  $(window).resize(onResize);
  onResize();

  stage = new PIXI.Container();

  var emitters = new Array();
  for (var i = 0; i < 1; i++) {
    var emitter = new Emitter(stage).numParticles(500).scale(.5);
    reset(emitter);
    emitters.push(emitter);
  }

  var last = performance.now();
  function render() {
    requestAnimationFrame(render);
    var now = performance.now();
    var t = now - last;
    last = now;
    Dust.update(t);
    var now2 = performance.now();
    renderer.render(stage);
    var now3 = performance.now();

     console.log("update took " + (now2 - now));
     console.log("render took " + (now3 - now2));

    for (var i = 0; i < emitters.length; i++) {
      var emitter = emitters[i];
      if (emitter.x < -100 || emitter.y < -100 || emitter.x > width + 100 || emitter.y > height + 100) {
        reset(emitter);
      }
    }
  }
  requestAnimationFrame(render);
});

function reset(emitter) {
  emitter.position(Math.random() * width, Math.random() * height);
  emitter.velocity(Math.random() * 1000 - 500, Math.random() * 1000 - 500);
  emitter.setLife(10000);
}
