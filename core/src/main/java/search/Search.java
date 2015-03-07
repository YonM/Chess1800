package search;

import board.Bitboard;
import board.Chessboard;
import board.Evaluator;

/**
 * Created by Yonathan on 02/02/2015.
 * Simple Search interface.
 */
public interface Search {

    public int findBestMove(Chessboard b, int depth, int timeLeft, int increment, int moveTime);

    public long getBestMoveTime();

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

}
