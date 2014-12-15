package BitBoard;

/**
 * Created by Yonathan on 15/12/2014.
 */
public class BitboardMagicAttacks extends BitboardAttacks {
    public static final long[] FILEMAGIC = new long[64];
    public static final long[] DIAGA8H1MAGIC = new long[64];
    public static final long[] DIAGA1H8MAGIC = new long[64];

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
