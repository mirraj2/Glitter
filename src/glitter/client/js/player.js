function Player(data) {
  this.id = data.id;
  this.x = data.x;
  this.y = data.y;

  this.acceptStats(data.stats);

  this.width = 48;
  this.height = 64;
  this.sprite = null;
  this.hitbox = new PIXI.Rectangle(12, 48, 24, 16);
  this.alive = true;
  this.stunned = false;
  this.flying = false;

  // the keys this player has pressed down
  this.keys = {};

  this.statusEffects = {};
}

Player.prototype.addStatusEffect = function(msg) {
  var self = this;
  var name = msg.name;
  if (name == "Toxic Cloud") {
    var emitter = window.particleSystem.createAndRegister(window.spells.container, "toxicCloud");
    emitter.callback = function() {
      emitter.position(self.centerX(), self.centerY());
    }
    emitter.onEnd = function() {
      this.finishUp();
    };
    this.statusEffects[name] = emitter;
  } else if (name == "Stunned") {
    this.stunned = true;
  }
}

Player.prototype.removeStatusEffect = function(msg) {
  var name = msg.name;
  if (name == "Stunned") {
    this.stunned = false;
  } else {
    var effect = this.statusEffects[name];
    if (effect != null) {
      delete this.statusEffects[name];
      effect.onEnd();
    }
  }
}

Player.prototype.acceptStats = function(stats) {
  this.health = stats.health;
  this.mana = stats.mana;
  this.maxHealth = stats.maxHealth;
  this.maxMana = stats.maxMana;
  this.healthRegenPerSecond = stats.healthRegen;
  this.manaRegenPerSecond = stats.manaRegen;
  this.speed = stats.speed;
  this.luck = stats.luck;
}

Player.prototype.centerX = function() {
  return this.x + this.width / 2;
}

Player.prototype.centerY = function() {
  return this.y + this.height / 2;
}

Player.prototype.setX = function(x) {
  this.x = x;
  this.sprite.x = Math.round(x);
}

Player.prototype.setY = function(y) {
  this.y = y;
  this.sprite.y = Math.round(y);
}

Player.prototype.setKeys = function(keys) {
  this.keys = {};
  for (var i = 0; i < keys.length; i++) {
    this.keys[keys[i]] = true;
  }
}

Player.prototype.getHitbox = function(rect, buffer) {
  if (!buffer) {
    buffer = 0;
  }
  rect.x = this.x + this.hitbox.x - buffer;
  rect.y = this.y + this.hitbox.y - buffer;
  rect.width = this.hitbox.width + buffer * 2;
  rect.height = this.hitbox.height + buffer * 2;
  return rect;
}

Player.prototype.spriteIntersects = function(x, y) {
  return x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
}
