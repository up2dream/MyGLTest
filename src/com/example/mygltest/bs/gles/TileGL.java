package com.example.mygltest.bs.gles;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL11;

import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.Size;

import com.example.mygltest.bs.Coordinate;
import com.example.mygltest.bs.Tile;
import com.example.mygltest.bs.TiledBackingStore;

public class TileGL implements Tile {
	private Coordinate mCoordinate = new Coordinate();
	private TextureBuffer mTextureBuffer;
	private int mFrontTextureID;
	private int mBackTextureID;
	private TiledBackingStore mBS;
	private Rect mRect = new Rect();

	public TileGL(TiledBackingStore bs, final Coordinate coordinate) {
		mBS = bs;
		mCoordinate.SetPoint(coordinate.getX(), coordinate.getY());
		
		mTextureBuffer = mBS.getTextureBuffer();
		int tileW = mBS.tileSize().getWidth();
		int tileH = mBS.tileSize().getHeight();
		
		mRect.setRect(mCoordinate.getX() * tileW, mCoordinate.getY() * tileH, tileW, tileH);
	}
	
	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void invalidate(Rect rt) {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<Rect> updateBackBuffer() {
		// TODO Auto-generated method stub
		return null;
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
	public void paint(GL11 gl, Rect rt) {
		mTextureBuffer.draw(gl, mFrontTextureID, mBS.getX() + mRect.getLeft(), mBS.getY() + mRect.getTop(), mRect.getWidth(), mRect.getHeight());
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
