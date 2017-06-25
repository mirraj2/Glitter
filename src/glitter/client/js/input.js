/**
 * Listens for user input.
 */

function Input() {
  this.rect = new PIXI.Rectangle();

  // whether the user has pressed any keys since the last update
  this.dirty = false;
}

Input.prototype.update = function(t) {
  var self = this;

  if (window.me == null) {
    return;
  }

  if (this.dirty) {
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

  $.each(world.idPlayers, function(id, player) {
    self.movePlayer(player, t);
  });
}

Input.prototype.movePlayer = function(player, t) {
  var keys = player.keys;

  var speed = 6;
  var distance = speed * TILE_SIZE * t / 1000;

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

  if (dx != 0) {
    this.move(player, dx, 0);
  }
  if (dy != 0) {
    this.move(player, 0, dy);
  }
}

Input.prototype.move = function(player, dx, dy) {
  var rect = this.rect;
  rect.x = player.x + dx + player.hitbox.x;
  rect.y = player.y + dy + player.hitbox.y;
  rect.width = player.hitbox.width;
  rect.height = player.hitbox.height;

  if (this.intersectsBadTerrain(rect)) {
    return;
  }

  player.setX(player.x + dx);
  player.setY(player.y + dy);
}

Input.prototype.intersectsBadTerrain = function(rect) {
  var minI = Math.floor(rect.x / TILE_SIZE);
  var minJ = Math.floor(rect.y / TILE_SIZE);
  var maxI = Math.floor((rect.x + rect.width) / TILE_SIZE);
  var maxJ = Math.floor((rect.y + rect.height) / TILE_SIZE);

  for (var i = minI; i <= maxI; i++) {
    for (var j = minJ; j <= maxJ; j++) {
      if (!world.terrain.isWalkable(i, j)) {
        return true;
      }
    }
  }
  return false;
}

Input.prototype.listen = function() {
  var self = this;
  var consoleVisible = false;
  $(window).keydown(function(e) {
    if (e.key == "Enter") {
      if (consoleVisible) {
        var text = $(".console input").val().trim();
        $(".console input").val("");
        if (text) {
          network.send({
            command : "consoleInput",
            text : text
          });
        } else {
          $(".console").stop().fadeOut();
          consoleVisible = false;
        }
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
    if (window.me) {
      delete me.keys[e.key];
      self.dirty = true;
    }
  });
  $(".console input").blur(function() {
    $(this).focus();
  });
}