package com.example.mygltest.bs.gles;

import javax.microedition.khronos.opengles.GL11;

import com.example.mygltest.bs.Coordinate;
import com.example.mygltest.bs.Tile;
import com.example.mygltest.bs.TiledBackingStore;
import com.example.mygltest.bs.TiledBackingStoreBackend;

public class TiledBackingStoreBackendGL implements TiledBackingStoreBackend {
	
	private TiledBackingStore mTiledBackingStore;
	private TextureBuffer mTextureBuffer;
	
	public TiledBackingStoreBackendGL(TiledBackingStore tiledBackingStore) {
		mTiledBackingStore = tiledBackingStore;
		mTextureBuffer = mTiledBackingStore.getTextureBuffer();
	}

	@Override
	public Tile createTile(final Coordinate coordinate) {
		return new TileGL(mTiledBackingStore, coordinate);
	}

	@Override
	public void paintCheckerPattern(GL11 gl, final float left, final float top, final float width, final float height) {
		mTextureBuffer.drawChecker(gl, mTiledBackingStore.getX() + left, mTiledBackingStore.getY() + top, width, height);
	}

}
