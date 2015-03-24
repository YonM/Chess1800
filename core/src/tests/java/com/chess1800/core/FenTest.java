package com.chess1800.core;

import com.chess1800.core.board.Bitboard;
import com.chess1800.core.board.Chessboard;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Yonathan on 03/03/2015.
 */
public class FenTest {
    @Test
    public void test_InitializeFromFEN_With_Real_FEN(){
        Chessboard board = new Bitboard();
        String fen = "1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - -";


        Assert.assertTrue(board.initializeFromFEN(fen));
    }

    @Test
    public void test_InitializeFromFEN_With_False_FEN(){
        Chessboard board = new Bitboard();
        String fen = "1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b ";


        Assert.assertFalse(board.initializeFromFEN(fen));
    }
}
