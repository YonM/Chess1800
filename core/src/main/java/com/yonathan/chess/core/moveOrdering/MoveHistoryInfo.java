package com.yonathan.chess.core.moveOrdering;

import com.yonathan.chess.core.move.Move;

/**
 * Created by Yonathan on 31/03/2015.
 */
public class MoveHistoryInfo {
    private int [][] whiteHistory;
    private int [][] blackHistory;


    public MoveHistoryInfo() {
        whiteHistory = new int [64] [64];
        blackHistory = new int [64] [64];
    }

    public void clear(){
        whiteHistory = new int [64] [64];
        blackHistory = new int [64] [64];
    }

    public void betaCutOff(int move, int depth, boolean whiteToMove){
        if(whiteToMove) whiteHistory[Move.getFromIndex(move)] [Move.getToIndex(move)] += depth * depth;
        else blackHistory[Move.getFromIndex(move)] [Move.getToIndex(move)] += depth * depth;
    }

    public int getMoveScore(int move, boolean whiteToMove){
        if(whiteToMove) return whiteHistory[Move.getFromIndex(move)] [Move.getToIndex(move)];

        return blackHistory[Move.getFromIndex(move)] [Move.getToIndex(move)];
    }
}
