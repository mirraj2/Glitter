function Spells(parent) {
  this.container = new PIXI.Container();
  parent.addChild(this.container);
}

Spells.prototype.cast = function(spell, toX, toY) {
  if (me.mana < spell.manaCost) {
    return;
  }
  me.mana -= spell.manaCost;

  toX -= world.container.x;
  toY -= world.container.y;

  var fromX = me.centerX();
  var fromY = me.centerY();
  var dx = toX - fromX;
  var dy = toY - fromY;
  var normalizationVal = 1 / Math.sqrt((dx * dx) + (dy * dy));
  dx *= normalizationVal;
  dy *= normalizationVal;

  console.log("Casting " + spell.name);
  console.log(spell);

  var locs = {
    fromX : fromX,
    fromY : fromY,
    toX : toX,
    toY : toY,
    dx : dx,
    dy : dy
  }

  var projectileIds = this[spell.name.toLowerCase()](me, spell, locs, null);

  network.send({
    command : "cast",
    spellId : spell.id,
    locs : locs,
    projectileIds : projectileIds
  });
}

Spells.prototype.onCast = function(json) {
  var spell = json.spell;
  var player = world.idPlayers[json.casterId];
  this[spell.name.toLowerCase()](player, spell, json.locs, json.projectileIds);
}

Spells.prototype.fireball = function(player, spell, locs, projectileIds) {
  var speed = spell.speed * Tile.SIZE;

  var projectile = new Emitter(this.container).numParticles(256).scale(.4).position(locs.fromX, locs.fromY).velocity(
      locs.dx * speed, locs.dy * speed).setLife(spell.range / spell.speed * 1000);

  if (projectileIds) {
    projectile.id = projectileIds[0];
  } else {
    projectile.id = spell.id + "." + spell.idCounter++;
  }

  return [ projectile.id ];
}