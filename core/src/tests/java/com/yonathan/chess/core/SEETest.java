package com.yonathan.chess.core;

import com.yonathan.chess.core.board.Bitboard;
import com.yonathan.chess.core.move.Move;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Yonathan on 05/04/2015.
 */
public class SEETest {
    Bitboard b;

    private void testSEE(String fen, String moveString, int expectedSEE) {
        b=new Bitboard();
        b.initializeFromFEN(fen);
        int move = b.getMoveFromString(moveString,true);
        int calculatedSEE = b.sEE(Move.getFromIndex(move), Move.getToIndex(move), Move.getPieceMoved(move), b.getPieceCaptured(move));
        assertEquals("Bad SEE", expectedSEE, calculatedSEE);
    }

    @Test
    public void seeTest1() {
       testSEE("1k1r4/1pp4p/p7/4p3/8/P5P1/1PP4P/2K1R3 w - -", "Rxe5", 100);
    }

    @Test
    public void seeTest2(){
        testSEE("1k1r3q/1ppn3p/p4b2/4p3/8/P2N2P1/1PP1R1BP/2K1Q3 w - -", "Nxe5",-225);
    }
}
