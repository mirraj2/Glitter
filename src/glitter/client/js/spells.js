function Spells(parent) {
  this.container = new PIXI.Container();
  this.idCounter = 0;
  this.idProjectiles = {};
  parent.addChild(this.container);
}

Spells.prototype.castEffects = function(msg) {
  var ids = msg.entityIds;
  var projectiles = this.idProjectiles[msg.castId];
  delete this.idProjectiles[msg.castId];

  this.assignIds(projectiles, ids);
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

  var castId = this.idCounter++;
  var projectiles = this[spell.name.toLowerCase()](me, spell, locs);
  this.idProjectiles[castId] = projectiles;

  network.send({
    command : "cast",
    castId : castId,
    spellId : spell.id,
    locs : locs
  });
}

Spells.prototype.onCast = function(json) {
  var spell = json.spell;
  var player = world.idPlayers[json.casterId];
  var projectiles = this[spell.name.toLowerCase()](player, spell, json.locs);

  this.assignIds(projectiles, json.entityIds);
}

Spells.prototype.assignIds = function(entities, ids) {
  for (var i = 0; i < entities.length; i++) {
    var id = ids[i];
    var e = entities[i];
    e.id = id;
    world.idEntities[id] = e;
  }
}

Spells.prototype.fireball = function(player, spell, locs) {
  var speed = spell.speed * Tile.SIZE;

  var projectile = new Emitter(this.container).numParticles(256).scale(.4).position(locs.fromX, locs.fromY).velocity(
      locs.dx * speed, locs.dy * speed).setLife(spell.range / spell.speed * 1000);

  return [ projectile ];
}