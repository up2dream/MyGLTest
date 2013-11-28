package com.example.mygltest.bs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import cn.wps.moffice.presentation.sal.drawing.PointF;
import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.Size;


public class TextureBuffer
{
	public static final int TILE_DIM = 512;
	
    private TexturesArray mTextures = new TexturesArray();
    private Size mBufferSize = new Size();
    public GL11 mGL;
    public float mZoomFactor;
    private int[] mTexture_array_used_in_remove_method = new int [1];
    
	private FloatBuffer vertexBuffer;	// buffer holding the vertices

	private float vertices[] = {
			0.0f, 0.0f,  0.0f,		// V1 - top left
			0.0f, 1.0f,  0.0f,		// V2 - bottom left
			1.0f, 0.0f,  0.0f,		// V3 - top right
			1.0f, 1.0f,  0.0f			// V4 - bottom right
	};

	private FloatBuffer textureBuffer;	// buffer holding the texture coordinates
	private float texture[] = {
			// Mapping coordinates for the vertices
			0.0f, 0.0f,		// top left	    (V1)
			0.0f, 1.0f,		// bottom left	(V2)
			1.0f, 0.0f,		// top right	(V3)
			1.0f, 1.0f,		// bottom right	(V4)
	};
	
    public static Bitmap createCheckerboardPattern(int dim)
    {
    	Bitmap pattern = Bitmap.createBitmap(dim, dim, Config.ARGB_8888);
        int color1 = Color.rgb(240, 240, 240);
        int color2 = Color.rgb(255, 255, 255);
        if (BSGLRenderer.DEBUG) {
        	color2 = Color.rgb(128, 128, 128);
        }
        for (int y = 0; y < TILE_DIM; ++y)
            for (int x = 0; x < TILE_DIM; ++x)
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
    
    public boolean isEmpty() {
    	return mTextures.isEmpty();
    }
    
    public void copyTo(TextureBuffer textureBuffer) {
    	
    }
    
    public void clear() {
    	if (!mTextures.isEmpty()) {
    		int[] buffer = mTextures.buffer();
			mGL.glDeleteTextures(buffer.length, buffer, 0);
	        mTextures.clear();
	    }
	    mBufferSize.SetSize(0, 0);
    }

    public int at(int x, int y) {
    	int index = y * mBufferSize.getWidth() + x;
	    if (index < 0 || index >= mTextures.count())
	        return 0;
	    return mTextures.at(index);
    }
    
    public void replace(int x, int y, int texture) {
    	int index = y * mBufferSize.getWidth() + x;
        if (index < 0 || index >= mTextures.count())
            return;
        mTextures.set(index, texture);
    }
    
    public void remove(int x, int y) {
    	int index = y * mBufferSize.getWidth() + x;
        if (index < 0 || index >= mTextures.count())
            return;
        if (mTextures.at(index) != 0) {
        	mTexture_array_used_in_remove_method[0] = mTextures.at(index);
            mGL.glDeleteTextures(1, mTexture_array_used_in_remove_method, 0);
            mTextures.set(index, 0);
        }
    }

    public int width() {
    	return mBufferSize.getWidth();
    }
    
    public int height() {
    	return mBufferSize.getHeight();
    }
    
    public void resize(int w, int h) {
    	mBufferSize.SetSize(w, h);
        mTextures.resize(w * h);
        mTextures.fill(0);
    }

    public void draw(int x, int y, int substitute) {
    	int index = y * mBufferSize.getWidth() + x;
	    if (index < 0 || index >= mTextures.count())
	        return;

	    int texture = mTextures.at(index);
	    if (texture == 0)
	        texture = substitute;
	    if (texture == 0)
	        return;

	    float tx = x * TILE_DIM;
	    float ty = y * TILE_DIM;

	    mGL.glEnable(GL11.GL_TEXTURE_2D);
	    mGL.glBindTexture(GL11.GL_TEXTURE_2D, texture);
	    
	    vertexBuffer.put(0, tx);
	    vertexBuffer.put(1, ty);
	    vertexBuffer.put(3, tx);
	    vertexBuffer.put(4, ty + TextureBuffer.TILE_DIM);
	    vertexBuffer.put(6, tx + TextureBuffer.TILE_DIM);
	    vertexBuffer.put(7, ty);
	    vertexBuffer.put(9, tx + TextureBuffer.TILE_DIM);
	    vertexBuffer.put(10, ty + TextureBuffer.TILE_DIM);
	    
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
    
    public void setViewModelMatrix(final  PointF viewOffset, float viewZoomFactor) {
    	mGL.glMatrixMode(GL11.GL_MODELVIEW);
    	mGL.glLoadIdentity();
    	mGL.glTranslatef(viewOffset.getX(), viewOffset.getY(), 0);
    	mGL.glScalef(viewZoomFactor / mZoomFactor, viewZoomFactor / mZoomFactor, 1f);
    }
    
    public Rect visibleRange(final PointF viewOffset, float viewZoomFactor, final Size viewSize) {
    	float dim = TILE_DIM * viewZoomFactor / mZoomFactor;

        int tx1 = (int) (-viewOffset.getX() / dim);
        int tx2 = (int) ((viewSize.getWidth() - viewOffset.getX()) / dim);
        int ty1 = (int) (-viewOffset.getY() / dim);
        int ty2 = (int) ((viewSize.getHeight() - viewOffset.getY()) / dim);

        tx1 = Math.max(0, tx1);
        tx2 = Math.min(mBufferSize.getWidth() - 1, tx2);
        ty1 = Math.max(0, ty1);
        ty2 = Math.min(mBufferSize.getHeight() - 1, ty2);

        return Rect.fromLTRB(tx1, ty1, tx2, ty2);
    }

};