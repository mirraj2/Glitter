function Terrain(json) {
  this.tiles = json.tiles;
  this.width = this.tiles.length;
  this.height = this.tiles[0].length;
}

Terrain.prototype.getPixelWidth = function() {
  return this.width * Tile.SIZE;
}

Terrain.prototype.getPixelHeight = function() {
  return this.height * Tile.SIZE;
}

Terrain.prototype.isWalkable = function(i, j) {
  if (i < 0 || j < 0 || i >= this.width || j >= this.height) {
    return false;
  }
  var tile = this.tiles[i][j];
  return this.isWalkable(tile);
}

Terrain.prototype.isWalkable = function(tile) {
  return tile == Tile.GRASS || tile == Tile.BRIDGE;
}

Terrain.prototype.getTilesIntersecting = function(x, y, w, h, callback) {
  var minI = Math.max(Math.floor(x / Tile.SIZE), 0);
  var minJ = Math.max(Math.floor(y / Tile.SIZE), 0);
  var maxI = Math.min(Math.floor((x + w) / Tile.SIZE), this.width - 1);
  var maxJ = Math.min(Math.floor((y + h) / Tile.SIZE), this.height - 1);

  for (var i = minI; i <= maxI; i++) {
    for (var j = minJ; j <= maxJ; j++) {
      var tile = {
        x : i,
        y : j,
        type : this.tiles[i][j]
      };
      if (callback(tile) === false) {
        return;
      }
    }
  }
}
