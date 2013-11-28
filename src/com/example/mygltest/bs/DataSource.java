package com.example.mygltest.bs;

import android.graphics.Canvas;
import android.graphics.Paint;

public interface DataSource {
	
	public int getWidth();
	
	public int getHeight();
	
	public void draw(Canvas canvas, int updateX, int updateY, Paint paint);

}
