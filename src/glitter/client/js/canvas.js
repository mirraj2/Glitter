function Canvas() {
  var renderer = PIXI.autoDetectRenderer($("body").width(), $("body").height(), {
    antialias : true
  });
  renderer.autoResize = true;

  $("body").append(renderer.view);

  var stage = new PIXI.Container();

  var width = $("body").width(), height = $("body").height();

  var fpsText = new PIXI.Text("", {
    fill : "white",
    fontSize : 16
  });
  fpsText.y = 30;
  stage.addChild(fpsText);

  function onResize() {
    width = $("body").width();
    height = $("body").height();
    renderer.resize(width, height);
    fpsText.x = width - 80;
  }
  $(window).resize(onResize);
  onResize();

  this.renderer = renderer;
  this.fpsText = fpsText;
  this.stage = stage;
}

Canvas.prototype.render = function() {
  this.renderer.render(this.stage);
}

Canvas.prototype.setFPS = function(fps) {
  this.fpsText.text = fps + " fps";
}