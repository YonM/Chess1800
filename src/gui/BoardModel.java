package gui;

import definitions.Definitions;
import search.MTDF;
import board.Board;
import move.MoveAC;
import movegen.MoveGeneratorAC;
import search.PVS;
import search.Search;
import utilities.SANUtils;

import java.util.Observable;

/**
 * Created by Yonathan on 29/01/2015.
 * Based on Ulysse Carion's Godot. Source @ https://github.com/ucarion
 */
public class BoardModel extends Observable implements Definitions{
    private final BoardView view;
    private final Board b;
    private final MoveGeneratorAC moveGenerator;
    private final Search search;
    private final Search search2;
    private final SANUtils sanUtils;

    public BoardModel(Board b, BoardView view) {
        this.b=b;
        this.view=view;
        moveGenerator = MoveGeneratorAC.getInstance();
        search = PVS.getInstance();
        search2 = MTDF.getInstance();
        sanUtils = SANUtils.getInstance();
    }
    public void makeMove(int move) {
        if (b.moveNumber % 2 == 0)
            System.out.println(" " + sanUtils.getSAN(b, move));
        else
            System.out.print( ((b.moveNumber + 1) / 2) + ". " + sanUtils.getSAN(b, move));

        int from = MoveAC.getFromIndex(move);
        int to = MoveAC.getToIndex(move);
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

        int[] moves = new int[Board.MAX_MOVES];
        int num_moves = moveGenerator.getAllLegalMoves(b, moves);
        for (int i = 0; i < num_moves; i++) {
            if (s.equals(sanUtils.intToAlgebraicLoc(MoveAC.getFromIndex(moves[i])) + "-"
                    + sanUtils.intToAlgebraicLoc(MoveAC.getToIndex(moves[i])))) {
                makeMove(moves[i]);
                break;
            }
        }
    }

    public void makeEngineMove() {
        int move = search.findBestMove(b,0,0,0,0);
        makeMove(move);
    }

    public void makeEngineMove2(){
        int move = search2.findBestMove(b,0,0,0,0);
        makeMove(move);
    }

    public void unmakeMove() {
        if (b.moveNumber > 0) {
            b.unmakeMove();
            b.unmakeMove();
            view.setLastMove(0, 0, 0, 0);
            setChanged();
            notifyObservers();
        }
    }

    public boolean whiteWins() {
        return b.isCheckMate() && !b.whiteToMove;
    }

    public boolean blackWins() {
        return b.isCheckMate() && b.whiteToMove;
    }

    public boolean isDraw() {
        return b.isDraw() != NO_DRAW;
    }

    public void displayConclusionInfo() {
        if (b.moveNumber % 2 == 0)
            System.out.print(b.moveNumber / 2 + ". ");
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
