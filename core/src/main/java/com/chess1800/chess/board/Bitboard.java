package com.chess1800.chess.board;

/**
 * Created by Yonathan on 01/04/2015.
 */
public interface Bitboard extends Chessboard{


    public long getWhitePawns();
    public long getBlackPawns();
    public long getWhiteKnights();
    public long getBlackKnights();
    public long getWhiteBishops();
    public long getBlackBishops();
    public long getWhiteRooks();
    public long getBlackRooks();
    public long getWhiteQueens();
    public long getBlackQueens();
    public long getWhiteKing();
    public long getBlackKing();

}
