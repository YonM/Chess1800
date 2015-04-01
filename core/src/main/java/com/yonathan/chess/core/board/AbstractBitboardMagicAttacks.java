package com.yonathan.chess.core.board;

/**
 * Created by Yonathan on 07/12/2014.
 * Based on Alberto Ruibal's Carballo. Source @ https://githucom/albertoruibal/carballo/
 */
public abstract class AbstractBitboardMagicAttacks {

    protected static long[] rook;
    protected static long[] rookMask;
    protected static long[][] rookMagic;
    protected static long[] bishop;
    protected static long[] bishopMask;
    protected static long[][] bishopMagic;
    protected static long[] knight;
    protected static long[] king;
    protected static long[] blackPawn;
    protected static long[] whitePawn;

    protected long whitePawns;
    protected long whiteKnights;
    protected long whiteBishops;
    protected long whiteRooks;
    protected long whiteQueens;
    protected long whiteKing;

    protected long blackPawns;
    protected long blackKnights;
    protected long blackBishops;
    protected long blackRooks;
    protected long blackQueens;
    protected long blackKing;

    protected long whitePieces;//Aggregation bitboards
    protected long blackPieces;
    protected long allPieces;

    protected static final long A8 = 0x8000000000000000L;
    protected static final long H1 = 0x0000000000000001L; // AbstractBitboardMagicAttacks uses H1=0 A8=63
    protected static final long WHITE_SQUARES = 0xaa55aa55aa55aa55L;
    protected static final long BLACK_SQUARES = 0x55aa55aa55aa55aaL;
    // Board borders
    protected static final long b_d = 0x00000000000000ffL; // down
    protected static final long b_u = 0xff00000000000000L; // up
    protected static final long b_r = 0x0101010101010101L; // right
    protected static final long b_l = 0x8080808080808080L; // left
    // Board borders (2 squares),for the knight
    protected static final long b2_d = 0x000000000000ffffL; // down
    protected static final long b2_u = 0xffff000000000000L; // up
    protected static final long b2_r = 0x0303030303030303L; // right
    protected static final long b2_l = 0xC0C0C0C0C0C0C0C0L; // left

    protected final String[] squareNames =
            {"a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                    "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
                    "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
                    "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
                    "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
                    "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
                    "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                    "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8"};


    // 0 is a, 7 is h
    public static final long[] COLUMN = {b_l, b_r << 6, b_r << 5, b_r << 4, b_r << 3, b_r << 2, b_r << 1, b_r};

    // 0 is 1, 7 is 8
    public static final long[] RANK = {b_d, b_d << 8, b_d << 16, b_d << 24, b_d << 32, b_d << 40, b_d << 48, b_d << 56};


    // Magic numbers provided by Russell Newman & Chris Moreton @ http://www.rivalchess.com/magic-bitboards/
    private final long magicNumberRook[] = {
            0xa180022080400230L, 0x40100040022000L, 0x80088020001002L, 0x80080280841000L, 0x4200042010460008L, 0x4800a0003040080L, 0x400110082041008L, 0x8000a041000880L, 0x10138001a080c010L, 0x804008200480L, 0x10011012000c0L, 0x22004128102200L, 0x200081201200cL, 0x202a001048460004L, 0x81000100420004L, 0x4000800380004500L, 0x208002904001L, 0x90004040026008L, 0x208808010002001L, 0x2002020020704940L, 0x8048010008110005L, 0x6820808004002200L, 0xa80040008023011L, 0xb1460000811044L, 0x4204400080008ea0L, 0xb002400180200184L, 0x2020200080100380L, 0x10080080100080L, 0x2204080080800400L, 0xa40080360080L, 0x2040604002810b1L, 0x8c218600004104L, 0x8180004000402000L, 0x488c402000401001L, 0x4018a00080801004L, 0x1230002105001008L, 0x8904800800800400L, 0x42000c42003810L, 0x8408110400b012L, 0x18086182000401L, 0x2240088020c28000L, 0x1001201040c004L, 0xa02008010420020L, 0x10003009010060L, 0x4008008008014L, 0x80020004008080L, 0x282020001008080L, 0x50000181204a0004L, 0x102042111804200L, 0x40002010004001c0L, 0x19220045508200L, 0x20030010060a900L, 0x8018028040080L, 0x88240002008080L, 0x10301802830400L, 0x332a4081140200L, 0x8080010a601241L, 0x1008010400021L, 0x4082001007241L, 0x211009001200509L, 0x8015001002441801L, 0x801000804000603L, 0xc0900220024a401L, 0x1000200608243L
    };

    // Magic numbers provided by Russell Newman & Chris Moreton @ http://www.rivalchess.com/magic-bitboards/
    private final long magicNumberBishop[] = {
            0x2910054208004104L, 0x2100630a7020180L, 0x5822022042000000L, 0x2ca804a100200020L, 0x204042200000900L, 0x2002121024000002L, 0x80404104202000e8L, 0x812a020205010840L, 0x8005181184080048L, 0x1001c20208010101L, 0x1001080204002100L, 0x1810080489021800L, 0x62040420010a00L, 0x5028043004300020L, 0xc0080a4402605002L, 0x8a00a0104220200L, 0x940000410821212L, 0x1808024a280210L, 0x40c0422080a0598L, 0x4228020082004050L, 0x200800400e00100L, 0x20b001230021040L, 0x90a0201900c00L, 0x4940120a0a0108L, 0x20208050a42180L, 0x1004804b280200L, 0x2048020024040010L, 0x102c04004010200L, 0x20408204c002010L, 0x2411100020080c1L, 0x102a008084042100L, 0x941030000a09846L, 0x244100800400200L, 0x4000901010080696L, 0x280404180020L, 0x800042008240100L, 0x220008400088020L, 0x4020182000904c9L, 0x23010400020600L, 0x41040020110302L, 0x412101004020818L, 0x8022080a09404208L, 0x1401210240484800L, 0x22244208010080L, 0x1105040104000210L, 0x2040088800c40081L, 0x8184810252000400L, 0x4004610041002200L, 0x40201a444400810L, 0x4611010802020008L, 0x80000b0401040402L, 0x20004821880a00L, 0x8200002022440100L, 0x9431801010068L, 0x1040c20806108040L, 0x804901403022a40L, 0x2400202602104000L, 0x208520209440204L, 0x40c000022013020L, 0x2000104000420600L, 0x400000260142410L, 0x800633408100500L, 0x2404080a1410L, 0x138200122002900L
    };

    private final int magicNumberShiftsRook[] = {
            12, 11, 11, 11, 11, 11, 11, 12, 11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11, 12, 11, 11, 11, 11, 11, 11, 12
    };

    private final int magicNumberShiftsBishop[] = {
            6, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 8, 8, 8, 8, 5, 5, 5, 5, 8, 9, 9, 8, 5, 5,
            5, 5, 8, 9, 9, 8, 5, 5, 5, 5, 8, 8, 8, 8, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 5, 5, 6
    };

    // To use with square2Index
    private static final byte[] bitTable = {63, 30, 3, 32, 25, 41, 22, 33, 15, 50, 42, 13, 11, 53, 19, 34, 61, 29, 2, 51, 21, 43, 45, 10, 18, 47, 1, 54, 9, 57,
            0, 35, 62, 31, 40, 4, 49, 5, 52, 26, 60, 6, 23, 44, 46, 27, 56, 16, 7, 39, 48, 24, 59, 14, 12, 55, 38, 28, 58, 20, 37, 17, 36, 8};


    protected final void generateAttacks() {
        rook = new long[64];
        rookMask = new long[64];
        rookMagic = new long[64][];
        bishop = new long[64];
        bishopMask = new long[64];
        bishopMagic = new long[64][];
        knight = new long[64];
        king = new long[64];
        blackPawn = new long[64];
        whitePawn = new long[64];
        long square = 1;
        byte i = 0;
        while (square != 0) {

            rook[i] = squareAttackedAuxSlider(square, +8, b_u)
                    | squareAttackedAuxSlider(square, -8, b_d)
                    | squareAttackedAuxSlider(square, -1, b_r)
                    | squareAttackedAuxSlider(square, +1, b_l);

            rookMask[i] = squareAttackedAuxSliderMask(square, +8, b_u)
                    | squareAttackedAuxSliderMask(square, -8, b_d)
                    | squareAttackedAuxSliderMask(square, -1, b_r)
                    | squareAttackedAuxSliderMask(square, +1, b_l);

            bishop[i] = squareAttackedAuxSlider(square, +9, b_u | b_l)
                    | squareAttackedAuxSlider(square, +7, b_u | b_r)
                    | squareAttackedAuxSlider(square, -7, b_d | b_l)
                    | squareAttackedAuxSlider(square, -9, b_d | b_r);

            bishopMask[i] = squareAttackedAuxSliderMask(square, +9, b_u | b_l)
                    | squareAttackedAuxSliderMask(square, +7, b_u | b_r)
                    | squareAttackedAuxSliderMask(square, -7, b_d | b_l)
                    | squareAttackedAuxSliderMask(square, -9, b_d | b_r);

            knight[i] = squareAttackedAux(square, +17, b2_u | b_l)
                    | squareAttackedAux(square, +15, b2_u | b_r)
                    | squareAttackedAux(square, -15, b2_d | b_l)
                    | squareAttackedAux(square, -17, b2_d | b_r)
                    | squareAttackedAux(square, +10, b_u | b2_l)
                    | squareAttackedAux(square, +6, b_u | b2_r)
                    | squareAttackedAux(square, -6, b_d | b2_l)
                    | squareAttackedAux(square, -10, b_d | b2_r);

            whitePawn[i] = squareAttackedAux(square, 7, b_u | b_r)
                    | squareAttackedAux(square, 9, b_u | b_l);
            blackPawn[i] = squareAttackedAux(square, -7, b_d | b_l)
                    | squareAttackedAux(square, -9, b_d | b_r);

            king[i] = squareAttackedAux(square, +8, b_u)
                    | squareAttackedAux(square, -8, b_d)
                    | squareAttackedAux(square, -1, b_r)
                    | squareAttackedAux(square, +1, b_l)
                    | squareAttackedAux(square, +9, b_u | b_l)
                    | squareAttackedAux(square, +7, b_u | b_r)
                    | squareAttackedAux(square, -7, b_d | b_l)
                    | squareAttackedAux(square, -9, b_d | b_r);

            // And now generate magics
            int rookPositions = (1 << magicNumberShiftsRook[i]);
            rookMagic[i] = new long[rookPositions];
            for (int j = 0; j < rookPositions; j++) {
                long pieces = generatePieces(j, magicNumberShiftsRook[i], rookMask[i]);
                int magicIndex = magicTransform(pieces, magicNumberRook[i], magicNumberShiftsRook[i]);
                rookMagic[i][magicIndex] = getRookShiftAttacks(square, pieces);
            }

            int bishopPositions = (1 << magicNumberShiftsBishop[i]);
            bishopMagic[i] = new long[bishopPositions];
            for (int j = 0; j < bishopPositions; j++) {
                long pieces = generatePieces(j, magicNumberShiftsBishop[i], bishopMask[i]);
                int magicIndex = magicTransform(pieces, magicNumberBishop[i], magicNumberShiftsBishop[i]);
                bishopMagic[i][magicIndex] = getBishopShiftAttacks(square, pieces);
            }
            square <<= 1;
            i++;
        }

    }

    //Fills a board according to the mask. Used for magics.
    private long generatePieces(int index, int bits, long mask) {
        int i;
        long lsb;
        long result = 0L;
        for (i = 0; i < bits; i++) {
            lsb = Long.lowestOneBit(mask);
            mask ^= lsb; // Deactivates lsb bit of the mask to get next bit next time
            if ((index & (1 << i)) != 0)
                result |= lsb; // if bit is set to 1
        }
        return result;
    }

    //For non-slider pieces
    private long squareAttackedAux(long square, int shift, long border) {
        if ((square & border) == 0) {
            if (shift > 0)
                square <<= shift;
            else
                square >>>= -shift;
            return square;
        }
        return 0;
    }

    //For slider pieces
    private long squareAttackedAuxSliderMask(long square, int shift, long border) {
        long mask = 0;
        while ((square & border) == 0) {
            if (shift > 0) {
                square <<= shift;
            } else {
                square >>>= -shift;
            }
            if ((square & border) == 0) {
                mask |= square;
            }
        }
        return mask;
    }

    /**
     * Determines if a square is being attacked by a given side.
     *
     * @param square the target square.
     * @param white  true if white is supposedly attacking the square, false
     *               otherwise.
     * @return true if the square is being attacked, false otherwise.
     */
    public boolean isSquareAttacked(long square, boolean white) {
        return isIndexAttacked(square2Index(square), white);
    }

    /**
     * Converts a square to its index 0=H1, 63=A8
     */
    public static byte square2Index(long square) {
        long b = square ^ (square - 1);
        int fold = (int) (b ^ (b >>> 32));
        return bitTable[(fold * 0x783a9b23) >>> 26];
    }


    protected boolean isIndexAttacked(byte i, boolean white) {
        if (i < 0 || i > 63)
            return false;
        long others = white ? blackPieces : whitePieces;
        long all = allPieces;
        if (((white ? whitePawn[i] : blackPawn[i])
                & (whitePawns | blackPawns) & others) != 0)
            return true;
        if ((king[i] & (whiteKing | blackKing) & others) != 0)
            return true;
        if ((knight[i] & (whiteKnights | blackKnights) & others) != 0)
            return true;
        if ((getRookAttacks(i, all)
                & ((whiteRooks | blackRooks) | (whiteQueens | blackQueens)) & others) != 0)
            return true;
        if ((getBishopAttacks(i, all)
                & ((whiteBishops | blackBishops) | (whiteQueens | blackQueens)) & others) != 0)
            return true;
        return false;
    }

    /**
     * Finds all attackers that attack a square through another square.
     *
     * @param i   the target location
     * @param all the bitboard representing the places through which we can
     *            move.
     * @return
     */
    protected long getXrayAttacks(int i, long all) {
        if (i < 0 || i > 63)
            return 0;
        return ((getRookAttacks(i, all) & ((whiteRooks | blackRooks) | (whiteQueens | blackQueens))) | (getBishopAttacks(
                i, all) & ((whiteBishops | blackBishops) | (whiteQueens | blackQueens))))
                & all;
    }


    protected int magicTransform(long b, long magic, int bits) {
        return (int) ((b * magic) >>> (64 - bits));
    }

    protected long getRookAttacks(int index, long all) {
        int i = magicTransform(all & rookMask[index], magicNumberRook[index],
                magicNumberShiftsRook[index]);
        return rookMagic[index][i];
    }

    protected long getBishopAttacks(int index, long all) {
        int i = magicTransform(all & bishopMask[index], magicNumberBishop[index],
                magicNumberShiftsBishop[index]);
        return bishopMagic[index][i];
    }

    protected long getQueenAttacks(int index, long all) {
        return getRookAttacks(index, all) | getBishopAttacks(index, all);
    }

    protected long getRookShiftAttacks(long square, long all) {
        return checkSquareAttackedAux(square, all, +8, b_u) | checkSquareAttackedAux(square, all, -8, b_d)
                | checkSquareAttackedAux(square, all, -1, b_r) | checkSquareAttackedAux(square, all, +1, b_l);
    }

    protected long getBishopShiftAttacks(long square, long all) {
        return checkSquareAttackedAux(square, all, +9, b_u | b_l)
                | checkSquareAttackedAux(square, all, +7, b_u | b_r)
                | checkSquareAttackedAux(square, all, -7, b_d | b_l)
                | checkSquareAttackedAux(square, all, -9, b_d | b_r);
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

    private long squareAttackedAuxSlider(long square, int shift, long border) {
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

    //Finds & returns locations of attackers of a square.
    protected long getIndexAttacks(int index) {
        if (index < 0 || index > 63) return 0;

        long all = allPieces;

        return ((blackPieces & whitePawn[index] | whitePieces | blackPawn[index]) & (whitePawns | blackPawns))
                | (knight[index] & (whiteKnights | blackKnights))
                | (king[index] & (whiteKing | blackKing))
                | (getRookAttacks(index, all) & ((whiteRooks | blackRooks) | (whiteQueens | blackQueens)))
                | (getBishopAttacks(index, all) & ((whiteBishops | blackBishops) | (whiteQueens | blackQueens)));
    }

}
