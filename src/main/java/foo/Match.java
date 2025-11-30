package foo;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Match implements Comparable<Match>{
    String winner;
    String loser;
    long date;
    public Match(String winner, String loser, long date) {
        this.winner = winner;
        this.loser = loser;
        this.date = date;
    }
    @Override
    public int compareTo(Match match) {
        return Long.compare(this.date, match.date); // Ascending order
    }

    public String getFormattedDate() {
        return Instant.ofEpochSecond(date)
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
