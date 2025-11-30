package foo;

public class CalcLK {

    public static double calculate(double winnerLK, double looserLK) {
        //Use values rounded to 1 decimal
        double winnerLK1d = (double) (Math.floor(winnerLK*10.0)/10.0);
        double looserLK1d = (double) (Math.floor(looserLK*10.0)/10.0);
        //System.out.println("Winner " + winnerLK1d);
        //System.out.println("Looser " + looserLK1d);
        //System.out.println("Differenz " + (winnerLK1d-looserLK1d));
        double points = calcPoints(winnerLK1d-looserLK1d);

        //Doppelte Punkte im Verein für mehr Dynamik. Damit kann sich ein guter Spieler innerhalb einer Saison von ganz hinten nach vorne spielen
        points*=2;
        //System.out.println("Punkte " + points);
        double barrier = calcBarrier(winnerLK1d);
        //System.out.println("Hürde " + barrier);
        double improvement = points/barrier;
        //System.out.println("Verbesserung " + improvement);

        double newWinnerLK = Math.min(winnerLK - improvement, 25.0);
        return newWinnerLK;
        //Altersklassenfaktor und Zählweisenfaktor 100%
    }

    static double calcPoints(double diff){
        double P = 0.0;
        if (diff <= -4) P = 10.0;
        else if (diff > -4 && diff <= -2) P = 1.25 * Math.pow(diff,3.0) + 15 * Math.pow(diff,2.0) + 60 * diff +90;
        else if (diff > -2 && diff <= 4) P = 15 * diff + 50;
        else if (diff > 4 && diff <= 6) P = -3.75 * Math.pow(diff,2.0) + 45 * diff -10;
        else if (diff > 6) P = 125.0;
        return P;
    }

    static double calcBarrier(double winnerLK1d){
        double barrier;
        if (winnerLK1d > 10) barrier = 10 * (30 - winnerLK1d);
        else barrier = 10 * (30 - winnerLK1d) + 6435.0/289.0*(20*(5-winnerLK1d)/(winnerLK1d*winnerLK1d)+1);
        return barrier;
    }

}
