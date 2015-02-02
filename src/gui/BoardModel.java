package gui;

import utilities.BitboardUtilsAC;
import board.Board;
import move.MoveAC;
import movegen.MoveGeneratorAC;
import search.PVS;
import search.Search;

import java.util.Observable;

/**
 * Created by Yonathan on 29/01/2015.
 */
public class BoardModel extends Observable{
    private final BoardView view;
    private final Board b;
    private final MoveGeneratorAC moveGenerator;
    private final Search search;

    public BoardModel(Board b, BoardView view) {
        this.b=b;
        this.view=view;
        moveGenerator = MoveGeneratorAC.getInstance();
        search = PVS.getInstance();
    }
    public void makeMove(int move) {
        if (b.moveNumber % 2 == 0)
            System.out.println(" " + BitboardUtilsAC.getSAN(b, move));
        else
            System.out.print( ((b.moveNumber + 1) / 2) + ". " + BitboardUtilsAC.getSAN(b, move));

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
        int num_moves = moveGenerator.getAllMoves(b, moves);
        for (int i = 0; i < num_moves; i++) {
            if (s.equals(BitboardUtilsAC.intToAlgebraicLoc(MoveAC.getFromIndex(moves[i])) + "-"
                    + BitboardUtilsAC.intToAlgebraicLoc(MoveAC.getToIndex(moves[i])))) {
                makeMove(moves[i]);

            }
        }
    }


    public char get(int loc) {
        return b.getPieceAt(loc);
    }
}
