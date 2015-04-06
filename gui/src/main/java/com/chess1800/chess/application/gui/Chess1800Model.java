package com.chess1800.chess.application.gui;



import com.yonathan.chess.core.board.Chessboard;
import com.yonathan.chess.core.search.Search;
import com.yonathan.chess.core.search.SearchObserver;


/**
 * Created by Yonathan on 18/03/2015.
 */
public class Chess1800Model {
    private final Chessboard b;
    private final Search engine1;
    private final Search engine2;
    public Chess1800Model(Chessboard board, Search engine1, Search engine2) {
        b= board;
        this.engine1 = engine1;
        this.engine2 = engine2;
    }
    private boolean isMoveLegal(int move){
        return b.isMoveLegal(move);
    }
    public int getMoveFromIndices(int from, int to){
        String move= b.index2Algebraic(from)+b.index2Algebraic(to);
        System.out.println("String User Move: " + move);
        return b.getMoveFromString(move, false);
    }
    public boolean userMove(int move) {
        if(isMoveLegal(move)) {
            if(b.makeMove(move)){
                System.out.println("move legal");
// System.out.println(Long.toBinaryString(b.getAllPieces()));
                return true;
            }
            System.out.println("move illegal (discrepancy): " +move);
            return false;
        }{
            System.out.println("move illegal: " + move);
            return false;
        }
    }

    public void makeEngineMove(int move){
        b.makeMove(move);
    }
    public String getFEN(){
        return b.getFEN();
    }
    public int eval(){
        return b.eval();
    }
    public int isEndOfGame(){
        return b.isEndOfGame();
    }
    public int getMoveNumber() {
        return b.getMoveNumber();
    }
    public boolean isWhiteToMove() {
        return b.isWhiteToMove();
    }
    public void engine1Go() {
        engine1.go();
    }
    public void engine2Go() {
        engine2.go();
    }
    public void stop(){
        stopEngine1();
        stopEngine2();
    }
    public void stopEngine1() {
        engine1.stop();
    }
    public void stopEngine2() {
        engine2.stop();
    }
    public void setSearchObserver(SearchObserver observer){
        engine1.setObserver(observer);
        engine2.setObserver(observer);
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
        engine1.resetMoveTime(time);
        engine2.resetMoveTime(time);
    }
    public Chessboard getBoard(){
        return b;
    }
}