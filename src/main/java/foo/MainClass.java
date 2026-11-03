package foo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainClass {
    static int maxEncountersPerPair = 4;
    static List<Player> playerList = new ArrayList<>();
    static List<Match> matchList = new ArrayList<>();
    static Set<String> participatedPlayers = new HashSet<>();
    static String title = "";

    public static void main(String[] arg) {

        try {
            FileInputStream inputStream = new FileInputStream("input.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = reader.readLine()) != null) {
                if (line.startsWith("TITLE")) {
                    title = line.split("TITLE")[1].trim();
                } else if (line.startsWith("ADD")) {
                    String content = line.split("ADD")[1];
                    String name = content.split(",")[0].trim().toUpperCase();
                    double lk = Double.parseDouble(content.split(",")[1]);
                    playerList.add(new Player(name, lk));
                } else if (line.startsWith("CALIBRATE")){
                    calibrate();
                } else if (line.startsWith("MATCH")){
                    String content = line.split("MATCH")[1].trim();
                    String date = content.split(",")[0].trim().toUpperCase();
                    LocalDate localDate = LocalDate.parse(date);
                    long dateSeconds = localDate.atTime(12,0).toEpochSecond(ZoneOffset.UTC);

                    String winner = content.split(",")[1].trim().toUpperCase();
                    String loser = content.split(",")[2].trim().toUpperCase();
                    matchList.add(new Match(winner, loser, dateSeconds));
                }

            }
        } catch (IOException e) {
            System.out.println(e);
        }

        Collections.sort(matchList); //sort matches by date
        // Calculate rankings
        for (int i=0; i < matchList.size(); i++){
            if (matchList.get(i).winner.equals("-")){
                System.out.println("\n"+matchList.get(i).getDateTime()+" " + matchList.get(i).loser+" nicht angetreten.");
            } else {
                System.out.println("\n"+matchList.get(i).getDateTime()+" " + matchList.get(i).winner + " schlägt " + matchList.get(i).loser);
            }
            updateImprovement(matchList.get(i));

            if (i+1 < matchList.size() && (matchList.get(i+1).date == matchList.get(i).date )) { //first update all improvements for same timestamp, then update LKs
                System.out.println("\nCollecting matches with same timestamp");
                continue;
            }
            updateLKs();
            Collections.sort(playerList);
            listRanking();
        }

        System.out.println("\nEndstand");
        listRanking();

        writeHtmlRanking();
        writeHtmlMatchlist();
        writeMarkdown();

        //Print warnings
        System.out.println("\nWarnungen:");
        checkMissingParticipants();
        checkDuplicateMatches();
        checkFrequentPairEncounters();
    }

    private static void writeMarkdown() {
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(Paths.get("current.md")))) {
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String today = LocalDateTime.now(ZoneOffset.UTC).format(dateFormat);

            writer.println("# " + title);
            writer.println();
            writer.println("<h2 style=\"margin-left: 12px; margin-top: 0; margin-bottom: 0;\">Stand: " + today + "</h2>");
            writer.println("<p>&nbsp;</p>");
            writer.println("<table style=\"font-size: 24px; border-collapse: collapse;\">");
            writer.println("  <thead>");
            writer.println("    <tr>");
            writer.println("      <th style=\"padding: 1px 12px; text-align: left;\">Platz</th>");
            writer.println("      <th style=\"padding: 1px 12px; text-align: left;\">Spieler</th>");
            writer.println("      <th style=\"padding: 1px 12px; text-align: left;\">LK</th>");
            writer.println("    </tr>");
            writer.println("  </thead>");
            writer.println("  <tbody>");

            Collections.sort(playerList);
            int rank = 1;
            for (Player p : playerList) {
                writer.println("    <tr>");
                writer.println("      <td style=\"padding: 1px 12px;\">" + rank + "</td>");
                writer.println("      <td style=\"padding: 1px 12px;\">" + p.name + "</td>");
                writer.println("      <td style=\"padding: 1px 12px;\">" + String.format("%.3f", p.lkValue) + "</td>");
                writer.println("    </tr>");
                rank++;
            }

            writer.println("  </tbody>");
            writer.println("</table>");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static void checkFrequentPairEncounters() {
        // Map to store how many times each unique pair has played
        Map<String, Integer> pairCounts = new HashMap<>();

        for (Match match : matchList) {
            String p1 = match.winner;
            String p2 = match.loser;

            // Normalize the pair so "A vs B" and "B vs A" map to the same key
            String pairKey = p1.compareTo(p2) < 0 ? p1 + " vs " + p2 : p2 + " vs " + p1;

            pairCounts.put(pairKey, pairCounts.getOrDefault(pairKey, 0) + 1);
        }

        // Report pairs that exceeded 4 encounters
        for (Map.Entry<String, Integer> entry : pairCounts.entrySet()) {
            if (entry.getValue() > maxEncountersPerPair) {
                System.out.println("* Das Spielerpaar " + entry.getKey()
                        + " hatte mehr als " + maxEncountersPerPair + " Begegnungen (insgesamt: " + entry.getValue() + ").");
            }
        }
    }

    private static void checkMissingParticipants() {
        List<String> notParticipated = new ArrayList<>();
        for (Player p : playerList) {
            if (!participatedPlayers.contains(p.name)) {
                notParticipated.add(p.name);
            }
        }
        if (!notParticipated.isEmpty()) {
            System.out.println("* Spieler ohne Spielteilnahme:");
            for (String name : notParticipated) {
                System.out.println("* " + name);
            }
        }
    }

    private static void checkDuplicateMatches() {
        for (int i = 0; i < matchList.size(); i++) {
            for (int j = i + 1; j < matchList.size(); j++) {
                Match m1 = matchList.get(i);
                Match m2 = matchList.get(j);
                if (m1.getDate().equals(m2.getDate())
                        && m1.winner.equals(m2.winner)
                        && m1.loser.equals(m2.loser)) {
                    System.out.println("* Duplikat erkannt am " + m1.getDateTime()
                            + ": " + m1.winner + " schlägt " + m1.loser);
                }
            }
        }
    }

    private static void writeHtmlMatchlist() {
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(Paths.get("matches.html")))) {
            for (int i=0; i < matchList.size(); i++){
                writer.println("<tr>");
                writer.println("  <td align=\"left\">" + matchList.get(i).getDateTime() + "</td>");
                writer.println("  <td align=\"left\">" + matchList.get(i).winner + "</td>");
                writer.println("  <td align=\"left\">" + matchList.get(i).loser + "</td>");
                writer.println("</tr>");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static void writeHtmlRanking(){
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(Paths.get("current.html")))) {
            // Sort players by lkValue ascending
            Collections.sort(playerList);

            for (Player p : playerList) {
                writer.println("<tr>");
                writer.println("  <td align=\"left\">" + p.name + "</td>");
                writer.println("  <td align=\"left\">" + String.format("%.3f", p.lkValue).replace('.', ',') + "</td>");
                writer.println("</tr>");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static void listRanking() {
        Collections.sort(playerList); // Sorts by original lkValue ascending
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(Paths.get("current.csv")))) {
            for (int i=0; i < playerList.size(); i++){
                String line = playerList.get(i).name + " " + String.format("%.3f",playerList.get(i).lkValue);
                System.out.println(line);
                writer.println(line);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void calibrate() {
        System.out.println("Ausgangslage");
        listRanking();

        int n = playerList.size();
        if (n < 2) return;

        //Find number of groups in case players have identical LK
        int i = 0;
        int numGroups = 0;
        while (i < n) {
            int groupStart = i;
            while (i + 1 < n && playerList.get(i + 1).lkValue.equals(playerList.get(i).lkValue)) {
                i++;
            }
            int groupSize = i - groupStart + 1;
            numGroups++;
            i = groupStart + groupSize;
        }
        //System.out.println(numGroups + " Gruppen");

        //Calculate step size
        double step = 10.0 / (numGroups -1);
        //System.out.println("Schrittgröße " + step);

        //Equally distribute LK between 15 and 25
        i=0;
        int group=0;
        while (i < n) {
            int groupStart = i;
            // Find all players with the same original lkValue
            while (i + 1 < n && playerList.get(i + 1).lkValue.equals(playerList.get(i).lkValue)) {
                i++;
            }
            int groupSize = i - groupStart + 1;

            // Assign the same adjusted value to all players in this group
            double adjustedValue = 15.0 + group * step;
            for (int j = groupStart; j <= i; j++) {
                playerList.get(j).lkValue = adjustedValue;
            }
            group++;
            i = groupStart + groupSize;
        }
        System.out.println("\nAusgangslage nach Skalierung");
        listRanking();
    }

    public static void updateImprovement(Match match){
        Player winner = null;
        Player loser = null;

        if (match.winner.equals("-")){
            for (int i=0; i < playerList.size(); i++){
                if (match.loser.equals(playerList.get(i).name)) loser=playerList.get(i);
            }
            loser.improvement = loser.improvement - 0.3;
            participatedPlayers.add(match.loser);
        } else {
            for (int i=0; i < playerList.size(); i++){
                if (match.loser.equals(playerList.get(i).name)) loser=playerList.get(i);
                if (match.winner.equals(playerList.get(i).name)) winner = playerList.get((i));
            }
            assert winner != null;
            assert loser != null;

            participatedPlayers.add(winner.name);
            participatedPlayers.add(loser.name);

            winner.improvement = winner.improvement + CalcLK.calcImprovement(winner.lkValue, loser.lkValue, 2.0); //Doppelte Punkte im Verein für mehr Dynamik. Damit kann sich ein guter Spieler innerhalb einer Saison von ganz hinten nach vorne spielen
        }
    }

     static void updateLKs(){ //update LKs with cumulated improvements and reset improvements
        for (Player player: playerList){
            player.lkValue = Math.min(player.lkValue - player.improvement, 25.0);
            player.improvement = 0.0;
        }
    }
}
