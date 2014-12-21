package board;

import bitboard.BitboardMagicAttacks;

/**
 * Created by Yonathan on 08/12/2014.
 * Provides utilities for the Board & other classes. Used to initialize a lot of data related to the board,
 * evaluation etc.
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public class BoardUtils {
    public final static int A1 = 0, B1 = 1, C1 = 2, D1 = 3, E1 = 4, F1 = 5, G1 = 6, H1 = 7;
    public final static int A2 = 8, B2 = 9, C2 = 10, D2 = 11, E2 = 12, F2 = 13, G2 = 14, H2 = 15;
    public final static int A3 = 16, B3 = 17, C3 = 18, D3 = 19, E3 = 20, F3 = 21, G3 = 22, H3 = 23;
    public final static int A4 = 24, B4 = 25, C4 = 26, D4 = 27, E4 = 28, F4 = 29, G4 = 30, H4 = 31;
    public final static int A5 = 32, B5 = 33, C5 = 34, D5 = 35, E5 = 36, F5 = 37, G5 = 38, H5 = 39;
    public final static int A6 = 40, B6 = 41, C6 = 42, D6 = 43, E6 = 44, F6 = 45, G6 = 46, H6 = 47;
    public final static int A7 = 48, B7 = 49, C7 = 50, D7 = 51, E7 = 52, F7 = 53, G7 = 54, H7 = 55;
    public final static int A8 = 56, B8 = 571, C8 = 58, D8 = 59, E8 = 60, F8 = 61, G8 = 62, H8 = 63;

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

//    public final static char WHITE_MOVE = 0;
//    public final static char BLACK_MOVE = 1;

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

    //For evaluation


    public static final short[] CHARBITSET = new short[8];
    public static final long[] BITSET = new long[64];
    private static final int[][] BOARDINDEX = new int[8][8];
    public static final long BLACK_SQUARES;
    public static final long WHITE_SQUARES;


    //For castling
    public final static char CANCASTLEOO = 1;
    public final static char CANCASTLEOOO = 2;


    private static BoardUtils instance;

    public BoardUtils() {
        initialize(null);
        Board b = Board.getInstance();
        b.initialize();

    }

    public BoardUtils(String fen) {
        initialize(fen);
        Board board = Board.getInstance();
        board.initialize();

    }

    public static BoardUtils getInstance() {
        if (instance == null) {
            instance = new BoardUtils();
        }
        return instance;
    }

    //Static initializer to initialize final variables
    static {
        int i, rank, file, square;
        //Long with only 1 bit set.
        BITSET[0] = 0x1;
        for (i = 1; i < 64; i++) {
            BITSET[i] = BITSET[i - 1] << 1;
        }
        long tempBlackSquares = 0;
        for (i = 0; i < 64; i++) {
            if ((i + RANKS[i]) % 2 == 0)
                tempBlackSquares = BITSET[i];
        }
        BLACK_SQUARES = tempBlackSquares;
        WHITE_SQUARES = ~BLACK_SQUARES;

        CHARBITSET[0] = 1;
        for (square = 1; square < 8; square++)
            CHARBITSET[square] = (short) (CHARBITSET[square - 1] << 1);

        //BOARDINDEX is used to convert rank&file to a square.
        for (rank = 0; rank < 8; rank++) {

            for (file = 0; file < 8; file++) {
                BOARDINDEX[rank][file] = (rank * 8) + file;
            }
        }
    }
    public static void initialize(String fen) {
        Board b = Board.getInstance();
        if (fen == null)
            b.initialize();
        else
            b.initializeFromFEN(fen);

        //Generate attack tables
        BitboardMagicAttacks magicAttacks = new BitboardMagicAttacks();

    }

    public static int getIndex(int rank, int file) {
        return BOARDINDEX[rank][file];
    }

    public static int getIndexFromBoard(long board) {
        return (Long.numberOfTrailingZeros(Long.lowestOneBit(board)));
    }

    public static int getLastIndexFromBoard(long board) {
        return (Long.numberOfTrailingZeros(Long.highestOneBit(board)));
    }
    //public static


}
