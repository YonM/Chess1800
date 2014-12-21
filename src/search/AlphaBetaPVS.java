package search;

import board.Board;
import evaluation.Evaluator;
import move.Move;
import movegen.MoveGenerator;

/**
 * Created by Yonathan on 21/12/2014.
 * Main search class, uses Alpha-beta algorithm with Principal Variation Search.
 */
public class AlphaBetaPVS {

    private static Move[][] triangularArray;
    private static int[] triangularLength;

    private static Evaluator evaluator;

    static {
        triangularArray = new Move[Board.MAX_GAME_LENGTH][Board.MAX_GAME_LENGTH];
        triangularLength = new int[Board.MAX_GAME_LENGTH];
    }

    public static void setEvaluator(Evaluator eval) {
        evaluator = eval;
    }

    public static int alphaBetaPVS(Board b, int ply, int depth, int alpha, int beta) {
        int i, j, movesFound, val;
        triangularLength[ply] = ply;
        if (depth == 0)
            return evaluator.eval(b);

        movesFound = 0;
        b.moveBufLen[ply + 1] = MoveGenerator.moveGen(b.moveBufLen[ply]);

        for (i = b.moveBufLen[ply]; i < b.moveBufLen[ply + 1]; i++) {
            b.makeMove(b.moves[i]);
            if (!b.isOtherKingAttacked()) {

            }

        }

        return -1;
    }
}
