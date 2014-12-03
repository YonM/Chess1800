package Move;

/**
 * Created by Yonathan on 03/12/2014.
 * This class, as the name indicates, is for the moves of a chess game.
 * A move is represented by an int, which contains 32-bits. Based
 * on Ruibal's Carballo.
 */
public class Move {

    // Move pieces ordered by value
    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;

    // Move Types
    public static final int TYPE_KINGSIDE_CASTLING = 1;
    public static final int TYPE_QUEENSIDE_CASTLING = 2;
    public static final int TYPE_PASSANT = 3;
    // promotions must be always >= TYPE_PROMOTION_QUEEN
    public static final int TYPE_PROMOTION_QUEEN = 4;
    public static final int TYPE_PROMOTION_KNIGHT = 5;
    public static final int TYPE_PROMOTION_BISHOP = 6;
    public static final int TYPE_PROMOTION_ROOK = 7;

    public static int genMove(int fromIndex, int toIndex, int pieceMoved, boolean capture, int moveType) {
        return toIndex | fromIndex << 6 | pieceMoved << 12 | (capture ? 1 << 15 : 0) | moveType << 16;
    }

    public static int getToIndex(int move) {
        return move & 0x3f;
    }

    public static long getToSquare(int move) {
        return 0x1L << (move & 0x3f);
    }

    public static int getFromIndex(int move) {
        return ((move >>> 6) & 0x3f);
    }

    public static long getFromSquare(int move) {
        return 0x1L << ((move >>> 6) & 0x3f);
    }

    /**
     * square index in a 64*64 array (12 bits)
     */
    public static int getFromToIndex(int move) {
        return move & 0xfff;
    }

    public static int getPieceMoved(int move) {
        return ((move >>> 12) & 0x7);
    }

    public static boolean getCapture(int move) {
        return ((move >>> 15) & 0x1) != 0;
    }

    public static int getMoveType(int move) {
        return ((move >>> 16) & 0x7);
    }

    public static boolean isCapture(int move) {
        return (move & (0x1 << 15)) != 0;
    }



}
