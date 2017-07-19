function CastIndicator() {
  this.container = new PIXI.Container();
  this.container.displayGroup = new PIXI.DisplayGroup(1);

  var circle = this.circle = new PIXI.Graphics();

  this.container.addChild(circle);
  world.container.addChild(this.container);

  glitter.register(this);
}

CastIndicator.prototype.update = function(millis) {
  if (window.me) {
    var circle = this.circle;

    var spell = window.quickbar.getSelectedItem();
    var radius = 0;
    if (spell && spell.range) {
      radius = spell.range * Tile.SIZE;
    }

    circle.clear();
    circle.lineStyle(2, 0xFFFFFF, .2);
    circle.beginFill(0x444444, 0.0);
    circle.drawCircle(0, 0, radius);
    circle.endFill();

    circle.x = me.centerX();
    circle.y = me.centerY();
  }
}