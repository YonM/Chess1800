package search;

import board.Board;
import evaluation.Evaluator;
import move.Move;
import movegen.MoveGenerator;

/**
 * Created by Yonathan on 21/12/2014.
 * Main search class, uses alpha-beta algorithm with Principal Variation Search.
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

/*    public static Move findBestMove(Board b){
        int i,j,val;

    }*/

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
                if (movesFound != 0) {
                    val = -alphaBetaPVS(b, ply + 1, depth - 1, -alpha - 1, -alpha);
                    if ((val > alpha) && (val < beta))
                        val = -alphaBetaPVS(b, ply + 1, depth - 1, -beta, -alpha); //Better move found, normal alpha-beta.
                } else {
                    val = -alphaBetaPVS(b, ply + 1, depth - 1, -beta, -alpha); // Normal alpha-beta
                }
                b.unmakeMove(b.moves[i]);
                if (val >= beta)
                    return beta;

                if (val > alpha) {
                    alpha = val;
                    movesFound++;
                    triangularArray[ply][ply] = b.moves[i];    //save the move
                    for (j = ply + 1; j < triangularLength[ply + 1]; j++)
                        triangularArray[ply][j] = triangularArray[ply + 1][j];  //appends latest best PV from deeper plies

                    triangularLength[ply] = triangularLength[ply + 1];
                }
            } else {
                b.unmakeMove(b.moves[i]);
            }

        }

        return alpha;
    }
}
