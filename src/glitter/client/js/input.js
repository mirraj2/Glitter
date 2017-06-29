/**
 * Listens for user input.
 */

function Input() {
  this.rect = new PIXI.Rectangle();

  // whether the user has pressed any keys since the last update
  this.dirty = false;
  this.interactionEntity = null;
  this.allowMovement = true;
}

Input.prototype.haltMovement = function() {
  this.allowMovement = false;
  me.keys = {};
  this.sendMyState();
}

Input.prototype.interact = function() {
  if (this.interactionEntity) {
    network.send({
      command : "interact",
      entityId : this.interactionEntity.id
    });
  }
}

Input.prototype.sendMyState = function() {
  var keyList = [];
  Object.keys(me.keys).forEach(function(key) {
    keyList.push(key);
  });
  network.send({
    command : "myState",
    keys : keyList,
    x : me.x,
    y : me.y
  });
  this.dirty = false;
}

Input.prototype.update = function(t) {
  var self = this;

  if (window.me == null) {
    return;
  }

  if (this.dirty && this.allowMovement) {
    self.sendMyState();
  }

  $.each(world.idPlayers, function(id, player) {
    if (self.allowMovement || player != window.me) {
      self.movePlayer(player, t);
    }
  });
}

Input.prototype.movePlayer = function(player, t) {
  var keys = player.keys;

  var speed = 6;
  if (player.flying) {
    speed *= 10;
  }
  var distance = speed * Tile.SIZE * t / 1000;

  var dx = 0, dy = 0;

  if (keys['w']) {
    dy -= distance;
  }
  if (keys['a']) {
    dx -= distance;
  }
  if (keys['s']) {
    dy += distance;
  }
  if (keys['d']) {
    dx += distance;
  }

  if (dx != 0 && dy != 0) {
    dx /= 1.4142135;// divide by sqrt(2)
    dy /= 1.4142135;// divide by sqrt(2)
  }

  var moved = false;
  if (dx != 0) {
    moved |= this.move(player, dx, 0);
  }
  if (dy != 0) {
    moved |= this.move(player, 0, dy);
  }

  if (moved && player == me) {
    this.findInteraction();
  }
}

Input.prototype.findInteraction = function() {
  // see if we are near a treasure chest

  var chest = null;
  var self = this;
  var rect = me.getHitbox(this.rect, 16);

  $.each(world.idEntities, function(key, value) {
    if (self.intersects(rect, value)) {
      chest = value;
      return false;
    }
  });

  if (chest != this.interactionEntity) {
    this.interactionEntity = chest;
    if (this.interactionEntity) {
      $(".spacebar").fadeIn(200);
    } else {
      $(".spacebar").fadeOut(200);
    }
  }
}

Input.prototype.move = function(player, dx, dy) {
  if (!player.flying) {
    var rect = player.getHitbox(this.rect);
    rect.x += dx;
    rect.y += dy;
    if (this.isCollision(rect)) {
      return false;
    }
  }

  player.setX(player.x + dx);
  player.setY(player.y + dy);

  return true;
}

Input.prototype.isCollision = function(rect) {
  var self = this;

  var ret = false;
  world.terrain.getTilesIntersecting(rect.x, rect.y, rect.width, rect.height, function(tile) {
    if (!world.terrain.isWalkable(tile.type)) {
      ret = true;
    }
  });

  if (ret) {
    return true;
  }

  $.each(world.idEntities, function(key, value) {
    if (self.intersects(rect, value)) {
      ret = true;
      return false;
    }
  });

  return ret;
}

Input.prototype.intersects = function(rect, entity) {
  if ((rect.x >= entity.x + entity.width) || (rect.x + rect.width <= entity.x) || (rect.y >= entity.y + entity.height)
      || (rect.y + rect.height <= entity.y)) {
    return false;
  }
  return true;
}

Input.prototype.listen = function() {
  var self = this;
  var consoleVisible = false;
  $(window).keydown(function(e) {
    e.key = e.key.toLowerCase();
    if (e.key == " ") {
      self.interact();
    } else if (e.key == "enter") {
      if (consoleVisible) {
        var text = $(".console input").val().trim();
        $(".console input").val("");
        if (text) {
          if (text == "/fly") {
            me.flying = !me.flying;
          } else {
            network.send({
              command : "consoleInput",
              text : text
            });
          }
        }
        $(".console").stop().hide();
        consoleVisible = false;
      } else {
        $(".console").stop().fadeIn();
        $(".console input").focus();
        consoleVisible = true;
      }
    } else if (e.key == "/") {
      if (!consoleVisible) {
        $(".console").stop().fadeIn();
        $(".console input").focus();
        consoleVisible = true;
      }
    } else if (e.which >= 48 && e.which < 58) {
      window.quickbar.select(e.which - 48);
    }
    if (consoleVisible) {
      return;
    }
    if (window.me && !me.keys[e.key]) {
      me.keys[e.key] = true;
      self.dirty = true;
    }
  });
  $(window).keyup(function(e) {
    e.key = e.key.toLowerCase();
    if (window.me) {
      delete me.keys[e.key];
      self.dirty = true;
    }
  });
  $(".console input").blur(function() {
    $(this).focus();
  });
}