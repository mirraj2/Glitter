function Spells(parent, particleSystem) {
  this.container = new PIXI.Container();
  this.container.displayGroup = new PIXI.DisplayGroup(1);
  this.idCounter = -1;
  this.particleSystem = particleSystem;
  parent.addChild(this.container);
}

Spells.prototype.castEffects = function(msg) {
  msg.idMapping.forEach(function(mapping) {
    var projectile = world.idEntities[mapping[0]];
    delete world.idEntities[projectile.id];
    projectile.id = mapping[1];
    world.idEntities[projectile.id] = projectile;
  });
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

  console.log("Casting " + spell.name);
  var projectiles = this[spell.name.toLowerCase().replace(" ", "_")](me, spell, locs);

  if (projectiles === null) {
    return;
  }

  me.mana -= spell.manaCost;

  for (var i = 0; i < projectiles.length; i++) {
    // temporary ids are negative. the server will send us a 'castEffects'
    // message which we'll use to map to the real ids
    var projectile = projectiles[i];
    projectile.id = this.idCounter--;
    world.idEntities[projectile.id] = projectiles[i];
  }

  network.send({
    command : "cast",
    spellId : spell.id,
    locs : locs,
    tempIds : $.map(projectiles, function(p) {
      return p.id;
    })
  });
}

/**
 * Called when another player casts a spell.
 */
Spells.prototype.onCast = function(json) {
  var spell = json.spell;
  var player = world.idPlayers[json.casterId];
  var projectiles = this[spell.name.toLowerCase().replace(" ", "_")](player, spell, json.locs);

  for (var i = 0; i < projectiles.length; i++) {
    var id = json.entityIds[i];
    var e = projectiles[i];
    e.id = id;
    world.idEntities[id] = e;
  }

  console.log("Applying " + json.latency + "ms of latency compensation.");
  while (json.latency > 0) {
    var millis = Math.min(json.latency, 30);
    json.latency -= millis;
    for (var i = 0; i < projectiles.length; i++) {
      projectiles[i].update(millis);
    }
  }
}

Spells.prototype.onHit = function(msg) {
  var player = world.idPlayers[msg.targetId];
  if (msg.currentHealth != null) {
    player.health = msg.currentHealth;
  }

  if (player.health <= 0) {
    player.alive = false;
    world.removePlayer(player.id);

    var numPlayersLeft = Object.keys(world.idPlayers).length;
    if (player == me) {
      $(".summary h1").text("Better luck next time!");
      $(".summary .rank").text("#" + (numPlayersLeft + 1));
      $(".summary").fadeIn();
    } else if (numPlayersLeft == 1 && me.alive) {
      $(".summary h1").text("Perfect Victory");
      $(".summary .rank").text("#1");
      $(".summary").fadeIn();
    }
  }

  if (msg.spell == "heal") {
    // add the heal animation
    var emitter = this.particleSystem.createAndRegister(this.container, "heal");
    emitter.callback = function() {
      emitter.container.x = player.centerX();
      emitter.container.y = player.centerY();
    }
  } else if (msg.spell == "toxic_cloud") {
    // add the toxin effect
  }
}

Spells.prototype.fireball = function(player, spell, locs) {
  return [ this.particleSystem.createProjectile(this.container, "fireball", spell, locs) ];
}

Spells.prototype.frostbolt = function(player, spell, locs) {
  return [ this.particleSystem.createProjectile(this.container, "frostbolt", spell, locs) ];
}

Spells.prototype.heal = function(player, spell, locs) {
  var targets = world.getPlayersAt(locs.toX, locs.toY).filter(function(player) {
    return player == me; // TODO, allow heal on friendly players as well
  });

  if (targets.length == 0) {
    console.log("Not a valid heal target.");
    return null;
  }

  locs.targetId = targets[0].id;

  return [];
}

Spells.prototype.toxic_cloud = function(player, spell, locs) {
  if (locs.targetId == null) {
    var targets = world.getPlayersAt(locs.toX, locs.toY).filter(function(player) {
      return player != me; // TODO, don't allow you to target teammates
    });

    if (targets.length == 0) {
      console.log("Not a valid target.");
      return null;
    }
    
    locs.targetId = targets[0].id;
    
    if(!this.isInRange(player, targets[0], spell.range)){
      showError("Out of range!");
      return null;
    }
  }

  var target = world.idPlayers[locs.targetId];

  var emitter = this.particleSystem.createEmitter(this.container, "toxicCloudProjectile");
  emitter.position(player.centerX(), player.centerY());

  var projectile = new Projectile(emitter);
  projectile.homeInOn(target, spell.speed);

  return [ projectile ];
}

Spells.prototype.isInRange = function(a, b, range) {
  range *= Tile.SIZE;
  var dx = a.x - b.x;
  var dy = a.y - b.y;
  return dx * dx + dy * dy < range * range;
}