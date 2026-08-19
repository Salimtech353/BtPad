package com.example;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Custom GestureDetector.SimpleOnGestureListener handling touchpad interactions:
 * - Finger drag: Relative cursor movement (dx, dy)
 * - Single tap: Left click (onSingleTapConfirmed)
 * - Double tap: Double click (onDoubleTap)
 * - Long press: Right click (onLongPress)
 */
public class TouchpadGestureListener extends GestureDetector.SimpleOnGestureListener {

    /**
     * Interface for dispatching touchpad gestures to the hosting activity or controller.
     */
    public interface TouchpadCallback {
        /**
         * Invoked when the user drags a finger on the touchpad to move the mouse pointer.
         *
         * @param dx Relative delta along X-axis
         * @param dy Relative delta along Y-axis
         */
        void onMouseMove(float dx, float dy);

        /**
         * Invoked on a single tap confirmed gesture (Left Click).
         */
        void onLeftClick();

        /**
         * Invoked on a double tap gesture (Double Click).
         */
        void onDoubleClick();

        /**
         * Invoked on a long press gesture (Right Click).
         */
        void onRightClick();
    }

    private final TouchpadCallback callback;
    private float sensitivity = 1.2f;

    public TouchpadGestureListener(TouchpadCallback callback) {
        this.callback = callback;
    }

    public void setSensitivity(float sensitivity) {
        if (sensitivity > 0.1f) {
            this.sensitivity = sensitivity;
        }
    }

    public float getSensitivity() {
        return sensitivity;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // Return true to accept touch stream
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (callback != null) {
            // In Android GestureDetector:
            // distanceX = previousX - currentX
            // distanceY = previousY - currentY
            // Cursor movement requires dx = (currentX - previousX) = -distanceX
            float dx = -distanceX * sensitivity;
            float dy = -distanceY * sensitivity;
            callback.onMouseMove(dx, dy);
        }
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (callback != null) {
            callback.onLeftClick();
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (callback != null) {
            callback.onDoubleClick();
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (callback != null) {
            callback.onRightClick();
        }
    }
}
