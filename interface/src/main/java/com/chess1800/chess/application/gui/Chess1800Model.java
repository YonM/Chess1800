package com.chess1800.chess.application.gui;

import com.chess1800.chess.board.Bitboard;
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

    public Chess1800Model(Search engine1, Search engine2) {
        b= new Bitboard();
        b.initialize();
        moveTime=1000;
        this.engine1 = engine1;
        this.engine2 = engine2;
    }

    private boolean isMoveLegal(int move){
        return b.isMoveLegal(move);

    }
    public int getMoveFromIndices(int from, int to){
        String move=b.index2Algebraic(from^7)+b.index2Algebraic(to^7);
        return b.getMoveFromString(move, false);
    }

    public boolean userMove(int move) {
        if(isMoveLegal(move)) {
            if(b.makeMove(move)){
                System.out.println("move legal");
                System.out.println(Long.toBinaryString(b.getAllPieces()));
                setChanged();
                return true;
            }
            System.out.println("move legality discrepancy (move not legal)");
            return false;
        }{
            System.out.println("move illegal");
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
        return b.isEndOfGame();
    }

    public int getMoveNumber() {
        return b.getMoveNumber();
    }

    public boolean isWhiteToMove() {
        return b.isWhiteToMove();
    }

    public void engine1Move() {
            int move=engine1.findBestMove(b,0,0,0,moveTime);
            userMove(move);
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
