package foo;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Match implements Comparable<Match>{
    String winner;
    String loser;
    long date;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

    public Match(String winner, String loser, long date) {
        this.winner = winner;
        this.loser = loser;
        this.date = date;
    }
    @Override
    public int compareTo(Match match) {
        return Long.compare(this.date, match.date); // Ascending order
    }

    public String getDate() {
        return DATE_FORMAT.format(Instant.ofEpochSecond(date));
    }

    public String getDateTime() {
        return DATETIME_FORMAT.format(Instant.ofEpochSecond(date));
    }

}
