package tests;

import board.Board;
import org.junit.Test;
import search.PVS;
import search.Search;
import java.io.BufferedReader;
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

        return 0;
    }
}
