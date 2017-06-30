function checkNotNull(arg) {
  if (typeof arg === "undefined") {
    throw new Error("This should not be null!");
  }
}