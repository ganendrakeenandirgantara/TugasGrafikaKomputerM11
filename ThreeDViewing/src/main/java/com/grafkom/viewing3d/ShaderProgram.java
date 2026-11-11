package com.grafkom.viewing3d;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    public ShaderProgram(String vertPath, String fragPath) {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException("Could not create Shader");
        }

        vertexShaderId = createShader(loadResource(vertPath), GL_VERTEX_SHADER);
        fragmentShaderId = createShader(loadResource(fragPath), GL_FRAGMENT_SHADER);

        link();
    }

    private String loadResource(String path) {
        try {
            InputStream inputStream = getClass().getResourceAsStream(path);
            if (inputStream == null) {
                // Coba alternatif path tanpa garis miring di depan
                inputStream = getClass().getResourceAsStream(path.startsWith("/") ? path.substring(1) : "/" + path);
            }
            
            if (inputStream == null) {
                throw new RuntimeException("Shader file not found at path: " + path);
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                return builder.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading shader file: " + path, e);
        }
    }

    private int createShader(String shaderCode, int shaderType) {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader type " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);
        return shaderId;
    }

    private void link() {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }

        glValidateProgram(programId);
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void setUniform(String uniformName, boolean value) {
        int location = glGetUniformLocation(programId, uniformName);
        glUniform1i(location, value ? 1 : 0);
    }
    
    public void setUniform(String uniformName, org.joml.Vector3f value) {
        int location = glGetUniformLocation(programId, uniformName);
        glUniform3f(location, value.x, value.y, value.z);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }
}