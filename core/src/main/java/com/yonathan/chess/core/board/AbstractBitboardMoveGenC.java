package com.yonathan.chess.core.board;


import org.apache.commons.lang3.ArrayUtils;

import com.yonathan.chess.core.move.Move;

/**
 * Created by Yonathan on 21/03/2015.
 */
public abstract class AbstractBitboardMoveGenC extends AbstractBitboardMagicAttacks implements MoveGenerator {

    private int[] moves;
    private int moveIndex;
    private long others;


    protected boolean whiteToMove;
    protected int castleWhite;
    protected int castleBlack;
    protected long ePSquare;
    public static final int CANCASTLEOO = 1;
    public static final int CANCASTLEOOO = 2;

    //For SEE & Quiescence Search
    public static final int MINCAPTVAL = 1;

    protected int initMoveNumber;

    public static final long[] getSquare;

    static {
        getSquare = new long[64];
        for (int i = 0; i < getSquare.length; i++) getSquare[i] = 1L << i;
    }

    public static final long[] COLUMN = {b_l, b_r << 6, b_r << 5, b_r << 4, b_r << 3, b_r << 2, b_r << 1, b_r};

    // 0 is Rank 1, 7 is Rank 8
    public static final long[] RANK = {b_d, b_d << 8, b_d << 16, b_d << 24, b_d << 32, b_d << 40, b_d << 48, b_d << 56};


    public int getAllMoves(int[] moves) {
        this.moves = moves;
        moveIndex = 0;
        long all = allPieces;
        long mines = getMyPieces();
        others = getOpponentPieces();

        int index = 0;
        long square = 0x1L;
        while (square != 0) {
            if (isWhiteToMove() == ((square & whitePieces) != 0)) {

                if ((square & (whiteRooks | blackRooks)) != 0) { // Rook
                    generateMovesFromAttacks(Move.ROOK, index, getRookAttacks(index, all) & ~mines);
                } else if ((square & (whiteBishops | blackBishops)) != 0) { // Bishop
                    generateMovesFromAttacks(Move.BISHOP, index, getBishopAttacks(index, all) & ~mines);
                } else if ((square & (whiteQueens | blackQueens)) != 0) { // Queen
                    generateMovesFromAttacks(Move.QUEEN, index, (getRookAttacks(index, all) | getBishopAttacks(index, all)) & ~mines);
                } else if ((square & (whiteKing | blackKing)) != 0) { // King
                    generateMovesFromAttacks(Move.KING, index, king[index] & ~mines);
                } else if ((square & (whiteKnights | blackKnights)) != 0) { // Knight
                    generateMovesFromAttacks(Move.KNIGHT, index, knight[index] & ~mines);
                } else if ((square & (whitePawns | blackPawns)) != 0) { // Pawns
                    if ((square & whitePieces) != 0) {
                        if (((square << 8) & all) == 0) {
                            addMoves(Move.PAWN, index, index + 8, false, 0);
                            // Two squares if it is in he first row
                            if (((square & b2_d) != 0) && (((square << 16) & all) == 0))
                                addMoves(Move.PAWN, index, index + 16, false, 0);
                        }
                        generatePawnCapturesFromAttacks(index, whitePawn[index], ePSquare);
                    } else {
                        if (((square >>> 8) & all) == 0) {
                            addMoves(Move.PAWN, index, index - 8, false, 0);
                            // Two squares if it is in he first row
                            if (((square & b2_u) != 0) && (((square >>> 16) & all) == 0))
                                addMoves(Move.PAWN, index, index - 16, false, 0);
                        }
                        generatePawnCapturesFromAttacks(index, blackPawn[index], ePSquare);
                    }
                }
            }
            square <<= 1;
            index++;
        }

        square = (whiteKing | blackKing) & mines; // my king
        int myKingIndex = -1;
        // Castling: disabled when in check or squares attacked
        if ((((all & (isWhiteToMove() ? 0x06L : 0x0600000000000000L)) == 0 &&
                (isWhiteToMove() ? ((castleWhite & CANCASTLEOO) != 0) : ((castleBlack & CANCASTLEOO) != 0))
        ))) {
            myKingIndex = square2Index(square);
            if (!isCheck() &&
                    !isIndexAttacked((byte) (myKingIndex - 1), isWhiteToMove())
                    && !isIndexAttacked((byte) (myKingIndex - 2), isWhiteToMove()))
                addMoves(Move.KING, myKingIndex, myKingIndex - 2, false, Move.TYPE_KINGSIDE_CASTLING);
        }
        if ((((all & (isWhiteToMove() ? 0x70L : 0x7000000000000000L)) == 0 &&
                (isWhiteToMove() ? ((castleWhite & CANCASTLEOOO) != 0) : ((castleBlack & CANCASTLEOOO) != 0))
        ))) {
            if (myKingIndex == -1) {
                myKingIndex = square2Index(square);
            }
            if (!isCheck() &&
                    !isIndexAttacked((byte) (myKingIndex + 1), isWhiteToMove())
                    && !isIndexAttacked((byte) (myKingIndex + 2), isWhiteToMove()))
                addMoves(Move.KING, myKingIndex, myKingIndex + 2, false, Move.TYPE_QUEENSIDE_CASTLING);
        }
        return moveIndex;
    }

    public int getAllLegalMoves(int[] moves) {
        int moveIndex = getAllMoves(moves);
        int j = 0;
        for (int i = 0; i < moveIndex; i++) {
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

    @Override
    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    protected final long getAllPieces() {
        return (whitePieces | blackPieces);
    }

    protected final long getMyPieces() {
        return whiteToMove ? whitePieces : blackPieces;
    }

    protected final long getOpponentPieces() {
        return whiteToMove ? blackPieces : whitePieces;
    }

    public int getCastleBlack() {
        return castleBlack;
    }


    public int getCastleWhite() {
        return castleWhite;
    }

    public long getEPSquare() {
        return ePSquare;
    }


    protected final void updateAggregateBitboards() {
        whitePieces = whiteKing | whiteQueens | whiteRooks | whiteBishops | whiteKnights | whitePawns;
        blackPieces = blackKing | blackQueens | blackRooks | blackBishops | blackKnights | blackPawns;
        allPieces = whitePieces | blackPieces;
    }


    /**
     * Gets what is found at a particular location.
     *
     * @param square an integer in [0, 63] representing a position.
     * @return a character representing the piece at the passed location.
     */
    public char getPieceAt(long square) {
        if (((whitePawns | blackPawns) & square) != 0L)
            return (whitePieces & square) != 0 ? 'P' : 'p';
        if (((whiteKnights | blackKnights) & square) != 0L)
            return (whitePieces & square) != 0 ? 'N' : 'n';
        if (((whiteBishops | blackBishops) & square) != 0L)
            return (whitePieces & square) != 0 ? 'B' : 'b';
        if (((whiteRooks | blackRooks) & square) != 0L)
            return (whitePieces & square) != 0 ? 'R' : 'r';
        if (((whiteQueens | blackQueens) & square) != 0L)
            return (whitePieces & square) != 0 ? 'Q' : 'q';
        if (((whiteKing | blackKing) & square) != 0L)
            return (whitePieces & square) != 0 ? 'K' : 'k';
        return ' ';
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


    private int getAllCaptures(int[] captures) {
        int lastIndex = getAllMoves(captures);
        int num_captures = 0;
        for (int i = 0; i < lastIndex; i++) {
            if (Move.isPromotion(captures[i]) || Move.isCapture(captures[i]))
                captures[num_captures++] = captures[i];
        }
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


    /**
     * Generates moves from an attack mask
     */
    private void generateMovesFromAttacks(int pieceMoved, int fromIndex, long attacks) {
        while (attacks != 0) {
            long to = Long.lowestOneBit(attacks);
            addMoves(pieceMoved, fromIndex, square2Index(to), ((to & others) != 0), 0);
            attacks ^= to;
        }
    }

    private void generatePawnCapturesFromAttacks(int fromIndex, long attacks, long passant) {
        while (attacks != 0) {
            long to = Long.lowestOneBit(attacks);
            if ((to & others) != 0) {
                addMoves(Move.PAWN, fromIndex, square2Index(to), true, 0);
            } else if ((to & passant) != 0) {
                addMoves(Move.PAWN, fromIndex, square2Index(to), true, Move.TYPE_EN_PASSANT);
            }
            attacks ^= to;
        }
    }

    /**
     * Adds a move
     */
    private void addMoves(int pieceMoved, int fromIndex, int toIndex, boolean capture, int moveType) {
        if (pieceMoved == Move.PAWN && (toIndex < 8 || toIndex >= 56)) {
            moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_QUEEN);
            moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_KNIGHT);
            moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_ROOK);
            moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_BISHOP);
        } else {
            moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);
        }
    }
}
