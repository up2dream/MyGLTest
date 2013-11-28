package cn.wps.moffice.presentation.sal.drawing;

import java.io.Serializable;

public class Size implements Serializable {
	private int width;
	private int height;
	private boolean empty;

	
	public Size() {
		width = 0;
		height = 0;
		empty = true;
	}

	public Size(int width, int height) {
		this.width = width;
		this.height = height;
		empty = false;
	}

	public void Dispose() {

	}

	public final boolean IsEmpty() {
		return empty;
	}

	public final int getWidth() {
		return width;
	}

	public final void setWidth(int width) {
		if (this.getWidth() != width) {
			this.width = width;
		}
	}

	public final int getHeight() {

		return height;
	}

	public final void setHeight(int height) {
		if (this.getHeight() != height) {
			this.height = height;
			empty = false;
		}
	}

	public final void SetSize(int width, int height) {
		if (this.getWidth() != width || this.getHeight() != height) {
			this.setWidth(width);
			this.setHeight(height);
			empty = false;
		}
	}

	@Override
	public final Size clone() {
		Size clonedSize = new Size();
		copyTo(clonedSize);

		return clonedSize;
	}

	public final void copyTo(Size size) {
		size.SetSize(getWidth(), getHeight());
	}

	public final boolean IsInverse() {
		return getWidth() < 0 || getHeight() < 0;
	}

	public final boolean IsZero() {
		return getWidth() == 0 && getHeight() == 0;
	}

	public final boolean IsPositive() {
		return getWidth() > 0 && getHeight() > 0;
	}

	@Override
	public String toString() {
		return String.format("(%f, %f)", getWidth(), getHeight());
	}
}
