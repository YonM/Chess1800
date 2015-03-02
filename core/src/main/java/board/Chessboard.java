package board;

/**
 * Created by Yonathan on 02/03/2015.
 * Interface for a chess board.
 */
public interface Chessboard extends Evaluator {



    public boolean makeMove(int move);
    public void makeNullMove();
    public void unmakeMove();


    public boolean initializeFromFEN(String fen);

    public int movingSidePieceMaterial();

    public long getKey();

    public boolean isEndOfGame();
    public boolean isCheck();

    public boolean isWhiteToMove();

    public int getFiftyMove();

    public int getMoveFromString(String move, boolean legalityCheck);


}
