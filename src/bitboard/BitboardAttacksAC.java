package bitboard;

import board.Board;

/**
 * Created by Yonathan on 07/12/2014.
 */
public class BitboardAttacksAC {

    public static long[] rook;
    public static long[] bishop;
    public static long[] blackPawn;
    public static long[] whitePawn;
    public static long[] knight;
    public static long[] king;

    public static boolean USE_MAGIC = true;
    private static BitboardAttacksAC instance;

    /*    public static BitboardAttacksAC getInstance() {
            if (instance == null) {
                if (USE_MAGIC) {
                    instance = new BitboardMagicAttacksAC();
                } else {
                    instance = new BitboardAttacksAC();
                }
            }
            return instance;
        }*/
    long squareAttackedAux(long square, int shift, long border) {
        if ((square & border) == 0) {
            if (shift > 0)
                square <<= shift;
            else
                square >>>= -shift;
            return square;
        }
        return 0;
    }

    long squareAttackedAuxSlider(long square, int shift, long border) {
        long ret = 0;
        while ((square & border) == 0) {
            if (shift > 0)
                square <<= shift;
            else
                square >>>= -shift;
            ret |= square;
        }
        return ret;
    }

    public BitboardAttacksAC() {
        rook = new long[64];
        bishop = new long[64];
        blackPawn = new long[64];
        whitePawn = new long[64];
        knight = new long[64];
        king = new long[64];

        long square = 1;
        byte i = 0;

        while (square != 0) {
            rook[i] = squareAttackedAuxSlider(square, +8, BitboardUtilsAC.b_u) //
                    | squareAttackedAuxSlider(square, -8, BitboardUtilsAC.b_d) //
                    | squareAttackedAuxSlider(square, -1, BitboardUtilsAC.b_r) //
                    | squareAttackedAuxSlider(square, +1, BitboardUtilsAC.b_l);
            bishop[i] = squareAttackedAuxSlider(square, +9, BitboardUtilsAC.b_u | BitboardUtilsAC.b_l) //
                    | squareAttackedAuxSlider(square, +7, BitboardUtilsAC.b_u | BitboardUtilsAC.b_r) //
                    | squareAttackedAuxSlider(square, -7, BitboardUtilsAC.b_d | BitboardUtilsAC.b_l) //
                    | squareAttackedAuxSlider(square, -9, BitboardUtilsAC.b_d | BitboardUtilsAC.b_r);
            knight[i] = squareAttackedAux(square, +17, BitboardUtilsAC.b2_u | BitboardUtilsAC.b_l) //
                    | squareAttackedAux(square, +15, BitboardUtilsAC.b2_u | BitboardUtilsAC.b_r) //
                    | squareAttackedAux(square, -15, BitboardUtilsAC.b2_d | BitboardUtilsAC.b_l) //
                    | squareAttackedAux(square, -17, BitboardUtilsAC.b2_d | BitboardUtilsAC.b_r) //
                    | squareAttackedAux(square, +10, BitboardUtilsAC.b_u | BitboardUtilsAC.b2_l) //
                    | squareAttackedAux(square, +6, BitboardUtilsAC.b_u | BitboardUtilsAC.b2_r) //
                    | squareAttackedAux(square, -6, BitboardUtilsAC.b_d | BitboardUtilsAC.b2_l) //
                    | squareAttackedAux(square, -10, BitboardUtilsAC.b_d | BitboardUtilsAC.b2_r);
            whitePawn[i] = squareAttackedAux(square, 7, BitboardUtilsAC.b_u | BitboardUtilsAC.b_r) //
                    | squareAttackedAux(square, 9, BitboardUtilsAC.b_u | BitboardUtilsAC.b_l);
            blackPawn[i] = squareAttackedAux(square, -7, BitboardUtilsAC.b_d | BitboardUtilsAC.b_l) //
                    | squareAttackedAux(square, -9, BitboardUtilsAC.b_d | BitboardUtilsAC.b_r);
            king[i] = squareAttackedAux(square, +8, BitboardUtilsAC.b_u) //
                    | squareAttackedAux(square, -8, BitboardUtilsAC.b_d) //
                    | squareAttackedAux(square, -1, BitboardUtilsAC.b_r) //
                    | squareAttackedAux(square, +1, BitboardUtilsAC.b_l) //
                    | squareAttackedAux(square, +9, BitboardUtilsAC.b_u | BitboardUtilsAC.b_l) //
                    | squareAttackedAux(square, +7, BitboardUtilsAC.b_u | BitboardUtilsAC.b_r) //
                    | squareAttackedAux(square, -7, BitboardUtilsAC.b_d | BitboardUtilsAC.b_l) //
                    | squareAttackedAux(square, -9, BitboardUtilsAC.b_d | BitboardUtilsAC.b_r);
            square <<= 1;
            i++;
        }
    }

    /**
     * Discover attacks to squares using magics: expensive version
     */
    public boolean isSquareAttacked(Board board, long square, boolean white) {
        return isIndexAttacked(board, BitboardUtilsAC.square2Index(square), white);
    }

    /**
     * Discover attacks to squares using magics: cheap version
     */
    public boolean isIndexAttacked(Board board, byte index, boolean white) {
        if (index < 0 || index > 63)
            return false;
        long others = (white ? board.blackPieces : board.whitePieces);
        long all = board.allPieces;
        if (((white ? whitePawn[index] : blackPawn[index]) & (board.whitePawns | board.blackPawns) & others) != 0) {
            return true;
        } else if ((king[index] & (board.whiteKing | board.blackKing) & others) != 0) {
            return true;
        } else if ((knight[index] & (board.whiteKnights | board.blackKnights) & others) != 0) {
            return true;
        } else if ((getRookAttacks(index, all) & ((board.whiteRooks | board.blackRooks) | (board.whiteQueens | board.blackQueens)) & others) != 0) {
            return true;
        } else if ((getBishopAttacks(index, all) & ((board.whiteBishops | board.blackBishops) | (board.whiteQueens | board.blackQueens)) & others) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Discover attacks to squares using magics: cheap version
     */
    public long getIndexAttacks(Board board, int index) {
        if (index < 0 || index > 63)
            return 0;
        long all = board.allPieces;
        return ((board.blackPieces & whitePawn[index] | board.whitePieces & blackPawn[index]) & (board.whitePawns | board.blackPawns))
                | (king[index] & (board.whiteKing | board.blackKing))
                | (knight[index] & (board.whiteKnights | board.blackKnights))
                | (getRookAttacks(index, all) & ((board.whiteRooks | board.blackRooks) | (board.whiteQueens | board.blackQueens))) | (getBishopAttacks(index, all) & ((board.whiteBishops | board.blackBishops) | (board.whiteQueens | board.blackQueens)));
    }

    public long getXrayAttacks(Board board, int index, long all) {
        if (index < 0 || index > 63)
            return 0;
        return (getRookAttacks(index, all) & ((board.whiteRooks | board.blackRooks) | (board.whiteQueens | board.blackQueens)) | (getBishopAttacks(index, all) & ((board.whiteBishops | board.blackBishops) | (board.whiteQueens | board.blackQueens)))) & all;
    }

    /**
     * without magic bitboards, too expensive, but uses less memory
     */
    public long getRookAttacks(int index, long all) {
        return getRookShiftAttacks(BitboardUtilsAC.index2Square((byte) index), all);
    }

    public long getBishopAttacks(int index, long all) {
        return getBishopShiftAttacks(BitboardUtilsAC.index2Square((byte) index), all);
    }

    public long getRookShiftAttacks(long square, long all) {
        return checkSquareAttackedAux(square, all, +8, BitboardUtilsAC.b_u) | checkSquareAttackedAux(square, all, -8, BitboardUtilsAC.b_d)
                | checkSquareAttackedAux(square, all, -1, BitboardUtilsAC.b_r) | checkSquareAttackedAux(square, all, +1, BitboardUtilsAC.b_l);
    }

    public long getBishopShiftAttacks(long square, long all) {
        return checkSquareAttackedAux(square, all, +9, BitboardUtilsAC.b_u | BitboardUtilsAC.b_l)
                | checkSquareAttackedAux(square, all, +7, BitboardUtilsAC.b_u | BitboardUtilsAC.b_r)
                | checkSquareAttackedAux(square, all, -7, BitboardUtilsAC.b_d | BitboardUtilsAC.b_l)
                | checkSquareAttackedAux(square, all, -9, BitboardUtilsAC.b_d | BitboardUtilsAC.b_r);
    }

    /**
     * Attacks for sliding pieces
     */
    private long checkSquareAttackedAux(long square, long all, int shift, long border) {
        long ret = 0;
        while ((square & border) == 0) {
            if (shift > 0)
                square <<= shift;
            else
                square >>>= -shift;
            ret |= square;
// If we collide with other piece
            if ((square & all) != 0)
                break;
        }
        return ret;
    }
}