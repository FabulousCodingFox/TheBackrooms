import engine.Engine;
import org.joml.Vector3f;
import structures.Chunk;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Engine engine = new Engine(1280, 720, "The Backrooms [WIP]");

        ArrayList<Chunk> chunks = new ArrayList<>(List.of(new Chunk(0, 0)));
        chunks.get(0).generateTerrain();
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
