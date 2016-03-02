package com.vmd.webViewControl.motionDetection.image;

import android.util.Log;

/**
 * Generic class for (simple) image processing. 
 * It doesn't subclass Bitmap in android.graphics as it's declared final.
 * 
 * TODO refactor: create various classes, one for each PixelFormat and implement
 * the abstract operations defined here instead of having an ugly switch case
 * 
 */
public abstract class AbstractAndroidImage implements AndroidImage {
	
	private final String TAG = "AbstractAndroidImage";
	protected byte[] mData;
	protected Size<Integer, Integer> mSize;

	public AbstractAndroidImage(byte[] data, Size<Integer, Integer> size) {
		mData = data;
		mSize = size;
	}

	protected boolean assertImage(AndroidImage other) {
		boolean result = true;
		
		byte[] otherData = other.get();
		
		if(mData.length != otherData.length) {
			Log.e(TAG, "Data length between images to compare is different");
			// data length must be the same
			result = false;
		}
		
		/* FIXME
		if(other.getClass() == this.getClass()) {
			Log.e(TAG, "Cannot compare two different implementations: " + 
					getClass().getName() + " and " + other.getClass().getName());
			result = false;
		}*/
		
		otherData = null;
		
//		Log.d(TAG, "Images are compatible: " + result);
		
		return result;
	}
		
	@Override
	public byte[] get() {
		return mData;
	}
}
