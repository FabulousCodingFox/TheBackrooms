import engine.Engine;
import org.joml.Vector3f;
import structures.Chunk;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Engine engine = new Engine(1280, 720, "The Backrooms [WIP]");


        ArrayList<Chunk> chunks = new ArrayList<>();
        chunks.add(new Chunk());
        chunks.get(0).generateMesh();


        boolean running = true;
        while (running) {
            running = engine.render(
                    chunks,
                    new Vector3f(0,0,0),
                    new Vector3f(0,0,1)
            );
        }
        engine.cleanup();
    }
}
