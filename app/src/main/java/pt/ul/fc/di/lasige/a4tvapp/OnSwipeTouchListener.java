package pt.ul.fc.di.lasige.a4tvapp;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by LaSIGE on 11/08/2017.
 */
public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context ctx) {
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 400;
        private static final int PINCH_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;


        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {onDoubletap(); return false;}

        @Override
        public void onLongPress(MotionEvent e) {holdingDown();}

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                System.out.println("----------------->Difference X:  " + diffX);
                System.out.println("----------------->Difference Y:  " + diffY);
                if (Math.abs(diffX) > Math.abs(diffY)) {

                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }else if(Math.abs(diffX) > PINCH_THRESHOLD && Math.abs(diffX) < SWIPE_THRESHOLD){
                        if (diffX > 0) {
                            onPinchIn();
                        } else {
                            onPinchOut();
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
                    } else if(Math.abs(diffY) > PINCH_THRESHOLD && Math.abs(diffY) < SWIPE_THRESHOLD ){
                        if (diffY > 0) {
                            onPinchOut();
                        } else {
                            onPinchIn();
                        }
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
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

    public void onPinchIn() {
    }

    public void onPinchOut() {
    }


}
