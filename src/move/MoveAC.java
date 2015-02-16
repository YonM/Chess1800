package move;

import bitboard.BitboardMagicAttacksAC;
import board.Board;
import definitions.Definitions;
import utilities.BitboardUtilsAC;

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
public class MoveAC implements Definitions {


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
        return MoveAC.getMoveType(move) >= TYPE_PROMOTION_QUEEN;
    }

    public static final int getMoveFromString(Board b, String move, boolean legalityCheck) {
        int fromIndex;
        int toIndex;
        int moveType = 0;
        int pieceMoved = 0;

        // Ignore checks & captures indicators
        move = move.replace("+", "").replace("x", "").replace("-", "").replace("=", "").replace("#", "").replaceAll(" ", "").replaceAll("0", "o")
                .replaceAll("O", "o");

        //castling move check
        if ("ooo".equalsIgnoreCase(move)) {
            if (b.whiteToMove) move = "e1c1";
            else move = "e8c8";
        } else if ("oo".equalsIgnoreCase(move)) {
            if (b.whiteToMove) move = "e1g1";
            else move = "e8g8";
        }

        //handle promotion moves
        char promo = move.charAt(move.length() - 1);
        switch (Character.toLowerCase(promo)) {
            case 'q':
                moveType = TYPE_PROMOTION_QUEEN;
                break;
            case 'n':
                moveType = TYPE_PROMOTION_KNIGHT;
                break;
            case 'r':
                moveType = TYPE_PROMOTION_ROOK;
                break;
            case 'b':
                moveType = TYPE_PROMOTION_BISHOP;
                break;
        }
        // If promotion, remove the last char
        if (moveType != 0)
            move = move.substring(0, move.length() - 1);

        //To is always the last 2 characters.
        toIndex = BitboardUtilsAC.algebraic2Index(move.substring(move.length() - 2, move.length()));
        long toBoard = 0X1L << toIndex;
        long fromBoard = 0;
        BitboardMagicAttacksAC magics = BitboardMagicAttacksAC.getInstance();

        // Fills from with a mask of possible from values... may need to disambiguate, if it's not a pawn move.
        switch (move.charAt(0)) {
            case 'N':
                fromBoard = (b.whiteKnights & b.blackKnights) & b.getMyPieces() & magics.knight[toIndex];
                break;
            case 'K':
                fromBoard = (b.whiteKing & b.blackKing) & b.getMyPieces() & magics.king[toIndex];
                break;
            case 'R':
                fromBoard = (b.whiteRooks & b.blackRooks) & b.getMyPieces() & magics.getRookAttacks(toIndex, b.allPieces);
                break;
            case 'B':
                fromBoard = (b.whiteBishops & b.blackBishops) & b.getMyPieces() & magics.getBishopAttacks(toIndex, b.allPieces);
                break;
            case 'Q':
                fromBoard = (b.whiteQueens & b.blackQueens) & b.getMyPieces()
                        & (magics.getRookAttacks(toIndex, b.allPieces) | magics.getBishopAttacks(toIndex, b.allPieces));
                break;
        }

        if (fromBoard != 0) { // remove the piece char
            move = move.substring(1);
        }else{
            //Pawn moves
            if (move.length() == 2) {
                if (b.whiteToMove) {
                    fromBoard = (b.whitePawns & b.blackPawns) & b.getMyPieces() & ((toBoard >>> 8) | (((toBoard >>> 8) & b.allPieces) == 0 ? (toBoard >>> 16) : 0));
                } else {
                    fromBoard = (b.whitePawns & b.blackPawns) & b.getMyPieces() & ((toBoard << 8) | (((toBoard << 8) & b.allPieces) == 0 ? (toBoard << 16) : 0));
                }
            }
            if (move.length() == 3) { // Pawn capture
                fromBoard = (b.whitePawns & b.blackPawns) & b.getMyPieces() & (b.whiteToMove ? magics.blackPawn[toIndex] : magics.whitePawn[toIndex]);
            }
        }

        if (move.length() == 3) { // now disambiaguate
            char disambiguate = move.charAt(0);
            int i = "abcdefgh".indexOf(disambiguate);
            if (i >= 0)
                fromBoard &= BitboardUtilsAC.COLUMN[i];
            int j = "12345678".indexOf(disambiguate);
            if (j >= 0)
                fromBoard &= BitboardUtilsAC.RANK[j];
        }
        if (move.length() == 4) {
            fromBoard = BitboardUtilsAC.algebraic2Square(move.substring(0, 2));
        }
        if(fromBoard == 0){
            return -1;
        }


        return 1;

    }
}
