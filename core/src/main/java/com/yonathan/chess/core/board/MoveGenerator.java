package com.yonathan.chess.core.board;

/**
 * Created by Yonathan on 02/03/2015.
 */
public interface MoveGenerator {

    public int getAllMoves(int[] moves);

    public int getAllLegalMoves(int[] moves);

    public boolean legalMovesAvailable();

    public int genCaptures(int[] captures);

    public int generateCaptures (int[] moves, int startIndex, int ttMove);

    public int generateNonCaptures (int[] moves, int startIndex, int ttMove);


    public int sEE(int move);

    public boolean makeMove(int move);

    public void makeNullMove();

    public void unmakeMove();

    public void unmakeMove(int moveNumber);

    //Maximum moves per position and max game length.
    public static final int MAX_GAME_LENGTH = 1024; // Maximum number of half-moves, if 50-move rule is obeyed.
    public static final int MAX_MOVES = 256;

    //Game State
    public boolean isCheck();

    public boolean isWhiteToMove();

    //Pieces
    public char getPieceAt(long square);

    public long getEPSquare();

}
