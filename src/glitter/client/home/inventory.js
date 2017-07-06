$(".inventory .equipped [slot]").each(function() {
  $(this).addClass("empty");
  var slot = $(this).attr("slot");
  var img = $("<img>").attr("src", "/inventory/" + slot + ".png").appendTo(this);

  if (slot == "bag") {
    img.attr("src", "/small-bag.png");
    $(this).removeClass("empty");
  }
});