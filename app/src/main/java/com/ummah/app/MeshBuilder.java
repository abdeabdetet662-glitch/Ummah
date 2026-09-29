package com.ummah.app;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * يبني أشكال 3D: Sphere, Cylinder, Cube
 * كل شكل = vertices + normals + indices
 */
public class MeshBuilder {

    public static class Mesh {
        public FloatBuffer vertices;
        public FloatBuffer normals;
        public ShortBuffer indices;
        public int indexCount;

        public Mesh(float[] v, float[] n, short[] i) {
            vertices = toFloatBuffer(v);
            normals = toFloatBuffer(n);
            indices = toShortBuffer(i);
            indexCount = i.length;
        }
    }

    private static FloatBuffer toFloatBuffer(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(data);
        fb.position(0);
        return fb;
    }

    private static ShortBuffer toShortBuffer(short[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 2);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(data);
        sb.position(0);
        return sb;
    }

    // ═══════ كرة (للرأس) ═══════
    public static Mesh buildSphere(float radius, int stacks, int slices) {
        int vertexCount = (stacks + 1) * (slices + 1);
        float[] v = new float[vertexCount * 3];
        float[] n = new float[vertexCount * 3];
        short[] idx = new short[stacks * slices * 6];

        int vp = 0;
        for (int i = 0; i <= stacks; i++) {
            float phi = (float) (Math.PI * i / stacks);
            for (int j = 0; j <= slices; j++) {
                float theta = (float) (2 * Math.PI * j / slices);
                float x = (float) (Math.sin(phi) * Math.cos(theta));
                float y = (float) Math.cos(phi);
                float z = (float) (Math.sin(phi) * Math.sin(theta));

                v[vp] = x * radius;
                v[vp + 1] = y * radius;
                v[vp + 2] = z * radius;
                n[vp] = x;
                n[vp + 1] = y;
                n[vp + 2] = z;
                vp += 3;
            }
        }

        int ip = 0;
        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int first = i * (slices + 1) + j;
                int second = first + slices + 1;
                idx[ip++] = (short) first;
                idx[ip++] = (short) second;
                idx[ip++] = (short) (first + 1);
                idx[ip++] = (short) second;
                idx[ip++] = (short) (second + 1);
                idx[ip++] = (short) (first + 1);
            }
        }
        return new Mesh(v, n, idx);
    }

    // ═══════ أسطوانة (للذراعين + الأرجل) ═══════
    public static Mesh buildCylinder(float radius, float height, int slices) {
        int vCount = (slices + 1) * 4 + 2;
        float[] v = new float[vCount * 3];
        float[] n = new float[vCount * 3];

        int vp = 0;
        float halfH = height / 2f;

        // الجسم الجانبي (علوي + سفلي)
        for (int i = 0; i <= slices; i++) {
            float theta = (float) (2 * Math.PI * i / slices);
            float x = (float) Math.cos(theta);
            float z = (float) Math.sin(theta);

            v[vp] = x * radius; v[vp + 1] = halfH; v[vp + 2] = z * radius;
            n[vp] = x; n[vp + 1] = 0; n[vp + 2] = z;
            vp += 3;
        }
        for (int i = 0; i <= slices; i++) {
            float theta = (float) (2 * Math.PI * i / slices);
            float x = (float) Math.cos(theta);
            float z = (float) Math.sin(theta);

            v[vp] = x * radius; v[vp + 1] = -halfH; v[vp + 2] = z * radius;
            n[vp] = x; n[vp + 1] = 0; n[vp + 2] = z;
            vp += 3;
        }
        // القمة
        v[vp] = 0; v[vp + 1] = halfH; v[vp + 2] = 0;
        n[vp] = 0; n[vp + 1] = 1; n[vp + 2] = 0;
        vp += 3;
        // القاعدة
        v[vp] = 0; v[vp + 1] = -halfH; v[vp + 2] = 0;
        n[vp] = 0; n[vp + 1] = -1; n[vp + 2] = 0;
        vp += 3;

        // indices
        short[] idx = new short[slices * 6 + slices * 3 * 2];
        int ip = 0;
        for (int i = 0; i < slices; i++) {
            idx[ip++] = (short) i;
            idx[ip++] = (short) (i + slices + 1);
            idx[ip++] = (short) (i + 1);
            idx[ip++] = (short) (i + 1);
            idx[ip++] = (short) (i + slices + 1);
            idx[ip++] = (short) (i + slices + 2);
        }
        int topCenter = (slices + 1) * 2;
        int botCenter = topCenter + 1;
        for (int i = 0; i < slices; i++) {
            idx[ip++] = (short) topCenter;
            idx[ip++] = (short) (i + 1);
            idx[ip++] = (short) i;
            idx[ip++] = (short) botCenter;
            idx[ip++] = (short) (i + slices + 1);
            idx[ip++] = (short) (i + slices + 2);
        }
        return new Mesh(v, n, idx);
    }

    // ═══════ مكعب (للجسم) ═══════
    public static Mesh buildBox(float w, float h, float d) {
        float x = w / 2f, y = h / 2f, z = d / 2f;
        float[] v = {
            -x,-y, z,  x,-y, z,  x, y, z, -x, y, z,  // أمام
            -x,-y,-z, -x, y,-z,  x, y,-z,  x,-y,-z,  // خلف
            -x, y,-z, -x, y, z,  x, y, z,  x, y,-z,  // فوق
            -x,-y,-z,  x,-y,-z,  x,-y, z, -x,-y, z,  // تحت
             x,-y,-z,  x, y,-z,  x, y, z,  x,-y, z,  // يمين
            -x,-y,-z, -x,-y, z, -x, y, z, -x, y,-z   // يسار
        };
        float[] n = {
             0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1,
             0, 0,-1,  0, 0,-1,  0, 0,-1,  0, 0,-1,
             0, 1, 0,  0, 1, 0,  0, 1, 0,  0, 1, 0,
             0,-1, 0,  0,-1, 0,  0,-1, 0,  0,-1, 0,
             1, 0, 0,  1, 0, 0,  1, 0, 0,  1, 0, 0,
            -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0
        };
        short[] idx = {
            0,1,2, 0,2,3,   4,5,6, 4,6,7,
            8,9,10, 8,10,11,  12,13,14, 12,14,15,
            16,17,18, 16,18,19,  20,21,22, 20,22,23
        };
        return new Mesh(v, n, idx);
    }
}
