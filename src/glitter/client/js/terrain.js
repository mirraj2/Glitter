function Terrain(json) {
  this.tiles = json.tiles;
}

Terrain.prototype.getPixelWidth = function() {
  return this.tiles.length * TILE_SIZE;
}

Terrain.prototype.getPixelHeight = function() {
  return this.tiles[0].length * TILE_SIZE;
}
