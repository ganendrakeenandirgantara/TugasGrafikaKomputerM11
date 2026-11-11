package com.grafkom.viewing3d;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.joml.Vector3f;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class ThreeDViewing {
    private long window;
    private Camera3D camera;
    private PerspectiveProjection projection;
    private StereoAnaglyphRenderer stereoRenderer;
    private Texture2D marbelTexture; 
    private float rotationAngle = 0.0f;
    
    private ProjectionTaxonomyManager.ProjectionType currentProjection = 
        ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_1POINT;
    
    private float currentFov = 45.0f;

    // Status Fitur Visual Realism
    private boolean enableDepthTest = true;
    private boolean enableCulling = false; 
    private boolean useTexture = true;     
    private boolean enableLighting = true; // Status awal pencahayaan (Lighting ON)

    public void run() {
        init();
        loop();

        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Gagal menginisialisasi GLFW");
        }

        window = glfwCreateWindow(800, 600, "3D Visual Realism Suite - NetBeans", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Gagal membuat jendela GLFW");
        }

        glfwMakeContextCurrent(window);
        glfwShowWindow(window);
        GL.createCapabilities();

        HSRManager.setupDepthTest(enableDepthTest);
        HSRManager.setupBackFaceCulling(enableCulling);

        // Inisialisasi awal pencahayaan OpenGL
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        
        // Mengatur parameter cahaya sederhana agar permukaan kubus terlihat ada bayangan/dimensi
        glLightfv(GL_LIGHT0, GL_POSITION, new float[]{1.0f, 1.0f, 1.0f, 0.0f});
        glLightfv(GL_LIGHT0, GL_DIFFUSE, new float[]{1.0f, 1.0f, 1.0f, 1.0f});
        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);

        // Memuat tekstur menggunakan absolute path
        try {
            String projectPath = System.getProperty("user.dir");
            String texturePath = projectPath + "/src/main/resources/marbel.jpg";
            marbelTexture = new Texture2D(texturePath); 
            System.out.println("[Info] Tekstur marbel berhasil dimuat.");
        } catch (Exception e) {
            System.out.println("[Warning] Gagal memuat tekstur marbel, lanjut dengan warna standar.");
            marbelTexture = null;
        }

        camera = new Camera3D(new Vector3f(0, 0, 3.5f), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        projection = new PerspectiveProjection(currentFov, 800.0f / 600.0f, 0.1f, 50.0f);
        stereoRenderer = new StereoAnaglyphRenderer();
        
        System.out.println("==========================================");
        System.out.println(" KONTROL INTERAKTIF AKTIF:");
        System.out.println(" [T]   Toggle Tekstur Marbel (On/Off)");
        System.out.println(" [Z]   Toggle Z-Buffer Depth Test (On/Off)");
        System.out.println(" [C]   Toggle Back-Face Culling (On/Off)");
        System.out.println(" [B]   Toggle Pencahayaan / Lighting (On/Off)");
        System.out.println(" [W/S] Zoom In / Zoom Out");
        System.out.println("==========================================");
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            handleInput();

            glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            projection.setFovDegree(currentFov);
            ProjectionTaxonomyManager.applyProjection(currentProjection, 800.0f / 600.0f, currentFov);

            stereoRenderer.renderAnaglyph(() -> {
                glMatrixMode(GL_MODELVIEW);
                glLoadIdentity();
                
                float[] fb = new float[16];
                Matrix4f viewMat = camera.calculateLookAtMatrix();
                glLoadMatrixf(viewMat.get(fb));

                glRotatef(rotationAngle, 1.0f, 1.0f, 0.0f);
                drawCubeWithNormalsAndTex();
            });

            rotationAngle += 0.15f;

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void handleInput() {
        if (glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS) currentProjection = ProjectionTaxonomyManager.ProjectionType.ORTHOGRAPHIC;
        if (glfwGetKey(window, GLFW_KEY_2) == GLFW_PRESS) currentProjection = ProjectionTaxonomyManager.ProjectionType.ISOMETRIC;
        if (glfwGetKey(window, GLFW_KEY_3) == GLFW_PRESS) currentProjection = ProjectionTaxonomyManager.ProjectionType.OBLIQUE_CAVALIER;
        if (glfwGetKey(window, GLFW_KEY_4) == GLFW_PRESS) currentProjection = ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_1POINT;

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
            currentFov -= 0.5f; if (currentFov < 10.0f) currentFov = 10.0f;
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
            currentFov += 0.5f; if (currentFov > 120.0f) currentFov = 120.0f;
        }

        if (glfwGetKey(window, GLFW_KEY_T) == GLFW_PRESS) {
            useTexture = !useTexture;
            System.out.println("[Texture Mode] Tekstur Marbel Aktif: " + useTexture);
            try { Thread.sleep(200); } catch (Exception e) {}
        }

        if (glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS) {
            enableDepthTest = !enableDepthTest;
            HSRManager.setupDepthTest(enableDepthTest);
            System.out.println("[HSR] Z-Depth Test: " + enableDepthTest);
            try { Thread.sleep(200); } catch (Exception e) {}
        }

        if (glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS) {
            enableCulling = !enableCulling;
            HSRManager.setupBackFaceCulling(enableCulling);
            System.out.println("[HSR] Back-Face Culling: " + enableCulling);
            try { Thread.sleep(200); } catch (Exception e) {}
        }

        // --- TAMBAHAN TOMBOL B UNTUK TOGGLE LIGHTING ---
        if (glfwGetKey(window, GLFW_KEY_B) == GLFW_PRESS) {
            enableLighting = !enableLighting;
            if (enableLighting) {
                glEnable(GL_LIGHTING);
                System.out.println("[Lighting] Pencahayaan (B): ON");
            } else {
                glDisable(GL_LIGHTING);
                System.out.println("[Lighting] Pencahayaan (B): OFF");
            }
            try { Thread.sleep(200); } catch (Exception e) {}
        }
    }

    private void drawCubeWithNormalsAndTex() {
        if (useTexture && marbelTexture != null) {
            glEnable(GL_TEXTURE_2D);
            marbelTexture.bind(0);
        } else {
            glDisable(GL_TEXTURE_2D);
        }

        glBegin(GL_QUADS);
        
        glColor3f(1.0f, 1.0f, 1.0f);

        // Sisi Depan
        glNormal3f(0.0f, 0.0f, 1.0f);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(-0.5f, -0.5f,  0.5f);
        glTexCoord2f(1.0f, 0.0f); glVertex3f( 0.5f, -0.5f,  0.5f);
        glTexCoord2f(1.0f, 1.0f); glVertex3f( 0.5f,  0.5f,  0.5f);
        glTexCoord2f(0.0f, 1.0f); glVertex3f(-0.5f,  0.5f,  0.5f);

        // Sisi Belakang
        glNormal3f(0.0f, 0.0f, -1.0f);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(-0.5f, -0.5f, -0.5f);
        glTexCoord2f(1.0f, 0.0f); glVertex3f(-0.5f,  0.5f, -0.5f);
        glTexCoord2f(1.0f, 1.0f); glVertex3f( 0.5f,  0.5f, -0.5f);
        glTexCoord2f(0.0f, 1.0f); glVertex3f( 0.5f, -0.5f, -0.5f);

        // Sisi Atas
        glNormal3f(0.0f, 1.0f, 0.0f);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(-0.5f,  0.5f, -0.5f);
        glTexCoord2f(1.0f, 0.0f); glVertex3f(-0.5f,  0.5f,  0.5f);
        glTexCoord2f(1.0f, 1.0f); glVertex3f( 0.5f,  0.5f,  0.5f);
        glTexCoord2f(0.0f, 1.0f); glVertex3f( 0.5f,  0.5f, -0.5f);

        // Sisi Bawah
        glNormal3f(0.0f, -1.0f, 0.0f);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(-0.5f, -0.5f, -0.5f);
        glTexCoord2f(1.0f, 0.0f); glVertex3f( 0.5f, -0.5f, -0.5f);
        glTexCoord2f(1.0f, 1.0f); glVertex3f( 0.5f, -0.5f,  0.5f);
        glTexCoord2f(0.0f, 1.0f); glVertex3f(-0.5f, -0.5f,  0.5f);

        // Sisi Kanan
        glNormal3f(1.0f, 0.0f, 0.0f);
        glTexCoord2f(0.0f, 0.0f); glVertex3f( 0.5f, -0.5f, -0.5f);
        glTexCoord2f(1.0f, 0.0f); glVertex3f( 0.5f,  0.5f, -0.5f);
        glTexCoord2f(1.0f, 1.0f); glVertex3f( 0.5f,  0.5f,  0.5f);
        glTexCoord2f(0.0f, 1.0f); glVertex3f( 0.5f, -0.5f,  0.5f);

        // Sisi Kiri
        glNormal3f(-1.0f, 0.0f, 0.0f);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(-0.5f, -0.5f, -0.5f);
        glTexCoord2f(1.0f, 0.0f); glVertex3f(-0.5f, -0.5f,  0.5f);
        glTexCoord2f(1.0f, 1.0f); glVertex3f(-0.5f,  0.5f,  0.5f);
        glTexCoord2f(0.0f, 1.0f); glVertex3f(-0.5f,  0.5f, -0.5f);
        
        glEnd();
        glDisable(GL_TEXTURE_2D);
    }

    public static void main(String[] args) {
        new ThreeDViewing().run();
    }
}