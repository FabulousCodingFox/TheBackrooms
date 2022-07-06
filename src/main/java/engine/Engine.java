package engine;

import engine.enums.Key;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import structures.Chunk;
import utils.Log;

import java.util.ArrayList;
import java.util.function.Consumer;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;


public class Engine {
    private final long window;
    private int windowWidth, windowHeight;

    private Matrix4f modelMatrix, viewMatrix, projectionMatrix;

    private float deltaTime;
    private float lastFrame;
    
    private int WORLD_VAO;

    private Shader WORLD_SHADER;

    private Texture BACKROOMS_WALL_TEXTURE, BACKROOMS_WALL_TEXTURE_B;

    public Engine(int windowWidth, int windowHeight, String windowTitle){
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        
        //////////////////////////////////////////////////////////////////////////////////////

        Log.init();
        Log.info("Initializing Logging...");
        Log.debug("LWJGL Version: " + Version.getVersion());
        Log.debug("GLFW Version: " + org.lwjgl.glfw.GLFW.glfwGetVersionString());
        
        //////////////////////////////////////////////////////////////////////////////////////
        
        Log.info("Initializing GLFW...");
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL);
        glfwMakeContextCurrent(window);

        //////////////////////////////////////////////////////////////////////////////////////

        Log.info("Setting GLFW window callbacks...");
        glfwSetWindowSizeCallback(window, (window, width, height) -> {
            this.windowWidth = width;
            this.windowHeight = height;
            projectionMatrix = getProjectionMatrix(this.windowWidth, this.windowHeight, 60, 100);
        });
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) glfwSetWindowShouldClose(window, true);
        });
        
        //////////////////////////////////////////////////////////////////////////////////////

        Log.info("Initialize GLFW framebuffer...");
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            assert vidmode != null;
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }
        
        //////////////////////////////////////////////////////////////////////////////////////

        Log.info("Initializing GLFW OpenGL Context...");
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
        glClearColor(0.0f,0.0f,0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        //glDepthFunc(GL_LEQUAL);
        //glEnable(GL_CULL_FACE);
        //glCullFace(GL_BACK);
        //glFrontFace(GL_CW);

        WORLD_VAO = glGenVertexArrays();

        //////////////////////////////////////////////////////////////////////////////////////

        Log.info("Initializing Shaders...");
        WORLD_SHADER = new Shader(
                "shader/world.vert",
                "shader/world.frag"
        );
        
        //////////////////////////////////////////////////////////////////////////////////////

        Log.info("Initializing Textures...");
        BACKROOMS_WALL_TEXTURE = new Texture("textures/wall.png");
        BACKROOMS_WALL_TEXTURE_B = new Texture("textures/wall_b.jpg");

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, BACKROOMS_WALL_TEXTURE.get());

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, BACKROOMS_WALL_TEXTURE_B.get());



        //////////////////////////////////////////////////////////////////////////////////////
        Log.info("Initializing Matrix...");

        projectionMatrix = getProjectionMatrix(windowWidth, windowHeight, 60, 100);
        modelMatrix = new Matrix4f();
        viewMatrix = getViewMatrix(new Vector3f(0,0,0), new Vector3f(0,0,1));
    }

    public Matrix4f getViewMatrix(Vector3f position, Vector3f direction){
        Vector3f front = direction.normalize();
        Vector3f right = new Vector3f(front).cross(new Vector3f(0,1,0)).normalize();
        Vector3f up = new Vector3f(right).cross(front).normalize();
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
    }

    public Matrix4f getProjectionMatrix(float width, float height, float fov, float viewdistance) {
        return new Matrix4f().perspective((float) Math.toRadians(fov), width/height, 0.1f, viewdistance);
    }
    
    public boolean render(ArrayList<Chunk> chunks, Vector3f position, Vector3f direction){
        if(glfwWindowShouldClose(window)) return false;

        glClearColor(0.2f,0.3f,0.2f,1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;

        viewMatrix = getViewMatrix(position, direction);

        //TODO: INPUT

        WORLD_SHADER.use();
        WORLD_SHADER.setInt("WALL_TEXTURE", 0);
        WORLD_SHADER.setInt("WALL_TEXTURE_B", 1);
        WORLD_SHADER.setMatrix4f("projection", projectionMatrix);
        WORLD_SHADER.setMatrix4f("view", viewMatrix);
        WORLD_SHADER.setMatrix4f("model", modelMatrix);

        glBindVertexArray(WORLD_VAO);
        for(Chunk chunk : chunks){
            glBindBuffer(GL_ARRAY_BUFFER, chunk.getVBO());
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0);
            glEnableVertexAttribArray(0); // Position
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12);
            glEnableVertexAttribArray(1); // Texture Coordinates
            glDrawArrays(GL_TRIANGLES, 0, chunk.getVertCount());
        }
        glfwSwapBuffers(window);
        glfwPollEvents();
        return true;
    }

    public void cleanup(){
        Log.info("Cleaning up...");
        WORLD_SHADER.delete();
        glDeleteVertexArrays(WORLD_VAO);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public void setKeyCallback(Consumer<Key[]> callback) {} //TODO: Implement this

}