package com.grafkom.viewing3d;

import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.*;

public class ProjectionTaxonomyManager {
    public enum ProjectionType {
        ORTHOGRAPHIC, ISOMETRIC, OBLIQUE_CAVALIER, 
        PERSPECTIVE_1POINT, PERSPECTIVE_2POINT, PERSPECTIVE_3POINT
    }

    // Ditambahkan parameter fovDegree agar zoom bekerja secara dinamis
    public static void applyProjection(ProjectionType type, float aspect, float fovDegree) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        switch (type) {
            case ORTHOGRAPHIC:
                glOrtho(-3.0 * aspect, 3.0 * aspect, -3.0, 3.0, 1.0, 20.0);
                break;
            case ISOMETRIC:
                glOrtho(-3.0 * aspect, 3.0 * aspect, -3.0, 3.0, 1.0, 20.0);
                glRotatef(35.264f, 1.0f, 0.0f, 0.0f);
                glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
                break;
            case OBLIQUE_CAVALIER:
                glOrtho(-3.0 * aspect, 3.0 * aspect, -3.0, 3.0, 1.0, 20.0);
                break;
            case PERSPECTIVE_1POINT:
            default:
                // Menggunakan kelas PerspectiveProjection dengan fovDegree dinamis
                PerspectiveProjection perspective = new PerspectiveProjection(fovDegree, aspect, 0.1f, 50.0f);
                Matrix4f projMat = perspective.calculateProjectionMatrix();
                float[] fb = new float[16];
                glLoadMatrixf(projMat.get(fb));
                break;
        }
    }
}