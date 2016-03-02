package com.vmd.webViewControl.motionDetection.image;

public interface AndroidImage {

	/** Check whether the current image is different from the given image. 
	 * A pixel is different if pixel_value - 3 < pixel_threshold < pixel_value + 3 
	 * An image is different if the total of different pixels is > than threshold.
	 * */
	public abstract int isDifferent(AndroidImage background, int pixel_threshold, int threshold);
	
	/** Access the low level data of the image. Data layout is 
	 * implementation dependent 
	 * */
	public abstract byte[] get();

}