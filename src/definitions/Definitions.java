package definitions;

import move.MoveAC;

/**
 * Created by Yonathan on 24/12/2014.
 * Interface containing constants used by multiple classes.
 */
public interface Definitions {
    public static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public static final int A1 = 0, B1 = 1, C1 = 2, D1 = 3, E1 = 4, F1 = 5, G1 = 6, H1 = 7;
    public static final int A2 = 8, B2 = 9, C2 = 10, D2 = 11, E2 = 12, F2 = 13, G2 = 14, H2 = 15;
    public static final int A3 = 16, B3 = 17, C3 = 18, D3 = 19, E3 = 20, F3 = 21, G3 = 22, H3 = 23;
    public static final int A4 = 24, B4 = 25, C4 = 26, D4 = 27, E4 = 28, F4 = 29, G4 = 30, H4 = 31;
    public static final int A5 = 32, B5 = 33, C5 = 34, D5 = 35, E5 = 36, F5 = 37, G5 = 38, H5 = 39;
    public static final int A6 = 40, B6 = 41, C6 = 42, D6 = 43, E6 = 44, F6 = 45, G6 = 46, H6 = 47;
    public static final int A7 = 48, B7 = 49, C7 = 50, D7 = 51, E7 = 52, F7 = 53, G7 = 54, H7 = 55;
    public static final int A8 = 56, B8 = 57, C8 = 58, D8 = 59, E8 = 60, F8 = 61, G8 = 62, H8 = 63;

    //For Attack generation
//    public static final int[] RANKSHIFT = {
//            1, 1, 1, 1, 1, 1, 1, 1,
//            9, 9, 9, 9, 9, 9, 9, 9,
//            17, 17, 17, 17, 17, 17, 17, 17,
//            25, 25, 25, 25, 25, 25, 25, 25,
//            33, 33, 33, 33, 33, 33, 33, 33,
//            41, 41, 41, 41, 41, 41, 41, 41,
//            49, 49, 49, 49, 49, 49, 49, 49,
//            57, 57, 57, 57, 57, 57, 57, 57
//    };

    public static final int RANK_1 = 0;
    public static final int RANK_2 = 1;
    public static final int RANK_3 = 2;
    public static final int RANK_4 = 3;
    public static final int RANK_5 = 4;
    public static final int RANK_6 = 5;
    public static final int RANK_7 = 6;
    public static final int RANK_8 = 7;
    public static final int FILE_A = 0;
    public static final int FILE_B = 1;
    public static final int FILE_C = 2;
    public static final int FILE_D = 3;
    public static final int FILE_E = 4;
    public static final int FILE_F = 5;
    public static final int FILE_G = 6;
    public static final int FILE_H = 7;

    //Array of piece names. Capitals for whites.
    public static final char[] PIECENAMES = {' ', 'P', 'K', 'N', ' ', 'B', 'R', 'Q',
            ' ', 'p', 'k', 'n', ' ', 'b', 'r', 'q'};


    // Move pieces ordered by value
    public static final int EMPTY = 0;
    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;

    // Move Types
    public static final int TYPE_KINGSIDE_CASTLING = 1;
    public static final int TYPE_QUEENSIDE_CASTLING = 2;
    public static final int TYPE_EN_PASSANT = 3;


    //Promotions
    // promotions must be always >= TYPE_PROMOTION_QUEEN
    public static final int TYPE_PROMOTION_QUEEN = 4;
    public static final int TYPE_PROMOTION_KNIGHT = 5;
    public static final int TYPE_PROMOTION_BISHOP = 6;
    public static final int TYPE_PROMOTION_ROOK = 7;

    //Pre generated castling moves
//    public static final int WHITE_OOO_CASTLE = MoveAC.genMove(E1, C1, KING, false, MoveAC.TYPE_QUEENSIDE_CASTLING);
//    public static final int BLACK_OOO_CASTLE = MoveAC.genMove(E8, C8, KING, false, MoveAC.TYPE_QUEENSIDE_CASTLING);
//    public static final int WHITE_OO_CASTLE = MoveAC.genMove(E1, G1, KING, false, MoveAC.TYPE_KINGSIDE_CASTLING);
//    public static final int BLACK_OO_CASTLE = MoveAC.genMove(E8, G8, KING, false, MoveAC.TYPE_KINGSIDE_CASTLING);

    //Castling values
    public final int CANCASTLEOO = 1;
    public final int CANCASTLEOOO = 2;

    //Material value for evaluation
    public static final int PAWN_VALUE = 100;
    public static final int KNIGHT_VALUE = 325;
    public static final int BISHOP_VALUE = 325;
    public static final int ROOK_VALUE = 500;
    public static final int QUEEN_VALUE = 975;
    public static final int KING_VALUE = 999999;
    public static final int CHECKMATE = KING_VALUE;
    public static final int DRAWSCORE = 0;
    public static final int INFINITY = KING_VALUE +1;
    
    //For Transposition table
    public static final int HASH_EXACT = 0;
    public static final int HASH_ALPHA = 1;
    public static final int HASH_BETA = 2;

    //For SEE & Quiescence Search
    public static final int MINCAPTVAL = 1;


    //Null move + Late Move Reduction related
    public static final int LATEMOVE_THRESHOLD = 4;
    public static final int LATEMOVE_DEPTH_THRESHOLD = 3;
    public static final int NULLMOVE = 0;
    public static final int NULLMOVE_REDUCTION = 4;
    public static final int NULLMOVE_THRESHOLD = KNIGHT_VALUE - 1; //Only do null move when material value(excluding pawn & king)
    // is below the value of a Knight.

    //Maximum moves per position and max game length.
    public static final int MAX_GAME_LENGTH = 1024; // Maximum number of half-moves, if 50-move rule is obeyed.
    public static final int MAX_MOVES = 256;

    public static final int MAX_PLY = 64;



}
