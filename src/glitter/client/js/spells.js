function Spells(parent, particleSystem) {
  this.container = new PIXI.Container();
  this.container.displayGroup = new PIXI.DisplayGroup(1);
  this.idCounter = 0;
  this.idProjectiles = {};
  this.particleSystem = particleSystem;
  parent.addChild(this.container);
}

Spells.prototype.castEffects = function(msg) {
  var ids = msg.entityIds;
  var projectiles = this.idProjectiles[msg.castId];
  delete this.idProjectiles[msg.castId];

  this.assignIds(projectiles, ids);
}

/**
 * Called when we cast a spell.
 */
Spells.prototype.cast = function(spell, toX, toY) {
  if (me.mana < spell.manaCost) {
    return;
  }

  toX -= world.container.x;
  toY -= world.container.y;

  var fromX = me.centerX();
  var fromY = me.centerY();
  var dx = toX - fromX;
  var dy = toY - fromY;
  var normalizationVal = 1 / Math.sqrt((dx * dx) + (dy * dy));
  dx *= normalizationVal;
  dy *= normalizationVal;

  var locs = {
    fromX : fromX,
    fromY : fromY,
    toX : toX,
    toY : toY,
    dx : dx,
    dy : dy
  }

  var castId = this.idCounter++;

  console.log("Casting " + spell.name);
  var projectiles = this[spell.name.toLowerCase()](me, spell, locs);

  if (projectiles === null) {
    return;
  }

  me.mana -= spell.manaCost;

  this.idProjectiles[castId] = projectiles;

  network.send({
    command : "cast",
    castId : castId,
    spellId : spell.id,
    locs : locs
  });
}

/**
 * Called when another player casts a spell.
 */
Spells.prototype.onCast = function(json) {
  var spell = json.spell;
  var player = world.idPlayers[json.casterId];
  var projectiles = this[spell.name.toLowerCase()](player, spell, json.locs);

  console.log("Applying " + json.latency + "ms of latency compensation.");
  while (json.latency > 0) {
    var millis = Math.min(json.latency, 30);
    json.latency -= millis;
    for (var i = 0; i < projectiles.length; i++) {
      projectiles[i].update(millis);
    }
  }

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
  return [ this.particleSystem.createProjectile(this.container, "fireball", spell, locs) ];
}

Spells.prototype.frostbolt = function(player, spell, locs) {
  return [ this.particleSystem.createProjectile(this.container, "frostbolt", spell, locs) ];
}

Spells.prototype.heal = function(player, spell, locs) {
  var targets = world.getPlayersAt(locs.toX, locs.toY);
  //TODO filter friendly players
  
  if (targets.length == 0) {
    console.log("Not a valid heal target.");
    return null;
  }
  return [];
}