package com.yonathan.chess.core;

import com.yonathan.chess.core.board.Bitboard;
import com.yonathan.chess.core.search.AI1;
import com.yonathan.chess.core.search.Search;
import org.junit.Test;

/**
 * Created by Yonathan on 05/04/2015.
 */
public class BT2450EloTest extends EPDTest {

    Bitboard b;
    Search search;
    @Test
    public void testBT2450AI1(){
        b= new Bitboard();
        search = new AI1(b);
        long time = processEPDFile(this.getClass().getResourceAsStream("/BT2450.epd"), 2 * 60 * 1000);
        double timeSeconds = time/1000;
        double elo = 2450- timeSeconds /30;
        System.out.println("AI 1 BT 2450 Elo = " + elo);
    }
}
