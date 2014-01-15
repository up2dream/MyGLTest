package cn.wps.moffice.presentation.interaction.view.gl.bs.gles;

import android.graphics.Bitmap;

import cn.wps.moffice.presentation.interaction.view.gl.GLCanvas;
import cn.wps.moffice.presentation.interaction.view.gl.bs.Coordinate;
import cn.wps.moffice.presentation.interaction.view.gl.bs.ITile;
import cn.wps.moffice.presentation.interaction.view.gl.bs.ITiledBackingStoreBackend;
import cn.wps.moffice.presentation.interaction.view.gl.bs.TiledBackingStore;

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

	public int writeToTextureSync(int textureID, Bitmap bitmap) {
		return mTextureBuffer.writeToTextureSync(textureID, bitmap);
	}

}
