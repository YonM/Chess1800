package com.chess1800.core;

import com.chess1800.chess.board.Bitboard;
import com.chess1800.chess.move.Move;
import org.junit.Test;

/**
 * Created by Yonathan on 21/12/2014.
 * Tests that the move generator generates the correct number of moves for a given position and how quickly.
 * Test strings and expected results provided by Sharper @ http://www.albert.nu/programs/sharper/perft
 */
public class Perft {
    Bitboard b;
    private final int DEPTH =5;
    long moveCount[];
    long captures[];
    long passantCaptures[];
    long castles[];
    long promotions[];
    long checks[];
    long checkMates[];
    long pawnMoves[];
    long knightMoves[];
    long bishopMoves[];
    long queenMoves[];
    long rookMoves[];
    long kingMoves[];
    long pawnCaptures[];
    long knightCaptures[];
    long bishopCaptures[];
    long queenCaptures[];
    long rookCaptures[];
    long kingCaptures[];

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
        long nodes;
        if (loaded) {
            //System.out.println("true");

            moveCount = new long[DEPTH];
            captures = new long[DEPTH];
            passantCaptures = new long[DEPTH];
            castles = new long[DEPTH];
            promotions = new long[DEPTH];
            checks = new long[DEPTH];
            checkMates = new long[DEPTH];
            pawnMoves= new long [DEPTH];
            knightMoves= new long [DEPTH];
            bishopMoves= new long [DEPTH];
            queenMoves= new long [DEPTH];
            rookMoves= new long [DEPTH];
            kingMoves= new long [DEPTH];
            pawnCaptures= new long [DEPTH];
            knightCaptures= new long [DEPTH];
            bishopCaptures= new long [DEPTH];
            queenCaptures= new long [DEPTH];
            rookCaptures= new long [DEPTH];
            kingCaptures= new long [DEPTH];
            long start = System.currentTimeMillis();
            nodes = perft(b, 0, DEPTH -1);
            long stop = System.currentTimeMillis();
            System.out.println("Found " + nodes + " nodes in " + (stop - start) + " ms.");
            for (int i = 1; i < DEPTH; i++) {
                System.out.println("Moves: " + moveCount[i] + " Pawn Moves: "+ pawnMoves[i] +
                        " Knight Moves: "+ knightMoves[i] + " Bishop Moves: "+ bishopMoves[i] +
                        " Rook Moves: "+ rookMoves[i] + " Queen Moves: "+ queenMoves[i] +
                        " King Moves: "+ kingMoves[i] + " Pawn Captures: "+ pawnCaptures[i] +
                        " Knight Captures: "+ knightCaptures[i] + " Bishop Captures: "+ bishopCaptures[i] +
                        " Rook Captures: "+ rookCaptures[i] + " Queen Captures: "+ queenCaptures[i] +
                        " King Captures: "+ kingCaptures[i] +
                        " Captures=" + captures[i] + " E.P.=" + passantCaptures[i] + " Castles="
                        + castles[i] + " Promotions=" + promotions[i] + " Checks="
                        + checks[i] + " CheckMates=" + checkMates[i]);
            }
            System.out.println();
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
//                System.out.println(Move.getPieceMoved(moves[i]));
                if(depth>0) {
                    moveCount[depth]++;
                    if (Move.isCapture(moves[i])) captures[depth]++;
                    if (Move.getMoveType(moves[i]) == Move.TYPE_EN_PASSANT) passantCaptures[depth]++;
                    if (Move.getMoveType(moves[i]) == Move.TYPE_KINGSIDE_CASTLING
                            || Move.getMoveType(moves[i]) == Move.TYPE_QUEENSIDE_CASTLING) castles[depth]++;
                    if (Move.isPromotion(moves[i])) promotions[depth]++;
                    if (b.isCheck()) {
                        checks[depth]++;
                    }
                    if (b.isCheckMate()) {
                        checkMates[depth]++;
                    }
                    switch(Move.getPieceMoved(moves[i])){
                        case Move.PAWN:
                            pawnMoves[depth]++;
                            if(Move.isCapture(moves[i]))pawnCaptures[depth]++;
                            break;
                        case Move.KNIGHT:
                            knightMoves[depth]++;
                            if(Move.isCapture(moves[i]))knightCaptures[depth]++;
                            break;
                        case Move.BISHOP:
                            bishopMoves[depth]++;
                            if(Move.isCapture(moves[i]))bishopCaptures[depth]++;
                            break;
                        case Move.ROOK:
                            rookMoves[depth]++;
                            if(Move.isCapture(moves[i]))rookCaptures[depth]++;
                            break;
                        case Move.QUEEN:
                            queenMoves[depth]++;
                            if(Move.isCapture(moves[i]))queenCaptures[depth]++;
                            break;
                        case Move.KING:
                            kingMoves[depth]++;
                            if(Move.isCapture(moves[i]))kingCaptures[depth]++;
                            break;
                    }
                }
                count += perft(b, ply + 1, depth - 1);
                b.unmakeMove();
            }

        }
        return count;

    }
}
