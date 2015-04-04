package com.yonathan.chess.core.move;


import com.yonathan.chess.core.board.Bitboard;
import com.yonathan.chess.core.board.Chessboard;

/**
 * Created by Yonathan on 03/12/2014.
 * This class, as the name indicates, is for the moves of a chess game.
 * A move is represented by an int, which contains 32-bits. Based
 * on Alberto Ruibal's Carballo. Source @ https://github.com/albertoruibal/carballo/
 * The format is as follows:
 * <p/>
 * Type | c | piece | To       | From  |
 * 100  | 0 | 001   | 111111   | 110111|
 * MSB                             LSB
 * Where 'Type' is the move type, such as a promotion (as in the above case).
 * 'c' is capture, representing whether the move is a capture or not.
 * 'piece' is the piece that is moving represented by a number from 1 to 6.
 * 'To' is the square on the bitboard, the piece is moving to. 6 bits for 0-63.
 * 'From' is the square the piece started its move from. Again, 6 bits for 0-63.
 *
 * @author Alberto Alonso Ruibal updated by Yonathan Maalo
 */
public class Move {

    //For TT entry
    public static final int MOVE_MASK = 0x7FFFF;
    public static final int MOVE_SHIFT =19;

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

    public static final int genMove(int from, int to, int piece, boolean capture, int flag) {
        return (from) | (to << 6) | (piece << 12) | ((capture ? 1 : 0) << 15)
                | (flag << 16);
    }

    public static final int getFromIndex(int move) {
        return move & SQUARE_MASK;
    }

    public static final long getFromSquare(int move) {
        return 0x1L << (move & SQUARE_MASK);
    }

    public static final int getToIndex(int move) {
        return ((move >>> 6) & SQUARE_MASK);
    }

    public static final long getToSquare(int move) {
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

    public static final boolean isUnderPromotion(int move) {
        return  Move.getMoveType(move) > TYPE_PROMOTION_QUEEN;
    }

    public static final String getPromotionPiece(int move){
        switch (getMoveType(move)) {
            case TYPE_PROMOTION_QUEEN:
                return "q";
            case TYPE_PROMOTION_ROOK:
                return "r";
            case TYPE_PROMOTION_KNIGHT:
                return "n";
            case TYPE_PROMOTION_BISHOP:
                return "b";
            default:
                return " ";
        }
    }

    //SAN representation
    public static String moveToString(int move, Chessboard board) {
        String moveString = "";
        if (Move.getMoveType(move) == Move.TYPE_KINGSIDE_CASTLING)
            return "0-0";
        if (Move.getMoveType(move) == Move.TYPE_QUEENSIDE_CASTLING)
            return "0-0-0";
        switch (Move.getPieceMoved(move)) {
            case Move.KNIGHT:
                moveString += "N";
                break;
            case Move.BISHOP:
                moveString += "B";
                break;
            case Move.ROOK:
                moveString += "R";
                break;
            case Move.QUEEN:
                moveString += "Q";
                break;
            case Move.KING:
                moveString += "K";
                break;
            default:
                moveString = "";
                break;
        }
        moveString += board.index2Algebraic(Move.getFromIndex(move));
        if (Move.isCapture(move))
            moveString += "x";
        else
            moveString += "-";
        moveString += board.index2Algebraic(Move.getToIndex(move));

        switch (Move.getMoveType(move)) {
            case Move.TYPE_EN_PASSANT:
                moveString += " e.p.";
                break;
            case Move.TYPE_PROMOTION_BISHOP:
                moveString += "=B";
                break;
            case Move.TYPE_PROMOTION_KNIGHT:
                moveString += "=N";
                break;
            case Move.TYPE_PROMOTION_ROOK:
                moveString += "=R";
                break;
            case Move.TYPE_PROMOTION_QUEEN:
                moveString += "=Q";
                break;
        }
        return moveString;
    }

    //For UCI
    public static String toString (int move, Chessboard board){
        if(move ==0) return "none";
        String moveString="";
        moveString+=board.index2Algebraic(Move.getFromIndex(move));
        moveString+=board.index2Algebraic(Move.getToIndex(move));

        if(Move.isPromotion(move))moveString+=Move.getPromotionPiece(move);

        return moveString;
    }



}