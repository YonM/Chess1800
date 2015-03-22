package com.chess1800.chess.board;
/**
 * Created by Yonathan on 07/12/2014.
 * Based on Alberto Ruibal's Carballo. Source @ https://githucom/albertoruibal/carballo/
 */
public abstract class AbstractBitboardMagicAttacks{

    public long[] rook;
    public long[] rookMask;
    public long[][] rookMagic;
    public long[] bishop;
    public long[] bishopMask;
    public long[][] bishopMagic;
    public long[] knight;
    public long[] king;
    public long[] blackPawn;
    public long[] whitePawn;

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

    protected static final String[] squareNames =changeEndianArray64(new String []
                   {"a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8", //
                    "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7", //
                    "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6", //
                    "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5", //
                    "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4", //
                    "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3", //
                    "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2", //
                    "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"});



    
    // 0 is a, 7 is h
    public static final long[] COLUMN = {b_l, b_r << 6, b_r << 5, b_r << 4, b_r << 3, b_r << 2, b_r << 1, b_r};

    // 0 is 1, 7 is 8
    public static final long[] RANK = {b_d, b_d << 8, b_d << 16, b_d << 24, b_d << 32, b_d << 40, b_d << 48, b_d << 56};


    // Magic numbers provided by Russell Newman & Chris Moreton @ http://www.rivalchess.com/magic-bitboards/
    private final long magicNumberRook[] = {
            0x1080108000400020L, 0x40200010004000L, 0x100082000441100L, 0x480041000080080L, 0x100080005000210L,
            0x100020801000400L, 0x280010000800200L, 0x100008020420100L, 0x400800080400020L, 0x401000402000L, 0x100801000200080L, 0x801000800800L,
            0x800400080080L, 0x800200800400L, 0x1000200040100L, 0x4840800041000080L, 0x20008080004000L, 0x404010002000L, 0x808010002000L, 0x828010000800L,
            0x808004000800L, 0x14008002000480L, 0x40002100801L, 0x20001004084L, 0x802080004000L, 0x200080400080L, 0x810001080200080L, 0x10008080080010L,
            0x4000080080040080L, 0x40080020080L, 0x1000100040200L, 0x80008200004124L, 0x804000800020L, 0x804000802000L, 0x801000802000L, 0x2000801000800804L,
            0x80080800400L, 0x80040080800200L, 0x800100800200L, 0x8042000104L, 0x208040008008L, 0x10500020004000L, 0x100020008080L, 0x2000100008008080L,
            0x200040008008080L, 0x8020004008080L, 0x1000200010004L, 0x100040080420001L, 0x80004000200040L, 0x200040100140L, 0x20004800100040L, 0x100080080280L,
            0x8100800400080080L, 0x8004020080040080L, 0x9001000402000100L, 0x40080410200L, 0x208040110202L, 0x800810022004012L, 0x1000820004011L,
            0x1002004100009L, 0x41001002480005L, 0x81000208040001L, 0x4000008201100804L, 0x2841008402L
    };

    // Magic numbers provided by Russell Newman & Chris Moreton @ http://www.rivalchess.com/magic-bitboards/
    private final long magicNumberBishop[] = {
            0x1020041000484080L, 0x20204010a0000L, 0x8020420240000L, 0x404040085006400L, 0x804242000000108L,
            0x8901008800000L, 0x1010110400080L, 0x402401084004L, 0x1000200810208082L, 0x20802208200L, 0x4200100102082000L, 0x1024081040020L, 0x20210000000L,
            0x8210400100L, 0x10110022000L, 0x80090088010820L, 0x8001002480800L, 0x8102082008200L, 0x41001000408100L, 0x88000082004000L, 0x204000200940000L,
            0x410201100100L, 0x2000101012000L, 0x40201008200c200L, 0x10100004204200L, 0x2080020010440L, 0x480004002400L, 0x2008008008202L, 0x1010080104000L,
            0x1020001004106L, 0x1040200520800L, 0x8410000840101L, 0x1201000200400L, 0x2029000021000L, 0x4002400080840L, 0x5000020080080080L, 0x1080200002200L,
            0x4008202028800L, 0x2080210010080L, 0x800809200008200L, 0x1082004001000L, 0x1080202411080L, 0x840048010101L, 0x40004010400200L, 0x500811020800400L,
            0x20200040800040L, 0x1008012800830a00L, 0x1041102001040L, 0x11010120200000L, 0x2020222020c00L, 0x400002402080800L, 0x20880000L, 0x1122020400L,
            0x11100248084000L, 0x210111000908000L, 0x2048102020080L, 0x1000108208024000L, 0x1004100882000L, 0x41044100L, 0x840400L, 0x4208204L,
            0x80000200282020cL, 0x8a001240100L, 0x2040104040080L
    };

    private final int magicNumberShiftsRook[] = {
            12, 11, 11, 11, 11, 11, 11, 12, //
            11, 10, 10, 10, 10, 10, 10, 11, //
            11, 10, 10, 10, 10, 10, 10, 11, //
            11, 10, 10, 10, 10, 10, 10, 11, //
            11, 10, 10, 10, 10, 10, 10, 11, //
            11, 10, 10, 10, 10, 10, 10, 11, //
            11, 10, 10, 10, 10, 10, 10, 11, //
            12, 11, 11, 11, 11, 11, 11, 12 //
    };

    private final int magicNumberShiftsBishop[] = {
            6, 5, 5, 5, 5, 5, 5, 6, //
            5, 5, 5, 5, 5, 5, 5, 5, //
            5, 5, 7, 7, 7, 7, 5, 5, //
            5, 5, 7, 9, 9, 7, 5, 5, //
            5, 5, 7, 9, 9, 7, 5, 5, //
            5, 5, 7, 7, 7, 7, 5, 5, //
            5, 5, 5, 5, 5, 5, 5, 5, //
            6, 5, 5, 5, 5, 5, 5, 6 //
    };

    // To use with square2Index
    private static final byte[] bitTable = {63, 30, 3, 32, 25, 41, 22, 33, 15, 50, 42, 13, 11, 53, 19, 34, 61, 29, 2, 51, 21, 43, 45, 10, 18, 47, 1, 54, 9, 57,
            0, 35, 62, 31, 40, 4, 49, 5, 52, 26, 60, 6, 23, 44, 46, 27, 56, 16, 7, 39, 48, 24, 59, 14, 12, 55, 38, 28, 58, 20, 37, 17, 36, 8};

    public static String[] changeEndianArray64(String sArray[]) {
        String out[] = new String[64];
        for (int i = 0; i < 64; i++) {
            out[i] = sArray[63 - i];
        }
        return out;
    }


    protected final void generateAttacks() {
        rook = new long [64];
        rookMask = new long[64];
        rookMagic = new long[64][];
        bishop = new long [64];
        bishopMask = new long[64];
        bishopMagic = new long[64][];
        knight = new long[64];
        king = new long [64];
        blackPawn = new long [64];
        whitePawn = new long [64];
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
    protected static byte square2Index(long square) {
        long b = square ^ (square - 1);
        int fold = (int) (b ^ (b >>> 32));
        return bitTable[(fold * 0x783a9b23) >>> 26];
    }


    protected boolean isIndexAttacked(byte i, boolean white) {
        if (i < 0 || i > 63)
            return false;
        long others = white ? blackPieces: whitePieces;
        long all = allPieces;
        if (((white ? whitePawn[i] : blackPawn[i])
                & (whitePawns | blackPawns) & others) != 0)
            return true;
        if ((king[i] & (whiteKing | blackKing) & others) != 0)
            return true;
        if ((knight[i] & (whiteKnights | blackKnights) & others) != 0)
            return true;
        if ((getRookAttacks(i, all)
                & ( (whiteRooks | blackRooks) | (whiteQueens | blackQueens) ) & others) != 0)
            return true;
        if ((getBishopAttacks(i, all)
                & ( (whiteBishops | blackBishops) | (whiteQueens | blackQueens) ) & others) != 0)
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

        return ((blackPieces & whitePawn[index] | whitePieces & blackPawn[index]) & (whitePawns & blackPawns))
                | (knight[index] & (whiteKnights & blackKnights))
                | (king[index] & (whiteKing & blackKing))
                | (getRookAttacks(index, all) & ((whiteRooks & blackRooks) | (whiteQueens | blackQueens)))
                | (getBishopAttacks(index, all) & ((whiteBishops & blackBishops) | (whiteQueens | blackQueens)));
    }

    protected final static int getColumnOfIndex(int index) {
        return 7 - (index & 7);
    }

    protected final static int getRankOfIndex(int index) {
        return index >> 3;
    }
}
