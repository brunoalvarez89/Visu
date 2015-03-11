package com.ufavaloro.android.visu.draw.channel;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ScreenBitmap extends ScreenElement {
	
	private Bitmap mBitmap;

	
	public ScreenBitmap(Context context, int bitmapCode) {
		mBitmap = BitmapFactory.decodeResource(context.getResources(), bitmapCode);
	}
	
	public void scale(double height, double width) {
		mBitmap = Bitmap.createScaledBitmap(mBitmap, (int) width, (int) height, false);
		mHeight = (float) height;
		mWidth = (float) width;
	}
	
	public Bitmap getBitmap() {
		return mBitmap;
	}

}
