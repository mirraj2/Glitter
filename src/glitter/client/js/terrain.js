function Terrain(json) {
  this.tiles = json.tiles;
  this.width = this.tiles.length;
  this.height = this.tiles[0].length;
}

Terrain.prototype.getPixelWidth = function() {
  return this.width * TILE_SIZE;
}

Terrain.prototype.getPixelHeight = function() {
  return this.height * TILE_SIZE;
}

Terrain.prototype.isWalkable = function(i, j) {
  if (i < 0 || j < 0 || i >= this.width || j >= this.height) {
    return false;
  }
  var tile = this.tiles[i][j];
  return tile == 1 || tile == 2;
}
