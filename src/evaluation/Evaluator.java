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
    //Material value
    public final static int PAWN_VALUE = 100;
    public final static int KNIGHT_VALUE = 325;
    public final static int BISHOP_VALUE = 325;
    public final static int ROOK_VALUE = 500;
    public final static int QUEEN_VALUE = 975;
    public final static int KING_VALUE = 999999;
    public final static int CHECKMATE = KING_VALUE;

    //Bonus/Penalty constants
    private final static int PENALTY_DOUBLED_PAWN = 10;
    private final static int PENALTY_ISOLATED_PAWN = 20;
    private final static int PENALTY_BACKWARD_PAWN = 8;
    private final static int BONUS_PASSED_PAWN = 20;
    private final static int BONUS_BISHOP_PAIR = 10;
    private final static int BONUS_ROOK_BEHIND_PASSED_PAWN = 20;
    private final static int BONUS_ROOK_ON_OPEN_FILE = 20;
    private final static int BONUS_TWO_ROOKS_ON_OPEN_FILE = 20;
    private final static int BONUS_PAWN_SHIELD_STRONG = 9;
    private final static int BONUS_PAWN_SHIELD_WEAK = 4;

    //King Distance Safety
    private final static int[] PAWN_OWN_DISTANCE = {0, 8, 4, 2, 0, 0, 0, 0};
    private final static int[] PAWN_OPPONENT_DISTANCE = {0, 2, 1, 0, 0, 0, 0, 0};
    private final static int[] KNIGHT_DISTANCE = {0, 4, 4, 0, 0, 0, 0, 0};
    private final static int[] BISHOP_DISTANCE = {0, 5, 4, 3, 2, 1, 0, 0};
    private final static int[] ROOK_DISTANCE = {0, 7, 5, 4, 3, 0, 0, 0};
    private final static int[] QUEEN_DISTANCE = {0, 10, 8, 5, 4, 0, 0, 0};
    private final static int[][] DISTANCE;

    public final static int[] MIRROR = {
            56, 57, 58, 59, 60, 61, 62, 63,
            48, 49, 50, 51, 52, 53, 54, 55,
            40, 41, 42, 43, 44, 45, 46, 47,
            32, 33, 34, 35, 36, 37, 38, 39,
            24, 25, 26, 27, 28, 29, 30, 31,
            16, 17, 18, 19, 20, 21, 22, 23,
            8, 9, 10, 11, 12, 13, 14, 15,
            0, 1, 2, 3, 4, 5, 6, 7
    };

    //PIECE SQUARE TABLES
    private static final int[] PAWN_POS_W;
    private static final int[] KNIGHT_POS_W;
    private static final int[] BISHOP_POS_W;
    private static final int[] ROOK_POS_W;
    private static final int[] QUEEN_POS_W;
    private static final int[] KING_POS_W;
    private static final int[] KING_POS_ENDGAME_W;

    private static final int[] PAWN_POS_B = {
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 15, 20, 20, 15, 10, 5,
            4, 8, 12, 16, 16, 12, 8, 4,
            3, 6, 9, 12, 12, 9, 6, 3,
            2, 4, 6, 8, 8, 6, 4, 2,
            1, 2, 3, -10, -10, 3, 2, 1,
            0, 0, 0, -40, -40, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    private static final int[] KNIGHT_POS_B = {
            -10, -10, -10, -10, -10, -10, -10, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, -30, -10, -10, -10, -10, -30, -10
    };

    private static final int[] BISHOP_POS_B = {
            -10, -10, -10, -10, -10, -10, -10, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, -10, -20, -10, -10, -20, -10, -10
    };

    private static final int[] ROOK_POS_B = {
            0, 0, 0, 0, 0, 0, 0, 0,
            15, 15, 15, 15, 15, 15, 15, 15,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            -10, 0, 0, 10, 10, 0, 0, -10
    };

    private static final int[] QUEEN_POS_B = {
            -10, -10, -10, -10, -10, -10, -10, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, -10, -20, -10, -10, -20, -10, -10
    };

    private static final int[] KING_POS_B = {
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -20, -20, -20, -20, -20, -20, -20, -20,
            0, 20, 40, -20, 0, -20, 40, 20
    };

    private static final int[] KING_POS_ENDGAME_B = {
            0, 10, 20, 30, 30, 20, 10, 0,
            10, 20, 30, 40, 40, 30, 20, 10,
            20, 30, 40, 50, 50, 40, 30, 20,
            30, 40, 50, 60, 60, 50, 40, 30,
            30, 40, 50, 60, 60, 50, 40, 30,
            20, 30, 40, 50, 50, 40, 30, 20,
            10, 20, 30, 40, 40, 30, 20, 10,
            0, 10, 20, 30, 30, 20, 10, 0
    };

    //Static initializer
    static {
        //White Piece Square Tables
        PAWN_POS_W = new int[64];
        KNIGHT_POS_W = new int[64];
        BISHOP_POS_W = new int[64];
        ROOK_POS_W = new int[64];
        QUEEN_POS_W = new int[64];
        KING_POS_W = new int[64];
        KING_POS_ENDGAME_W = new int[64];
        int i;
        for (i = 0; i < 64; i++) {
            PAWN_POS_W[i] = PAWN_POS_B[MIRROR[i]];
            KNIGHT_POS_W[i] = KNIGHT_POS_B[MIRROR[i]];
            BISHOP_POS_W[i] = BISHOP_POS_B[MIRROR[i]];
            ROOK_POS_W[i] = ROOK_POS_B[MIRROR[i]];
            QUEEN_POS_W[i] = QUEEN_POS_B[MIRROR[i]];
            KING_POS_W[i] = KING_POS_B[MIRROR[i]];
            KING_POS_ENDGAME_W[i] = KING_POS_ENDGAME_B[MIRROR[i]];
        }

        //DISTANCE -distance is measured as max of (rank,file)-difference
        DISTANCE = new int[64][64];
        int square;
        for (i = 0; i < 64; i++) {
            for (square = 0; square < 64; square++) {
                if (Math.abs(BoardUtils.RANKS[i] - BoardUtils.RANKS[square]) >
                        Math.abs(BoardUtils.FILES[i] - BoardUtils.FILES[square]))
                    DISTANCE[i][square] = Math.abs(BoardUtils.RANKS[i] - BoardUtils.RANKS[square]);
                else
                    DISTANCE[i][square] = Math.abs(BoardUtils.FILES[i] - BoardUtils.FILES[square]);
            }
        }

    }

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
        whiteTotalMat = 3 * whiteKnights + 3 * whiteBishops + 5 * whiteRooks + 10 * whiteQueens;
        whiteTotal = whitePawns + whiteKnights + whiteBishops + whiteRooks + whiteQueens;

        blackPawns = BitOperations.popCount(board.blackPawns);
        blackKnights = BitOperations.popCount(board.blackKnights);
        blackBishops = BitOperations.popCount(board.blackBishops);
        blackRooks = BitOperations.popCount(board.blackRooks);
        blackQueens = BitOperations.popCount(board.blackQueens);
        blackTotalMat = 3 * blackKnights + 3 * blackBishops + 5 * blackRooks + 10 * blackQueens;
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
            score += 45 + 3 * whiteTotal - 6 * blackTotal;

        } else if (whiteTotalMat + whitePawns < blackTotalMat + blackPawns) {
            score -= 45 + 3 * blackTotal - 6 * whiteTotal;
        }
        evaluateWhiteMaterial(board);

        return -1;
    }

    private void evaluateWhiteMaterial(Board board) {
        evaluateWhitePawns(board);

    }

    private void evaluateWhitePawns(Board board) {
        whitePassedPawns = 0;
        temp = board.whitePawns;
        while (temp != 0) {
            square = (int) BitOperations.lsb(temp);

            score += PAWN_POS_W[square];
            score += PAWN_OPPONENT_DISTANCE[DISTANCE[square][blackKingSquare]];
            if (endGame) {
                score += PAWN_OWN_DISTANCE[DISTANCE[square][whiteKingSquare]];
            }
        }
    }
}