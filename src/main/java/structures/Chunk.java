package structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL46.*;

public class Chunk {
    public static final int SIZE = 10;

    private int VBO;
    private float[] mesh;
    private final Cube[][] cubes;

    private final int chunkX, chunkY;

    public Chunk(int x, int y) {
        cubes = new Cube[SIZE][SIZE];
        chunkX = x;
        chunkY = y;
    }

    public int getVBO(){
        return VBO;
    }

    public int getVertCount(){
        return mesh.length / 6;
    }

    public void generateTerrain(){
        Random random = new Random();
        for(int x=0; x<SIZE; x++){
            for(int y=0; y<SIZE; y++){
                cubes[x][y] = random.nextInt(100) < 30 ? Cube.NORMAL_WALL : Cube.NORMAL_VOID;
            }
        }
    }

    public void generateMesh(){
        /*float dist = 10.5f;

        this.mesh = new float[]{
                -0.5f, -0.5f, dist,  0.0f, 0.0f, 0.0f,
                0.5f, -0.5f, dist,  1.0f, 0.0f, 0.0f,
                0.5f,  0.5f, dist,  1.0f, 1.0f, 0.0f,
                0.5f,  0.5f, dist,  1.0f, 1.0f, 0.0f,
                -0.5f,  0.5f, dist,  0.0f, 1.0f, 0.0f,
                -0.5f, -0.5f, dist,  0.0f, 0.0f, 0.0f
        };

        this.VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, this.mesh, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);*/

        ArrayList<Float> vertices = new ArrayList<>();

        for(int x = 0; x < SIZE; x++){
            for(int y = 0; y < SIZE; y++){
                int xp = x + chunkX * SIZE;
                int yp = y + chunkY * SIZE;

                if (cubes[x][y] == Cube.NORMAL_WALL) {
                    vertices.addAll(List.of(
                            // +z
                            -0.5f+xp, -0.5f, 0.5f+yp,  0.0f, 0.0f, 0.0f,
                            0.5f+xp, -0.5f, 0.5f+yp,  1.0f, 0.0f, 0.0f,
                            0.5f+xp,  0.5f, 0.5f+yp,  1.0f, 1.0f, 0.0f,
                            0.5f+xp,  0.5f, 0.5f+yp,  1.0f, 1.0f, 0.0f,
                            -0.5f+xp,  0.5f, 0.5f+yp,  0.0f, 1.0f, 0.0f,
                            -0.5f+xp, -0.5f, 0.5f+yp,  0.0f, 0.0f, 0.0f,

                            // -z
                            -0.5f+xp, -0.5f, -0.5f+yp,  0.0f, 0.0f, 0.0f,
                            0.5f+xp, -0.5f, -0.5f+yp,  1.0f, 0.0f, 0.0f,
                            0.5f+xp,  0.5f, -0.5f+yp,  1.0f, 1.0f, 0.0f,
                            0.5f+xp,  0.5f, -0.5f+yp,  1.0f, 1.0f, 0.0f,
                            -0.5f+xp,  0.5f, -0.5f+yp,  0.0f, 1.0f, 0.0f,
                            -0.5f+xp, -0.5f, -0.5f+yp,  0.0f, 0.0f, 0.0f,

                            // +x
                            0.5f+xp, -0.5f, -0.5f+yp,  0.0f, 0.0f, 0.0f,
                            0.5f+xp, -0.5f, 0.5f+yp,  1.0f, 0.0f, 0.0f,
                            0.5f+xp,  0.5f, 0.5f+yp,  1.0f, 1.0f, 0.0f,
                            0.5f+xp,  0.5f, 0.5f+yp,  1.0f, 1.0f, 0.0f,
                            0.5f+xp,  0.5f, -0.5f+yp,  0.0f, 1.0f, 0.0f,
                            0.5f+xp, -0.5f, -0.5f+yp,  0.0f, 0.0f, 0.0f,

                            // -x
                            -0.5f+xp, -0.5f, -0.5f+yp,  0.0f, 0.0f, 0.0f,
                            -0.5f+xp, -0.5f, 0.5f+yp,  1.0f, 0.0f, 0.0f,
                            -0.5f+xp,  0.5f, 0.5f+yp,  1.0f, 1.0f, 0.0f,
                            -0.5f+xp,  0.5f, 0.5f+yp,  1.0f, 1.0f, 0.0f,
                            -0.5f+xp,  0.5f, -0.5f+yp,  0.0f, 1.0f, 0.0f,
                            -0.5f+xp, -0.5f, -0.5f+yp,  0.0f, 0.0f, 0.0f
                    ));

                }
                else if (cubes[x][y] == Cube.NORMAL_VOID) {
                    vertices.addAll(List.of(
                            -0.5f+xp, -0.5f, -0.5f+yp, 0.0f, 0.0f, 0.2f,
                            0.5f+xp, -0.5f, -0.5f+yp, 1.0f, 0.0f, 0.2f,
                            0.5f+xp, -0.5f, 0.5f+yp, 1.0f, 1.0f, 0.2f,
                            0.5f+xp, -0.5f, 0.5f+yp, 1.0f, 1.0f, 0.2f,
                            -0.5f+xp, -0.5f, 0.5f+yp, 0.0f, 1.0f, 0.2f,
                            -0.5f+xp, -0.5f, -0.5f+yp, 0.0f, 0.0f, 0.2f,

                            -0.5f+xp, 0.5f, -0.5f+yp, 0.0f, 0.0f, 0.1f,
                            0.5f+xp, 0.5f, -0.5f+yp, 1.0f, 0.0f, 0.1f,
                            0.5f+xp, 0.5f, 0.5f+yp, 1.0f, 1.0f, 0.1f,
                            0.5f+xp, 0.5f, 0.5f+yp, 1.0f, 1.0f, 0.1f,
                            -0.5f+xp, 0.5f, 0.5f+yp, 0.0f, 1.0f, 0.1f,
                            -0.5f+xp, 0.5f, -0.5f+yp, 0.0f, 0.0f, 0.1f
                    ));
                }
            }
        }

        this.mesh = new float[vertices.size()];
        for(int i = 0; i < vertices.size(); i++){
            this.mesh[i] = vertices.get(i);
        }

        this.VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, this.mesh, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
