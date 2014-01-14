package com.example.mygltest.bs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;
import cn.wps.moffice.presentation.sal.drawing.Point;
import cn.wps.moffice.presentation.sal.drawing.PointF;
import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.RectF;
import cn.wps.moffice.presentation.sal.drawing.Size;

import com.example.mygltest.bs.BSTaskTimer.BSTimerTask;
import com.example.mygltest.bs.gles.TextureBuffer;
import com.example.mygltest.gl.GLCanvas;

public class TiledBackingStore {
	
	public static final boolean DEBUG = true;
	private static final int DEF_TILE_DIM = 512;
    private static final long TILE_CREATION_DELAY_MS = 10;
	
	private TiledBackingStoreClient mClient;
	private ITiledBackingStoreBackend mBackend;
	private	TextureBuffer mTextureBuffer;
    private TileBuffer mMainTiles;
    private TileBuffer mScaleBufferTiles;

    private Size mTileSize = new Size(DEF_TILE_DIM, DEF_TILE_DIM);
    private float mCoverAreaMultiplier = 2.0f;

    private PointF mTrajectoryVector = new PointF();
    private PointF mPendingTrajectoryVector = new PointF();
    private Rect mVisibleRect = new Rect();

    private Rect mCoverRect = new Rect();
    private Rect mKeepRect = new Rect();
    private Rect mRect = new Rect();

    private float mContentsScale = 1.0f;
    private float mPendingScale = 0;

    private boolean mCommitTileUpdatesOnIdleEventLoop = true;
    private boolean mContentsFrozen = false;
    private boolean mSupportsAlpha = false;
    private boolean mPendingTileCreation = false;
    private boolean mUpdateFlowBreaked = false;
    
    private BSTaskTimer mUpdateTimer = new BSTaskTimer("TBSUpdate");
    
	public TiledBackingStore(TiledBackingStoreClient client, ITiledBackingStoreBackend backend, TextureBuffer textureBuffer) {
		mClient = client;
		mBackend = backend;
		mTextureBuffer = textureBuffer;
		mMainTiles = new TileBuffer(mTextureBuffer, mContentsScale);
		mScaleBufferTiles = new TileBuffer(mTextureBuffer, mContentsScale);
	}

	public TiledBackingStoreClient getClient() {
    	return mClient; 
	}
	
	public TextureBuffer getTextureBuffer() {
		return mTextureBuffer;
	}

	public ITiledBackingStoreBackend getBackend() {
		return mBackend;
	}
	
    // Used when class methods cannot be called asynchronously by client.
    // Updates of tiles are committed as soon as all the events in event queue have been processed.
	public void setCommitTileUpdatesOnIdleEventLoop(boolean enable) {
    	mCommitTileUpdatesOnIdleEventLoop = enable; 
	}

	public void setTrajectoryVector(final PointF trajectoryVector) {
		if (trajectoryVector == null)
			return;
		
		trajectoryVector.copyTo(mPendingTrajectoryVector);
		mPendingTrajectoryVector.normalize();
    }
    
	public void coverWithTilesIfNeeded(BSTimerTask task) {
		Rect visibleRect = visibleRect();
		Rect rect = mapFromContents(mClient.tbsGetContentsRect());

		boolean didChange = !mTrajectoryVector.equals(mPendingTrajectoryVector) || !mVisibleRect.equals(visibleRect) || !mRect.equals(rect);
		if (didChange || mPendingTileCreation)
			createTiles(task);
    }

	public float getContentsScale() {
		return mContentsScale; 
	}
	
	public void setContentsScale(float scale) {
		if (mPendingScale == mContentsScale) {
	        mPendingScale = 0;
	        return;
	    }
		
	    mPendingScale = scale;
	    if (mContentsFrozen)
	        return;
	    
	    startCommitScaleChangeTask();
	}

	public boolean isContentsFrozen() {
		return mContentsFrozen; 
	}
	
	public void setContentsFrozen(boolean freeze) {
		if (mContentsFrozen == freeze)
	        return;

	    mContentsFrozen = freeze;

	    // Restart the timers. There might be pending invalidations that
	    // were not painted or created because tiles are not created or
	    // painted when in frozen state.
	    if (mContentsFrozen)
	        return;
	    if (mPendingScale != 0)
	        startCommitScaleChangeTask();
	    else {
	        startBSUpdateTask();
	        startTileBufferUpdateTask();
	    }
	}

	private void updateTileBuffers(BSTimerTask task) {
		if (mContentsFrozen)
	        return;

		mUpdateFlowBreaked = false;
	    mClient.tbsPaintBegin();

	    List<Rect> paintedArea = new ArrayList<Rect>();
	    List<ITile> dirtyTiles = new ArrayList<ITile>();
	    Iterator<Entry<Coordinate, ITile>> it = mMainTiles.iterator();
	    while (it.hasNext()) {
	    	Entry<Coordinate, ITile> entry = it.next();
	        if (!entry.getValue().isDirty())
	            continue;
	        dirtyTiles.add(entry.getValue());
	    }

	    if (dirtyTiles.isEmpty()) {
	        mClient.tbsPaintEnd(paintedArea);
	        return;
	    }

	    // FIXME: In single threaded case, tile back buffers could be updated asynchronously 
	    // one by one and then swapped to front in one go. This would minimize the time spent
	    // blocking on tile updates.
	    int size = dirtyTiles.size();
	    for (int n = 0; n < size; ++n) {
	        Collection<Rect> paintedRects = dirtyTiles.get(n).updateBackBuffer();
	        paintedArea.addAll(paintedRects);
	        dirtyTiles.get(n).swapBackBufferToFront();
	        try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        
	        if (task != null && task.cancelled()) {
	        	mUpdateFlowBreaked = true;
	        	break;
	        }
	    }

	    mClient.tbsPaintEnd(paintedArea);
	}

	public void invalidate(final Rect contentsDirtyRect) {
		Rect dirtyRect = new Rect(mapFromContents(contentsDirtyRect));
	    Rect keepRectFitToTileSize = tileRectForCoordinate(tileCoordinateForPoint(mKeepRect.getLeft(), mKeepRect.getTop()));
	    keepRectFitToTileSize.composite(tileRectForCoordinate(tileCoordinateForPoint(mKeepRect.getRight(), mKeepRect.getBottom())));

	    // Only iterate on the part of the rect that we know we might have tiles.
	    Rect coveredDirtyRect = Rect.intersect(dirtyRect, keepRectFitToTileSize);
	    Coordinate topLeft = tileCoordinateForPoint(coveredDirtyRect.getLeft(), coveredDirtyRect.getTop());
	    Coordinate bottomRight = tileCoordinateForPoint(coveredDirtyRect.getRight(), coveredDirtyRect.getBottom());

	    for (int yCoordinate = topLeft.getY(); yCoordinate <= bottomRight.getY(); ++yCoordinate) {
	        for (int xCoordinate = topLeft.getX(); xCoordinate <= bottomRight.getX(); ++xCoordinate) {
	            ITile currentTile = getTileAt(xCoordinate, yCoordinate);
	            if (currentTile == null)
	                continue;
	            // Pass the full rect to each tile as coveredDirtyRect might not
	            // contain them completely and we don't want partial tile redraws.
	            currentTile.invalidate(dirtyRect);
	        }
	    }

	    startTileBufferUpdateTask();
	}
    
    public void paint(GLCanvas canvas, int offsetX, int offsetY, final Rect rect) {
    	synchronized (mScaleBufferTiles) {
        	Iterator<Entry<Coordinate, ITile>> it = mScaleBufferTiles.iterator();
        	while (it.hasNext()) {
        		Entry<Coordinate, ITile> entry = it.next();
        		ITile currentTile = entry.getValue();
        		if (currentTile != null && currentTile.isReadyToPaint()) {
        			float scale = mPendingScale == 0 ? mContentsScale : mPendingScale;
                    currentTile.paint(canvas, offsetX, offsetY, null, scale / mScaleBufferTiles.getScale());
                }
        	}
		}
        Rect dirtyRect = mapFromContents(rect, mScaleBufferTiles.getScale());
        Coordinate topLeft = tileCoordinateForPoint(dirtyRect.getLeft(), dirtyRect.getTop());
        Coordinate bottomRight = tileCoordinateForPoint(dirtyRect.getRight(), dirtyRect.getBottom());
//        for (int yCoordinate = topLeft.getY(); yCoordinate <= bottomRight.getY(); ++yCoordinate) {
//	            for (int xCoordinate = topLeft.getX(); xCoordinate <= bottomRight.getX(); ++xCoordinate) {
//	                Coordinate currentCoordinate = new Coordinate(xCoordinate, yCoordinate);
//	                ITile currentTile = mScaleBufferTiles.get(currentCoordinate);
//		            if (currentTile != null && currentTile.isReadyToPaint()) {
//		                currentTile.paint(canvas, offsetX, offsetY, dirtyRect, mPendingScale / mScaleBufferTiles.getScale());
//		            } else {
//		                Rect tileRect = tileRectForCoordinate(currentCoordinate);
//		                Rect target = Rect.intersect(tileRect, dirtyRect);
//		                if (target == null || target.isEmpty())
//		                    continue;
//		                
////		                float scaleFactor = mOldScale == 0 ? 1 : mContentsScale / mOldScale;
////		                float left = offsetX + target.getLeft() * scaleFactor;
////		                float top = offsetY + target.getTop() * scaleFactor;
////		                float width = target.getWidth() * scaleFactor;
////		                float height = target.getHeight() * scaleFactor;
//		//                    canvas.drawTexture(mTextureBuffer.getCheckerTextureID(canvas.getGL(), (int)width, (int)height), left, top, width, height);
//		            }
//	            
//	        }
//        }
        
        dirtyRect = mapFromContents(rect);
        topLeft = tileCoordinateForPoint(dirtyRect.getLeft(), dirtyRect.getTop());
        bottomRight = tileCoordinateForPoint(dirtyRect.getRight(), dirtyRect.getBottom());
        for (int yCoordinate = topLeft.getY(); yCoordinate <= bottomRight.getY(); ++yCoordinate) {
            for (int xCoordinate = topLeft.getX(); xCoordinate <= bottomRight.getX(); ++xCoordinate) {
                Coordinate currentCoordinate = new Coordinate(xCoordinate, yCoordinate);
                ITile currentTile = mMainTiles.get(currentCoordinate);
                if (currentTile != null && currentTile.isReadyToPaint()) {
                	float scaleFactor = mPendingScale == 0 ? 1 : mPendingScale / mContentsScale;
                    currentTile.paint(canvas, offsetX, offsetY, dirtyRect, scaleFactor);
                } else {
                    Rect tileRect = tileRectForCoordinate(currentCoordinate);
                    Rect target = Rect.intersect(tileRect, dirtyRect);
                    if (target == null || target.isEmpty())
                        continue;
                    
                    float scaleFactor = mPendingScale == 0 ? 1 : mPendingScale / mContentsScale;
                    float left = offsetX + target.getLeft() * scaleFactor;
                    float top = offsetY + target.getTop() * scaleFactor;
                    float width = target.getWidth() * scaleFactor;
                    float height = target.getHeight() * scaleFactor;
//                    canvas.drawTexture(mTextureBuffer.getCheckerTextureID(canvas.getGL(), (int)width, (int)height), left, top, width, height);
                }
            }
        }
    }

    public Size getTileSize() { 
    	return mTileSize; 
	}
    
    public void setTileSize(final Size sz) {
    	if (sz == null)
    		return;
    	
    	sz.copyTo(mTileSize);
	    mMainTiles.clear();
	    startBSUpdateTask();
    }

    public Rect mapToContents(final Rect rect) {
    	int l = (int) (rect.getLeft() / mContentsScale);
    	int t = (int) (rect.getTop() / mContentsScale);
    	int r = (int) (l + rect.getWidth() / mContentsScale + 0.5f);
    	int b = (int) (t + rect.getHeight() / mContentsScale + 0.5f);
    	return new Rect(l, t, r - l, b - t);
    }
    
    public Rect mapFromContents(final Rect rect) {
    	return mapFromContents(rect, mContentsScale);
    }   
    
    private Rect mapFromContents(final Rect rect, float contentsScale) {
    	int l = (int) (rect.getLeft() * contentsScale);
    	int t = (int) (rect.getTop() * contentsScale);
    	int r = (int) (l + rect.getWidth() * contentsScale + 0.5f);
    	int b = (int) (t + rect.getHeight() * contentsScale + 0.5f);
    	return new Rect(l, t, r - l, b - t);
    }

    public Rect tileRectForCoordinate(final Coordinate coordinate) {
	   return tileRectForCoordinate(coordinate.getX(), coordinate.getY());
    }
    
    public Rect tileRectForCoordinate(final int tileX, final int tileY) {
    	Rect rect = new Rect(tileX * mTileSize.getWidth(),
            tileY * mTileSize.getHeight(),
            mTileSize.getWidth(),
            mTileSize.getHeight());

	   rect.intersect(mRect);
	   return rect;
    }
    
    public Coordinate tileCoordinateForPoint(final Point point) {
	    return tileCoordinateForPoint(point.getX(), point.getY());
    }    
    
    public Coordinate tileCoordinateForPoint(final int tileX, final int tileY) {
    	int x = tileX / mTileSize.getWidth();
	    int y = tileY / mTileSize.getHeight();
	    return new Coordinate(Math.max(x, 0), Math.max(y, 0));
    }
    
    public double tileDistance(final Rect viewport, final Coordinate tileCoordinate) {
    	if (viewport.intersectsWith(tileRectForCoordinate(tileCoordinate)))
    		return 0;

	    Coordinate centerCoordinate = tileCoordinateForPoint(viewport.centerX(), viewport.centerY());

	    return Math.max(Math.abs(centerCoordinate.getY() - tileCoordinate.getY()), Math.abs(centerCoordinate.getX() - tileCoordinate.getX()));
    }

    public double tileDistance(final Rect viewport, final int tileX, final int tileY) {
    	if (viewport.intersectsWith(tileRectForCoordinate(tileX, tileY)))
    		return 0;

	    Coordinate centerCoordinate = tileCoordinateForPoint(viewport.centerX(), viewport.centerY());

	    return Math.max(Math.abs(centerCoordinate.getY() - tileY), Math.abs(centerCoordinate.getX() - tileX));
    }

    public Rect coverRect() { 
    	return mCoverRect; 
	}
    
    public boolean visibleAreaIsCovered() {
    	Rect boundedVisibleContentsRect = Rect.intersect(mClient.tbsGetVisibleRect(), mClient.tbsGetContentsRect());
        return coverageRatio(boundedVisibleContentsRect) == 1.0f;
    }
    
    public void removeAllNonVisibleTiles() {
    	Rect boundedVisibleRect = mapFromContents(Rect.intersect(mClient.tbsGetVisibleRect(), mClient.tbsGetContentsRect()));
        setKeepRect(boundedVisibleRect);
    }

    public void setSupportsAlpha(boolean supportsAlpha) {
    	if (supportsAlpha == mSupportsAlpha)
	        return;
    	 
	    mSupportsAlpha = supportsAlpha;
	    invalidate(mRect);
    }

    private void startTileBufferUpdateTask() {
    	if (!mCommitTileUpdatesOnIdleEventLoop)
            return;

        if (isTileBufferUpdatesSuspended())
            return;
        
        Log.d("tt", "Tile Update scheduleTask scale " + mContentsScale + " pending scale " + mPendingScale);
        mUpdateTimer.cancelAllTasks();
        mUpdateTimer.scheduleTask(new BufferUpdateTask(), 0);
    }
    
    private void startBSUpdateTask() {
    	startBSUpdateTask(0);
    }

    private void startBSUpdateTask(long interval) {
    	if (!mCommitTileUpdatesOnIdleEventLoop)
            return;

        if (isBackingStoreUpdatesSuspended())
            return;

        Log.d("tt", "BSUpdate scheduleTask scale " + mContentsScale + " pending scale " + mPendingScale + " interval " + interval);
        mUpdateTimer.cancelAllTasks();
        mUpdateTimer.scheduleTask(new BSUpdateTimerTask(), interval);
    }

    private void createTiles(BSTimerTask task) {
    	// Guard here as as these can change before the timer fires.
        if (isBackingStoreUpdatesSuspended())
            return;

        // Update our backing store geometry.
        final Rect previousRect = mRect.clone();
        mapFromContents(mClient.tbsGetContentsRect()).copyTo(mRect);
        mPendingTrajectoryVector.copyTo(mTrajectoryVector);
        visibleRect().copyTo(mVisibleRect);

        if (mRect.isEmpty()) {
            setCoverRect(new Rect());
            setKeepRect(new Rect());
            return;
        }

        /* We must compute cover and keep rects using the visibleRect, instead of the rect intersecting the visibleRect with m_rect,
         * because TBS can be used as a backing store of GraphicsLayer and the visible rect usually does not intersect with m_rect.
         * In the below case, the intersecting rect is an empty.
         *
         *  +---------------+
         *  |               |
         *  |   m_rect      |
         *  |       +-------|-----------------------+
         *  |       | HERE  |  cover or keep        |
         *  +---------------+      rect             |
         *          |         +---------+           |
         *          |         | visible |           |
         *          |         |  rect   |           |
         *          |         +---------+           |
         *          |                               |
         *          |                               |
         *          +-------------------------------+
         *
         * We must create or keep the tiles in the HERE region.
         */

        Rect coverRect = new Rect();
        Rect keepRect = new Rect();
        computeCoverAndKeepRect(mVisibleRect, coverRect, keepRect);

        setCoverRect(coverRect);
        setKeepRect(keepRect);

        if (coverRect.isEmpty())
            return;

        // Resize tiles at the edge in case the contents size has changed, but only do so
        // after having dropped tiles outside the keep rect.
        boolean didResizeTiles = false;
        if (!previousRect.equals(mRect))
            didResizeTiles = resizeEdgeTiles();

        // Search for the tile position closest to the viewport center that does not yet contain a tile.
        // Which position is considered the closest depends on the tileDistance function.
        double shortestDistance = Double.POSITIVE_INFINITY;
        List<Coordinate> tilesToCreate = new ArrayList<Coordinate>();
        int requiredTileCount = 0;

        // Cover areas (in tiles) with minimum distance from the visible rect. If the visible rect is
        // not covered already it will be covered first in one go, due to the distance being 0 for tiles
        // inside the visible rect.
        Coordinate topLeft = tileCoordinateForPoint(coverRect.getLeft(), coverRect.getTop());
        Coordinate bottomRight = tileCoordinateForPoint(coverRect.getRight(), coverRect.getBottom());
        for (int yCoordinate = topLeft.getY(); yCoordinate <= bottomRight.getY(); ++yCoordinate) {
            for (int xCoordinate = topLeft.getX(); xCoordinate <= bottomRight.getX(); ++xCoordinate) {
                if (getTileAt(xCoordinate, yCoordinate) != null)
                    continue;
                ++requiredTileCount;
                double distance = tileDistance(mVisibleRect, xCoordinate, yCoordinate);
                if (distance > shortestDistance)
                    continue;
                if (distance < shortestDistance) {
                    tilesToCreate.clear();
                    shortestDistance = distance;
                }
                tilesToCreate.add(new Coordinate(xCoordinate, yCoordinate));
            }
        }

        // Now construct the tile(s) within the shortest distance.
        int tilesToCreateCount = tilesToCreate.size();
        for (int n = 0; n < tilesToCreateCount; ++n) {
            Coordinate coordinate = tilesToCreate.get(n);
            setTile(coordinate, mBackend.createTile(coordinate));
        }
        requiredTileCount -= tilesToCreateCount;

        // Paint the content of the newly created tiles or resized tiles.
        if (tilesToCreateCount != 0 || didResizeTiles)
            updateTileBuffers(task);

        // Re-call createTiles on a timer to cover the visible area with the newest shortest distance.
        mPendingTileCreation = requiredTileCount != 0;
        if (mPendingTileCreation) {
            if (!mCommitTileUpdatesOnIdleEventLoop) {
                mClient.tbsHasPendingTileCreation();
                return;
            }

            Log.d("tt", "start BSUpdate scheduleTask in createTiles func scale " + mContentsScale + " pending scale " + mPendingScale);
            if (task == null || !task.cancelled())
            	startBSUpdateTask(TILE_CREATION_DELAY_MS);
        }
    }
    
    private void computeCoverAndKeepRect(final Rect visibleRect, Rect coverRect, Rect keepRect) {
    	visibleRect.copyTo(coverRect);
        visibleRect.copyTo(keepRect);

        // If we cover more that the actual viewport we can be smart about which tiles we choose to render.
        if (mCoverAreaMultiplier > 1) {
            // The initial cover area covers equally in each direction, according to the coverAreaMultiplier.
            coverRect.inflate((int)(visibleRect.getWidth() * (mCoverAreaMultiplier - 1) / 2), (int)(visibleRect.getHeight() * (mCoverAreaMultiplier - 1) / 2));
            coverRect.copyTo(keepRect);

            if (mPendingTrajectoryVector.getX() != 0 || mPendingTrajectoryVector.getY() != 0) {
                // A null trajectory vector (no motion) means that tiles for the coverArea will be created.
                // A non-null trajectory vector will shrink the covered rect to visibleRect plus its expansion from its
                // center toward the cover area edges in the direction of the given vector.

                // E.g. if visibleRect == (10,10)5x5 and coverAreaMultiplier == 3.0:
                // a (0,0) trajectory vector will create tiles intersecting (5,5)15x15,
                // a (1,0) trajectory vector will create tiles intersecting (10,10)10x5,
                // and a (1,1) trajectory vector will create tiles intersecting (10,10)10x10.

                // Multiply the vector by the distance to the edge of the cover area.
                float trajectoryVectorMultiplier = (mCoverAreaMultiplier - 1) / 2;

                // Unite the visible rect with a "ghost" of the visible rect moved in the direction of the trajectory vector.
                visibleRect.copyTo(coverRect);
                coverRect.offset((int)(coverRect.getWidth() * mTrajectoryVector.getX() * trajectoryVectorMultiplier),
                               (int)(coverRect.getHeight() * mTrajectoryVector.getY() * trajectoryVectorMultiplier));

                coverRect.composite(visibleRect);
            }
            
            assert(keepRect.contains(coverRect));
        }

        adjustForContentsRect(coverRect);

        // The keep rect is an inflated version of the cover rect, inflated in tile dimensions.
        keepRect.composite(coverRect);
        keepRect.inflate(mTileSize.getWidth() / 2, mTileSize.getHeight() / 2);
        keepRect.intersect(mRect);

        assert(coverRect.isEmpty() || keepRect.contains(coverRect));
    }

    private boolean isBackingStoreUpdatesSuspended() {
    	return mContentsFrozen;
    }
    
    private boolean isTileBufferUpdatesSuspended() {
    	return mContentsFrozen;
    }

    private void startCommitScaleChangeTask() {
    	if (isBackingStoreUpdatesSuspended())
             return;

        CommitScaleChangeTask task = new CommitScaleChangeTask();
        Log.d("tt", "CommitScale scheduleTask " + task.hashCode() + " " + mContentsScale + " pending scale " + mPendingScale);
    	mUpdateTimer.cancelAllTasks();
		mUpdateTimer.scheduleTask(task, 0);
    }

    private boolean resizeEdgeTiles() {
    	boolean wasResized = false;
    	List<Coordinate> tilesToRemove = new ArrayList<Coordinate>();
	    Iterator<Entry<Coordinate, ITile>> it = mMainTiles.iterator();
	    while (it.hasNext()) {
	    	Entry<Coordinate, ITile> entry = it.next();
	        Coordinate tileCoordinate = entry.getValue().coordinate();
	        Rect tileRect = entry.getValue().rect();
	        Rect expectedTileRect = tileRectForCoordinate(tileCoordinate);
	        if (expectedTileRect.isEmpty())
	            tilesToRemove.add(tileCoordinate);
	        else if (!expectedTileRect.equals(tileRect)) {
	            entry.getValue().resize(expectedTileRect.getWidth(), expectedTileRect.getHeight());
	            wasResized = true;
	        }
	    }
	    int removeCount = tilesToRemove.size();
	    for (int n = 0; n < removeCount; ++n)
	        removeTile(tilesToRemove.get(n));
	    return wasResized;
    }
    
    private void setCoverRect(final Rect rt) { rt.copyTo(mCoverRect); }
    
    private void setKeepRect(final Rect keepRect) {
    	 // Drop tiles outside the new keepRect.

        RectF keepRectF = keepRect.toRectF();

        List<Coordinate> toRemove = new ArrayList<Coordinate>();;
        Iterator<Entry<Coordinate, ITile>> it = mMainTiles.iterator();
        while (it.hasNext()) {
        	Entry<Coordinate, ITile> entry = it.next();
            Coordinate coordinate = entry.getValue().coordinate();
            RectF tileRect = entry.getValue().rect().toRectF();
            if (!tileRect.intersectsWith(keepRectF))
                toRemove.add(coordinate);
        }
        int removeCount = toRemove.size();
        for (int n = 0; n < removeCount; ++n)
            removeTile(toRemove.get(n));

        keepRect.copyTo(mKeepRect);
    }

    private ITile getTileAt(int x, int y) {
    	return mMainTiles.get(new Coordinate(x, y)); // TODO effective problem
    }
    
    private void setTile(final Coordinate coordinate, ITile tile) {
    	mMainTiles.put(coordinate, tile);
    }
    
    private void removeTile(final Coordinate coordinate) {
    	mMainTiles.remove(coordinate);
    }

    private Rect visibleRect() {
    	return mapFromContents(mClient.tbsGetVisibleRect());
    }

    private float coverageRatio(final Rect contentsRect) {
    	Rect dirtyRect = mapFromContents(contentsRect);
        float rectArea = dirtyRect.getWidth() * dirtyRect.getHeight();
        float coverArea = 0.0f;

        Coordinate topLeft = tileCoordinateForPoint(dirtyRect.getLeft(), dirtyRect.getTop());
        Coordinate bottomRight = tileCoordinateForPoint(dirtyRect.getRight(), dirtyRect.getBottom());

        for (int yCoordinate = topLeft.getY(); yCoordinate <= bottomRight.getY(); ++yCoordinate) {
            for (int xCoordinate = topLeft.getX(); xCoordinate <= bottomRight.getX(); ++xCoordinate) {
                ITile currentTile = getTileAt(xCoordinate, yCoordinate);
                if (currentTile != null && currentTile.isReadyToPaint()) {
                    Rect coverRect = Rect.intersect(dirtyRect, currentTile.rect());
                    coverArea += coverRect.getWidth() * coverRect.getHeight();
                }
            }
        }
        return coverArea / rectArea;
    }
    
    private void adjustForContentsRect(Rect rect) {
        Rect bounds = mRect;
        Size candidateSize = new Size(rect.getWidth(), rect.getHeight());

        rect.intersect(bounds);

        if (rect.getWidth() == candidateSize.getWidth() && rect.getHeight() == candidateSize.getHeight())
            return;

        /*
         * In the following case, there is no intersection of the contents rect and the cover rect.
         * Thus the latter should not be inflated.
         *
         *  +---------------+
         *  |   m_rect      |
         *  +---------------+
         *
         *          +-------------------------------+
         *          |          cover rect           |
         *          |         +---------+           |
         *          |         | visible |           |
         *          |         |  rect   |           |
         *          |         +---------+           |
         *          +-------------------------------+
         */
        if (rect.isEmpty())
            return;

        // Try to create a cover rect of the same size as the candidate, but within content bounds.
        int pixelsCovered = candidateSize.getWidth() * candidateSize.getHeight();

        if (rect.getWidth() < candidateSize.getWidth())
            rect.inflate(0, ((pixelsCovered / rect.getWidth()) - rect.getHeight()) / 2);
        if (rect.getHeight() < candidateSize.getHeight())
            rect.inflate(((pixelsCovered / rect.getHeight()) - rect.getWidth()) / 2, 0);

        rect.intersect(bounds);
    }

    private class BufferUpdateTask extends BSTimerTask {
		@Override
		public void runTask() {
			updateTileBuffers(this);
		}
	};
	
    private class BSUpdateTimerTask extends BSTimerTask {
		@Override
		public void runTask() {
			createTiles(this);
		}
	};
	
	private class CommitScaleChangeTask extends BSTimerTask {
		@Override
		public void runTask() {
			float oldScale = mContentsScale;
	    	mContentsScale = mPendingScale;
		    mPendingScale = 0;
	    	Log.d("tt", "Scale task run (break " + mUpdateFlowBreaked + ") " + this.hashCode());
		    if (mUpdateFlowBreaked) {
		    	mMainTiles.clear();
		    } else {
		    	synchronized (mScaleBufferTiles) {
			    	mScaleBufferTiles.setScale(oldScale);
				    mScaleBufferTiles.clear();
				    mMainTiles.moveAllTo(mScaleBufferTiles);
				}
		    }
		    
		    coverWithTilesIfNeeded(this);
	    }
	}
}
