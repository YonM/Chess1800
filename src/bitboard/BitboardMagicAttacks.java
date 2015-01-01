package bitboard;
import board.Board;
import board.BoardUtils;

/**
 * Created by Yonathan on 15/12/2014.
 * Magic move generation for the sliding pieces.
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public class BitboardMagicAttacks extends BitboardAttacks {
    //    private static BitboardMagicAttacks instance;
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
    //private static final Board b;
    private static long targets;

    //Static Initializer
    static {
        //b = Board.getInstance();
        clearMasks();
        setupMasks();
    }
    
/*    public static BitboardMagicAttacks getInstance() {
        if (instance == null) {
            instance = new BitboardMagicAttacks();
        }
        return instance;
    }*/

/*    public BitboardMagicAttacks() {
        super();
        initialize();
    }*/

    private static void initialize() {
        clearMasks();
        setupMasks();

    }

    private static void setupMasks() {
        for (rank = 0; rank < 8; rank++) {
            for (file = 0; file < 8; file++) {
                FILEMAGIC[BoardUtils.getIndex(rank, file)] = FILEMAGICS[file];
                DIAGA8H1MAGIC[BoardUtils.getIndex(rank, file)] = DIAGA8H1MAGICS[file + rank];
                DIAGA1H8MAGIC[BoardUtils.getIndex(rank, file)] = DIAGA1H8MAGICS[file - rank + 7];
            }
        }
    }

    private static void clearMasks() {
        for (square = 0; square < 64; square++) {
            FILEMAGIC[square] = 0x0;
            DIAGA8H1MAGIC[square] = 0x0;
            DIAGA1H8MAGIC[square] = 0x0;
        }
    }

    public static long rookMoves(Board b, int from) {
        return (rankMoves(b, from) | fileMoves(b, from));
    }

    private static long rankMoves(Board b, int from) {
        setTargets(b);
//        System.out.println("Rook Move: " + Long.toBinaryString(RANK_ATTACKS[from][(int) ((b.allPieces & RANKMASK[from]) >>> RANKSHIFT[from])] & targets));
        return RANK_ATTACKS[from][(int) ((b.allPieces & RANKMASK[from]) >>> RANKSHIFT[from])] & targets;
    }

    private static long fileMoves(Board b, int from) {
        setTargets(b);
//        System.out.println("File Move: " + Long.toBinaryString(FILE_ATTACKS[from][(int) (((b.allPieces & FILEMASK[from]) * FILEMAGIC[from]) >>> 57)] & targets));
        return FILE_ATTACKS[from][(int) (((b.allPieces & FILEMASK[from]) * FILEMAGIC[from]) >>> 57)] & targets;
    }

    public static long bishopMoves(Board b, int from) {
        return diagA8H1Moves(b, from) | diagA1H8Moves(b, from);
    }

    public static long QueenMoves(Board b, int from) {
        return bishopMoves(b, from) | rookMoves(b, from);
    }

    private static long diagA8H1Moves(Board b, int from) {

        setTargets(b);
        return DIAGA8H1_ATTACKS[from][(int) (((b.allPieces & DIAGA8H1MASK[from]) * DIAGA8H1MAGIC[from]) >>> 57)] & targets;
    }

    private static long diagA1H8Moves(Board b, int from) {

        setTargets(b);
        return DIAGA1H8_ATTACKS[from][(int) (((b.allPieces & DIAGA1H8MASK[from]) * DIAGA1H8MAGIC[from]) >>> 57)] & targets;
    }

    private static void setTargets(Board b) {
        targets = b.whiteToMove ? ~b.whitePieces : ~b.blackPieces;
    }
}
