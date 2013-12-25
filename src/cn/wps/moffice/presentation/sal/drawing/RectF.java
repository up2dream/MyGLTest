package cn.wps.moffice.presentation.sal.drawing;

import java.io.Serializable;


public class RectF implements Serializable {
	private float _left;
	private float _top;
	private float _width;
	private float _height;

	public RectF() {
		_left = 0;
		_top = 0;
		_width = 0;
		_height = 0;
	}

	public RectF(RectF rect) {
		_left = rect.getLeft();
		_top = rect.getTop();
		_width = rect.getWidth();
		_height = rect.getHeight();

	}

	public RectF(float left, float top, float width, float height) {
		this._left = left;
		this._top = top;
		this._width = width;
		this._height = height;
	}

	public final float getLeft() {

		return _left;
	}

	public final void setLeft(float left) {
		this._left = left;
	}

	public final void moveLeft(float offset) {
		moveLeftTo(this._left + offset);
	}

	public final void moveLeftTo(float left) {
		float offset = left - getLeft();
		this._left = left;
		setWidth(getWidth() - offset);
	}

	public final float getRight() {
		return _left + _width;
	}

	public final void setRight(float right) {
		_width = right - _left;
	}

	public final void moveRight(float offset) {
		moveRightTo(getRight() + offset);
	}

	public final void moveRightTo(float right) {
		float offset = right - getRight();
		setWidth(getWidth() + offset);
	}

	public final float getTop() {
		return _top;
	}

	public final void setTop(float top) {
		this._top = top;
	}

	public final void moveTop(float offset) {
		moveTopTo(_top + offset);
	}

	public final void moveTopTo(float top) {
		float offset = top - getTop();
		setTop(top);
		setHeight(getHeight() - offset);
	}

	public final float getBottom() {
		return _top + _height;
	}

	public final void setBottom(float bottom) {
		_height = bottom - _top;
	}

	public final void moveBottom(float offset) {
		moveBottomTo(getBottom() + offset);
	}

	public final void moveBottomTo(float bottom) {
		float offset = bottom - getBottom();

		setHeight(getHeight() + offset);
	}

	public final float getWidth() {
		return _width;
	}

	public final void setWidth(float width) {
		this._width = width;
	}

	public final float getHeight() {
		return _height;
	}

	public final void setHeight(float height) {
		this._height = height;
	}

	public final void setLeftTop(float left, float top) {
		this._left = left;
		this._top = top;
	}

	public final void setSize(float width, float height) {
		this._width = width;
		this._height = height;
	}

	public final void setRect(float left, float top, float width, float height) {
		this._left = left;
		this._top = top;
		this._width = width;
		this._height = height;
	}

	public final boolean contains(float x, float y) {
		float attrLeft = getLeft();
		float attrTop = getTop();
		float attrRight = getRight();
		float attrBottom = getBottom();

		if (x > attrLeft && x < attrRight && y > attrTop && y < attrBottom)
			return true;
		else
			return false;
	}

	public final boolean contains(PointF point) {
		return contains(point.getX(), point.getY());
	}
	
	public final boolean contains(Point point) {
		return contains(point.getX(), point.getY());
	}

	public final boolean contains(RectF rect) {
		float attrLeft = getLeft();
		float attrTop = getTop();
		float attrRight = getRight();
		float attrBottom = getBottom();

		float left = rect.getLeft();
		float top = rect.getTop();
		float right = rect.getRight();
		float bottom = rect.getBottom();

		if (left > attrLeft && right < attrRight && top > attrTop && bottom < attrBottom)
			return true;
		else
			return false;
	}

	public final void offset(float dx, float dy) {
		setLeft(getLeft() + dx);
		setTop(getTop() + dy);
	}

	public final void offset(SizeF offset) {
		offset(offset.getWidth(), offset.getHeight());
	}

	@Override
	public final String toString() {
		String result = String.format("(x = %f, y = %f, width = %f, height = %f)", getLeft(), getTop(), getWidth(), getHeight());
		return result;
	}

	@Override
	public final RectF clone() {
		RectF cloned = new RectF(new RectF());
		copyTo(cloned);
		return cloned;
	}

	public final void copyTo(RectF rect) {
		assert rect != null;
		rect.setLeft(_left);
		rect.setTop(_top);
		rect.setWidth(_width);
		rect.setHeight(_height);
	}

	public final boolean isSizePositive() {
		return _width > 0 && _height > 0;
	}

	public void intersect(RectF rect) {
		if (rect == null) {
			return;
		}

		float dest_x, dest_y;
		float dest_w, dest_h;

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

	public static RectF intersect(RectF first, RectF second) {
		if (first == null && second == null) {
			return null;
		} else if (first == null) {
			return second.clone();
		} else if (second == null) {
			return first.clone();
		} else {
			float dest_x;
			float dest_y;
			float dest_w;
			float dest_h;

			dest_x = first.getLeft() > second.getLeft() ? first.getLeft() : second.getLeft();
			dest_y = first.getTop() > second.getTop() ? first.getTop() : second.getTop();
			dest_w = (first.getRight() < second.getRight() ? first.getRight() : second.getRight()) - dest_x;
			dest_h = (first.getBottom() < second.getBottom() ? first.getBottom() : second.getBottom()) - dest_y;

			if (dest_w > 0 && dest_h > 0) {
				float dest_r = dest_x + dest_w;
				float dest_b = dest_y + dest_h;
				return RectF.fromLTRB(dest_x, dest_y, dest_r, dest_b);
			} else {
				return null;
			}
		}
	}

	public boolean intersectsWith(RectF rect) {
		if (rect == null) {
			return false;
		}

		float dest_x, dest_y;
		float dest_w, dest_h;

		dest_x = rect.getLeft() > getLeft() ? rect.getLeft() : getLeft();
		dest_y = rect.getTop() > getTop() ? rect.getTop() : getTop();
		dest_w = (rect.getRight() < getRight() ? rect.getRight() : getRight()) - dest_x;
		dest_h = (rect.getBottom() < getBottom() ? rect.getBottom() : getBottom()) - dest_y;

		if (dest_w > 0 && dest_h > 0) {
			return true;
		}

		return false;
	}

	public static RectF fromLTRB(float left, float top, float right, float bottom) {
		RectF rectLTRB = new RectF(new RectF());
		rectLTRB.setLeft(left);
		rectLTRB.setTop(top);
		float width = right - left;
		float height = bottom - top;
		rectLTRB.setWidth(width);
		rectLTRB.setHeight(height);
		return rectLTRB;
	}

	public static RectF composite(RectF first, RectF second) {
		assert (first != null);
		assert (second != null);

		float left = first.getLeft() < second.getLeft() ? first.getLeft() : second.getLeft();
		float top = first.getTop() < second.getTop() ? first.getTop() : second.getTop();
		float right = first.getRight() > second.getRight() ? first.getRight() : second.getRight();
		float bottom = first.getBottom() > second.getBottom() ? first.getBottom() : second.getBottom();

		return RectF.fromLTRB(left, top, right, bottom);
	}

	public void composite(RectF rect) {
		composite(this, rect).copyTo(this);
	}

	public boolean equalWith(RectF rt) {
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
	public static boolean equals(RectF rt1, RectF rt2) {
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
	
	public float centerX() {
		return _left + _width / 2;
	}

	public float centerY() {
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

	public Rect toRect() {
		int l = (int) _left;
		int t = (int) _top;
		int w = (int) _width;
		int h = (int) _height;
		return new Rect(l, t, w, h);
	}

	public void inflate(float cx, float cy) {
		_left -= cx;
		_top -= cy;
		_width += 2 * cx;
		_height += 2 * cy;
	}

}