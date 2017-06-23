function Canvas() {
  var renderer = PIXI.autoDetectRenderer($("body").width(), $("body").height(), {
    antialias : true
  });
  renderer.autoResize = true;

  $("body").append(renderer.view);

  var stage = new PIXI.Container();

  var width = $("body").width(), height = $("body").height();

  function onResize() {
    width = $("body").width();
    height = $("body").height();
    renderer.resize(width, height);
  }
  $(window).resize(onResize);
  onResize();

  this.renderer = renderer;
  this.stage = stage;
}

Canvas.prototype.render = function() {
  this.renderer.render(this.stage);
}

Canvas.prototype.setFPS = function(fps) {
  $(".fps").text(fps + " fps");
}