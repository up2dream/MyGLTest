package cn.wps.moffice.presentation.sal.drawing;

import java.io.Serializable;

public class Point implements Serializable {
	private int x;
	private int y;
	private boolean empty;

	public Point() {
		x = 0;
		y = 0;
		empty = false;
	}

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
		empty = false;
	}

	public Point(Point point) {
		x = point.getX();
		y = point.getY();
		empty = false;
	}

	public void Dispose() {
	}

	public final void offset(int dx, int dy) {
		x += dx;
		y += dy;
	}

	public final int getX() {
		return x;
	}

	public final void setX(int x) {
		if (this.x != x) {
			this.x = x;
			empty = false;
		}
	}

	public final int getY() {
		return y;
	}

	public final void setY(int y) {
		if (this.y != y) {
			this.y = y;
			empty = false;
		}
	}

	public final void SetPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public final boolean IsEmpty() {
		return empty;
	}

	@Override
	public final Point clone() {
		Point clonedPoint = new Point();
		CopyTo(clonedPoint);

		return clonedPoint;
	}

	public final void CopyTo(Point point) {
		point.setX(getX());
		point.setY(getY());
	}

	public PointF toPointF() {
		return new PointF(x, y);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) 
			return false;
		if (this == o)
			return true;
		if (!(o instanceof Point))
			return false;
		
		Point value = (Point)o;
		return x == value.x && y == value.y;
	}
	
	@Override
	public String toString() {
		return String.format("(%d, %d)", getX(), getY());
	}

}