package evaluation;

import bitboard.BitOperations;
import board.Board;
import board.BoardUtils;

/**
 * Created by Yonathan on 18/12/2014.
 * Evaluation class based on http://web.archive.org/web/20120112113825/http://www.sluijten.com/winglet/
 * Takes into account:
 * -Material value. If the material is unbalanced, then the winning side gets a bonus for exchanging pieces.
 * -Bonus for Bishop pair.
 * -Pawn position and structure (giving a bonus for passed pawns and penalizing doubled, isolated or backward pawns)
 * -Piece Square Tables including bonus for Rooking being on open file or behind a passed pawn.
 * -King protection by own pawns in the opening and midgame and away from enemy pieces.
 * -King positional bonus/penalty different in end game, must be central and near his own pawns.
 *
 * Scores calculated from white perspective and then returns the score from the perspective
 * of the side to move.
 */
public class Evaluator {

    public final static int PAWN_VALUE = 100;
    public final static int KNIGHT_VALUE = 325;
    public final static int BISHOP_VALUE = 325;
    public final static int ROOK_VALUE = 500;
    public final static int QUEEN_VALUE = 975;
    public final static int KING_VALUE = 999999;
    public final static int CHECKMATE = KING_VALUE;

    private int score, square;
    private int whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens;
    private int blackPawns, blackKnights, blackBishops, blackRooks, blackQueens;
    private int whiteKingSquare, blackKingSquare;
    private int whiteTotalMat, blackTotalMat;
    private int whiteTotal, blackTotal;
    private boolean endGame;
    private long temp, whitePassedPawns, blackPassedPawns;

    public int eval(Board board) {
        score = board.material;
        whiteKingSquare = (int) BitOperations.lsb(board.whiteKing);
        blackKingSquare = (int) BitOperations.lsb(board.blackKing);

        whitePawns = BitOperations.popCount(board.whitePawns);
        whiteKnights = BitOperations.popCount(board.whiteKnights);
        whiteBishops = BitOperations.popCount(board.whiteBishops);
        whiteRooks = BitOperations.popCount(board.whiteRooks);
        whiteQueens = BitOperations.popCount(board.whiteQueens);
        whiteTotalMat = KNIGHT_VALUE * whiteKnights + BISHOP_VALUE * whiteBishops + ROOK_VALUE * whiteRooks + QUEEN_VALUE * whiteQueens;
        whiteTotal = whitePawns + whiteKnights + whiteBishops + whiteRooks + whiteQueens;

        blackPawns = BitOperations.popCount(board.blackPawns);
        blackKnights = BitOperations.popCount(board.blackKnights);
        blackBishops = BitOperations.popCount(board.blackBishops);
        blackRooks = BitOperations.popCount(board.blackRooks);
        blackQueens = BitOperations.popCount(board.blackQueens);
        blackTotalMat = KNIGHT_VALUE * blackKnights + BISHOP_VALUE * blackBishops + ROOK_VALUE * blackRooks + QUEEN_VALUE * blackQueens;
        blackTotal = blackPawns + blackKnights + blackBishops + blackRooks + blackQueens;

        //Test for end game if white or black total material less than the value of a Rook+ Queen.
        endGame = (whiteTotalMat < (QUEEN_VALUE + ROOK_VALUE) || blackTotalMat < (QUEEN_VALUE + ROOK_VALUE));

        //Evaluate for draw due to insufficient material

        if (whitePawns == 0 && blackPawns == 0) {

            // king vs king
            if (whiteTotalMat + blackTotalMat == 0) return 0;

            // king and knight vs king
            if (((whiteTotalMat == KNIGHT_VALUE) && (whiteKnights == 1) && (blackTotalMat == 0)) ||
                    ((blackTotalMat == KNIGHT_VALUE)) && (blackKnights == 1) && (whiteTotalMat == 0)) return 0;

            // 2 kings with one or more bishops and all bishops on the same colour
            if (whiteBishops + blackBishops > 0) {
                if (whiteKnights + whiteRooks + whiteQueens + blackKnights + blackRooks + blackQueens == 0) {
                    if ((((board.whiteBishops | board.blackBishops) & BoardUtils.WHITE_SQUARES) == 0) ||
                            (((board.whiteBishops | board.blackBishops) & BoardUtils.BLACK_SQUARES) == 0)) return 0;
                }
            }
        }

        /* Evaluate material. Winning side will prefer to exchange pieces.
        *  Add 3 centipawns to score for exchange with unequal material
        *  Losing a piece (from balanced material) becomes more severe in the endgame.
        */

        if (whiteTotalMat + whitePawns > blackTotalMat + blackPawns) {
            score += 450 + 300 * whiteTotal - 600 * blackTotal;

        } else if (whiteTotalMat + whitePawns < blackTotalMat + blackPawns) {

        }


        return -1;
    }
}
