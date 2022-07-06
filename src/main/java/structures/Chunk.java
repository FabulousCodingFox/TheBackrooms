package structures;

import static org.lwjgl.opengl.GL46.*;

public class Chunk {
    private int VBO;
    private float[] mesh;

    public Chunk() {
    }

    public int getVBO(){
        return VBO;
    }

    public int getVertCount(){
        return mesh.length / 5;
    }

    public void generateMesh(){
        float dist = 10.5f;

        this.mesh = new float[]{
                -0.5f, -0.5f, dist,  0.0f, 0.0f,
                0.5f, -0.5f, dist,  1.0f, 0.0f,
                0.5f,  0.5f, dist,  1.0f, 1.0f,
                0.5f,  0.5f, dist,  1.0f, 1.0f,
                -0.5f,  0.5f, dist,  0.0f, 1.0f,
                -0.5f, -0.5f, dist,  0.0f, 0.0f
        };

        this.VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, this.mesh, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
