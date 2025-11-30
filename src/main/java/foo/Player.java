package foo;

public class Player implements Comparable<Player> {
    String name;
    Double lkValue;
    public Player(String name, double lk) {
        this.name = name;
        this.lkValue = lk;
    }

    @Override
    public int compareTo(Player player) {
        return this.lkValue.compareTo(player.lkValue); // Ascending
    }
}
