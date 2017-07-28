function Forcefield() {
  this.t = 0;

  this.circle = new PIXI.Graphics();
  this.circle.displayGroup = new PIXI.DisplayGroup(8);
  world.container.addChild(this.circle);
  
  glitter.register(this);
}

Forcefield.prototype.update = function(millis) {
  if (!this.isActive()) {
    return;
  }

  this.t = Math.min(this.t + millis, this.transitionSeconds * 1000);

  var p = this.t / 1000 / this.transitionSeconds;
  this.x = this.fromX + p * (this.toX - this.fromX);
  this.y = this.fromY + p * (this.toY - this.fromY);
  this.radius = this.fromRadius + p * (this.toRadius - this.fromRadius);
  
  var circle = this.circle;
  circle.clear();
  circle.lineStyle(1, 0xFFFFFF, .8);
  circle.drawCircle(this.x,this.y,this.radius);
}

Forcefield.prototype.updateBounds = function(bounds) {
  $.extend(this, bounds);
  this.t = 0;
}

Forcefield.prototype.isActive = function() {
  return this.x != null;
}