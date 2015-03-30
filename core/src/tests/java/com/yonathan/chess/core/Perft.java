package com.yonathan.chess.core;

import com.yonathan.chess.core.board.Bitboard;

import org.junit.Test;

/**
 * Created by Yonathan on 21/12/2014.
 * Tests that the move generator generates the correct number of moves for a given position and how quickly.
 * Test strings and expected results provided by Sharper @ http://www.albert.nu/programs/sharper/perft
 */
public class Perft {
    Bitboard b;

    /*
    * This test starts from the initial position. The theoretical results should be:
    *
    * 1 : 20
    * 2 : 400
    * 3 : 8902
    * 4 : 197281
    * 5 : 4865609
    * 6 : 119060324
    */
    private final static String test1 = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    /*
    * 1 : 48
    * 2 : 2039
    * 3 : 97862
    * 4 : 4085603
    * 5 : 193690690
    * 6 : 8031647685
    */
    private final static String test2 = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";

    @Test
    public void perft() {
        b = new Bitboard();
        boolean loaded = b.initializeFromFEN(test2);
        System.out.println(b);
        long i;
        if (loaded) {
            //System.out.println("true");
            long start = System.currentTimeMillis();
            i = perft(b, 0, 5);
            long stop = System.currentTimeMillis();
            System.out.println("Found " + i + " nodes in " + (stop - start) + " ms.");
        } else {
            System.out.print("false");
        }

    }

    private long perft(Bitboard b, int ply, int depth) {
        if (depth == 0) return 1;

        int[] moves = new int[b.MAX_MOVES];
        int num_moves = b.getAllMoves(moves);
        long count = 0;

        for (int i = 0; i < num_moves; i++) {

            if (b.makeMove(moves[i])) {
                count += perft(b, ply + 1, depth - 1);
                b.unmakeMove();
            }

        }
        return count;

    }
}
