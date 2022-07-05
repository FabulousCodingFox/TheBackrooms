import engine.Engine;
import org.joml.Vector3f;

public class Main {
    public static void main(String[] args) {
        Engine engine = new Engine(1280, 720, "Backrooms '80");
        boolean running = true;
        while (running) {
            running = engine.render(
                    new Vector3f(0,0,0),
                    new Vector3f(0,0,1)
            );
        }
        engine.cleanup();
    }
}
