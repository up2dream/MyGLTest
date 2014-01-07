package com.example.mygltest.bs;

import java.util.Collection;

import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.Size;

import com.example.mygltest.gl.GLCanvas;

public interface ITile {
	public boolean isDirty();
	public void invalidate(final Rect rt);
	public Collection<Rect> updateBackBuffer();
	public void swapBackBufferToFront();
	public boolean isReadyToPaint();
	public void paint(GLCanvas canvas, int offsetX, int offsetY, final Rect rt, float scaleFactor);
	public Coordinate coordinate();
	public Rect rect();
	public void resize(final Size sz);
	public void resize(final int cx, final int cy);
}
