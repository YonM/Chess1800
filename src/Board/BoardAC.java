package Board;

import Fen.FENValidator;

/**
 * Created by Yonathan on 03/12/2014.
 * Class for the board of the chess game.
 */
public class BoardAC {

    public static final int MAX_GAME_LENGTH = 1024;

    public static final int MAX_MOVES = 255;

    public static final String FEN_START_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public long whitePawns;
    public long whiteKnights;
    public long whiteBishops;
    public long whiteRooks;
    public long whiteQueens;
    public long whiteKing;

    public long blackPawns;
    public long blackKnights;
    public long blackBishops;
    public long blackRooks;
    public long blackQueens;
    public long blackKing;

    public long whitePieces;
    public long blackPieces;
    public long allPieces;


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

    public BoardAC() {
    }

    public BoardAC(String fen) {
        this();
        loadFEN(fen);
    }

    /*
    * Setting up the board, using a FEN string. First the string is validated.
    * */
    public void loadFEN(String fen) {
        // if valid FEN
        if (FENValidator.isValidFEN(fen)) {

        } else {
            //FEN IS NOT VALID
        }
    }

    public void updateAggregateBoards() {
        whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;

        blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;

        allPieces = whitePieces | blackPieces;
    }
}
