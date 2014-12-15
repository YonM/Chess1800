package BitBoard;

import Board.BoardUtils;
import Move.Move;

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
    public static final int WHITE_OOO_CASTLE = Move.generateMove(BoardUtils.E1, BoardUtils.G1, BoardUtils.WHITE_KING, BoardUtils.EMPTY, BoardUtils.WHITE_KING);
    public static final int BLACK_OOO_CASTLE = Move.generateMove(BoardUtils.E8, BoardUtils.G8, BoardUtils.BLACK_KING, BoardUtils.EMPTY, BoardUtils.BLACK_KING);
    ;
    public static final int WHITE_OO_CASTLE = Move.generateMove(BoardUtils.E1, BoardUtils.C1, BoardUtils.WHITE_KING, BoardUtils.EMPTY, BoardUtils.WHITE_KING);
    ;
    public static final int BLACK_OO_CASTLE = Move.generateMove(BoardUtils.E8, BoardUtils.C8, BoardUtils.BLACK_KING, BoardUtils.EMPTY, BoardUtils.BLACK_KING);
    ;


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

    protected int square, rank, aRank, file, aFile, attackBit, diaga8h1, diaga1h8;
    ;
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

        MASKEG[0] = BoardUtils.BITSET[BoardUtils.E1] | BoardUtils.BITSET[BoardUtils.F1] | BoardUtils.BITSET[BoardUtils.G1];
        MASKEG[1] = BoardUtils.BITSET[BoardUtils.E8] | BoardUtils.BITSET[BoardUtils.F8] | BoardUtils.BITSET[BoardUtils.G8];

        MASKFG[0] = BoardUtils.BITSET[BoardUtils.F1] | BoardUtils.BITSET[BoardUtils.G1];
        MASKFG[1] = BoardUtils.BITSET[BoardUtils.F8] | BoardUtils.BITSET[BoardUtils.G8];

        MASKBD[0] = BoardUtils.BITSET[BoardUtils.B1] | BoardUtils.BITSET[BoardUtils.C1] | BoardUtils.BITSET[BoardUtils.D1];
        MASKBD[1] = BoardUtils.BITSET[BoardUtils.B8] | BoardUtils.BITSET[BoardUtils.C8] | BoardUtils.BITSET[BoardUtils.D8];

        MASKCE[0] = BoardUtils.BITSET[BoardUtils.C1] | BoardUtils.BITSET[BoardUtils.D1] | BoardUtils.BITSET[BoardUtils.E1];
        MASKCE[1] = BoardUtils.BITSET[BoardUtils.C8] | BoardUtils.BITSET[BoardUtils.D8] | BoardUtils.BITSET[BoardUtils.E8];



    }

    //For Bishops & Queens
    private void generateDiagA1H8Attacks() {
        for (square = 0; square < 64; square++) {
            for (state6Bit = 0; state6Bit < 64; state6Bit++) {
                for (attackBit = 0; attackBit < 8; attackBit++) {
                    //  Conversion from 64 board squares to the 8 corresponding positions in the GEN_SLIDING_ATTACKS array: MIN((RANKS[square]),(FILES[square]))
                    if ((GEN_SLIDING_ATTACKS[(BoardUtils.RANKS[square]) < BoardUtils.FILES[square] ? (BoardUtils.RANKS[square]) : BoardUtils.FILES[square]][state6Bit] & BoardUtils.CHARBITSET[attackBit]) != 0) {
                        // Conversion of square/attackBit to the corresponding 64 board file and rank:
                        diaga1h8 = BoardUtils.FILES[square] - BoardUtils.RANKS[square]; //from -7 to 7 & longest=0
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
    private void generateDiagA8H1Attacks() {
        for (square = 0; square < 64; square++) {
            for (state6Bit = 0; state6Bit < 64; state6Bit++) {
                for (attackBit = 0; attackBit < 8; attackBit++) {
                    //  conversion from 64 board squares to the 8 corresponding positions in the GEN_SLIDING_ATTACKS array: MIN((7-RANKS[square]),(FILES[square]))
                    if ((GEN_SLIDING_ATTACKS[(7 - BoardUtils.RANKS[square]) < BoardUtils.FILES[square] ? (7 - BoardUtils.RANKS[square]) : BoardUtils.FILES[square]][state6Bit] & BoardUtils.CHARBITSET[attackBit]) != 0) {
                        // conversion of square/attackBit to the corresponding 64 board file and rank:
                        diaga8h1 = BoardUtils.FILES[square] + BoardUtils.RANKS[square]; //from 0 to 14 & longest =7
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
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                RANKMASK[BoardUtils.getIndex(rank, file)] = BoardUtils.BITSET[BoardUtils.getIndex(rank, 1)] | BoardUtils.BITSET[BoardUtils.getIndex(rank, 2)] | BoardUtils.BITSET[BoardUtils.getIndex(rank, 3)];
                RANKMASK[BoardUtils.getIndex(rank, file)] = BoardUtils.BITSET[BoardUtils.getIndex(rank, 4)] | BoardUtils.BITSET[BoardUtils.getIndex(rank, 5)] | BoardUtils.BITSET[BoardUtils.getIndex(rank, 6)];

                FILEMASK[BoardUtils.getIndex(rank, file)] = BoardUtils.BITSET[BoardUtils.getIndex(1, file)] | BoardUtils.BITSET[BoardUtils.getIndex(2, file)] | BoardUtils.BITSET[BoardUtils.getIndex(3, file)];
                FILEMASK[BoardUtils.getIndex(rank, file)] = BoardUtils.BITSET[BoardUtils.getIndex(4, file)] | BoardUtils.BITSET[BoardUtils.getIndex(5, file)] | BoardUtils.BITSET[BoardUtils.getIndex(6, file)];

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

    private void clearMasks() {
        for (int square = 0; square < 64; square++) {
            RANKMASK[square] = 0X0;
            FILEMASK[square] = 0X0;
            DIAGA8H1MASK[square] = 0X0;
            DIAGA1H8MASK[square] = 0X0;
        }
    }
}
