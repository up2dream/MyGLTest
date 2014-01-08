package com.example.mygltest;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import cn.wps.moffice.presentation.sal.drawing.Point;

import com.example.mygltest.bs.BSGLSurfaceView;
import com.example.mygltest.bs.RenderLayerPh_GL;

public class MainActivity extends Activity {

	private static MainActivity sInstance;

	private BSGLSurfaceView mGLSurfaceView;
	private float oldDist = 0;
	private float oldWidth = 0;
	private float oldHeight = 0;

	public static MainActivity getInstance() {
		return sInstance;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sInstance = this;
		
		super.onCreate(savedInstanceState);
		
	    mGLSurfaceView = new BSGLSurfaceView(this);
	    // Check if the system supports OpenGL ES 2.0.
	    final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
	    final boolean supportsEs2 = false;//configurationInfo.reqGlEsVersion >= 0x20000;
	 
//	    mRenderer.setDS(new SimpleDataSource());
	    
	    if (supportsEs2) {
	        // Request an OpenGL ES 2.0 compatible context.
	        mGLSurfaceView.setEGLContextClientVersion(2);
	 
	        // Set the renderer to our demo renderer, defined below.
//	        mGLSurfaceView.setRenderer(new MyGLRenderer(mGLSurfaceView));
//	        mGLSurfaceView.setRenderer(mGLSurfaceView);
	    } else {
	        // This is where you could create an OpenGL ES 1.x compatible
	        // renderer if you wanted to support both ES 1 and ES 2.
//	        mGLSurfaceView.setRenderer(new MyGLRenderer(mGLSurfaceView));
//	        mGLSurfaceView.setRenderer(mGLSurfaceView);
	    }
	 
	    mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	    
	    setContentView(mGLSurfaceView);
	}

	@Override
	protected void onResume()
	{
	    // The activity must call the GL surface view's onResume() on activity onResume().
	    super.onResume();
	    mGLSurfaceView.onResume();
	}
	 
	@Override
	protected void onPause()
	{
	    // The activity must call the GL surface view's onPause() on activity onPause().
	    super.onPause();
	    mGLSurfaceView.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		RenderLayerPh_GL renderLayer = mGLSurfaceView.getRenderLayer();
		switch (item.getItemId()) {
		case R.id.request_render:
			mGLSurfaceView.requestRender();
			return true;
		case R.id.draw_bs:
			renderLayer.setSize(800, 800);
			
			return true;
		case R.id.zoom_out:
			renderLayer.setSize((int)(renderLayer.getWidth() * .7f), (int)(renderLayer.getHeight() * .7f));
			return true;
		case R.id.zoom_in:
			renderLayer.setSize((int)(renderLayer.getWidth() * 1.3f), (int)(renderLayer.getHeight() * 1.3f));
			return true;
		case R.id.draw_simple:
			Bitmap bitmap = Bitmap.createBitmap(200, 200, Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(Color.RED);
			mGLSurfaceView.getTextureBuffer().writeToTextureSync(0, bitmap);
			bitmap.recycle();
//			mRenderer.mouseZoom(scale, new Point(mGLSurfaceView.getWidth()/2, mGLSurfaceView.getHeight()/2));
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
	
	private Point m_mousePressPosition = new Point();
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			m_mousePressPosition.SetPoint((int)event.getX(), (int)event.getY());
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() > 1) {  
		        float newDist = spacing(event);  
		        if (Math.abs(newDist - oldDist) > 1) {
					float scale = newDist / oldDist;
					Log.d("test", "scale " + scale);
					RenderLayerPh_GL layer = mGLSurfaceView.getRenderLayer();
					layer.setSize((int)(oldWidth * scale), (int)(oldHeight * scale));
					mGLSurfaceView.update();
		        }  
		        break;  
		    } else {
				int offsetX = (int) (event.getX() - m_mousePressPosition.getX());
				int offsetY = (int) (event.getY() - m_mousePressPosition.getY());
				RenderLayerPh_GL layer = mGLSurfaceView.getRenderLayer();
				layer.setPosition(layer.getX() + offsetX, layer.getY() + offsetY);
				
				m_mousePressPosition.SetPoint((int)event.getX(), (int)event.getY());
				mGLSurfaceView.update();
		    }
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldWidth = mGLSurfaceView.getRenderLayer().getWidth();
			oldHeight = mGLSurfaceView.getRenderLayer().getHeight();
			oldDist = spacing(event);//两点按下时的距离  
			mGLSurfaceView.getRenderLayer().setContentsFrozen(true);
			break;
		case MotionEvent.ACTION_UP:
			mGLSurfaceView.getRenderLayer().setContentsFrozen(false);
			break;
		}
		
		return super.onTouchEvent(event);
	}
	
	private float spacing(MotionEvent event) {  
	    float x = event.getX(0) - event.getX(1);  
	    float y = event.getY(0) - event.getY(1);  
	    return FloatMath.sqrt(x * x + y * y);  
	}  

}
