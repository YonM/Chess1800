package bitboard;

import utilities.BitboardUtilsAC;
import board.Board;

/**
 * Created by Yonathan on 07/12/2014.
 * Based on Alberto Ruibal's Carballo. Source @ https://github.com/albertoruibal/carballo/
 */
public class BitboardMagicAttacksAC {

    private static BitboardMagicAttacksAC instance;
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

    public static BitboardMagicAttacksAC getInstance() {
        if(instance==null){
            instance = new BitboardMagicAttacksAC();
            return instance;
        }
        return instance;
    }

    private BitboardMagicAttacksAC()
    {
        generateAttacks();
    }


    private void generateAttacks() {
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

            rook[i] = squareAttackedAuxSlider(square, +8, BitboardUtilsAC.b_u)
                    | squareAttackedAuxSlider(square, -8, BitboardUtilsAC.b_d) 
                    | squareAttackedAuxSlider(square, -1, BitboardUtilsAC.b_r) 
                    | squareAttackedAuxSlider(square, +1, BitboardUtilsAC.b_l);
            
            rookMask[i] = squareAttackedAuxSliderMask(square, +8, BitboardUtilsAC.b_u)
                    | squareAttackedAuxSliderMask(square, -8, BitboardUtilsAC.b_d)
                    | squareAttackedAuxSliderMask(square, -1, BitboardUtilsAC.b_r) 
                    | squareAttackedAuxSliderMask(square, +1, BitboardUtilsAC.b_l);

            bishop[i] = squareAttackedAuxSlider(square, +9, BitboardUtilsAC.b_u | BitboardUtilsAC.b_l) 
                    | squareAttackedAuxSlider(square, +7, BitboardUtilsAC.b_u | BitboardUtilsAC.b_r) 
                    | squareAttackedAuxSlider(square, -7, BitboardUtilsAC.b_d | BitboardUtilsAC.b_l) 
                    | squareAttackedAuxSlider(square, -9, BitboardUtilsAC.b_d | BitboardUtilsAC.b_r);

            bishopMask[i] = squareAttackedAuxSliderMask(square, +9, BitboardUtilsAC.b_u | BitboardUtilsAC.b_l)
                    | squareAttackedAuxSliderMask(square, +7, BitboardUtilsAC.b_u | BitboardUtilsAC.b_r)
                    | squareAttackedAuxSliderMask(square, -7, BitboardUtilsAC.b_d | BitboardUtilsAC.b_l) 
                    | squareAttackedAuxSliderMask(square, -9, BitboardUtilsAC.b_d | BitboardUtilsAC.b_r);

            knight[i] = squareAttackedAux(square, +17, BitboardUtilsAC.b2_u | BitboardUtilsAC.b_l)
                    | squareAttackedAux(square, +15, BitboardUtilsAC.b2_u | BitboardUtilsAC.b_r)
                    | squareAttackedAux(square, -15, BitboardUtilsAC.b2_d | BitboardUtilsAC.b_l)
                    | squareAttackedAux(square, -17, BitboardUtilsAC.b2_d | BitboardUtilsAC.b_r)
                    | squareAttackedAux(square, +10, BitboardUtilsAC.b_u | BitboardUtilsAC.b2_l)
                    | squareAttackedAux(square, +6, BitboardUtilsAC.b_u | BitboardUtilsAC.b2_r)
                    | squareAttackedAux(square, -6, BitboardUtilsAC.b_d | BitboardUtilsAC.b2_l)
                    | squareAttackedAux(square, -10, BitboardUtilsAC.b_d | BitboardUtilsAC.b2_r);

            whitePawn[i] = squareAttackedAux(square, 7, BitboardUtilsAC.b_u | BitboardUtilsAC.b_r)
                    | squareAttackedAux(square, 9, BitboardUtilsAC.b_u | BitboardUtilsAC.b_l);
            blackPawn[i] = squareAttackedAux(square, -7, BitboardUtilsAC.b_d | BitboardUtilsAC.b_l)
                    | squareAttackedAux(square, -9, BitboardUtilsAC.b_d | BitboardUtilsAC.b_r);

            king[i] = squareAttackedAux(square, +8, BitboardUtilsAC.b_u)
                    | squareAttackedAux(square, -8, BitboardUtilsAC.b_d)
                    | squareAttackedAux(square, -1, BitboardUtilsAC.b_r)
                    | squareAttackedAux(square, +1, BitboardUtilsAC.b_l)
                    | squareAttackedAux(square, +9, BitboardUtilsAC.b_u | BitboardUtilsAC.b_l)
                    | squareAttackedAux(square, +7, BitboardUtilsAC.b_u | BitboardUtilsAC.b_r)
                    | squareAttackedAux(square, -7, BitboardUtilsAC.b_d | BitboardUtilsAC.b_l)
                    | squareAttackedAux(square, -9, BitboardUtilsAC.b_d | BitboardUtilsAC.b_r);

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
     * @param b      the board to consider.
     * @param square the target square.
     * @param white  true if white is supposedly attacking the square, false
     *               otherwise.
     * @return true if the square is being attacked, false otherwise.
     */
    public boolean isSquareAttacked(Board b, long square, boolean white) {
        return isIndexAttacked(b, BitboardUtilsAC.square2Index(square), white);
    }

    private boolean isIndexAttacked(Board b, byte i, boolean white) {
        if (i < 0 || i > 63)
            return false;
        long others = white ? b.blackPieces: b.whitePieces;
        long all = b.allPieces;
        if (((white ? whitePawn[i] : blackPawn[i])
                & (b.whitePawns | b.blackPawns) & others) != 0)
            return true;
        if ((king[i] & (b.whiteKing | b.blackKing) & others) != 0)
            return true;
        if ((knight[i] & (b.whiteKnights | b.blackKnights) & others) != 0)
            return true;
        if ((getRookAttacks(i, all)
                & ( (b.whiteRooks | b.blackRooks) | (b.whiteQueens | b.blackQueens) ) & others) != 0)
            return true;
        if ((getBishopAttacks(i, all)
                & ( (b.whiteBishops | b.blackBishops) | (b.whiteQueens | b.blackQueens) ) & others) != 0)
            return true;
        return false;
    }

    /**
     * Finds all attackers that attack a square through another square.
     *
     * @param b   the board to consider
     * @param i   the target location
     * @param all the bitboard representing the places through which we can
     *            move.
     * @return
     */
    public long getXrayAttacks(Board b, int i, long all) {
        if (i < 0 || i > 63)
            return 0;
        return ((getRookAttacks(i, all) & ((b.whiteRooks | b.blackRooks) | (b.whiteQueens | b.blackQueens))) | (getBishopAttacks(
                i, all) & ((b.whiteBishops | b.blackBishops) | (b.whiteQueens | b.blackQueens))))
                & all;
    }



    public int magicTransform(long b, long magic, int bits) {
        return (int) ((b * magic) >>> (64 - bits));
    }

    public long getRookAttacks(int index, long all) {
        int i = magicTransform(all & rookMask[index], magicNumberRook[index],
                magicNumberShiftsRook[index]);
        return rookMagic[index][i];
    }

    public long getBishopAttacks(int index, long all) {
        int i = magicTransform(all & bishopMask[index], magicNumberBishop[index],
                magicNumberShiftsBishop[index]);
        return bishopMagic[index][i];
    }

    public long getQueenAttacks(int index, long all) {
        return getRookAttacks(index, all) | getBishopAttacks(index, all);
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
    public long getIndexAttacks(Board b, int index) {
        if (index < 0 || index > 63) return 0;

        long all = b.allPieces;

        return ((b.blackPieces & whitePawn[index] | b.whitePieces & blackPawn[index]) & (b.whitePawns & b.blackPawns))
                | (knight[index] & (b.whiteKnights & b.blackKnights))
                | (king[index] & (b.whiteKing & b.blackKing))
                | (getRookAttacks(index, all) & ((b.whiteRooks & b.blackRooks) | (b.whiteQueens | b.blackQueens)))
                | (getBishopAttacks(index, all) & ((b.whiteBishops & b.blackBishops) | (b.whiteQueens | b.blackQueens)));
    }

}
