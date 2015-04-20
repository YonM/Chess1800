package com.chess1800.application.gui;

import com.yonathan.chess.core.board.Chessboard;
import com.yonathan.chess.core.search.Search;
import com.yonathan.chess.core.search.SearchObserver;


/**
 * Created by Yonathan on 18/03/2015.
 */
public class Chess1800Model {
    private final Search engine1;


    public Chess1800Model(Search engine1) {
        this.engine1 = engine1;
    }

    private boolean isMoveLegal(int move){
        return engine1.getBoard().isMoveLegal(move);

    }
    public int getMoveFromIndices(int from, int to){
        String move= engine1.getBoard().index2Algebraic(from)+engine1.getBoard().index2Algebraic(to);
        return engine1.getBoard().getMoveFromString(move, false);
    }

    public boolean userMove(int move) {
        if(isMoveLegal(move)) {
            if(engine1.getBoard().makeMove(move)){
                return true;
            }
            return false;
        }{
            return false;
        }
    }

    public void makeEngineMove(int move){
        engine1.getBoard().makeMove(move);
    }

    public String getFEN(){
        return engine1.getBoard().getFEN();
    }

    public int eval(){
        return engine1.getBoard().eval();
    }

    public int isEndOfGame(){
        return engine1.getBoard().isEndOfGame();
    }

    public int getMoveNumber() {
        return engine1.getBoard().getMoveNumber();
    }

    public boolean isWhiteToMove() {
        return engine1.getBoard().isWhiteToMove();
    }

    public void engine1Go() {
        engine1.go();
    }


    public void stop(){
        stopEngine1();
    }

    public void stopEngine1() {
        engine1.stop();
    }

    public void setSearchObserver(SearchObserver observer){
        engine1.setObserver(observer);
    }

    public void startPosition() {
        engine1.getBoard().initialize();
    }

    public void unmakeMove() {
        engine1.getBoard().unmakeMove();
    }

    public void initializeFromFEN(String fen) {
        engine1.getBoard().initializeFromFEN(fen);
    }

    public boolean isSearching() {
        return engine1.isSearching();
    }

    public void setMoveTime(int time){
        engine1.resetMoveTime(time);
    }
    public Chessboard getBoard(){
        return engine1.getBoard();
    }
}
