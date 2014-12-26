package board;

import bitboard.BitboardMagicAttacks;
import definitions.Definitions;

/**
 * Created by Yonathan on 08/12/2014.
 * Provides utilities for the Board & other classes. Used to initialize a lot of data related to the board,
 * evaluation etc.
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public class BoardUtils implements Definitions {

//    public final static char WHITE_MOVE = 0;
//    public final static char BLACK_MOVE = 1;

    //For evaluation
    public static final short[] CHARBITSET = new short[8];
    public static final long[] BITSET = new long[64];
    private static final int[][] BOARDINDEX = new int[8][8];
    public static final long BLACK_SQUARES;
    public static final long WHITE_SQUARES;

    //For SEE & Quiescence Search
    public static long[] RAY_W = new long[64];
    public static long[] RAY_NW = new long[64];
    public static long[] RAY_N = new long[64];
    public static long[] RAY_NE = new long[64];
    public static long[] RAY_E = new long[64];
    public static long[] RAY_SE = new long[64];
    public static long[] RAY_S = new long[64];
    public static long[] RAY_SW = new long[64];
    public static final int[][] HEADINGS = new int[64][64];
    private static BoardUtils instance;

    public BoardUtils() {
        initialize(null);
        Board b = Board.getInstance();
    }

    public BoardUtils(String fen) {
        initialize(fen);
        Board board = Board.getInstance();
    }

    public static BoardUtils getInstance(String fen) {
        if (instance == null) {
            if (fen != null)
                instance = new BoardUtils(fen);
            else
                instance = new BoardUtils();
        }
        return instance;
    }

    //Static initializer to initialize final variables
    static {
        int i, rank, aRank, file, aFile, square;
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

        //For HEADINGS & RAYS which are used in SEE.

        for (rank = 0; rank < 8; rank++) {
            for (file = 0; file < 8; file++) {
                i = BOARDINDEX[rank][file];

                //NORTH
                for (aRank = rank + 1; aRank < 8; aRank++) {
                    HEADINGS[i][BOARDINDEX[aRank][file]] = NORTH;
                    RAY_N[i] |= BITSET[BOARDINDEX[aRank][file]];
                }
                //NORTHEAST
                for (aFile = file + 1, aRank = rank + 1; (aFile < 8) && (aRank < 8); aFile++, aRank++) {
                    HEADINGS[i][BOARDINDEX[aRank][aFile]] = NORTHEAST;
                    RAY_NE[i] |= BITSET[BOARDINDEX[aRank][aFile]];
                }
                //EAST
                for (aFile = file + 1; aFile < 8; aFile++) {
                    HEADINGS[i][BOARDINDEX[rank][aFile]] = EAST;
                    RAY_E[i] |= BITSET[BOARDINDEX[rank][aFile]];
                }
                //SOUTHEAST
                for (aFile = file + 1, aRank = rank - 1; (aFile < 8) && (aRank >= 0); aFile++, aRank--) {
                    HEADINGS[i][BOARDINDEX[aRank][aFile]] = SOUTHEAST;
                    RAY_SE[i] |= BITSET[BOARDINDEX[aRank][aFile]];
                }
                //SOUTH
                for (aRank = rank - 1; aRank >= 0; aRank--) {
                    HEADINGS[i][BOARDINDEX[aRank][file]] = SOUTH;
                    RAY_N[i] |= BITSET[BOARDINDEX[aRank][file]];
                }
                //SOUTHWEST
                for (aFile = file - 1, aRank = rank - 1; (aFile >= 0) && (aRank >= 0); aFile--, aRank--) {
                    HEADINGS[i][BOARDINDEX[aRank][aFile]] = SOUTHWEST;
                    RAY_SW[i] |= BITSET[BOARDINDEX[aRank][aFile]];
                }
                //WEST
                for (aFile = file - 1; aFile >= 0; aFile--) {
                    HEADINGS[i][BOARDINDEX[rank][aFile]] = WEST;
                    RAY_W[i] |= BITSET[BOARDINDEX[rank][aFile]];
                }
                //NORTHWEST
                for (aFile = file - 1, aRank = rank + 1; (aFile >= 0) && (aRank < 8); aFile--, aRank++) {
                    HEADINGS[i][BOARDINDEX[aRank][aFile]] = NORTHWEST;
                    RAY_NW[i] |= BITSET[BOARDINDEX[aRank][aFile]];
                }

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
