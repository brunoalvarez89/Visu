package com.ufavaloro.android.visu.draw;

import com.ufavaloro.android.visu.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;

public class BitmapManager{
	
	private int mIconsWidth;
	private int mIconsHeight;
	private int mIconsLeftPadding;
	private int mIconsUpperPadding;
	private Context mContext;
	
	public BitmapManager(Context context) {
		mContext = context;
	}

	public int getIconsWidth() {
		return mIconsWidth;
	}

	
	public void setIconsWidth(int mIconsWidth) {
		this.mIconsWidth = mIconsWidth;
	}

	public int getIconsHeight() {
		return mIconsHeight;
	}

	public void setIconsHeight(int mIconsHeight) {
		this.mIconsHeight = mIconsHeight;
	}

	public int getIconsLeftPadding() {
		return mIconsLeftPadding;
	}

	public void setIconsLeftPadding(int mIconsLeftPadding) {
		this.mIconsLeftPadding = mIconsLeftPadding;
	}

	public int getIconsUpperPadding() {
		return mIconsUpperPadding;
	}

	public void setIconsUpperPadding(int mIconsUpperPadding) {
		this.mIconsUpperPadding = mIconsUpperPadding;
	}

	public void setup() {
		// Ícono de nuevo estudio
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.new_study);
		setNewStudyIconX(getIconsLeftPadding());
		setNewStudyIconY(getIconsUpperPadding());
		setNewStudyIcon(Bitmap.createScaledBitmap(bitmap, getIconsWidth(), getIconsHeight(), false));

		// Ícono de configurar canales
		bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.configure_channels);
		setConfigureChannelsIconX(getIconsLeftPadding());
		setConfigureChannelsIconY(getIconsUpperPadding() + getNewStudyIconY());
		setConfigureChannelsIcon(Bitmap.createScaledBitmap(bitmap, getIconsWidth(), getIconsHeight(), false));
		
		// Ícono de parar estudio
		bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.stop_study);
		setStopStudyIcon(Bitmap.createScaledBitmap(bitmap, getIconsWidth(), getIconsHeight(), false));
		setStopStudyIconX(getIconsLeftPadding());
		setStopStudyIconY(getIconsUpperPadding() + getConfigureChannelsIconY());
	}
	
	

/*****************************************************************************************
* New Study Icon																	     *
*****************************************************************************************/
	private Bitmap mNewStudyIcon;
	private int mNewStudyIconX;
	private int mNewStudyIconY;

	public Bitmap getNewStudyIcon() {
		return mNewStudyIcon;
	}
	
	public void setNewStudyIcon(Bitmap mNewStudyIcon) {
		this.mNewStudyIcon = mNewStudyIcon;
	}
	
	public int getNewStudyIconX() {
		return mNewStudyIconX;
	}
	
	public void setNewStudyIconX(int mNewStudyIconX) {
		this.mNewStudyIconX = mNewStudyIconX;
	}
	
	public int getNewStudyIconY() {
		return mNewStudyIconY;
	}
	
	public void setNewStudyIconY(int mNewStudyIconY) {
		this.mNewStudyIconY = mNewStudyIconY;
	}

/*****************************************************************************************
* Configure Channels Icon															     *
*****************************************************************************************/
	private Bitmap mConfigureChannelsIcon;
	private int mConfigureChannelsIconX;
	private int mConfigureChannelsIconY;

	public Bitmap getConfigureChannelsIcon() {
		return mConfigureChannelsIcon;
	}
	
	public void setConfigureChannelsIcon(Bitmap mConfigureChannelsIcon) {
		this.mConfigureChannelsIcon = mConfigureChannelsIcon;
	}
	
	public int getConfigureChannelsIconX() {
		return mConfigureChannelsIconX;
	}
	
	public void setConfigureChannelsIconX(int mConfigureChannelsIconX) {
		this.mConfigureChannelsIconX = mConfigureChannelsIconX;
	}
	
	public int getConfigureChannelsIconY() {
		return mConfigureChannelsIconY;
	}
	
	public void setConfigureChannelsIconY(int mConfigureChannelsIconY) {
		this.mConfigureChannelsIconY = mConfigureChannelsIconY;
	}

/*****************************************************************************************
* Stop Study Icon																     *
*****************************************************************************************/
	private Bitmap mStopStudyIcon;
	private int mStopStudyIconX;
	private int mStopStudyIconY;

	public Bitmap getStopStudyIcon() {
		return mStopStudyIcon;
	}

	public void setStopStudyIcon(Bitmap mStopStudyIcon) {
		this.mStopStudyIcon = mStopStudyIcon;
	}

	public int getStopStudyIconX() {
		return mStopStudyIconX;
	}

	public void setStopStudyIconX(int mStopStudyIconX) {
		this.mStopStudyIconX = mStopStudyIconX;
	}

	public int getStopStudyIconY() {
		return mStopStudyIconY;
	}

	public void setStopStudyIconY(int mStopStudyIconY) {
		this.mStopStudyIconY = mStopStudyIconY;
	}

/*****************************************************************************************
* Background Logo																     *
*****************************************************************************************/
	private Bitmap mBackgroundLogo;
	private int mBackgroundLogoWidth;
	private int mBackgroundLogoHeight;
	private int mBackgroundLogoX;
	private int mBackgroundLogoY;
	
	public Bitmap getBackgroundLogo() {
		return mBackgroundLogo;
	}

	public void setBackgroundLogo(Bitmap mBackgroundLogo) {
		this.mBackgroundLogo = mBackgroundLogo;
	}

	public int getBackgroundLogoWidth() {
		return mBackgroundLogoWidth;
	}

	public void setBackgroundLogoWidth(int mBackgroundLogoWidth) {
		this.mBackgroundLogoWidth = mBackgroundLogoWidth;
	}

	public int getBackgroundLogoHeight() {
		return mBackgroundLogoHeight;
	}

	public void setBackgroundLogoHeight(int mBackgroundLogoHeight) {
		this.mBackgroundLogoHeight = mBackgroundLogoHeight;
	}

	public int getBackgroundLogoX() {
		return mBackgroundLogoX;
	}

	public void setBackgroundLogoX(int mBackgroundLogoX) {
		this.mBackgroundLogoX = mBackgroundLogoX;
	}

	public int getBackgroundLogoY() {
		return mBackgroundLogoY;
	}

	public void setBackgroundLogoY(int mBackgroundLogoY) {
		this.mBackgroundLogoY = mBackgroundLogoY;
	}


}
