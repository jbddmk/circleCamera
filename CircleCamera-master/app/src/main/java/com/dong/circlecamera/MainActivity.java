package com.dong.circlecamera;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dong.circlecamera.view.CameraListener;
import com.dong.circlecamera.view.CameraPreview;
import com.dong.circlecamera.view.CircleCameraLayout;
import com.dong.circlecamera.view.GestureListener;
import com.dong.circlecamera.view.Util;

/**
 * @author 李欢庆
 * @create 2020/5/25
 * @Describe 自定义圆形拍照、解决非全屏(竖屏)下预览相机拉伸问题。
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, GestureListener.UseActionToDo {

    private static final int PERMISSION_REQUEST_CODE = 10;
    private String[] mPermissions = {Manifest.permission.CAMERA};

    private CircleCameraLayout rootLayout;
    private ImageView imageView;
    private CameraPreview cameraPreview;
    private boolean hasPermissions;
    private boolean resume = false;//解决home键黑屏问题
    public int RECORD_AUDIO_REQUEST_CODE;


    public Button btn_photo,btn_video;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_photo=findViewById(R.id.bt_take_photo);
        btn_video=findViewById(R.id.bt_re_take_photo);
        btn_photo.setOnClickListener(this);
        btn_video.setOnClickListener(this);
        rootLayout = findViewById(R.id.rootLayout);
        imageView = findViewById(R.id.image);

        GestureListener gestureListener=new GestureListener();
        gestureListener.getObj(this);//传入监听器

        //权限检查
        if (Util.checkPermissionAllGranted(this, mPermissions)) {
            hasPermissions = true;
        } else {
            ActivityCompat.requestPermissions(this, mPermissions, PERMISSION_REQUEST_CODE);
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (hasPermissions) {
            startCamera();
            resume = true;
        }
    }

    private void startCamera() {
        if (null != cameraPreview) cameraPreview.releaseCamera();
        cameraPreview = new CameraPreview(this);
        rootLayout.removeAllViews();
        rootLayout.setCameraPreview(cameraPreview);
        if (!hasPermissions || resume) {
            rootLayout.startView();
        }
        cameraPreview.setCameraListener(new CameraListener() {
            @Override
            public void onCaptured(Bitmap bitmap) {
                if (null != bitmap) {
                    imageView.setImageBitmap(bitmap);
                    Toast.makeText(MainActivity.this, "拍照成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "拍照失败", Toast.LENGTH_SHORT).show();
                }
                cameraPreview.startPreview();//新加
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (null == cameraPreview) return;
        switch (v.getId()) {
            case R.id.bt_take_photo:
                cameraPreview.captureImage();//抓取照片
                break;
            case R.id.bt_re_take_photo://视频录制
//                cameraPreview.startPreview();
                cameraPreview.startRecord();
                if(cameraPreview.isRecording){
                    cameraPreview.setCaptureButtonText((Button) v,"stop");
                }else{
                    cameraPreview.setCaptureButtonText((Button) v,"start");
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != cameraPreview) {
            cameraPreview.releaseCamera();
        }
        rootLayout.release();
    }

    /**
     * 申请权限结果返回处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;
            for (int grant : grantResults) {  // 判断是否所有的权限都已经授予了
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) { // 所有的权限都授予了
                startCamera();
            } else {// 提示需要权限的原因
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("拍照需要允许权限, 是否再次开启?")
                        .setTitle("提示")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, mPermissions, PERMISSION_REQUEST_CODE);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                builder.create().show();
            }
        }
    }

    //执行上下滑动需要的动画及切换动作
    @Override
    public void slideAction() {
        Log.d("test","this is activity ");
        if(cameraPreview.isRecording){
            cameraPreview.stopRecord();
            cameraPreview.setCaptureButtonText(btn_video,"start");
        }else{
            cameraPreview.startRecord();
            cameraPreview.setCaptureButtonText(btn_video,"stop");
        }

    }

    //双击操作
    @Override
    public void doubleTab() {

    }

}
