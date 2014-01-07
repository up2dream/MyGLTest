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
import com.example.mygltest.gl.GLCanvas;

public class BSGLSurfaceView extends GLSurfaceView implements Renderer {

	public static final boolean DEBUG = true;

	private static final String TAG = DEBUG ? BSGLSurfaceView.class.getSimpleName() : null;
	
	private TextureBuffer mTextureBuffer;

	private GLCanvas mCanvas;
	private Size mSize = new Size();
	
	private Square square1 = new Square();
	private Square square2 = new Square();
	private Triangle triangle = new Triangle();
	
	private RenderLayerPh_GL mLayer;

	/** Constructor to set the handed over context */
	public BSGLSurfaceView(Context context) {
		super(context);
		
	    mTextureBuffer = new TextureBuffer(this);
	    
	    
	    mLayer = new RenderLayerPh_GL(mTextureBuffer);
	}
	
	public TextureBuffer getTextureBuffer() {
		return mTextureBuffer;
	}
	
	public RenderLayerPh_GL getRenderLayer() {
		return mLayer;
	}
	
	public void paintGL() {
		mCanvas.drawColor(1.0f, 1.0f, 1.0f, 1.0f);
	    
	    mCanvas.drawTexture(mTextureBuffer.getCheckerTextureID(mCanvas.getGL(), 1500, 1000), 100, 100, 1500, 1000);

	    mLayer.paint(mCanvas);
	}
	
	public void update() {
		requestRender();
	}
	
	public GLCanvas getCanvas() {
		return mCanvas;
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		// clear Screen and Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Reset the Modelview Matrix
		gl.glLoadIdentity();
		
		mTextureBuffer.updateTexture((GL11)gl);

		mCanvas.preset();
		
		paintGL();

//		square1.draw(gl);
//		square2.offset(500, 500);
//		square2.draw(gl);
		
//		paintGL();
//		triangle.draw(gl);
		
//	    GLPaint paint = new GLPaint();
//	    paint.setColor(Color.GREEN);
//	    mGLCanvas.drawLine(0, 0, 300, 300, paint);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}
		
		mSize.SetSize(width, height);

		mCanvas.resize(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl1, EGLConfig config) {
		GL11 gl = (GL11) gl1;
        if (mCanvas == null) {
            mCanvas = new GLCanvas(gl);
        } else {
            // The GL Object has changed.
            Log.i(TAG, "GLObject has changed from " + gl1 + " to " + gl);
            mCanvas.destroy();
        }
	        
		// Load the texture for the square
		square1.loadGLTexture(gl, getContext());
		square2.loadGLTexture(gl, getContext());
	}
}

	