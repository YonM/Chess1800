package com.chess1800.core;

import board.Bitboard;
import board.Chessboard;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Yonathan on 03/03/2015.
 */
public class FenTest {
    @Test
    public void testInitializeFromFEN(){
        Chessboard board = new Bitboard();
        String fen = "1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - -";


        Assert.assertTrue(board.initializeFromFEN(fen));
    }
}
