package cn.wps.moffice.presentation.sal.drawing.color;

/**
 * 文件�?�?RgbColor.java 
 * 创建�?�?wangyuxi 
 * 维护�?�?wangyuxi 
 * 创建时间 �?2011-10-27 下午06:04:43 
 * 功能描述 �?
 */
public class Color {

	public Color(int argb) {
		this._argb = argb;
	}

	public Color() {
		//android.graphics.Color.BLACK
		this._argb = 0xFF000000;
	}
	
	public static Color BLACK() {
		return new Color(0xFF000000);
	}

	public static Color DKGRAY() {
		//android.graphics.Color.DKGRAY
		return new Color(0xff444444);
	}

	public static Color GRAY() {
		//android.graphics.Color.GRAY
		return new Color(0xff888888);
	}

	public static Color LTGRAY() {
		//android.graphics.Color.LTGRAY
		return new Color(0xffcccccc);

	}

	public static Color WHITE() {
		//android.graphics.Color.WHITE
		return new Color(0xffffffff);
	}

	public static Color RED() {
		//android.graphics.Color.RED
		return new Color(0xffff0000);
	}

	public static Color GREEN() {
		//android.graphics.Color.GREEN
		return new Color(0xff00ff00);

	}

	public static Color BLUE() {
		//android.graphics.Color.BLUE
		return new Color(0xff0000ff);

	}

	public static Color YELLOW() {
		//android.graphics.Color.YELLOW
		return new Color(0xffffff00);

	}

	public static Color CYAN() {
		//android.graphics.Color.CYAN
		return new Color(0xff00ffff);
	}

	public static Color MAGENTA() {
		//android.graphics.Color.MAGENTA
		return new Color(0xffff00ff);
	}

	public static Color TRANSPARENT() {
		return new Color(0x00000000);
	}

	public final int getR() {
		return (_argb >> 16) & 0xFF;
	}

	public final int getG() {
		return (_argb >> 8) & 0xFF;
	}

	public final int getB() {
		return _argb & 0xFF;
	}

	public final int getAlpha() {
		return _argb >>> 24;
	}

	@Override
	public final Color clone() {
		return new Color(_argb);
	}

	public static Color parse(String colorString) {
		if (colorString.charAt(0) == '#') {
			// Use a long to avoid rollovers on #ffXXXXXX
			long color = Long.parseLong(colorString.substring(1), 16);
			if (colorString.length() == 7) {
				// Set the alpha value
				color |= 0x00000000ff000000;
			} else if (colorString.length() != 9) {
				throw new IllegalArgumentException("Unknown color");
			}
			return new Color((int) color);
		}
		throw new IllegalArgumentException("Unknown color");
	}

	@Override
	public final String toString() {
		return String.format("%02x%02x%02x", getR(), getG(), getB());//# is not needed
	}

	public final int getArgb() {
		return _argb;
	}
	
	public final void setArgb(int color) {
		 _argb = color;
	}

	private int _argb;
}
