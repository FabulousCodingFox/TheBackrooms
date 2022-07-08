package engine.font;

import utils.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;

public class TextToTexture {
    private static Map<Character, BitmapChar> charMap;
    private static final int CHAR_WIDTH = 16;
    private static final int CHAR_HEIGHT = 16;
    private static final int TERMINAL_WIDTH = 80;
    private static final int TERMINAL_HEIGHT = 50;
    private static final Color TERMINAL_BG = Color.BLUE;
    private static final Color TERMINAL_FG = Color.CYAN;
    private static BufferedImage image;
    private static final String[] charTexMap = new String[]{
            "@abcdefghijklmno",
            "pqrstuvwxyz{~}~~",
            "~!\"#$%&'()*+,-./",
            "0123456789:;<=>?",
            "~ABCDEFGHIJKLMNO",
            "PQRSTUVWXYZ~~~~~"
    };

    public static void init() throws IOException {
        // Read the atlas
        BufferedImage atlas = ImageIO.read(new File(Objects.requireNonNull(FileUtils.class.getClassLoader().getResource("textures/font.png")).getPath()));
        image = new BufferedImage(TERMINAL_WIDTH * CHAR_WIDTH, TERMINAL_HEIGHT * CHAR_HEIGHT, BufferedImage.TYPE_INT_RGB);
        charMap = new java.util.HashMap<>();

        // Generate the character map
        for(int row=0; row<charTexMap.length; row++){
            for(int col=0; col<charTexMap[row].length(); col++){
                char c = charTexMap[row].charAt(col);
                if(c=='~') continue;

                BitmapChar bitmapChar = new BitmapChar(CHAR_WIDTH, CHAR_HEIGHT);
                for(int x=0; x<CHAR_WIDTH; x++){
                    for(int y=0; y<CHAR_HEIGHT; y++){
                        int xp = col*CHAR_WIDTH + x;
                        int yp = row*CHAR_HEIGHT + y + 2;

                        bitmapChar.setPixel(x, y,
                                new Color(atlas.getRGB(xp, yp), true).getAlpha() > 30
                        );
                    }
                }
                charMap.put(c, bitmapChar);
            }
        }
    }

    public static void RenderText(String text){
        Graphics2D g = image.createGraphics();
        g.setColor(TERMINAL_BG);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        int offsetX = 0;
        int offsetY = 0;
        for(int i=0; i<text.length(); i++){
            char c = text.charAt(i);
            if(c==' '){
                offsetX += CHAR_WIDTH;
                continue;
            }
            if(c=='\n'){
                offsetX = 0;
                offsetY += CHAR_HEIGHT;
                continue;
            }
            if(offsetX/CHAR_WIDTH >= TERMINAL_WIDTH){
                offsetX = 0;
                offsetY += CHAR_HEIGHT;
            }
            BitmapChar bitmapChar = charMap.get(c);
            if(bitmapChar==null){
                offsetX += CHAR_WIDTH;
                continue;
            }
            for(int x=0; x<CHAR_WIDTH; x++){
                for(int y=0; y<CHAR_HEIGHT; y++){
                    if(bitmapChar.getPixel(x, y)){
                        image.setRGB(offsetX + x, offsetY + y, TERMINAL_FG.getRGB());
                    }
                }
            }
            offsetX += CHAR_WIDTH;
        }
    }

    public static ByteBuffer getImageData(){
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
        for(int h = 0; h < image.getHeight(); h++) {
            for(int w = 0; w < image.getWidth(); w++) {
                int pixel = pixels[h * image.getWidth() + w];

                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();
        return buffer;
    }

    public static void main(String[] args) throws IOException {
        init();
        RenderText("Hello World!\nThis is a demo of the retro text-to-image converter meant for OpenGL applications. (It supports many C64 characters, but only the ones I've used.) Linebreaks are also supported and automatically inserted");
        ImageIO.write(image, "png", new File("C:\\Users\\fabif\\IdeaProjects\\TheBackrooms\\src\\main\\resources\\textures\\text.png"));
    }

}
