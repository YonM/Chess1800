package perft;

import board.Board;
import definitions.Definitions;
import move.Move;
import movegen.MoveGenerator;

/**
 * Created by Yonathan on 21/12/2014.
 * Tests that the move generator generates the correct number of moves for a given position and how quickly.
 */
public class Perft implements Definitions {

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
    private static String test1 = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    /*
    * 1 : 48
    * 2 : 2039
    * 3 : 97862
    * 4 : 4085603
    * 5 : 193690690
    */
    private static String test2 = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";

    public static void main(String[] args) {
        Board b = Board.getInstance();
        boolean loaded = b.initializeFromFEN(test1);
        int i;
        if (loaded) {
            //System.out.println("true");
            long start = System.currentTimeMillis();
            i = perft(b, 0, 2);
            long stop = System.currentTimeMillis();
            System.out.println("Found " + i + " nodes in " + (stop - start) + " ms.");
        } else {
            System.out.print("false");
        }

    }

    private static int perft(Board b, int ply, int depth) {
        if (depth == 0) return 1;


        //Move[] moves = new Move [MAX_MOVES];
        b.moveBufLen[ply + 1] = MoveGenerator.moveGen(b, b.moveBufLen[ply]);
        int count = 0;

        for (int i = b.moveBufLen[ply]; i < b.moveBufLen[ply + 1]; i++) {
            b.makeMove(b.moves[i]);
            if (!b.isOtherKingAttacked()) {
                if (Move.getPiece(b.moves[i].moveInt) <= 7)
                    System.out.println("White piece moved: " + Move.getPiece(b.moves[i].moveInt));
                else
                    System.out.println("Black piece moved: " + Move.getPiece(b.moves[i].moveInt));
                count += perft(b, ply + 1, depth - 1);
            }
            b.unmakeMove(b.moves[i]);

        }
        return count;

    }
}
