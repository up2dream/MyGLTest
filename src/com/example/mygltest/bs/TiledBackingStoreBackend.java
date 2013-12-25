package com.example.mygltest.bs;

import javax.microedition.khronos.opengles.GL11;

import cn.wps.moffice.presentation.sal.drawing.RectF;

public interface TiledBackingStoreBackend {
	Tile createTile(TiledBackingStore tiledBackingStore, final Coordinate coordinate);
    void paintCheckerPattern(GL11 gl, final RectF rect);
}
