package bitboard;

import board.Board;
import board.BoardUtils;
import definitions.Definitions;

/**
 * Created by Yonathan on 15/12/2014.
 * Attack moves are pre-generated here.
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public abstract class BitboardAttacks implements Definitions {

    //6bit masks for move generation
    public static final long[] RANKMASK = new long[64];
    public static final long[] FILEMASK = new long[64];
    public static final long[] DIAGA8H1MASK = new long[64];
    public static final long[] DIAGA1H8MASK = new long[64];

    //Masks for castling
    public static final long[] MASKEG = new long[2];
    public static final long[] MASKFG = new long[2];
    public static final long[] MASKBD = new long[2];
    public static final long[] MASKCE = new long[2];

    //For generating attacks
    public static final short[][] GEN_SLIDING_ATTACKS = new short[8][64];


    //Attack bitboards
    public static final long[] KNIGHT_ATTACKS = new long[64];
    public static final long[] KING_ATTACKS = new long[64];
    public static final long[] WHITE_PAWN_ATTACKS = new long[64];
    public static final long[] WHITE_PAWN_MOVES = new long[64];
    public static final long[] WHITE_PAWN_DOUBLE_MOVES = new long[64];
    public static final long[] BLACK_PAWN_ATTACKS = new long[64];
    public static final long[] BLACK_PAWN_MOVES = new long[64];
    public static final long[] BLACK_PAWN_DOUBLE_MOVES = new long[64];
    public static final long[][] RANK_ATTACKS = new long[64][64];
    public static final long[][] FILE_ATTACKS = new long[64][64];
    public static final long[][] DIAGA8H1_ATTACKS = new long[64][64];
    public static final long[][] DIAGA1H8_ATTACKS = new long[64][64];

    protected static int square, rank, aRank, file, aFile, attackBit, diaga8h1, diaga1h8;
    protected static short state6Bit;

/*    protected BitboardAttacks() {
        initialize();
    }*/

    private static void initialize() {
        clearMasks();
        setupMasks();
        generateAttacks();
    }

    static {
        initialize();
    }

    private static void generateAttacks() {
        int slide;
        short state8Bit, attack8Bit;
        for (square = 0; square < 8; square++) {
            for (state6Bit = 0; state6Bit < 64; state6Bit++) {
                state8Bit = (short) (state6Bit << 1);
                attack8Bit = 0;
                if (square < 7)
                    attack8Bit |= BoardUtils.SHORTBITSET[square + 1];
                slide = square + 2;
                while (slide <= 7) {
                    if (((~state8Bit) & (BoardUtils.SHORTBITSET[slide - 1])) != 0)
                        attack8Bit |= BoardUtils.SHORTBITSET[slide];
                    else break;
                    slide++;
                }
                if (square > 0)
                    attack8Bit |= BoardUtils.SHORTBITSET[square - 1];
                slide = square - 2;
                while (slide >= 0) {
                    if (((~state8Bit) & (BoardUtils.SHORTBITSET[slide + 1])) != 0)
                        attack8Bit |= BoardUtils.SHORTBITSET[slide];
                    else
                        break;
                    slide--;
                }
                GEN_SLIDING_ATTACKS[square][state6Bit] = attack8Bit;
            }
        }
        clearAttackBoards();
        generatePawnAttacksAndMoves();
        generateKnightAttacks();
        generateKingAttacks();
        generateRankAttacks();
        generateFileAttacks();
        generateDiagA8H1Attacks();
        generateDiagA1H8Attacks();

        MASKEG[0] = BoardUtils.BITSET[E1] | BoardUtils.BITSET[F1] | BoardUtils.BITSET[G1];
        MASKEG[1] = BoardUtils.BITSET[E8] | BoardUtils.BITSET[F8] | BoardUtils.BITSET[G8];

        MASKFG[0] = BoardUtils.BITSET[F1] | BoardUtils.BITSET[G1];
        MASKFG[1] = BoardUtils.BITSET[F8] | BoardUtils.BITSET[G8];

        MASKBD[0] = BoardUtils.BITSET[B1] | BoardUtils.BITSET[C1] | BoardUtils.BITSET[D1];
        MASKBD[1] = BoardUtils.BITSET[B8] | BoardUtils.BITSET[C8] | BoardUtils.BITSET[D8];

        MASKCE[0] = BoardUtils.BITSET[C1] | BoardUtils.BITSET[D1] | BoardUtils.BITSET[E1];
        MASKCE[1] = BoardUtils.BITSET[C8] | BoardUtils.BITSET[D8] | BoardUtils.BITSET[E8];
    }

    //For Bishops & Queens
    private static void generateDiagA1H8Attacks() {
        for (square = 0; square < 64; square++) {
            for (state6Bit = 0; state6Bit < 64; state6Bit++) {
                for (attackBit = 0; attackBit < 8; attackBit++) {
                    //  Conversion from 64 board squares to the 8 corresponding positions in the GEN_SLIDING_ATTACKS array: MIN((RANKS[square]),(FILES[square]))
                    if ((GEN_SLIDING_ATTACKS[(RANKS[square]) < FILES[square] ? (RANKS[square]) : FILES[square]][state6Bit] & BoardUtils.SHORTBITSET[attackBit]) != 0) {
                        // Conversion of square/attackBit to the corresponding 64 board file and rank:
                        diaga1h8 = FILES[square] - RANKS[square]; //from -7 to 7 & longest=0
                        if (diaga1h8 < 0) {
                            file = attackBit;
                            rank = file - diaga1h8;
                        } else {
                            rank = attackBit;
                            file = diaga1h8 + rank;
                        }
                        //if within the board add attack
                        if ((file >= 0) && (file < 8) && (rank >= 0) && (rank < 8))
                            DIAGA1H8_ATTACKS[square][state6Bit] |= BoardUtils.BITSET[BoardUtils.getIndex(rank, file)];
                    }
                }
            }
        }
    }

    //For Bishops & Queens
    private static void generateDiagA8H1Attacks() {
        for (square = 0; square < 64; square++) {
            for (state6Bit = 0; state6Bit < 64; state6Bit++) {
                for (attackBit = 0; attackBit < 8; attackBit++) {
                    //  conversion from 64 board squares to the 8 corresponding positions in the GEN_SLIDING_ATTACKS array: MIN((7-RANKS[square]),(FILES[square]))
                    if ((GEN_SLIDING_ATTACKS[(7 - RANKS[square]) < FILES[square] ? (7 - RANKS[square]) : FILES[square]][state6Bit] & BoardUtils.SHORTBITSET[attackBit]) != 0) {
                        // conversion of square/attackBit to the corresponding 64 board file and rank:
                        diaga8h1 = FILES[square] + RANKS[square]; //from 0 to 14 & longest =7
                        if (diaga8h1 < 8) {
                            file = attackBit;
                            rank = diaga8h1 - file;
                        } else {
                            rank = 7 - attackBit;
                            file = diaga8h1 - rank;
                        }
                        //if within the board add attack
                        if ((file >= 0) && (file < 8) && (rank >= 0) && (rank < 8))
                            DIAGA8H1_ATTACKS[square][state6Bit] |= BoardUtils.BITSET[BoardUtils.getIndex(rank, file)];
                    }
                }
            }
        }
    }


    private static void generateFileAttacks() {
        for (square = 0; square < 64; square++) {
            for (state6Bit = 0; state6Bit < 64; state6Bit++) {
                for (attackBit = 0; attackBit < 8; attackBit++) {
                    if ((GEN_SLIDING_ATTACKS[7 - RANKS[square]][state6Bit] & BoardUtils.SHORTBITSET[attackBit]) != 0) {
                        file = FILES[square];
                        rank = 7 - attackBit;
                        FILE_ATTACKS[square][state6Bit] |= BoardUtils.BITSET[BoardUtils.getIndex(rank, file)];
                    }
                }
            }
        }
    }

    private static void generateRankAttacks() {
        for (square = 0; square < 64; square++) {
            for (state6Bit = 0; state6Bit < 64; state6Bit++) {
                RANK_ATTACKS[square][state6Bit] |= (GEN_SLIDING_ATTACKS[FILES[square]][state6Bit] << (RANKSHIFT[square] - 1));
            }
        }
    }

    private static void generateKingAttacks() {
        for (square = 0; square < 64; square++) {
            file = FILES[square];
            rank = RANKS[square];
            aFile = file - 1;
            aRank = rank;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KING_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file - 1;
            aRank = rank + 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KING_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file;
            aRank = rank + 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KING_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file + 1;
            aRank = rank + 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KING_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file + 1;
            aRank = rank;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KING_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file + 1;
            aRank = rank - 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KING_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file;
            aRank = rank - 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KING_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file - 1;
            aRank = rank - 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KING_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
        }
    }

    private static void generateKnightAttacks() {
        for (square = 0; square < 64; square++) {
            file = FILES[square];
            rank = RANKS[square];
            aFile = file - 2;
            aRank = rank + 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KNIGHT_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file - 1;
            aRank = rank + 2;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KNIGHT_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file + 1;
            aRank = rank + 2;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KNIGHT_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file + 2;
            aRank = rank + 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KNIGHT_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file + 2;
            aRank = rank - 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KNIGHT_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file + 1;
            aRank = rank - 2;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KNIGHT_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file - 1;
            aRank = rank - 2;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KNIGHT_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file - 2;
            aRank = rank - 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                KNIGHT_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
        }
    }

    private static void generatePawnAttacksAndMoves() {
        //white pawn attacks
        for (square = 0; square < 64; square++) {
            file = FILES[square];
            rank = RANKS[square];
            aFile = file - 1;
            aRank = rank + 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                WHITE_PAWN_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file + 1;
            aRank = rank + 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                WHITE_PAWN_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
        }

        //white pawn moves
        for (square = 0; square < 64; square++) {
            file = FILES[square];
            rank = RANKS[square];
            aFile = file;
            aRank = rank + 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7)) {
                WHITE_PAWN_MOVES[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            }
            if (rank == 1) {
                aFile = file;
                aRank = rank + 2;
                if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7)) {
                    WHITE_PAWN_DOUBLE_MOVES[square] = BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
                }

            }
        }

        //black pawn attacks
        for (square = 0; square < 64; square++) {
            file = FILES[square];
            rank = RANKS[square];
            aFile = file - 1;
            aRank = rank - 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                BLACK_PAWN_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file + 1;
            aRank = rank - 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                BLACK_PAWN_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
        }

        //black pawn moves
        for (square = 0; square < 64; square++) {
            file = FILES[square];
            rank = RANKS[square];
            aFile = file;
            aRank = rank - 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                BLACK_PAWN_MOVES[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            if (rank == 6) {
                aFile = file;
                aRank = rank - 2;
                if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                    BLACK_PAWN_DOUBLE_MOVES[square] = BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];

            }
        }
    }

    private static void clearAttackBoards() {
        int state;
        for (square = 0; square < 64; square++) {
            KNIGHT_ATTACKS[square] = 0x0;
            KING_ATTACKS[square] = 0x0;
            WHITE_PAWN_ATTACKS[square] = 0x0;
            WHITE_PAWN_MOVES[square] = 0x0;
            WHITE_PAWN_DOUBLE_MOVES[square] = 0x0;
            BLACK_PAWN_ATTACKS[square] = 0x0;
            BLACK_PAWN_MOVES[square] = 0x0;
            BLACK_PAWN_DOUBLE_MOVES[square] = 0x0;
            for (state = 0; state < 64; state++) {
                RANK_ATTACKS[square][state] = 0x0;
                FILE_ATTACKS[square][state] = 0x0;
                DIAGA8H1_ATTACKS[square][state] = 0x0;
                DIAGA1H8_ATTACKS[square][state] = 0x0;
            }
        }
    }

    private static void setupMasks() {
        int file;
        for (int rank = 0; rank < 8; rank++) {
            for (file = 0; file < 8; file++) {
                RANKMASK[BoardUtils.getIndex(rank, file)] = BoardUtils.BITSET[BoardUtils.getIndex(rank, 1)] | BoardUtils.BITSET[BoardUtils.getIndex(rank, 2)] | BoardUtils.BITSET[BoardUtils.getIndex(rank, 3)];
                RANKMASK[BoardUtils.getIndex(rank, file)] |= BoardUtils.BITSET[BoardUtils.getIndex(rank, 4)] | BoardUtils.BITSET[BoardUtils.getIndex(rank, 5)] | BoardUtils.BITSET[BoardUtils.getIndex(rank, 6)];

                FILEMASK[BoardUtils.getIndex(rank, file)] = BoardUtils.BITSET[BoardUtils.getIndex(1, file)] | BoardUtils.BITSET[BoardUtils.getIndex(2, file)] | BoardUtils.BITSET[BoardUtils.getIndex(3, file)];
                FILEMASK[BoardUtils.getIndex(rank, file)] |= BoardUtils.BITSET[BoardUtils.getIndex(4, file)] | BoardUtils.BITSET[BoardUtils.getIndex(5, file)] | BoardUtils.BITSET[BoardUtils.getIndex(6, file)];

                diaga8h1 = file + rank; // 0 to 14 & longest = 7
                DIAGA8H1MASK[BoardUtils.getIndex(rank, file)] = 0x0;
                if (diaga8h1 < 8) {
                    for (square = 1; square < diaga8h1; square++)
                        DIAGA8H1MASK[BoardUtils.getIndex(rank, file)] |= BoardUtils.BITSET[BoardUtils.getIndex(diaga8h1 - square, square)];

                } else {
                    for (square = 1; square < 15 - diaga8h1; square++)
                        DIAGA8H1MASK[BoardUtils.getIndex(rank, file)] |= BoardUtils.BITSET[BoardUtils.getIndex(7 - square, diaga8h1 + square - 7)];

                }
                diaga1h8 = file - rank; //-7 to 7 & longest = 0;
                DIAGA1H8MASK[BoardUtils.getIndex(rank, file)] = 0x0;
                if (diaga1h8 > -1) {
                    for (square = 1; square < 7 - diaga1h8; square++)
                        DIAGA1H8MASK[BoardUtils.getIndex(rank, file)] |= BoardUtils.BITSET[BoardUtils.getIndex(square, square + diaga1h8)];

                } else {
                    for (square = 1; square < 7 + diaga1h8; square++)
                        DIAGA1H8MASK[BoardUtils.getIndex(rank, file)] |= BoardUtils.BITSET[BoardUtils.getIndex(square - diaga1h8, square)];

                }
            }
        }
    }

    /*     ===========================================================================
    *     isAttacked is used mainly as a move legality test to see if targetBitmap is
    *     attacked by white or black.
    *      Returns true at the first attack found, and returns false if no attack is found.
    *      It can be used for:
    *      - check detection, and
    *      - castling legality: test to see if the king passes through, or ends up on,
    *      a square that is attacked
    *     ============================================================================
    */
    public static boolean isAttacked(Board b, long target, boolean white_to_move) {
        long tempTarget = target, slidingAttackers;
        int to;
        if (white_to_move) { //Test for attacks from WHITE to target;
            while (tempTarget != 0) {
                to = BoardUtils.getIndexFromBoard(tempTarget);
                if ((b.whitePawns & BLACK_PAWN_ATTACKS[to]) != 0 || (b.whiteKnights & KNIGHT_ATTACKS[to]) != 0
                        || (b.whiteKing & KING_ATTACKS[to]) != 0)
                    return true;
                //File & rank attacks
                slidingAttackers = b.whiteQueens | b.whiteRooks;
                if (slidingAttackers != 0) {
                    if ((RANK_ATTACKS[to][(int) ((b.allPieces & RANKMASK[to]) >>> RANKSHIFT[to])]
                            & slidingAttackers) != 0 ||
                            (FILE_ATTACKS[to][(int) (((b.allPieces & FILEMASK[to]) * BitboardMagicAttacks.FILEMAGIC[to]) >>> 57)] & slidingAttackers) != 0)
                        return true;
                }
                //Diagonal attacks
                slidingAttackers = b.whiteQueens | b.whiteBishops;
                if (slidingAttackers != 0) {
                    if ((DIAGA8H1_ATTACKS[to][(int) (((b.allPieces & DIAGA8H1MASK[to]) * BitboardMagicAttacks.DIAGA8H1MAGIC[to]) >>> 57)] & slidingAttackers) != 0
                            || (DIAGA1H8_ATTACKS[to][(int) (((b.allPieces & DIAGA1H8MASK[to]) * BitboardMagicAttacks.DIAGA1H8MAGIC[to]) >>> 57)] & slidingAttackers) != 0)
                        return true;
                }
                tempTarget ^= BoardUtils.BITSET[to];
            }

        } else {            //test for attacks from BLACK to target;
            while (tempTarget != 0) {
                to = BoardUtils.getIndexFromBoard(tempTarget);
                if ((b.blackPawns & WHITE_PAWN_ATTACKS[to]) != 0 || (b.blackKnights & KNIGHT_ATTACKS[to]) != 0
                        || (b.blackKing & KING_ATTACKS[to]) != 0)
                    return true;
                //File & rank attacks
                slidingAttackers = b.blackQueens | b.blackRooks;
                if (slidingAttackers != 0) {
                    if ((RANK_ATTACKS[to][(int) ((b.allPieces & RANKMASK[to]) >>> RANKSHIFT[to])]
                            & slidingAttackers) != 0 ||
                            (FILE_ATTACKS[to][(int) (((b.allPieces & FILEMASK[to]) * BitboardMagicAttacks.FILEMAGIC[to]) >>> 57)] & slidingAttackers) != 0)
                        return true;
                }
                //Diagonal attacks
                slidingAttackers = b.blackQueens | b.blackBishops;
                if (slidingAttackers != 0) {
                    if ((DIAGA8H1_ATTACKS[to][(int) (((b.allPieces & DIAGA8H1MASK[to]) * BitboardMagicAttacks.DIAGA8H1MAGIC[to]) >>> 57)] & slidingAttackers) != 0
                            || (DIAGA1H8_ATTACKS[to][(int) (((b.allPieces & DIAGA1H8MASK[to]) * BitboardMagicAttacks.DIAGA1H8MAGIC[to]) >>> 57)] & slidingAttackers) != 0)
                        return true;
                }
                tempTarget ^= BoardUtils.BITSET[to];
            }

        }
        return false;
    }

    private static void clearMasks() {
        for (int square = 0; square < 64; square++) {
            RANKMASK[square] = 0X0;
            FILEMASK[square] = 0X0;
            DIAGA8H1MASK[square] = 0X0;
            DIAGA1H8MASK[square] = 0X0;
        }
    }
}
