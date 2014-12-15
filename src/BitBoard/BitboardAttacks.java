package BitBoard;

import Board.BoardUtils;

/**
 * Created by Yonathan on 15/12/2014.
 *
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public abstract class BitboardAttacks {

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

    //Pre generated castling moves
    public static int white_OOO_Castle;
    public static int black_OOO_Castle;
    public static int white_OO_Castle;
    public static int black_OO_Castle;


    //For generating attacks
    public static final short[][] GEN_SLIDING_ATTACKS = new short[8][64];
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

    int square, rank, aRank, file, aFile, attackBit;
    short state6Bit;

    public BitboardAttacks() {
        initialize();
    }

    private void initialize() {

        clearMasks();
        setupMasks();
        generateAttacks();

    }

    private void generateAttacks() {
        int slide;
        short state8Bit, attack8Bit;
        for (square = 0; square < 8; square++) {
            for (state6Bit = 0; state6Bit < 64; state6Bit++) {
                state8Bit = (short) (state6Bit << 1);
                attack8Bit = 0;
                if (square < 7)
                    attack8Bit |= BoardUtils.CHARBITSET[square + 1];
                slide = square + 2;
                while (slide <= 7) {
                    if (((~state8Bit) & (BoardUtils.CHARBITSET[slide - 1])) != 0)
                        attack8Bit |= BoardUtils.CHARBITSET[slide];
                    else break;
                    slide++;
                }
                if (square > 0)
                    attack8Bit |= BoardUtils.CHARBITSET[square - 1];
                slide = square - 2;
                while (slide >= 0) {
                    if (((~state8Bit) & (BoardUtils.CHARBITSET[slide + 1])) != 0)
                        attack8Bit |= BoardUtils.CHARBITSET[slide];
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
        genereateKingAttacks();
        generateRankAttacks();
        generateFileAttacks();
        generateDiagA8H1Attacks();
        generateDiagA1H8Attacks();


    }

    private void generateDiagA8H1Attacks() {
        for (square = 0; square < 64; square++) {
            for (state6Bit = 0; state6Bit < 64; state6Bit++) {
                for (attackBit = 0; attackBit < 8; attackBit++) {
                    if ()
                }
            }
        }
    }


    private void generateFileAttacks() {
        for (square = 0; square < 64; square++) {
            for (state6Bit = 0; state6Bit < 64; state6Bit++) {
                for (attackBit = 0; attackBit < 8; attackBit++) {
                    if ((GEN_SLIDING_ATTACKS[7 - BoardUtils.RANKS[square]][state6Bit] & BoardUtils.CHARBITSET[attackBit]) != 0) {
                        file = BoardUtils.FILES[square];
                        rank = 7 - attackBit;
                        FILE_ATTACKS[square][state6Bit] |= BoardUtils.BITSET[BoardUtils.getIndex(rank, file)];
                    }
                }
            }
        }
    }

    private void generateRankAttacks() {
        for (square = 0; square < 64; square++) {
            for (state6Bit = 0; state6Bit < 64; state6Bit++) {
                RANK_ATTACKS[square][state6Bit] |= GEN_SLIDING_ATTACKS[BoardUtils.FILES[square]][state6Bit] << (RANKSHIFT[square] - 1);
            }
        }
    }

    private void genereateKingAttacks() {
        for (square = 0; square < 64; square++) {
            file = BoardUtils.FILES[square];
            rank = BoardUtils.RANKS[square];
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

    private void generateKnightAttacks() {
        for (square = 0; square < 64; square++) {
            file = BoardUtils.FILES[square];
            rank = BoardUtils.RANKS[square];
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

    private void generatePawnAttacksAndMoves() {
        //white pawn attacks
        for (square = 0; square < 64; square++) {
            file = BoardUtils.FILES[square];
            rank = BoardUtils.RANKS[square];
            aFile = file - 1;
            aRank = rank + 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (rank <= 7))
                WHITE_PAWN_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file + 1;
            aRank = rank + 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                WHITE_PAWN_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
        }

        //white pawn moves
        for (square = 0; square < 64; square++) {
            file = BoardUtils.FILES[square];
            rank = BoardUtils.RANKS[square];
            aFile = file;
            aRank = rank + 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (rank <= 7))
                WHITE_PAWN_MOVES[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            if (rank == 1) {
                aFile = file;
                aRank = rank + 2;
                if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                    WHITE_PAWN_DOUBLE_MOVES[square] = BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];

            }
        }

        //black pawn attacks
        for (square = 0; square < 64; square++) {
            file = BoardUtils.FILES[square];
            rank = BoardUtils.RANKS[square];
            aFile = file - 1;
            aRank = rank - 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (rank <= 7))
                BLACK_PAWN_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            aFile = file + 1;
            aRank = rank - 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                BLACK_PAWN_ATTACKS[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
        }

        //black pawn moves
        for (square = 0; square < 64; square++) {
            file = BoardUtils.FILES[square];
            rank = BoardUtils.RANKS[square];
            aFile = file;
            aRank = rank - 1;
            if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (rank <= 7))
                BLACK_PAWN_MOVES[square] |= BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];
            if (rank == 6) {
                aFile = file;
                aRank = rank - 2;
                if ((aFile >= 0) & (aFile <= 7) & (aRank >= 0) & (aRank <= 7))
                    BLACK_PAWN_DOUBLE_MOVES[square] = BoardUtils.BITSET[BoardUtils.getIndex(aRank, aFile)];

            }
        }
    }

    private void clearAttackBoards() {
        for (square = 0; square < 64; square++) {
            KNIGHT_ATTACKS[square] = 0x0;
            KING_ATTACKS[square] = 0x0;
            WHITE_PAWN_ATTACKS[square] = 0x0;
            WHITE_PAWN_MOVES[square] = 0x0;
            WHITE_PAWN_DOUBLE_MOVES[square] = 0x0;
            BLACK_PAWN_ATTACKS[square] = 0x0;
            BLACK_PAWN_MOVES[square] = 0x0;
            BLACK_PAWN_DOUBLE_MOVES[square] = 0x0;
            for (int state = 0; state < 64; state++) {
                RANK_ATTACKS[square][state] = 0x0;
                FILE_ATTACKS[square][state] = 0x0;
                DIAGA8H1_ATTACKS[square][state] = 0x0;
                DIAGA1H8_ATTACKS[square][state] = 0x0;
            }
        }
    }

    private void setupMasks() {
        int diag8h1, diaga1h8;
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                RANKMASK[BoardUtils.getIndex(rank, file)] = BoardUtils.BITSET[BoardUtils.getIndex(rank, 1)] | BoardUtils.BITSET[BoardUtils.getIndex(rank, 2)] | BoardUtils.BITSET[BoardUtils.getIndex(rank, 3)];
                RANKMASK[BoardUtils.getIndex(rank, file)] = BoardUtils.BITSET[BoardUtils.getIndex(rank, 4)] | BoardUtils.BITSET[BoardUtils.getIndex(rank, 5)] | BoardUtils.BITSET[BoardUtils.getIndex(rank, 6)];

                FILEMASK[BoardUtils.getIndex(rank, file)] = BoardUtils.BITSET[BoardUtils.getIndex(1, file)] | BoardUtils.BITSET[BoardUtils.getIndex(2, file)] | BoardUtils.BITSET[BoardUtils.getIndex(3, file)];
                FILEMASK[BoardUtils.getIndex(rank, file)] = BoardUtils.BITSET[BoardUtils.getIndex(4, file)] | BoardUtils.BITSET[BoardUtils.getIndex(5, file)] | BoardUtils.BITSET[BoardUtils.getIndex(6, file)];

                diag8h1 = file + rank; // 0 to 14 & longest = 7
                DIAGA8H1MASK[BoardUtils.getIndex(rank, file)] = 0x0;
                if (diag8h1 < 8) {
                    for (square = 1; square < diag8h1; square++)
                        DIAGA8H1MASK[BoardUtils.getIndex(rank, file)] |= BoardUtils.BITSET[BoardUtils.getIndex(diag8h1 - square, square)];

                } else {
                    for (square = 1; square < 15 - diag8h1; square++)
                        DIAGA8H1MASK[BoardUtils.getIndex(rank, file)] |= BoardUtils.BITSET[BoardUtils.getIndex(7 - square, diag8h1 + square - 7)];

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

    private void clearMasks() {
        for (int square = 0; square < 64; square++) {
            RANKMASK[square] = 0X0;
            FILEMASK[square] = 0X0;
            DIAGA8H1MASK[square] = 0X0;
            DIAGA1H8MASK[square] = 0X0;
        }
    }
}
