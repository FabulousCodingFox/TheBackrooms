package engine;

import engine.enums.Key;
import engine.font.TextToTexture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import structures.Chunk;
import utils.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Engine {
    private final long window;

    private boolean sprintToggle = false;
    private boolean crouchToggle = false;
    private boolean wasSprintPressed = false;
    private boolean wasCrouchPressed = false;
    private boolean wasHatPressed = false;

    private int gamepad;
    private int windowWidth, windowHeight;

    private final Matrix4f modelMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;

    private float deltaTime;
    private float lastFrame;

    private double lastMouseXP, lastMouseYP, mouseOffX, mouseOffY;

    private final int VAO_WORLD, VAO_POST_QUAD, VBO_POST_QUAD;

    private int FRAMEBUFFER;
    private int FRAMEBUFFER_COLORBUFFER;
    private int FRAMEBUFFER_RENDERBUFFER1;

    private final Shader SHADER_WORLD_STATIC_DEFAULT, SHADER_POST_NONE, SHADER_POST_LITE, SHADER_POST_CINEMATIC, SHADER_POST_GLICH, SHADER_MENU_DEFAULT;

    private final Texture TEXTURE_BACKROOMS_WALL, TEXTURE_BACKROOMS_FLOOR, TEXTURE_BACKROOMS_CEILING;
    private int menuTexture;

    private int postShaderNum = 1;
    private boolean lightingEnabled = true;

    private String typedText = "";

    private final int targetFPS = 60;

    public Engine(int windowWidth, int windowHeight, String windowTitle){
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        this.deltaTime = 0.0f;

        gamepad = -1;

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
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL);
        glfwMakeContextCurrent(window);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        double[] a = new double[1];
        double[] b = new double[1];
        glfwGetCursorPos(window, a, b);
        lastMouseXP = a[0];
        lastMouseYP = b[0];
        mouseOffX = 0d;
        mouseOffY = 0d;

        /////////////////////////////////////////////////////////////////////////////////////

        System.out.println(glfwGetJoystickName(GLFW_JOYSTICK_1));
        System.out.println(glfwGetJoystickName(GLFW_JOYSTICK_2));
        System.out.println(glfwGetJoystickName(GLFW_JOYSTICK_3));

        if(glfwJoystickPresent(GLFW_JOYSTICK_1)) {
            System.out.println("Controller detected");
            gamepad = GLFW_JOYSTICK_1;
        }

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
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER, 0);
            glBindRenderbuffer(GL_RENDERBUFFER, FRAMEBUFFER_RENDERBUFFER1);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, FRAMEBUFFER_RENDERBUFFER1);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glBindTexture(GL_TEXTURE_2D, 0);
            glBindRenderbuffer(GL_RENDERBUFFER, 0);
        });

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) glfwSetWindowShouldClose(window, true);

            if(action == GLFW_PRESS){
                char c = switch (key){
                    case GLFW_KEY_A -> 'a';
                    case GLFW_KEY_B -> 'b';
                    case GLFW_KEY_C -> 'c';
                    case GLFW_KEY_D -> 'd';
                    case GLFW_KEY_E -> 'e';
                    case GLFW_KEY_F -> 'f';
                    case GLFW_KEY_G -> 'g';
                    case GLFW_KEY_H -> 'h';
                    case GLFW_KEY_I -> 'i';
                    case GLFW_KEY_J -> 'j';
                    case GLFW_KEY_K -> 'k';
                    case GLFW_KEY_L -> 'l';
                    case GLFW_KEY_M -> 'm';
                    case GLFW_KEY_N -> 'n';
                    case GLFW_KEY_O -> 'o';
                    case GLFW_KEY_P -> 'p';
                    case GLFW_KEY_Q -> 'q';
                    case GLFW_KEY_R -> 'r';
                    case GLFW_KEY_S -> 's';
                    case GLFW_KEY_T -> 't';
                    case GLFW_KEY_U -> 'u';
                    case GLFW_KEY_V -> 'v';
                    case GLFW_KEY_W -> 'w';
                    case GLFW_KEY_X -> 'x';
                    case GLFW_KEY_Y -> 'y';
                    case GLFW_KEY_Z -> 'z';

                    case GLFW_KEY_0 -> '0';
                    case GLFW_KEY_1 -> '1';
                    case GLFW_KEY_2 -> '2';
                    case GLFW_KEY_3 -> '3';
                    case GLFW_KEY_4-> '4';
                    case GLFW_KEY_5 -> '5';

                    default -> '?';
                };
                if(c!='?') typedText+=c;
                if(key == GLFW_KEY_ENTER) typedText += "\n> ";
                if(key == GLFW_KEY_SPACE) typedText += " ";
                if(key == GLFW_KEY_BACKSPACE && typedText.length()>0
                        && typedText.charAt(typedText.length()-1) != ' '
                        && typedText.charAt(typedText.length()-2) != '>'
                        && typedText.charAt(typedText.length()-3) != '\n'
                ) typedText = typedText.substring(0, typedText.length()-1);

            }
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
        Log.info("SHADER_WORLD_STATIC_DEFAULT");
        SHADER_WORLD_STATIC_DEFAULT = new Shader(
                "shader/world.vert",
                "shader/world.frag"
        );
        Log.info("SHADER_POST_CINEMATIC");
        SHADER_POST_CINEMATIC = new Shader(
                "shader/post/advanced.vert",
                "shader/post/advanced.frag"
        );
        Log.info("SHADER_POST_NONE");
        SHADER_POST_NONE = new Shader(
                "shader/post/default.vert",
                "shader/post/none.frag"
        );
        Log.info("SHADER_POST_LITE");
        SHADER_POST_LITE = new Shader(
                "shader/post/default.vert",
                "shader/post/default.frag"
        );
        Log.info("SHADER_POST_GLICH");
        SHADER_POST_GLICH = new Shader(
                "shader/post/advanced.vert",
                "shader/post/glitch.frag"
        );
        Log.info("SHADER_MENU_DEFAULT");
        SHADER_MENU_DEFAULT = new Shader(
                "shader/menu.vert",
                "shader/menu.frag"
        );

        //////////////////////////////////////////////////////////////////////////////////////

        Log.info("Initializing Textures...");
        TEXTURE_BACKROOMS_WALL = new Texture("textures/STRUCTURE_WALL.png",GL_TEXTURE1 );
        TEXTURE_BACKROOMS_CEILING = new Texture("textures/STRUCTURE_CEILING.png",GL_TEXTURE2 );
        TEXTURE_BACKROOMS_FLOOR = new Texture("textures/STRUCTURE_FLOOR.png", GL_TEXTURE3);

        //////////////////////////////////////////////////////////////////////////////////////
        Log.info("Initializing Matrix...");

        projectionMatrix = getProjectionMatrix(windowWidth, windowHeight, 60, 100);
        modelMatrix = new Matrix4f();
        viewMatrix = getViewMatrix(new Vector3f(0,0,0), new Vector3f(0,0,1));


        try {
            TextToTexture.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public String getTypedText(){
        return typedText;
    }

    public void clearTypedText(){
        typedText = "> ";
    }

    public void setPostShader(int i){
        this.postShaderNum = i;
    }

    public void setLightingEnabled(boolean light){
        lightingEnabled = light;
    }

    public boolean render(ArrayList<Chunk> chunks, Vector3f position, Vector3f direction, int renderDistance){
        final boolean DRAW_LINES = false;
        final boolean ENABLE_RENDERBUFFER = true;

        if(glfwWindowShouldClose(window)) return false;

        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;

        // First Pass
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if(ENABLE_RENDERBUFFER) glBindFramebuffer(GL_FRAMEBUFFER, FRAMEBUFFER);
        if(DRAW_LINES) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
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
        SHADER_WORLD_STATIC_DEFAULT.setVector3f("camPos", position);
        SHADER_WORLD_STATIC_DEFAULT.setInt("renderDistance", (renderDistance-1) * Chunk.SIZE);
        SHADER_WORLD_STATIC_DEFAULT.setBool("lightingEnabled", lightingEnabled);
        SHADER_WORLD_STATIC_DEFAULT.setVector2f("iResolution", new Vector2f(windowWidth, windowHeight));
        SHADER_WORLD_STATIC_DEFAULT.setFloat("iTime", (float) glfwGetTime());

        for(Chunk chunk : chunks){
            glBindBuffer(GL_ARRAY_BUFFER, chunk.getVBO());
            //   4   8   12  16  20  24    28
            //   X   Y   Z   U   V   TEXID AO
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 28, 0);
            glEnableVertexAttribArray(0); // Position
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 28, 12);
            glEnableVertexAttribArray(1); // Texture Coordinates + ID
            glVertexAttribPointer(2, 1, GL_FLOAT, false, 28, 24);
            glEnableVertexAttribArray(2); // AO
            glDrawArrays(GL_TRIANGLES, 0, chunk.getVertCount());
        }

        // Second Pass
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if(ENABLE_RENDERBUFFER) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0); // back to default
            glDisable(GL_DEPTH_TEST);
            glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            Shader s = switch (postShaderNum) {
                case 0 -> SHADER_POST_NONE;
                case 1 -> SHADER_POST_LITE;
                case 2 -> SHADER_POST_CINEMATIC;
                default -> SHADER_POST_GLICH;
            };
            s.use();
            s.setVector2f("iResolution", new Vector2f(windowWidth, windowHeight));
            s.setFloat("iTime", (float) glfwGetTime());
            glBindVertexArray(VAO_POST_QUAD);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindVertexArray(0);
        }
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        double[] a = new double[1];
        double[] b = new double[1];
        glfwGetCursorPos(window, a, b);
        mouseOffX = a[0] - lastMouseXP;
        mouseOffY = b[0] - lastMouseYP;
        lastMouseXP = a[0];
        lastMouseYP = b[0];

        glfwSwapBuffers(window);
        glfwPollEvents();
        return true;
    }

    public boolean renderTerminal(String text){
        if(glfwWindowShouldClose(window)) return false;

        if(gamepad != -1){
            ByteBuffer pressedButtons = glfwGetJoystickButtons(gamepad);
            int hats = 0;
            if(pressedButtons.getInt(3) == GLFW_PRESS){
                typedText+="start\n";
            }
            if(pressedButtons.getInt(4) == GLFW_PRESS){
                typedText+="exit\n";
            }
            if(pressedButtons.getInt(16) == GLFW_PRESS && !wasHatPressed){
                typedText+="shader 0\n";
                wasHatPressed = true;
            }
            if(pressedButtons.getInt(17) == GLFW_PRESS && !wasHatPressed) {
                typedText+="shader 1\n";
                wasHatPressed = true;
            }
            if(pressedButtons.getInt(18) == GLFW_PRESS && !wasHatPressed) {
                typedText+="shader 2\n";
                wasHatPressed = true;
            }
            if(pressedButtons.getInt(19) == GLFW_PRESS && !wasHatPressed) {
                typedText+="shader 3\n";
                wasHatPressed = true;
            }

            if(pressedButtons.getInt(16) != GLFW_PRESS &&
                pressedButtons.getInt(17) != GLFW_PRESS &&
                pressedButtons.getInt(18) != GLFW_PRESS &&
                pressedButtons.getInt(19) != GLFW_PRESS
            ){
                wasHatPressed = false;
            }
        }


        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // First Pass
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        glBindFramebuffer(GL_FRAMEBUFFER, FRAMEBUFFER);
        glDisable(GL_DEPTH_TEST);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);


        SHADER_MENU_DEFAULT.use();
        SHADER_MENU_DEFAULT.setVector2f("iResolution", new Vector2f(windowWidth, windowHeight));
        SHADER_MENU_DEFAULT.setFloat("iTime", (float) glfwGetTime());

        TextToTexture.RenderText(text);
        ByteBuffer buffer = TextToTexture.getImageData();
        menuTexture = glGenTextures();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, menuTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, TextToTexture.getWidth(), TextToTexture.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glBindVertexArray(VAO_POST_QUAD);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        buffer.clear();
        glDeleteTextures(menuTexture);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Second Pass
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        glBindFramebuffer(GL_FRAMEBUFFER, 0); // back to default
        glClear(GL_COLOR_BUFFER_BIT);

        Shader s = switch (postShaderNum){
            case 0 -> SHADER_POST_NONE;
            case 1 -> SHADER_POST_LITE;
            case 2 -> SHADER_POST_CINEMATIC;
            default -> SHADER_POST_GLICH;
        };
        s.use();
        s.setVector2f("iResolution", new Vector2f(windowWidth, windowHeight));
        s.setFloat("iTime", (float) glfwGetTime());
        glBindVertexArray(VAO_POST_QUAD);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
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

    private float zeroBelowThreshold(float value){
        if(Math.abs(value) < 0.2f) return 0f;
        return value;
    }

    public double getMouseMoveX(){
        if(gamepad != -1){
            return zeroBelowThreshold(glfwGetJoystickAxes(gamepad).get(2)) * 5;
        }
        return mouseOffX;
    }

    public double getMouseMoveY(){
        if(gamepad != -1){
            return zeroBelowThreshold(glfwGetJoystickAxes(gamepad).get(3)) * 5;
        }
        return mouseOffY;
    }

    public boolean hasJoystick(){
        return gamepad != -1;
    }

    public float[] getJoystickAxes(){
        FloatBuffer axes = glfwGetJoystickAxes(gamepad);
        System.out.println(zeroBelowThreshold(-axes.get(1)));
        return new float[]{
                zeroBelowThreshold(-axes.get(1)),
                zeroBelowThreshold(axes.get(0))
        };
    }

    public boolean getIfKeyIsPressed(Key key){
        if(gamepad != -1){
            ByteBuffer pressedButtons = glfwGetJoystickButtons(gamepad);
            if(pressedButtons.getInt(14) == GLFW_PRESS && !wasSprintPressed){
                sprintToggle = !sprintToggle;
                wasSprintPressed = true;
            }
            if(pressedButtons.getInt(14) != GLFW_PRESS && wasSprintPressed){
                wasSprintPressed = false;
            }
            if(pressedButtons.getInt(15) == GLFW_PRESS && !wasCrouchPressed){
                crouchToggle = !crouchToggle;
                wasCrouchPressed = true;
            }
            if(pressedButtons.getInt(15) != GLFW_PRESS && wasCrouchPressed){
                wasCrouchPressed = false;
            }
            if(key == Key.SPRINT) return sprintToggle;
            if(key == Key.CROUCH) return crouchToggle;
            if(key == Key.TERMINAL) return pressedButtons.getInt(11) == GLFW_PRESS || pressedButtons.getInt(12) == GLFW_PRESS;
            if(key == Key.ENTER) return pressedButtons.getInt(7) == GLFW_PRESS;
            if(key == Key.JUMP) return pressedButtons.getInt(3) == GLFW_PRESS;
            return false;
        }else{
            if(key == Key.WALK_FORWARD) return glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS;
            if(key == Key.WALK_BACKWARD) return glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS;
            if(key == Key.WALK_LEFT) return glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS;
            if(key == Key.WALK_RIGHT) return glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS;
            if(key == Key.SPRINT) return glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;
            if(key == Key.CROUCH) return glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS;
            if(key == Key.TERMINAL) return glfwGetKey(window, GLFW_KEY_TAB) == GLFW_PRESS;
            if(key == Key.ENTER) return glfwGetKey(window, GLFW_KEY_ENTER) == GLFW_PRESS;
            if(key == Key.JUMP) return glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS ;
            return false;
        }
    }

    public float getFrameTime(){
        return deltaTime;
    }

    public double getTime(){
        return glfwGetTime();
    }



}