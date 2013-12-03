package com.example.mygltest.bs;

import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL11;
import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.Size;

public interface Tile {
	public boolean isDirty();
	public void invalidate(final Rect rt);
	public ArrayList<Rect> updateBackBuffer();
	public void swapBackBufferToFront();
	public boolean isReadyToPaint();
	public void paint(GL11 gl, final Rect rt);

	public Coordinate coordinate();
	public Rect rect();
	public void resize(final Size sz);
}
