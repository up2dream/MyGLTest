package com.example.mygltest.bs;

import javax.microedition.khronos.opengles.GL11;

public interface ITiledBackingStoreBackend {
	
	ITile createTile(final Coordinate coordinate);
    void paintCheckerPattern(GL11 gl, final float left, final float top, final float width, final float height);
    
}
