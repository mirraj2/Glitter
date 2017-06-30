function interpolate (from, to, percent) {
  percent = Math.min(percent, 1)
  return from + percent * (to - from)
}

module.exports = function camera (options = {}) {
  let update = function (millis) {
    const w = $(window).width()
    const h = $(window).height()
    const me = window.me
    const world = window.world

    if (me) {
      // move 10% of the way torwards the character's current position
      world.container.x = Math.round(interpolate(world.container.x, w / 2 - (me.x + me.width / 2), millis / 2000))
      world.container.y = Math.round(interpolate(world.container.y, h / 2 - (me.y + me.height / 2), millis / 2000))
    }
  }

  return Object.freeze({ update })
}
