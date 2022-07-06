package engine;

import engine.enums.Key;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import structures.Chunk;
import utils.Log;

import java.util.ArrayList;

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

    private final Matrix4f modelMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;

    private float deltaTime;
    private float lastFrame;

    private final int VAO_WORLD, VAO_POST_QUAD, VBO_POST_QUAD;

    private int FRAMEBUFFER;
    private int FRAMEBUFFER_COLORBUFFER;
    private int FRAMEBUFFER_RENDERBUFFER1;

    private final Shader SHADER_WORLD_STATIC_DEFAULT, SHADER_POST_DEFAULT;

    private final Texture TEXTURE_BACKROOMS_WALL, TEXTURE_BACKROOMS_FLOOR, TEXTURE_BACKROOMS_CEILING;

    public Engine(int windowWidth, int windowHeight, String windowTitle){
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        this.deltaTime = 0.0f;

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
            glViewport(0, 0, width, height);
            projectionMatrix = getProjectionMatrix(this.windowWidth, this.windowHeight, 60, 100);

            // Resize Framebuffer
            glBindFramebuffer(GL_FRAMEBUFFER, FRAMEBUFFER);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, windowWidth, windowHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER, 0);
            glBindRenderbuffer(GL_RENDERBUFFER, FRAMEBUFFER_RENDERBUFFER1);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, windowWidth, windowHeight);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, FRAMEBUFFER_RENDERBUFFER1);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glBindTexture(GL_TEXTURE_2D, 0);
            glBindRenderbuffer(GL_RENDERBUFFER, 0);




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

        VAO_WORLD = glGenVertexArrays();
        glBindVertexArray(VAO_WORLD);

        float[] quadVertices = {
                -1.0f,  1.0f,  0.0f, 1.0f,
                -1.0f, -1.0f,  0.0f, 0.0f,
                1.0f, -1.0f,  1.0f, 0.0f,
                -1.0f,  1.0f,  0.0f, 1.0f,
                1.0f, -1.0f,  1.0f, 0.0f,
                1.0f,  1.0f,  1.0f, 1.0f
        };
        VAO_POST_QUAD = glGenVertexArrays();
        VBO_POST_QUAD = glGenBuffers();
        glBindVertexArray(VAO_POST_QUAD);
        glBindBuffer(GL_ARRAY_BUFFER, VBO_POST_QUAD);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 16, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 16, 8);

        Log.info("Ititializing Framebuffer");

        FRAMEBUFFER = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, FRAMEBUFFER);

        FRAMEBUFFER_COLORBUFFER = glGenTextures();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, windowWidth, windowHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER, 0);

        FRAMEBUFFER_RENDERBUFFER1 = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, FRAMEBUFFER_RENDERBUFFER1);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, windowWidth, windowHeight);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, FRAMEBUFFER_RENDERBUFFER1);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
            Log.severe("Framebuffer not initialized");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        //////////////////////////////////////////////////////////////////////////////////////

        Log.info("Initializing Shaders...");
        SHADER_WORLD_STATIC_DEFAULT = new Shader(
                "shader/world.vert",
                "shader/world.frag"
        );
        SHADER_POST_DEFAULT = new Shader(
                "shader/post/default.vert",
                "shader/post/default.frag"
        );

        //////////////////////////////////////////////////////////////////////////////////////

        Log.info("Initializing Textures...");
        TEXTURE_BACKROOMS_WALL = new Texture("textures/wall.png",GL_TEXTURE1 );
        TEXTURE_BACKROOMS_CEILING = new Texture("textures/ceiling.png",GL_TEXTURE2 );
        TEXTURE_BACKROOMS_FLOOR = new Texture("textures/carpet.png", GL_TEXTURE3);

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

        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;

        // First Pass
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        glBindFramebuffer(GL_FRAMEBUFFER, FRAMEBUFFER);
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        viewMatrix = getViewMatrix(position, direction);

        SHADER_WORLD_STATIC_DEFAULT.use();
        SHADER_WORLD_STATIC_DEFAULT.setInt("WALL_TEXTURE", 1);
        SHADER_WORLD_STATIC_DEFAULT.setInt("CEILING_TEXTURE", 3);
        SHADER_WORLD_STATIC_DEFAULT.setInt("FLOOR_TEXTURE", 2);
        SHADER_WORLD_STATIC_DEFAULT.setMatrix4f("projection", projectionMatrix);
        SHADER_WORLD_STATIC_DEFAULT.setMatrix4f("view", viewMatrix);
        SHADER_WORLD_STATIC_DEFAULT.setMatrix4f("model", modelMatrix);

        for(Chunk chunk : chunks){
            glBindBuffer(GL_ARRAY_BUFFER, chunk.getVBO());
            //   4   8   12  16  20  24
            //   X   Y   Z   U   V   TEXID
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 24, 0);
            glEnableVertexAttribArray(0); // Position
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 24, 12);
            glEnableVertexAttribArray(1); // Texture Coordinates + ID
            glDrawArrays(GL_TRIANGLES, 0, chunk.getVertCount());
        }

        // Second Pass
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        glBindFramebuffer(GL_FRAMEBUFFER, 0); // back to default
        glDisable(GL_DEPTH_TEST);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        SHADER_POST_DEFAULT.use();
        SHADER_POST_DEFAULT.setVector2f("iResolution", new Vector2f(windowWidth, windowHeight));
        SHADER_POST_DEFAULT.setFloat("iTime", currentFrame);
        glBindVertexArray(VAO_POST_QUAD);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        glfwSwapBuffers(window);
        glfwPollEvents();
        return true;
    }

    public void cleanup(){
        Log.info("Cleaning up...");
        SHADER_WORLD_STATIC_DEFAULT.delete();
        glDeleteTextures(TEXTURE_BACKROOMS_WALL.get());
        glDeleteTextures(TEXTURE_BACKROOMS_CEILING.get());
        glDeleteTextures(TEXTURE_BACKROOMS_FLOOR.get());
        glDeleteVertexArrays(VAO_WORLD);
        glDeleteVertexArrays(VAO_POST_QUAD);
        glDeleteBuffers(VBO_POST_QUAD);
        glDeleteTextures(FRAMEBUFFER_COLORBUFFER);
        glDeleteFramebuffers(FRAMEBUFFER);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public boolean getIfKeyIsPressed(Key key){
        if(key == Key.WALK_FORWARD) return glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS;
        if(key == Key.WALK_BACKWARD) return glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS;
        if(key == Key.TURN_LEFT) return glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS;
        if(key == Key.TURN_RIGHT) return glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS;
        if(key == Key.SPRINT) return glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;
        if(key == Key.CROUCH) return glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS;
        return false;
    }

    public float getFrameTime(){
        return deltaTime;
    }



}