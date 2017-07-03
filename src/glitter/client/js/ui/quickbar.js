function Quickbar() {
  var self = this;
  $(".quickbar").on("click", ".slot", function() {
    self.selectSlot(this);
  });
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

Quickbar.prototype.add = function(item) {
  var slot = this.getEmptySlot();
  if (slot) {
    $(slot).data("item", item);
    $("<img>").attr("src", item.iconUrl).appendTo(slot);
  }
}

Quickbar.prototype.getSelectedItem = function() {
  return $(".quickbar .selected.slot").data("item");
}

Quickbar.prototype.getEmptySlot = function() {
  var slots = $(".quickbar .slot");
  for (var i = 0; i < slots.length; i++) {
    var slot = $(slots[i]);
    if (slot.find("img").length == 0) {
      return slots[i];
    }
  }
  return null;
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
