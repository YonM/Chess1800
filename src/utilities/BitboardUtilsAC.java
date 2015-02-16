package utilities;

import board.Board;
import definitions.Definitions;
import move.MoveAC;

/**
 * Created by Yonathan on 07/12/2014.
 * Class provided mostly by Alberto Alonso Rubial's Carballo Chess Engine @ https://github.com/albertoruibal/carballo
 * With tweaks provided by Yonathan Maalo.
 */

public class BitboardUtilsAC implements Definitions{
    public static final long A8 = 0x8000000000000000L;
    public static final long H1 = 0x0000000000000001L; // BitboardMagicAttacksAC uses H1=0 A8=63
    public static final long WHITE_SQUARES = 0x55aa55aa55aa55aaL;
    public static final long BLACK_SQUARES = 0xaa55aa55aa55aa55L;
    // Board borders
    public static final long b_d = 0x00000000000000ffL; // down
    public static final long b_u = 0xff00000000000000L; // up
    public static final long b_r = 0x0101010101010101L; // right
    public static final long b_l = 0x8080808080808080L; // left
    // Board borders (2 squares),for the knight
    public static final long b2_d = 0x000000000000ffffL; // down
    public static final long b2_u = 0xffff000000000000L; // up
    public static final long b2_r = 0x0303030303030303L; // right
    public static final long b2_l = 0xC0C0C0C0C0C0C0C0L; // left
/*
    public static final long b3_d = 0x0000000000ffffffL; // down
    public static final long b3_u = 0xffffff0000000000L; // up
    public static final long r2_d = 0x000000000000ff00L; // rank 2 down
    public static final long r2_u = 0x00ff000000000000L; // up
    public static final long r3_d = 0x0000000000ff0000L; // rank 3 down
    public static final long r3_u = 0x0000ff0000000000L; // up
    // Board centers (for evaluation)
    public static final long c4 = 0x0000001818000000L; // center (4 squares)
    public static final long c16 = 0x00003C3C3C3C0000L; // center (16 squares)
    public static final long c36 = 0x007E7E7E7E7E7E00L; // center (36 squares)
    public static final long r4 = 0xC3C300000000C3C3L; // corners (4 squares)
    public static final long r9 = 0xE7E7E70000E7E7E7L; // corners (9 squares)
*/

    /**
     * To get the square at A1, give 0. To get the square at A2, give 1. To get
     * the square at H8, give 63.
     */
    public static final long[] getSquare;
    // 0 is a, 7 is h
    public static final long[] COLUMN = {b_l, b_r << 6, b_r << 5, b_r << 4, b_r << 3, b_r << 2, b_r << 1, b_r};
    public static final long[] COLUMNS_ADJACENTS = { //
            COLUMN[1], //
            COLUMN[0] | COLUMN[2], //
            COLUMN[1] | COLUMN[3], //
            COLUMN[2] | COLUMN[4], //
            COLUMN[3] | COLUMN[5], //
            COLUMN[4] | COLUMN[6], //
            COLUMN[5] | COLUMN[7], //
            COLUMN[6] //
    };
    public static final long[] ROWS_LEFT = { //
            0, //
            COLUMN[0], //
            COLUMN[0] | COLUMN[1], //
            COLUMN[0] | COLUMN[1] | COLUMN[2], //
            COLUMN[0] | COLUMN[1] | COLUMN[2] | COLUMN[3], //
            COLUMN[0] | COLUMN[1] | COLUMN[2] | COLUMN[3] | COLUMN[4], //
            COLUMN[0] | COLUMN[1] | COLUMN[2] | COLUMN[3] | COLUMN[4] | COLUMN[5], //
            COLUMN[0] | COLUMN[1] | COLUMN[2] | COLUMN[3] | COLUMN[4] | COLUMN[5] | COLUMN[6] //
    };
    public static final long[] ROWS_RIGHT = { //
            COLUMN[1] | COLUMN[2] | COLUMN[3] | COLUMN[4] | COLUMN[5] | COLUMN[6] | COLUMN[7], //
            COLUMN[2] | COLUMN[3] | COLUMN[4] | COLUMN[5] | COLUMN[6] | COLUMN[7], //
            COLUMN[3] | COLUMN[4] | COLUMN[5] | COLUMN[6] | COLUMN[7], //
            COLUMN[4] | COLUMN[5] | COLUMN[6] | COLUMN[7], //
            COLUMN[5] | COLUMN[6] | COLUMN[7], //
            COLUMN[6] | COLUMN[7], //
            COLUMN[7], //
            0 //
    };
    // 0 is 1, 7 is 8
    public static final long[] RANK = {b_d, b_d << 8, b_d << 16, b_d << 24, b_d << 32, b_d << 40, b_d << 48, b_d << 56};
    public static final long[] RANKS_UPWARDS = { //
            RANK[1] | RANK[2] | RANK[3] | RANK[4] | RANK[5] | RANK[6] | RANK[7], //
            RANK[2] | RANK[3] | RANK[4] | RANK[5] | RANK[6] | RANK[7], //
            RANK[3] | RANK[4] | RANK[5] | RANK[6] | RANK[7], //
            RANK[4] | RANK[5] | RANK[6] | RANK[7], //
            RANK[5] | RANK[6] | RANK[7], //
            RANK[6] | RANK[7], //
            RANK[7], //
            0 //
    };
    public static final long[] RANKS_DOWNWARDS = { //
            0, //
            RANK[0], //
            RANK[0] | RANK[1], //
            RANK[0] | RANK[1] | RANK[2], //
            RANK[0] | RANK[1] | RANK[2] | RANK[3], //
            RANK[0] | RANK[1] | RANK[2] | RANK[3] | RANK[4], //
            RANK[0] | RANK[1] | RANK[2] | RANK[3] | RANK[4] | RANK[5], //
            RANK[0] | RANK[1] | RANK[2] | RANK[3] | RANK[4] | RANK[5] | RANK[6] //
    };
    // Ranks fordward in pawn direction
    public static final long[][] RANKS_FORWARD = {RANKS_UPWARDS, RANKS_DOWNWARDS};
    public static final long[][] RANKS_BACKWARD = {RANKS_DOWNWARDS, RANKS_UPWARDS};
    public static final String[] squareNames =
                   {"a1","b1","c1","d1","e1","f1","g1","h1",
                    "a2","b2","c2","d2","e2","f2","g2","h2",
                    "a3","b3","c3","d3","e3","f3","g3","h3",
                    "a4","b4","c4","d4","e4","f4","g4","h4",
                    "a5","b5","c5","d5","e5","f5","g5","h5",
                    "a6","b6","c6","d6","e6","f6","g6","h6",
                    "a7","b7","c7","d7","e7","f7","g7","h7",
                    "a8","b8","c8","d8","e8","f8","g8","h8"};

    // To use with square2Index
    public static final byte[] bitTable = {63, 30, 3, 32, 25, 41, 22, 33, 15, 50, 42, 13, 11, 53, 19, 34, 61, 29, 2, 51, 21, 43, 45, 10, 18, 47, 1, 54, 9, 57,
            0, 35, 62, 31, 40, 4, 49, 5, 52, 26, 60, 6, 23, 44, 46, 27, 56, 16, 7, 39, 48, 24, 59, 14, 12, 55, 38, 28, 58, 20, 37, 17, 36, 8};

    static {
        getSquare = new long[64];
        for (int i = 0; i < getSquare.length; i++) getSquare[i] = 1L << i;
    }

    /**
     * Converts a square to its index 0=H1, 63=A8
     */
    public static byte square2Index(long square) {
        long b = square ^ (square - 1);
        int fold = (int) (b ^ (b >>> 32));
        return bitTable[(fold * 0x783a9b23) >>> 26];
    }

    /**
     * And viceversa
     */
    public static long index2Square(int index) {
        return H1 << index;
    }

    public static int getIndexFromBoard(long board) {
        return (Long.numberOfTrailingZeros(Long.lowestOneBit(board)));
    }

    public static int getLastIndexFromBoard(long board) {
        return (Long.numberOfTrailingZeros(Long.highestOneBit(board)));
    }



    /**
     * Changes element 0 with 63 and consecutively: this way array constants are
     * more legible
     */
    public static String[] changeEndianArray64(String sArray[]) {
        String out[] = new String[64];
        for (int i = 0; i < 64; i++) {
            out[i] = sArray[63 - i];
        }
        return out;
    }

    public static int[] changeEndianArray64(int sArray[]) {
        int out[] = new int[64];
        for (int i = 0; i < 64; i++) {
            out[i] = sArray[63 - i];
        }
        return out;
    }

    /**
     * prints a BitBoard to standard output
     */
    public static String toString(long b) {
        StringBuilder sb = new StringBuilder();
        long i = A8;
        while (i != 0) {
            sb.append(((b & i) != 0 ? "1 " : "0 "));
            if ((i & b_r) != 0)
                sb.append("\n");
            i >>>= 1;
        }
        return sb.toString();
    }


    /**
     * Convert a bitboard square to algebraic notation Number depends of rotated
     * board.
     *
     * @param square
     * @return
     */
    public static String square2Algebraic(long square) {
        return squareNames[square2Index(square)];
    }

    public static String index2Algebraic(int index) {
        return squareNames[index];
    }

    public static int algebraic2Index(String name) {
        for (int i = 0; i < 64; i++) {
            if (name.equals(squareNames[i]))
                return i;
        }
        return -1;
    }
    
    /**
     * Converts a location in "algebraic notation" (ie. of the form 'a7', 'c5',
     * etc.) into its integer representation.
     *
     * <b>Note:</b> this method may throw a <code>NumberFormatException</code>
     * if the passed string is malformed-- no error checking occurs in this
     * method.
     *
     * @param loc
     *            a string representing a location
     * @return the integer representation of the string
     */
    public static int algebraicLocToInt(String loc) {
        if (loc.equals("-"))
            return -1;
        int out = loc.charAt(0) - 'a';
        int up = Integer.parseInt(loc.charAt(1) + "") - 1;
        return up * 8 + out;
    }

}
