package engine;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import utils.FileUtils;

import java.io.IOException;
import static org.lwjgl.opengl.GL46.*;

public class Shader {
    int ID;

    /*
        * Creates a shader program from the given vertex and fragment shader source code.
        * @param vertexPath The path to the vertex shader source code.
        * @param fragmentPath The path to the fragment shader source code.
     */
    public Shader(String vertexPath, String fragmentPath) {
        // 1. retrieve the vertex/fragment source code from filePath
        String vertexCode = "";
        String fragmentCode = "";
        try {
            vertexCode = FileUtils.readResourceFile(vertexPath);
            fragmentCode = FileUtils.readResourceFile(fragmentPath);
        }catch (IOException e) {
            e.printStackTrace();
        }

        // 2. compile shaders
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexCode);
        glCompileShader(vertexShader);
        if(glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) throw new RuntimeException("Vertex shader failed to compile: "+vertexPath);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentCode);
        glCompileShader(fragmentShader);
        if(glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) throw new RuntimeException("Fragment shader failed to compile: "+fragmentPath);

        // 3. create shader program
        ID = glCreateProgram();
        glAttachShader(ID, vertexShader);
        glAttachShader(ID, fragmentShader);
        glLinkProgram(ID);
        if(glGetProgrami(ID, GL_LINK_STATUS) == GL_FALSE) throw new RuntimeException("Shader program failed to link: "+vertexPath+" and "+fragmentPath);

        // 4. clean up
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    /*
        * Uses the shader program.
     */
    public void use() {
        glUseProgram(ID);
    }

    /*
        * Sets the value of a uniform variable in the shader program.
        * @param name The name of the uniform variable.
        * @param value The value of the uniform variable.
     */
    public void setInt(String name, int value) {
        glUniform1i(glGetUniformLocation(ID, name), value);
    }

    /*
        * Sets the value of a uniform variable in the shader program.
        * @param name The name of the uniform variable.
        * @param value The value of the uniform variable.
     */
    public void setFloat(String name, float value) {
        glUniform1f(glGetUniformLocation(ID, name), value);
    }

    /*
        * Sets the value of a uniform variable in the shader program.
        * @param name The name of the uniform variable.
        * @param value The value of the uniform variable.
     */
    public void setBool(String name, boolean value) {
        glUniform1i(glGetUniformLocation(ID, name), value ? 1 : 0);
    }

    /*
        * Sets the value of a uniform variable in the shader program.
        * @param name The name of the uniform variable.
        * @param value The value of the uniform variable.
     */
    public void setMatrix4f(String name, Matrix4f value) {
        glUniformMatrix4fv(glGetUniformLocation(ID, name), false, value.get(new float[16]));
    }

    /*
        * Sets the value of a uniform variable in the shader program.
        * @param name The name of the uniform variable.
        * @param value The value of the uniform variable.
     */
    public void setVector2f(String name, Vector2f value) {
        glUniform2fv(glGetUniformLocation(ID, name),new float[]{value.x, value.y} );
    }

    /*
        * Delete the shader program.
    */
    public void delete() {
        glDeleteProgram(ID);
    }


}
