package BitBoard;

import Board.BoardUtils;

/**
 * Created by Yonathan on 15/12/2014.
 *
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public class BitboardAttacks {

    //6bit masks for move generation
    public static final long[] RANKMASK = new long[64];
    public static final long[] FILEMASK = new long[64];
    public static final long[] DIAGA8H1MASK = new long[64];
    public static final long[] DIAGA1H8MASK = new long[64];

    public BitboardAttacks() {
        initialize();
    }

    private void initialize() {

        clearMasks();
        setupMasks();

    }

    private void setupMasks() {
        int diag8h1, diaga1h8, square;
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
                diaga1h8 = rank - file; //-7 to 7 & longest = 0;
                DIAGA1H8MASK[BoardUtils.getIndex(rank, file)] = 0x0;
                if (diaga1h8 > -1) {
                    for (square = 1; square < 8 - diaga1h8; square++)
                        DIAGA1H8MASK[BoardUtils.getIndex(rank, file)] |= BoardUtils.BITSET[BoardUtils.getIndex(square, diaga1h8 + square)];

                } else {
                    for (square = 1; square < 8 + diaga1h8; square++)
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
