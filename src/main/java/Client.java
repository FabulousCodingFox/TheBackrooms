import engine.Engine;
import engine.enums.Key;
import org.joml.Vector3f;
import structures.Chunk;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.stream.Collectors;

public class Client {
    private Engine engine;

    private Vector3f playerPosition, playerLookAt, playerPrevPosition;
    private double playerRotation;

    private float playerWalkSpeed, playerSprintSpeed, playerTurnSpeed, playerCrouchSpeed;

    private Thread chunkThread;

    private ArrayList<Chunk> chunks, chunksToDestroy, chunksToRender;
    private final int playerRenderDistance = 5;

    public Client(){
        engine = new Engine(1280,960, "The Backrooms");
        backrooms();
    }

    public void terminal(){
        String header = "***** BACKROOMS INC.[BackOS 64 Basic System] *****\n\n";
        String additionalText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim v";
        String prompt = "> ";

        int index = 0;

        double starttime = engine.getTime();

        System.out.println(header);

        boolean running = true;
        while (running) {
            running = engine.renderTerminal(header + prompt + additionalText);
        }
    }


    public void backrooms(){
        playerPosition = new Vector3f(0, 0, 0);
        playerPrevPosition = new Vector3f(0, 0, 0);
        playerLookAt = new Vector3f(0, 0, 1);
        playerRotation = 0d;

        playerWalkSpeed = 2f;
        playerSprintSpeed = playerWalkSpeed * 2f;
        playerTurnSpeed = 90f;
        playerCrouchSpeed = playerWalkSpeed / 2f;

        chunks = new ArrayList<>();
        chunksToDestroy = new ArrayList<>();
        chunksToRender = new ArrayList<>();

        // Load in the first chunks
        chunkThread = new Thread(this::updateChunks);
        chunkThread.start();


        boolean running = true;
        while (running) {
            try {
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
                playerLookAt = new Vector3f((float) Math.sin(-Math.toRadians(playerRotation)), 0, (float) Math.cos(-Math.toRadians(playerRotation)));
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
                    new ArrayList<>(chunks).stream().filter(Chunk::isReady).collect(Collectors.toCollection(ArrayList::new)),
                    playerPosition,
                    playerLookAt
            );

            if((int)(playerPosition.x/Chunk.SIZE)!=(int)(playerPrevPosition.x/Chunk.SIZE) || (int)(playerPosition.z/Chunk.SIZE)!=(int)(playerPrevPosition.z/Chunk.SIZE)){
                playerPrevPosition.set(playerPosition);
                if(chunkThread.isAlive()) chunkThread.interrupt();
                chunkThread = new Thread(this::updateChunks);
                chunkThread.start();
            }
            playerPrevPosition.set(playerPosition);
        }
        engine.cleanup();

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
            Chunk[] neighbors = new Chunk[4];
            for(Chunk search : chunks) {
                if(search.getX()==chunk.getX() && search.getY()==chunk.getY()-1){ sides++;neighbors[0] = search; }
                if(search.getX()==chunk.getX()-1 && search.getY()==chunk.getY()){ sides++;neighbors[1] = search; }
                if(search.getX()==chunk.getX()+1 && search.getY()==chunk.getY()){ sides++;neighbors[2] = search; }
                if(search.getX()==chunk.getX() && search.getY()==chunk.getY()+1){ sides++;neighbors[3] = search; }
            }
            chunk.setNeighbors(neighbors);
            if(sides == 4) {
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
            chunk.generateMesh( // -z -x +x +z
                    chunk.getNeighbors()[2],
                    chunk.getNeighbors()[1],
                    chunk.getNeighbors()[3],
                    chunk.getNeighbors()[0]
            );
            chunksToRender.add(chunk);
            chunk.setNeighbors(null);
        }
    }



}
