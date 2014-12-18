package board;

import bitboard.BitOperations;
import fen.FENValidator;
import move.Move;

/**
 * Created by Yonathan on 08/12/2014.
 * Singleton class to represent the board.
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
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

    public int ePSquare;
    public int fiftyMove;
    public boolean whiteToMove;

    public final int CANCASTLEOO = 1;
    public final int CANCASTLEOOO = 2;
    public int castleWhite;
    public int castleBlack;

    public static final int MAX_GAME_LENGTH = 1024; // Maximum number of half-moves, if 50-move rule is obeyed.
    public static final int MAX_MOVES = 256;

    public static final int MAX_PLY = 64;

    public Move[] moves;
    public int[] moveBufLen;

    public int endOfGame, endOfSearch; // Index for board.gameLine
    public final GameLineRecord[] gameLine = new GameLineRecord[MAX_GAME_LENGTH]; // Current search line + moves that have actually been played.

    //For (un)make move
    private int from, to, piece, captured;
    private long fromBoard, toBoard, fromToBoard;

    public boolean viewRotated;
    private static Board instance;

    public Board() {
        moves = new Move[MAX_GAME_LENGTH * 4];
        moveBufLen = new int[MAX_PLY];
    }

    public void initialize() {
        for (int i = 0; i < 64; i++)
            square[i] = BoardUtils.EMPTY;
        setupBoard();
        initializeFromSquares(square, true, 0, BoardUtils.CANCASTLEOO + BoardUtils.CANCASTLEOOO, BoardUtils.CANCASTLEOO + BoardUtils.CANCASTLEOOO, 0);


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
            boolean nextToMove = tokens[1].toCharArray()[0] == 'w';
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


    public void initializeFromSquares(int[] input, boolean nextToMove, int fiftyMove, int castleWhiteSide, int castleBlackSide, int ePSquare) {
        clearBitboards();
        //setup the 12 boards
        for (int i = 0; i < 64; i++) {
            square[i] = input[i];
            switch (i) {
                case BoardUtils.WHITE_KING:
                    whiteKing |= BoardUtils.BITSET[i];
                    break;
                case BoardUtils.WHITE_QUEEN:
                    whiteQueens |= BoardUtils.BITSET[i];
                    break;
                case BoardUtils.WHITE_ROOK:
                    whiteRooks |= BoardUtils.BITSET[i];
                    break;
                case BoardUtils.WHITE_BISHOP:
                    whiteBishops |= BoardUtils.BITSET[i];
                    break;
                case BoardUtils.WHITE_KNIGHT:
                    whiteKnights |= BoardUtils.BITSET[i];
                    break;
                case BoardUtils.WHITE_PAWN:
                    whitePawns |= BoardUtils.BITSET[i];
                    break;
                case BoardUtils.BLACK_KING:
                    blackKing |= BoardUtils.BITSET[i];
                    break;
                case BoardUtils.BLACK_QUEEN:
                    blackQueens |= BoardUtils.BITSET[i];
                    break;
                case BoardUtils.BLACK_ROOK:
                    blackRooks |= BoardUtils.BITSET[i];
                    break;
                case BoardUtils.BLACK_BISHOP:
                    blackBishops |= BoardUtils.BITSET[i];
                    break;
                case BoardUtils.BLACK_KNIGHT:
                    blackKnights |= BoardUtils.BITSET[i];
                    break;
                case BoardUtils.BLACK_PAWN:
                    blackPawns |= BoardUtils.BITSET[i];
                    break;
            }


        }
        updateAggregateBitboards();

        whiteToMove = nextToMove;
        castleWhite = castleWhiteSide;
        castleBlack = castleBlackSide;
        this.ePSquare = ePSquare;
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

    public void doMove(Move move) {
        from = move.getFrom();
        to = move.getTo();
        piece = move.getPiece();
        captured = move.getCapture();
        fromBoard = BoardUtils.BITSET[from];
        fromToBoard = fromBoard | BoardUtils.BITSET[to];

        gameLine[endOfSearch].move.moveInt = move.moveInt;
        gameLine[endOfSearch].castleWhite = castleWhite;
        gameLine[endOfSearch].castleBlack = castleBlack;
        gameLine[endOfSearch].fiftyMove = fiftyMove;
        gameLine[endOfSearch].ePSquare = ePSquare;
        endOfSearch++;
        switch (piece) {
            case 1:
                makeWhitePawnMove(move);
                break;
            case 2:
                makeWhiteKingMove(move);
                break;
            case 3:
                makeWhiteKnightMove();
                break;
            case 5:
                makeWhiteBishopMove();
                break;
            case 6:
                makeWhiteRookMove();
                break;
            case 7:
                makeWhiteQueenMove();
                break;
            case 9:
                makeBlackPawnMove(move);
                break;
            case 10:
                makeBlackKingMove(move);
                break;
            case 11:
                makeBlackKnightMove();
                break;
            case 13:
                makeBlackBishopMove();
                break;
            case 14:
                makeBlackRookMove();
                break;
            case 15:
                makeBlackQueenMove();
                break;
            default:
                throw new RuntimeException("Unreachable");
        }
        whiteToMove = !whiteToMove;
    }

    public void undoMove(Move move) {
        piece = move.getPiece();
        captured = move.getCapture();
        from = move.getFrom();
        to = move.getTo();
        fromBoard = BoardUtils.BITSET[from];
        fromToBoard = fromBoard | BoardUtils.BITSET[to];

        switch (piece) {
            case 1:
                unmakeWhitePawnMove(move);
                break;
            case 2:
                unmakeWhiteKingMove(move);
                break;
            case 3:
                unmakeWhiteKnightMove();
                break;
            case 5:
                unmakeWhiteBishopMove();
                break;
            case 6:
                unmakeWhiteRookMove();
                break;
            case 7:
                unmakeWhiteQueenMove();
                break;
            case 9:
                unmakeBlackPawnMove(move);
                break;
            case 10:
                unmakeBlackKingMove(move);
                break;
            case 11:
                unmakeBlackKnightMove();
                break;
            case 13:
                unmakeBlackBishopMove();
                break;
            case 14:
                unmakeBlackRookMove();
                break;
            case 15:
                unmakeBlackQueenMove();
                break;
            default:
                throw new RuntimeException("Unreachable");
        }
        endOfSearch--;
        castleWhite = gameLine[endOfSearch].castleWhite;
        castleBlack = gameLine[endOfSearch].castleBlack;
        ePSquare = gameLine[endOfSearch].ePSquare;
        fiftyMove = gameLine[endOfSearch].fiftyMove;
        whiteToMove = !whiteToMove;
    }

    private void makeWhitePawnMove(Move move) {
        whitePawns ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.WHITE_PAWN;
        ePSquare = 0;
        fiftyMove = 0;
        if (BoardUtils.RANKS[from] == 1)
            if (BoardUtils.RANKS[to] == 3)
                ePSquare = from + 8;
        if (captured != 0) {
            if (move.isEnPassant()) {
                blackPawns ^= BoardUtils.BITSET[to - 8];
                blackPieces ^= BoardUtils.BITSET[to - 8];
                allPieces ^= fromToBoard | BoardUtils.BITSET[to - 8];
                square[to - 8] = BoardUtils.EMPTY;
                material += BoardUtils.PAWN_VALUE;
            } else {
                makeCapture(captured, to);
                allPieces ^= fromBoard;
            }
        } else {
            allPieces ^= fromToBoard;
        }
        if (move.isPromotion()) {
            makeWhitePromotion(move.getPromotion(), to);
            square[to] = move.getPromotion();
        }
    }

    private void makeWhiteKingMove(Move move) {
        whiteKing ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.WHITE_KING;
        ePSquare = 0;
        fiftyMove++;
        castleWhite = 0;
        if (captured != 0) {
            makeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
        if (move.isCastle()) {
            if (move.isCastleOO()) {
                whiteRooks ^= BoardUtils.BITSET[BoardUtils.H1] | BoardUtils.BITSET[BoardUtils.F1];
                whitePieces ^= BoardUtils.BITSET[BoardUtils.H1] | BoardUtils.BITSET[BoardUtils.F1];
                allPieces ^= BoardUtils.BITSET[BoardUtils.H1] | BoardUtils.BITSET[BoardUtils.F1];
                square[BoardUtils.H1] = BoardUtils.EMPTY;
                square[BoardUtils.F1] = BoardUtils.WHITE_ROOK;

            } else {
                whiteRooks ^= BoardUtils.BITSET[BoardUtils.A1] | BoardUtils.BITSET[BoardUtils.D1];
                whitePieces ^= BoardUtils.BITSET[BoardUtils.A1] | BoardUtils.BITSET[BoardUtils.D1];
                allPieces ^= BoardUtils.BITSET[BoardUtils.A1] | BoardUtils.BITSET[BoardUtils.D1];
                square[BoardUtils.A1] = BoardUtils.EMPTY;
                square[BoardUtils.D1] = BoardUtils.WHITE_ROOK;

            }

        }

    }

    private void makeWhiteKnightMove() {
        whiteKnights ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.WHITE_KNIGHT;
        ePSquare = 0;
        fiftyMove++;
        if (captured != 0) {
            makeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }

    }

    private void makeWhiteBishopMove() {
        whiteBishops ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.WHITE_BISHOP;
        ePSquare = 0;
        fiftyMove++;
        if (captured != 0) {
            makeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }

    }

    private void makeWhiteRookMove() {
        whiteRooks ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.WHITE_ROOK;
        ePSquare = 0;
        fiftyMove++;
        if (from == BoardUtils.A1)
            castleWhite &= ~CANCASTLEOOO;
        if (from == BoardUtils.H1)
            castleWhite &= ~CANCASTLEOO;

        if (captured != 0) {
            makeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
    }

    private void makeWhiteQueenMove() {
        whiteQueens ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.WHITE_QUEEN;
        ePSquare = 0;
        fiftyMove++;
        if (captured != 0) {
            makeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }

    }

    private void makeBlackPawnMove(Move move) {
        blackPawns ^= fromToBoard;
        blackPieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.BLACK_PAWN;
        ePSquare = 0;
        fiftyMove = 0;
        if (BoardUtils.RANKS[from] == 6)
            if (BoardUtils.RANKS[to] == 4)
                ePSquare = from - 8;
        if (captured != 0) {
            if (move.isEnPassant()) {
                whitePawns ^= BoardUtils.BITSET[to + 8];
                whitePieces ^= BoardUtils.BITSET[to + 8];
                allPieces ^= fromToBoard | BoardUtils.BITSET[to + 8];
                square[to + 8] = BoardUtils.EMPTY;
                material -= BoardUtils.PAWN_VALUE;
            } else {
                makeCapture(captured, to);
                allPieces ^= fromBoard;
            }
        } else {
            allPieces ^= fromToBoard;
        }
        if (move.isPromotion()) {
            makeBlackPromotion(move.getPromotion(), to);
            square[to] = move.getPromotion();
        }
    }


    private void makeBlackKingMove(Move move) {
        blackKing ^= fromToBoard;
        blackPieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.BLACK_KING;
        ePSquare = 0;
        fiftyMove++;
        castleWhite = 0;
        if (captured != 0) {
            makeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
        if (move.isCastle()) {
            if (move.isCastleOO()) {
                blackRooks ^= BoardUtils.BITSET[BoardUtils.H8] | BoardUtils.BITSET[BoardUtils.F8];
                blackPieces ^= BoardUtils.BITSET[BoardUtils.H8] | BoardUtils.BITSET[BoardUtils.F8];
                allPieces ^= BoardUtils.BITSET[BoardUtils.H8] | BoardUtils.BITSET[BoardUtils.F8];
                square[BoardUtils.H8] = BoardUtils.EMPTY;
                square[BoardUtils.F8] = BoardUtils.BLACK_ROOK;

            } else {
                blackRooks ^= BoardUtils.BITSET[BoardUtils.A8] | BoardUtils.BITSET[BoardUtils.D8];
                blackPieces ^= BoardUtils.BITSET[BoardUtils.A8] | BoardUtils.BITSET[BoardUtils.D8];
                allPieces ^= BoardUtils.BITSET[BoardUtils.A8] | BoardUtils.BITSET[BoardUtils.D8];
                square[BoardUtils.A8] = BoardUtils.EMPTY;
                square[BoardUtils.D8] = BoardUtils.BLACK_ROOK;

            }

        }
    }

    private void makeBlackKnightMove() {
        blackKnights ^= fromToBoard;
        blackPieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.BLACK_KNIGHT;
        ePSquare = 0;
        fiftyMove++;
        if (captured != 0) {
            makeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
    }

    private void makeBlackBishopMove() {
        blackBishops ^= fromToBoard;
        blackPieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.BLACK_BISHOP;
        ePSquare = 0;
        fiftyMove++;
        if (captured != 0) {
            makeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
    }

    private void makeBlackRookMove() {
        blackRooks ^= fromToBoard;
        blackPieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.BLACK_ROOK;
        ePSquare = 0;
        fiftyMove++;
        if (from == BoardUtils.A8)
            castleBlack &= ~CANCASTLEOOO;
        if (from == BoardUtils.H8)
            castleBlack &= ~CANCASTLEOO;

        if (captured != 0) {
            makeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
    }

    private void makeBlackQueenMove() {
        blackQueens ^= fromToBoard;
        blackPieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.BLACK_QUEEN;
        ePSquare = 0;
        fiftyMove++;
        if (captured != 0) {
            makeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
    }

    private void unmakeWhitePawnMove(Move move) {
        whitePawns ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.WHITE_PAWN;
        square[to] = BoardUtils.EMPTY;
        if (captured != 0) {
            if (move.isEnPassant()) {
                blackPawns ^= BoardUtils.BITSET[to - 8];
                blackPieces ^= BoardUtils.BITSET[to - 8];
                allPieces ^= fromToBoard | BoardUtils.BITSET[to - 8];
                square[to - 8] = BoardUtils.BLACK_PAWN;
                material -= BoardUtils.PAWN_VALUE;
            } else {
                unmakeCapture(captured, to);
                allPieces ^= fromBoard;
            }
        } else {
            allPieces ^= fromToBoard;
        }
        if (move.isPromotion()) {
            unmakeWhitePromotion(move.getPromotion(), to);
        }
    }

    private void unmakeWhiteKingMove(Move move) {
        whiteKing ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.WHITE_KING;
        square[to] = BoardUtils.EMPTY;
        if (captured != 0) {
            unmakeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }

        if (move.isCastle()) {
            if (move.isCastleOO()) {
                whiteRooks ^= BoardUtils.BITSET[BoardUtils.H1] | BoardUtils.BITSET[BoardUtils.F1];
                whitePieces ^= BoardUtils.BITSET[BoardUtils.H1] | BoardUtils.BITSET[BoardUtils.F1];
                allPieces ^= BoardUtils.BITSET[BoardUtils.H1] | BoardUtils.BITSET[BoardUtils.F1];
                square[BoardUtils.H1] = BoardUtils.WHITE_ROOK;
                square[BoardUtils.F1] = BoardUtils.EMPTY;
            } else {
                whiteRooks ^= BoardUtils.BITSET[BoardUtils.A1] | BoardUtils.BITSET[BoardUtils.D1];
                whitePieces ^= BoardUtils.BITSET[BoardUtils.A1] | BoardUtils.BITSET[BoardUtils.D1];
                allPieces ^= BoardUtils.BITSET[BoardUtils.A1] | BoardUtils.BITSET[BoardUtils.D1];
                square[BoardUtils.A1] = BoardUtils.WHITE_ROOK;
                square[BoardUtils.D1] = BoardUtils.EMPTY;
            }
        }

    }

    private void unmakeWhiteKnightMove() {
        whiteKnights ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.WHITE_KNIGHT;
        square[to] = BoardUtils.EMPTY;
        if (captured != 0) {
            unmakeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
    }

    private void unmakeWhiteBishopMove() {
        whiteBishops ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.WHITE_BISHOP;
        square[to] = BoardUtils.EMPTY;
        if (captured != 0) {
            unmakeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
    }

    private void unmakeWhiteRookMove() {
        whiteRooks ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.WHITE_ROOK;
        square[to] = BoardUtils.EMPTY;
        if (captured != 0) {
            unmakeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
    }

    private void unmakeWhiteQueenMove() {
        whiteQueens ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.WHITE_QUEEN;
        square[to] = BoardUtils.EMPTY;
        if (captured != 0) {
            unmakeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
    }

    private void unmakeBlackPawnMove(Move move) {
        blackPawns ^= fromToBoard;
        blackPieces ^= fromToBoard;
        square[from] = BoardUtils.BLACK_PAWN;
        square[to] = BoardUtils.EMPTY;
        if (captured != 0) {
            if (move.isEnPassant()) {
                whitePawns ^= BoardUtils.BITSET[to + 8];
                whitePieces ^= BoardUtils.BITSET[to + 8];
                allPieces ^= fromToBoard | BoardUtils.BITSET[to + 8];
                square[to + 8] = BoardUtils.EMPTY;
                material += BoardUtils.PAWN_VALUE;
            } else {
                unmakeCapture(captured, to);
                allPieces ^= fromBoard;
            }
        } else {
            allPieces ^= fromToBoard;
        }
        if (move.isPromotion()) {
            unmakeBlackPromotion(move.getPromotion(), to);
        }
    }

    private void unmakeBlackKingMove(Move move) {
        blackKing ^= fromToBoard;
        blackPieces ^= fromToBoard;
        square[from] = BoardUtils.BLACK_KING;
        square[to] = BoardUtils.EMPTY;
        if (captured != 0) {
            unmakeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }

        if (move.isCastle()) {
            if (move.isCastleOO()) {
                blackRooks ^= BoardUtils.BITSET[BoardUtils.H8] | BoardUtils.BITSET[BoardUtils.F8];
                blackPieces ^= BoardUtils.BITSET[BoardUtils.H8] | BoardUtils.BITSET[BoardUtils.F8];
                allPieces ^= BoardUtils.BITSET[BoardUtils.H8] | BoardUtils.BITSET[BoardUtils.F8];
                square[BoardUtils.H8] = BoardUtils.BLACK_ROOK;
                square[BoardUtils.F8] = BoardUtils.EMPTY;
            } else {
                blackRooks ^= BoardUtils.BITSET[BoardUtils.A8] | BoardUtils.BITSET[BoardUtils.D8];
                blackPieces ^= BoardUtils.BITSET[BoardUtils.A8] | BoardUtils.BITSET[BoardUtils.D8];
                allPieces ^= BoardUtils.BITSET[BoardUtils.A8] | BoardUtils.BITSET[BoardUtils.D8];
                square[BoardUtils.A8] = BoardUtils.BLACK_ROOK;
                square[BoardUtils.D8] = BoardUtils.EMPTY;
            }
        }
    }

    private void unmakeBlackKnightMove() {
        blackKnights ^= fromToBoard;
        blackPieces ^= fromToBoard;
        square[from] = BoardUtils.BLACK_KNIGHT;
        square[to] = BoardUtils.EMPTY;
        if (captured != 0) {
            unmakeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
    }

    private void unmakeBlackBishopMove() {
        blackBishops ^= fromToBoard;
        blackPieces ^= fromToBoard;
        square[from] = BoardUtils.BLACK_BISHOP;
        square[to] = BoardUtils.EMPTY;
        if (captured != 0) {
            unmakeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }

    }

    private void unmakeBlackRookMove() {
        blackRooks ^= fromToBoard;
        blackPieces ^= fromToBoard;
        square[from] = BoardUtils.BLACK_ROOK;
        square[to] = BoardUtils.EMPTY;
        if (captured != 0) {
            unmakeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
    }

    private void unmakeBlackQueenMove() {
        blackQueens ^= fromToBoard;
        blackPieces ^= fromToBoard;
        square[from] = BoardUtils.BLACK_QUEEN;
        square[to] = BoardUtils.EMPTY;
        if (captured != 0) {
            unmakeCapture(captured, to);
            allPieces ^= fromBoard;
        } else {
            allPieces ^= fromToBoard;
        }
    }

    private void makeWhitePromotion(int promotion, int to) {
        toBoard = BoardUtils.BITSET[to];
        whitePawns ^= toBoard;
        material -= BoardUtils.PAWN_VALUE;

        if (promotion == 7) {
            whiteQueens ^= toBoard;
            material += BoardUtils.QUEEN_VALUE;
        } else if (promotion == 6) {
            whiteRooks ^= toBoard;
            material += BoardUtils.ROOK_VALUE;
        } else if (promotion == 5) {
            whiteBishops ^= toBoard;
            material += BoardUtils.BISHOP_VALUE;
        } else if (promotion == 3) {
            whiteKnights ^= toBoard;
            material += BoardUtils.KNIGHT_VALUE;
        }
    }

    private void makeBlackPromotion(int promotion, int to) {

    }

    private void unmakeWhitePromotion(int promotion, int to) {
        toBoard = BoardUtils.BITSET[to];
        whitePawns ^= toBoard;
        material += BoardUtils.PAWN_VALUE;
        if (promotion == 7) {
            whiteQueens ^= toBoard;
            material -= BoardUtils.QUEEN_VALUE;
        } else if (promotion == 6) {
            whiteRooks ^= toBoard;
            material -= BoardUtils.ROOK_VALUE;
        } else if (promotion == 5) {
            whiteBishops ^= toBoard;
            material -= BoardUtils.BISHOP_VALUE;
        } else if (promotion == 3) {
            whiteKnights ^= toBoard;
            material -= BoardUtils.KNIGHT_VALUE;
        }

    }

    private void unmakeBlackPromotion(int promotion, int to) {

    }


    private void makeCapture(int captured, int to) {
        toBoard = BoardUtils.BITSET[to];
        switch (captured) {
            case 1:
                whitePawns ^= toBoard;
                whitePieces ^= toBoard;
                material -= BoardUtils.PAWN_VALUE;
                break;
            case 2:
                whiteKing ^= toBoard;
                whitePieces ^= toBoard;
                break;
            case 3:
                whiteKnights ^= toBoard;
                whitePieces ^= toBoard;
                material -= BoardUtils.KNIGHT_VALUE;
                break;
            case 5:
                whiteBishops ^= toBoard;
                whitePieces ^= toBoard;
                material -= BoardUtils.BISHOP_VALUE;
                break;
            case 6:
                whiteRooks ^= toBoard;
                whitePieces ^= toBoard;
                material -= BoardUtils.ROOK_VALUE;
                if (to == BoardUtils.A1)
                    castleWhite &= ~CANCASTLEOOO;
                if (to == BoardUtils.H1)
                    castleWhite &= ~CANCASTLEOO;
                break;
            case 7:
                whiteQueens ^= toBoard;
                whitePieces ^= toBoard;
                material -= BoardUtils.QUEEN_VALUE;
                break;
            case 9:
                blackPawns ^= toBoard;
                blackPieces ^= toBoard;
                material += BoardUtils.PAWN_VALUE;
                break;
            case 10:
                blackKing ^= toBoard;
                blackPieces ^= toBoard;
                break;
            case 11:
                blackKnights ^= toBoard;
                blackPieces ^= toBoard;
                material += BoardUtils.KNIGHT_VALUE;
                break;
            case 13:
                blackBishops ^= toBoard;
                blackPieces ^= toBoard;
                material += BoardUtils.BISHOP_VALUE;
                break;
            case 14:
                blackRooks ^= toBoard;
                blackPieces ^= toBoard;
                material += BoardUtils.ROOK_VALUE;
                if (to == BoardUtils.A8)
                    castleBlack &= ~CANCASTLEOOO;
                if (to == BoardUtils.H8)
                    castleBlack &= ~CANCASTLEOO;
                break;
            case 15:
                blackQueens ^= toBoard;
                blackPieces ^= toBoard;
                material += BoardUtils.QUEEN_VALUE;
                break;
            default:
                throw new RuntimeException("Unreachable");
        }
        fiftyMove = 0;
    }

    private void unmakeCapture(int captured, int to) {
        toBoard = BoardUtils.BITSET[to];
        switch (captured) {

            case 1:
                whitePawns ^= toBoard;
                whitePieces ^= toBoard;
                square[to] = BoardUtils.WHITE_PAWN;
                material += BoardUtils.PAWN_VALUE;
                break;
            case 2:
                whiteKing ^= toBoard;
                whitePieces ^= toBoard;
                square[to] = BoardUtils.WHITE_KING;
                break;
            case 3:
                whiteKnights ^= toBoard;
                whitePieces ^= toBoard;
                square[to] = BoardUtils.WHITE_KNIGHT;
                material += BoardUtils.KNIGHT_VALUE;
                break;
            case 5:
                whiteBishops ^= toBoard;
                whitePieces ^= toBoard;
                square[to] = BoardUtils.WHITE_BISHOP;
                material += BoardUtils.BISHOP_VALUE;
                break;
            case 6:
                whiteRooks ^= toBoard;
                whitePieces ^= toBoard;
                square[to] = BoardUtils.WHITE_ROOK;
                material += BoardUtils.ROOK_VALUE;
                break;
            case 7:
                whiteQueens ^= toBoard;
                whitePieces ^= toBoard;
                square[to] = BoardUtils.WHITE_QUEEN;
                material += BoardUtils.QUEEN_VALUE;
                break;
            case 9:
                blackPawns ^= toBoard;
                blackPieces ^= toBoard;
                square[to] = BoardUtils.BLACK_PAWN;
                material -= BoardUtils.PAWN_VALUE;
                break;
            case 10:
                blackKing ^= toBoard;
                blackPieces ^= toBoard;
                square[to] = BoardUtils.BLACK_KING;
                break;
            case 11:
                blackKnights ^= toBoard;
                blackPieces ^= toBoard;
                square[to] = BoardUtils.BLACK_KNIGHT;
                material -= BoardUtils.KNIGHT_VALUE;
                break;
            case 13:
                blackBishops ^= toBoard;
                blackPieces ^= toBoard;
                square[to] = BoardUtils.BLACK_BISHOP;
                material -= BoardUtils.BISHOP_VALUE;
                break;
            case 14:
                blackRooks ^= toBoard;
                blackPieces ^= toBoard;
                square[to] = BoardUtils.BLACK_ROOK;
                material -= BoardUtils.ROOK_VALUE;
                break;
            case 15:
                blackQueens ^= toBoard;
                blackPieces ^= toBoard;
                square[to] = BoardUtils.BLACK_QUEEN;
                material -= BoardUtils.QUEEN_VALUE;
                break;
            default:
                throw new RuntimeException("Unreachable");
        }
    }

    private class GameLineRecord {
        private Move move;
        private int castleWhite;
        private int castleBlack;
        private int ePSquare;
        private int fiftyMove;
    }


}
