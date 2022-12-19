package engine;

public class Rect2D {
    private float x, y, w, h;

    public Rect2D(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.w = width;
        this.h = height;
    }

    public Rect2D(float x, float y, float width, float height){
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

        double rect1MinX = this.getX();
        double rect1MaxX = this.getX() + this.getWidth();
        double rect1MinY = this.getY();
        double rect1MaxY = this.getY() + this.getHeight();

        double rect2MinX = other.getX();
        double rect2MaxX = other.getX() + other.getWidth();
        double rect2MinY = other.getY();
        double rect2MaxY = other.getY() + other.getHeight();

        return (rect1MinX <= rect2MaxX && rect1MaxX >= rect2MinX) &&
                (rect1MinY <= rect2MaxY && rect1MaxY >= rect2MinY);
    }

    public boolean isInside(float xp, float yp){
        return ((xp >= getX() && xp <= getX() + getWidth()) && (yp >= getY() && yp <= getY() + getHeight()));
    }
}
