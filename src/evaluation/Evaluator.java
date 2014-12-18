package evaluation;

import board.Board;

/**
 * Created by Yonathan on 18/12/2014.
 * Evaluation class based on http://web.archive.org/web/20120112113825/http://www.sluijten.com/winglet/
 * Takes into account:
 * Material value. If the material is unbalanced, then the winning side gets a bonus for exchanging pieces.
 * Bonus for Bishop pair.
 * Pawn position and structure (giving a bonus for passed pawns and penalizing doubled, isolated or backward pawns)
 * Piece Square Tables including bonus for Rooking being on open file or behind a passed pawn.
 * King protection by own pawns in the opening and midgame and away from enemy pieces.
 * King positional bonus/penalty different in end game, must be central and near his own pawns.
 */
public class Evaluator {

    public final static int PAWN_VALUE = 100;
    public final static int KNIGHT_VALUE = 325;
    public final static int BISHOP_VALUE = 325;
    public final static int ROOK_VALUE = 500;
    public final static int QUEEN_VALUE = 975;
    public final static int KING_VALUE = 999999;
    public final static int CHECKMATE = KING_VALUE;

    public int eval(Board b) {
        return -1;
    }
}
