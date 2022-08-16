package me.curz46.chess4j.util;

public class Tuple<L, R> {
    private L left;
    private R right;

    public static <L, R> Tuple<L, R> of(L left, R right) {
        return new Tuple<>(left, right);
    }

    private Tuple(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public R getRight() {
        return right;
    }

    public void setRight(R right) {
        this.right = right;
    }
}
