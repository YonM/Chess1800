package com.chess1800.chess.application.gui;

import com.chess1800.chess.board.Chessboard;
import com.chess1800.chess.board.Board;
import com.chess1800.chess.board.MoveGenerator;
import com.chess1800.chess.search.AI2;
import com.chess1800.chess.search.AI1;
import com.chess1800.chess.search.Search;
import com.chess1800.chess.move.Move;

import java.util.Observable;

/**
 * Created by Yonathan on 29/01/2015.
 * Based on Ulysse Carion's Godot. Source @ https://github.com/ucarion
 */
public class BoardModel extends Observable {
    private final BoardView view;
    private final Chessboard b;
    private final Search searchSoft;
    private final Search searchHard;

    public BoardModel(Chessboard b, BoardView view) {
        this.b=b;
        this.view=view;
        searchSoft = new AI1(this.b);
        searchHard = new AI2(this.b);
    }
    public void makeMove(int move) {
        if (b.getMoveNumber() % 2 == 0)
            System.out.println(" " + b.getSAN(move));
        else
            System.out.print( ((b.getMoveNumber() + 1) / 2) + ". " + b.getSAN(move));

        int from = Move.getFromIndex(move);
        int to = Move.getToIndex(move);
        b.makeMove(move);
        view.setLastMove(from % 8, 7 - from / 8, to % 8, 7 - to / 8);
        //	System.out.println(b);
        setChanged();
        notifyObservers();
    }

    public void makeMove(int x1, int x2, int y1, int y2) {
        String s =
                (char) (x1 + 'a') + "" + (char) ('8' - y1) + "-" + (char) (x2 + 'a')
                        + (char) ('8' - y2);
        if(!s.equals("0")) {
            int[] moves = new int[MoveGenerator.MAX_MOVES];
            int num_moves = b.getAllLegalMoves(moves);
            for (int i = 0; i < num_moves; i++) {
                if (s.equals(Board.intToAlgebraicLoc(Move.getFromIndex(moves[i])) + "-"
                        + Board.intToAlgebraicLoc(Move.getToIndex(moves[i])))) {
                    makeMove(moves[i]);
                    break;
                }
            }
        }
    }

    public void makeEngineMove(int engine_time) {
        //int move = searchSoft.findBestMove(b,6,engine_time,10000,0);
        //makeMove(move);
    }

    public void makeEngineMove2(int engine_time){
        //int move = searchHard.findBestMove(b,6,engine_time,10000,0);
        //makeMove(move);
    }

    public void unmakeMove() {
        if (b.getMoveNumber() > 0) {
            b.unmakeMove();
            b.unmakeMove();
            view.setLastMove(0, 0, 0, 0);
            setChanged();
            notifyObservers();
        }
    }

    public boolean whiteWins() {
        return b.isCheckMate() && !b.isWhiteToMove();
    }

    public boolean blackWins() {
        return b.isCheckMate() && b.isWhiteToMove();
    }

    public boolean isDraw() {
        return b.isDraw() != Chessboard.NOT_ENDED;
    }

    public void displayConclusionInfo() {
        if (b.getMoveNumber() % 2 == 0)
            System.out.print(b.getMoveNumber() / 2 + ". ");
        else
            System.out.print(" ");

        if (whiteWins())
            System.out.println("1-0");
        if (blackWins())
            System.out.println("0-1");
        else
            System.out.println(".5-.5");
    }

    public char get(int loc) {
        return b.getPieceAt(loc);
    }
}
