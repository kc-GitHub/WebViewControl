package com.vmd.webViewControl.motionDetection.image;

/**
 * @author Marco Dinacci <marco.dinacci@gmail.com>
 */
public class AndroidImage_NV21 extends AbstractAndroidImage {

	public AndroidImage_NV21(byte[] data, Size<Integer, Integer> size) {
		super(data, size);
	}

	@Override
	public int isDifferent(AndroidImage other, int pixel_threshold, int threshold) {
		
		if(!assertImage(other)) {
			return 0;
		}
		
		byte[] otherData = other.get();
		int totDifferentPixels = 0;
		
		// FIXME for the sake of making it working
		// 640x480 = 307200
		int height = 480;
		int width = 640;
		int size= height*width;
		for (int i = 0, ij=0; i < height; i++) {
			for (int j = 0; j < width; j++,ij++) {
				int pix = (0xff & ((int) mData[ij])) - 16;
				int otherPix = (0xff & ((int) otherData[ij])) - 16;
				
				if (pix < 0) pix = 0;
				if (pix > 255) pix = 255;
				if (otherPix < 0) otherPix = 0;
				if (otherPix > 255) otherPix = 255;

				if(Math.abs(pix - otherPix) >= pixel_threshold)
					totDifferentPixels++;
			}
		}
		
		if(totDifferentPixels == 0) {
			totDifferentPixels = 1;
		}
		
		return totDifferentPixels;
	}
	
}
