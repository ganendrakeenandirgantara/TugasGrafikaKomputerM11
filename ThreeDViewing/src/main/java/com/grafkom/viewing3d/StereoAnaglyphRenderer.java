package com.grafkom.viewing3d;

import static org.lwjgl.opengl.GL11.*;

public class StereoAnaglyphRenderer {
    private float ipd = 0.2f;          // Interpupillary Distance
    private float focalLength = 4.0f;

    public void renderAnaglyph(Runnable renderSceneCallback) {
        // PASS 1: Mata Kiri (Hanya Saluran Warna Merah / Red Channel)
        glColorMask(true, false, false, true);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(ipd / 2.0f, 0, 0); // Pergeseran kamera mata kiri
        renderSceneCallback.run();      // Eksekusi callback render

        // PASS 2: Mata Kanan (Saluran Warna Hijau & Biru / Cyan Channel)
        glColorMask(false, true, true, true);
        glClear(GL_DEPTH_BUFFER_BIT);   // Depth buffer dibersihkan
        
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(-ipd / 2.0f, 0, 0); // Pergeseran kamera mata kanan
        renderSceneCallback.run();

        // Reset Color Mask ke kondisi normal
        glColorMask(true, true, true, true);
    }
}