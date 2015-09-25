package com.xmel.criminalintent;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;


/**
 * Created by Admin on 06.09.2015.
 */
public class CrimeCameraFragment extends Fragment {
    private static final String TAG = "CrimeCameraFragment";

    public static final String EXTRA_PHOTO_FILENAME = "com.xmel.criminalintent.photo_filename";

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private View mProgressContainer;

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            mProgressContainer.setVisibility(View.VISIBLE);
        }
    };

    private Camera.PictureCallback mJpegCallack = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            String fileName = UUID.randomUUID().toString() + ".jpg";

            FileOutputStream os = null;
            boolean success = true;
            try {
                os = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);
                os.write(data);
            } catch (Exception e) {
                Log.e(TAG, "Error writing to file " + fileName, e);
                success = false;

            } finally {
                try {
                    if (os != null)
                        os.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing file", e);
                    success = false;
                }
            }

            if (success) {
                Log.i(TAG, "Success! JPEG saved at " + fileName);

                Intent i = new Intent();
                i.putExtra(EXTRA_PHOTO_FILENAME, fileName);
                getActivity().setResult(Activity.RESULT_OK, i);
            } else {
                getActivity().setResult(Activity.RESULT_CANCELED);
            }
            getActivity().finish();
        }
    };



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_camera, container, false);

        Button takePictureButton = (Button) v.findViewById(R.id.crime_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    mCamera.takePicture(mShutterCallback,null, mJpegCallack);
                }
            }
        });

        mProgressContainer = v.findViewById(R.id.crime_camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);
        mSurfaceView = (SurfaceView) v.findViewById(R.id.crime_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error setting up preview display", e);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                if (mCamera == null) {
                    return;
                }

                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), width, height);
                parameters.setPreviewSize(s.width, s.height);
                s = getBestSupportedSize(parameters.getSupportedPictureSizes(), width, height);
                parameters.setPictureSize(s.width, s.height);
                mCamera.setParameters(parameters);

                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    Log.e(TAG, "Could not start preview");
                    mCamera.release();
                    mCamera = null;
                }


            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        mCamera = Camera.open(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.Size getBestSupportedSize(List<Camera.Size> list, int width, int height) {
        Camera.Size bestSize = list.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size s : list) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }
}
