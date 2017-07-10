/**
 * HTML5 dnd fucks with the cursor and has a lot of other drawbacks.
 * 
 * Use this class when you don't need cross-window DND support.
 */
var DND = (function() {
  var pressed = null;
  var dragging = null;
  var pressX, pressY;
  var dragStartCallback, dragEndCallback;

  $(document).on("mousemove", function(e) {
    if (pressed != null) {
      if (dragging == null) {
        var dx = e.pageX - pressX;
        var dy = e.pageY - pressY;
        if (dx * dx + dy * dy >= 9) {
          dragging = pressed;
          dragStartCallback($(pressed));
        }
      }
    }
  });
  $(document).on("mouseup", function(e) {
    if (dragging != null) {
      dragEndCallback($(e.target));
    }
    pressed = dragging = null;
  });
  return {
    listen : function(parent, child, onDragStart, onDragEnd) {
      $(parent).on("mousedown", child, function(e) {
        e.preventDefault();
        pressX = e.pageX;
        pressY = e.pageY;
        pressed = e.currentTarget;
        dragStartCallback = onDragStart;
        dragEndCallback = onDragEnd;
      });
    }
  };
})();