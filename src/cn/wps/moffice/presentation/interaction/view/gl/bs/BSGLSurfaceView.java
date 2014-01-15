package cn.wps.moffice.presentation.interaction.view.gl.bs;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import cn.wps.moffice.presentation.interaction.view.gl.GLCanvas;
import cn.wps.moffice.presentation.interaction.view.gl.Square;
import cn.wps.moffice.presentation.interaction.view.gl.Triangle;
import cn.wps.moffice.presentation.interaction.view.gl.bs.gles.TextureBuffer;
import cn.wps.moffice.presentation.sal.drawing.Size;


public class BSGLSurfaceView extends GLSurfaceView implements Renderer {

	public static final boolean DEBUG = true;

	private static final String TAG = DEBUG ? BSGLSurfaceView.class.getSimpleName() : null;
	
	private TextureBuffer mTextureBuffer;

	private GLCanvas mCanvas;
	private Size mSize = new Size();
	
	private Square square1 = new Square();
	private Square square2 = new Square();
	private Triangle triangle = new Triangle();
	
	private RenderLayerPh_GL mLayer1;
	private RenderLayerPh_GL mLayer2;

	/** Constructor to set the handed over context */
	public BSGLSurfaceView(Context context) {
		super(context);
		
		setRenderer(this);
		
	    mTextureBuffer = new TextureBuffer(this);
	    
	    
	    mLayer1 = new RenderLayerPh_GL(mTextureBuffer);
	    mLayer2 = new RenderLayerPh_GL(mTextureBuffer);
	}
	
	public TextureBuffer getTextureBuffer() {
		return mTextureBuffer;
	}
	
	public RenderLayerPh_GL getRenderLayer1() {
		return mLayer1;
	}
	
	public void paintGL() {
		mCanvas.drawColor(1.0f, 1.0f, 1.0f, 1.0f);
	    
//	    mCanvas.drawTexture(mTextureBuffer.getCheckerTextureID(mCanvas.getGL(), 1500, 1000), 100, 100, 1500, 1000);

	    mLayer1.paint(mCanvas);
	    mLayer2.paint(mCanvas);
	}
	
	public void update() {
		requestRender();
	}
	
	public GLCanvas getCanvas() {
		return mCanvas;
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		mCanvas.preset();

		mTextureBuffer.updateTexture((GL11)gl);

		paintGL();

//		square1.draw(gl);
//		square2.offset(500, 500);
//		square2.draw(gl);
		
//		paintGL();
//		triangle.draw(gl);
		mCanvas.setFillColor(0, 1, 0, 1);
		mCanvas.setLineColor(0, 0, 1, 1);
		mCanvas.setLineWidth(2);
		mCanvas.drawRect(20, 20, 100, 100, true);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}
		
		mSize.SetSize(width, height);

		mCanvas.resize(width, height);
		
		update();
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

	