package foo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainClass {
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
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);
                    long dateSeconds = localDateTime.toEpochSecond(ZoneOffset.UTC);

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
            updateLK(matchList.get(i));
            listRanking();
        }

        System.out.println("\nEndstand");
        listRanking();

        writeHtmlRanking();
        writeHtmlMatchlist();
        writeMarkdown();

        checkMissingParticipants();
        checkDuplicateMatches();
    }

    private static void writeMarkdown() {
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(Paths.get("current.md")))) {
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String today = LocalDateTime.now(ZoneOffset.UTC).format(dateFormat);

            writer.println("# " );
            writer.println("## " + title);
            writer.println();
            writer.println("### **Stand: " + today + "**");
            writer.println("<p>&nbsp;</p>");
            writer.println("| Platz&nbsp;&nbsp;&nbsp; | Spieler | LK |");
            writer.println("| :--- | :--- | :--- |");

            Collections.sort(playerList);
            int rank = 1;
            for (Player p : playerList) {
                writer.println("| " + rank + " | " + p.name + " | " + String.format("%.3f", p.lkValue) + " |");
                rank++;
            }
        } catch (IOException e) {
            System.out.println(e);
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
            System.out.println("\nSpieler ohne Spielteilnahme:");
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
                    System.out.println("\nWarnung: Duplikat erkannt am " + m1.getDateTime()
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

    public static void updateLK(Match match){
        double winnerLK;
        Player winner = null;
        Player loser = null;

        if (match.winner.equals("-")){
            for (int i=0; i < playerList.size(); i++){
                if (match.loser.equals(playerList.get(i).name)) loser=playerList.get(i);
            }
            loser.lkValue = Math.min(loser.lkValue + 0.3, 25.0);
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

            winnerLK = CalcLK.calculate(winner.lkValue, loser.lkValue);
            winner.lkValue = winnerLK;
        }

        Collections.sort(playerList);
    }
}
