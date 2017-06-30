function Camera() {
}


function clamp(val, min, max) {
  if(val >= max) return max;

  if(val <= min) return min;

  return val;
}


function scalarLerp(p, n, t) {
  let _t = clamp(Number(t), 0.0, 1.0);
  return p + _t * (n - p);
}


function pointLerp(p, n, t) {
  p.x = scalarLerp(p.x, n.x, t);
  p.y = scalarLerp(p.y, n.y, t);
}


Camera.prototype.update = function(t) {
  var w = $(window).width();
  var h = $(window).height();
  // if (world.terrain) {
  // world.container.x = Math.round((w - world.terrain.getPixelWidth()) / 2);
  // world.container.y = Math.round((h - world.terrain.getPixelHeight()) / 2);
  // }

  if (window.me) {
    var newCharacterPosition = {
      x: Math.round(w / 2 - (Math.round(me.x) + me.width / 2)),
      y: Math.round(h / 2 - (Math.round(me.y) + me.height / 2))
    }

    // move 10% of the way torwards the character's current position
    pointLerp(world.container, newCharacterPosition, 0.1);

    //world.container.x = 100;
    //world.container.y = 100;
    //world.container.scale.x = .1;
    //world.container.scale.y = .1;
  }
}
