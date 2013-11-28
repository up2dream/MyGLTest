package cn.wps.moffice.presentation.sal.drawing;

import java.io.Serializable;

public class Rect implements Serializable {
	private int _left;
	private int _top;
	private int _width;
	private int _height;

	public Rect() {
		_left = 0;
		_top = 0;
		_width = 0;
		_height = 0;
	}

	public Rect(Rect rect) {
		_left = rect.getLeft();
		_top = rect.getTop();
		_width = rect.getWidth();
		_height = rect.getHeight();

	}

	public Rect(int left, int top, int width, int height) {
		this._left = left;
		this._top = top;
		this._width = width;
		this._height = height;
	}

	public final int getLeft() {

		return _left;
	}

	public final void setLeft(int left) {
		this._left = left;
	}

	public final void moveLeft(int offset) {
		moveLeftTo(this._left + offset);
	}

	public final void moveLeftTo(int left) {
		int offset = left - getLeft();
		this._left = left;
		setWidth(getWidth() - offset);
	}

	public final int getRight() {
		return _left + _width;
	}

	public final void setRight(int right) {
		_width = right - _left;
	}

	public final void moveRight(int offset) {
		moveRightTo(getRight() + offset);
	}

	public final void moveRightTo(int right) {
		int offset = right - getRight();
		setWidth(getWidth() + offset);
	}

	public final int getTop() {
		return _top;
	}

	public final void setTop(int top) {
		this._top = top;
	}

	public final void moveTop(int offset) {
		moveTopTo(_top + offset);
	}

	public final void moveTopTo(int top) {
		int offset = top - getTop();
		setTop(top);
		setHeight(getHeight() - offset);
	}

	public final int getBottom() {
		return _top + _height;
	}

	public final void setBottom(int bottom) {
		_height = bottom - _top;
	}

	public final void moveBottom(int offset) {
		moveBottomTo(getBottom() + offset);
	}

	public final void moveBottomTo(int bottom) {
		int offset = bottom - getBottom();

		setHeight(getHeight() + offset);
	}

	public final int getWidth() {
		return _width;
	}

	public final void setWidth(int width) {
		this._width = width;
	}

	public final int getHeight() {
		return _height;
	}

	public final void setHeight(int height) {
		this._height = height;
	}

	public final void setLeftTop(int left, int top) {
		this._left = left;
		this._top = top;
	}

	public final void setSize(int width, int height) {
		this._width = width;
		this._height = height;
	}

	public final void setRect(int left, int top, int width, int height) {
		this._left = left;
		this._top = top;
		this._width = width;
		this._height = height;
	}

	public final boolean contains(int x, int y) {
		int attrLeft = getLeft();
		int attrTop = getTop();
		int attrRight = getRight();
		int attrBottom = getBottom();

		if (x > attrLeft && x < attrRight && y > attrTop && y < attrBottom)
			return true;
		else
			return false;
	}
	
	public final boolean contains(float x, float y) {
		return contains((int)x, (int)y);
	}

	public final boolean contains(Point point) {
		return contains(point.getX(), point.getY());
	}

	public final boolean contains(Rect rect) {
		int attrLeft = getLeft();
		int attrTop = getTop();
		int attrRight = getRight();
		int attrBottom = getBottom();

		int left = rect.getLeft();
		int top = rect.getTop();
		int right = rect.getRight();
		int bottom = rect.getBottom();

		if (left > attrLeft && right < attrRight && top > attrTop && bottom < attrBottom)
			return true;
		else
			return false;
	}

	public final void offset(int dx, int dy) {
		setLeft(getLeft() + dx);
		setTop(getTop() + dy);
	}

	public final void offset(Size offset) {
		offset(offset.getWidth(), offset.getHeight());
	}

	@Override
	public final String toString() {
		String result = String.format("(x = %d, y = %d, width = %d, height = %d)", getLeft(), getTop(), getWidth(), getHeight());
		return result;
	}

	@Override
	public final Rect clone() {
		Rect cloned = new Rect(new Rect());
		copyTo(cloned);
		return cloned;
	}

	public final void copyTo(Rect rect) {
		assert rect != null;
		rect.setLeft(_left);
		rect.setTop(_top);
		rect.setWidth(_width);
		rect.setHeight(_height);
	}

	public final boolean isSizePositive() {
		return _width > 0 && _height > 0;
	}

	public void intersect(Rect rect) {
		if (rect == null) {
			return;
		}

		int dest_x, dest_y;
		int dest_w, dest_h;

		dest_x = rect.getLeft() > getLeft() ? rect.getLeft() : getLeft();
		dest_y = rect.getTop() > getTop() ? rect.getTop() : getTop();
		dest_w = (rect.getRight() < getRight() ? rect.getRight() : getRight()) - dest_x;
		dest_h = (rect.getBottom() < getBottom() ? rect.getBottom() : getBottom()) - dest_y;

		if (dest_w > 0 && dest_h > 0) {
			setLeft(dest_x);
			setTop(dest_y);
			setWidth(dest_w);
			setHeight(dest_h);
		} else {
			setWidth(0);
			setHeight(0);
		}
	}

	public static Rect intersect(Rect first, Rect second) {
		if (first == null && second == null) {
			return null;
		} else if (first == null) {
			return second.clone();
		} else if (second == null) {
			return first.clone();
		} else {
			int dest_x;
			int dest_y;
			int dest_w;
			int dest_h;

			dest_x = first.getLeft() > second.getLeft() ? first.getLeft() : second.getLeft();
			dest_y = first.getTop() > second.getTop() ? first.getTop() : second.getTop();
			dest_w = (first.getRight() < second.getRight() ? first.getRight() : second.getRight()) - dest_x;
			dest_h = (first.getBottom() < second.getBottom() ? first.getBottom() : second.getBottom()) - dest_y;

			if (dest_w > 0 && dest_h > 0) {
				int dest_r = dest_x + dest_w;
				int dest_b = dest_y + dest_h;
				return Rect.fromLTRB(dest_x, dest_y, dest_r, dest_b);
			} else {
				return null;
			}
		}
	}

	public boolean intersectsWith(Rect rect) {
		if (rect == null) {
			return false;
		}

		int dest_x, dest_y;
		int dest_w, dest_h;

		dest_x = rect.getLeft() > getLeft() ? rect.getLeft() : getLeft();
		dest_y = rect.getTop() > getTop() ? rect.getTop() : getTop();
		dest_w = (rect.getRight() < getRight() ? rect.getRight() : getRight()) - dest_x;
		dest_h = (rect.getBottom() < getBottom() ? rect.getBottom() : getBottom()) - dest_y;

		if (dest_w > 0 && dest_h > 0) {
			return true;
		}

		return false;
	}

	public static Rect fromLTRB(int left, int top, int right, int bottom) {
		Rect rectLTRB = new Rect(new Rect());
		rectLTRB.setLeft(left);
		rectLTRB.setTop(top);
		int width = right - left;
		int height = bottom - top;
		rectLTRB.setWidth(width);
		rectLTRB.setHeight(height);
		return rectLTRB;
	}

	public static Rect composite(Rect first, Rect second) {
		assert (first != null);
		assert (second != null);

		int left = first.getLeft() < second.getLeft() ? first.getLeft() : second.getLeft();
		int top = first.getTop() < second.getTop() ? first.getTop() : second.getTop();
		int right = first.getRight() > second.getRight() ? first.getRight() : second.getRight();
		int bottom = first.getBottom() > second.getBottom() ? first.getBottom() : second.getBottom();

		return Rect.fromLTRB(left, top, right, bottom);
	}

	public void composite(Rect rect) {
		composite(this, rect).copyTo(this);
	}

	public boolean equalWith(Rect rt) {
		if ( null == rt) {
			return false;
		}
		if (this == rt) {
			return true;
		} else {
			return this._left == rt._left && this._top == rt._top && this._width == rt._width && this._height == rt._height;
		}
	}
	
	/**
	 * @param rt1
	 * @param rt2
	 * @return Specially, when rt1 == rt2 == null, return true.
	 */
	public static boolean equals(Rect rt1, Rect rt2) {
		if(rt1 == null) {
			if(rt2 == null) {
				return true;
			} else {
				return rt2.equalWith(rt1);
			}
		} else {
			return rt1.equalWith(rt2);
		}
	}

	public int centerX() {
		return _left + _width / 2;
	}

	public int centerY() {
		return _top + _height / 2;
	}

	// public android.graphics.RectF getSysRect(){
	// return new android.graphics.RectF(_left, _top, _left + _width, _top +
	// _height);
	// }

	public boolean existenceSrcRect() {
		if (this._left != 0 || this.getRight() != 0 || this._top != 0 || this.getBottom() != 0) {
			return true;
		}
		return false;
	}

//	public RectF toRectF() {
//		return new RectF(_left, _top, _width, _height);
//	}

	public void inflate(int cx, int cy) {
		_left -= cx;
		_top -= cy;
		_width += 2 * cx;
		_height += 2 * cy;

	}
}