package com.example.mygltest.bs;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;
import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.color.Color;

import com.example.mygltest.bs.gles.TextureBuffer;
import com.example.mygltest.bs.gles.TiledBackingStoreBackendGL;
import com.example.mygltest.gl.GLCanvas;

public class RenderLayerPh_GL extends TiledBackingStoreClient {

	private TiledBackingStore mBS;
	private Rect mContentsRect = new Rect();
	private Color mBgColor = Color.parse("#7F0000FF");
	private int mX;
	private int mY;
	private int mWidth;
	private int mHeight;
	
	public RenderLayerPh_GL(TextureBuffer textureBuffer) {
		TiledBackingStoreBackendGL backend = new TiledBackingStoreBackendGL();
		mBS = new TiledBackingStore(this, backend, textureBuffer);
		backend.init(mBS);

		mContentsRect.setRect(0, 0, 4000, 3000);

		mBS.setContentsFrozen(true);
		
		setPosition(200, 200);
		setSize(1000, 1000);
	}
	
	public int getX() {
		return mX;
	}
	
	public void setX(int x) {
		mX = x;
	}
	
	public int getY() {
		return mY;
	}
	
	public void setY(int y) {
		mY = y;
	}
	
	public int getWidth() {
		return mWidth;
	}
	
	public void setWidth(int width) {
		mWidth = width;
	}
	
	public int getHeight() {
		return mHeight;
	}
	
	public void setHeight(int height) {
		mHeight = height;
	}
	
	public void setPosition(int x, int y) {
		mX = x;
		mY = y;
	}
	
	public void setSize(int width, int height) {
		mWidth = width;
		mHeight = height;
		float scaleW = mWidth / (float)mContentsRect.getWidth();
		float scaleH = mHeight / (float)mContentsRect.getHeight();
		if (scaleW > scaleH) {
			mBS.setContentsScale(scaleH);
		} else {
			mBS.setContentsScale(scaleW);
		}
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
		Rect visible = new Rect(-mX, -mY, 2560, 1600);
		visible.intersect(new Rect(0, 0, mWidth, mHeight));
		float scale = mWidth / (float)mContentsRect.getWidth();
		visible.setLeft((int) (visible.getLeft() / scale));
		visible.setTop((int) (visible.getTop() / scale));
		visible.setWidth((int) (visible.getWidth() / scale));
		visible.setHeight((int) (visible.getHeight() / scale));
		return visible;
	}

	@Override
	public Color tbsGetBackgroundColor() {
		return mBgColor ;
	}

	public void paint(GLCanvas canvas) {
		mBS.paint(canvas, mX, mY, tbsGetVisibleRect());
		
		canvas.setLineColor(0, 0, 1, 1);
		canvas.setLineWidth(2);
		canvas.drawRect(mX, mY, mWidth, mHeight, false);
	}
	
	public void setContentsFrozen(boolean freeze) {
		mBS.setContentsFrozen(freeze);
	}
}
