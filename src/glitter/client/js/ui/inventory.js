function Inventory(quickbar) {
  this.quickbar = quickbar;
}

Inventory.prototype.add = function(item) {
  var slot = this.quickbar.getEmptySlot();
  if (slot) {
    $("<img>").attr("src", item.iconUrl).attr("draggable", "true").data("item", item).appendTo(slot);
    $(slot).removeClass("empty");
  }
}

Inventory.prototype.canGoInSlot = function(item, slot) {
  if (item.type == "spell") {
    console.log(slot);
    return !slot.hasClass("armor");
  } else if (item.type == "armor") {
    return !slot.hasClass("spell");
  } else {
    return false;
  }
}

$(function() {
  $(".gui .slot").each(function() {
    $(this).addClass("empty");
    var part = $(this).attr("part");
    if (part) {
      $(this).css("background-image", "url('/inventory/" + part + ".png')");
    }
  });

  $(".gui").on("dragstart", ".slot", function(e) {
    self.sourceSlot = $(this);
  });

  $(".gui").on("dragover", ".slot", function(e) {
    var to = $(e.target);
    if (!to.hasClass("slot")) {
      to = to.parents(".slot");
    }
    var from = self.sourceSlot;
    if (from && from[0] != to[0]) {
      // make sure this item can go into this slot
      if (inventory.canGoInSlot(from.find("img").data("item"), to)) {
        e.preventDefault();
      }
    }
  });

  $(".gui").on("drop", ".slot", function(e) {
    e.preventDefault();

    var from = self.sourceSlot;
    var to = $(e.target);

    var imgA = from.find("img");
    var imgB = to.find("img");

    from.append(imgB);
    to.append(imgA);

    to.toggleClass("empty", imgA.length == 0);
    from.toggleClass("empty", imgB.length == 0);

    self.sourceSlot = null;
  });
});
