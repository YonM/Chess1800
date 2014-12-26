package definitions;

import move.Move;

/**
 * Created by Yonathan on 24/12/2014.
 * Interface containing constants used by multiple classes.
 */
public interface Definitions {

    public final static int A1 = 0, B1 = 1, C1 = 2, D1 = 3, E1 = 4, F1 = 5, G1 = 6, H1 = 7;
    public final static int A2 = 8, B2 = 9, C2 = 10, D2 = 11, E2 = 12, F2 = 13, G2 = 14, H2 = 15;
    public final static int A3 = 16, B3 = 17, C3 = 18, D3 = 19, E3 = 20, F3 = 21, G3 = 22, H3 = 23;
    public final static int A4 = 24, B4 = 25, C4 = 26, D4 = 27, E4 = 28, F4 = 29, G4 = 30, H4 = 31;
    public final static int A5 = 32, B5 = 33, C5 = 34, D5 = 35, E5 = 36, F5 = 37, G5 = 38, H5 = 39;
    public final static int A6 = 40, B6 = 41, C6 = 42, D6 = 43, E6 = 44, F6 = 45, G6 = 46, H6 = 47;
    public final static int A7 = 48, B7 = 49, C7 = 50, D7 = 51, E7 = 52, F7 = 53, G7 = 54, H7 = 55;
    public final static int A8 = 56, B8 = 571, C8 = 58, D8 = 59, E8 = 60, F8 = 61, G8 = 62, H8 = 63;

    //For Attack generation
    public static final int[] RANKSHIFT = {
            1, 1, 1, 1, 1, 1, 1, 1,
            9, 9, 9, 9, 9, 9, 9, 9,
            17, 17, 17, 17, 17, 17, 17, 17,
            25, 25, 25, 25, 25, 25, 25, 25,
            33, 33, 33, 33, 33, 33, 33, 33,
            41, 41, 41, 41, 41, 41, 41, 41,
            49, 49, 49, 49, 49, 49, 49, 49,
            57, 57, 57, 57, 57, 57, 57, 57
    };

    //Convert index to file number
    public final static int FILES[] = {
            0, 1, 2, 3, 4, 5, 6, 7,
            0, 1, 2, 3, 4, 5, 6, 7,
            0, 1, 2, 3, 4, 5, 6, 7,
            0, 1, 2, 3, 4, 5, 6, 7,
            0, 1, 2, 3, 4, 5, 6, 7,
            0, 1, 2, 3, 4, 5, 6, 7,
            0, 1, 2, 3, 4, 5, 6, 7,
            0, 1, 2, 3, 4, 5, 6, 7
    };

    //Convert index to RANK number
    public final static int RANKS[] = {
            0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 1, 1, 1, 1, 1, 1,
            2, 2, 2, 2, 2, 2, 2, 2,
            3, 3, 3, 3, 3, 3, 3, 3,
            4, 4, 4, 4, 4, 4, 4, 4,
            5, 5, 5, 5, 5, 5, 5, 5,
            6, 6, 6, 6, 6, 6, 6, 6,
            7, 7, 7, 7, 7, 7, 7, 7
    };

    //Array of piece names. Capitals for whites.
    public final static char[] PIECENAMES = {' ', 'P', 'K', 'N', ' ', 'B', 'R', 'Q',
            ' ', 'p', 'k', 'n', ' ', 'b', 'r', 'q'};

    //Piece values for moves
    public final static int EMPTY = 0;
    public final static int WHITE_PAWN = 1;
    public final static int WHITE_KING = 2;
    public final static int WHITE_KNIGHT = 3;
    public final static int WHITE_BISHOP = 5;
    public final static int WHITE_ROOK = 6;
    public final static int WHITE_QUEEN = 7;
    public final static int BLACK_PAWN = 9;
    public final static int BLACK_KING = 10;
    public final static int BLACK_KNIGHT = 11;
    public final static int BLACK_BISHOP = 13;
    public final static int BLACK_ROOK = 14;
    public final static int BLACK_QUEEN = 15;

    //Pre generated castling moves
    public static final int WHITE_OOO_CASTLE = Move.generateMove(E1, G1, WHITE_KING, EMPTY, WHITE_KING);
    public static final int BLACK_OOO_CASTLE = Move.generateMove(E8, G8, BLACK_KING, EMPTY, BLACK_KING);
    public static final int WHITE_OO_CASTLE = Move.generateMove(E1, C1, WHITE_KING, EMPTY, WHITE_KING);
    public static final int BLACK_OO_CASTLE = Move.generateMove(E8, C8, BLACK_KING, EMPTY, BLACK_KING);

    //Castling values
    public final int CANCASTLEOO = 1;
    public final int CANCASTLEOOO = 2;

    //Material value
    public final static int PAWN_VALUE = 100;
    public final static int KNIGHT_VALUE = 325;
    public final static int BISHOP_VALUE = 325;
    public final static int ROOK_VALUE = 500;
    public final static int QUEEN_VALUE = 975;
    public final static int KING_VALUE = 999999;
    public final static int CHECKMATE = KING_VALUE;
    public final static int DRAWSCORE = 0;

    //For SEE & Quiescence Search
    public static final int OFFSET = 128;
    public static final int MINCAPTVAL = 1;
    public static final int WEST = -1;
    public static final int NORTHWEST = +7;
    public static final int NORTH = +8;
    public static final int NORTHEAST = +9;
    public static final int EAST = +1;
    public static final int SOUTHEAST = -7;
    public static final int SOUTH = -8;
    public static final int SOUTHWEST = -9;


    public final static Move nullMove = new Move(0);

    public static final int MAX_GAME_LENGTH = 1024; // Maximum number of half-moves, if 50-move rule is obeyed.
    public static final int MAX_MOVES = 256;
}
