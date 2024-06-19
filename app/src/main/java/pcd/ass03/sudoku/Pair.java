package pcd.ass03.sudoku;

import java.io.Serializable;

public class Pair<A, B> implements Serializable {
    private final A left;
    private final B right;

    public Pair(A left, B right) {
        this.left = left;
        this.right = right;
    }

    public A getLeft() {
        return this.left;
    }

    public B getRight() {
        return this.right;
    }
}
