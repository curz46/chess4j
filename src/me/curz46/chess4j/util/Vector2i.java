package me.curz46.chess4j.util;

public class Vector2i {
    public static final Vector2i a1 = new Vector2i(0, 0);
    public static final Vector2i a2 = new Vector2i(0, 1);
    public static final Vector2i a3 = new Vector2i(0, 2);
    public static final Vector2i a4 = new Vector2i(0, 3);
    public static final Vector2i a5 = new Vector2i(0, 4);
    public static final Vector2i a6 = new Vector2i(0, 5);
    public static final Vector2i a7 = new Vector2i(0, 6);
    public static final Vector2i a8 = new Vector2i(0, 7);
    public static final Vector2i b1 = new Vector2i(1, 0);
    public static final Vector2i b2 = new Vector2i(1, 1);
    public static final Vector2i b3 = new Vector2i(1, 2);
    public static final Vector2i b4 = new Vector2i(1, 3);
    public static final Vector2i b5 = new Vector2i(1, 4);
    public static final Vector2i b6 = new Vector2i(1, 5);
    public static final Vector2i b7 = new Vector2i(1, 6);
    public static final Vector2i b8 = new Vector2i(1, 7);
    public static final Vector2i c1 = new Vector2i(2, 0);
    public static final Vector2i c2 = new Vector2i(2, 1);
    public static final Vector2i c3 = new Vector2i(2, 2);
    public static final Vector2i c4 = new Vector2i(2, 3);
    public static final Vector2i c5 = new Vector2i(2, 4);
    public static final Vector2i c6 = new Vector2i(2, 5);
    public static final Vector2i c7 = new Vector2i(2, 6);
    public static final Vector2i c8 = new Vector2i(2, 7);
    public static final Vector2i d1 = new Vector2i(3, 0);
    public static final Vector2i d2 = new Vector2i(3, 1);
    public static final Vector2i d3 = new Vector2i(3, 2);
    public static final Vector2i d4 = new Vector2i(3, 3);
    public static final Vector2i d5 = new Vector2i(3, 4);
    public static final Vector2i d6 = new Vector2i(3, 5);
    public static final Vector2i d7 = new Vector2i(3, 6);
    public static final Vector2i d8 = new Vector2i(3, 7);
    public static final Vector2i e1 = new Vector2i(4, 0);
    public static final Vector2i e2 = new Vector2i(4, 1);
    public static final Vector2i e3 = new Vector2i(4, 2);
    public static final Vector2i e4 = new Vector2i(4, 3);
    public static final Vector2i e5 = new Vector2i(4, 4);
    public static final Vector2i e6 = new Vector2i(4, 5);
    public static final Vector2i e7 = new Vector2i(4, 6);
    public static final Vector2i e8 = new Vector2i(4, 7);
    public static final Vector2i f1 = new Vector2i(5, 0);
    public static final Vector2i f2 = new Vector2i(5, 1);
    public static final Vector2i f3 = new Vector2i(5, 2);
    public static final Vector2i f4 = new Vector2i(5, 3);
    public static final Vector2i f5 = new Vector2i(5, 4);
    public static final Vector2i f6 = new Vector2i(5, 5);
    public static final Vector2i f7 = new Vector2i(5, 6);
    public static final Vector2i f8 = new Vector2i(5, 7);
    public static final Vector2i g1 = new Vector2i(6, 0);
    public static final Vector2i g2 = new Vector2i(6, 1);
    public static final Vector2i g3 = new Vector2i(6, 2);
    public static final Vector2i g4 = new Vector2i(6, 3);
    public static final Vector2i g5 = new Vector2i(6, 4);
    public static final Vector2i g6 = new Vector2i(6, 5);
    public static final Vector2i g7 = new Vector2i(6, 6);
    public static final Vector2i g8 = new Vector2i(6, 7);
    public static final Vector2i h1 = new Vector2i(7, 0);
    public static final Vector2i h2 = new Vector2i(7, 1);
    public static final Vector2i h3 = new Vector2i(7, 2);
    public static final Vector2i h4 = new Vector2i(7, 3);
    public static final Vector2i h5 = new Vector2i(7, 4);
    public static final Vector2i h6 = new Vector2i(7, 5);
    public static final Vector2i h7 = new Vector2i(7, 6);
    public static final Vector2i h8 = new Vector2i(7, 7);

    private final int x;
    private final int y;

    public static Vector2i from(int x, int y) {
        return new Vector2i(x, y);
    }

    private Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Vector2i add(Vector2i v) {
        return new Vector2i(x + v.x, y + v.y);
    }

    public Vector2i add(int x, int y) {
        return new Vector2i(this.x + x, this.y + y);
    }

    public Vector2i sub(Vector2i v) {
        return new Vector2i(x - v.x, y - v.y);
    }

    public Vector2i sub(int x, int y) {
        return new Vector2i(this.x - x, this.y - y);
    }

    public double distance(Vector2i v) {
        return Math.sqrt((x * v.x) + (y * v.y));
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Vector2i))
            return false;
        return ((Vector2i) obj).getX() == x && ((Vector2i) obj).getY() == y;
    }
}
