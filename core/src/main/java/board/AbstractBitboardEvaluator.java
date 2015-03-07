package board;


/**
 * Created by Yonathan on 18/12/2014.
 * Evaluation class based on Stef Luijten's Winglet source @ http://wearchive.org/web/20120112113825/http://www.sluijten.com/winglet/
 * Takes into account:
 * -Material value. If the material is unbalanced, then the winning side gets a bonus for exchanging pieces.
 * -Bonus for Bishop pair.
 * -Pawn position and structure (giving a bonus for passed pawns and penalizing doubled, isolated or backward pawns)
 * -Piece Square Tables including bonus for Rooking being on open file or behind a passed pawn.
 * -King protection by own pawns in the opening and mid-game and away from enemy pieces.
 * -King positional bonus/penalty different in end game, must be central and near his own pawns.
 *
 * Scores calculated from white perspective and then returns the score from the perspective
 * of the side to move.
 */
public abstract class AbstractBitboardEvaluator extends AbstractAbstractBitboardMoveGenerator implements Evaluator {

    //Bonus/Penalty constants
    private final int PENALTY_DOUBLED_PAWN = 10;
    private final int PENALTY_ISOLATED_PAWN = 20;
    private final int PENALTY_BACKWARD_PAWN = 8;
    private final int BONUS_PASSED_PAWN = 20;
    private final int BONUS_BISHOP_PAIR = 50;
    private final int BONUS_ROOK_BEHIND_PASSED_PAWN = 20;
    private final int BONUS_ROOK_ON_OPEN_FILE = 20;
    private final int BONUS_TWO_ROOKS_ON_OPEN_FILE = 20;
    private final int BONUS_PAWN_SHIELD_STRONG = 9;
    private final int BONUS_PAWN_SHIELD_WEAK = 4;




    //King Distance Safety
    private final int[] PAWN_OWN_DISTANCE = {0, 8, 4, 2, 0, 0, 0, 0};
    private final int[] PAWN_OPPONENT_DISTANCE = {0, 2, 1, 0, 0, 0, 0, 0};
    private final int[] KNIGHT_DISTANCE = {0, 4, 4, 0, 0, 0, 0, 0};
    private final int[] BISHOP_DISTANCE = {0, 5, 4, 3, 2, 1, 0, 0};
    private final int[] ROOK_DISTANCE = {0, 7, 5, 4, 3, 0, 0, 0};
    private final int[] QUEEN_DISTANCE = {0, 10, 8, 5, 4, 0, 0, 0};
    private final int[][] DISTANCE;

    public final int[] MIRROR = {
            56, 57, 58, 59, 60, 61, 62, 63,
            48, 49, 50, 51, 52, 53, 54, 55,
            40, 41, 42, 43, 44, 45, 46, 47,
            32, 33, 34, 35, 36, 37, 38, 39,
            24, 25, 26, 27, 28, 29, 30, 31,
            16, 17, 18, 19, 20, 21, 22, 23,
            8, 9, 10, 11, 12, 13, 14, 15,
            0, 1, 2, 3, 4, 5, 6, 7
    };

    //PIECE SQUARE TABLES provided by Stef Luijten
    private final int[] PAWN_POS_W;
    private final int[] KNIGHT_POS_W;
    private final int[] BISHOP_POS_W;
    private final int[] ROOK_POS_W;
    private final int[] QUEEN_POS_W;
    private final int[] KING_POS_W;
    private final int[] KING_POS_ENDGAME_W;

    private final int[] PAWN_POS_B = {
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 15, 20, 20, 15, 10, 5,
            4, 8, 12, 16, 16, 12, 8, 4,
            3, 6, 9, 12, 12, 9, 6, 3,
            2, 4, 6, 8, 8, 6, 4, 2,
            1, 2, 3, -10, -10, 3, 2, 1,
            0, 0, 0, -40, -40, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    private final int[] KNIGHT_POS_B = {
            -10, -10, -10, -10, -10, -10, -10, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, -30, -10, -10, -10, -10, -30, -10
    };

    private final int[] BISHOP_POS_B = {
            -10, -10, -10, -10, -10, -10, -10, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, -10, -20, -10, -10, -20, -10, -10
    };

    private final int[] ROOK_POS_B = {
            0, 0, 0, 0, 0, 0, 0, 0,
            15, 15, 15, 15, 15, 15, 15, 15,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            -10, 0, 0, 10, 10, 0, 0, -10
    };

    private final int[] QUEEN_POS_B = {
            -10, -10, -10, -10, -10, -10, -10, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, -10, -20, -10, -10, -20, -10, -10
    };

    private final int[] KING_POS_B = {
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -20, -20, -20, -20, -20, -20, -20, -20,
            0, 20, 40, -20, 0, -20, 40, 20
    };

    private final int[] KING_POS_ENDGAME_B = {
            0, 10, 20, 30, 30, 20, 10, 0,
            10, 20, 30, 40, 40, 30, 20, 10,
            20, 30, 40, 50, 50, 40, 30, 20,
            30, 40, 50, 60, 60, 50, 40, 30,
            30, 40, 50, 60, 60, 50, 40, 30,
            20, 30, 40, 50, 50, 40, 30, 20,
            10, 20, 30, 40, 40, 30, 20, 10,
            0, 10, 20, 30, 30, 20, 10, 0
    };

    //Pawn structure tables
    private final long[] PASSED_WHITE;
    private final long[] ISOLATED_WHITE;
    private final long[] BACKWARD_WHITE;
    private final long[] STRONG_SAFE_WHITE;
    private final long[] WEAK_SAFE_WHITE;

    private final long[] PASSED_BLACK;
    private final long[] ISOLATED_BLACK;
    private final long[] BACKWARD_BLACK;
    private final long[] STRONG_SAFE_BLACK;
    private final long[] WEAK_SAFE_BLACK;
    


    public AbstractBitboardEvaluator(){
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
                if (Math.abs(i / 8 - square / 8) >
                        Math.abs(i % 8 - square % 8))
                    DISTANCE[i][square] = Math.abs(i / 8 - square / 8);
                else
                    DISTANCE[i][square] = Math.abs(i % 8 - square % 8);
            }
        }

        //Pawn structures
        PASSED_WHITE = new long[64];
        ISOLATED_WHITE = new long[64];
        BACKWARD_WHITE = new long[64];
        STRONG_SAFE_WHITE = new long[64];
        WEAK_SAFE_WHITE = new long[64];

        int rank, file;
        for (i = 0; i < 64; i++) {
            //Passed white pawns
            for (rank = i / 8 + 1; rank < 7; rank++) {
                file = i % 8;
                if (file > 0)
                    PASSED_WHITE[i] ^= getSquare[rank * 8 + (file - 1)];
                PASSED_WHITE[i] ^= getSquare[rank * 8 + file];
                if (file < 7)
                    PASSED_WHITE[i] ^= getSquare[rank * 8 + (file + 1)];
            }
            //Isolated white pawns
            for (rank = 1; rank < 7; rank++) {
                file = i % 8;
                if (file > 0) ISOLATED_WHITE[i] ^= getSquare[rank * 8 + (file - 1)];
                if (file < 7) ISOLATED_WHITE[i] ^= getSquare[rank * 8 + (file + 1)];
            }

            //Backward white pawns
            for (rank = 1; rank < 7; rank++) {
                file = i % 8;
                if (file > 0) BACKWARD_WHITE[i] ^= getSquare[rank * 8 + (file - 1)];
                if (file > 7) BACKWARD_WHITE[i] ^= getSquare[rank * 8 + (file + 1)];
            }

        }

        //Strong/Weak squares for white pawns, used for king safety. Only if the king is on the first 3 ranks.
        for (i = 0; i < 24; i++) {
            STRONG_SAFE_WHITE[i] ^= getSquare[i + 8];
            file = i % 8;
            if (file > 0) {
                STRONG_SAFE_WHITE[i] ^= getSquare[i + 7];
            } else {
                STRONG_SAFE_WHITE[i] ^= getSquare[i + 10];
            }
            if (file < 7) {
                STRONG_SAFE_WHITE[i] ^= getSquare[i + 9];
            } else {
                STRONG_SAFE_WHITE[i] ^= getSquare[i + 6];
            }
            WEAK_SAFE_WHITE[i] = STRONG_SAFE_WHITE[i] << 8;
        }
        //Mirrored for black pawns
        PASSED_BLACK = new long[64];
        ISOLATED_BLACK = new long[64];
        BACKWARD_BLACK = new long[64];
        STRONG_SAFE_BLACK = new long[64];
        WEAK_SAFE_BLACK = new long[64];
        for (i = 0; i < 64; i++) {
            for (square = 0; square < 64; square++) {
                if ((PASSED_WHITE[i] & getSquare[square]) != 0)
                    PASSED_BLACK[MIRROR[i]] |= getSquare[MIRROR[square]];

                if ((ISOLATED_WHITE[i] & getSquare[square]) != 0)
                    ISOLATED_BLACK[MIRROR[i]] |= getSquare[MIRROR[square]];

                if ((BACKWARD_WHITE[i] & getSquare[square]) != 0)
                    BACKWARD_BLACK[MIRROR[i]] |= getSquare[MIRROR[square]];

                if ((STRONG_SAFE_WHITE[i] & getSquare[square]) != 0)
                    STRONG_SAFE_BLACK[MIRROR[i]] |= getSquare[MIRROR[square]];

                if ((WEAK_SAFE_WHITE[i] & getSquare[square]) != 0)
                    WEAK_SAFE_BLACK[MIRROR[i]] |= getSquare[MIRROR[square]];

            }
        }

    }

    private int score, square;
    private int whitePawnCount, whiteKnightCount, whiteBishopCount, whiteRookCount, whiteQueenCount;
    private int blackPawnCount, blackKnightCount, blackBishopCount, blackRookCount, blackQueenCount;
    private int whiteKingSquare, blackKingSquare;
    private int whiteTotal, blackTotal;
    private boolean endGame;
    private long temp, whitePassedPawns, blackPassedPawns;


    public static int getIndexFromBoard(long board) {
        return (Long.numberOfTrailingZeros(Long.lowestOneBit(board)));
    }

    public static int getLastIndexFromBoard(long board) {
        return (Long.numberOfTrailingZeros(Long.highestOneBit(board)));
    }

    public int eval() {
        if(isCheckMate()) return -CHECKMATE;
        if(isDraw()!= NO_DRAW)return DRAWSCORE;
        score = 0;
        whiteKingSquare = Long.numberOfTrailingZeros(whiteKing);
        blackKingSquare = Long.numberOfTrailingZeros(blackKing);

        whitePawnCount = Long.bitCount(whitePawns);
        whiteKnightCount = Long.bitCount(whiteKnights);
        whiteBishopCount = Long.bitCount(whiteBishops);
        whiteRookCount = Long.bitCount(whiteRooks);
        whiteQueenCount = Long.bitCount(whiteQueens);
        whiteTotal = whitePawnCount + whiteKnightCount + whiteBishopCount + whiteRookCount + whiteQueenCount;

        blackPawnCount = Long.bitCount(blackPawns);
        blackKnightCount = Long.bitCount(blackKnights);
        blackBishopCount = Long.bitCount(blackBishops);
        blackRookCount = Long.bitCount(blackRooks);
        blackQueenCount = Long.bitCount(blackQueens);
        blackTotal = blackPawnCount + blackKnightCount + blackBishopCount + blackRookCount + blackQueenCount;

        //Test for end game if white or black total material less than the value of a Rook+ Queen.
        endGame = (whitePieceMaterial() < (QUEEN_VALUE + ROOK_VALUE) || blackPieceMaterial() < (QUEEN_VALUE + ROOK_VALUE));


        /* Evaluate material. Winning side will prefer to exchange pieces.
        *  Add 3 centipawns to score for exchange with unequal material
        *  Losing a piece (from balanced material) becomes more severe in the endgame.
        */

        if (whitePieceMaterial() + PAWN_VALUE * whitePawnCount > blackPieceMaterial() + PAWN_VALUE * blackPawnCount) {
            score += 45 + 3 * whiteTotal - 6 * blackTotal;
        } else if (whitePieceMaterial() + PAWN_VALUE * whitePawnCount < blackPieceMaterial() + PAWN_VALUE * blackPawnCount) {
            score -= 45 + 3 * blackTotal - 6 * whiteTotal;
        }
        evaluateWhiteMaterial();
        evaluateBlackMaterial();
        if (whiteToMove) return score;
        return -score;
    }

    public abstract int whitePieceMaterial();
    
    public abstract int blackPieceMaterial();

    public abstract boolean isCheckMate();


    public abstract boolean isCheck();




    public abstract int isDraw();

    protected void evaluateWhiteMaterial() {
        evaluateWhitePawns();
        evaluateWhiteKnights();
        evaluateWhiteBishops();
        evaluateWhiteRooks();
        evaluateWhiteQueens();
        evaluateWhiteKing();
    }


    protected void evaluateBlackMaterial() {
        evaluateBlackPawns();
        evaluateBlackKnights();
        evaluateBlackBishops();
        evaluateBlackRooks();
        evaluateBlackQueens();
        evaluateBlackKing();
    }

    private void evaluateWhitePawns() {
        whitePassedPawns = 0;
        temp = whitePawnCount;
        while (temp != 0) {
            square = getIndexFromBoard(temp);
            score += PAWN_VALUE;
            score += PAWN_POS_W[square];
            score += PAWN_OPPONENT_DISTANCE[DISTANCE[square][blackKingSquare]];
            if (endGame)
                score += PAWN_OWN_DISTANCE[DISTANCE[square][whiteKingSquare]];

            //Passed pawn bonus
            if ((PASSED_WHITE[square] & blackPawnCount) == 0) {
                score += BONUS_PASSED_PAWN;
                whitePassedPawns ^= getSquare[square];
            }

            //Doubled pawn penalty
            if (((whitePawnCount ^ getSquare[square]) & COLUMN[square % 8]) != 0)
                score -= PENALTY_DOUBLED_PAWN;

            //Isolated pawn penalty
            if ((ISOLATED_WHITE[square] & whitePawnCount) == 0) {
                score -= PENALTY_ISOLATED_PAWN;
            } else {
                /*  Not isolated but maybe backwards if:
                 *  1. the next square is controlled by an enemy pawn - PAWN_ATTACKS board used to check. AND
                 *  2. No pawns left that can defend the pawn.
                */

                if ((whitePawn[square + 8] & blackPawnCount) != 0)
                    if ((BACKWARD_WHITE[square] & whitePawnCount) == 0)
                        score -= PENALTY_BACKWARD_PAWN;
            }
            temp ^= getSquare[square];
        }
    }

    private void evaluateWhiteKnights() {
        temp = whiteKnightCount;
        while (temp != 0) {
            square = getIndexFromBoard(temp);
            score += KNIGHT_VALUE;
            score += KNIGHT_POS_W[square];
            score += KNIGHT_DISTANCE[DISTANCE[square][blackKingSquare]];
            temp ^= getSquare[square];
        }
    }

    private void evaluateWhiteBishops() {
        if(Long.bitCount(whiteBishopCount)>1)
                score += BONUS_BISHOP_PAIR;
        temp = whiteBishopCount;
        while (temp != 0) {
            square = getIndexFromBoard(temp);
            score += BISHOP_VALUE;
            score += BISHOP_POS_W[square];
            score += BISHOP_DISTANCE[DISTANCE[square][blackKingSquare]];
            temp ^= getSquare[square];
        }
    }


    private void evaluateWhiteRooks() {
        temp = whiteRookCount;
        while (temp != 0) {
            square = getIndexFromBoard(temp);
            score += ROOK_VALUE;
            score += ROOK_POS_W[square];
            score += ROOK_DISTANCE[DISTANCE[square][blackKingSquare]];
            if ((COLUMN[square % 8] & whitePassedPawns) != 0)
                if (square < getLastIndexFromBoard((COLUMN[square % 8] & whitePassedPawns)))
                    score += BONUS_ROOK_BEHIND_PASSED_PAWN;

            if ((COLUMN[square % 8] & blackPawnCount) == 0) {
                score += BONUS_ROOK_ON_OPEN_FILE;
                if ((COLUMN[square % 8] & (whiteRookCount & ~Long.lowestOneBit(temp))) != 0)
                    score+=BONUS_TWO_ROOKS_ON_OPEN_FILE;
            }
            temp ^= getSquare[square];
        }
    }


    private void evaluateWhiteQueens() {
        temp = whiteQueenCount;
        while (temp != 0) {
            square = getIndexFromBoard(temp);
            score += QUEEN_VALUE;
            score += QUEEN_POS_W[square];
            score += QUEEN_DISTANCE[DISTANCE[square][blackKingSquare]];
            temp ^= getSquare[square];
        }
    }


    private void evaluateWhiteKing() {
        if (endGame) {
            score += KING_POS_ENDGAME_W[whiteKingSquare];
        } else {
            score += KING_POS_W[whiteKingSquare];
            //Not end-game so add pawn shield bonus
            //Strong pawn shield bonus if pawns are close to the king
            score += BONUS_PAWN_SHIELD_STRONG * Long.bitCount((STRONG_SAFE_WHITE[whiteKingSquare] & whitePawnCount));

            //Weak pawn shield bonus if pawns are not very close to the king
            score += BONUS_PAWN_SHIELD_WEAK * Long.bitCount((WEAK_SAFE_WHITE[whiteKingSquare] & whitePawnCount));
        }
    }

    private void evaluateBlackPawns() {
        blackPassedPawns = 0;
        temp = blackPawnCount;
        while (temp != 0) {
            square = getIndexFromBoard(temp);
            score -= PAWN_VALUE;
            score -= PAWN_POS_B[square];
            score -= PAWN_OPPONENT_DISTANCE[DISTANCE[square][whiteKingSquare]];
            if (endGame)
                score -= PAWN_OWN_DISTANCE[DISTANCE[square][blackKingSquare]];

            //Passed pawn bonus
            if ((PASSED_BLACK[square] & blackPawnCount) == 0) {
                score -= BONUS_PASSED_PAWN;
                blackPassedPawns ^= getSquare[square];
            }

            //Doubled pawn penalty
            if (((blackPawnCount ^ getSquare[square]) & COLUMN[square % 8]) != 0)
                score += PENALTY_DOUBLED_PAWN;

            //Isolated pawn penalty
            if ((ISOLATED_BLACK[square] & blackPawnCount) == 0) {
                score += PENALTY_ISOLATED_PAWN;
            } else {
                /*  Not isolated but maybe backwards if:
                 *  1. the next square is controlled by an enemy pawn - PAWN_ATTACKS board used to check. AND
                 *  2. No pawns left that can defend the pawn.
                */

                if ((blackPawn[square + 8] & blackPawnCount) != 0)
                    if ((BACKWARD_BLACK[square] & blackPawnCount) == 0)
                        score += PENALTY_BACKWARD_PAWN;
            }
            temp ^= getSquare[square];
        }
    }

    private void evaluateBlackKnights() {
        temp = blackKnightCount;
        while (temp != 0) {
            square = getIndexFromBoard(temp);
            score -= KNIGHT_VALUE;
            score -= KNIGHT_POS_B[square];
            score -= KNIGHT_DISTANCE[DISTANCE[square][whiteKingSquare]];
            temp ^= getSquare[square];
        }
    }

    private void evaluateBlackBishops() {
        if(Long.bitCount(blackBishopCount)>1)
                score -= BONUS_BISHOP_PAIR;
        temp = blackBishopCount;
        while (temp != 0) {
            square = getIndexFromBoard(temp);
            score -= BISHOP_VALUE;
            score -= BISHOP_POS_B[square];
            score -= BISHOP_DISTANCE[DISTANCE[square][whiteKingSquare]];
            temp ^= getSquare[square];
        }
    }

    private void evaluateBlackRooks() {
        temp = blackRookCount;
        while (temp != 0) {
            square = getIndexFromBoard(temp);
            score -= ROOK_VALUE;
            score -= ROOK_POS_B[square];
            score -= ROOK_DISTANCE[DISTANCE[square][whiteKingSquare]];
            if ((COLUMN[square % 8] & blackPassedPawns) != 0)
                if (square < getLastIndexFromBoard((COLUMN[square % 8] & blackPassedPawns)))
                    score -= BONUS_ROOK_BEHIND_PASSED_PAWN;
            if ((COLUMN[square % 8] & whitePawnCount) == 0) {
                score -= BONUS_ROOK_ON_OPEN_FILE;
                if ((COLUMN[square % 8] & (blackRookCount & ~Long.lowestOneBit(temp))) != 0)
                    score -= BONUS_TWO_ROOKS_ON_OPEN_FILE;
            }
            temp ^= getSquare[square];
        }
    }

    private void evaluateBlackQueens() {
        temp = blackQueenCount;
        while (temp != 0) {
            square = getIndexFromBoard(temp);
            score -= QUEEN_VALUE;
            score -= QUEEN_POS_B[square];
            score -= QUEEN_DISTANCE[DISTANCE[square][whiteKingSquare]];
            temp ^= getSquare[square];
        }
    }

    private void evaluateBlackKing() {
        if (endGame) {
            score -= KING_POS_ENDGAME_B[blackKingSquare];
        } else {
            score -= KING_POS_B[blackKingSquare];
            //Not end-game so add pawn shield bonus
            //Strong pawn shield bonus if pawns are close to the king
            score -= BONUS_PAWN_SHIELD_STRONG * Long.bitCount((STRONG_SAFE_BLACK[blackKingSquare] & blackPawnCount));

            //Weak pawn shield bonus if pawns are not very close to the king
            score -= BONUS_PAWN_SHIELD_WEAK * Long.bitCount((WEAK_SAFE_WHITE[blackKingSquare] & blackPawnCount));
        }
    }

}
