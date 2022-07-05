package structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import static org.lwjgl.opengl.GL46.*;

public class Chunk {
    public static final int CHUNK_SIZE = 10;

    private final int chunkX, chunkY;
    private final Cube[][] cubes;

    private int VBO;
    private float[] mesh;

    public Chunk(int chunkX, int chunkY) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        cubes = new Cube[CHUNK_SIZE][CHUNK_SIZE];
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkY() {
        return chunkY;
    }

    public Cube getCube(int x, int y) {
        return cubes[x][y];
    }

    public void setCube(int x, int y, Cube cube) {
        cubes[x][y] = cube;
    }

    public void generateTerrain(){
        Random random = new Random();
        for(int x = 0; x < CHUNK_SIZE; x++){
            for(int y = 0; y < CHUNK_SIZE; y++){
                cubes[x][y] = random.nextInt(100) < 30 ? Cube.NORMAL_WALL : Cube.NORMAL_VOID;
            }
        }
    }

    public float[] getMesh(){
        return mesh;
    }

    public int getVBO(){
        return VBO;
    }

    public void generateMesh(){
        ArrayList<Float> vertices = new ArrayList<>();
        for(int x = 0; x < CHUNK_SIZE; x++){
            for(int y = 0; y < CHUNK_SIZE; y++){
                int xp = x + chunkX * CHUNK_SIZE;
                int yp = y + chunkY * CHUNK_SIZE;
                if(getCube(x,y) == Cube.NORMAL_VOID){
                    vertices.addAll(Arrays.asList(
                            -0.5f+xp, -2.5f, -0.5f+yp,  0.0f, 0.0f, 0.1f,
                            0.5f+xp, -2.5f, -0.5f+yp,  1.0f, 0.0f, 0.1f,
                            0.5f+xp, -2.5f, 0.5f+yp,  1.0f, 1.0f, 0.1f,
                            0.5f+xp, -2.5f, 0.5f+yp,  1.0f, 1.0f, 0.1f,
                            -0.5f+xp, -2.5f, 0.5f+yp,  0.0f, 1.0f, 0.1f,
                            -0.5f+xp, -2.5f, -0.5f+yp,  0.0f, 0.0f, 0.1f
                    ));
                }
            }
        }
        mesh = new float[vertices.size()];
        for(int i = 0; i < vertices.size(); i++){
            mesh[i] = vertices.get(i);
        }

        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, mesh, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
