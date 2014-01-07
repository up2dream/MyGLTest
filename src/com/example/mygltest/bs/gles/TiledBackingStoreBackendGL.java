package com.example.mygltest.bs.gles;

import android.graphics.Bitmap;

import com.example.mygltest.bs.Coordinate;
import com.example.mygltest.bs.ITile;
import com.example.mygltest.bs.ITiledBackingStoreBackend;
import com.example.mygltest.bs.TiledBackingStore;
import com.example.mygltest.gl.GLCanvas;

public class TiledBackingStoreBackendGL implements ITiledBackingStoreBackend {
	
	private TiledBackingStore mTiledBackingStore;
	private TextureBuffer mTextureBuffer;
	
	public TiledBackingStoreBackendGL() {

	}
	
	public void init(TiledBackingStore tiledBackingStore) {
		mTiledBackingStore = tiledBackingStore;
		mTextureBuffer = mTiledBackingStore.getTextureBuffer();
	}

	@Override
	public ITile createTile(final Coordinate coordinate) {
		return new TileGL(mTiledBackingStore, coordinate);
	}

	@Override
	public void paintCheckerPattern(GLCanvas canvas, final float left, final float top, final float width, final float height) {
		canvas.drawTexture(mTextureBuffer.getCheckerTextureID(canvas.getGL(), (int)width, (int)height), mTiledBackingStore.getX() + left, mTiledBackingStore.getY() + top, width, height);
		
	}
	
	public int writeToTextureSync(int textureID, Bitmap bitmap) {
		return mTextureBuffer.writeToTextureSync(textureID, bitmap);
	}

}
