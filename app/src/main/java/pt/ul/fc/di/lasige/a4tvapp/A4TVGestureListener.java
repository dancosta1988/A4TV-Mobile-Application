package pt.ul.fc.di.lasige.a4tvapp;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by LaSIGE on 11/08/2017.
 */
public class A4TVGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    //private final GestureDetector gestureDetector;
    private static final int SWIPE_THRESHOLD = 400;
    private static final int SWIPE_VELOCITY_THRESHOLD = 50;

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        onDoubletap();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        onSingleTap();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float v, float v1) {
        float diffY = e2.getY() - e1.getY();
        if (diffY > SWIPE_THRESHOLD){
            onScrollDown();
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        holdingDown();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffX) > Math.abs(diffY)) {

                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    result = true;
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }


    public void onDoubletap(){
    }

    public void holdingDown(){
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

    public void onSingleTap() {
    }

    public void onScrollDown() {
    }




}
