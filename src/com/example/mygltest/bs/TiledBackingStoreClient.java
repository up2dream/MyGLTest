package com.example.mygltest.bs;

import java.util.List;

import javax.microedition.khronos.opengles.GL11;

import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.color.Color;

public abstract class TiledBackingStoreClient {
	
	abstract void tiledBackingStorePaintBegin();
	abstract void tiledBackingStorePaint(GL11 gl, final Rect rect);
	abstract void tiledBackingStorePaintEnd(final List<Rect> paintedArea);
	void tiledBackingStoreHasPendingTileCreation() { }
	abstract Rect tiledBackingStoreContentsRect();
	abstract Rect tiledBackingStoreVisibleRect();
	abstract Color tiledBackingStoreBackgroundColor();
	
}
