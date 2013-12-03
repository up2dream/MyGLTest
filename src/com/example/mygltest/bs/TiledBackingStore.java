package com.example.mygltest.bs;

import javax.microedition.khronos.opengles.GL11;

import cn.wps.moffice.presentation.sal.drawing.Point;
import cn.wps.moffice.presentation.sal.drawing.PointF;
import cn.wps.moffice.presentation.sal.drawing.Rect;
import cn.wps.moffice.presentation.sal.drawing.Size;

public class TiledBackingStore {
	private TiledBackingStoreClient m_client;
	private TiledBackingStoreBackend m_backend;

    TileMap m_tiles;

    private Timer<TiledBackingStore> m_tileBufferUpdateTimer;
    private Timer<TiledBackingStore> m_backingStoreUpdateTimer;

    private Size m_tileSize;
    private float m_coverAreaMultiplier;

    private PointF m_trajectoryVector;
    private PointF m_pendingTrajectoryVector;
    private Rect m_visibleRect;

    private Rect m_coverRect;
    private Rect m_keepRect;
    private Rect m_rect;

    private float m_contentsScale;
    private float m_pendingScale;

    private boolean m_commitTileUpdatesOnIdleEventLoop;
    private boolean m_contentsFrozen;
    private boolean m_supportsAlpha;
    private boolean m_pendingTileCreation;
	    
	public TiledBackingStore(TiledBackingStoreClient client, TiledBackingStoreBackend backend) {
	
	}

	public TiledBackingStoreClient client() {
    	return m_client; 
	}

    // Used when class methods cannot be called asynchronously by client.
    // Updates of tiles are committed as soon as all the events in event queue have been processed.
	public void setCommitTileUpdatesOnIdleEventLoop(boolean enable) {
    	m_commitTileUpdatesOnIdleEventLoop = enable; 
	}

	public void setTrajectoryVector(final PointF pt) {
    	
    }
    
	public void coverWithTilesIfNeeded() {
    }

	public float contentsScale() {
		return m_contentsScale; 
	}
	
	public void setContentsScale(float scale) {
	}

	public boolean contentsFrozen() {
		return m_contentsFrozen; 
	}
	
	public void setContentsFrozen(boolean frozen) {
	}

	public void updateTileBuffers() {
	}

	public void invalidate(final Rect dirtyRect) {
    }
    
    public void paint(GL11 gl, final Rect rt) {
    }

    public Size tileSize() { 
    	return m_tileSize; 
	}
    
    public void setTileSize(final Size sz) {
    }

    public Rect mapToContents(final Rect rt) {
    }
    
    public Rect mapFromContents(final Rect rt) {
    }

    public Rect tileRectForCoordinate(final Coordinate coord) {
    }
    
    public Coordinate tileCoordinateForPoint(final Point pt) {
    }
    
    public double tileDistance(final Rect viewport, final Coordinate coord) {
    }

    public Rect coverRect() { 
    	return m_coverRect; 
	}
    
    public boolean visibleAreaIsCovered() {
    }
    
    public void removeAllNonVisibleTiles() {
    }

    public void setSupportsAlpha(boolean value) {
    }

    private void startTileBufferUpdateTimer() {
    }
    
    private void startBackingStoreUpdateTimer() {
    	startBackingStoreUpdateTimer(0);
    }

    private void startBackingStoreUpdateTimer(double value) {
    }

    void tileBufferUpdateTimerFired(Timer<TiledBackingStore>*);
    void backingStoreUpdateTimerFired(Timer<TiledBackingStore>*);

    private void createTiles() {
    }
    
    private void computeCoverAndKeepRect(final Rect visibleRect, Rect coverRect, Rect keepRect) {
    }

    private boolean isBackingStoreUpdatesSuspended() {
    }
    
    private boolean isTileBufferUpdatesSuspended() {
    }

    private void commitScaleChange() {
    }

    private boolean resizeEdgeTiles() {
    }
    
    private void setCoverRect(final Rect rt) { m_coverRect = rt; }
    private void setKeepRect(final Rect rt) {
    }

    private Tile tileAt(final Coordinate coord) {
    }
    
    private void setTile(final Coordinate coordinate, Tile tile) {
    }
    
    private void removeTile(final Coordinate coordinate) {
    }

    private Rect visibleRect() {
    }

    private float coverageRatio(final Rect rect) {
    }
    
    private void adjustForContentsRect(Rect rect) {
    }

    private void paintCheckerPattern(GL11 gl, final Rect rect, final Coordinate coord) {
	}
}
