function LootChooser() {
  var self = this;
  $(".loot-chooser").on("click", ".loot", function() {
    if (self.closing) {
      return;
    }

    var item = $(this).data("item");

    network.send({
      command : "choose",
      id : item.id
    });
    self.closing = true;
    $(".loot-chooser").fadeOut();
    window.input.allowMovement = true;

    item.idCounter = 0;
    window.quickbar.add(item);
  });
}

LootChooser.prototype.show = function(choices) {
  var chooser = $(".loot-chooser");
  chooser.find(".loot").remove();
  for (var i = 0; i < choices.length; i++) {
    var choice = choices[i];
    var loot = $(".loot.prototype").clone().removeClass("prototype").appendTo(chooser);
    loot.data("item", choice);
    loot.find("img").attr("src", choice.iconUrl);
    loot.find(".name").text(choice.name);
    loot.find(".mana").text(choice.manaCost + " mana");
    loot.find(".description").text(choice.description);
  }
  chooser.fadeIn();

  this.closing = false;

  window.input.haltMovement();
}

