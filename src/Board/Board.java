package Board;

import BitBoard.BitOperations;
import Fen.FENValidator;

/**
 * Created by Yonathan on 08/12/2014.
 */
public class Board {
    public int[] square = new int[64];
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

    public long whitePieces;//Aggregation bitboards
    public long blackPieces;
    public long allPieces;

    public int epSquare;
    public int fiftyMove;
    public int nextMove;
    public int castleWhite;
    public int castleBlack;

    public static final int MAX_GAME_LENGTH = 1024;

    public boolean viewRotated;
    private static Board instance;

    public void initialize() {
        for (int i = 0; i < 64; i++)
            square[i] = BoardUtils.EMPTY;
        setupBoard();
        initializeFromSquares(square, BoardUtils.WHITE_MOVE, 0, BoardUtils.CANCASTLEOO + BoardUtils.CANCASTLEOOO, BoardUtils.CANCASTLEOO + BoardUtils.CANCASTLEOOO, 0);


    }

    public boolean initializeFromFEN(String fen) {
        if (FENValidator.isValidFEN(fen)) {
//            StringTokenizer st = new StringTokenizer(fen, "/ ");
//            ArrayList<String> arr = new ArrayList<String>();
//
//            while (st.hasMoreTokens()) {
//                arr.add(st.nextToken());
//            }
            int[] fenSquares = new int[64];
            String[] tokens = fen.split("[ \\t\\n\\x0B\\f\\r]+");
            String board = tokens[0];
            int rank = 7;
            int file = 0;
            for (char c : board.toCharArray()) {
                if (Character.isDigit(c)) {
                    for (int j = 0; j < Character.digit(c, 10); j++)
                        file++;

                } else {
                    switch (c) {
                        case '/':
                            rank--;
                            file = 0;
                            break;
                        case 'P':
                            fenSquares[BoardUtils.getIndex(rank, file)] = BoardUtils.WHITE_PAWN;
                            break;
                        case 'N':
                            fenSquares[BoardUtils.getIndex(rank, file)] = BoardUtils.WHITE_KNIGHT;
                            break;
                        case 'B':
                            fenSquares[BoardUtils.getIndex(rank, file)] = BoardUtils.WHITE_BISHOP;
                            break;
                        case 'R':
                            fenSquares[BoardUtils.getIndex(rank, file)] = BoardUtils.WHITE_ROOK;
                            break;
                        case 'Q':
                            fenSquares[BoardUtils.getIndex(rank, file)] = BoardUtils.WHITE_QUEEN;
                            break;
                        case 'K':
                            fenSquares[BoardUtils.getIndex(rank, file)] = BoardUtils.WHITE_KING;
                            break;
                        case 'p':
                            fenSquares[BoardUtils.getIndex(rank, file)] = BoardUtils.BLACK_PAWN;
                            break;
                        case 'n':
                            fenSquares[BoardUtils.getIndex(rank, file)] = BoardUtils.BLACK_KNIGHT;
                            break;
                        case 'b':
                            fenSquares[BoardUtils.getIndex(rank, file)] = BoardUtils.BLACK_BISHOP;
                            break;
                        case 'r':
                            fenSquares[BoardUtils.getIndex(rank, file)] = BoardUtils.BLACK_ROOK;
                            break;
                        case 'q':
                            fenSquares[BoardUtils.getIndex(rank, file)] = BoardUtils.BLACK_QUEEN;
                            break;
                        case 'k':
                            fenSquares[BoardUtils.getIndex(rank, file)] = BoardUtils.BLACK_KING;
                            break;
                    }
                    if (c != '/') file++;
                }
            }
            char nextToMove = tokens[1].toCharArray()[0];
            int tempWhiteCastle = 0, tempBlackCastle = 0, ePSquare = 0, temp50Move = 0;
            //For castling
            if (tokens.length > 2) {
                String castleInfo = tokens[2];
                if (castleInfo.contains("K")) {
                    tempWhiteCastle += BoardUtils.CANCASTLEOO;
                }
                if (castleInfo.contains("Q")) {
                    tempWhiteCastle += BoardUtils.CANCASTLEOOO;
                }
                if (castleInfo.contains("k")) {
                    tempBlackCastle += BoardUtils.CANCASTLEOO;
                }
                if (castleInfo.contains("q")) {
                    tempBlackCastle += BoardUtils.CANCASTLEOOO;
                }
                if (tokens.length > 3) {
                    char[] enPassant = tokens[3].toCharArray();
                    if (!enPassant.equals("-")) {
                        ePSquare = BoardUtils.getIndex(enPassant[0] - 96, enPassant[1]);
                    }
                }
                if (tokens.length > 4) {
                    try {
                        temp50Move = Integer.parseInt(tokens[4]);
                    } catch (Exception ignore) {

                    }
                }


            }

            initializeFromSquares(fenSquares, nextToMove, temp50Move, tempWhiteCastle, tempBlackCastle, ePSquare);
            return true;
        } else {
            //if fen is not valid
            return false;
        }


    }


    public void initializeFromSquares(int[] input, char nextToMove, int fiftyMove, int castleWhiteSide, int castleBlackSide, int epSquare) {
        clearBitboards();
        //setup the 12 boards
        for (int i = 0; i < 64; i++) {
            square[i] = input[i];
            switch (i) {
                case BoardUtils.WHITE_KING:
                    whiteKing |= BoardUtils.bitSet[i];
                    break;
                case BoardUtils.WHITE_QUEEN:
                    whiteQueens |= BoardUtils.bitSet[i];
                    break;
                case BoardUtils.WHITE_ROOK:
                    whiteRooks |= BoardUtils.bitSet[i];
                    break;
                case BoardUtils.WHITE_BISHOP:
                    whiteBishops |= BoardUtils.bitSet[i];
                    break;
                case BoardUtils.WHITE_KNIGHT:
                    whiteKnights |= BoardUtils.bitSet[i];
                    break;
                case BoardUtils.WHITE_PAWN:
                    whitePawns |= BoardUtils.bitSet[i];
                    break;
                case BoardUtils.BLACK_KING:
                    blackKing |= BoardUtils.bitSet[i];
                    break;
                case BoardUtils.BLACK_QUEEN:
                    blackQueens |= BoardUtils.bitSet[i];
                    break;
                case BoardUtils.BLACK_ROOK:
                    blackRooks |= BoardUtils.bitSet[i];
                    break;
                case BoardUtils.BLACK_BISHOP:
                    blackBishops |= BoardUtils.bitSet[i];
                    break;
                case BoardUtils.BLACK_KNIGHT:
                    blackKnights |= BoardUtils.bitSet[i];
                    break;
                case BoardUtils.BLACK_PAWN:
                    blackPawns |= BoardUtils.bitSet[i];
                    break;
            }


        }
        updateAggregateBitboards();

        nextMove = nextToMove;
        castleWhite = castleWhiteSide;
        castleBlack = castleBlackSide;
        this.epSquare = epSquare;
        this.fiftyMove = fiftyMove;

        material = BitOperations.popCount(whitePawns) * BoardUtils.PAWN_VALUE + BitOperations.popCount(whiteKnights) * BoardUtils.KNIGHT_VALUE
                + BitOperations.popCount(whiteBishops) * BoardUtils.BISHOP_VALUE + BitOperations.popCount(whiteRooks) * BoardUtils.ROOK_VALUE
                + BitOperations.popCount(whiteQueens) * BoardUtils.QUEEN_VALUE;
        material -= (BitOperations.popCount(blackPawns) * BoardUtils.PAWN_VALUE + BitOperations.popCount(blackKnights) * BoardUtils.KNIGHT_VALUE
                + BitOperations.popCount(blackBishops) * BoardUtils.BISHOP_VALUE + BitOperations.popCount(blackRooks) * BoardUtils.ROOK_VALUE
                + BitOperations.popCount(blackQueens) * BoardUtils.QUEEN_VALUE);

    }

    private void updateAggregateBitboards() {
        whitePieces = whiteKing | whiteQueens | whiteRooks | whiteBishops | whiteKnights | whitePawns;
        blackPieces = blackKing | blackQueens | blackRooks | blackBishops | blackKnights | blackPawns;
        allPieces = whitePieces | blackPieces;
    }

    private void clearBitboards() {
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

    private void setupBoard() {
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

    }

    public static Board getInstance() {
        if (instance == null) {
            instance = new Board();
        }
        return instance;
    }


}
