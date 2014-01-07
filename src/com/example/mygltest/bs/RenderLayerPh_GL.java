package com.example.mygltest.bs;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.color.Color;

import com.example.mygltest.bs.gles.TextureBuffer;
import com.example.mygltest.bs.gles.TiledBackingStoreBackendGL;
import com.example.mygltest.gl.GLCanvas;

public class RenderLayerPh_GL extends TiledBackingStoreClient {

	private TiledBackingStore mBS;
	private Rect mVisibleRect = new Rect();
	private Rect mContentsRect = new Rect();
	private Color mBgColor = Color.parse("#7F0000FF");
	
	public RenderLayerPh_GL(TextureBuffer textureBuffer) {
		TiledBackingStoreBackendGL backend = new TiledBackingStoreBackendGL();
		mBS = new TiledBackingStore(this, backend, textureBuffer);
		backend.init(mBS);
		
		mBS.setPosition(200, 200);
		
		mVisibleRect.setRect(100, 100, 1000, 1000);
		mContentsRect.setRect(0, 0, 4000, 3000);
	}
	
	public int getX() {
		return mBS.getX();
	}
	
	public void setX(int x) {
		mBS.setX(x);
	}
	
	public int getY() {
		return mBS.getY();
	}
	
	public void setY(int y) {
		mBS.setY(y);
	}
	
	public void setPosition(int x, int y) {
		mBS.setPosition(x, y);
	}
	
	@Override
	public void tbsPaintBegin() {

	}

	@Override
	public void tbsPaint(Canvas canvas, Rect rect) {
		canvas.translate(-rect.getLeft(), -rect.getTop());
		float contentsScale = mBS.getContentsScale();
		LinearGradient mLinearGradient = new LinearGradient(0,0,500*contentsScale,500*contentsScale,  
                new int[] {android.graphics.Color.RED, android.graphics.Color.GREEN, android.graphics.Color.BLUE, android.graphics.Color.WHITE},  
                null, Shader.TileMode.REPEAT);
		Paint paint = new Paint();
		paint.setShader(mLinearGradient);
		canvas.clipRect(rect.getLeft(), rect.getTop(), rect.getRight(), rect.getBottom());
		canvas.drawRect(mContentsRect.getLeft() * contentsScale, mContentsRect.getTop() * contentsScale, 
				mContentsRect.getRight() * contentsScale, mContentsRect.getBottom() * contentsScale, paint);  
	}

	@Override
	public void tbsPaintEnd(List<Rect> paintedArea) {

	}

	@Override
	public Rect tbsGetContentsRect() {
		return mContentsRect;
	}

	@Override
	public Rect tbsGetVisibleRect() {
		Rect visibleRect = mVisibleRect.clone();
		
		int left = (int) (visibleRect.getLeft()/mBS.getContentsScale());
		int top = (int)(visibleRect.getTop()/mBS.getContentsScale());
		int width = (int)(visibleRect.getWidth()/mBS.getContentsScale());
		int height = (int)(visibleRect.getHeight()/mBS.getContentsScale());
		visibleRect.setRect(left, top, width, height);
		
		return visibleRect;
	}

	@Override
	public Color tbsGetBackgroundColor() {
		return mBgColor ;
	}

	public void paint(GLCanvas canvas) {
		mBS.paint(canvas, tbsGetVisibleRect());
	}
	
	public void setContentsScale(float scale) {
		mBS.setContentsScale(scale);
	}
	
	public float getContentsScale() {
		return mBS.getContentsScale();
	}
	
	public boolean isContentsFrozen() {
		return mBS.isContentsFrozen(); 
	}
	
	public void setContentsFrozen(boolean freeze) {
		mBS.setContentsFrozen(freeze);
	}
}
