package com.ufavaloro.android.visu.draw.channel;



import android.graphics.Paint;
import android.graphics.Rect;

public class Label extends ScreenElement {
	
	private String mText;
	private int mTextSize;
	
	public Label(int id, float x, float y, String mText) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.mText = mText;
		mBoundingBox = new Rect();
	}
	
	public Label(double value) {
		mText = String.valueOf(value);
		mBoundingBox = new Rect();
	}
	
	public Label(int value) {
		mText = String.valueOf(value);
		mBoundingBox = new Rect();
	}

	public Label() {
		// TODO Auto-generated constructor stub
	}

	public void setUnits(String units) {
		mText = mText + units;
	}
	
	public void setText(String mTexto) {
		this.mText = mTexto;
	}
	
	public String getText() {
		return mText;
	}
	
	public void setTextSize(int mTextSize) {
		this.mTextSize = mTextSize;
		Paint p = new Paint();
		Rect rect = new Rect();
		p.setTextSize(mTextSize);
		p.getTextBounds(mText, 0, mText.length(), rect);
		mBoundingBox = rect;

	}
	
	
	public float getTextSize() {
		return mTextSize;
	}
}
