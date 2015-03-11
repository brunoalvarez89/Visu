package com.ufavaloro.android.visu.draw.channel;

import android.graphics.Rect;

public abstract class ScreenElement {
	
	protected int id;
	protected float x;
	protected float y;
	protected float mHeight;
	protected float mWidth;
	protected Rect mBoundingBox;
	
	public float getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public float getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}

	public int getId() {
		return id;
	}

	public Rect getBoundingBox() {
		return mBoundingBox;
	}
	
	public void setBoundingBox(Rect boundingBox) {
		mBoundingBox = boundingBox;
	}

}
