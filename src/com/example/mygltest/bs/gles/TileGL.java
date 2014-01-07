package com.example.mygltest.bs.gles;

import java.util.ArrayList;
import java.util.Collection;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.Size;

import com.example.mygltest.bs.Coordinate;
import com.example.mygltest.bs.ITile;
import com.example.mygltest.bs.TiledBackingStore;
import com.example.mygltest.gl.GLCanvas;

public class TileGL implements ITile {
	private Coordinate mCoordinate = new Coordinate();
	private int mFrontTextureID;
	private int mBackTextureID;
	private TiledBackingStore mBS;
	private Rect mRect = new Rect();
	private boolean mDirty = true;

	public TileGL(TiledBackingStore bs, final Coordinate coordinate) {
		mBS = bs;
		mCoordinate.SetPoint(coordinate.getX(), coordinate.getY());
		
		int tileW = mBS.getTileSize().getWidth();
		int tileH = mBS.getTileSize().getHeight();
		
		mRect.setRect(mCoordinate.getX() * tileW, mCoordinate.getY() * tileH, tileW, tileH);
	}
	
	@Override
	public boolean isDirty() {
		return mDirty;
	}

	@Override
	public void invalidate(Rect rt) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Rect> updateBackBuffer() {
		Bitmap bitmap = Bitmap.createBitmap(mRect.getWidth(), mRect.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.save();
		
		canvas.drawColor(mBS.getClient().tbsGetBackgroundColor().getArgb());
		mBS.getClient().tbsPaint(canvas, mRect);
		canvas.restore();
		
		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		paint.setColor(Color.GREEN);
		canvas.drawRect(0, 0, mRect.getWidth()-1, mRect.getHeight()-1, paint);
		paint.setColor(Color.RED);
		canvas.drawRect(10, 10, mRect.getWidth() - 10, mRect.getHeight() - 10, paint);
		paint.setTextSize(15);
		canvas.drawText("(" + mCoordinate.getX() + ", " + mCoordinate.getY() + ")", 15, 25, paint);
		
		TiledBackingStoreBackendGL backend = (TiledBackingStoreBackendGL) mBS.getBackend();
		mBackTextureID = backend.writeToTextureSync(mBackTextureID, bitmap);
		
		bitmap.recycle();
		
		ArrayList<Rect> result = new ArrayList<Rect>(1);
		return result;
	}

	@Override
	public void swapBackBufferToFront() {
		int tmp = mFrontTextureID;
		mFrontTextureID = mBackTextureID;
		mBackTextureID = tmp;
	}

	@Override
	public boolean isReadyToPaint() {
		return mFrontTextureID != 0;
	}

	@Override
	public void paint(GLCanvas canvas, Rect rt, float scaleFactor) {
		canvas.drawTexture(mFrontTextureID, 
				mBS.getX() + mRect.getLeft() * scaleFactor, mBS.getY() + mRect.getTop() * scaleFactor, 
				mRect.getWidth() * scaleFactor, mRect.getHeight() * scaleFactor);
	}

	@Override
	public Coordinate coordinate() {
		return mCoordinate;
	}

	@Override
	public final Rect rect() {
		return mRect;
	}

	@Override
	public void resize(Size sz) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resize(int cx, int cy) {
		// TODO Auto-generated method stub

	}

}
