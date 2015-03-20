package com.chess1800.chess.application.gui;

import com.chess1800.chess.board.Chessboard;
import com.chess1800.chess.search.Search;

import java.util.Observable;

/**
 * Created by Yonathan on 18/03/2015.
 */
public class Chess1800Model extends Observable {
    private final Chessboard b;
    private final Search engine1;
    private final Search engine2;
    private boolean searching;
    private int moveTime;

    public Chess1800Model(Chessboard b, Search engine1, Search engine2) {
        this.b = b;
        this.engine1 = engine1;
        this.engine2 = engine2;
    }

    private boolean isMoveLegal(int move){
        return b.isMoveLegal(move);

    }
    public int getMoveFromIndices(int from, int to){
        String move=b.index2Algebraic(from)+b.index2Algebraic(to);
        return b.getMoveFromString(move, false);
    }

    public boolean userMove(int move) {
        if(isMoveLegal(move)) {
            if(b.makeMove(move)){
                setChanged();
                return true;
            }
            return false;
        }{
            return false;
        }
    }

    public String getFEN(){
        return b.getFEN();
    }

    public int eval(){
        return b.eval();
    }

    public int isEndOfGame(){
        return -1;
    }

    public int getMoveNumber() {
        return b.getMoveNumber();
    }

    public boolean isWhiteToMove() {
        return b.isWhiteToMove();
    }

    public void engine1Move() {

    }

    public void engine2Move() {

    }

    public void stop() {

    }

    public void startPosition() {
        b.initialize();
    }

    public void unmakeMove() {
        b.unmakeMove();
    }

    public void initializeFromFEN(String fen) {
        b.initializeFromFEN(fen);
    }

    public boolean isSearching() {
        return engine1.isSearching() | engine2.isSearching();
    }

    public void setMoveTime(int time){
        moveTime = time;
    }
}
