package com.chess1800.chess.board;

/**
 * Created by Yonathan on 02/03/2015.
 */
public interface MoveGenerator {

    public int getAllMoves(int[] moves);

    public int getAllLegalMoves(int[] moves);

    public boolean legalMovesAvailable();

    public int genCaptures(int[] captures);

    public boolean makeMove(int move);
    public void makeNullMove();
    public void unmakeMove();

    //Maximum moves per position and max game length.
    public static final int MAX_GAME_LENGTH = 1024; // Maximum number of half-moves, if 50-move rule is obeyed.
    public static final int MAX_MOVES = 256;

    //Game State
    public boolean isCheck();
    public boolean isWhiteToMove();

    //Pieces
    public long getMyPieces();
    public long getOpponentPieces();
    public long getAllPieces();
    public int getEPIndex();

}
