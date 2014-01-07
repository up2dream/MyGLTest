package com.example.mygltest.bs;

import com.example.mygltest.gl.GLCanvas;

public interface ITiledBackingStoreBackend {
	
	ITile createTile(final Coordinate coordinate);
    void paintCheckerPattern(GLCanvas canvas, final float left, final float top, final float width, final float height);
    
}
