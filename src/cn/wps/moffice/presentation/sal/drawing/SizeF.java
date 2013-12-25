package cn.wps.moffice.presentation.sal.drawing;

import java.io.Serializable;

public class SizeF implements Serializable {
	private float width;
	private float height;
	private boolean empty;

	public SizeF() {
		width = 0;
		height = 0;
		empty = true;
	}

	public SizeF(float width, float height) {
		this.width = width;
		this.height = height;
		empty = false;
	}

	public SizeF(SizeF size) {
		this.width = size.width;
		this.height = size.height;
		empty = false;
	}

	public final boolean isEmpty() {
		return empty;
	}

	public final float getWidth() {
		return width;
	}

	public final void setWidth(float width) {
		if (this.getWidth() != width) {
			this.width = width;
		}
	}

	public final float getHeight() {

		return height;
	}

	public final void setHeight(float height) {
		if (this.getHeight() != height) {
			this.height = height;
			empty = false;
		}
	}

	public final void setSize(float width, float height) {
		if (this.getWidth() != width || this.getHeight() != height) {
			this.setWidth(width);
			this.setHeight(height);
			empty = false;
		}
	}

	@Override
	public final SizeF clone() {
		SizeF clonedSize = new SizeF();
		copyTo(clonedSize);

		return clonedSize;
	}

	public final void copyTo(SizeF size) {
		size.setSize(getWidth(), getHeight());
	}

	public final boolean isInverse() {
		return getWidth() < 0 || getHeight() < 0;
	}

	public final boolean isZero() {
		return getWidth() == 0 && getHeight() == 0;
	}

	public final boolean isPositive() {
		return getWidth() > 0 && getHeight() > 0;
	}

	public SizeF substract(float cx, float cy) {
		width -= cx;
		height -= cy;
		
		return this;
	}
	
	public SizeF substract(SizeF sz) {
		substract(sz.getWidth(), sz.getHeight());
		
		return this;
	}

	public SizeF add(float cx, float cy) {
		width += cx;
		height += cy;
		
		return this;
	}
	
	public SizeF add(SizeF sz) {
		add(sz.getWidth(), sz.getHeight());
		
		return this;
	}

	@Override
	public String toString() {
		return String.format("(%f, %f)", getWidth(), getHeight());
	}
}
