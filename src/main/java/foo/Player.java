package foo;

public class Player implements Comparable<Player> {
    String name;
    Double lkValue;
    Double improvement;
    public Player(String name, double lk) {
        this.name = name;
        this.lkValue = lk;
        this.improvement = 0.0;
    }

    @Override
    public int compareTo(Player player) {
        return this.lkValue.compareTo(player.lkValue); // Ascending
    }
}
