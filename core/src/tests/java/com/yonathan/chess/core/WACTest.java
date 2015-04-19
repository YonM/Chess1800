package com.yonathan.chess.core;

import com.yonathan.chess.core.board.Bitboard;
import com.yonathan.chess.core.search.AI1;
import com.yonathan.chess.core.search.Search;
import org.junit.Test;

/**
 * Created by Yonathan on 19/04/2015.
 */
public class WACTest  extends EPDTest{

    Bitboard b;
    Search search;
    @Test
    public void testWACAI1(){
        b= new Bitboard();
        search = new AI1(b);
        processEPDFile(this.getClass().getResourceAsStream("/wac300.epd"), 1000);
        System.out.println("solved: " + solved);
        System.out.println("fails: " + fails);
    }
}
