package tests;

import board.Board;
import org.junit.Test;
import search.PVS;
import search.Search;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Created by Yonathan on 10/02/2015.
 * Based on Alberto Ruibal's Carballo. Sources @ Source @ https://github.com/albertoruibal/carballo/
 */
public class EloTest {

    Board b;
    Search search;

    int solved;
    int fails;
    int total;
    int totalTime;
    int lctPoints;


    @Test
    public void testBT2450(){
        b= new Board();
        search = PVS.getInstance();
        long time = processEPDFile(this.getClass().getResourceAsStream("/bt2450.epd"), 15 * 60 * 1000);
        double timeSeconds = time/1000;
        double elo = 2450- timeSeconds /30;
        System.out.println("BT 2450 Elo = " + elo);
    }

    private long processEPDFile(InputStream is, int timeLimit) {
        solved = 0;
        total = 0;
        totalTime = 0;
        lctPoints = 0;
        StringBuilder notSolved = new StringBuilder();
        //goes through all positions
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                //logger.debug("Test = " + line);

                int i0 = line.indexOf(" am ");
                int i1 = line.indexOf(" bm ");
                if (i0 < 0 || i1 < i0) {
                    i0 = i1;
                }

                int i2 = line.indexOf(";", i1 + 4);
                int timeSolved = testPosition(line.substring(0, i0), line.substring(i1 + 4, i2), timeLimit);
                totalTime += timeSolved;

                /*
                *   * 30 points, if solution is found between 0 and 9 seconds
                *   * 25 points, if solution is found between 10 and 29 seconds
                *   * 20 points, if solution is found between 30 and 89 seconds
                *   * 15 points, if solution is found between 90 and 209 seconds
                *   * 10 points, if solution is found between 210 and 389 seconds
                *   * 5 points, if solution is found between 390 and 600 seconds
                *   * 0 points, if not found with in 10 minutes
                */
                if (timeSolved < timeLimit) {
                    if (0 <= timeSolved && timeSolved < 10000) {
                        lctPoints += 30;
                    } else if (10000 <= timeSolved && timeSolved < 30000) {
                        lctPoints += 25;
                    } else if (30000 <= timeSolved && timeSolved < 90000) {
                        lctPoints += 20;
                    } else if (90000 <= timeSolved && timeSolved < 210000) {
                        lctPoints += 15;
                    } else if (210000 <= timeSolved && timeSolved < 390000) {
                        lctPoints += 10;
                    } else if (390000 <= timeSolved && timeSolved < 600000) {
                        lctPoints += 5;
                    }
                } else {
                    notSolved.append(line);
                    notSolved.append("\n");
                }
            }
        }catch (IOException e){

        }
        return 0;
    }


    private int testPosition(String fen, String moveString, int timeLimit) {
        return 0;
    }


}
