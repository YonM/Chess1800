package move;


/**
 * Created by Yonathan on 03/12/2014.
 * This class, as the name indicates, is for the moves of a chess game.
 * A move is represented by an int, which contains 32-bits. Based
 * on Alberto Ruibal's Carballo. Source @ https://github.com/albertoruibal/carballo/
 * The format is as follows:
 *
 *  Type | c | piece | To       | From  |
 *  100  | 0 | 001   | 111111   | 110111|
 *  MSB                             LSB
 *  Where 'Type' is the move type, such as a promotion (as in the above case).
 *  'c' is capture, representing whether the move is a capture or not.
 *  'piece' is the piece that is moving represented by a number from 1 to 6.
 *  'To' is the square on the bitboard, the piece is moving to. 6 bits for 0-63.
 *  'From' is the square the piece started its move from. Again, 6 bits for 0-63.
 *  @author Alberto Alonso Ruibal updated by Yonathan Maalo
 */
public class Move{


    // Move pieces ordered by value
    public static final int EMPTY = 0;
    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;

    // Move Types
    public static final int TYPE_KINGSIDE_CASTLING = 1;
    public static final int TYPE_QUEENSIDE_CASTLING = 2;
    public static final int TYPE_EN_PASSANT = 3;

    //Promotions
    // promotions must be always >= TYPE_PROMOTION_QUEEN
    public static final int TYPE_PROMOTION_QUEEN = 4;
    public static final int TYPE_PROMOTION_KNIGHT = 5;
    public static final int TYPE_PROMOTION_BISHOP = 6;
    public static final int TYPE_PROMOTION_ROOK = 7;
    
    //Masks
    public static final int SQUARE_MASK = 0x3f;
    public static final int TYPE_MASK = 0x7;

   /* public static int genMove(int fromIndex, int toIndex, int pieceMoved, boolean capture, int moveType) {
        return toIndex | fromIndex << 6 | pieceMoved << 12 | (capture ? 1 << 15 : 0) | moveType << 16;
    }*/

    public static final int genMove(int from, int to, int type, boolean capture, int flag) {
        return (from) | (to << 6) | (type << 12) | ((capture ? 1 : 0) << 15)
                | (flag << 16);
    }

    public static final int getFromIndex(int move) {
        return move & SQUARE_MASK;
    }

    public static final long getToSquare(int move) {
        return 0x1L << (move & SQUARE_MASK);
    }

    public static final int getToIndex(int move) {
        return ((move >>> 6) & SQUARE_MASK);
    }

    public static final long getFromSquare(int move) {
        return 0x1L << ((move >>> 6) & SQUARE_MASK);
    }

    /**
     * square index in a 64*64 array (12 bits)
     */
    public static final int getFromToIndex(int move) {
        return move & 0xfff;
    }

    public static final int getPieceMoved(int move) {
        return ((move >>> 12) & TYPE_MASK);
    }

    public static final boolean isCapture(int move) {
        return ((move >>> 15) & 0x1) != 0;
    }

    public static final int getMoveType(int move) {
        return ((move >>> 16) & TYPE_MASK);
    }


    public static final boolean isPromotion(int move) {
        return Move.getMoveType(move) >= TYPE_PROMOTION_QUEEN;
    }


}
