function Tooltips() {
  this.listen();
}

Tooltips.prototype.hide = function() {
  $(".tooltip").css("opacity", 0);
}

Tooltips.prototype.listen = function() {
  var tooltip = $(".tooltip");
  var last = null;

  var self = this;
  $(".gui").on("mouseenter", ".slot", function() {
    var slot = $(this);
    if (!slot.hasClass("empty")) {
      var newlyShown = tooltip.css("opacity") == 0;
      last = setTimeout(function() {
        self.renderTooltip(slot, tooltip);
        var width = tooltip.outerWidth();
        var height = tooltip.outerHeight();
        if (newlyShown) {
          tooltip.addClass("notransition");
        }
        tooltip.css({
          left : (slot.offset().left + slot.width() / 2 - width / 2) + "px",
          top : (slot.offset().top - height - 8) + "px"
        });
        tooltip[0].offsetHeight; // Trigger a reflow, flushing the CSS changes
        tooltip.removeClass("notransition");

        tooltip.css("opacity", 1);
      }, newlyShown ? 500 : 0);
    }
  });
  $(".gui").on("mouseleave", ".slot", function() {
    clearInterval(last);
    tooltip.css({
      opacity : 0
    });
  });
}

Tooltips.prototype.renderTooltip = function(slot, tooltip) {
  tooltip.empty();

  var item = slot.find("img").data("item");
  $("<div>").addClass("name").text(item.name).appendTo(tooltip);

  if (item.type == "spell") {
    $("<div>").addClass("mana").text(item.manaCost + " mana").appendTo(tooltip);
    $("<div>").addClass("description").text(item.description).appendTo(tooltip);
  } else if (item.type == "armor") {
    item.stats.forEach(function(e) {
      var stat = e.stat.toLowerCase();
      var text, school;
      if (stat == "health") {
        text = "+" + e.value + " Health";
      } else if (stat == "fire" || stat == "ice" || stat == "holy" || stat == "unholy") {
        school = stat;
        text = e.value + "% increased " + stat + " damage";
      } else {
        text = "+" + e.value + " " + stat;
      }
      $("<div>").addClass("stat").addClass(school).text(text).appendTo(tooltip);
    });
  }
}
