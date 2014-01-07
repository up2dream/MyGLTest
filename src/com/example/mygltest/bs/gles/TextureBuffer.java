package com.example.mygltest.bs.gles;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.opengl.GLUtils;
import cn.wps.moffice.presentation.sal.base.Triple;

import com.example.mygltest.bs.BSGLSurfaceView;
import com.example.mygltest.bs.TiledBackingStore;

public class TextureBuffer {
	private BSGLSurfaceView mView;
    private int[] mTexture_array_used_in_remove_method = new int [1];
    private Set<Integer> mTextures = new HashSet<Integer>();
    private BlockingQueue<Triple<Integer, Bitmap, BlockingQueue<Integer>>> mTexturesToBeBinding = new LinkedBlockingQueue<Triple<Integer, Bitmap, BlockingQueue<Integer>>>();
    private int mCheckerTextureID = 0;
	
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
    
    public TextureBuffer(BSGLSurfaceView view) {
    	mView = view;
    }
    
	public int bindTexture(GL11 gl, Bitmap bitmap) {
		return bindTexture(gl, 0, bitmap);
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
	
	public int writeToTextureSync(int textureID, Bitmap bitmap) {
		ArrayBlockingQueue<Integer> mSync = new ArrayBlockingQueue<Integer>(1);
		mTexturesToBeBinding.add(new Triple<Integer, Bitmap, BlockingQueue<Integer>>(textureID, bitmap, mSync));
		mView.update();
		
		try {
			return mSync.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public void updateTexture(GL11 gl) {
		Triple<Integer, Bitmap, BlockingQueue<Integer>> triple = mTexturesToBeBinding.poll();
		if (triple == null)
			return;
		
		triple.third.add(bindTexture(gl, triple.first, triple.second));
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
    
    public int getCheckerTextureID(GL11 gl, int w, int h) {
    	if (mCheckerTextureID == 0) {
    		Bitmap checker = createCheckerboardPattern((int)w, (int)h);
    		mCheckerTextureID = bindTexture(gl, checker);
    		checker.recycle();
    	}
    	
    	return mCheckerTextureID;
    }
};