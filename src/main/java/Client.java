import engine.Engine;
import engine.enums.Key;
import org.joml.Vector3f;
import structures.Chunk;

import java.util.ArrayList;

public class Client {
    private final Engine engine;

    private Vector3f playerPosition, playerLookAt;
    private double playerRotation;

    private float playerWalkSpeed, playerSprintSpeed, playerTurnSpeed, playerCrouchSpeed;

    public Client() {
        playerPosition = new Vector3f(0, 0, 0);
        playerLookAt = new Vector3f(0, 0, 1);
        playerRotation = 0d;

        playerWalkSpeed = 0.1f;
        playerSprintSpeed = 0.2f;
        playerTurnSpeed = 0.1f;
        playerCrouchSpeed = 0.05f;

        engine = new Engine(640,480, "The Backrooms");

        ArrayList<Chunk> chunks = new ArrayList<>();
        chunks.add(new Chunk());
        chunks.get(0).generateMesh();


        boolean running = true;
        while (running) {

            // Event Queue
            float deltaTime = engine.getFrameTime();

            boolean keyWalkForward = engine.getIfKeyIsPressed(Key.WALK_FORWARD);
            boolean keyWalkBackward = engine.getIfKeyIsPressed(Key.WALK_BACKWARD);
            boolean keyTurnLeft = engine.getIfKeyIsPressed(Key.TURN_LEFT);
            boolean keyTurnRight = engine.getIfKeyIsPressed(Key.TURN_RIGHT);
            boolean keySprint = engine.getIfKeyIsPressed(Key.SPRINT);
            boolean keyCrouch = engine.getIfKeyIsPressed(Key.CROUCH);

            if(keyTurnLeft || keyTurnRight){
                playerRotation += (keyTurnLeft ? -1 : 1) * playerTurnSpeed * deltaTime;
                if(playerRotation >= 360) playerRotation -= 360;
                if(playerRotation < 0) playerRotation += 360;
                playerLookAt = new Vector3f((float) Math.sin(Math.toRadians(playerRotation)), 0, (float) Math.cos(Math.toRadians(playerRotation)));
                playerLookAt.normalize();
            }

            if(keyWalkForward || keyWalkBackward) {
                float speed = keySprint ? playerSprintSpeed : playerWalkSpeed;
                float multiplier = (keyWalkForward ? 1 : -1) * deltaTime;
                if (keySprint && !keyCrouch) multiplier *= playerSprintSpeed;
                else if (!keySprint && keyCrouch) multiplier *= playerCrouchSpeed;
                else if (keySprint && keyCrouch) multiplier *= playerCrouchSpeed;
                else multiplier *= playerWalkSpeed;
                playerPosition.add(new Vector3f(playerLookAt).mul(multiplier));
            }

            // Render Queue
            running = engine.render(
                    chunks,
                    playerPosition,
                    playerLookAt
            );
        }
        engine.cleanup();

    }
}
