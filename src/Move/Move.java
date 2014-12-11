package Move;

/**
 * Created by Yonathan on 11/12/2014.
 */
public class Move {
    public static final int SQUARE_MASK = 0x3f;
    public static final int PIECE_TYPE_MASK = 0x0000000f;

    public static int generateMove(int from, int to, int piece, int capture, int promotion) {
        return from | to << 6 | piece << 12 | capture << 16 | promotion << 20;
    }

    public static int getFrom(int move) {
        return (move & SQUARE_MASK);
    }

    public static int getTo(int move) {
        return (move >>> 6) & SQUARE_MASK;
    }

    public static int getPiece(int move) {
        return (move >>> 12) & PIECE_TYPE_MASK;
    }

    public static int getCapture(int move) {
        return (move >>> 16) & PIECE_TYPE_MASK;
    }

    public static int getPromotion(int move) {
        return (move >>> 20) & PIECE_TYPE_MASK;
    }

}
