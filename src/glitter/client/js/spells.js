function Spells(parent) {
  this.container = new PIXI.Container();
  parent.addChild(this.container);
}

Spells.prototype.cast = function(spell, x, y) {
  // spell.manaCost = 0;
  if (me.mana >= spell.manaCost) {
    me.mana -= spell.manaCost;
    console.log("Casting " + spell.name);

    x -= world.container.x;
    y -= world.container.y;
    this[spell.name.toLowerCase()](spell, x, y);
  }
}

Spells.prototype.fireball = function(spell, x, y) {
  var dx = x - me.centerX();
  var dy = y - me.centerY();
  var normalizationVal = 1 / Math.sqrt((dx * dx) + (dy * dy));
  dx *= normalizationVal;
  dy *= normalizationVal;
  new Emitter(this.container).numParticles(256).scale(.4).position(me.centerX(), me.centerY()).velocity(dx * 600,
      dy * 600);
}