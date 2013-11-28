package com.example.mygltest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.example.mygltest.bs.TextureBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;

public class Square {

	static private FloatBuffer vertexBuffer;	// buffer holding the vertices

	private float vertices[] = {
			0.0f, 0.0f,  0.0f,		// V1 - top left
			0.0f,  800.0f,  0.0f,		// V2 - bottom left
			800.0f, 0.0f,  0.0f,		// V3 - top right
			800.0f,  800.0f,  0.0f			// V4 - bottom right
	};

	static private FloatBuffer textureBuffer;	// buffer holding the texture coordinates
	private float texture[] = {
			// Mapping coordinates for the vertices
			0.0f, 0.0f,		// top left	    (V1)
			0.0f, 1.0f,		// bottom left	(V2)
			1.0f, 0.0f,		// top right	(V3)
			1.0f, 1.0f,		// bottom right	(V4)
	};
	
	/** The texture pointer */
	private int[] textures = new int[300];

	public void loadGLTexture(GL10 gl, Context context) {
		// loading texture
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.penguins);
//		bitmap = Bitmap.createBitmap(bitmap, 0, 0, 512, 512);
		// generate one texture pointer
		gl.glGenTextures(1, textures, 0);
		long timeStamp_t = System.currentTimeMillis();
		for (int i=0; i<1; ++i) {
			// ...and bind it to our array
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
	
			// create nearest filtered texture
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	
			// Use Android GLUtils to specify a two-dimensional texture image from our bitmap
			long timeStamp = System.currentTimeMillis();
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			Log.d("perf", "time (" + bitmap.getWidth() + ", " + bitmap.getHeight() + ") " + (System.currentTimeMillis() - timeStamp) + " ms");
		}
		Log.d("perf", "time toal " + (System.currentTimeMillis() - timeStamp_t) + " ms");
		// Clean up
		bitmap.recycle();
	}
	
	public Square() {
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

	/** The draw method for the square with the GL context */
	public void draw(GL10 gl) {
		gl.glEnable(gl.GL_TEXTURE_2D);
		// bind the previously generated texture
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		// Point to our buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		// Set the face rotation
		gl.glFrontFace(GL10.GL_CW);

		// Point to our vertex buffer
		vertexBuffer.position(0);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		// Draw the vertices as triangle strip
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(gl.GL_TEXTURE_2D);
	}
	
	public void offset(int dx, int dy) {
	    vertexBuffer.put(0, vertexBuffer.get(0) + dx);
	    vertexBuffer.put(1, vertexBuffer.get(1) + dy);
	    vertexBuffer.put(3, vertexBuffer.get(3) + dx);
	    vertexBuffer.put(4, vertexBuffer.get(4) + dy);
	    vertexBuffer.put(6, vertexBuffer.get(6) + dx);
	    vertexBuffer.put(7, vertexBuffer.get(7) + dy);
	    vertexBuffer.put(9, vertexBuffer.get(9) + dx);
	    vertexBuffer.put(10, vertexBuffer.get(10) + dy);
	}
}
