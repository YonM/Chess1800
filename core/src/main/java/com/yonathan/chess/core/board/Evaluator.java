package com.yonathan.chess.core.board;

/**
 * Created by Yonathan on 02/03/2015.
 */
public interface Evaluator extends MoveGenerator {

    public int eval();

    //Game state related
    public boolean isCheckMate();

    public int isDraw();

    //Draw reason
    public static final int NOT_ENDED = -1;
    public static final int DRAW_BY_STALEMATE = 1;
    public static final int DRAW_BY_MATERIAL = 2;
    public static final int DRAW_BY_FIFTYMOVE = 3;
    public static final int DRAW_BY_REP = 4;

    //Material value for evaluation
    public static final int PAWN_VALUE = 100;
    public static final int KNIGHT_VALUE = 325;
    public static final int BISHOP_VALUE = 325;
    public static final int ROOK_VALUE = 500;
    public static final int QUEEN_VALUE = 975;
    public static final int KING_VALUE = 999999;
    public static final int CHECKMATE = KING_VALUE;

    public static final int DRAWSCORE = 0;
    public static final int INFINITY = KING_VALUE + 1;
}
