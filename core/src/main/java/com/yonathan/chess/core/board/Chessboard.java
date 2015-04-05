package com.yonathan.chess.core.board;

/**
 * Created by Yonathan on 02/03/2015.
 * Interface for a chess board.
 */
public interface Chessboard extends Evaluator {

    public static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public static final int WHITE_WIN = 100;
    public static final int BLACK_WIN = -100;



    //Move related
    public int getMoveFromString(String move, boolean legalityCheck);
    public boolean isMoveLegal(int move);



    public int getLastMove();


    //Position related
    public boolean initializeFromFEN(String fen);
    public void initialize();
    public String getFEN();
    public long getKey();





    //Game state related
    public int isEndOfGame();



    public int getFiftyMove();
    public int getMoveNumber();
    public int movingSideMaterial();


    //Move Notation
    public String getSAN(int move);


}