package com.example.mygltest.bs;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.util.Log;
import cn.wps.moffice.presentation.sal.drawing.Point;
import cn.wps.moffice.presentation.sal.drawing.PointF;
import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.Size;

import com.example.mygltest.Square;
import com.example.mygltest.Triangle;

public class BSGLRenderer implements Renderer {

	public static final boolean DEBUG = true;

	private static final String TAG = BSGLRenderer.class.getSimpleName();
	
	private static final int UPDATE_DELAY = DEBUG ? 100 : 10;
	private static final int REFRESH_DELAY = DEBUG ? 600 : 20;//100;
	private static final int EXTRA_TILES = DEBUG ? 1 : 2;
	
	private static final float MinZoom = 0.2f;
	private static final float MaxZoom = 20f;
	
	private Timer mUpdateTimer;
	private Timer mRefreshTimer;
	private UpdateTimerTask mUpdateTimerTask = new UpdateTimerTask();
	private RefreshTimerTask mRefreshTimerTask = new RefreshTimerTask();

	public PointF m_viewOffset;
	private float m_viewZoomFactor;
	private DataSource mDS;

	private int m_defaultTexture;
	private TextureBuffer m_mainBuffer;
	private TextureBuffer m_secondaryBuffer;

	private BSGLSurfaceView mView;
	private GL11 mGL;
	private Size mSize = new Size();
	private boolean mNeedBindTexture = false;
	
	private Square square1 = new Square();
	private Square square2 = new Square();
	private Triangle triangle = new Triangle();

	private Bitmap mContent = Bitmap.createBitmap(TextureBuffer.TILE_DIM, TextureBuffer.TILE_DIM, Config.ARGB_8888);
	private int mUpdateX;
	private int mUpdateY;
    
	/** Constructor to set the handed over context */
	public BSGLRenderer(BSGLSurfaceView view) {
		mView = view;
		
		m_viewOffset = new PointF(0, 0);
		m_viewZoomFactor = 1;
	    m_defaultTexture = 0;
	    
	    m_mainBuffer = new TextureBuffer();
	    m_secondaryBuffer = new TextureBuffer();
	}
	
	public void scheduleUpdate() {
		killUpdateTimer();
		if (mUpdateTimer==null)
		mUpdateTimer = new Timer("bs_update_timer");
		Log.d("bs", "scheduleUpdate");
		mUpdateTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				updateBackingStore();
			}
		}, UPDATE_DELAY);
	}

	public void scheduleRefresh() {
		killRefreshTimer();
		if (mRefreshTimer==null)
		mRefreshTimer = new Timer("bs_refresh_timer");
		
	    mRefreshTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				refreshBackingStore();
			}
		}, REFRESH_DELAY);
	}
	
	private void killUpdateTimer() {
//		if (mUpdateTimer != null) {
//			mUpdateTimer.cancel();
//			mUpdateTimer = null;
//		}
	}
	
	public void killRefreshTimer() {
//		if (mRefreshTimer != null) {
//			mRefreshTimer.cancel();
//			mRefreshTimer = null;
//		}
	}
	
	public void refreshBackingStore()
	{
	    if (m_mainBuffer.mZoomFactor == m_viewZoomFactor)
	        return;

	    // All secondary textures serve as the "background overlay".
	    m_secondaryBuffer.clear();
	    m_secondaryBuffer = m_mainBuffer;

	    // Replace the primary textures with an invalid, dirty texture.
	    float width = mDS.getWidth() * m_viewZoomFactor;
	    float height = mDS.getHeight() * m_viewZoomFactor;
	    int horizontal = (int) ((width + TextureBuffer.TILE_DIM - 1) / TextureBuffer.TILE_DIM);
	    int vertical = (int) ((height + TextureBuffer.TILE_DIM - 1) / TextureBuffer.TILE_DIM);
	    m_mainBuffer.resize(horizontal, vertical);

	    m_mainBuffer.mZoomFactor = m_viewZoomFactor;
	    scheduleUpdate();

	    killRefreshTimer();
	}
	
	public void updateBackingStore()
	{
	    // During zooming in and out, do not bother.
	    if (m_mainBuffer.mZoomFactor != m_viewZoomFactor)
	        return;
	    
	    // Extend the update range with extra tiles in every direction, this is
	    // to anticipate panning and scrolling.
	    Rect updateRange = m_mainBuffer.visibleRange(m_viewOffset, m_viewZoomFactor, mSize);
	    updateRange.inflate(EXTRA_TILES, EXTRA_TILES);

	    // Collect all visible tiles which need update.
	    ArrayList<Point> dirtyTiles = new ArrayList<Point>();
	    for (int x = 0; x < m_mainBuffer.width(); ++x) {
	        for (int y = 0; y < m_mainBuffer.height(); ++y) {
	            if (m_mainBuffer.at(x, y) == 0 && updateRange.contains(x, y))
	                dirtyTiles.add(new Point(x, y));
	        }
	    }

	    if (!dirtyTiles.isEmpty()) {

	        // Find the closest tile to the center (using Manhattan distance)
	        int updateX = dirtyTiles.get(0).getX();
	        int updateY = dirtyTiles.get(0).getX();
	        float closestDistance = 1e6f;
	        for (int i = 0; i < dirtyTiles.size(); ++i) {
	            int tx = dirtyTiles.get(i).getX();
	            int ty = dirtyTiles.get(i).getY();
	            float dim = TextureBuffer.TILE_DIM * m_viewZoomFactor / m_mainBuffer.mZoomFactor;
	            float cx = m_viewOffset.getX() + dim * (0.5f + tx);
	            float cy = m_viewOffset.getY() + dim * (0.5f + ty);
	            float dist = Math.abs(cx - mSize.getWidth() / 2) + Math.abs(cy - mSize.getHeight() / 2);
	            if (dist < closestDistance) {
	            	updateX = tx;
	            	updateY = ty;
	                closestDistance = dist;
	            }
	        }

	        Log.d("BS", "Update " + updateX + ", " + updateY);
//	        if (!mNeedUpdate){
		        mUpdateX = updateX;
		        mUpdateY = updateY;
		        // Update the closest tile and bind as texture.dd
		        Canvas canvas = new Canvas(mContent);
		        Paint paint = new Paint();

		        if (mDS != null) {
		        	mDS.draw(canvas, mUpdateX, mUpdateY, paint);
		        }
		        
		        if (DEBUG) {
		        	paint.setColor(Color.rgb(0, 0, 0));
		        	paint.setStyle(Style.STROKE);
		        	canvas.drawRect(3, 3, TextureBuffer.TILE_DIM - 6, TextureBuffer.TILE_DIM - 6, paint);
		        	canvas.drawText(mUpdateX + ", " + mUpdateY, 4, 16, paint);
		        }
		       
		        mNeedBindTexture = true;
		        update();
//	        } else if (mNeedUpdate && (mUpdateX != updateX || mUpdateY != updateY)) {
//	        	Log.d("BS", "Update WRONG" + mUpdateX + ", " + mUpdateY);
////	        	throw new RuntimeException("Update WRONG" + mUpdateX + ", " + mUpdateY);
//	        }
	    }
	    
	    killUpdateTimer();
	}
	
	public void initializeGL()
	{
	    mGL.glDisable(GL11.GL_DEPTH_TEST);
	    mGL.glEnable(GL11.GL_TEXTURE_2D);
	    m_defaultTexture = bindTexture(TextureBuffer.createCheckerboardPattern(TextureBuffer.TILE_DIM));
	    
	    refreshBackingStore();
	}
	
	public void setDS(DataSource ds) {
		mDS = ds;
	}
	
	public void paintGL()
	{
	    mGL.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
	    mGL.glClear(GL11.GL_COLOR_BUFFER_BIT);

	    // For very fast zooming in and out, do not apply any expensive filter.
	    mGL.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
	    mGL.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

	    // Ensure we would have seamless transition between adjecent tiles.
	    mGL.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
	    mGL.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);

	    // This is background, i.e. all the outdated tiles while
	    // new primary ones are being prepared (e.g. after zooming).
	    if (!m_secondaryBuffer.isEmpty()) {
	        Rect backgroundRange = m_secondaryBuffer.visibleRange(m_viewOffset, m_viewZoomFactor, mSize);
	        m_secondaryBuffer.setViewModelMatrix(m_viewOffset, m_viewZoomFactor);

	        for (int x = 0; x < m_secondaryBuffer.width(); ++x) {
	            for (int y = 0; y < m_secondaryBuffer.height(); ++y) {
	                if (backgroundRange.contains(x, y))
	                    m_secondaryBuffer.draw(x, y, m_defaultTexture);
	                else
	                    m_secondaryBuffer.remove(x, y);
	            }
	        }
	    }

	    // Extend the update range with extra tiles in every direction, this is
	    // to anticipate panning and scrolling.
	    Rect updateRange = m_mainBuffer.visibleRange(m_viewOffset, m_viewZoomFactor, mSize);
	    updateRange.inflate(EXTRA_TILES, EXTRA_TILES);

	    // When zooming in/out, we have secondary textures as
	    // the background. Thus, do not overdraw the background
	    // with the checkerboard pattern (default texture).
	    int substitute = m_secondaryBuffer.isEmpty() ? m_defaultTexture : 0;

	    m_mainBuffer.setViewModelMatrix(m_viewOffset, m_viewZoomFactor);
	    boolean needsUpdate = false;
	    for (int x = 0; x < m_mainBuffer.width(); ++x) {
	        for (int y = 0; y < m_mainBuffer.height(); ++y) {
	            int texture = m_mainBuffer.at(x, y);
	            if (updateRange.contains(x, y)) {
	                m_mainBuffer.draw(x, y, substitute);
	                if (texture == 0)
	                    needsUpdate = true;
	            }

	            // Save GPU memory and throw out unneeded texture
	            if (texture != 0 && !updateRange.contains(x, y))
	                m_mainBuffer.remove(x, y);
	        }
	    }

	    if (!mNeedBindTexture) {
		    if (needsUpdate) {
		        scheduleUpdate();
		    } else {
		        // Every tile is up-to-date, thus discard the background.
		        if (!m_secondaryBuffer.isEmpty()) {
		            m_secondaryBuffer.clear();
		            update();
		        }
		    }
	
		    // Zooming means we need a fresh set of resolution-correct tiles.
		    if (m_viewZoomFactor != m_mainBuffer.mZoomFactor)
		        scheduleRefresh();
	    }
	}
	
	public void resizeGL(int width, int height)
	{
	    // Ensure that (0,0) is top left and (width - 1, height -1) is bottom right.
	    mGL.glViewport(0, 0, width, height);
	    
	    mGL.glMatrixMode(GL11.GL_PROJECTION);
	    mGL.glLoadIdentity();
	    mGL.glOrthof(0, width, height, 0, -1, 1); // ?glOrtho
	    mGL.glMatrixMode(GL11.GL_MODELVIEW);
	    mGL.glLoadIdentity();
	}
	
	public int bindTexture(Bitmap bitmap) {
		int [] textures = new int[1];
		mGL.glGenTextures(1, textures, 0);
			// ...and bind it to our array
		mGL.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		// create nearest filtered texture
		mGL.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		mGL.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		// Use Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		
		return textures[0];
	}
	
	public void update() {
		mView.requestRender();
	}
	
	public void mouseZoom(float zoomFactor, Point pos)
	{
	    float oldZoom = m_viewZoomFactor;
	    m_viewZoomFactor = Math.max(MinZoom, Math.min(zoomFactor, MaxZoom));

	    // We center the zooming relative to the mouse positions,
	    // hence the translation before and after the scaling.
	    PointF center = new PointF(pos.getX() - m_viewOffset.getX(), pos.getY() - m_viewOffset.getY());
	    center.setPoint(center.getX() * m_viewZoomFactor / oldZoom, center.getY() * m_viewZoomFactor / oldZoom);
	    m_viewOffset.setPoint(pos.getX() - center.getX(), pos.getY() - center.getY());

	    update();
	}
	
	public BSGLSurfaceView getView() {
		return mView;
	}
	
	public GL11 getGL() {
		return mGL;
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		if (mNeedBindTexture) {
			m_mainBuffer.replace(mUpdateX, mUpdateY, bindTexture(mContent));
		}
		
		// clear Screen and Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Reset the Modelview Matrix
		gl.glLoadIdentity();

		paintGL();

//		square1.draw(gl);
//		square2.offset(500, 500);
//		square2.draw(gl);
		
//		paintGL();
//		triangle.draw(gl);
		
		if (mNeedBindTexture) {
			mNeedBindTexture = false;
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}
		
		mSize.SetSize(width, height);

		resizeGL(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl1, EGLConfig config) {
		GL11 gl = (GL11) gl1;
        if (mGL == null) {
            mGL = gl;
        } else {
            // The GL Object has changed.
            Log.i(TAG, "GLObject has changed from " + mGL + " to " + gl);
            mGL = gl;
        }
	        
		// Load the texture for the square
		square1.loadGLTexture(gl, mView.getContext());
		square2.loadGLTexture(gl, mView.getContext());
//
//		gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping ( NEW )
//		gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
//		gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f); 	//Black Background
//		gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
//		gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
//		gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do
//
//		//Really Nice Perspective Calculations
//		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
//		
//		//---------------------------------------------------
////		mView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//        // Increase the priority of the render thread.
//        Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
//	       // Disable unused state.
//        gl.glEnable(GL11.GL_DITHER);
//        gl.glDisable(GL11.GL_LIGHTING);
//
//        // Set global state.
//        // gl.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
//
//        // Enable textures.
//        gl.glEnable(GL11.GL_TEXTURE_2D);
//        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
//
//        // Set up state for multitexture operations. Since multitexture is
//        // currently used
//        // only for layered crossfades the needed state can be factored out into
//        // one-time
//        // initialization. This section may need to be folded into drawMixed2D()
//        // if multitexture
//        // is used for other effects.
//
//        // Enable Vertex Arrays
//        gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
//        gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
//        gl.glClientActiveTexture(GL11.GL_TEXTURE1);
//        gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
//        gl.glClientActiveTexture(GL11.GL_TEXTURE0);
//
//        // Enable depth test.
//        gl.glEnable(GL11.GL_DEPTH_TEST);
//        gl.glDepthFunc(GL11.GL_LEQUAL);
//
//        // Set the blend function for premultiplied alpha.
//        gl.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        m_mainBuffer.mGL = gl;
        m_secondaryBuffer.mGL = gl;
        
        initializeGL();
	}
	
	public class UpdateTimerTask extends TimerTask {

		@Override
		public void run() {
			updateBackingStore();
		}
	}	
	
	public class RefreshTimerTask extends TimerTask {

		@Override
		public void run() {
			refreshBackingStore();
		}
	}
}

	