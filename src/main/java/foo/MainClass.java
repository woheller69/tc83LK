package foo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainClass {
    static List<Player> playerList = new ArrayList<>();
    static List<Match> matchList = new ArrayList<>();
    public static void main(String[] arg) {

        try {
            FileInputStream inputStream = new FileInputStream("input.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = reader.readLine()) != null) {
                if (line.startsWith("ADD")) {
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
                System.out.println("\n"+matchList.get(i).getFormattedDate()+" " + matchList.get(i).loser+" nicht angetreten.");
            } else {
                System.out.println("\n"+matchList.get(i).getFormattedDate()+" " + matchList.get(i).winner + " schlägt " + matchList.get(i).loser);
            }
            updateLK(matchList.get(i));
            listRanking();
        }

        System.out.println("\nEndstand");
        listRanking();
        System.out.println("\n\nHTML RANKING\n");
        printHtmlRanking();
        System.out.println("\n\nHTML MATCHLIST\n");
        printHtmlMatchlist();
    }

    private static void printHtmlMatchlist() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < matchList.size(); i++){
            sb.append("<tr>\n");
            sb.append("  <td align=\"left\">").append(matchList.get(i).getFormattedDate()).append("</td>\n");
            sb.append("  <td align=\"left\">").append(matchList.get(i).winner).append("</td>\n");
            sb.append("  <td align=\"left\">").append(matchList.get(i).loser).append("</td>\n");
            sb.append("</tr>\n");
        }
        System.out.println(sb.toString());
    }

    private static void printHtmlRanking(){
        StringBuilder sb = new StringBuilder();

        // Sort players by lkValue ascending
        Collections.sort(playerList);

        for (Player p : playerList) {
            sb.append("<tr>\n");
            sb.append("  <td align=\"left\">").append(p.name).append("</td>\n");
            // Format lkValue - use comma as decimal separator if needed
            sb.append("  <td align=\"left\">").append(String.format("%.3f", p.lkValue).replace('.', ',')).append("</td>\n");
            sb.append("</tr>\n");
        }
        System.out.println(sb.toString());
    }
    private static void listRanking() {
        Collections.sort(playerList); // Sorts by original lkValue ascending
        for (int i=0; i < playerList.size(); i++){
            System.out.println(playerList.get(i).name + " " + String.format("%.3f",playerList.get(i).lkValue));
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
        } else {
            for (int i=0; i < playerList.size(); i++){
                if (match.loser.equals(playerList.get(i).name)) loser=playerList.get(i);
                if (match.winner.equals(playerList.get(i).name)) winner = playerList.get((i));
            }
            assert winner != null;
            assert loser != null;

            winnerLK = CalcLK.calculate(winner.lkValue, loser.lkValue);
            winner.lkValue = winnerLK;
        }

        Collections.sort(playerList);
    }
}
