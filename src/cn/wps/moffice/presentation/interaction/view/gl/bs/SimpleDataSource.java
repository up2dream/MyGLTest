package cn.wps.moffice.presentation.interaction.view.gl.bs;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class SimpleDataSource implements DataSource {
	
	public int getWidth() {
		return 30000;
	}
	
	public int getHeight() {
		return 20000;
	}

	public void draw(Canvas canvas, int updateX, int updateY, Paint paint) {
		canvas.drawColor(Color.rgb(255, 255, 0));
	}

}
