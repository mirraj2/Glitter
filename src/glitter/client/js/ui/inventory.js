function Inventory(quickbar) {
  this.quickbar = quickbar;
  this.init();
}

Inventory.prototype.add = function(item) {
  if (item.type == "spell-slot") {
    $("<div>").addClass("empty spell slot").appendTo(".quickbar");
    return;
  }

  var img = $("<img>").attr("src", item.imageUrl).data("item", item);

  if (item.type == "spell") {
    // see if we can place the spell right into the quickbar
    var slot = this.quickbar.getEmptySlot();
    if (slot) {
      $(slot).append(img).removeClass("empty");
      return;
    }
  } else if (item.type == "armor") {
    // see if we can equip this armor
    var armorSlot = $(".inventory .empty.slot[part=" + item.part.toLowerCase() + "]:first");
    if (armorSlot.length) {
      armorSlot.append(img).removeClass("empty");
      return;
    }
  }

  var bagSlot = $(".inventory .bag .empty.slot:first");
  if (bagSlot.length) {
    bagSlot.append(img).removeClass("empty");
  } else {
    console.log("Not enough space!");
  }
}

Inventory.prototype.equip = function(img) {
  var item = img.data("item");
  var from = img.parents(".slot");

  if (item.type == "spell") {
    // see if we can place the spell right into the quickbar
    var to = this.quickbar.getEmptySlot();
    if (to) {
      this.swap(from, $(to));
    }
  } else if (item.type == "armor") {
    // first try to find an empty slot
    var to = $(".inventory .empty.slot[part=" + item.part.toLowerCase() + "]:first");

    // just find a non-empty armor slot for this part
    if (to.length == 0) {
      to = $(".inventory .slot[part=" + item.part.toLowerCase() + "]:first");
    }

    this.swap(from, to);
  }
}

Inventory.prototype.canGoInSlot = function(item, slot) {
  if (item == null) {
    return true;
  }

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

  $(".inventory .bag").on("mousedown", ".slot", function(e) {
    if (e.button == 2) {
      var img = $(this).find("img");
      if (img.length) {
        self.equip(img);
      }
    }
  });

  $(".gui .slot").each(function() {
    $(this).addClass("empty");
    var part = $(this).attr("part");
    if (part) {
      $(this).css("background-image", "url('/inventory/" + part + ".png')");
    }
  });

  DND.listen(".gui", ".slot:not(.empty)", function(slot) {
    if (slot.closest(".loot-chooser").length) {
      // can't drag things from the loot chooser
      return false;
    }

    self.sourceSlot = slot;

    $(".gui .slot").each(function() {
      var fromItem = self.sourceSlot.find("img").data("item");
      var toItem = $(this).find("img").data("item");
      if (this != self.sourceSlot[0] && self.canGoInSlot(fromItem, $(this)) && self.canGoInSlot(toItem, self.sourceSlot)) {
        $(this).addClass("drop-target");
      }
    });
  }, function(target) {
    self.onDrop(target);
  });
}

Inventory.prototype.onDrop = function(target) {
  var to = target.closest(".slot");
  if (to.length) {
    var from = this.sourceSlot;
    if (from && from[0] != to[0]) {
      // make sure this item can go into this slot
      if (this.canGoInSlot(from.find("img").data("item"), to) && this.canGoInSlot(to.find("img").data("item"), from)) {
        this.swap(from, to);
      }
    }
  } else {
    // they didn't drop it on a slot.
    if (target.is("canvas")) {
      var slot = this.sourceSlot;
      var img = slot.find("img");
      slot.addClass("empty");
      network.send({
        command : "drop",
        id : img.data("item").id
      });
      img.remove();
    }
  }

  $(".slot.drop-target").removeClass("drop-target");
}

/**
 * Swaps the contents of two inventory slots. Also notifies the server of any changes.
 */
Inventory.prototype.swap = function(from, to) {
  var imgA = from.find("img");
  var imgB = to.find("img");

  from.append(imgB);
  to.append(imgA);

  to.toggleClass("empty", imgA.length == 0);
  from.toggleClass("empty", imgB.length == 0);

  self.sourceSlot = null;

  var aInBag = from.parents(".bag").length > 0;
  var bInBag = to.parents(".bag").length > 0;
  if (aInBag ^ bInBag) {
    var itemA = imgA.data("item");
    var itemB = imgB.data("item");
    network.send({
      command : "swap",
      itemA : itemA ? itemA.id : null,
      itemB : itemB ? itemB.id : null
    });
  }
}

/**
 * Server is telling us the current contents of our bag. This is called after we change the item in our 'bag' slot.
 */
Inventory.prototype.bagUpdate = function(msg) {
  var bag = $(".inventory .bag").empty();
  for (var i = 0; i < msg.numSlots; i++) {
    var slot = $("<div>").addClass("slot").appendTo(bag);
    if (i < msg.items.length) {
      var item = msg.items[i];
      $("<img>").attr("src", item.imageUrl).data("item", item).appendTo(slot);
    } else {
      slot.addClass("empty");
    }
  }
}
