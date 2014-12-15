package BitBoard;

import Board.BoardUtils;

/**
 * Created by Yonathan on 15/12/2014.
 */
public class BitboardAttacks {

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
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                RANKMASK[BoardUtils.getIndex(rank, file)] = BoardUtils.BITSET[BoardUtils.getIndex(rank, 2)]
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
