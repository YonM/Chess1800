package com.yonathan.chess.core;

import com.yonathan.chess.core.board.Bitboard;

import com.yonathan.chess.core.board.MoveGenerator;
import com.yonathan.chess.core.move.Move;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by Yonathan on 21/12/2014.
 * Tests that the move generator generates the correct number of moves for a given position and how quickly.
 * Test strings and expected results provided by Sharper @ http://www.albert.nu/programs/sharper/perft
 */
public class Perft {
    Bitboard b;

    private final int DEPTH =6;
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

    private final static String test3 = "8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67";

    private final static String test4 = "8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28";

    private final static String test5 = "rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3";

    private final static String test6 = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -";



    @Test
    public void perft() {
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
        b = new Bitboard();
        boolean loaded = b.initializeFromFEN(test2);
        System.out.println(b);
        long nodes;
            //System.out.println("true");
            long start = System.currentTimeMillis();
            nodes = perft(b, 0, DEPTH-1);
            long stop = System.currentTimeMillis();
            System.out.println("Found " + nodes + " nodes in " + (stop - start) + " ms.");
        Assert.assertEquals(193690690, nodes);

    }
    @Test
    public void sortPerft(){
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
        b = new Bitboard();
        b.initializeFromFEN(test2);

        //System.out.println(b);
        long nodes;
            long start = System.currentTimeMillis();
            nodes = recursive(b, 0, DEPTH-1);
            long stop = System.currentTimeMillis();
            print();
        Assert.assertEquals(193690690, nodes);



    }
    private void print() {
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

    }

    private long perft(Bitboard b, int ply, int depth) {
        if (depth == 0) return 1;

        int[] moves = new int[b.MAX_MOVES];
        int num_moves = b.getAllMoves(moves);
        long count = 0;

        for (int i = 0; i < num_moves; i++) {

            if (b.makeMove(moves[i])) {
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

    private long recursive(Bitboard b,int ply, int depth){
        if (depth == 0) return 1;
//        int[] moves = new int[b.MAX_MOVES];
//        int num_moves = b.getAllLegalMoves(moves);
//        ArrayList<Integer> moveList = new ArrayList<Integer>();
//        for (int i = 0; i < num_moves; i++) {
//            moveList.add(moves[i]);
//        }

        Integer move;
        long count = 0;
        int moves[] = new int[MoveGenerator.MAX_MOVES*4];
        int captureCount;
        int nonCaptureCount;
        /*Staged Move Generation.
    *  Order:
    *  1. TT move
    *  2. Good Captures (Ordered by SEE)
    *  3. Equal Captures
    *  4. Non-Captures (Ordered by History Heuristic)
    *  5. Bad Captures
     */
        //int phase = MoveStagedGenerator.PHASE_HASH;
        int ttMove = Move.EMPTY;
        //int movesToGenerate = MoveStagedGenerator.GENERATE_ALL;
        captureCount= b.generateCaptures(moves,0,ttMove);
        nonCaptureCount = b.generateNonCaptures(moves, captureCount, ttMove);

        for(int i = 0; i<(captureCount +nonCaptureCount);i++){
            if(b.makeMove(moves[i])) {
                if (depth > 0) {
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
                    switch (Move.getPieceMoved(moves[i])) {
                        case Move.PAWN:
                            pawnMoves[depth]++;
                            if (Move.isCapture(moves[i])) pawnCaptures[depth]++;
                            break;
                        case Move.KNIGHT:
                            knightMoves[depth]++;
                            if (Move.isCapture(moves[i])) knightCaptures[depth]++;
                            break;
                        case Move.BISHOP:
                            bishopMoves[depth]++;
                            if (Move.isCapture(moves[i])) bishopCaptures[depth]++;
                            break;
                        case Move.ROOK:
                            rookMoves[depth]++;
                            if (Move.isCapture(moves[i])) rookCaptures[depth]++;
                            break;
                        case Move.QUEEN:
                            queenMoves[depth]++;
                            if (Move.isCapture(moves[i])) queenCaptures[depth]++;
                            break;
                        case Move.KING:
                            kingMoves[depth]++;
                            if (Move.isCapture(moves[i])) kingCaptures[depth]++;
                            break;
                        default:
                            System.out.println("no piece moved" + moves[i]);
                            if(Move.isCapture(moves[i])) System.out.println("capture error");
                            else System.out.println("non capture error");
                    }
                }
                count += recursive(b, ply + 1, depth - 1);
                b.unmakeMove();
                //moveList.remove(move);
            }


        }
        return count;
    }
}
