package com.jimcosmos.opengl.GLUtils;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.jimcosmos.opengl.R;

/**
 * Created by guobichuan on 7/20/16.
 */
public class GLProgram {

    public static final String TAG = GLProgram.class.getSimpleName();

    Context context;
    private boolean built = false;
    private int programId = 0;

    public GLProgram(Context context) {
        this.context = context;
    }

    public boolean isBuilt() {
        return built;
    }

    public void build(String[] variables) {
        final String vertexShader = RawReader.readTextFileFromRawResource(context, R.raw.vertex_shader);
        final String fragmentShader = RawReader.readTextFileFromRawResource(context, R.raw.fragment_shader);

        Log.d(TAG, vertexShader);
        Log.d(TAG, fragmentShader);

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        programId = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, variables);

        GLES20.glUseProgram(programId);
        built = true;
    }

    public int getProgram() {
        return programId;
    }

}
