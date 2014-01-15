package cn.wps.moffice.presentation.interaction.view.gl.bs;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import cn.wps.moffice.presentation.interaction.view.gl.bs.gles.TextureBuffer;

// TODO �迼��Texture��leak����
public class TileBuffer {

	LinkedHashMap<Coordinate, ITile> mTiles = new LinkedHashMap<Coordinate, ITile>();
	 
	private TextureBuffer mTextureBuffer;
	private float mScale = 1;

	public TileBuffer(TextureBuffer textureBuffer, float scale) {
		mTextureBuffer = textureBuffer;
		mScale = scale;
	}

	public Iterator<Entry<Coordinate, ITile>> iterator() {
		return mTiles.entrySet().iterator();
	}

	public ITile get(Coordinate coordinate) {
		return mTiles.get(coordinate);
	}

	public void put(Coordinate coordinate, ITile tile) {
		mTiles.put(coordinate, tile);
	}

	public void moveAllTo(TileBuffer tiles) {
		tiles.mTiles.putAll(mTiles);
		mTiles.clear();
	}

	public ITile remove(Coordinate coordinate) {
		return mTiles.remove(coordinate);
	}

	public void clear() {
		mTiles.clear();
	}
	
	public float getScale() {
		return mScale;
	}
	
	public void setScale(float scale) {
		mScale = scale;
	}
}
