package engine;

public class Rect2D {
    private float x, y, w, h;

    public Rect2D(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.w = width;
        this.h = height;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return w;
    }

    public float getHeight() {
        return h;
    }

    public boolean doesIntersect(Rect2D other){
        // if rectangle has area 0, no overlap
        if (getWidth() == 0 ||getHeight() == 0 || other.getWidth() == 0 || other.getHeight() == 0) return false;

        // If one rectangle is on left side of other
        if (getX() > other.getX()+other.getWidth() || other.getX() > getX() + getWidth()) return false;

        // If one rectangle is above other
        if (getY() + getHeight() > other.getY() || other.getY() + other.getHeight() > getY()) return false;

        return true;
    }
}
