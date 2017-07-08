function checkNotNull(arg) {
  if (typeof arg === "undefined") {
    throw new Error("This should not be null!");
  }
  return arg;
}

function interpolate(from, to, percent) {
  percent = Math.min(percent, 1)
  return from + percent * (to - from)
}