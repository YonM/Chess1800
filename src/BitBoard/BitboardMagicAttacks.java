package BitBoard;

import Board.BoardUtils;

/**
 * Created by Yonathan on 15/12/2014.
 */
public class BitboardMagicAttacks extends BitboardAttacks {
    public static final long[] FILEMAGIC = new long[64];
    public static final long[] DIAGA8H1MAGIC = new long[64];
    public static final long[] DIAGA1H8MAGIC = new long[64];
    public static final long[] FILEMAGICS = {
            0x8040201008040200L,
            0x4020100804020100L,
            0x2010080402010080L,
            0x1008040201008040L,
            0x0804020100804020L,
            0x0402010080402010L,
            0x0201008040201008L,
            0x0100804020100804L
    };

    public static final long[] DIAGA8H1MAGICS = {
            0x0L,
            0x0L,
            0x0101010101010100L,
            0x0101010101010100L,
            0x0101010101010100L,
            0x0101010101010100L,
            0x0101010101010100L,
            0x0101010101010100L,
            0x0080808080808080L,
            0x0040404040404040L,
            0x0020202020202020L,
            0x0010101010101010L,
            0x0008080808080808L,
            0x0L,
            0x0L
    };

    public static final long[] DIAGA1H8MAGICS = {
            0x0L,
            0x0L,
            0x0101010101010100L,
            0x0101010101010100L,
            0x0101010101010100L,
            0x0101010101010100L,
            0x0101010101010100L,
            0x0101010101010100L,
            0x8080808080808000L,
            0x4040404040400000L,
            0x2020202020000000L,
            0x1010101000000000L,
            0x0808080000000000L,
            0x0L,
            0x0L
    };

    public BitboardMagicAttacks() {
        super();
        initialize();
    }

    private void initialize() {
        clearMasks();
        setupMasks();

    }

    private void setupMasks() {
        for (rank = 0; rank < 8; rank++) {
            for (file = 0; file < 8; file++) {
                FILEMAGIC[BoardUtils.getIndex(rank, file)] = FILEMAGICS[file];
                DIAGA8H1MAGIC[BoardUtils.getIndex(rank, file)] = DIAGA8H1MAGICS[file + rank];
                DIAGA1H8MAGIC[BoardUtils.getIndex(rank, file)] = DIAGA1H8MAGICS[file - rank + 7];
            }
        }
    }

    private void clearMasks() {
        for (square = 0; square < 64; square++) {
            FILEMAGIC[square] = 0x0;
            DIAGA8H1MAGIC[square] = 0x0;
            DIAGA1H8MAGIC[square] = 0x0;
        }
    }
}
