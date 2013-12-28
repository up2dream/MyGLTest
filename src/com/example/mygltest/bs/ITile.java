package com.example.mygltest.bs;

import java.util.Collection;

import javax.microedition.khronos.opengles.GL11;

import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.Size;

public interface ITile {
	public boolean isDirty();
	public void invalidate(final Rect rt);
	public Collection<Rect> updateBackBuffer();
	public void swapBackBufferToFront();
	public boolean isReadyToPaint();
	public void paint(GL11 gl, final Rect rt, float scaleFactor);
	public Coordinate coordinate();
	public Rect rect();
	public void resize(final Size sz);
	public void resize(final int cx, final int cy);
}
