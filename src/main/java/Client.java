import engine.Engine;
import engine.enums.Key;
import org.joml.Vector3f;
import structures.Chunk;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.stream.Collectors;

public class Client {
    private final Engine engine;

    private Vector3f playerPosition, playerLookAt, playerPrevPosition;
    private double playerCrouchAnim;
    private double yaw, pitch;

    private final float playerWalkSpeed, playerSprintSpeed, playerTurnSpeed, playerCrouchSpeed;

    private float bobbingOffsetX = 0.0f, bobbingOffsetY = 0.0f;

    private Thread chunkThread;

    private final ArrayList<Chunk> chunks, chunksToDestroy, chunksToRender;

    private int playerRenderDistance = 6;

    public final Vector3f worldUp = new Vector3f(0,1,0);

    public Client(){
        engine = new Engine(1280,960, "The Backrooms");

        playerPosition = new Vector3f(0, 0, 0);
        playerPrevPosition = new Vector3f(0, 0, 0);
        playerLookAt = new Vector3f(0, 0, 1);
        yaw = 0d;
        pitch = 0d;

        playerWalkSpeed = 2f;
        playerSprintSpeed = playerWalkSpeed * 2f;
        playerTurnSpeed = 25f;
        playerCrouchSpeed = playerWalkSpeed / 2f;

        playerCrouchAnim = 0;

        chunks = new ArrayList<>();
        chunksToDestroy = new ArrayList<>();
        chunksToRender = new ArrayList<>();

        // Load in the first chunks
        chunkThread = new Thread(this::updateChunks);
        chunkThread.start();

        while (true) {
            if(terminal()) break;
            if(backrooms()) break;
        }

        engine.cleanup();
    }

    public boolean terminal(){
        String header = "***** BACKROOMS INC.[BackOS 64 Basic System] *****\n\n";

        String additionalText = "";

        int index = 0;

        engine.clearTypedText();

        boolean running = true;
        while (running) {
            if(engine.getTypedText().contains("\n")){
                additionalText += engine.getTypedText().split("\n")[0] + "\n";

                String cmd = engine.getTypedText().split("\n")[0].substring(2);
                if(cmd.equalsIgnoreCase("start")) return false;
                if(cmd.equalsIgnoreCase("exit")) return true;
                if(cmd.startsWith("shader ") && cmd.split(" ").length == 2){
                    if(cmd.endsWith("0")) engine.setPostShader(0);
                    if(cmd.endsWith("1")) engine.setPostShader(1);
                    if(cmd.endsWith("2")) engine.setPostShader(2);
                }
                if(cmd.startsWith("rd ") && cmd.split(" ").length == 2){
                    try{
                        int number = Integer.parseInt(cmd.split(" ")[1]);
                        System.out.println(number);
                        playerRenderDistance = number;
                    }catch (NumberFormatException ex){
                        ex.printStackTrace();
                    }
                }
                if(cmd.startsWith("light ") && cmd.split(" ").length == 2){
                    if(cmd.endsWith("0")) engine.setLightingEnabled(false);
                    if(cmd.endsWith("1")) engine.setLightingEnabled(true);
                }

                engine.clearTypedText();
            }

            running = engine.renderTerminal(header+additionalText+engine.getTypedText());
        }
        return true;
    }


    public boolean backrooms(){
        boolean running = true;

        double timestamp = engine.getTime();
        int fps = 0;

        while (running) {
            try {
                if(engine.getTime()-timestamp > 1d){
                    timestamp = engine.getTime();
                    System.out.println("[FPS]: "+fps);
                    fps = 0;
                }

                fps++;

                // Update Chunks
                ArrayList<Chunk> chunkToRender = new ArrayList<>(chunksToRender);
                for (Chunk chunk : chunkToRender) {
                    chunk.generateVBO();
                }
                chunksToRender.removeIf(chunkToRender::contains);
                ArrayList<Chunk> chunkToDestroy = new ArrayList<>(chunksToDestroy);
                for (Chunk chunk : chunkToDestroy) {
                    chunk.destroy();
                }
                chunksToDestroy.removeIf(chunkToDestroy::contains);
                chunks.removeIf(chunkToDestroy::contains);
            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
            }

            // Event Queue
            float deltaTime = engine.getFrameTime();

            double mouseX = engine.getMouseMoveX();
            double mouseY = engine.getMouseMoveY();
            yaw = yaw + mouseX * playerTurnSpeed * deltaTime;
            pitch = pitch - mouseY * playerTurnSpeed * deltaTime;

            if(pitch < -90) pitch=-90;
            if(pitch > 90) pitch=90;
            if(yaw >= 360) yaw-=360;
            if(yaw < 0) yaw+=360;

            Vector3f front = new Vector3f();
            front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
            front.y = (float) Math.sin(Math.toRadians(pitch));
            front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
            playerLookAt = front.normalize();

            boolean keyWalkForward = engine.getIfKeyIsPressed(Key.WALK_FORWARD);
            boolean keyWalkBackward = engine.getIfKeyIsPressed(Key.WALK_BACKWARD);
            boolean keyWalkLeft = engine.getIfKeyIsPressed(Key.WALK_LEFT);
            boolean keyWalkRight = engine.getIfKeyIsPressed(Key.WALK_RIGHT);
            boolean keySprint = engine.getIfKeyIsPressed(Key.SPRINT);
            boolean keyCrouch = engine.getIfKeyIsPressed(Key.CROUCH);

            boolean keyTerminal = engine.getIfKeyIsPressed(Key.TERMINAL);
            if(keyTerminal) return false;

            // Crouching

            if(keyCrouch && playerCrouchAnim>-0.3){
                playerCrouchAnim-= 0.3 * (deltaTime/0.2);
            }
            else if (!keyCrouch && playerCrouchAnim<0) {
                playerCrouchAnim+= 0.3 * (deltaTime/0.2);
            }

            if(keyWalkForward || keyWalkBackward || keyWalkLeft || keyWalkRight) {
                float speed = keySprint ? playerSprintSpeed : playerWalkSpeed;
                float multiplier = deltaTime;
                if (keySprint && !keyCrouch) multiplier *= playerSprintSpeed;
                else if (!keySprint && keyCrouch) multiplier *= playerCrouchSpeed;
                else if (keySprint && keyCrouch) multiplier *= playerCrouchSpeed;
                else multiplier *= playerWalkSpeed;

                Vector3f ns = new Vector3f(playerLookAt.x, 0, playerLookAt.z).mul(keyWalkForward?1:(keyWalkBackward?-1:0));
                Vector3f ow = new Vector3f(playerLookAt.x, 0, playerLookAt.z).cross(worldUp).mul(keyWalkRight?0.5f:(keyWalkLeft?-0.5f:0));
                Vector3f dir = ns.add(ow).normalize().mul(multiplier);

                playerPosition = new Vector3f(playerPosition).add(dir);
            }

            // View Bobbing

            float bobbingSpeed = 0f;
            if(keyCrouch){ bobbingSpeed = 0f; }
            else if(keySprint && (keyWalkForward || keyWalkBackward || keyWalkLeft || keyWalkRight)){ bobbingSpeed = 0.025f; }
            else if(keyWalkForward || keyWalkBackward || keyWalkLeft || keyWalkRight){ bobbingSpeed = 0.0125f; }

            bobbingOffsetY += 0.1f;
            if (bobbingOffsetY > Math.PI * 2) {
                bobbingOffsetY -= Math.PI * 2;
            }
            bobbingOffsetX += (0.1f / 2);
            if (bobbingOffsetX > Math.PI * 2) {
                bobbingOffsetX -= Math.PI * 2;
            }
            float bobbingAmountY = (float) Math.sin(bobbingOffsetY) * bobbingSpeed;
            float bobbingAmountX = (float) Math.sin(bobbingOffsetX) * (bobbingSpeed * 0.5f);

            Vector3f camLocation = new Vector3f(playerPosition.x, (float) playerCrouchAnim + bobbingAmountY, playerPosition.z);
            camLocation.add(new Vector3f(playerLookAt).cross(worldUp).mul(bobbingAmountX));

            // Render Queue
            running = engine.render(
                    new ArrayList<>(chunks).stream().filter(Chunk::isReady).collect(Collectors.toCollection(ArrayList::new)),
                    camLocation,
                    playerLookAt,
                    playerRenderDistance
            );

            if((int)(playerPosition.x/Chunk.SIZE)!=(int)(playerPrevPosition.x/Chunk.SIZE) || (int)(playerPosition.z/Chunk.SIZE)!=(int)(playerPrevPosition.z/Chunk.SIZE)){
                playerPrevPosition.set(playerPosition);
                if(chunkThread.isAlive()) chunkThread.interrupt();
                chunkThread = new Thread(this::updateChunks);
                chunkThread.start();
            }
            playerPrevPosition.set(playerPosition);
        }
        return true;
    }

    public void updateChunks() {
        // Get the players position
        int x = (int) (playerPosition.x /  Chunk.SIZE);
        int z = (int) (playerPosition.z /  Chunk.SIZE);

        // Remove all chunks outside the render distance
        chunks.forEach(chunk -> {if(chunk.getX() > x + playerRenderDistance + 1 || chunk.getX() < x - playerRenderDistance - 1 || chunk.getY() > z + playerRenderDistance + 1 || chunk.getY() < z - playerRenderDistance - 1) chunksToDestroy.add(chunk);});
        chunks.removeIf(chunk -> chunk.getX() > x + playerRenderDistance + 1 || chunk.getX() < x - playerRenderDistance - 1 || chunk.getY() > z + playerRenderDistance + 1 || chunk.getY() < z - playerRenderDistance - 1);

        // Adding chunks to the list that are within the render distance
        ArrayList<Chunk> newChunks = new ArrayList<>();
        for(int i = x - playerRenderDistance; i <= x + playerRenderDistance; i++) {
            for(int j = z - playerRenderDistance; j <= z + playerRenderDistance; j++) {
                int finalI = i;
                int finalJ = j;
                if(chunks.stream().noneMatch(chunk -> chunk.getX() == finalI && chunk.getY() == finalJ)) {
                    newChunks.add(new Chunk(i, j));
                }
            }
        }
        chunks.addAll(newChunks);

        ArrayList<Chunk> chunksToGenerate = new ArrayList<>();

        for(Chunk chunk : chunks) {
            if(chunk.getMesh() != null) continue;

            int sides = 0;
            Chunk[] neighbors = new Chunk[8];
            for(Chunk search : chunks) {
                //px_pz, px_nz, px_mz, mx_pz, mx_nz, mx_mz, nx_pz, nx_mz
                if(search.getX()==chunk.getX()+1 && search.getY()==chunk.getY()+1){ sides++;neighbors[0] = search; } //px_pz
                if(search.getX()==chunk.getX()+1 && search.getY()==chunk.getY())  { sides++;neighbors[1] = search; } //px_nz
                if(search.getX()==chunk.getX()+1 && search.getY()==chunk.getY()-1){ sides++;neighbors[2] = search; } //px_mz

                if(search.getX()==chunk.getX()-1 && search.getY()==chunk.getY()+1){ sides++;neighbors[3] = search; } //mx_pz
                if(search.getX()==chunk.getX()-1 && search.getY()==chunk.getY())  { sides++;neighbors[4] = search; } //mx_nz
                if(search.getX()==chunk.getX()-1 && search.getY()==chunk.getY()-1){ sides++;neighbors[5] = search; } //mx_mz

                if(search.getX()==chunk.getX() && search.getY()==chunk.getY()+1){ sides++;neighbors[6] = search; } //nx_pz
                if(search.getX()==chunk.getX() && search.getY()==chunk.getY()-1){ sides++;neighbors[7] = search; } //nx_mz
            }
            chunk.setNeighbors(neighbors);
            if(sides == 8) {
                chunksToGenerate.add(chunk);
            }
        }

        //Sort chunks by distance
        chunksToGenerate.sort((a, b) -> {
            double valueA = Math.sqrt(Math.pow(a.getX() - x, 2) + Math.pow(a.getY() - z, 2));
            double valueB = Math.sqrt(Math.pow(b.getX() - x, 2) + Math.pow(b.getY() - z, 2));
            int value = Double.compare(valueA, valueB);
            return Integer.compare(value, 0);
        });

        //Generate Chunks
        for(Chunk chunk : chunksToGenerate) {
            if(chunk.getCube(0,0)==null) chunk.generateTerrain();
            if(chunk.getNeighbors()[0].getCube(0,0)==null){ chunk.getNeighbors()[0].generateTerrain();}
            if(chunk.getNeighbors()[1].getCube(0,0)==null){ chunk.getNeighbors()[1].generateTerrain();}
            if(chunk.getNeighbors()[2].getCube(0,0)==null){ chunk.getNeighbors()[2].generateTerrain();}
            if(chunk.getNeighbors()[3].getCube(0,0)==null){ chunk.getNeighbors()[3].generateTerrain();}
            if(chunk.getNeighbors()[4].getCube(0,0)==null){ chunk.getNeighbors()[4].generateTerrain();}
            if(chunk.getNeighbors()[5].getCube(0,0)==null){ chunk.getNeighbors()[5].generateTerrain();}
            if(chunk.getNeighbors()[6].getCube(0,0)==null){ chunk.getNeighbors()[6].generateTerrain();}
            if(chunk.getNeighbors()[7].getCube(0,0)==null){ chunk.getNeighbors()[7].generateTerrain();}
            chunk.generateMesh(
                    chunk.getNeighbors()[0],
                    chunk.getNeighbors()[1],
                    chunk.getNeighbors()[2],
                    chunk.getNeighbors()[3],
                    chunk.getNeighbors()[4],
                    chunk.getNeighbors()[5],
                    chunk.getNeighbors()[6],
                    chunk.getNeighbors()[7]
            );
            chunksToRender.add(chunk);
            chunk.setNeighbors(null);
        }
    }



}
