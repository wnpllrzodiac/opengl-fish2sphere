package com.jimcosmos.opengl.GLUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by guobichuan on 7/20/16.
 */
public class GLRenderer implements GLSurfaceView.Renderer {

    public static final String TAG = GLRenderer.class.getSimpleName();

    private GLProgram prog;
    private GLSurfaceView view;

    private int width;
    private int height;

    private int a_texCoord_h;
    private int a_position_h;
    private int u_texture_h;
    private int u_MVPMatrix_h;

    private FloatBuffer vertices;
    private int vertexBuffer[] = new int[1];
    private int textureBuffer[] = new int[1];

    private float geo_r = 5.0f;
    private int geo_angleStep = 10;
    private int geo_vrange = 180;
    private int geo_hrange = 360;

    private int vCount;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int GEO_COORDS_PER_VERTEX = 3;
    private static final int TEX_COORDS_PER_VERTEX = 2; // 每个纹理坐标为 S T两个
    private static final int VERTEX_PER_SQUARE = 6;

    private Bitmap bitmap;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];

    private class Vertex {

        private float x;
        private float y;
        private float z;
        private float u;
        private float v;

        public Vertex(float r, float phi, float theta) {
            x = (float) (r * Math.sin(Math.toRadians(theta)) * Math.cos(Math.toRadians(phi)));
            y = (float) (r * Math.sin(Math.toRadians(theta)) * Math.sin(Math.toRadians(phi)));
            z = (float) (r * Math.cos(Math.toRadians(theta)));
            u = phi / 360.0f;
            v = 1.0f - theta / 180.0f;
        }

        public void put(float buffer[], int idx, int stride) {
            buffer[idx * stride] = x;
            buffer[idx * stride + 1] = y;
            buffer[idx * stride + 2] = z;
            buffer[idx * stride + 3] = u;
            buffer[idx * stride + 4] = v;
        }
    }

    public GLRenderer(Context context, GLSurfaceView view) {
        this.prog = new GLProgram(context);
        this.view = view;

        setupVertex();

        bitmap = BitmapFactory.decodeResource(
                context.getResources(),
                context.getResources().getIdentifier("earth", "drawable", context.getPackageName()), new BitmapFactory.Options());

    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void resize(int w, int h) {
        this.width = w;
        this.height = h;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");

        setupGL();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");

        GLES20.glViewport(0, 0, width, height);

        this.width = width;
        this.height = height;

        float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 100);

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        //GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        //bitmap.recycle();

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setLookAtM(mViewMatrix, 0,
                0, 0, 0f,
                0f, 0f, -10.0f,
                0f, 1.0f, 0.0f);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(u_MVPMatrix_h, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

    }

    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }

    private void setupVertex() {
        ArrayList<Float> alVertex = new ArrayList<>();

        int idx = 0;
        int stride = GEO_COORDS_PER_VERTEX + TEX_COORDS_PER_VERTEX;
        vCount = geo_vrange / geo_angleStep * geo_hrange / geo_angleStep * VERTEX_PER_SQUARE * stride;
        float verticesList[] = new float[vCount];

        for (int theta = 0; theta < geo_vrange; theta = theta + geo_angleStep)
        {
            for (int phi = 0; phi < geo_hrange; phi = phi + geo_angleStep)
            {
                Vertex v[] = new Vertex[4];
                v[0] = new Vertex(geo_r, phi, theta);
                v[1] = new Vertex(geo_r, phi + geo_angleStep, theta);
                v[2] = new Vertex(geo_r, phi + geo_angleStep, theta + geo_angleStep);
                v[3] = new Vertex(geo_r, phi, theta + geo_angleStep);

                v[0].put(verticesList, idx++, stride);
                v[1].put(verticesList, idx++, stride);
                v[3].put(verticesList, idx++, stride);

                v[1].put(verticesList, idx++, stride);
                v[2].put(verticesList, idx++, stride);
                v[3].put(verticesList, idx++, stride);

            }
        }

        vertices = ByteBuffer
                .allocateDirect(verticesList.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertices.put(verticesList).position(0);
    }

    private void setupGL() {
        if (true) {
            prog.build(new String[]{
                    "a_position",
                    "a_texCoord",
                    "u_MVPMatrix",
                    "u_texture"});
            checkGlError("build program");
            Log.d(TAG, "program built");
        }

        a_texCoord_h = GLES20.glGetAttribLocation(prog.getProgram(), "a_texCoord");
        a_position_h = GLES20.glGetAttribLocation(prog.getProgram(), "a_position");
        u_texture_h = GLES20.glGetUniformLocation(prog.getProgram(), "u_texture");
        u_MVPMatrix_h = GLES20.glGetUniformLocation(prog.getProgram(), "u_MVPMatrix");

        GLES20.glGenBuffers(1, vertexBuffer, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.capacity() * BYTES_PER_FLOAT, vertices, GLES20.GL_STATIC_DRAW);

        int stride = (GEO_COORDS_PER_VERTEX + TEX_COORDS_PER_VERTEX) * BYTES_PER_FLOAT;
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer[0]);
        GLES20.glEnableVertexAttribArray(a_position_h);
        GLES20.glVertexAttribPointer(a_position_h, GEO_COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, stride, 0);
        GLES20.glEnableVertexAttribArray(a_texCoord_h);
        GLES20.glVertexAttribPointer(a_texCoord_h, TEX_COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, stride, GEO_COORDS_PER_VERTEX * BYTES_PER_FLOAT);

        GLES20.glGenTextures(1, textureBuffer, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureBuffer[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glUniform1i(u_texture_h, 0);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
    }

}
