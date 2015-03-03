package board;

/**
 * Created by Yonathan on 02/03/2015.
 * Interface for a chess board.
 */
public interface Chessboard extends Evaluator {

    //Move related
    public boolean makeMove(int move);
    public void makeNullMove();
    public void unmakeMove();
    public int getMoveFromString(String move, boolean legalityCheck);

    //Position related
    public boolean initializeFromFEN(String fen);
    public long getKey();




    //Game state related
    public boolean isEndOfGame();
    public boolean isCheck();
    public boolean isWhiteToMove();
    public int getFiftyMove();
    public int getMoveNumber();
    public int movingSidePieceMaterial();




}
