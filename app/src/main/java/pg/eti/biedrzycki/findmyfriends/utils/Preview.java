package pg.eti.biedrzycki.findmyfriends.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import pg.eti.biedrzycki.findmyfriends.CameraPreview;

class Preview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera camera;
    CameraPreview cameraPreview;

    Preview(Context context) {
        super(context);

        cameraPreview = (CameraPreview) context;

        mHolder = getHolder();

        mHolder.addCallback(this);

        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public Preview(Context context, AttributeSet attrs) {
        super(context, attrs);

        cameraPreview = (CameraPreview) context;

        mHolder = getHolder();

        mHolder.addCallback(this);

        //musi być mimo że jest w dokumentacji jest deprecated
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (camera != null) {
                try {
                    camera.stopPreview();
                } catch (Exception ignore) {

                }

                try {
                    camera.release();
                } catch (Exception ignore) {

                }

                camera = null;
            }

            camera = Camera.open();
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(holder);
        } catch (Exception ex) {
            try {
                if (camera != null) {
                    try {
                        camera.stopPreview();
                    } catch (Exception ignore) {

                    }

                    try {
                        camera.release();
                    } catch (Exception ignore) {

                    }

                    camera = null;
                }
            } catch (Exception ignore) {

            }
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            if (camera != null) {
                try {
                    camera.stopPreview();
                } catch (Exception ignore) {

                }

                try {
                    camera.release();
                } catch (Exception ignore) {

                }

                camera = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            try {
                List<Camera.Size> supportedSizes = null;
                supportedSizes = Compatibility.getSupportedPreviewSizes(parameters);

                Iterator<Camera.Size> itr = supportedSizes.iterator();
                while(itr.hasNext()) {
                    Camera.Size element = itr.next();
                    element.width -= w;
                    element.height -= h;
                }
                Collections.sort(supportedSizes, new ResolutionsOrder());
                parameters.setPreviewSize(w + supportedSizes.get(supportedSizes.size()-1).width, h + supportedSizes.get(supportedSizes.size()-1).height);
            } catch (Exception ex) {


                parameters.setPreviewSize(cameraPreview.screenWidth, cameraPreview.screenHeight);
            }

            camera.setParameters(parameters);
            camera.startPreview();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class ResolutionsOrder implements java.util.Comparator<Camera.Size> {
    public int compare(Camera.Size left, Camera.Size right) {

        return Float.compare(left.width + left.height, right.width + right.height);
    }
}