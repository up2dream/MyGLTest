package cn.wps.moffice.presentation.interaction.view.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import cn.wps.moffice.presentation.sal.drawing.PointF;

public class GLCanvas {

	private GL11 mGL;
	
    public float mZoomFactor;
    private float[] mFillColor = { 1f, 1f, 1f, 1f };
    private float[] mLineColor = { 0f, 0f, 0f, 1f };
    private float mLineWidth = 1f;

	
	private FloatBuffer mVertexBuffer;	// buffer holding the vertices

	private float mVertices[] = {
			0.0f, 0.0f,  0.0f,		// V1 - top left
			0.0f, 1.0f,  0.0f,		// V2 - bottom left
			1.0f, 0.0f,  0.0f,		// V3 - top right
			1.0f, 1.0f,  0.0f		// V4 - bottom right
	};

	private FloatBuffer mTextureCoordBuffer;	// buffer holding the texture coordinates
	private float mTextureCoord[] = {
			// Mapping coordinates for the vertices
			0.0f, 0.0f,		// top left	    (V1)
			0.0f, 1.0f,		// bottom left	(V2)
			1.0f, 0.0f,		// top right	(V3)
			1.0f, 1.0f,		// bottom right	(V4)
	};

	private FloatBuffer mColorBuffer;	// buffer holding the vertices
	float[] mColors = {
			 1f, 0f, 0f, 1f, // vertex 0 color
			 1f, 0f, 0f, 1f, // vertex 1 color
			 1f, 0f, 0f, 1f, // vertex 2 color
			 1f, 0f, 0f, 1f, // vertex 3 color
	};

	
	public GLCanvas(GL11 gl) {
		mGL = gl;
		
		initialize();
	}
	
	private void initialize() {
		mGL.glEnable(GL11.GL_BLEND);
		mGL.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	    mGL.glDisable(GL11.GL_DEPTH_TEST);
	    mGL.glEnable(GL11.GL_TEXTURE_2D);
	    
	    mZoomFactor = 1f;
	    
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mVertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		mVertexBuffer = byteBuffer.asFloatBuffer();
		mVertexBuffer.put(mVertices);
		mVertexBuffer.position(0);

		byteBuffer = ByteBuffer.allocateDirect(mTextureCoord.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		mTextureCoordBuffer = byteBuffer.asFloatBuffer();
		mTextureCoordBuffer.put(mTextureCoord);
		mTextureCoordBuffer.position(0);
		
		byteBuffer = ByteBuffer.allocateDirect(mColors.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		mColorBuffer = byteBuffer.asFloatBuffer();
		mColorBuffer.put(mColors);
		mColorBuffer.position(0);
	}

	public GL11 getGL() {
		return mGL;
	}
	
	public float[] getFillColor() {
		return mFillColor;
	}

	public void setFillColor(float red, float green, float blue, float alpha) {
		mFillColor[0] = red;
		mFillColor[1] = green;
		mFillColor[2] = blue;
		mFillColor[3] = alpha;
	}
	
	public float[] getLineColor() {
		return mLineColor;
	}

	public void setLineColor(float red, float green, float blue, float alpha) {
		mLineColor[0] = red;
		mLineColor[1] = green;
		mLineColor[2] = blue;
		mLineColor[3] = alpha;
	}
	
	public float getLineWidth() {
		return mLineWidth;
	}
	
	public void setLineWidth(float lineWidth) {
		mLineWidth = lineWidth;
	}
	
	public void resize(int width, int height) {
	    // Ensure that (0,0) is top left and (width - 1, height -1) is bottom right.
	    mGL.glViewport(0, 0, width, height);
	    
	    mGL.glMatrixMode(GL11.GL_PROJECTION);
	    mGL.glLoadIdentity();
	    mGL.glOrthof(0, width, height, 0, -1, 1); // ?glOrtho
	    mGL.glMatrixMode(GL11.GL_MODELVIEW);
	    mGL.glLoadIdentity();
	}
	
	public void preset() {
		// clear Screen and Depth Buffer
		mGL.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Reset the Modelview Matrix
		mGL.glLoadIdentity();

	    // For very fast zooming in and out, do not apply any expensive filter.
	    mGL.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
	    mGL.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

	    // Ensure we would have seamless transition between adjecent tiles.
	    mGL.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
	    mGL.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
	}
	
    public void setViewModelMatrix(GL11 gl, final  PointF viewOffset, float viewZoomFactor) {
    	gl.glMatrixMode(GL11.GL_MODELVIEW);
    	gl.glLoadIdentity();
    	gl.glTranslatef(viewOffset.getX(), viewOffset.getY(), 0);
    	gl.glScalef(viewZoomFactor / mZoomFactor, viewZoomFactor / mZoomFactor, 1f);
    }
	
    public void drawColor(float red, float green, float blue, float alpha) {
    	mGL.glClearColor(red, green , blue, alpha);	
    }
    
	public void drawTexture(int textureID, float x, float y, float w, float h) {
		mGL.glEnable(GL11.GL_TEXTURE_2D);
		mGL.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	    
	    mVertexBuffer.put(0, x);
	    mVertexBuffer.put(1, y);
	    mVertexBuffer.put(3, x);
	    mVertexBuffer.put(4, y + h);
	    mVertexBuffer.put(6, x + w);
	    mVertexBuffer.put(7, y);
	    mVertexBuffer.put(9, x + w);
	    mVertexBuffer.put(10, y + h);
	    
	    // Point to our buffers
	    mGL.glEnableClientState(GL11.GL_VERTEX_ARRAY);
	    mGL.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

 		// Set the face rotation
	    mGL.glFrontFace(GL11.GL_CW);

 		// Point to our vertex buffer
 		mVertexBuffer.position(0);
 		mGL.glVertexPointer(3, GL11.GL_FLOAT, 0, mVertexBuffer);
 		mTextureCoordBuffer.position(0);
 		mGL.glTexCoordPointer(2, GL11.GL_FLOAT, 0, mTextureCoordBuffer);

 		// Draw the vertices as triangle strip
 		mGL.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, mVertices.length / 3);

 		//Disable the client state before leaving
 		mGL.glDisableClientState(GL11.GL_VERTEX_ARRAY);
 		mGL.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
 		mGL.glDisable(GL11.GL_TEXTURE_2D);
	}
	
	public void drawRect(float x, float y, float w, float h, boolean isSolid) {
		if (isSolid)
			drawRectInner(x, y, w, h);
		
		drawRectBorder(x, y, w, h);
	}
	
	public void drawRectInner(float x, float y, float w, float h) {
	    mVertexBuffer.put(0, x);
	    mVertexBuffer.put(1, y);
	    mVertexBuffer.put(3, x);
	    mVertexBuffer.put(4, y + h);
	    mVertexBuffer.put(6, x + w);
	    mVertexBuffer.put(7, y);
	    mVertexBuffer.put(9, x + w);
	    mVertexBuffer.put(10, y + h);
	    
	    mColorBuffer.put(mFillColor).put(mFillColor).put(mFillColor).put(mFillColor);
	    
	    // Point to our buffers
	    mGL.glEnableClientState(GL11.GL_VERTEX_ARRAY);
	    mGL.glEnableClientState(GL11.GL_COLOR_ARRAY);

 		// Set the face rotation
	    mGL.glFrontFace(GL11.GL_CW);

 		// Point to our vertex buffer
 		mVertexBuffer.position(0);
 		mGL.glVertexPointer(3, GL11.GL_FLOAT, 0, mVertexBuffer);
 		mColorBuffer.position(0);
 		mGL.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

 		// Draw the vertices as triangle strip
 		mGL.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, mVertices.length / 3);

 		//Disable the client state before leaving
 		mGL.glDisableClientState(GL11.GL_VERTEX_ARRAY);
 		mGL.glDisableClientState(GL11.GL_COLOR_ARRAY);
	}
	
	public void drawRectBorder(float x, float y, float w, float h) {
	    mVertexBuffer.put(0, x);
	    mVertexBuffer.put(1, y);
	    mVertexBuffer.put(3, x);
	    mVertexBuffer.put(4, y + h);
	    mVertexBuffer.put(6, x + w);
	    mVertexBuffer.put(7, y + h);
	    mVertexBuffer.put(9, x + w);
	    mVertexBuffer.put(10, y);
	    
	    mColorBuffer.put(mLineColor).put(mLineColor).put(mLineColor).put(mLineColor);
	    
	    // Point to our buffers
	    mGL.glEnableClientState(GL11.GL_VERTEX_ARRAY);
	    mGL.glEnableClientState(GL11.GL_COLOR_ARRAY);

 		// Set the face rotation
	    mGL.glFrontFace(GL11.GL_CW);

 		// Point to our vertex buffer
 		mVertexBuffer.position(0);
 		mGL.glVertexPointer(3, GL11.GL_FLOAT, 0, mVertexBuffer);
 		mColorBuffer.position(0);
 		mGL.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
 		mGL.glLineWidth(mLineWidth);

 		// Draw the vertices as line loop
 		mGL.glDrawArrays(GL11.GL_LINE_LOOP, 0, mVertices.length / 3);

 		//Disable the client state before leaving
 		mGL.glDisableClientState(GL11.GL_VERTEX_ARRAY);
 		mGL.glDisableClientState(GL11.GL_COLOR_ARRAY);
	}
	
	public void destroy() {
		
	}
}
