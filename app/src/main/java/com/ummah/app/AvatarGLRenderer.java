package com.ummah.app;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AvatarGLRenderer implements GLSurfaceView.Renderer {

    // ألوان
    public float[] skinColor  = {0.96f, 0.82f, 0.66f};  // #F5D0A9
    public float[] shirtColor = {0.08f, 0.40f, 0.75f};  // #1565C0
    public float[] pantsColor = {0.13f, 0.13f, 0.13f};  // #212121
    public float[] hairColor  = {0.10f, 0.10f, 0.10f};  // #1A1A1A

    // تحريك
    public float rotationY = 0f;
    public float autoRotateSpeed = 0.4f;
    public boolean autoRotate = true;
    public float zoom = 3.0f;

    private MeshBuilder.Mesh sphere, cylinder, box, hair;

    private int program;
    private int uMVPMatrix, uModelMatrix, uLightPos;
    private int aPosition, aNormal, uColor;

    private float[] projection = new float[16];
    private float[] view = new float[16];
    private float[] model = new float[16];
    private float[] mvp = new float[16];

    // Shaders
    private static final String VERTEX_SHADER =
        "uniform mat4 uMVPMatrix;\n" +
        "uniform mat4 uModelMatrix;\n" +
        "attribute vec3 aPosition;\n" +
        "attribute vec3 aNormal;\n" +
        "varying vec3 vNormal;\n" +
        "varying vec3 vWorldPos;\n" +
        "void main() {\n" +
        "  gl_Position = uMVPMatrix * vec4(aPosition, 1.0);\n" +
        "  vNormal = normalize(mat3(uModelMatrix) * aNormal);\n" +
        "  vWorldPos = vec3(uModelMatrix * vec4(aPosition, 1.0));\n" +
        "}\n";

    private static final String FRAGMENT_SHADER =
        "precision mediump float;\n" +
        "uniform vec3 uColor;\n" +
        "uniform vec3 uLightPos;\n" +
        "varying vec3 vNormal;\n" +
        "varying vec3 vWorldPos;\n" +
        "void main() {\n" +
        "  vec3 lightDir = normalize(uLightPos - vWorldPos);\n" +
        "  float diff = max(dot(vNormal, lightDir), 0.3);\n" +
        "  vec3 finalColor = uColor * diff;\n" +
        "  gl_FragColor = vec4(finalColor, 1.0);\n" +
        "}\n";

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.04f, 0.04f, 0.04f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        program = buildProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        aPosition    = GLES20.glGetAttribLocation(program, "aPosition");
        aNormal      = GLES20.glGetAttribLocation(program, "aNormal");
        uMVPMatrix   = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        uModelMatrix = GLES20.glGetUniformLocation(program, "uModelMatrix");
        uColor       = GLES20.glGetUniformLocation(program, "uColor");
        uLightPos    = GLES20.glGetUniformLocation(program, "uLightPos");

        // بناء الأشكال
        sphere   = MeshBuilder.buildSphere(1f, 20, 30);
        cylinder = MeshBuilder.buildCylinder(1f, 1f, 20);
        box      = MeshBuilder.buildBox(1f, 1f, 1f);
        hair     = MeshBuilder.buildSphere(1f, 16, 20);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.perspectiveM(projection, 0, 45f, ratio, 0.1f, 100f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (autoRotate) rotationY += autoRotateSpeed;

        Matrix.setLookAtM(view, 0,
                0, 1.2f, zoom,
                0, 1.0f, 0,
                0, 1f, 0);

        GLES20.glUseProgram(program);
        GLES20.glUniform3f(uLightPos, 3f, 5f, 4f);

        float yOffset = 0f;

        // ═══ الرأس (Sphere) ═══
        drawPart(sphere, 0f, 1.55f, 0f, 0.28f, 0.32f, 0.28f, skinColor);

        // ═══ الشعر (Sphere مقطع) ═══
        drawPart(hair, 0f, 1.62f, 0f, 0.30f, 0.28f, 0.30f, hairColor);

        // ═══ الرقبة (Cylinder صغير) ═══
        drawPart(cylinder, 0f, 1.32f, 0f, 0.10f, 0.15f, 0.10f, skinColor);

        // ═══ الجسم (Box) ═══
        drawPart(box, 0f, 0.95f, 0f, 0.55f, 0.75f, 0.28f, shirtColor);

        // ═══ الزراع اليسار ═══
        drawPart(cylinder, -0.42f, 0.95f, 0f, 0.08f, 0.80f, 0.08f, skinColor);

        // ═══ الزراع اليمين ═══
        drawPart(cylinder, 0.42f, 0.95f, 0f, 0.08f, 0.80f, 0.08f, skinColor);

        // ═══ الرجل اليسرى ═══
        drawPart(cylinder, -0.15f, 0.20f, 0f, 0.10f, 0.80f, 0.10f, pantsColor);

        // ═══ الرجل اليمنى ═══
        drawPart(cylinder, 0.15f, 0.20f, 0f, 0.10f, 0.80f, 0.10f, pantsColor);
    }

    private void drawPart(MeshBuilder.Mesh mesh, float px, float py, float pz,
                           float sx, float sy, float sz, float[] color) {
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, px, py, pz);
        Matrix.rotateM(model, 0, rotationY, 0, 1, 0);
        Matrix.scaleM(model, 0, sx, sy, sz);

        float[] temp = new float[16];
        Matrix.multiplyMM(temp, 0, view, 0, model, 0);
        Matrix.multiplyMM(mvp, 0, projection, 0, temp, 0);

        GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mvp, 0);
        GLES20.glUniformMatrix4fv(uModelMatrix, 1, false, model, 0);
        GLES20.glUniform3f(uColor, color[0], color[1], color[2]);

        mesh.vertices.position(0);
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0, mesh.vertices);
        GLES20.glEnableVertexAttribArray(aPosition);

        mesh.normals.position(0);
        GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 0, mesh.normals);
        GLES20.glEnableVertexAttribArray(aNormal);

        mesh.indices.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mesh.indexCount, GLES20.GL_UNSIGNED_SHORT, mesh.indices);
    }

    private int buildProgram(String vs, String fs) {
        int v = compileShader(GLES20.GL_VERTEX_SHADER, vs);
        int f = compileShader(GLES20.GL_FRAGMENT_SHADER, fs);
        int p = GLES20.glCreateProgram();
        GLES20.glAttachShader(p, v);
        GLES20.glAttachShader(p, f);
        GLES20.glLinkProgram(p);
        return p;
    }

    private int compileShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
