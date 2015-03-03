package gui;

import board.Chessboard;
import definitions.Definitions;
import board.Bitboard;
import move.Move;
import board.AbstractAbstractBitboardMoveGenerator;
import search.PVS;
import search.PVSHard;
import search.PVSSoft;
import search.Search;
import utilities.SANUtils;

import java.util.Observable;

/**
 * Created by Yonathan on 29/01/2015.
 * Based on Ulysse Carion's Godot. Source @ https://github.com/ucarion
 */
public class BoardModel extends Observable implements Definitions{
    private final BoardView view;
    private final Bitboard b;
    private final Search searchSoft;
    private final Search searchHard;
    private final SANUtils sanUtils;

    public BoardModel(Bitboard b, BoardView view) {
        this.b=b;
        this.view=view;
        searchSoft = new PVSSoft();
        searchHard = new PVSHard();
        sanUtils = SANUtils.getInstance();
    }
    public void makeMove(int move) {
        if (b.getMoveNumber() % 2 == 0)
            System.out.println(" " + sanUtils.getSAN(b, move));
        else
            System.out.print( ((b.getMoveNumber() + 1) / 2) + ". " + sanUtils.getSAN(b, move));

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

        int[] moves = new int[Bitboard.MAX_MOVES];
        int num_moves = b.getAllLegalMoves(moves);
        for (int i = 0; i < num_moves; i++) {
            if (s.equals(sanUtils.intToAlgebraicLoc(Move.getFromIndex(moves[i])) + "-"
                    + sanUtils.intToAlgebraicLoc(Move.getToIndex(moves[i])))) {
                makeMove(moves[i]);
                break;
            }
        }
    }

    public void makeEngineMove() {
        int move = searchSoft.findBestMove(b,6,0,0,0);
        makeMove(move);
    }

    public void makeEngineMove2(){
        int move = searchHard.findBestMove(b,0,0,0,0);
        makeMove(move);
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
        return b.isDraw() != Chessboard.NO_DRAW;
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
