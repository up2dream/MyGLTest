package com.example.mygltest.bs.gles;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import com.example.mygltest.bs.TiledBackingStore;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.opengl.GLUtils;
import cn.wps.moffice.presentation.sal.drawing.PointF;

public class TextureBuffer
{
    public float mZoomFactor;
    private int[] mTexture_array_used_in_remove_method = new int [1];
    private Set<Integer> mTextures = new HashSet<Integer>();
    private int mCheckerTextureID = 0;
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
	
    private Bitmap createCheckerboardPattern(int dimW, int dimH) {
    	Bitmap pattern = Bitmap.createBitmap(dimW, dimH, Config.ARGB_8888);
        int color1 = Color.rgb(240, 240, 240);
        int color2 = Color.rgb(255, 255, 255);
        if (TiledBackingStore.DEBUG) {
        	color2 = Color.rgb(128, 128, 128);
        }

        for (int y = 0; y < dimH; ++y)
            for (int x = 0; x < dimW; ++x)
                pattern.setPixel(x, y, (((x >> 4) + (y >> 4)) & 1) != 0 ? color1 : color2);
        return pattern;
    }
    
    public TextureBuffer() {
    	mZoomFactor = 0.1f;
    	
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
    
	public int bindTexture(GL11 gl, Bitmap bitmap) {
		int [] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		// ...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		// create nearest filtered texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		// Use Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		
		mTextures.add(textures[0]);
		
		return textures[0];
	}
	
	public int bindTexture(GL11 gl, int textureID, Bitmap bitmap) {
		int [] textures = new int[1];
		if (textureID == 0)
			gl.glGenTextures(1, textures, 0);
		else
			textures[0] = textureID;
		
		// ...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		// create nearest filtered texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		// Use Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		
		mTextures.add(textures[0]);
		
		return textures[0];
	}
	
    public boolean isEmpty() {
    	return mTextures.isEmpty();
    }
    
    public void clear(GL11 gl) {
    	int [] buffer = new int[mTextures.size()];
    	Iterator<Integer> it = mTextures.iterator();
    	int i = 0;
    	while (it.hasNext()) {
    		buffer[i++] = it.next();
    	}
    	
		gl.glDeleteTextures(buffer.length, buffer, 0);
		
		mTextures.clear();
    }

    public void remove(GL11 gl, int textureID) {
    	mTexture_array_used_in_remove_method[0] = textureID;
        gl.glDeleteTextures(1, mTexture_array_used_in_remove_method, 0);
    }

    public void draw(GL11 gl, int textureID, float x, float y, float w, float h) {
    	gl.glEnable(GL11.GL_TEXTURE_2D);
    	gl.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	    
	    vertexBuffer.put(0, x);
	    vertexBuffer.put(1, y);
	    vertexBuffer.put(3, x);
	    vertexBuffer.put(4, y + h);
	    vertexBuffer.put(6, x + w);
	    vertexBuffer.put(7, y);
	    vertexBuffer.put(9, x + w);
	    vertexBuffer.put(10, y + h);
	    
	    // Point to our buffers
	    gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
	    gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

 		// Set the face rotation
	    gl.glFrontFace(GL11.GL_CW);

 		// Point to our vertex buffer
 		vertexBuffer.position(0);
 		gl.glVertexPointer(3, GL11.GL_FLOAT, 0, vertexBuffer);
 		textureBuffer.position(0);
 		gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, textureBuffer);

 		// Draw the vertices as triangle strip
 		gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

 		//Disable the client state before leaving
 		gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
 		gl.glDisable(GL11.GL_TEXTURE_2D);
    }
    
    public void drawChecker(GL11 gl, float x, float y, float w, float h) {
    	if (mCheckerTextureID == 0) {
    		Bitmap checker = createCheckerboardPattern((int)w, (int)h);
    		mCheckerTextureID = bindTexture(gl, checker);
    		checker.recycle();
    	}
    	
    	draw(gl, mCheckerTextureID, x, y, w, h);
    }
    
    public void setViewModelMatrix(GL11 gl, final  PointF viewOffset, float viewZoomFactor) {
    	gl.glMatrixMode(GL11.GL_MODELVIEW);
    	gl.glLoadIdentity();
    	gl.glTranslatef(viewOffset.getX(), viewOffset.getY(), 0);
    	gl.glScalef(viewZoomFactor / mZoomFactor, viewZoomFactor / mZoomFactor, 1f);
    }
};