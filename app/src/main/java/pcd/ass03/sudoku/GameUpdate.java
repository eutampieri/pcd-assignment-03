package pcd.ass03.sudoku;

import java.util.Arrays;

public final class GameUpdate {
    private final int x;
    private final int y;
    private final int value;
    // Voluntary Y2k38 bug here!
    private final int time;
    private final ValueType type;

    public GameUpdate(String rawUpdate) {
        var split = Arrays.stream(rawUpdate.split(" ")).mapToInt(x -> Integer.parseInt(x)).iterator();
        this.x = split.next();
        this.y = split.next();
        this.value = split.next();
        this.time = split.next();
        this.type = ValueType.values()[split.next()];
    }

    public GameUpdate(int x, int y, int value, ValueType type) {
        this.x = x;
        this.y = y;
        this.value = value;
        this.time = (int)(System.currentTimeMillis() / 1000);
        this.type = type;
    }

    public String serialize() {
        return this.x + " " + this.y + " " + this.value + " " + this.time + " " + this.type.ordinal();
    }
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getValue() {
        return value;
    }

    public ValueType getType() {
        return this.type;
    }

    public int compare(GameUpdate other) {
        return Integer.compare(this.time, other.time);
    }
}
