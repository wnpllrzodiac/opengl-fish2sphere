package com.jimcosmos.opengl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.jimcosmos.opengl.GLUtils.GLRenderer;
import java.io.FileOutputStream;

/**
 * Created by guobichuan on 8/16/16.
 */

public class GLView extends GLSurfaceView {

    private final String TAG = "VideoPlayGLView";
    private GLRenderer mRenderer;
    private ScaleGestureDetector mScaleDetector;

    private int width;
    private int height;

    private float mTouchDownX;
    private float mTouchDownY;
    private int mTouchType;
    private final int DRAG = 1;
    private final int SCALE = 2;

    public GLView(Context context) {
        super(context);

        mRenderer = new GLRenderer(context, this);
        this.mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        this.setEGLContextClientVersion(2);
        this.setRenderer(mRenderer);
        this.setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mRenderer.scaleByFloat(detector.getScaleFactor());
            requestRender();
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        final float touchX = event.getX();
        final float touchY = event.getY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = touchX;
                mTouchDownY = touchY;
                mTouchType = DRAG;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mTouchType = SCALE;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchType == DRAG) {
                    float dx = touchX - mTouchDownX;
                    float dy = touchY - mTouchDownY;
                    mTouchDownX = touchX;
                    mTouchDownY = touchY;
                    width = this.getWidth();
                    height = this.getHeight();
                    mRenderer.transByPointF(new PointF(dx / width * 2, dy / height * 2));
                    requestRender();
                }

                break;
        }

        return true;
    }
}
