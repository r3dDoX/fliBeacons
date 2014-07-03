package ch.fork.flibeacons.util;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
/**
 * Created by riba on 01.07.2014.
 */
public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {

    private final SurfaceHolder holdMe;
    private Camera theCamera;

    public ShowCamera(Context context, Camera camera) {
        super(context);
        theCamera = camera;
        holdMe = getHolder();
        holdMe.addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            theCamera.setPreviewDisplay(holder);
            theCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (theCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            theCamera.stopPreview();
            stopPreviewAndFreeCamera();
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stopPreviewAndFreeCamera() {

        if (theCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            theCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            theCamera.release();

            theCamera = null;
        }
    }
}