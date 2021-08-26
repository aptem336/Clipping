import java.awt.*;

public class Vector {
    private float x;
    private float y;

    public Vector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Vector sum(Vector vector) {
        return new Vector(x + vector.x, y + vector.y);
    }

    public Vector difference(Vector vector) {
        return new Vector(x - vector.x, y - vector.y);
    }

    public Vector product(float coefficient) {
        return new Vector(x * coefficient, y * coefficient);
    }

    public Vector rightNormal() {
        return new Vector(-y, x);
    }

    public Vector leftNormal() {
        return new Vector(y, -x);
    }

    public float dotProduct(Vector vector) {
        return x * vector.x + y * vector.y;
    }

    public float crossProduct(Vector vector) {
        return x * vector.y - y * vector.x;
    }
}
