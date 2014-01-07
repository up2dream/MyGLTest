package com.example.mygltest.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

import cn.wps.moffice.presentation.sal.drawing.PointF;

public class GLCanvas {

	private GL11 mGL;
	
    public float mZoomFactor;

	
	private FloatBuffer vertexBuffer;	// buffer holding the vertices

	private float vertices[] = {
			0.0f, 0.0f,  0.0f,		// V1 - top left
			0.0f, 1.0f,  0.0f,		// V2 - bottom left
			1.0f, 0.0f,  0.0f,		// V3 - top right
			1.0f, 1.0f,  0.0f		// V4 - bottom right
	};

	private FloatBuffer textureBuffer;	// buffer holding the texture coordinates
	private float texture[] = {
			// Mapping coordinates for the vertices
			0.0f, 0.0f,		// top left	    (V1)
			0.0f, 1.0f,		// bottom left	(V2)
			1.0f, 0.0f,		// top right	(V3)
			1.0f, 1.0f,		// bottom right	(V4)
	};

	
	public GLCanvas(GL11 gl) {
		mGL = gl;
		
		initialize();
	}
	
	public GL11 getGL() {
		return mGL;
	}
	
	private void initialize() {
		mGL.glEnable(GL11.GL_BLEND);
		mGL.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	    mGL.glDisable(GL11.GL_DEPTH_TEST);
	    mGL.glEnable(GL11.GL_TEXTURE_2D);
	    
	    mZoomFactor = 1f;
	    
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuffer.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuffer.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
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
	    mGL.glClear(GL11.GL_COLOR_BUFFER_BIT);

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
	    
	    vertexBuffer.put(0, x);
	    vertexBuffer.put(1, y);
	    vertexBuffer.put(3, x);
	    vertexBuffer.put(4, y + h);
	    vertexBuffer.put(6, x + w);
	    vertexBuffer.put(7, y);
	    vertexBuffer.put(9, x + w);
	    vertexBuffer.put(10, y + h);
	    
	    // Point to our buffers
	    mGL.glEnableClientState(GL11.GL_VERTEX_ARRAY);
	    mGL.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

 		// Set the face rotation
	    mGL.glFrontFace(GL11.GL_CW);

 		// Point to our vertex buffer
 		vertexBuffer.position(0);
 		mGL.glVertexPointer(3, GL11.GL_FLOAT, 0, vertexBuffer);
 		textureBuffer.position(0);
 		mGL.glTexCoordPointer(2, GL11.GL_FLOAT, 0, textureBuffer);

 		// Draw the vertices as triangle strip
 		mGL.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

 		//Disable the client state before leaving
 		mGL.glDisableClientState(GL11.GL_VERTEX_ARRAY);
 		mGL.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
 		mGL.glDisable(GL11.GL_TEXTURE_2D);
	}
	
	public void destroy() {
		
	}
}
