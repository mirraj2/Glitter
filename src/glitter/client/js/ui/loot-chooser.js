function LootChooser() {
  var self = this;
  $(".loot-chooser").on("click", ".slot", function() {
    if (self.closing) {
      return;
    }

    var item = $(this).find("img").data("item");

    network.send({
      command : "choose",
      id : item.id
    });
    self.closing = true;
    $(".loot-chooser").fadeOut();
    window.input.allowMovement = true;

    window.inventory.add(item);

    window.tooltips.hide();
  });
}

LootChooser.prototype.show = function(choices) {
  var panel = $(".loot-chooser .choices").empty();
  for (var i = 0; i < choices.length; i++) {
    var choice = choices[i];
    var slot = $("<div>").addClass("slot").appendTo(panel);
    slot.attr("rarity", choice.rarity);
    $("<img>").attr("src", choice.imageUrl).data("item", choice).appendTo(slot);
  }
  panel.closest(".loot-chooser").fadeIn();

  this.closing = false;

  window.input.haltMovement();
}
