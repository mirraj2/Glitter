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

    window.inventory.add(item);
  });
}

LootChooser.prototype.show = function(choices) {
  var chooser = $(".loot-chooser");
  chooser.find(".loot").remove();
  for (var i = 0; i < choices.length; i++) {
    var choice = choices[i];
    var loot = $(".loot.prototype").clone().removeClass("prototype").appendTo(chooser);
    loot.data("item", choice);
    loot.find("img").attr("src", choice.imageUrl);
    loot.find(".name").text(choice.name);

    if (choice.type == "spell") {
      loot.find(".mana").text(choice.manaCost + " mana");
      loot.find(".description").text(choice.description);
    } else if (choice.type == "armor") {
      var description = loot.find(".description");
      choice.stats.forEach(function(e) {
        e.stat = e.stat.toLowerCase();
        var text, school;
        if (e.stat == "health") {
          text = "+" + e.value + " Health";
        } else if (e.stat == "fire" || e.stat == "ice" || e.stat == "holy" || e.stat == "unholy") {
          school = e.stat;
          text = e.value + "% increased " + e.stat + " damage";
        } else {
          text = "+" + e.value + " " + e.stat;
        }
        var div = $("<div>").addClass("stat").text(text).appendTo(description);
        div.addClass(school);
      });
    }
  }
  chooser.fadeIn();

  this.closing = false;

  window.input.haltMovement();
}
