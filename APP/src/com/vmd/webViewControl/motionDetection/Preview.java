package com.vmd.webViewControl.motionDetection;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Extends SurfaceView to preview the Camera frames.
 * 
 * @author Marco Dinacci <marco.dinacci@gmail.com>
 * @see http
 *      ://developer.android.com/resources/samples/ApiDemos/src/com/example/android
 *      /apis/graphics/CameraPreview.html
 */
public class Preview extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "CameraView";
	private static final String PREFS_NAME = "prefs_camera";
	private final static String MOTION_DETECTION_KEY = "motion_detection_active";

	// Available from API level 9
	private static final String FOCUS_MODE_CONTINUOS_VIDEO = "continuos-video";

	private SurfaceHolder mHolder;
	private Camera mCamera;
	private Context mContext;

	private CameraCallback mCameraCallback;
	private boolean mMotionDetectionActive = true;

	private com.vmd.webViewControl.ActivityMain.motionDetectCallback mCallback;

	public Preview(Context context, com.vmd.webViewControl.ActivityMain.motionDetectCallback motionDetectCallback) {
		super(context);

		mCallback = motionDetectCallback;
		mContext = context;

		// Install a SurfaceHolder.Callback in order to receive notifications when the underlying surface is created and destroyed
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		int cameraId = findFrontFacingCamera();

		mCamera = Camera.open(cameraId);
//		mCamera.setDisplayOrientation(90);

		if(mCamera == null) {
			 // TODO show Toast
			throw new RuntimeException("Camera is null");
		}
		
		configure(mCamera);
		
		if(mMotionDetectionActive) {
			mCameraCallback = new CameraCallback(mContext, mCamera, mCallback);
			mCamera.setPreviewCallback(mCameraCallback);
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException exception) {
				Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
				closeCamera();
			}
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		closeCamera();
	}

	/**
	 * Closing camera and freeing its resources
	 */
	private void closeCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	private void configure(Camera camera) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		Camera.Parameters params = camera.getParameters();

		params.set("jpeg-quality", prefs.getInt("pim.image-quality", 75));

		// Configure image format
		List<Integer> formats = params.getSupportedPictureFormats();
		if (formats.contains(PixelFormat.RGB_565))
			params.setPictureFormat(PixelFormat.RGB_565);
		else
			params.setPictureFormat(PixelFormat.JPEG);

		// FIXME Configure picture size, choose the smallest supported for now
		List<Size> sizes = params.getSupportedPictureSizes();
		Camera.Size size = sizes.get(0);// sizes.get(sizes.size()-1);
		params.setPictureSize(size.width, size.height);

		/*
		 * A wrong config cause the screen to go black on a Milestone so I just leave the default one. 
		 * sizes = params.getSupportedPreviewSizes(); 
		 * smallestSize = sizes.get(0);
		 * params.setPreviewSize(smallestSize.width, smallestSize.height);
		 */

		// Deactivate Flash
		params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

		// Autofocus to FOCUS_MODE_INFINITY
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);

//		camera.setParameters(params);

		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "Picture format: " + params.getPictureFormat());
			Log.d(TAG, "Picture size: " + params.getPictureSize().width + " - " + params.getPictureSize().height);
			Log.d(TAG, "Preview size: " + params.getPreviewSize().width + " - " + params.getPreviewSize().height);
		}
	}

	private int findFrontFacingCamera() {
	    int cameraId = -1;
	    // Search for the front facing camera
	    int numberOfCameras = Camera.getNumberOfCameras();
	    for (int i = 0; i < numberOfCameras; i++) {
	      CameraInfo info = new CameraInfo();
	      Camera.getCameraInfo(i, info);
	      if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
//	        Log.d(DEBUG_TAG, "Camera found");
	        cameraId = i;
	        break;
	      }
	    }
	    return cameraId;
	  }
}

final class CameraCallback implements Camera.PreviewCallback, Camera.PictureCallback {
	private static final String TAG = "CameraCallback";

	private MotionDetection mMotionDetection;
	
	public CameraCallback(Context ct, Camera camera, com.vmd.webViewControl.ActivityMain.motionDetectCallback callback) {
		mMotionDetection = new MotionDetection(
			ct.getSharedPreferences(MotionDetection.PREFS_NAME, Context.MODE_PRIVATE),
			callback
		);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (mMotionDetection.detect(data)) {
//			Log.i(TAG, "Motion Detected");
		}
	}

}