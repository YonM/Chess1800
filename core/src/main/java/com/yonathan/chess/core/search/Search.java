package com.yonathan.chess.core.search;

import com.yonathan.chess.core.board.Chessboard;
import com.yonathan.chess.core.board.Evaluator;

/**
 * Created by Yonathan on 02/02/2015.
 * Simple Search interface.
 */
public interface Search extends Runnable, SearchInfo {

    public void findBestMove() throws SearchRunException;

    public Chessboard getBoard();

    public void go();

    public void stop();

    public long getGlobalBestMoveTime();
    public int getGlobalBestMove();

    //Null move + Late Move Reduction related
    public static final int LATEMOVE_THRESHOLD = 4;
    public static final int LATEMOVE_DEPTH_THRESHOLD = 3;
    public static final int NULLMOVE = 0;
    public static final int NULLMOVE_REDUCTION = 4;
    public static final int NULLMOVE_THRESHOLD = Evaluator.KNIGHT_VALUE - 1; //Only do null move when material value(excluding pawn & king)
    // is above the value of a Knight - 1.

    //Maximum moves per position and max game length.
    public static final int MAX_GAME_LENGTH = 1024; // Maximum number of half-moves, if 50-move rule is obeyed.

    public static final int MAX_PLY = 64;

    public void setObserver(SearchObserver observer);

    public boolean isSearching();

}
