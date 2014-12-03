package Board;

import Fen.FENValidator;

/**
 * Created by Yonathan on 03/12/2014.
 * Class for the board of the chess game.
 */
public class Board {

    public static final int MAX_GAME_LENGTH = 1024;

    public static final int MAX_MOVES = 255;


    public long whitePawns;
    public long whiteKnights;
    public long whiteBishops;
    public long whiteRooks;
    public long whiteQueen;
    public long whiteKing;

    public long blackPawns;
    public long blackKnights;
    public long blackBishops;
    public long blackRooks;
    public long blackQueen;
    public long blackKing;

    public long whitePieces;
    public long blackPieces;

    public long occupiedSquares;
    public int[] square;

    public boolean whiteToMove;
    public int epSquare;
    public int fiftyMoveRule;

    public boolean whiteCastleK;
    public boolean whiteCastleQ;
    public boolean whiteCastled;
    public boolean blackCastleK;
    public boolean blackCastleQ;
    public boolean blackCastled;

    public boolean viewRotated; //May not use, for viewing the board.

    public Board() {
    }

    public Board(String fen) {
        this();
        loadFEN(fen);
    }

    /*
    * Setting up the board, using a FEN string. First the string is validated.*/
    public void loadFEN(String fen) {
        // if valid FEN
        if (FENValidator.isValidFEN(fen)) {

        } else {
            //FEN IS NOT VALID
        }
    }
}
