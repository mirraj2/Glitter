function Quickbar() {
  var self = this;

  $(".quickbar").on("click", ".slot", function() {
    self.selectSlot(this);
  });
  
  glitter.register(this);
}

Quickbar.prototype.select = function(index) {
  if (--index == -1) {
    index = 9;
  }

  var slots = $(".quickbar .slot");
  this.selectSlot(slots[index]);
}

Quickbar.prototype.selectSlot = function(slot) {
  slot = $(slot);
  if (slot.hasClass("disabled")) {
    return;
  }
  slot.addClass("selected").siblings().removeClass("selected");
}

Quickbar.prototype.getSelectedItem = function() {
  return $(".quickbar .selected.slot img").data("item");
}

Quickbar.prototype.getEmptySlot = function() {
  var slots = $(".quickbar .empty.slot");
  if (slots.length == 0) {
    return null;
  }
  return slots[0];
}

Quickbar.prototype.update = function(millis) {
  if (window.me) {
    if (me.alive) {
      me.health = Math.min(me.maxHealth, me.health + me.healthRegenPerSecond * millis / 1000);
      me.mana = Math.min(me.maxMana, me.mana + me.manaRegenPerSecond * millis / 1000);
    }
    $(".health .text").text(Math.round(me.health) + " / " + me.maxHealth);
    $(".mana .text").text(Math.round(me.mana) + " / " + me.maxMana);
    this.interpBar($(".health .fill"), me.health / me.maxHealth, millis);
    this.interpBar($(".mana .fill"), me.mana / me.maxMana, millis);
  }
}

Quickbar.prototype.interpBar = function(bar, p, millis) {
  var w = bar.width();
  var w2 = p * bar.parent().width();
  bar.css("width", w + (w2 - w) * Math.min(1, millis / 300) + "px");
}
