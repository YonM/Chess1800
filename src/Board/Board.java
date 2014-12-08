package Board;

/**
 * Created by Yonathan on 08/12/2014.
 */
public class Board {
    public int[] square;
    public int material;

    public long whitePawns;
    public long whiteKnights;
    public long whiteBishops;
    public long whiteRooks;
    public long whiteQueens;
    public long whiteKing;

    public long blackPawns;
    public long blackKnights;
    public long blackBishops;
    public long blackRooks;
    public long blackQueens;
    public long blackKing;

    public long whitePieces;
    public long blackPieces;
    public long allPieces;

    public int epSquare;
    public int fiftyMove;
    public int nextMove;
    public int castleWhite;
    public int castleBlack;

    public boolean viewRotated;
    private static Board instance;

    public void initialize() {
        for (int i = 0; i < 64; i++)
            square[i] = BoardUtils.EMPTY;
        setupBoard(true);
        initializeFromSquares(square, BoardUtils.WHITE_MOVE, 0, BoardUtils.CANCASTLEOO + BoardUtils.CANCASTLEOOO, BoardUtils.CANCASTLEOO + BoardUtils.CANCASTLEOOO, 0);


    }


    public void initializeFromSquares(int[] input, char nextToMove, int fiftyMove, int castleWhiteSide, int castleBlackSide, int epSquare) {
        clearBoards();
        //setup the 12 boards
        for (int i = 0; i < 64; i++) {
            square[i] = input[i];
            switch (i) {
                case BoardUtils.WHITE_KING:
                    whiteKing |= BoardUtils.bitSet[i];

                case BoardUtils.WHITE_QUEEN:
                    whiteQueens |= BoardUtils.bitSet[i];

                case BoardUtils.WHITE_ROOK:
                    whiteRooks |= BoardUtils.bitSet[i];
                case BoardUtils.WHITE_BISHOP:
                    whiteBishops |= BoardUtils.bitSet[i];
                case BoardUtils.WHITE_KNIGHT:
                    whiteKnights |= BoardUtils.bitSet[i];
                case BoardUtils.WHITE_PAWN:
                    whitePawns |= BoardUtils.bitSet[i];
                case BoardUtils.BLACK_KING:
                    blackKing |= BoardUtils.bitSet[i];
                case BoardUtils.BLACK_QUEEN:
                    blackQueens |= BoardUtils.bitSet[i];
                case BoardUtils.BLACK_ROOK:
                    blackRooks |= BoardUtils.bitSet[i];
                case BoardUtils.BLACK_BISHOP:
                    blackBishops |= BoardUtils.bitSet[i];
                case BoardUtils.BLACK_KNIGHT:
                    blackKnights |= BoardUtils.bitSet[i];
                case BoardUtils.BLACK_PAWN:
                    blackPawns |= BoardUtils.bitSet[i];
            }


        }
        whitePieces = whiteKing | whiteQueens | whiteRooks | whiteBishops | whiteKnights | whitePawns;
        blackPieces = blackKing | blackQueens | blackRooks | blackBishops | blackKnights | blackPawns;
        allPieces = whitePieces | blackPieces;

        nextMove = nextToMove;
        this.epSquare = epSquare;
        this.fiftyMove = fiftyMove;

        //material;


    }

    private void clearBoards() {
        whiteKing = 0;
        whiteQueens = 0;
        whiteRooks = 0;
        whiteBishops = 0;
        whiteKnights = 0;
        whitePawns = 0;
        blackKing = 0;
        blackQueens = 0;
        blackRooks = 0;
        blackBishops = 0;
        blackKnights = 0;
        blackPawns = 0;
        whitePieces = 0;
        blackPieces = 0;
        allPieces = 0;
    }

    private void setupBoard(boolean start) {
        if (start) {
            square[BoardUtils.E1] = BoardUtils.WHITE_KING;
            square[BoardUtils.D1] = BoardUtils.WHITE_QUEEN;
            square[BoardUtils.A1] = BoardUtils.WHITE_ROOK;
            square[BoardUtils.H1] = BoardUtils.WHITE_ROOK;
            square[BoardUtils.B1] = BoardUtils.WHITE_KNIGHT;
            square[BoardUtils.G1] = BoardUtils.WHITE_KNIGHT;
            square[BoardUtils.C1] = BoardUtils.WHITE_BISHOP;
            square[BoardUtils.F1] = BoardUtils.WHITE_BISHOP;
            square[BoardUtils.A2] = BoardUtils.WHITE_PAWN;
            square[BoardUtils.B2] = BoardUtils.WHITE_PAWN;
            square[BoardUtils.C2] = BoardUtils.WHITE_PAWN;
            square[BoardUtils.D2] = BoardUtils.WHITE_PAWN;
            square[BoardUtils.E2] = BoardUtils.WHITE_PAWN;
            square[BoardUtils.F2] = BoardUtils.WHITE_PAWN;
            square[BoardUtils.G2] = BoardUtils.WHITE_PAWN;
            square[BoardUtils.H2] = BoardUtils.WHITE_PAWN;

            square[BoardUtils.E8] = BoardUtils.BLACK_KING;
            square[BoardUtils.D8] = BoardUtils.BLACK_QUEEN;
            square[BoardUtils.A8] = BoardUtils.BLACK_ROOK;
            square[BoardUtils.H8] = BoardUtils.BLACK_ROOK;
            square[BoardUtils.B8] = BoardUtils.BLACK_KNIGHT;
            square[BoardUtils.G8] = BoardUtils.BLACK_KNIGHT;
            square[BoardUtils.C8] = BoardUtils.BLACK_BISHOP;
            square[BoardUtils.F8] = BoardUtils.BLACK_BISHOP;
            square[BoardUtils.A7] = BoardUtils.BLACK_PAWN;
            square[BoardUtils.B7] = BoardUtils.BLACK_PAWN;
            square[BoardUtils.C7] = BoardUtils.BLACK_PAWN;
            square[BoardUtils.D7] = BoardUtils.BLACK_PAWN;
            square[BoardUtils.E7] = BoardUtils.BLACK_PAWN;
            square[BoardUtils.F7] = BoardUtils.BLACK_PAWN;
            square[BoardUtils.G7] = BoardUtils.BLACK_PAWN;
            square[BoardUtils.H7] = BoardUtils.BLACK_PAWN;
        } else {

        }
    }

    public static Board getInstance() {
        if (instance == null) {
            instance = new Board();
        }
        return instance;
    }

    public void initializeFromFEN() {

    }

}
