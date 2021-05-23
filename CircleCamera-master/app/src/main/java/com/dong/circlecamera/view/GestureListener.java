package com.dong.circlecamera.view;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.dong.circlecamera.MainActivity;

public class GestureListener implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener {

    private int verticalMinDistance = 10;
    public static String TAG="test";
    private static MainActivity mainActivity;
    private boolean isCapture=true;

    public interface UseActionToDo{
        //执行动画操作
        public void slideAction();//滑动操作
        public void doubleTab();//双击操作

    }

    public void getObj(MainActivity mainActivity){
        this.mainActivity=mainActivity;
    }

    
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d("test","listener");
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d("test","双击");

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {

        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d("test","按下");
//        mainActivity.doChange();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if(e1.getY()-e2.getY()>verticalMinDistance){
            //up
            Log.d("test", "onFling: 向上滑动");

        }else if(e2.getY()-e1.getY()>verticalMinDistance){
            //down
            Log.d("test", "onFling: 向下滑动");

        }
        mainActivity.slideAction();//切换到视频
        return true;
    }
}
