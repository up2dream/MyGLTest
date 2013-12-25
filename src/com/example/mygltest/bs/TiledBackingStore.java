package com.example.mygltest.bs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL11;

import cn.wps.moffice.presentation.sal.drawing.Point;
import cn.wps.moffice.presentation.sal.drawing.PointF;
import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.RectF;
import cn.wps.moffice.presentation.sal.drawing.Size;

public class TiledBackingStore {
	private static final int defaultTileDimension = 512;
    private static final long tileCreationDelay = 10;
	
	private TiledBackingStoreClient m_client;
	private TiledBackingStoreBackend m_backend;

    TileMap m_tiles;

    private Timer m_tileBufferUpdateTimer = new Timer("tileBufferUpdate");
    private Timer m_backingStoreUpdateTimer = new Timer("backingStoreUpdate");
    
    private BSTimerTask mTileBufferUpdateTimerTask = new BSTimerTask() {
		
		@Override
		public void runTask() {
			tileBufferUpdateTimerFired();
		}
	};

	private BSTimerTask mBackingStoreUpdateTimerTask = new BSTimerTask() {
		@Override
		public void runTask() {
			backingStoreUpdateTimerFired();
		}
	};
	
    private Size m_tileSize = new Size(defaultTileDimension, defaultTileDimension);
    private float m_coverAreaMultiplier = 2.0f;

    private PointF m_trajectoryVector;
    private PointF m_pendingTrajectoryVector;
    private Rect m_visibleRect;

    private Rect m_coverRect;
    private Rect m_keepRect;
    private Rect m_rect;

    private float m_contentsScale = 1.0f;
    private float m_pendingScale = 0;

    private boolean m_commitTileUpdatesOnIdleEventLoop = false;
    private boolean m_contentsFrozen = false;
    private boolean m_supportsAlpha = false;
    private boolean m_pendingTileCreation = false;
	    
	public TiledBackingStore(TiledBackingStoreClient client, TiledBackingStoreBackend backend) {
		m_client = client;
		m_backend = backend;
	}

	public TiledBackingStoreClient client() {
    	return m_client; 
	}

    // Used when class methods cannot be called asynchronously by client.
    // Updates of tiles are committed as soon as all the events in event queue have been processed.
	public void setCommitTileUpdatesOnIdleEventLoop(boolean enable) {
    	m_commitTileUpdatesOnIdleEventLoop = enable; 
	}

	public void setTrajectoryVector(final PointF trajectoryVector) {
		if (trajectoryVector == null)
			return;
		
		trajectoryVector.copyTo(m_pendingTrajectoryVector);
		m_pendingTrajectoryVector.normalize();
    }
    
	public void coverWithTilesIfNeeded() {
		Rect visibleRect = visibleRect();
		Rect rect = mapFromContents(m_client.tiledBackingStoreContentsRect());

		boolean didChange = m_trajectoryVector != m_pendingTrajectoryVector || m_visibleRect != visibleRect || m_rect != rect;
		if (didChange || m_pendingTileCreation)
			createTiles();
    }

	public float contentsScale() {
		return m_contentsScale; 
	}
	
	public void setContentsScale(float scale) {
		if (m_pendingScale == m_contentsScale) {
	        m_pendingScale = 0;
	        return;
	    }
		
	    m_pendingScale = scale;
	    if (m_contentsFrozen)
	        return;
	    
	    commitScaleChange();
	}

	public boolean contentsFrozen() {
		return m_contentsFrozen; 
	}
	
	public void setContentsFrozen(boolean freeze) {
		if (m_contentsFrozen == freeze)
	        return;

	    m_contentsFrozen = freeze;

	    // Restart the timers. There might be pending invalidations that
	    // were not painted or created because tiles are not created or
	    // painted when in frozen state.
	    if (m_contentsFrozen)
	        return;
	    if (m_pendingScale != 0)
	        commitScaleChange();
	    else {
	        startBackingStoreUpdateTimer();
	        startTileBufferUpdateTimer();
	    }
	}

	public void updateTileBuffers() {
		if (m_contentsFrozen)
	        return;

	    m_client.tiledBackingStorePaintBegin();

	    List<Rect> paintedArea = new ArrayList<Rect>();
	    List<Tile> dirtyTiles = new ArrayList<Tile>();
	    Iterator<Entry<Coordinate, Tile>> it = m_tiles.entrySet().iterator();
	    while (it.hasNext()) {
	    	Entry<Coordinate, Tile> entry = it.next();
	        if (!entry.getValue().isDirty())
	            continue;
	        dirtyTiles.add(entry.getValue());
	    }

	    if (dirtyTiles.isEmpty()) {
	        m_client.tiledBackingStorePaintEnd(paintedArea);
	        return;
	    }

	    // FIXME: In single threaded case, tile back buffers could be updated asynchronously 
	    // one by one and then swapped to front in one go. This would minimize the time spent
	    // blocking on tile updates.
	    int size = dirtyTiles.size();
	    for (int n = 0; n < size; ++n) {
	        List<Rect> paintedRects = dirtyTiles.get(n).updateBackBuffer();
	        paintedArea.addAll(paintedRects);
	        dirtyTiles.get(n).swapBackBufferToFront();
	    }

	    m_client.tiledBackingStorePaintEnd(paintedArea);
	}

	public void invalidate(final Rect contentsDirtyRect) {
		Rect dirtyRect = new Rect(mapFromContents(contentsDirtyRect));
	    Rect keepRectFitToTileSize = tileRectForCoordinate(tileCoordinateForPoint(m_keepRect.getLeft(), m_keepRect.getTop()));
	    keepRectFitToTileSize.composite(tileRectForCoordinate(tileCoordinateForPoint(m_keepRect.getRight(), m_keepRect.getBottom())));

	    // Only iterate on the part of the rect that we know we might have tiles.
	    Rect coveredDirtyRect = Rect.intersect(dirtyRect, keepRectFitToTileSize);
	    Coordinate topLeft = tileCoordinateForPoint(coveredDirtyRect.getLeft(), coveredDirtyRect.getTop());
	    Coordinate bottomRight = tileCoordinateForPoint(coveredDirtyRect.getRight(), coveredDirtyRect.getBottom());

	    for (int yCoordinate = topLeft.getY(); yCoordinate <= bottomRight.getY(); ++yCoordinate) {
	        for (int xCoordinate = topLeft.getX(); xCoordinate <= bottomRight.getX(); ++xCoordinate) {
	            Tile currentTile = tileAt(xCoordinate, yCoordinate);
	            if (currentTile == null)
	                continue;
	            // Pass the full rect to each tile as coveredDirtyRect might not
	            // contain them completely and we don't want partial tile redraws.
	            currentTile.invalidate(dirtyRect);
	        }
	    }

	    startTileBufferUpdateTimer();
	}
    
    public void paint(GL11 gl, final Rect rt) {
//    	context->save();
//
//        // Assumes the backing store is painted with the scale transform applied.
//        // Since tile content is already scaled, first revert the scaling from the painter.
//        context->scale(FloatSize(1.f / m_contentsScale, 1.f / m_contentsScale));
//
//        Rect dirtyRect = mapFromContents(rect);
//
//        Coordinate topLeft = tileCoordinateForPoint(dirtyRect.location());
//        Coordinate bottomRight = tileCoordinateForPoint(innerBottomRight(dirtyRect));
//
//        for (int yCoordinate = topLeft.getY(); yCoordinate <= bottomRight.getY(); ++yCoordinate) {
//            for (int xCoordinate = topLeft.getX(); xCoordinate <= bottomRight.getX(); ++xCoordinate) {
//                Coordinate currentCoordinate = new Coordinate(xCoordinate, yCoordinate);
//                Tile currentTile = tileAt(currentCoordinate);
//                if (currentTile != null && currentTile.isReadyToPaint())
//                    currentTile.paint(context, dirtyRect);
//                else {
//                    Rect tileRect = tileRectForCoordinate(currentCoordinate);
//                    Rect target = intersection(tileRect, dirtyRect);
//                    if (target.isEmpty())
//                        continue;
//                    m_backend.paintCheckerPattern(context, FloatRect(target));
//                }
//            }
//        }
//        context->restore();
    }

    public Size tileSize() { 
    	return m_tileSize; 
	}
    
    public void setTileSize(final Size sz) {
    	if (sz == null)
    		return;
    	
    	sz.copyTo(m_tileSize);
	    m_tiles.clear();
	    startBackingStoreUpdateTimer();
    }

    public Rect mapToContents(final Rect rect) {
    	int l = (int) (rect.getLeft() / m_contentsScale);
    	int t = (int) (rect.getTop() / m_contentsScale);
    	int r = (int) (l + rect.getWidth() / m_contentsScale + 0.5f);
    	int b = (int) (t + rect.getHeight() / m_contentsScale + 0.5f);
    	return new Rect(l, t, r - l, b - t);
    }
    
    public Rect mapFromContents(final Rect rect) {
    	int l = (int) (rect.getLeft() * m_contentsScale);
    	int t = (int) (rect.getTop() * m_contentsScale);
    	int r = (int) (l + rect.getWidth() * m_contentsScale + 0.5f);
    	int b = (int) (t + rect.getHeight() * m_contentsScale + 0.5f);
    	return new Rect(l, t, r - l, b - t);
    }

    public Rect tileRectForCoordinate(final Coordinate coordinate) {
	   return tileRectForCoordinate(coordinate.getX(), coordinate.getY());
    }
    
    public Rect tileRectForCoordinate(final int tileX, final int tileY) {
    	Rect rect = new Rect(tileX * m_tileSize.getWidth(),
            tileY * m_tileSize.getHeight(),
            m_tileSize.getWidth(),
            m_tileSize.getHeight());

	   rect.intersect(m_rect);
	   return rect;
    }
    
    public Coordinate tileCoordinateForPoint(final Point point) {
	    return tileCoordinateForPoint(point.getX(), point.getY());
    }    
    
    public Coordinate tileCoordinateForPoint(final int tileX, final int tileY) {
    	int x = tileX / m_tileSize.getWidth();
	    int y = tileY / m_tileSize.getHeight();
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
    	return m_coverRect; 
	}
    
    public boolean visibleAreaIsCovered() {
    	Rect boundedVisibleContentsRect = Rect.intersect(m_client.tiledBackingStoreVisibleRect(), m_client.tiledBackingStoreContentsRect());
        return coverageRatio(boundedVisibleContentsRect) == 1.0f;
    }
    
    public void removeAllNonVisibleTiles() {
    	Rect boundedVisibleRect = mapFromContents(Rect.intersect(m_client.tiledBackingStoreVisibleRect(), m_client.tiledBackingStoreContentsRect()));
        setKeepRect(boundedVisibleRect);
    }

    public void setSupportsAlpha(boolean a) {
    	 if (a == m_supportsAlpha)
    	        return;
    	    m_supportsAlpha = a;
    	    invalidate(m_rect);
    }

    private void startTileBufferUpdateTimer() {
    	if (!m_commitTileUpdatesOnIdleEventLoop)
            return;

        if (mTileBufferUpdateTimerTask.isActive() || isTileBufferUpdatesSuspended())
            return;
        m_tileBufferUpdateTimer.schedule(mTileBufferUpdateTimerTask, 0);
    }
    
    private void startBackingStoreUpdateTimer() {
    	startBackingStoreUpdateTimer(0);
    }

    private void startBackingStoreUpdateTimer(long interval) {
    	if (!m_commitTileUpdatesOnIdleEventLoop)
            return;

        if (mBackingStoreUpdateTimerTask.isActive() || isBackingStoreUpdatesSuspended())
            return;
        m_backingStoreUpdateTimer.schedule(mBackingStoreUpdateTimerTask, interval);
    }

    void tileBufferUpdateTimerFired() {
    	assert(m_commitTileUpdatesOnIdleEventLoop);
        updateTileBuffers();
    }
    
    void backingStoreUpdateTimerFired() {
    	assert(m_commitTileUpdatesOnIdleEventLoop);
	    createTiles();
    }

    private void createTiles() {
    	// Guard here as as these can change before the timer fires.
        if (isBackingStoreUpdatesSuspended())
            return;

        // Update our backing store geometry.
        final Rect previousRect = m_rect;
        m_rect = mapFromContents(m_client.tiledBackingStoreContentsRect());
        m_trajectoryVector = m_pendingTrajectoryVector;
        m_visibleRect = visibleRect();

        if (m_rect.isEmpty()) {
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
        computeCoverAndKeepRect(m_visibleRect, coverRect, keepRect);

        setCoverRect(coverRect);
        setKeepRect(keepRect);

        if (coverRect.isEmpty())
            return;

        // Resize tiles at the edge in case the contents size has changed, but only do so
        // after having dropped tiles outside the keep rect.
        boolean didResizeTiles = false;
        if (previousRect != m_rect)
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
                if (tileAt(xCoordinate, yCoordinate) != null)
                    continue;
                ++requiredTileCount;
                double distance = tileDistance(m_visibleRect, xCoordinate, yCoordinate);
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
            setTile(coordinate, m_backend.createTile(this, coordinate));
        }
        requiredTileCount -= tilesToCreateCount;

        // Paint the content of the newly created tiles or resized tiles.
        if (tilesToCreateCount != 0 || didResizeTiles)
            updateTileBuffers();

        // Re-call createTiles on a timer to cover the visible area with the newest shortest distance.
        m_pendingTileCreation = requiredTileCount != 0;
        if (m_pendingTileCreation) {
            if (!m_commitTileUpdatesOnIdleEventLoop) {
                m_client.tiledBackingStoreHasPendingTileCreation();
                return;
            }

            startBackingStoreUpdateTimer(tileCreationDelay);
        }
    }
    
    private void computeCoverAndKeepRect(final Rect visibleRect, Rect coverRect, Rect keepRect) {
    	coverRect = visibleRect;
        keepRect = visibleRect;

        // If we cover more that the actual viewport we can be smart about which tiles we choose to render.
        if (m_coverAreaMultiplier > 1) {
            // The initial cover area covers equally in each direction, according to the coverAreaMultiplier.
            coverRect.inflate((int)(visibleRect.getWidth() * (m_coverAreaMultiplier - 1) / 2), (int)(visibleRect.getHeight() * (m_coverAreaMultiplier - 1) / 2));
            keepRect = coverRect;

            if (m_pendingTrajectoryVector.getX() != 0 || m_pendingTrajectoryVector.getY() != 0) {
                // A null trajectory vector (no motion) means that tiles for the coverArea will be created.
                // A non-null trajectory vector will shrink the covered rect to visibleRect plus its expansion from its
                // center toward the cover area edges in the direction of the given vector.

                // E.g. if visibleRect == (10,10)5x5 and coverAreaMultiplier == 3.0:
                // a (0,0) trajectory vector will create tiles intersecting (5,5)15x15,
                // a (1,0) trajectory vector will create tiles intersecting (10,10)10x5,
                // and a (1,1) trajectory vector will create tiles intersecting (10,10)10x10.

                // Multiply the vector by the distance to the edge of the cover area.
                float trajectoryVectorMultiplier = (m_coverAreaMultiplier - 1) / 2;

                // Unite the visible rect with a "ghost" of the visible rect moved in the direction of the trajectory vector.
                coverRect = visibleRect;
                coverRect.offset((int)(coverRect.getWidth() * m_trajectoryVector.getX() * trajectoryVectorMultiplier),
                               (int)(coverRect.getHeight() * m_trajectoryVector.getY() * trajectoryVectorMultiplier));

                coverRect.composite(visibleRect);
            }
            
            assert(keepRect.contains(coverRect));
        }

        adjustForContentsRect(coverRect);

        // The keep rect is an inflated version of the cover rect, inflated in tile dimensions.
        keepRect.composite(coverRect);
        keepRect.inflate(m_tileSize.getWidth() / 2, m_tileSize.getHeight() / 2);
        keepRect.intersect(m_rect);

        assert(coverRect.isEmpty() || keepRect.contains(coverRect));
    }

    private boolean isBackingStoreUpdatesSuspended() {
    	return m_contentsFrozen;
    }
    
    private boolean isTileBufferUpdatesSuspended() {
    	return m_contentsFrozen;
    }

    private void commitScaleChange() {
    	m_contentsScale = m_pendingScale;
	    m_pendingScale = 0;
	    m_tiles.clear();
	    coverWithTilesIfNeeded();
    }

    private boolean resizeEdgeTiles() {
    	boolean wasResized = false;
    	List<Coordinate> tilesToRemove = new ArrayList<Coordinate>();
	    Iterator<Entry<Coordinate, Tile>> it = m_tiles.entrySet().iterator();
	    while (it.hasNext()) {
	    	Entry<Coordinate, Tile> entry = it.next();
	        Coordinate tileCoordinate = entry.getValue().coordinate();
	        Rect tileRect = entry.getValue().rect();
	        Rect expectedTileRect = tileRectForCoordinate(tileCoordinate);
	        if (expectedTileRect.isEmpty())
	            tilesToRemove.add(tileCoordinate);
	        else if (expectedTileRect != tileRect) {
	            entry.getValue().resize(expectedTileRect.getWidth(), expectedTileRect.getHeight());
	            wasResized = true;
	        }
	    }
	    int removeCount = tilesToRemove.size();
	    for (int n = 0; n < removeCount; ++n)
	        removeTile(tilesToRemove.get(n));
	    return wasResized;
    }
    
    private void setCoverRect(final Rect rt) { m_coverRect = rt; }
    private void setKeepRect(final Rect keepRect) {
    	 // Drop tiles outside the new keepRect.

        RectF keepRectF = keepRect.toRectF();

        List<Coordinate> toRemove = new ArrayList<Coordinate>();;
        Iterator<Entry<Coordinate, Tile>> it = m_tiles.entrySet().iterator();
        while (it.hasNext()) {
        	Entry<Coordinate, Tile> entry = it.next();
            Coordinate coordinate = entry.getValue().coordinate();
            RectF tileRect = entry.getValue().rect().toRectF();
            if (!tileRect.intersectsWith(keepRectF))
                toRemove.add(coordinate);
        }
        int removeCount = toRemove.size();
        for (int n = 0; n < removeCount; ++n)
            removeTile(toRemove.get(n));

        m_keepRect = keepRect;
    }

    private Tile tileAt(final Coordinate coordinate) {
    	return m_tiles.get(coordinate);
    }
    
    private Tile tileAt(int x, int y) {
    	return m_tiles.get(new Coordinate(x, y)); // TODO effective problem
    }
    
    private void setTile(final Coordinate coordinate, Tile tile) {
    	m_tiles.put(coordinate, tile);
    }
    
    private void removeTile(final Coordinate coordinate) {
    	m_tiles.remove(coordinate);
    }

    private Rect visibleRect() {
    	return mapFromContents(m_client.tiledBackingStoreVisibleRect());
    }

    private float coverageRatio(final Rect contentsRect) {
    	Rect dirtyRect = mapFromContents(contentsRect);
        float rectArea = dirtyRect.getWidth() * dirtyRect.getHeight();
        float coverArea = 0.0f;

        Coordinate topLeft = tileCoordinateForPoint(dirtyRect.getLeft(), dirtyRect.getTop());
        Coordinate bottomRight = tileCoordinateForPoint(dirtyRect.getRight(), dirtyRect.getBottom());

        for (int yCoordinate = topLeft.getY(); yCoordinate <= bottomRight.getY(); ++yCoordinate) {
            for (int xCoordinate = topLeft.getX(); xCoordinate <= bottomRight.getX(); ++xCoordinate) {
                Tile currentTile = tileAt(xCoordinate, yCoordinate);
                if (currentTile != null && currentTile.isReadyToPaint()) {
                    Rect coverRect = Rect.intersect(dirtyRect, currentTile.rect());
                    coverArea += coverRect.getWidth() * coverRect.getHeight();
                }
            }
        }
        return coverArea / rectArea;
    }
    
    private void adjustForContentsRect(Rect rect) {
        Rect bounds = m_rect;
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

    private void paintCheckerPattern(GL11 gl, final Rect rect, final Coordinate coord) {
    	
	}
    
    private abstract class BSTimerTask extends TimerTask {

    	private volatile boolean mActive = false;
    	
    	@Override
    	public void run() {
    		mActive = true;
    		
    		runTask();
    		
    		mActive = false;
    	}
    	
    	public abstract void runTask();
    	
		public boolean isActive() {
			return mActive;
		}
    	
    }
}
