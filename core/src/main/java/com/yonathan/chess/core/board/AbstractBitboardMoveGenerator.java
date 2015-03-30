package com.yonathan.chess.core.board;

import org.apache.commons.lang3.ArrayUtils;

import com.yonathan.chess.core.move.Move;

/**
 * Created by Yonathan on 15/01/2015.
 * Based on Alberto Ruibal's Carballo. Source @ https://githucom/albertoruibal/carballo/ &
 * Ulysse Carion's Godot. Source @ https://githucom/ucarion
 */
public abstract class AbstractBitboardMoveGenerator extends AbstractBitboardMagicAttacks implements MoveGenerator {


    private static final int RANK_1 = 0;
    private static final int RANK_2 = 1;
    private static final int RANK_3 = 2;
    private static final int RANK_4 = 3;
    private static final int RANK_5 = 4;
    private static final int RANK_6 = 5;
    private static final int RANK_7 = 6;
    private static final int RANK_8 = 7;
    private static final int FILE_A = 0;
    private static final int FILE_B = 1;
    private static final int FILE_C = 2;
    private static final int FILE_D = 3;
    private static final int FILE_E = 4;
    private static final int FILE_F = 5;
    private static final int FILE_G = 6;
    private static final int FILE_H = 7;

    /**
     * To get the square at A1, give 0. To get the square at A2, give 1. To get
     * the square at H8, give 63.
     */
    public static final long[] getSquare;
    // 0 is a, 7 is h
    public static final long[] COLUMN = {b_l, b_r << 6, b_r << 5, b_r << 4, b_r << 3, b_r << 2, b_r << 1, b_r};

    // 0 is 1, 7 is 8
    public static final long[] RANK = {b_d, b_d << 8, b_d << 16, b_d << 24, b_d << 32, b_d << 40, b_d << 48, b_d << 56};


    protected boolean whiteToMove;
    //Castling values
    public static final int CANCASTLEOO = 1;
    public static final int CANCASTLEOOO = 2;

    //For SEE & Quiescence Search
    public static final int MINCAPTVAL = 1;

    protected int initMoveNumber;

    protected int ePSquare;

    protected int castleWhite;
    protected int castleBlack;


    protected int[] moves;


    static {
        getSquare = new long[64];
        for (int i = 0; i < getSquare.length; i++) getSquare[i] = 1L << i;
    }


    public boolean isWhiteToMove() {
        return whiteToMove;
    }


    public int getCastleBlack() {
        return castleBlack;
    }


    public int getCastleWhite() {
        return castleWhite;
    }

    /**
     * Gets all <i>pseudo-legal</i> moves available for the side to move. If the
     * generated moves need to be legal (and not simply pseudo-legal), then
     * <code>MoveGenerator.getAllLegalMoves</code> should be used instead.
     *
     * @param moves the integer array to write onto
     * @return the number of <i>pseudo-legal</i> moves generated, with the
     * actual moves written onto the passed array.
     */
    public int getAllMoves(int[] moves) {
        if (whiteToMove)
            return getAllWhiteMoves(moves);
        return getAllBlackMoves(moves);
    }

    public int getAllLegalMoves(int[] moves) {
        int lastIndex = getAllMoves(moves);
        int j = 0;
        for (int i = 0; i < lastIndex; i++) {
            if (makeMove(moves[i])) {
                moves[j++] = moves[i];
                unmakeMove();
            }
        }
        return j;
    }

    public boolean legalMovesAvailable() {
        int[] moves = new int[MAX_MOVES];
        int lastIndex = getAllMoves(moves);
        for (int i = 0; i < lastIndex; i++) {
            if (makeMove(moves[i])) {
                unmakeMove();
                return true;
            }
        }
        return false;
    }


    private int getAllWhiteMoves(int[] moves) {
        int index = 0;
        index += getWhitePawnMoves(moves, index);
        index += getWhiteKnightMoves(moves, index);
        index += getWhiteKingMoves(moves, index);
        index += getWhiteRookMoves(moves, index);
        index += getWhiteBishopMoves(moves, index);
        index += getWhiteQueenMoves(moves, index);
        return index;
    }

    private int getAllBlackMoves(int[] moves) {
        int index = 0;
        index += getBlackPawnMoves(moves, index);
        index += getBlackKnightMoves(moves, index);
        index += getBlackKingMoves(moves, index);
        index += getBlackRookMoves(moves, index);
        index += getBlackBishopMoves(moves, index);
        index += getBlackQueenMoves(moves, index);
        return index;
    }

    // Pseudo-Legal capture generator.
    public int genCaptures(int[] captures) {
        int[] captureValues = new int[MAX_MOVES];
        int num_captures = getAllCaptures(captures);
        int val;
        int insertIndex;
        for (int i = 0; i < num_captures; i++) {
            val = sEE(captures[i]);
            captureValues[i] = val;
            if (val < MINCAPTVAL) {
                captures = ArrayUtils.remove(captures, i);
                captureValues = ArrayUtils.remove(captureValues, i);
                num_captures--;
                i--;
                continue;
            }
            insertIndex = i;

        }
        sortCaptures(captureValues, captures, num_captures);
        return num_captures;
    }

    private void sortCaptures(int[] captureValues, int[] captures, int num_captures) {
        //Insertion sort of captures.
        for (int i = 1; i < num_captures; i++) {
            int tempVal = captureValues[i];
            int tempCapture = captures[i];
            int j;
            for (j = i - 1; j >= 0 && tempVal > captureValues[j]; j--) {
                captureValues[j + 1] = captureValues[j];
                captures[j + 1] = captures[j];
            }
            captureValues[j + 1] = tempVal;
            captures[j + 1] = tempCapture;
        }
    }

    private int getAllCaptures(int[] captures) {
        int lastIndex = getAllMoves(captures);
        int num_captures = 0;
        for (int i = 0; i < lastIndex; i++) {
            if (Move.isPromotion(captures[i]) || Move.isCapture(captures[i]))
                captures[num_captures++] = captures[i];
        }
        return num_captures;

    }

    public abstract int sEE(int move);


    private int getAllNonCaptures(int[] nonCaptures) {
        int lastIndex = getAllMoves(nonCaptures);
        int num_non_captures = 0;
        for (int i = 0; i < lastIndex; i++) {
            if (!Move.isPromotion(nonCaptures[i]) || !Move.isCapture(nonCaptures[i]))
                nonCaptures[num_non_captures++] = nonCaptures[i];
        }
        return num_non_captures;
    }

    public abstract boolean makeMove(int move);



    public abstract void makeNullMove();

    public abstract void unmakeMove();

    public abstract void unmakeMove(int moveNumber);

    protected void updateAggregateBitboards() {
        whitePieces = whiteKing | whiteQueens | whiteRooks | whiteBishops | whiteKnights | whitePawns;
        blackPieces = blackKing | blackQueens | blackRooks | blackBishops | blackKnights | blackPawns;
        allPieces = whitePieces | blackPieces;
    }


    public int getEPSquare() {
        return ePSquare;
    }

    public long getAllPieces() {
        return allPieces;
    }

    public long getMyPieces() {
        return whiteToMove ? whitePieces : blackPieces;
    }

    public long getOpponentPieces() {
        return whiteToMove ? blackPieces : whitePieces;
    }


    /**
     * Gets what is found at a particular location.
     *
     * @param loc an integer in [0, 63] representing a position.
     * @return a character representing the piece at the passed location.
     */
    public char getPieceAt(int loc) {
        long sq = getSquare[loc];
        if (((whitePawns | blackPawns) & sq) != 0L)
            return (whitePieces & sq) != 0 ? 'P' : 'p';
        if (((whiteKnights | blackKnights) & sq) != 0L)
            return (whitePieces & sq) != 0 ? 'N' : 'n';
        if (((whiteBishops | blackBishops) & sq) != 0L)
            return (whitePieces & sq) != 0 ? 'B' : 'b';
        if (((whiteRooks | blackRooks) & sq) != 0L)
            return (whitePieces & sq) != 0 ? 'R' : 'r';
        if (((whiteQueens | blackQueens) & sq) != 0L)
            return (whitePieces & sq) != 0 ? 'Q' : 'q';
        if (((whiteKing | blackKing) & sq) != 0L)
            return (whitePieces & sq) != 0 ? 'K' : 'k';
        return ' ';
    }

    private final long[] kingMoves =
            {0x303L, 0x707L, 0xe0eL, 0x1c1cL, 0x3838L, 0x7070L, 0xe0e0L, 0xc0c0L, 0x30303L, 0x70707L, 0xe0e0eL, 0x1c1c1cL, 0x383838L, 0x707070L, 0xe0e0e0L, 0xc0c0c0L, 0x3030300L, 0x7070700L, 0xe0e0e00L, 0x1c1c1c00L, 0x38383800L, 0x70707000L, 0xe0e0e000L, 0xc0c0c000L, 0x303030000L, 0x707070000L, 0xe0e0e0000L, 0x1c1c1c0000L, 0x3838380000L, 0x7070700000L, 0xe0e0e00000L, 0xc0c0c00000L, 0x30303000000L, 0x70707000000L, 0xe0e0e000000L, 0x1c1c1c000000L, 0x383838000000L, 0x707070000000L, 0xe0e0e0000000L, 0xc0c0c0000000L, 0x3030300000000L, 0x7070700000000L, 0xe0e0e00000000L, 0x1c1c1c00000000L, 0x38383800000000L, 0x70707000000000L, 0xe0e0e000000000L, 0xc0c0c000000000L, 0x303030000000000L, 0x707070000000000L, 0xe0e0e0000000000L, 0x1c1c1c0000000000L, 0x3838380000000000L, 0x7070700000000000L, 0xe0e0e00000000000L, 0xc0c0c00000000000L, 0x303000000000000L, 0x707000000000000L, 0xe0e000000000000L, 0x1c1c000000000000L, 0x3838000000000000L, 0x7070000000000000L, 0xe0e0000000000000L, 0xc0c0000000000000L};
    private final long[] knightMoves =
            {0x20400L, 0x50800L, 0xa1100L, 0x142200L, 0x284400L, 0x508800L, 0xa01000L, 0x402000L, 0x2040004L, 0x5080008L, 0xa110011L, 0x14220022L, 0x28440044L, 0x50880088L, 0xa0100010L, 0x40200020L, 0x204000402L, 0x508000805L, 0xa1100110aL, 0x1422002214L, 0x2844004428L, 0x5088008850L, 0xa0100010a0L, 0x4020002040L, 0x20400040200L, 0x50800080500L, 0xa1100110a00L, 0x142200221400L, 0x284400442800L, 0x508800885000L, 0xa0100010a000L, 0x402000204000L, 0x2040004020000L, 0x5080008050000L, 0xa1100110a0000L, 0x14220022140000L, 0x28440044280000L, 0x50880088500000L, 0xa0100010a00000L, 0x40200020400000L, 0x204000402000000L, 0x508000805000000L, 0xa1100110a000000L, 0x1422002214000000L, 0x2844004428000000L, 0x5088008850000000L, 0xa0100010a0000000L, 0x4020002040000000L, 0x400040200000000L, 0x800080500000000L, 0x1100110a00000000L, 0x2200221400000000L, 0x4400442800000000L, 0x8800885000000000L, 0x100010a000000000L, 0x2000204000000000L, 0x4020000000000L, 0x8050000000000L, 0x110a0000000000L, 0x22140000000000L, 0x44280000000000L, 0x88500000000000L, 0x10a00000000000L, 0x20400000000000L};

    private long pseudoLegalKnightMoveDestinations(int loc, long targets) {
        return knightMoves[loc] & targets;
    }

    private long pseudoLegalKingMoveDestinations(int loc, long targets) {
        return kingMoves[loc] & targets;
    }


    public int getWhiteKingMoves(int[] moves, int index) {
        long king = whiteKing;
        int num_moves_generated = 0;
        int fromIndex = Long.numberOfTrailingZeros(king);
        long movelocs = pseudoLegalKingMoveDestinations(fromIndex, ~whitePieces);
        while (movelocs != 0) {
            long toBoard = Long.lowestOneBit(movelocs);
            int toIndex = Long.numberOfTrailingZeros(toBoard);
            boolean capt = (toBoard & blackPieces) != 0;
            int move = Move.genMove(fromIndex, toIndex, Move.KING, capt, 0);
            moves[index + num_moves_generated++] = move;
            movelocs &= ~toBoard;
        }
        if ((castleWhite & CANCASTLEOO) != 0) {
            if ((whiteKing << 1 & allPieces) == 0
                    && (whiteKing << 2 & allPieces) == 0) {
                if ((whiteKing << 3 & whiteRooks) != 0) {
                    if (!isSquareAttacked(whiteKing, true)
                            && !isSquareAttacked(whiteKing << 1,
                            true)
                            && !isSquareAttacked(whiteKing << 2,
                            true)) {
                        moves[index + num_moves_generated++] =
                                Move.genMove(fromIndex, fromIndex + 2, Move.KING,
                                        false, Move.TYPE_KINGSIDE_CASTLING);
                    }
                }
            }
        }
        if ((castleWhite & CANCASTLEOOO) != 0) {
            if ((whiteKing >>> 1 & allPieces) == 0
                    && (whiteKing >>> 2 & allPieces) == 0
                    && (whiteKing >>> 3 & allPieces) == 0) {
                if ((whiteKing >>> 4 & whiteRooks) != 0) {
                    if (!isSquareAttacked(whiteKing, true)
                            && !isSquareAttacked(whiteKing >>> 1,
                            true)
                            && !isSquareAttacked(whiteKing >>> 2,
                            true)) {
                        moves[index + num_moves_generated++] =
                                Move.genMove(fromIndex, fromIndex - 2, Move.KING,
                                        false, Move.TYPE_QUEENSIDE_CASTLING);
                    }
                }
            }
        }
        return num_moves_generated;
    }

    public int getBlackKingMoves(int[] moves, int index) {
        long king = blackKing;
        int num_moves_generated = 0;
        int fromIndex = Long.numberOfTrailingZeros(king);
        long movelocs = pseudoLegalKingMoveDestinations(fromIndex, ~blackPieces);
        while (movelocs != 0) {
            long toBoard = Long.lowestOneBit(movelocs);
            int toIndex = Long.numberOfTrailingZeros(toBoard);
            boolean capt = (toBoard & whitePieces) != 0;
            int move = Move.genMove(fromIndex, toIndex, Move.KING, capt, 0);
            moves[index + num_moves_generated++] = move;
            movelocs &= ~toBoard;
        }
        if ((castleBlack & CANCASTLEOO) != 0) {
            if ((blackKing << 1 & allPieces) == 0
                    && (blackKing << 2 & allPieces) == 0) {
                if ((blackKing << 3 & blackRooks) != 0) {
                    if (!isSquareAttacked(blackKing, false)
                            && !isSquareAttacked(blackKing << 1,
                            false)
                            && !isSquareAttacked(blackKing << 2,
                            false)) {
                        moves[index + num_moves_generated++] =
                                Move.genMove(fromIndex, fromIndex + 2, Move.KING,
                                        false, Move.TYPE_KINGSIDE_CASTLING);
                    }
                }
            }
        }
        if ((castleBlack & CANCASTLEOOO) != 0) {
            if ((blackKing >>> 1 & allPieces) == 0
                    && (blackKing >>> 2 & allPieces) == 0
                    && (blackKing >>> 3 & allPieces) == 0) {
                if ((blackKing >>> 4 & blackRooks) != 0) {
                    if (!isSquareAttacked(blackKing, false)
                            && !isSquareAttacked(blackKing >>> 1,
                            false)
                            && !isSquareAttacked(blackKing >>> 2,
                            false)) {
                        moves[index + num_moves_generated++] =
                                Move.genMove(fromIndex, fromIndex - 2, Move.KING,
                                        false, Move.TYPE_QUEENSIDE_CASTLING);
                    }
                }
            }
        }
        return num_moves_generated;
    }

    public int getWhiteKnightMoves(int[] moves, int index) {
        long knights = whiteKnights;
        int num_moves_generated = 0;
        while (knights != 0) {
            long fromBoard = Long.lowestOneBit(knights);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs = pseudoLegalKnightMoveDestinations(fromIndex, ~whitePieces);
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & blackPieces) != 0;
                int move =
                        Move.genMove(fromIndex, toIndex, Move.KNIGHT, capt,
                                0);
                moves[index + num_moves_generated++] = move;
                movelocs &= ~to;
            }
            knights &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getBlackKnightMoves(int[] moves, int index) {
        long knights = blackKnights;
        int num_moves_generated = 0;
        while (knights != 0) {
            long fromBoard = Long.lowestOneBit(knights);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs = pseudoLegalKnightMoveDestinations(fromIndex, ~blackPieces);
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & whitePieces) != 0;
                int move =
                        Move.genMove(fromIndex, toIndex, Move.KNIGHT, capt,
                                0);
                moves[index + num_moves_generated++] = move;
                movelocs &= ~to;
            }
            knights &= ~fromBoard;
        }
        return num_moves_generated;
    }

    /*
    * OK, this can be done the following way:
    * (1) Check what rank the pawn is on.
    * (2a) If the pawn is on the 7th rank (2nd for black), we can forget about
    * double jump and we must consider promotions.
    * (2b) If the pawn isn't on the 7th, then we proceed normally.
    *
    * Special note: If the pawn is on the a-file, it cannot capture left; the
    * same goes for an h-file pawn and capturing right.
    */
    public int getWhitePawnMoves(int[] moves, int index) {
        long pawns = whitePawns;
        int num_moves_generated = 0;
        while (pawns != 0) {
            long fromBoard = Long.lowestOneBit(pawns);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            if ((fromBoard & RANK[RANK_7]) != 0) {
                // promos are possible, no en passant
                if ((fromBoard & b_r) == 0 && (fromBoard << 7 & blackPieces) != 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 7);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_QUEEN);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_KNIGHT);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_ROOK);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_BISHOP);
                }
                if ((fromBoard & b_l) == 0 && (fromBoard << 9 & blackPieces) != 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 9);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_QUEEN);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_KNIGHT);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_ROOK);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_BISHOP);
                }
                if ((fromBoard << 8 & allPieces) == 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 8);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, false,
                                    Move.TYPE_PROMOTION_QUEEN);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, false,
                                    Move.TYPE_PROMOTION_KNIGHT);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, false,
                                    Move.TYPE_PROMOTION_ROOK);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, false,
                                    Move.TYPE_PROMOTION_BISHOP);
                }
            } else {
                // no promos to worry about, but there is en passant
                if (((fromBoard & b_r) == 0)
                        && (((fromBoard << 7 & blackPieces) != 0) || Long.numberOfTrailingZeros(fromBoard << 7) == ePSquare)) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 7);
                    if (Long.numberOfTrailingZeros(fromBoard << 7) == ePSquare)
                        moves[index + num_moves_generated++] =
                                Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                        Move.TYPE_EN_PASSANT);
                    else
                        moves[index + num_moves_generated++] =
                                Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                        0);
                }
                if (((fromBoard & b_l) == 0)
                        && (((fromBoard << 9 & blackPieces) != 0) || Long.numberOfTrailingZeros(fromBoard << 9) == ePSquare)) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 9);
                    if (Long.numberOfTrailingZeros(fromBoard << 9) == ePSquare)
                        moves[index + num_moves_generated++] =
                                Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                        Move.TYPE_EN_PASSANT);
                    else
                        moves[index + num_moves_generated++] =
                                Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                        0);
                }
                boolean one_square_ahead_clear = false;
                if ((fromBoard << 8 & allPieces) == 0) {
                    one_square_ahead_clear = true;
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 8);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, false,
                                    0);
                }
                if ((fromBoard & RANK[RANK_2]) != 0
                        && one_square_ahead_clear && (fromBoard << 16 & allPieces) == 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 16);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, false,
                                    0);
                }
            }
            pawns &= ~fromBoard;
        }
        return num_moves_generated;
    }

    /*
    * See description of getWhitePawnMoves for an explanation of logic.
    */
    public int getBlackPawnMoves(int[] moves, int index) {
        long pawns = blackPawns;
        int num_moves_generated = 0;
        while (pawns != 0) {
            long fromBoard = Long.lowestOneBit(pawns);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            if ((fromBoard & RANK[RANK_2]) != 0) {
                // promos are possible, no en passant
                if ((fromBoard & b_l) == 0 && (fromBoard >>> 7 & whitePieces) != 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 7);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_QUEEN);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_KNIGHT);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_ROOK);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_BISHOP);
                }
                if ((fromBoard & b_r) == 0
                        && (fromBoard >>> 9 & whitePieces) != 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 9);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_QUEEN);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_KNIGHT);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_ROOK);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                    Move.TYPE_PROMOTION_BISHOP);
                }
                if ((fromBoard >>> 8 & allPieces) == 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 8);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, false,
                                    Move.TYPE_PROMOTION_QUEEN);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, false,
                                    Move.TYPE_PROMOTION_KNIGHT);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, false,
                                    Move.TYPE_PROMOTION_ROOK);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, false,
                                    Move.TYPE_PROMOTION_BISHOP);
                }
            } else {
                // no promos to worry about, but there is en passant
                if (((fromBoard & b_l) == 0)
                        && (((fromBoard >>> 7 & whitePieces) != 0) || Long.numberOfTrailingZeros(fromBoard >>> 7) == ePSquare)) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 7);
                    if (Long.numberOfTrailingZeros(fromBoard >>> 7) == ePSquare)
                        moves[index + num_moves_generated++] =
                                Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                        Move.TYPE_EN_PASSANT);
                    else
                        moves[index + num_moves_generated++] =
                                Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                        0);
                }
                if (((fromBoard & b_r) == 0)
                        && (((fromBoard >>> 9 & whitePieces) != 0) || Long.numberOfTrailingZeros(fromBoard >>> 9) == ePSquare)) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 9);
                    if (Long.numberOfTrailingZeros(fromBoard >>> 9) == ePSquare)
                        moves[index + num_moves_generated++] =
                                Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                        Move.TYPE_EN_PASSANT);
                    else
                        moves[index + num_moves_generated++] =
                                Move.genMove(fromIndex, toIndex, Move.PAWN, true,
                                        0);
                }
                boolean one_square_ahead_clear = false;
                if ((fromBoard >>> 8 & allPieces) == 0) {
                    one_square_ahead_clear = true;
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 8);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, false,
                                    0);
                }
                if ((fromBoard & RANK[RANK_7]) != 0
                        && one_square_ahead_clear && (fromBoard >>> 16 & allPieces) == 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 16);
                    moves[index + num_moves_generated++] =
                            Move.genMove(fromIndex, toIndex, Move.PAWN, false,
                                    0);
                }
            }
            pawns &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getWhiteBishopMoves(int[] moves, int index) {
        long bishops = whiteBishops;
        int num_moves_generated = 0;
        while (bishops != 0) {
            long fromBoard = Long.lowestOneBit(bishops);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs = getBishopAttacks(fromIndex, allPieces & ~fromBoard);
            movelocs &= ~whitePieces;
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & blackPieces) != 0;
                int move =
                        Move.genMove(fromIndex, toIndex, Move.BISHOP, capt,
                                0);
                moves[index + num_moves_generated++] = move;
                movelocs &= ~to;
            }
            bishops &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getBlackBishopMoves(int[] moves, int index) {
        long bishops = blackBishops;
        int num_moves_generated = 0;
        while (bishops != 0) {
            long fromBoard = Long.lowestOneBit(bishops);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs = getBishopAttacks(fromIndex, allPieces & ~fromBoard);
            movelocs &= ~blackPieces;
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & whitePieces) != 0;
                int move =
                        Move.genMove(fromIndex, toIndex, Move.BISHOP, capt,
                                0);
                moves[index + num_moves_generated++] = move;
                movelocs &= ~to;
            }
            bishops &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getWhiteRookMoves(int[] moves, int index) {
        long rooks = whiteRooks;
        int num_moves_generated = 0;
        while (rooks != 0) {
            long fromBoard = Long.lowestOneBit(rooks);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs = getRookAttacks(fromIndex, allPieces & ~fromBoard);
            movelocs &= ~whitePieces;
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & blackPieces) != 0;
                int move =
                        Move.genMove(fromIndex, toIndex, Move.ROOK, capt, 0);
                moves[index + num_moves_generated++] = move;
                movelocs &= ~to;
            }
            rooks &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getBlackRookMoves(int[] moves, int index) {
        long rooks = blackRooks;
        int num_moves_generated = 0;
        while (rooks != 0) {
            long fromBoard = Long.lowestOneBit(rooks);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs = getRookAttacks(fromIndex, allPieces & ~fromBoard);
            movelocs &= ~blackPieces;
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & whitePieces) != 0;
                int move =
                        Move.genMove(fromIndex, toIndex, Move.ROOK, capt, 0);
                moves[index + num_moves_generated++] = move;
                movelocs &= ~to;
            }
            rooks &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getWhiteQueenMoves(int[] moves, int index) {
        long queens = whiteQueens;
        int num_moves_generated = 0;
        while (queens != 0) {
            long fromBoard = Long.lowestOneBit(queens);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs = getQueenAttacks(fromIndex, allPieces & ~fromBoard);
            movelocs &= ~whitePieces;
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & blackPieces) != 0;
                int move =
                        Move.genMove(fromIndex, toIndex, Move.QUEEN, capt,
                                0);
                moves[index + num_moves_generated++] = move;
                movelocs &= ~to;
            }
            queens &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getBlackQueenMoves(int[] moves, int index) {
        long queens = blackQueens;
        int num_moves_generated = 0;
        while (queens != 0) {
            long fromBoard = Long.lowestOneBit(queens);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs = getQueenAttacks(fromIndex, allPieces & ~fromBoard);
            movelocs &= ~blackPieces;
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & whitePieces) != 0;
                int move =
                        Move.genMove(fromIndex, toIndex, Move.QUEEN, capt,
                                0);
                moves[index + num_moves_generated++] = move;
                movelocs &= ~to;
            }
            queens &= ~fromBoard;
        }
        return num_moves_generated;
    }

}
