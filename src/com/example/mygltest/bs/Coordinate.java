package com.example.mygltest.bs;

import cn.wps.moffice.presentation.sal.drawing.Point;


public class Coordinate extends Point {
	
	private static final long serialVersionUID = -6036519899643473009L;

	public Coordinate() {
		
	}
	
	public Coordinate(int x, int y) {
		super(x, y);
	}
	
	@Override
	public int hashCode() {
		return getX()<<16 | getY();
	}
}
