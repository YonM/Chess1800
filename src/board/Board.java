package board;
import evaluation.Evaluator;
import fen.FENValidator;
import move.Move;
import movegen.MoveGenerator;
import search.AlphaBetaPVS;
import zobrist.Zobrist;

import javax.swing.*;

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

    public long key;

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
        key = 0;
        //setup the 12 boards
        for (int i = 0; i < 64; i++) {
            square[i] = input[i];
            switch (i) {
                case BoardUtils.WHITE_KING:
                    whiteKing |= BoardUtils.BITSET[i];
                    key ^= Zobrist.king[0][i];
                    break;
                case BoardUtils.WHITE_QUEEN:
                    whiteQueens |= BoardUtils.BITSET[i];
                    key ^= Zobrist.queen[0][i];
                    break;
                case BoardUtils.WHITE_ROOK:
                    whiteRooks |= BoardUtils.BITSET[i];
                    key ^= Zobrist.rook[0][i];
                    break;
                case BoardUtils.WHITE_BISHOP:
                    whiteBishops |= BoardUtils.BITSET[i];
                    key ^= Zobrist.bishop[0][i];
                    break;
                case BoardUtils.WHITE_KNIGHT:
                    whiteKnights |= BoardUtils.BITSET[i];
                    key ^= Zobrist.knight[0][i];
                    break;
                case BoardUtils.WHITE_PAWN:
                    whitePawns |= BoardUtils.BITSET[i];
                    key ^= Zobrist.pawn[0][i];
                    break;
                case BoardUtils.BLACK_KING:
                    blackKing |= BoardUtils.BITSET[i];
                    key ^= Zobrist.king[1][i];
                    break;
                case BoardUtils.BLACK_QUEEN:
                    blackQueens |= BoardUtils.BITSET[i];
                    key ^= Zobrist.queen[1][i];
                    break;
                case BoardUtils.BLACK_ROOK:
                    blackRooks |= BoardUtils.BITSET[i];
                    key ^= Zobrist.rook[1][i];
                    break;
                case BoardUtils.BLACK_BISHOP:
                    blackBishops |= BoardUtils.BITSET[i];
                    key ^= Zobrist.bishop[1][i];
                    break;
                case BoardUtils.BLACK_KNIGHT:
                    blackKnights |= BoardUtils.BITSET[i];
                    key ^= Zobrist.knight[1][i];
                    break;
                case BoardUtils.BLACK_PAWN:
                    blackPawns |= BoardUtils.BITSET[i];
                    key ^= Zobrist.pawn[1][i];
                    break;
            }


        }
        updateAggregateBitboards();

        whiteToMove = nextToMove;
        castleWhite = castleWhiteSide;
        castleBlack = castleBlackSide;
        this.ePSquare = ePSquare;
        this.fiftyMove = fiftyMove;

        if ((castleWhite & CANCASTLEOO) != 0) key ^= Zobrist.whiteKingSideCastling;
        if ((castleWhite & CANCASTLEOOO) != 0) key ^= Zobrist.whiteQueenSideCastling;
        if ((castleBlack & CANCASTLEOO) != 0) key ^= Zobrist.blackKingSideCastling;
        if ((castleBlack & CANCASTLEOOO) != 0) key ^= Zobrist.blackQueenSideCastling;

        if (whiteToMove)
            key ^= Zobrist.whiteMove;

        if (this.ePSquare != 0)
            key ^= Zobrist.passantColumn[BoardUtils.FILES[this.ePSquare]];

        material = Long.bitCount(whitePawns) * Evaluator.PAWN_VALUE + Long.bitCount(whiteKnights) * Evaluator.KNIGHT_VALUE
                + Long.bitCount(whiteBishops) * Evaluator.BISHOP_VALUE + Long.bitCount(whiteRooks) * Evaluator.ROOK_VALUE
                + Long.bitCount(whiteQueens) * Evaluator.QUEEN_VALUE;
        material -= (Long.bitCount(blackPawns) * Evaluator.PAWN_VALUE + Long.bitCount(blackKnights) * Evaluator.KNIGHT_VALUE
                + Long.bitCount(blackBishops) * Evaluator.BISHOP_VALUE + Long.bitCount(blackRooks) * Evaluator.ROOK_VALUE
                + Long.bitCount(blackQueens) * Evaluator.QUEEN_VALUE);

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

    public boolean isEndOfGame() {

        // Checks if the current position is end-of-game due to:
        // checkmate, stalemate, 50-move rule, or insufficient material
        // Is the other king checkmated?
        if (isOtherKingAttacked()) {
            if (whiteToMove) JOptionPane.showMessageDialog(null, "1-0 White checkmates");
            else JOptionPane.showMessageDialog(null, "1-0 Black checkmates");
            return true;
        }
        int i;
        AlphaBetaPVS.legalMoves = 0;
        moveBufLen[0] = 0;
        moveBufLen[1] = MoveGenerator.moveGen(moveBufLen[0]);
        for (i = moveBufLen[0]; i < moveBufLen[1]; i++) {
            makeMove(moves[i]);
            if (!isOtherKingAttacked()) {
                AlphaBetaPVS.legalMoves++;
                AlphaBetaPVS.singleMove = moves[i];
            }
            unmakeMove(moves[i]);
        }

        //Stalemate/Checkmate check.
        if (AlphaBetaPVS.legalMoves == 0) {
            if (isOwnKingAttacked()) {
                if (whiteToMove) JOptionPane.showMessageDialog(null, "1-0 Black checkmates");
                else JOptionPane.showMessageDialog(null, "1-0 White checkmates");
            } else JOptionPane.showMessageDialog(null, "0.5-0.5 Stalemate");
            return true;
        }

        int whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteTotalMat;
        int blackKnights, blackBishops, blackRooks, blackQueens, blackTotalMat;

        //Check if it's a draw due to insufficient material.
        if (whitePawns + blackPawns == 0) {
            whiteKnights = Long.bitCount(this.whiteKnights);
            whiteBishops = Long.bitCount(this.whiteBishops);
            whiteRooks = Long.bitCount(this.whiteRooks);
            whiteQueens = Long.bitCount(this.whiteQueens);
            whiteTotalMat = 3 * whiteKnights + 3 * whiteBishops + 5 * whiteRooks + 10 * whiteQueens;
            blackKnights = Long.bitCount(this.blackKnights);
            blackBishops = Long.bitCount(this.blackBishops);
            blackRooks = Long.bitCount(this.blackRooks);
            blackQueens = Long.bitCount(this.blackQueens);
            blackTotalMat = 3 * blackKnights + 3 * blackBishops + 5 * blackRooks + 10 * blackQueens;

            //King vs king.
            if (whiteTotalMat + blackTotalMat == 0) {
                JOptionPane.showMessageDialog(null, "0.5-0.5 Draw due to insufficient material.");
                return true;
            }

            //King and knight versus king.
            if (((whiteTotalMat == 3) && (whiteKnights == 1) && (blackTotalMat == 0) ||
                    ((blackTotalMat == 3)) && (blackKnights == 1) && (whiteTotalMat == 0))) {
                JOptionPane.showMessageDialog(null, "0.5-0.5 Draw due to insufficient material.");
                return true;
            }
            //Kings with one or more bishops, all bishops on the same colour.

            if (whiteBishops + blackBishops > 0) {
                if (whiteKnights + whiteRooks + whiteQueens + blackKnights + blackRooks + blackQueens == 0) {
                    if (((whiteBishops | blackBishops) & BoardUtils.WHITE_SQUARES) != 0
                            || ((whiteBishops | blackBishops) & BoardUtils.BLACK_SQUARES) != 0) {
                        JOptionPane.showMessageDialog(null, "0.5-0.5 Draw due to insufficient material.");
                        return true;
                    }
                }

            }
        }

        //50Move rule
        if (fiftyMove >= 100) {
            JOptionPane.showMessageDialog(null, "0.5-0.5 Draw due 50-move rule");
            return true;
        }

        //Three fold repetition
        if (repetitionCount() >= 3) {
            JOptionPane.showMessageDialog(null, "0.5 - 0.5 Draw due to repetition");
            return true;

        }
        return false;
    }

    public int repetitionCount() {
        int i, lastI, rep = 1; // current position is at least 1 repetition
        lastI = endOfSearch - fiftyMove;          // we don't need to go back all the way
        for (i = endOfSearch - 2; i >= lastI; i -= 2)   // Only search for current side, skip opposite side
        {
            if (gameLine[i].key == key) rep++;
            if (rep >= 3) return 3;

        }
        return rep;
    }
    public void makeMove(Move move) {
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
        gameLine[endOfSearch].key = key;
        key ^= Zobrist.getKeyPieceIndex(from, BoardUtils.PIECENAMES[piece]) ^ Zobrist.getKeyPieceIndex(to, BoardUtils.PIECENAMES[piece]);
        if (ePSquare != 0)
            key ^= Zobrist.passantColumn[BoardUtils.FILES[ePSquare]];
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
        endOfSearch++;
        whiteToMove = !whiteToMove;
        key ^= Zobrist.whiteMove;
    }

    public void unmakeMove(Move move) {
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
        whiteToMove = !whiteToMove;
        castleWhite = gameLine[endOfSearch].castleWhite;
        castleBlack = gameLine[endOfSearch].castleBlack;
        ePSquare = gameLine[endOfSearch].ePSquare;
        fiftyMove = gameLine[endOfSearch].fiftyMove;
        key ^= gameLine[endOfSearch].key;

    }

    private void makeWhitePawnMove(Move move) {
        whitePawns ^= fromToBoard;
        whitePieces ^= fromToBoard;
        square[from] = BoardUtils.EMPTY;
        square[to] = BoardUtils.WHITE_PAWN;
        ePSquare = 0;
        fiftyMove = 0;
        if (BoardUtils.RANKS[from] == 1)
            if (BoardUtils.RANKS[to] == 3) {
                ePSquare = from + 8;
                key ^= Zobrist.passantColumn[BoardUtils.FILES[from + 8]];
            }
        if (captured != 0) {
            if (move.isEnPassant()) {
                blackPawns ^= BoardUtils.BITSET[to - 8];
                blackPieces ^= BoardUtils.BITSET[to - 8];
                allPieces ^= fromToBoard | BoardUtils.BITSET[to - 8];
                square[to - 8] = BoardUtils.EMPTY;
                material += Evaluator.PAWN_VALUE;
                key ^= Zobrist.pawn[1][to - 8];
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
        if ((castleWhite & CANCASTLEOO) != 0) key ^= Zobrist.whiteKingSideCastling;
        if ((castleWhite & CANCASTLEOOO) != 0) key ^= Zobrist.whiteQueenSideCastling;
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
                key ^= Zobrist.rook[0][BoardUtils.H1] ^ Zobrist.rook[0][BoardUtils.F1];
            } else {
                whiteRooks ^= BoardUtils.BITSET[BoardUtils.A1] | BoardUtils.BITSET[BoardUtils.D1];
                whitePieces ^= BoardUtils.BITSET[BoardUtils.A1] | BoardUtils.BITSET[BoardUtils.D1];
                allPieces ^= BoardUtils.BITSET[BoardUtils.A1] | BoardUtils.BITSET[BoardUtils.D1];
                square[BoardUtils.A1] = BoardUtils.EMPTY;
                square[BoardUtils.D1] = BoardUtils.WHITE_ROOK;
                key ^= Zobrist.rook[0][BoardUtils.A1] ^ Zobrist.rook[0][BoardUtils.D1];
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
        if (from == BoardUtils.A1) {
            if ((castleWhite & CANCASTLEOOO) != 0) key ^= Zobrist.whiteQueenSideCastling;
            castleWhite &= ~CANCASTLEOOO;
        }
        if (from == BoardUtils.H1) {
            if ((castleWhite & CANCASTLEOO) != 0) key ^= Zobrist.whiteKingSideCastling;
            castleWhite &= ~CANCASTLEOO;
        }
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
            if (BoardUtils.RANKS[to] == 4) {
                ePSquare = from - 8;
                key ^= Zobrist.passantColumn[BoardUtils.FILES[from - 8]];
            }
        if (captured != 0) {
            if (move.isEnPassant()) {
                whitePawns ^= BoardUtils.BITSET[to + 8];
                whitePieces ^= BoardUtils.BITSET[to + 8];
                allPieces ^= fromToBoard | BoardUtils.BITSET[to + 8];
                square[to + 8] = BoardUtils.EMPTY;
                material -= Evaluator.PAWN_VALUE;
                key ^= Zobrist.pawn[0][to + 8];
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
        if ((castleBlack & CANCASTLEOO) != 0) key ^= Zobrist.blackKingSideCastling;
        if ((castleBlack & CANCASTLEOOO) != 0) key ^= Zobrist.blackQueenSideCastling;
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
                key ^= Zobrist.rook[1][BoardUtils.H8] ^ Zobrist.rook[1][BoardUtils.F8];
            } else {
                blackRooks ^= BoardUtils.BITSET[BoardUtils.A8] | BoardUtils.BITSET[BoardUtils.D8];
                blackPieces ^= BoardUtils.BITSET[BoardUtils.A8] | BoardUtils.BITSET[BoardUtils.D8];
                allPieces ^= BoardUtils.BITSET[BoardUtils.A8] | BoardUtils.BITSET[BoardUtils.D8];
                square[BoardUtils.A8] = BoardUtils.EMPTY;
                square[BoardUtils.D8] = BoardUtils.BLACK_ROOK;
                key ^= Zobrist.rook[1][BoardUtils.A8] ^ Zobrist.rook[1][BoardUtils.D8];
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
        if (from == BoardUtils.A8) {
            if ((castleBlack & CANCASTLEOOO) != 0) key ^= Zobrist.blackQueenSideCastling;
            castleBlack &= ~CANCASTLEOOO;
        }
        if (from == BoardUtils.H8) {
            if ((castleBlack & CANCASTLEOOO) != 0) key ^= Zobrist.blackKingSideCastling;
            castleBlack &= ~CANCASTLEOO;
        }
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
                material -= Evaluator.PAWN_VALUE;
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
                material += Evaluator.PAWN_VALUE;
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
        material -= Evaluator.PAWN_VALUE;

        if (promotion == 7) {
            key ^= Zobrist.pawn[0][to] ^ Zobrist.queen[0][to];
            whiteQueens ^= toBoard;
            material += Evaluator.QUEEN_VALUE;
        } else if (promotion == 6) {
            key ^= Zobrist.pawn[0][to] ^ Zobrist.rook[0][to];
            whiteRooks ^= toBoard;
            material += Evaluator.ROOK_VALUE;
        } else if (promotion == 5) {
            key ^= Zobrist.pawn[0][to] ^ Zobrist.bishop[0][to];
            whiteBishops ^= toBoard;
            material += Evaluator.BISHOP_VALUE;
        } else if (promotion == 3) {
            key ^= Zobrist.pawn[0][to] ^ Zobrist.knight[0][to];
            whiteKnights ^= toBoard;
            material += Evaluator.KNIGHT_VALUE;
        }
    }

    private void makeBlackPromotion(int promotion, int to) {
        toBoard = BoardUtils.BITSET[to];
        blackPawns ^= toBoard;
        material -= Evaluator.PAWN_VALUE;

        if (promotion == 15) {
            key ^= Zobrist.pawn[1][to] ^ Zobrist.queen[1][to];
            blackQueens ^= toBoard;
            material -= Evaluator.QUEEN_VALUE;
        } else if (promotion == 14) {
            key ^= Zobrist.pawn[1][to] ^ Zobrist.rook[1][to];
            blackRooks ^= toBoard;
            material -= Evaluator.ROOK_VALUE;
        } else if (promotion == 13) {
            key ^= Zobrist.pawn[1][to] ^ Zobrist.bishop[1][to];
            blackBishops ^= toBoard;
            material -= Evaluator.BISHOP_VALUE;
        } else if (promotion == 11) {
            key ^= Zobrist.pawn[1][to] ^ Zobrist.knight[1][to];
            blackKnights ^= toBoard;
            material -= Evaluator.KNIGHT_VALUE;
        }

    }

    private void unmakeWhitePromotion(int promotion, int to) {
        toBoard = BoardUtils.BITSET[to];
        whitePawns ^= toBoard;
        material += Evaluator.PAWN_VALUE;
        if (promotion == 7) {
            whiteQueens ^= toBoard;
            material -= Evaluator.QUEEN_VALUE;
        } else if (promotion == 6) {
            whiteRooks ^= toBoard;
            material -= Evaluator.ROOK_VALUE;
        } else if (promotion == 5) {
            whiteBishops ^= toBoard;
            material -= Evaluator.BISHOP_VALUE;
        } else if (promotion == 3) {
            whiteKnights ^= toBoard;
            material -= Evaluator.KNIGHT_VALUE;
        }
    }

    private void unmakeBlackPromotion(int promotion, int to) {
        toBoard = BoardUtils.BITSET[to];
        blackPawns ^= toBoard;
        material += Evaluator.PAWN_VALUE;
        if (promotion == 15) {
            blackQueens ^= toBoard;
            material += Evaluator.QUEEN_VALUE;
        } else if (promotion == 14) {
            blackRooks ^= toBoard;
            material += Evaluator.ROOK_VALUE;
        } else if (promotion == 13) {
            blackBishops ^= toBoard;
            material += Evaluator.BISHOP_VALUE;
        } else if (promotion == 11) {
            blackKnights ^= toBoard;
            material += Evaluator.KNIGHT_VALUE;
        }
    }

    private void makeCapture(int captured, int to) {
        toBoard = BoardUtils.BITSET[to];
        switch (captured) {
            case 1:
                key ^= Zobrist.pawn[0][to];
                whitePawns ^= toBoard;
                whitePieces ^= toBoard;
                material -= Evaluator.PAWN_VALUE;
                break;
            case 2:
                key ^= Zobrist.king[0][to];
                whiteKing ^= toBoard;
                whitePieces ^= toBoard;
                break;
            case 3:
                key ^= Zobrist.knight[0][to];
                whiteKnights ^= toBoard;
                whitePieces ^= toBoard;
                material -= Evaluator.KNIGHT_VALUE;
                break;
            case 5:
                key ^= Zobrist.bishop[0][to];
                whiteBishops ^= toBoard;
                whitePieces ^= toBoard;
                material -= Evaluator.BISHOP_VALUE;
                break;
            case 6:
                key ^= Zobrist.rook[0][to];
                whiteRooks ^= toBoard;
                whitePieces ^= toBoard;
                material -= Evaluator.ROOK_VALUE;
                if (to == BoardUtils.A1)
                    castleWhite &= ~CANCASTLEOOO;
                if (to == BoardUtils.H1)
                    castleWhite &= ~CANCASTLEOO;
                break;
            case 7:
                key ^= Zobrist.queen[0][to];
                whiteQueens ^= toBoard;
                whitePieces ^= toBoard;
                material -= Evaluator.QUEEN_VALUE;
                break;
            case 9:
                key ^= Zobrist.pawn[1][to];
                blackPawns ^= toBoard;
                blackPieces ^= toBoard;
                material += Evaluator.PAWN_VALUE;
                break;
            case 10:
                key ^= Zobrist.king[1][to];
                blackKing ^= toBoard;
                blackPieces ^= toBoard;
                break;
            case 11:
                key ^= Zobrist.knight[1][to];
                blackKnights ^= toBoard;
                blackPieces ^= toBoard;
                material += Evaluator.KNIGHT_VALUE;
                break;
            case 13:
                key ^= Zobrist.bishop[1][to];
                blackBishops ^= toBoard;
                blackPieces ^= toBoard;
                material += Evaluator.BISHOP_VALUE;
                break;
            case 14:
                key ^= Zobrist.rook[1][to];
                blackRooks ^= toBoard;
                blackPieces ^= toBoard;
                material += Evaluator.ROOK_VALUE;
                if (to == BoardUtils.A8)
                    castleBlack &= ~CANCASTLEOOO;
                if (to == BoardUtils.H8)
                    castleBlack &= ~CANCASTLEOO;
                break;
            case 15:
                key ^= Zobrist.queen[1][to];
                blackQueens ^= toBoard;
                blackPieces ^= toBoard;
                material += Evaluator.QUEEN_VALUE;
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
                material += Evaluator.PAWN_VALUE;
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
                material += Evaluator.KNIGHT_VALUE;
                break;
            case 5:
                whiteBishops ^= toBoard;
                whitePieces ^= toBoard;
                square[to] = BoardUtils.WHITE_BISHOP;
                material += Evaluator.BISHOP_VALUE;
                break;
            case 6:
                whiteRooks ^= toBoard;
                whitePieces ^= toBoard;
                square[to] = BoardUtils.WHITE_ROOK;
                material += Evaluator.ROOK_VALUE;
                break;
            case 7:
                whiteQueens ^= toBoard;
                whitePieces ^= toBoard;
                square[to] = BoardUtils.WHITE_QUEEN;
                material += Evaluator.QUEEN_VALUE;
                break;
            case 9:
                blackPawns ^= toBoard;
                blackPieces ^= toBoard;
                square[to] = BoardUtils.BLACK_PAWN;
                material -= Evaluator.PAWN_VALUE;
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
                material -= Evaluator.KNIGHT_VALUE;
                break;
            case 13:
                blackBishops ^= toBoard;
                blackPieces ^= toBoard;
                square[to] = BoardUtils.BLACK_BISHOP;
                material -= Evaluator.BISHOP_VALUE;
                break;
            case 14:
                blackRooks ^= toBoard;
                blackPieces ^= toBoard;
                square[to] = BoardUtils.BLACK_ROOK;
                material -= Evaluator.ROOK_VALUE;
                break;
            case 15:
                blackQueens ^= toBoard;
                blackPieces ^= toBoard;
                square[to] = BoardUtils.BLACK_QUEEN;
                material -= Evaluator.QUEEN_VALUE;
                break;
            default:
                throw new RuntimeException("Unreachable");
        }
    }

    public boolean isOtherKingAttacked() {
        if (whiteToMove) return MoveGenerator.isAttacked(blackKing, whiteToMove);
        return MoveGenerator.isAttacked(whiteKing, whiteToMove);

    }

    public boolean isOwnKingAttacked() {
        if (whiteToMove) return MoveGenerator.isAttacked(whiteKing, !whiteToMove);
        return MoveGenerator.isAttacked(blackKing, !whiteToMove);
    }

    private class GameLineRecord {
        private Move move;
        private int castleWhite;
        private int castleBlack;
        private int ePSquare;
        private int fiftyMove;
        private long key;
    }


}
