package com.yonathan.chess.core.board;

import com.yonathan.chess.core.move.Move;
import com.yonathan.chess.core.moveOrdering.MoveHistoryInfo;

/**
 * Created by Yonathan on 31/03/2015.
 */
public class MoveSorter {

    private int goodCaptureIndex;
    private int equalCaptureIndex;
    private int badCaptureIndex;
    private int nonCaptureIndex;

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

    public MoveSorter(MoveHistoryInfo moveHistoryInfo) {
        this.moveHistoryInfo = moveHistoryInfo;
    }


    private void initMoveGen() {
        goodCaptureIndex = 0;
        badCaptureIndex = 0;
        equalCaptureIndex = 0;
        nonCaptureIndex = 0;
    }

    public int next() {
        return Move.EMPTY;
    }
}
