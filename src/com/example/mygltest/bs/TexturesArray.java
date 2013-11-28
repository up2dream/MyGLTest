package com.example.mygltest.bs;

import android.util.SparseIntArray;

public class TexturesArray {

	private int mCount = 0;
	private SparseIntArray mTextures = new SparseIntArray();
	
	public TexturesArray() {
	}
	
	public boolean isEmpty() {
		return mCount == 0;
	}
	
	public int count() {
		return mCount;
	}
	
	public int realCount() {
		return mTextures.size();
	}
	
	public void clear() {
		mCount = 0;
		mTextures.clear();
	}
	
	public int[] buffer() {
		int[] buffer = new int[mTextures.size()];
		for (int i=0; i<mTextures.size(); ++i) {
			buffer[i] = mTextures.valueAt(i);
		}
		return buffer;
	}
	
	public int at(int index) {
		return mTextures.get(index);
	}
	
	public void set(int index, int value) {
		mTextures.put(index, value);
	}
	
	public void resize(int count) {
		mCount = count;
		mTextures.clear();
	}
	
	public void fill(int value) {
		for (int i=0; i<mTextures.size(); ++i) {
			int key = mTextures.keyAt(i);
			mTextures.put(key, value);
		}
	}
}
