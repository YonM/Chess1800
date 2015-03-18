package com.chess1800.chess.board;

/**
 * Created by Yonathan on 02/03/2015.
 * Interface for a chess board.
 */
public interface Chessboard extends Evaluator {

    public static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";



    //Move related
    public int getMoveFromString(String move, boolean legalityCheck);

    //Position related
    public boolean initializeFromFEN(String fen);
    public long getKey();
    public char getPieceAt(int loc);




    //Game state related
    public boolean isEndOfGame();
    public boolean isCheck();
    public boolean isWhiteToMove();
    public int getFiftyMove();
    public int getMoveNumber();
    public int movingSideMaterial();


    //Move Notation
    public String getSAN(int move);





}
