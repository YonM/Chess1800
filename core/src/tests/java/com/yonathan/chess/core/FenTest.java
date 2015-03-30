package com.yonathan.chess.core;

import org.junit.Assert;
import org.junit.Test;

import com.yonathan.chess.core.board.Bitboard;
import com.yonathan.chess.core.board.Chessboard;

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
