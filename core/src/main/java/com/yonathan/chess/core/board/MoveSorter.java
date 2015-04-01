package com.yonathan.chess.core.board;

import com.yonathan.chess.core.move.Move;
import com.yonathan.chess.core.moveOrdering.MoveHistoryInfo;

/**
 * Created by Yonathan on 31/03/2015.
 * Selects the next best move according to the phase of move generation. Based on Alberto Ruibal's Carballo https://github.com/albertoruibal/carballo
 */
public class MoveSorter extends AbstractBitboardMagicAttacks{

    private int goodCaptureIndex;
    private int equalCaptureIndex;
    private int badCaptureIndex;
    private int nonCaptureIndex;

    public final static int GENERATE_ALL = 0;
    public final static int GENERATE_CAPTURES_PROMOS = 1;

    private int ttMove;
    private int movesToGenerate;

    // Move generation phases
    //
    public final static int PHASE_TT = 0;
    public final static int PHASE_GEN_CAPTURES = 1;
    public final static int PHASE_GOOD_CAPTURES_AND_PROMOS = 2;
    public final static int PHASE_EQUAL_CAPTURES = 3;
    public final static int PHASE_GEN_NON_CAPTURES = 4;
    public final static int PHASE_NON_CAPTURES = 5;
    public final static int PHASE_BAD_CAPTURES = 6;
    public final static int PHASE_END = 7;
    private int phase;

    private static final int[] VICTIM_PIECE_VALUES = {0, 100, 325, 330, 500, 975, 10000};
    private static final int[] AGGRESSOR_PIECE_VALUES = {0, 10, 32, 33, 50, 97, 99};
    private static final int SCORE_PROMOTION_QUEEN = 975;
    private static final int SCORE_UNDERPROMOTION = Integer.MIN_VALUE + 1;
    private static final int SCORE_LOWEST = Integer.MIN_VALUE;

    public final static int SEE_NOT_CALCULATED = Short.MAX_VALUE;


    private int[] goodCaptures = new int[256]; // Stores captures and queen promotions
    private int[] goodCapturesSee = new int[256];
    private int[] goodCapturesScores = new int[256];
    private int[] badCaptures = new int[256]; // Stores captures and queen promotions
    private int[] badCapturesScores = new int[256];
    private int[] equalCaptures = new int[256]; // Stores captures and queen promotions
    private int[] equalCapturesSee = new int[256];
    private int[] equalCapturesScores = new int[256];
    private int[] nonCaptures = new int[256]; // Stores non captures and underpromotions
    private int[] nonCapturesSee = new int[256];
    private int[] nonCapturesScores = new int[256];
    private Chessboard board;


    private MoveHistoryInfo moveHistoryInfo;
    private int lastMoveSEE;
    private int move;


    public MoveSorter(MoveHistoryInfo moveHistoryInfo) {
        this.moveHistoryInfo = moveHistoryInfo;
    }


    public void genMoves(int ttMove) {
        genMoves(ttMove, GENERATE_ALL);
    }

    public void genMoves(int ttMove, int movesToGenerate) {
        this.ttMove = ttMove;
        this.movesToGenerate = movesToGenerate;

        phase = PHASE_TT;

    }

    private void initMoveGen() {
        goodCaptureIndex = 0;
        badCaptureIndex = 0;
        equalCaptureIndex = 0;
        nonCaptureIndex = 0;
    }

    public int next() {
        switch (phase){
            case PHASE_TT:
                phase++;
                if(ttMove != Move.EMPTY){
                    lastMoveSEE = Move.isCapture(ttMove) || Move.isPromotion(ttMove) ? board.sEE(ttMove) : 0;
                    if(movesToGenerate == GENERATE_ALL || Move.isPromotion(ttMove) || (movesToGenerate == GENERATE_CAPTURES_PROMOS && Move.isCapture(ttMove) && lastMoveSEE >= 0))
                        return ttMove;
                }

            case PHASE_GEN_CAPTURES:
                initMoveGen();
                phase++;

            case PHASE_GOOD_CAPTURES_AND_PROMOS:
                move = selectMove(goodCaptureIndex, goodCaptures, goodCapturesScores, goodCapturesSee);

        }
        return Move.EMPTY;
    }

    private int selectMove(int arrayLength, int[] arrayMoves, int[] arrayScores, int[] arraySee) {
        return 0;
    }
}
