public class Parallelepiped {
    public Vector max; // Координаты левого верхнего угла ближней грани
    public Vector min; // Координаты правого нижнего угла дальней грани

    public Parallelepiped(Vector max, Vector min) {
        this.max = max;
        this.min = min;
    }
}
