package com.ummah.app;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class AvatarGLSurfaceView extends GLSurfaceView {

    public AvatarGLRenderer renderer;
    private float lastX;
    private boolean dragging = false;
    private ScaleGestureDetector scaleDetector;

    public AvatarGLSurfaceView(Context ctx) {
        super(ctx);
        setEGLContextClientVersion(2);
        renderer = new AvatarGLRenderer();
        setRenderer(renderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        scaleDetector = new ScaleGestureDetector(ctx, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector d) {
                float factor = d.getScaleFactor();
                renderer.zoom = Math.max(1.5f, Math.min(8f, renderer.zoom / factor));
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        scaleDetector.onTouchEvent(e);

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = e.getX();
                dragging = true;
                renderer.autoRotate = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    float dx = e.getX() - lastX;
                    renderer.rotationY += dx * 0.5f;
                    lastX = e.getX();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                renderer.autoRotate = true;
                break;
        }
        return true;
    }
}
