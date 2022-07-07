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

    private Chunk[] neighbors;

    boolean ready = false;

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
                cubes[x][y] = random.nextInt(100) < 10 ? Cube.NORMAL_WALL : Cube.NORMAL_VOID;
            }
        }
    }

    public float[] getMesh(){
        return mesh;
    }

    public int getX(){
        return chunkX;
    }
    public int getY(){
        return chunkY;
    }

    public Cube getCube(int x, int y){
        return cubes[x][y];
    }

    private boolean getIfCubeIsTransparent(int x, int y, Chunk px, Chunk mx, Chunk pz, Chunk mz){
        if(x>=SIZE) return px.getCube(x-SIZE,y)==Cube.NORMAL_VOID;
        if(x<0) return mx.getCube(x+SIZE,y)==Cube.NORMAL_VOID;
        if(y>=SIZE) return pz.getCube(x,y-SIZE)==Cube.NORMAL_VOID;
        if(y<0) return mz.getCube(x,y+SIZE)==Cube.NORMAL_VOID;
        return this.getCube(x,y)==Cube.NORMAL_VOID;
    }

    public void generateMesh(Chunk px, Chunk mx, Chunk pz, Chunk mz){
        ArrayList<Float> vertices = new ArrayList<>();

        for(int x = 0; x < SIZE; x++){
            for(int y = 0; y < SIZE; y++){
                int xp = x + chunkX * SIZE;
                int yp = y + chunkY * SIZE;

                if (cubes[x][y] == Cube.NORMAL_WALL) {
                    if (getIfCubeIsTransparent(x + 1, y, px, mx, pz, mz)) {
                        vertices.addAll(List.of(
                                0.5f+xp, -0.5f, -0.5f+yp,  0.0f, 0.0f, 0.0f,
                                0.5f+xp, -0.5f, 0.5f+yp,  1.0f, 0.0f, 0.0f,
                                0.5f+xp,  0.5f, 0.5f+yp,  1.0f, 1.0f, 0.0f,
                                0.5f+xp,  0.5f, 0.5f+yp,  1.0f, 1.0f, 0.0f,
                                0.5f+xp,  0.5f, -0.5f+yp,  0.0f, 1.0f, 0.0f,
                                0.5f+xp, -0.5f, -0.5f+yp,  0.0f, 0.0f, 0.0f
                        ));
                    }
                    if (getIfCubeIsTransparent(x - 1, y, px, mx, pz, mz)) {
                        vertices.addAll(List.of(
                                -0.5f+xp, -0.5f, -0.5f+yp,  0.0f, 0.0f, 0.0f,
                                -0.5f+xp, -0.5f, 0.5f+yp,  1.0f, 0.0f, 0.0f,
                                -0.5f+xp,  0.5f, 0.5f+yp,  1.0f, 1.0f, 0.0f,
                                -0.5f+xp,  0.5f, 0.5f+yp,  1.0f, 1.0f, 0.0f,
                                -0.5f+xp,  0.5f, -0.5f+yp,  0.0f, 1.0f, 0.0f,
                                -0.5f+xp, -0.5f, -0.5f+yp,  0.0f, 0.0f, 0.0f
                        ));
                    }
                    if (getIfCubeIsTransparent(x, y + 1, px, mx, pz, mz)) {
                        vertices.addAll(List.of(
                                -0.5f+xp, -0.5f, 0.5f+yp,  0.0f, 0.0f, 0.0f,
                                0.5f+xp, -0.5f, 0.5f+yp,  1.0f, 0.0f, 0.0f,
                                0.5f+xp,  0.5f, 0.5f+yp,  1.0f, 1.0f, 0.0f,
                                0.5f+xp,  0.5f, 0.5f+yp,  1.0f, 1.0f, 0.0f,
                                -0.5f+xp,  0.5f, 0.5f+yp,  0.0f, 1.0f, 0.0f,
                                -0.5f+xp, -0.5f, 0.5f+yp,  0.0f, 0.0f, 0.0f
                        ));
                    }
                    if (getIfCubeIsTransparent(x, y - 1, px, mx, pz, mz)) {
                        vertices.addAll(List.of(
                                -0.5f+xp, -0.5f, -0.5f+yp,  0.0f, 0.0f, 0.0f,
                                0.5f+xp, -0.5f, -0.5f+yp,  1.0f, 0.0f, 0.0f,
                                0.5f+xp,  0.5f, -0.5f+yp,  1.0f, 1.0f, 0.0f,
                                0.5f+xp,  0.5f, -0.5f+yp,  1.0f, 1.0f, 0.0f,
                                -0.5f+xp,  0.5f, -0.5f+yp,  0.0f, 1.0f, 0.0f,
                                -0.5f+xp, -0.5f, -0.5f+yp,  0.0f, 0.0f, 0.0f
                        ));
                    }
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
    }

    public void generateVBO(){
        this.VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, this.mesh, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        ready = true;
    }

    public void destroy(){
        ready = false;
        glDeleteBuffers(this.VBO);
        this.neighbors = null;
    }

    public boolean isReady(){
        return ready;
    }

    public Chunk[] getNeighbors(){
        return this.neighbors;
    }

    public void setNeighbors(Chunk[] neighbors){
        this.neighbors = neighbors;
    }


}
