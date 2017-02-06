package com.pixsee.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Tudor Pop on 2/2/2017.
 */

class CameraConfiguration {
	protected Camera mCamera;

	public CameraConfiguration(final Camera camera) {
		mCamera = camera;
	}

	public void configureRotation(int cameraFacing, Activity activity) {
		int rotation;
		if (activity != null)
			rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		else
			rotation = Surface.ROTATION_0;
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break; // Natural orientation
			case Surface.ROTATION_90:
				degrees = 90;
				break; // Landscape left
			case Surface.ROTATION_180:
				degrees = 180;
				break;// Upside down
			case Surface.ROTATION_270:
				degrees = 270;
				break;// Landscape right
		}
		Camera.CameraInfo camInfo = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraFacing, camInfo);
		int cameraRotationOffset = camInfo.orientation;

		int displayRotation;
		if (isFrontFacingCam(cameraFacing)) {
			displayRotation = (cameraRotationOffset + degrees) % 360;
			displayRotation = (360 - displayRotation) % 360; // compensate
			// the
			// mirror
		} else { // back-facing
			displayRotation = (cameraRotationOffset - degrees + 360) % 360;
		}

		Log.v(TAG, "rotation cam / phone = displayRotation: " + cameraRotationOffset + " / " + degrees + " = " + displayRotation);

		mCamera.setDisplayOrientation(displayRotation);

		int rotate;
		if (isFrontFacingCam(cameraFacing)) {
			rotate = (360 + cameraRotationOffset + degrees) % 360;
		} else {
			rotate = (360 + cameraRotationOffset - degrees) % 360;
		}

		Log.v(TAG, "screenshot rotation: " + cameraRotationOffset + " / " + degrees + " = " + rotate);

		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setRotation(rotate);
		mCamera.setParameters(parameters);
	}

	protected boolean isFrontFacingCam(int cameraFacing) {
		return cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT;
	}

	public void configurePreviewSize(final TextureView preview, final int orientation) {
		// We need to make sure that our preview and recording video size are supported by the
		// camera. Query camera to find all the sizes and choose the optimal size given the
		// dimensions of our preview surface.
		Camera.Parameters parameters = mCamera.getParameters();
		List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
		Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(Collections.<Camera.Size>emptyList(),
				mSupportedPreviewSizes, preview.getWidth(), preview.getHeight(), orientation);

		// likewise for the camera object itself.
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);
		mCamera.setParameters(parameters);
	}

	public void setZoom(int zoom) {
		Camera.Parameters parameters = mCamera.getParameters();
		if (!parameters.isZoomSupported())
			return;
		if (zoom > parameters.getMaxZoom())
			zoom = parameters.getMaxZoom();
		parameters.setZoom(zoom);
		mCamera.setParameters(parameters);
	}

	public int getMaxZoom() {
		if (mCamera == null || mCamera.getParameters() == null)
			return 100;
		return mCamera.getParameters().getMaxZoom();
	}
}