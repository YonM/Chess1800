package com.chess1800.chess.application.gui;

import com.chess1800.chess.board.Chessboard;

import java.util.Observable;

/**
 * Created by Yonathan on 18/03/2015.
 */
public class Chess1800Model extends Observable {
    private final Chessboard b;

    public Chess1800Model(Chessboard b) {
        this.b = b;
    }

    public boolean isMoveLegal(int move){
        return b.isMoveLegal(move);

    }
    public int getMoveFromIndices(int from, int to){
        String move=b.in;
        return b.getMoveFromString(move, false);
    }
}
