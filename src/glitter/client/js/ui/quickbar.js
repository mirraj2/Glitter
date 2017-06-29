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
  console.log(slot);
  if (slot) {
    $("<img>").attr("src", item.iconUrl).appendTo(slot);
  }
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

Quickbar.prototype.syncHealthAndMana = function() {
  $(".health .text").text(Math.round(me.health) + " / " + me.maxHealth);
  $(".mana .text").text(Math.round(me.mana) + " / " + me.maxMana);
  $(".health .fill").css("width", 100 * me.health / me.maxHealth + "%");
  $(".mana .fill").css("width", 100 * me.mana / me.maxMana + "%");
}
