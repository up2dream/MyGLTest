package com.example.mygltest.bs;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import cn.wps.moffice.presentation.sal.drawing.Size;

import com.example.mygltest.Square;
import com.example.mygltest.Triangle;
import com.example.mygltest.bs.gles.TextureBuffer;

public class BSGLSurfaceView extends GLSurfaceView implements Renderer {

	public static final boolean DEBUG = true;

	private static final String TAG = DEBUG ? BSGLSurfaceView.class.getSimpleName() : null;
	
	private TextureBuffer mTextureBuffer;

	private GL11 mGL;
	private Size mSize = new Size();
	
	private Square square1 = new Square();
	private Square square2 = new Square();
	private Triangle triangle = new Triangle();

	/** Constructor to set the handed over context */
	public BSGLSurfaceView(Context context) {
		super(context);
		
	    mTextureBuffer = new TextureBuffer();
	}
	
	public void initializeGL()
	{
	    mGL.glDisable(GL11.GL_DEPTH_TEST);
	    mGL.glEnable(GL11.GL_TEXTURE_2D);
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

	    mTextureBuffer.drawChecker(mGL, 100, 100, 1500, 1000);
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
	
	public void update() {
		requestRender();
	}
	
	public GL11 getGL() {
		return mGL;
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
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
		square1.loadGLTexture(gl, getContext());
		square2.loadGLTexture(gl, getContext());
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
        
        initializeGL();
	}
}

	