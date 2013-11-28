package cn.wps.moffice.presentation.sal.drawing;

import java.io.Serializable;

public class PointF implements Serializable{
	public PointF() {
		x = 0;
		y = 0;
		empty = false;
	}

	public PointF(float x, float y) {
		this.x = x;
		this.y = y;
		empty = false;
	}

	public PointF(PointF point) {
		x = point.getX();
		y = point.getY();
		empty = false;
	}

	public void Dispose() {
	}

	public final void offset(float dx, float dy) {
		x += dx;
		y += dy;
	}

	public final float getX() {
		return x;
	}

	public final void setX(float x) {
		if (this.x != x) {
			this.x = x;
			empty = false;
		}
	}

	public final float getY() {
		return y;
	}

	public final void setY(float y) {
		if (this.y != y) {
			this.y = y;
			empty = false;
		}
	}

	public final void setPoint(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public final boolean IsEmpty() {
		return empty;
	}

	@Override
	public final PointF clone() {
		PointF clonedPoint = new PointF();
		copyTo(clonedPoint);

		return clonedPoint;
	}

	public final void copyTo(PointF point) {
		point.setX(getX());
		point.setY(getY());
	}
	
	public final Point toPoint() {
		return new Point((int)x, (int)y);
	}
	
	@Override
	public String toString() {
		return String.format("(%f, %f)", getX(), getY());
	}

	private float x;
	private float y;
	private boolean empty;
}