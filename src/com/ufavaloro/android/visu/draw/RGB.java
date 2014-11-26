package com.ufavaloro.android.visu.draw;

public class RGB {
	
	private int[] rgb = new int[3];
	
	RGB(int r, int g, int b) {
		rgb[0] = r;
		rgb[1] = g;
		rgb[2] = b;
	}

	public int[] getRGB() {
		return rgb;
	}
}
