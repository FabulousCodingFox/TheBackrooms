package engine.font;

public class BitmapChar {
    private final int width, height;
    private final boolean[] pixels;

    public BitmapChar(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new boolean[width*height];
    }

    public void setPixel(int x, int y, boolean value) {
        pixels[x + y*width] = value;
    }

    public boolean getPixel(int x, int y) {
        return pixels[x + y*width];
    }
}
