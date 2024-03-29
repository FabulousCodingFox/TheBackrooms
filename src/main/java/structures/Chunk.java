package structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL46.*;

public class Chunk {
    private enum Terrain{
        NORMAL,
        EMPTY_HALLWAY,
        SYMMETRICAL,
        VOID_HOLES
    }

    public static final int SIZE = 10;

    private int VBO;
    private float[] mesh;
    private final Cube[][] cubes;

    private final int chunkX, chunkY;

    private Chunk[] neighbors;

    boolean ready = false;

    private final Terrain terrain;

    public Chunk(int x, int y) {
        cubes = new Cube[SIZE][SIZE];
        chunkX = x;
        chunkY = y;

        terrain = Terrain.NORMAL;
    }

    public int getVBO(){
        return VBO;
    }

    public int getVertCount(){
        return mesh.length / 6;
    }

    public void generateTerrain(){
        if(terrain == Terrain.NORMAL){
            Random random = new Random();
            for(int x=0; x<SIZE; x++){
                for(int y=0; y<SIZE; y++){
                    int num = random.nextInt(1000);
                    if(num < 200) cubes[x][y] = Cube.NORMAL_WALL;
                    if(num >= 200) cubes[x][y] = Cube.NORMAL_HALLWAY;
                    if(num < 1) cubes[x][y] = Cube.EXIT;
                }
            }
        }
        if(terrain == Terrain.EMPTY_HALLWAY){
            for(int x=0; x<SIZE; x++){
                for(int y=0; y<SIZE; y++){
                    cubes[x][y] = Cube.NORMAL_HALLWAY;
                }
            }
        }
        if(terrain == Terrain.SYMMETRICAL){
            for(int x=0; x<SIZE; x++){
                for(int y=0; y<SIZE; y++){
                    if(x % 2 == 0 && y % 2 == 0){
                        cubes[x][y] = Cube.NORMAL_WALL;
                    }else{
                        cubes[x][y] = Cube.NORMAL_HALLWAY;
                    }
                }
            }
        }
        if(terrain == Terrain.VOID_HOLES){
            Random random = new Random();
            for(int x=0; x<SIZE; x++){
                for(int y=0; y<SIZE; y++){
                    int num = random.nextInt(100);
                    if(num < 10) cubes[x][y] = Cube.HOLE_IN_FLOOR;
                    if(num >= 10) cubes[x][y] = Cube.NORMAL_HALLWAY;
                }
            }
        }

        for(int x=0; x<SIZE; x++){
            for(int y=0; y<SIZE; y++){
                if((chunkX * Chunk.SIZE + x == 0 || chunkX * Chunk.SIZE + x == -1 || chunkX * Chunk.SIZE + x == 1) && (chunkY * Chunk.SIZE + y == 0 || chunkY * Chunk.SIZE + y == -1 || chunkY * Chunk.SIZE + y == 1)){
                    cubes[x][y] = Cube.NORMAL_HALLWAY;
                }
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

    private boolean getIfCubeIsTransparent(
            int x, int y,
            Chunk px_pz, Chunk px_nz, Chunk px_mz,
            Chunk mx_pz, Chunk mx_nz, Chunk mx_mz,
            Chunk nx_pz, Chunk nx_mz){

        if(x>=SIZE && y>=SIZE) return px_pz.getCube(x-SIZE,y-SIZE) == Cube.NORMAL_HALLWAY;
        if(x>=SIZE && y<0)     return px_mz.getCube(x-SIZE,y+SIZE) == Cube.NORMAL_HALLWAY;
        if(x>=SIZE)            return px_nz.getCube(x-SIZE,y)         == Cube.NORMAL_HALLWAY;

        if(x<0 && y>=SIZE) return mx_pz.getCube(x+SIZE,y-SIZE) == Cube.NORMAL_HALLWAY;
        if(x<0 && y<0)     return mx_mz.getCube(x+SIZE,y+SIZE) == Cube.NORMAL_HALLWAY;
        if(x<0)            return mx_nz.getCube(x+SIZE,y)         == Cube.NORMAL_HALLWAY;

        if(y>=SIZE) return nx_pz.getCube(x,y-SIZE) == Cube.NORMAL_HALLWAY;
        if(y<0)     return nx_mz.getCube(x,y+SIZE) == Cube.NORMAL_HALLWAY;

        return this.getCube(x,y)==Cube.NORMAL_HALLWAY;
    }

    private float getAO(boolean side1, boolean side2, boolean corner){
        float sides = 0;
        if(side1 && side2) {
            sides =  (3 - ((side1?1:0) + (side2?1:0) + (corner?1:0))) / 3f;
        }
        return 1f - sides;
    }

    public void generateMesh(Chunk px_pz, Chunk px_nz, Chunk px_mz,
                             Chunk mx_pz, Chunk mx_nz, Chunk mx_mz,
                             Chunk nx_pz, Chunk nx_mz){
        ArrayList<Float> vertices = new ArrayList<>();

        for(int x = 0; x < SIZE; x++){
            for(int y = 0; y < SIZE; y++){
                int xp = x + chunkX * SIZE;
                int yp = y + chunkY * SIZE;

                float AO_MM = 1f;
                float AO_MP = 1f;
                float AO_PM = 1f;
                float AO_PP = 1f;

                if (cubes[x][y] == Cube.NORMAL_WALL || cubes[x][y] == Cube.EXIT) {
                    float tex = cubes[x][y] == Cube.NORMAL_WALL ? 0.0f : 0.3f;
                    if (getIfCubeIsTransparent(x + 1, y, px_pz, px_nz, px_mz, mx_pz, mx_nz, mx_mz, nx_pz, nx_mz)) {
                        vertices.addAll(List.of(
                                1f+xp, -0.5f, 0f+yp, 0.0f, 0.0f, tex, AO_PM,
                                1f+xp, -0.5f, 1f+yp,  1.0f, 0.0f, tex, AO_PP,
                                1f+xp,  0.5f, 1f+yp,  1.0f, 1.0f, tex, AO_PP,
                                1f+xp,  0.5f, 1f+yp,  1.0f, 1.0f, tex, AO_PP,
                                1f+xp,  0.5f, 0f+yp,  0.0f, 1.0f, tex, AO_PM,
                                1f+xp, -0.5f, 0f+yp,  0.0f, 0.0f, tex, AO_PM
                        ));
                    }
                    if (getIfCubeIsTransparent(x - 1, y, px_pz, px_nz, px_mz, mx_pz, mx_nz, mx_mz, nx_pz, nx_mz)) {
                        vertices.addAll(List.of(
                                0f+xp, -0.5f, 0f+yp,  0.0f, 0.0f, tex, AO_MM,
                                0f+xp, -0.5f, 1f+yp,  1.0f, 0.0f, tex, AO_MP,
                                0f+xp,  0.5f, 1f+yp,  1.0f, 1.0f, tex, AO_MP,
                                0f+xp,  0.5f, 1f+yp,  1.0f, 1.0f, tex, AO_MP,
                                0f+xp,  0.5f, 0f+yp,  0.0f, 1.0f, tex, AO_MM,
                                0f+xp, -0.5f, 0f+yp,  0.0f, 0.0f, tex, AO_MM
                        ));
                    }
                    if (getIfCubeIsTransparent(x, y + 1, px_pz, px_nz, px_mz, mx_pz, mx_nz, mx_mz, nx_pz, nx_mz)) {
                        vertices.addAll(List.of(
                                0f+xp, -0.5f, 1f+yp,  0.0f, 0.0f, tex, AO_MP,
                                1f+xp, -0.5f, 1f+yp,  1.0f, 0.0f, tex, AO_PP,
                                1f+xp,  0.5f, 1f+yp,  1.0f, 1.0f, tex, AO_PP,
                                1f+xp,  0.5f, 1f+yp,  1.0f, 1.0f, tex, AO_PP,
                                0f+xp,  0.5f, 1f+yp,  0.0f, 1.0f, tex, AO_MP,
                                0f+xp, -0.5f, 1f+yp,  0.0f, 0.0f, tex, AO_MP
                        ));
                    }
                    if (getIfCubeIsTransparent(x, y - 1, px_pz, px_nz, px_mz, mx_pz, mx_nz, mx_mz, nx_pz, nx_mz)) {
                        vertices.addAll(List.of(
                                0f+xp, -0.5f, 0f+yp,  0.0f, 0.0f, tex, AO_MM,
                                1f+xp, -0.5f, 0f+yp,  1.0f, 0.0f, tex, AO_PM,
                                1f+xp,  0.5f, 0f+yp,  1.0f, 1.0f, tex, AO_PM,
                                1f+xp,  0.5f, 0f+yp,  1.0f, 1.0f, tex, AO_PM,
                                0f+xp,  0.5f, 0f+yp,  0.0f, 1.0f, tex, AO_MM,
                                0f+xp, -0.5f, 0f+yp,  0.0f, 0.0f, tex, AO_MM
                        ));
                    }
                }
                else if (cubes[x][y] == Cube.NORMAL_HALLWAY) {
                    vertices.addAll(List.of(
                            0f+xp, -0.5f, 0f+yp, 0.0f, 0.0f, 0.2f, AO_MM,
                            1f+xp, -0.5f, 0f+yp, 1.0f, 0.0f, 0.2f, AO_PM,
                            1f+xp, -0.5f, 1f+yp, 1.0f, 1.0f, 0.2f, AO_PP,
                            1f+xp, -0.5f, 1f+yp, 1.0f, 1.0f, 0.2f, AO_PP,
                            0f+xp, -0.5f, 1f+yp, 0.0f, 1.0f, 0.2f, AO_MP,
                            0f+xp, -0.5f, 0f+yp, 0.0f, 0.0f, 0.2f, AO_MM,

                            -0f+xp, 0.5f, 0f+yp, 0.0f, 0.0f, 0.1f, AO_MM,
                            1f+xp, 0.5f, 0f+yp, 1.0f, 0.0f, 0.1f, AO_PM,
                            1f+xp, 0.5f, 1f+yp, 1.0f, 1.0f, 0.1f, AO_PP,
                            1f+xp, 0.5f, 1f+yp, 1.0f, 1.0f, 0.1f, AO_PP,
                            0f+xp, 0.5f, 1f+yp, 0.0f, 1.0f, 0.1f, AO_MP,
                            0f+xp, 0.5f, 0f+yp, 0.0f, 0.0f, 0.1f, AO_MM
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
