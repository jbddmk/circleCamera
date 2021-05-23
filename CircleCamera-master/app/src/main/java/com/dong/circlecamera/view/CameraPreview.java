package com.dong.circlecamera.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 李欢庆 on 2020/5/23.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private Activity mContext;
    private CameraListener listener;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int displayDegree = 90;

    private MediaRecorder mediaRecorder;//视频对象
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public boolean isRecording=false;
    public InsertPictureToSys insertPictureToSys;

    public GestureListener gestureListener;
    public GestureDetectorCompat compat;

    public CameraPreview(Activity context) {
        super(context);
        mContext = context;
        mCamera = Camera.open(cameraId);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        insertPictureToSys=new InsertPictureToSys(context);

        //手势监听起
        gestureListener = new GestureListener();
        compat=new GestureDetectorCompat(this.getContext(),gestureListener);
        //compat.setOnDoubleTapListener(gestureListener);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != mCamera) {
            mCamera.autoFocus(null);
        }
//        if(compat.onTouchEvent(event)){
//            return true;
//        }

       // return super.onTouchEvent(event);
        return compat.onTouchEvent(event);
    }

    public void setCameraListener(CameraListener listener) {
        this.listener = listener;
    }

    //开启录制
    public void startRecord(){
        if(isRecording){
            mediaRecorder.stop();
            releaseMediaRecorder();
            mCamera.lock();
            isRecording=false;
        }else{
            if(prepareVideoRecorder()){
                mediaRecorder.start();
                isRecording=true;
            }else{
                releaseMediaRecorder();
            }
        }
    }

    //关闭录制
    public void stopRecord(){
        mediaRecorder.stop();
        releaseMediaRecorder();
        mCamera.lock();
        isRecording=false;
    }

    public void setCaptureButtonText(Button btn,String text){
        btn.setText(text);
    }

    //准备视频录制
    public boolean prepareVideoRecorder(){

        mediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).getFile().toString());
        //加入系统相册
        insertPictureToSys.saveVideo(getOutputMediaFile(MEDIA_TYPE_VIDEO).getFile());
        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(mHolder.getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    //释放mediaRecorder
    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }


    private static ReturnVals getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCamera");
        Log.d("test",mediaStorageDir.toString());


        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("test", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        String path;
        if (type == MEDIA_TYPE_IMAGE){
            path=mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg";
            mediaFile = new File(path);
        } else if(type == MEDIA_TYPE_VIDEO) {
            path=mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4";
            mediaFile = new File(path);
        } else {
            return null;
        }
         ReturnVals res=new ReturnVals();
        res.setFile(mediaFile);
        res.setFilrname(path);
//        Log.d("test",mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4");
        return res;
    }


    /**
     * 拍照获取bitmap
     */
    public void captureImage() {
        try {
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    if (null != listener) {
                        Bitmap bitmap = rotateBitmap(BitmapFactory.decodeByteArray(data, 0, data.length),
                                displayDegree);
                        listener.onCaptured(bitmap);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (null != listener) {
                listener.onCaptured(null);
            }
        }
    }

    /**
     * 预览拍照
     */
    public void startPreview() {
        mCamera.startPreview();
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            startCamera(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            startCamera(mHolder);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void startCamera(SurfaceHolder holder) throws IOException {
        mCamera.setPreviewDisplay(holder);
        setCameraDisplayOrientation(mContext, cameraId, mCamera);

        Camera.Size preSize = getCameraSize();

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(preSize.width, preSize.height);
        parameters.setPictureSize(preSize.width, preSize.height);
        parameters.setJpegQuality(100);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    public Camera.Size getCameraSize() {
        if (null != mCamera) {
            Camera.Parameters parameters = mCamera.getParameters();
            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
            Camera.Size preSize = Util.getCloselyPreSize(true, metrics.widthPixels, metrics.heightPixels,
                    parameters.getSupportedPreviewSizes());
            return preSize;
        }
        return null;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
        releaseMediaRecorder();
    }


    /**
     * Android API: Display Orientation Setting
     * Just change screen display orientation,
     * the rawFrame data never be changed.
     */
    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayDegree = (info.orientation + degrees) % 360;
            displayDegree = (360 - displayDegree) % 360;  // compensate the mirror
        } else {
            displayDegree = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(displayDegree);
    }


    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    private Bitmap rotateBitmap(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * 释放资源
     */
    public synchronized void releaseCamera() {
        try {
            if (null != mCamera) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();//停止预览
                mCamera.release(); // 释放相机资源
                mCamera = null;
            }
            if (null != mHolder) {
                mHolder.removeCallback(this);
                mHolder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}