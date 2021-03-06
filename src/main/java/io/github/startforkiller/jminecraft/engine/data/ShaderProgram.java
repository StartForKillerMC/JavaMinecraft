package io.github.startforkiller.jminecraft.engine.data;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {

    private final int programID;
    private int vertexShaderID;
    private int fragmentShaderID;

    private final Map<String, Integer> uniforms;

    public ShaderProgram() throws Exception {
        programID = glCreateProgram();
        if(programID == 0) throw new Exception("Shader failed to initialize: glCreateProgram");

        uniforms = new HashMap<>();
    }

    public void createUniform(String uniformName) throws Exception {
        int uniformLocation = glGetUniformLocation(programID, uniformName);
        if(uniformLocation < 0) throw new Exception("Shader uniform not found: " + uniformName);

        uniforms.put(uniformName, uniformLocation);
    }

    public void setUniform(String uniformName, Matrix4f value) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }

    public void setUniform(String uniformName, int value) {
            glUniform1i(uniforms.get(uniformName), value);
    }

    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderID = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderID = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderID = glCreateShader(shaderType);
        if(shaderID == 0) throw new Exception("Shader failed to initialize: glCreateShader of type " + shaderType);

        glShaderSource(shaderID, shaderCode);
        glCompileShader(shaderID);

        if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) throw new Exception("Shader failed to compile: " + glGetShaderInfoLog(shaderID, 1024));

        glAttachShader(programID, shaderID);

        return shaderID;
    }

    public void link() throws Exception {
        glLinkProgram(programID);
        if(glGetProgrami(programID, GL_LINK_STATUS) == 0) throw new Exception("Shader failed to link: " + glGetProgramInfoLog(programID, 1024));

        if(vertexShaderID != 0) glDetachShader(programID, vertexShaderID);
        if(fragmentShaderID != 0) glDetachShader(programID, fragmentShaderID);

        glValidateProgram(programID);
        if(glGetProgrami(programID, GL_VALIDATE_STATUS) == 0) System.err.println("Shader warning while validating: " + glGetProgramInfoLog(programID, 1024));
    }

    public void bind() {
        glUseProgram(programID);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if(programID != 0) glDeleteProgram(programID);
    }

}
