function Inventory(quickbar) {
  this.quickbar = quickbar;
  this.init();
}

Inventory.prototype.add = function(item) {
  var img = $("<img>").attr("src", item.imageUrl).attr("draggable", "true").data("item", item);

  if (item.type == "spell") {
    // see if we can place the spell right into the quickbar
    var slot = this.quickbar.getEmptySlot();
    if (slot) {
      $(slot).append(img).removeClass("empty");
      return;
    }
  } else if (item.type == "armor") {
    // see if we can equip this armor
    var armorSlot = $(".inventory .empty.slot[part=" + item.part.toLowerCase() + "]");
    if (armorSlot.length) {
      armorSlot.append(img).removeClass("empty");
      return;
    }
  }

  var bagSlot = $(".inventory .bag .empty.slot:first");
  if (bagSlot.length) {
    bagSlot.append(img).removeClass("empty");
  }
}

Inventory.prototype.canGoInSlot = function(item, slot) {
  if (item.type == "spell") {
    return !slot.hasClass("armor");
  } else if (item.type == "armor") {
    if (slot.hasClass("spell")) {
      return false;
    }
    var part = slot.attr("part");
    return part == null || part == item.part.toLowerCase();
  } else {
    return false;
  }
}

Inventory.prototype.init = function() {
  var self = this;

  $(".gui .slot").each(function() {
    $(this).addClass("empty");
    var part = $(this).attr("part");
    if (part) {
      $(this).css("background-image", "url('/inventory/" + part + ".png')");
    }
  });

  $(".gui").on("dragstart", ".slot", function(e) {
    self.sourceSlot = $(this);

    $(".gui .slot").each(function() {
      if (this != self.sourceSlot[0] && self.canGoInSlot(self.sourceSlot.find("img").data("item"), $(this))) {
        $(this).addClass("drop-target");
      }
    });
  });

  $(".gui").on("dragend", ".slot", function() {
    $(".slot.drop-target").removeClass("drop-target");
  });

  $(".gui").on("dragover", ".slot", function(e) {
    var to = $(e.target);
    if (!to.hasClass("slot")) {
      to = to.parents(".slot");
    }
    var from = self.sourceSlot;
    if (from && from[0] != to[0]) {
      // make sure this item can go into this slot
      if (self.canGoInSlot(from.find("img").data("item"), to)) {
        e.preventDefault();
      }
    }
  });

  $(".gui").on("drop", ".slot", function(e) {
    e.preventDefault();

    var from = self.sourceSlot;
    var to = $(e.target);
    if (!to.hasClass("slot")) {
      to = to.parents(".slot");
    }

    var imgA = from.find("img");
    var imgB = to.find("img");

    from.append(imgB);
    to.append(imgA);

    to.toggleClass("empty", imgA.length == 0);
    from.toggleClass("empty", imgB.length == 0);

    self.sourceSlot = null;
  });
}
