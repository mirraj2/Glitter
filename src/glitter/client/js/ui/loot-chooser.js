function LootChooser() {
  var self = this;
  $(".loot-chooser").on("click", ".loot", function() {
    if (self.closing) {
      return;
    }
    network.send({
      command : "choose",
      id : $(this).data("id")
    });
    self.closing = true;
    $(".loot-chooser").fadeOut();
    window.input.allowMovement = true;
  });
}

LootChooser.prototype.show = function(choices) {
  var chooser = $(".loot-chooser");
  chooser.find(".loot").remove();
  for (var i = 0; i < choices.length; i++) {
    var choice = choices[i];
    var loot = $(".loot.prototype").clone().removeClass("prototype").appendTo(chooser);
    loot.data("id", choice.id);
    loot.find("img").attr("src", this.getIconUrl(choice));
    loot.find(".name").text(choice.name);
    loot.find(".mana").text(choice.manaCost + " mana");
    loot.find(".description").text(choice.description);
  }
  chooser.fadeIn();

  this.closing = false;

  window.input.haltMovement();
}

LootChooser.prototype.getIconUrl = function(item) {
  return "/spells/" + item.name.toLowerCase() + ".png";
}