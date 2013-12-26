package com.example.mygltest;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import cn.wps.moffice.presentation.sal.drawing.Point;

import com.example.mygltest.bs.BSGLSurfaceView;
import com.example.mygltest.bs.SimpleDataSource;

public class MainActivity extends Activity {

	private BSGLSurfaceView mGLSurfaceView;
	private static MainActivity sInstance;

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
	        mGLSurfaceView.setRenderer(mGLSurfaceView);
	    } else {
	        // This is where you could create an OpenGL ES 1.x compatible
	        // renderer if you wanted to support both ES 1 and ES 2.
//	        mGLSurfaceView.setRenderer(new MyGLRenderer(mGLSurfaceView));
	        mGLSurfaceView.setRenderer(mGLSurfaceView);
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
	
	private float scale = 1f;
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.draw_on_layer_0:
			mGLSurfaceView.requestRender();
			return true;
		case R.id.zoom_out:
			scale -= .2f;
//			mRenderer.mouseZoom(scale, new Point(mGLSurfaceView.getWidth()/2, mGLSurfaceView.getHeight()/2));
			return true;
		case R.id.zoom_in:
			scale += .2f;
//			mRenderer.mouseZoom(scale, new Point(mGLSurfaceView.getWidth()/2, mGLSurfaceView.getHeight()/2));
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
	
	private Point m_mousePressPosition = new Point();
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			m_mousePressPosition.SetPoint((int)event.getX(), (int)event.getY());
		case MotionEvent.ACTION_MOVE:
//			mRenderer.m_viewOffset.offset(event.getX() - m_mousePressPosition.getX(), event.getY() - m_mousePressPosition.getY());
			
			m_mousePressPosition.SetPoint((int)event.getX(), (int)event.getY());
			mGLSurfaceView.update();
			return true;
		}
		
		return super.onTouchEvent(event);
	}

}
