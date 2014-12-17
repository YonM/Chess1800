package Move;

/**
 * Created by Yonathan on 11/12/2014.
 * Moves are represented by an int.
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 * Structure:
 *
 * Prom | Capture   | Piece | To    | From
 * 0111 | 1101      | 0001  | 111101| 110110
 *
 * Prom - 4bit identifier for promotion piece but also for special moves such as en-passant and castling.
 * Capture - 4bit identifier for the piece being captured in the move.
 * Piece - 4bit identifier for the piece being moved.
 * To - 6bit identifier of the square being moved to.
 * From - 6bit identifier for the square being move from.
 */
public class Move {
    public static final int SQUARE_MASK = 0x3f;
    public static final int PIECE_TYPE_MASK = 0x0000000f;

    protected int moveInt;

    public Move(int move) {
        moveInt = move;
    }

    public Move() {
        moveInt = 0;
    }

    public static int generateMove(int from, int to, int piece, int capture, int promotion) {
        return from | to << 6 | piece << 12 | capture << 16 | promotion << 20;
    }

    public int getFrom() {
        return (moveInt & SQUARE_MASK);
    }

    public int getTo() {
        return (moveInt >>> 6) & SQUARE_MASK;
    }

    public int getPiece() {
        return (moveInt >>> 12) & PIECE_TYPE_MASK;
    }

    public int getCapture() {
        return (moveInt >>> 16) & PIECE_TYPE_MASK;
    }

    public int getPromotion() {
        return (moveInt >>> 20) & PIECE_TYPE_MASK;
    }

    public void setFrom(int from) {
        moveInt &= 0xffffffc0;
        moveInt |= (from & SQUARE_MASK);
    }

    public void setTo(int to) {
        moveInt &= 0xfffff03f;
        moveInt |= (to & SQUARE_MASK) << 6;
    }

    public void setPiece(int piece) {
        moveInt &= 0xffff0fff;
        moveInt |= (piece & PIECE_TYPE_MASK) << 12;
    }

    public void setCapture(int capture) {
        moveInt &= 0xfff0ffff;
        moveInt |= (capture & PIECE_TYPE_MASK) << 16;
    }

    public void setPromotion(int promotion) {
        moveInt &= 0xff0fffff;
        moveInt |= (promotion & PIECE_TYPE_MASK) << 20;
    }
/*    public static int getFrom(int move) {
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
    }*/


}
