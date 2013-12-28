package com.example.mygltest.bs;

import java.util.List;

import android.graphics.Canvas;
import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.color.Color;

public abstract class TiledBackingStoreClient {
	
	public abstract void tbsPaintBegin();
	public abstract void tbsPaint(Canvas canvas, final Rect rect);
	public abstract void tbsPaintEnd(final List<Rect> paintedArea);
	public void tbsHasPendingTileCreation() { }
	public abstract Rect tbsGetContentsRect();
	public abstract Rect tbsGetVisibleRect();
	public abstract Color tbsGetBackgroundColor();
	
}
