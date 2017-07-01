function Spells() {
}

Spells.prototype.cast = function(spell, x, y) {
  if (me.mana >= spell.manaCost) {
    me.mana -= spell.manaCost;
    console.log("Casting " + spell.name);

    this[spell.name.toLowerCase()](spell, x, y);
  }
}

Spells.prototype.fireball = function(spell, x, y) {
  new Emitter().numParticles(256).scale(.5).position(x, y);
}