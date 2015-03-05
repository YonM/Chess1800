package board;

/**
 * Created by Yonathan on 02/03/2015.
 * Interface for a chess board.
 */
public interface Chessboard extends Evaluator {

    public static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";



    //Move related
    public boolean makeMove(int move);
    public void makeNullMove();
    public void unmakeMove();
    public int getMoveFromString(String move, boolean legalityCheck);

    //Position related
    public boolean initializeFromFEN(String fen);
    public long getKey();
    public char getPieceAt(int loc);




    //Game state related
    public boolean isEndOfGame();
    public boolean isCheck();
    public boolean isWhiteToMove();
    public boolean isCheckMate();
    public int isDraw();
    public int getFiftyMove();
    public int getMoveNumber();
    public int movingSidePieceMaterial();


    //Move Notation
    public String getSAN(int move);





}
