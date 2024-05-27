package pcd.ass03.sudokuMOM;

import java.util.Arrays;

public final class GameUpdate {
    private final int x;
    private final int y;
    private final int value;
    // Voluntary Y2k38 bug here!
    private final int time;

    public GameUpdate(String rawUpdate) {
        var split = Arrays.stream(rawUpdate.split(" ")).mapToInt(x -> Integer.parseInt(x)).iterator();
        this.x = split.next();
        this.y = split.next();
        this.value = split.next();
        this.time = split.next();
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

    public int compare(GameUpdate other) {
        return Integer.compare(this.time, other.time);
    }
}
