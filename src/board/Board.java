package board;

import bitboard.BitboardMagicAttacksAC;
import bitboard.BitboardUtilsAC;
import definitions.Definitions;
import fen.FENValidator;
import move.MoveAC;
import movegen.MoveGeneratorAC;
import search.Search;
import zobrist.Zobrist;

import javax.swing.*;

/**
 * Created by Yonathan on 08/12/2014.
 * Singleton class to represent the board.
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 * and Ulysse Carion's Godot @ https://github.com/ucarion/godot
 * and Alberto Alonso Ruibal's Carballo @ https://github.com/albertoruibal/carballo
 */
public class Board implements Definitions {
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
    public int moveNumber;

    public int initMoveNumber;


    public int castleWhite;
    public int castleBlack;



    public int[] moves;
    private int num_moves;

    //History attributes
    public long[] white_pawn_history;
    public long[] white_knight_history;
    public long[] white_bishop_history;
    public long[] white_rook_history;
    public long[] white_queen_history;
    public long[] white_king_history;
    public long[] black_pawn_history;
    public long[] black_knight_history;
    public long[] black_bishop_history;
    public long[] black_rook_history;
    public long[] black_queen_history;
    public long[] black_king_history;
    public long[] white_pieces_history;
    public long[] black_pieces_history;
    public long[] all_pieces_history;
    public boolean[] whiteToMove_history;
    public int[] fiftyMoveRule_history;
    public int[] enPassant_history;
    public int[] move_history;
    public int[] white_castle_history;
    public int[] black_castle_history;
    public long[] key_history;

    public int endOfGame, endOfSearch; // Index for board.gameLine
    //public final GameLineRecord[] gameLine = new GameLineRecord[MAX_GAME_LENGTH]; // Current search line + moves that have actually been played.

    //For (un)make move
    private int from, to, piece, moveType;
    private long fromBoard, toBoard, fromToBoard;
    private boolean capture;

    //public boolean viewRotated;
    private static Board instance;

    public long key; //Zobrist key

    private static final int[] SEE_PIECE_VALUES = {0, 100, 325, 325, 500, 975, 999999};
    private int[] seeGain;

    public int totalWhitePawns;
    public int totalBlackPawns;
    public int totalWhitePieces;
    public int totalBlackPieces;

    public Board() {
        moves = new int[MAX_GAME_LENGTH * 4];
        for (int i = 0; i < moves.length; i++) {
            moves[i] = 0;
        }
        seeGain = new int[32];

        white_pawn_history = new long[MAX_GAME_LENGTH];
        white_knight_history = new long[MAX_GAME_LENGTH];
        white_bishop_history = new long[MAX_GAME_LENGTH];
        white_rook_history = new long[MAX_GAME_LENGTH];
        white_queen_history = new long[MAX_GAME_LENGTH];
        white_king_history = new long[MAX_GAME_LENGTH];
        black_pawn_history = new long[MAX_GAME_LENGTH];
        black_knight_history = new long[MAX_GAME_LENGTH];
        black_bishop_history = new long[MAX_GAME_LENGTH];
        black_rook_history = new long[MAX_GAME_LENGTH];
        black_queen_history = new long[MAX_GAME_LENGTH];
        black_king_history = new long[MAX_GAME_LENGTH];
        white_pieces_history = new long[MAX_GAME_LENGTH];
        black_pieces_history = new long[MAX_GAME_LENGTH];
        all_pieces_history = new long[MAX_GAME_LENGTH];
        whiteToMove_history = new boolean[MAX_GAME_LENGTH];
        fiftyMoveRule_history = new int[MAX_GAME_LENGTH];
        enPassant_history = new int[MAX_GAME_LENGTH];
        move_history = new int[MAX_GAME_LENGTH];
        white_castle_history = new int[MAX_GAME_LENGTH];
        black_castle_history = new int[MAX_GAME_LENGTH];
        key_history = new long[MAX_GAME_LENGTH];
    }

    public void initialize() {
//        setupBoard();
        initializeFromFEN(START_FEN);

    }

    public boolean initializeFromFEN(String fen) {
        if (FENValidator.isValidFEN(fen)) {
//            StringTokenizer st = new StringTokenizer(fen, "/ ");
//            ArrayList<String> arr = new ArrayList<String>();
//
//            while (st.hasMoreTokens()) {
//                arr.add(st.nextToken());
//            }
            //int[] fenSquares = new int[64];
            clearBitboards();
            String[] tokens = fen.split("[ \\t\\n\\x0B\\f\\r]+");
            String board = tokens[0];
            int rank = 7;
            int file = 0;
            for (char c : board.toCharArray()) {
                if (Character.isDigit(c)) {
                    for (int j = 0; j < Character.digit(c, 10); j++) file++;

                } else {
                    long square = BitboardUtilsAC.getSquare[rank * 8 + file];
                    switch (c) {
                        case '/':
                            rank--;
                            file = 0;
                            break;
                        case 'P':
                            whitePawns |= square;
                            break;
                        case 'N':
                            whiteKnights |= square;
                            break;
                        case 'B':
                            whiteBishops |= square;
                            break;
                        case 'R':
                            whiteRooks |= square;
                            break;
                        case 'Q':
                            whiteQueens |= square;
                            break;
                        case 'K':
                            whiteKing |= square;
                            break;
                        case 'p':
                            blackPawns |= square;
                            break;
                        case 'n':
                            blackKnights |= square;
                            break;
                        case 'b':
                            blackBishops |= square;
                            break;
                        case 'r':
                            blackRooks |= square;
                            break;
                        case 'q':
                            blackQueens |= square;
                            break;
                        case 'k':
                            blackKing |= square;
                            break;
                    }
                    if (c != '/') file++;
                }
            }
            updateAggregateBitboards();

            whiteToMove = tokens[1].toCharArray()[0] == 'w';
            castleWhite = 0;
            castleBlack = 0;
            ePSquare = -1;
            fiftyMove = 0;
            moveNumber = 1;
            //For castling
            if (tokens.length > 2) {
                String castleInfo = tokens[2];
                if (castleInfo.contains("K")) {
                    castleWhite += CANCASTLEOO;
                }
                if (castleInfo.contains("Q")) {
                    castleWhite += CANCASTLEOOO;
                }
                if (castleInfo.contains("k")) {
                    castleBlack += CANCASTLEOO;
                }
                if (castleInfo.contains("q")) {
                    castleBlack += CANCASTLEOOO;
                }
                if (tokens.length > 3) {
                    char[] enPassant = tokens[3].toCharArray();

                    if (enPassant[0] != ('-')) {
                        ePSquare = BoardUtils.getIndex(enPassant[0] - 96, enPassant[1]);
                    }
                }
                if (tokens.length > 4) {
                    try {
                        fiftyMove = Integer.parseInt(tokens[4]);
                    } catch (Exception ignore) {

                    }
                }
                if (tokens.length > 5) {
                    try {
                        moveNumber = Integer.parseInt(tokens[5]);
                        initMoveNumber = moveNumber;
                    } catch (Exception ignore) {

                    }
                }

            }
            key = Zobrist.getKeyFromBoard(this);

            material = Long.bitCount(whitePawns) * PAWN_VALUE + Long.bitCount(whiteKnights) * KNIGHT_VALUE
                    + Long.bitCount(whiteBishops) * BISHOP_VALUE + Long.bitCount(whiteRooks) * ROOK_VALUE
                    + Long.bitCount(whiteQueens) * QUEEN_VALUE;
            material -= (Long.bitCount(blackPawns) * PAWN_VALUE + Long.bitCount(blackKnights) * KNIGHT_VALUE
                    + Long.bitCount(blackBishops) * BISHOP_VALUE + Long.bitCount(blackRooks) * ROOK_VALUE
                    + Long.bitCount(blackQueens) * QUEEN_VALUE);
            /*for(int s: fenSquares)
            System.out.println(s);
            System.out.println("Turn: "+ nextToMove);
            System.out.println("50-Move: "+temp50Move);
            System.out.println("White castling: " +tempWhiteCastle);
            System.out.println("Black castling: " + tempBlackCastle);
            System.out.println("ePSquare: " + ePSquare);*/
            //initializeFromSquares(fenSquares, nextToMove, temp50Move, tempWhiteCastle, tempBlackCastle, ePSquare);
            return true;
        } else {
            //if fen is not valid
            return false;
        }


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
        Search.legalMoves = 0;
        moves = new int[MAX_MOVES];
        num_moves = MoveGeneratorAC.getAllMoves(this, moves);
        for (i = 0; i < num_moves; i++) {
            makeMove(moves[i]);
            if (!isOtherKingAttacked()) {
                Search.legalMoves++;
                Search.singleMove = moves[i];
            }
            unmakeMove();
        }

        //Stalemate/Checkmate check.
        if (Search.legalMoves == 0) {
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
            if (key_history[i] == key) rep++;
            if (rep >= 3) return 3;

        }
        return rep;
    }

    /**
     * Gets what is found at a particular location.
     *
     * @param loc an integer in [0, 64) representing a position.
     * @return a character representing the piece at the passed location.
     */
    public char getPieceAt(int loc) {
        long sq = BitboardUtilsAC.getSquare[loc];
        if ((whitePawns & sq) != 0L)
            return 'P';
        if ((whiteKnights & sq) != 0L)
            return 'N';
        if ((whiteBishops & sq) != 0L)
            return 'B';
        if ((whiteRooks & sq) != 0L)
            return 'R';
        if ((whiteQueens & sq) != 0L)
            return 'Q';
        if ((whiteKing & sq) != 0L)
            return 'K';
        if ((blackPawns & sq) != 0L)
            return 'p';
        if ((blackKnights & sq) != 0L)
            return 'n';
        if ((blackBishops & sq) != 0L)
            return 'b';
        if ((blackRooks & sq) != 0L)
            return 'r';
        if ((blackQueens & sq) != 0L)
            return 'q';
        if ((blackKing & sq) != 0L)
            return 'k';
        return ' ';
    }

    public boolean makeMove(int move) {
       /*
        * General plan of attack is as follows:
        *
        * (0) First of all, check if you're moving your own piece.
        * (1) Account for captures by clearing that square from all arrays,
        * setting 50MR to 0.
        * (2) Make the move, but do so depending on the piece moving:
        * (2a) Pawns -- account for ep, promos, and 50MR
        * (2b) Kings -- account for castling
        * (2c) Rooks -- account for lost castling rights
        * (2d) Other -- business as usual
        * (3) Check if king is in check, if he is, undo move
        *
        */
        from = MoveAC.getFromIndex(move);
        to = MoveAC.getToIndex(move);
        piece = MoveAC.getPieceMoved(move);
        moveType = MoveAC.getMoveType(move);
        capture = MoveAC.isCapture(move);
        fromBoard = BitboardUtilsAC.getSquare[from];
        toBoard = BitboardUtilsAC.getSquare[to];
        fromToBoard = fromBoard | toBoard;

        saveHistory(moveNumber);
        fiftyMove++;
        moveNumber++;
        //key ^= Zobrist.getKeyPieceIndex(from, PIECENAMES[piece]) ^ Zobrist.getKeyPieceIndex(to, PIECENAMES[piece]);
        if ((from & getMyPieces()) == 0) return false;
        if (capture) {
            fiftyMove = 0;
            long pieceToRemove = toBoard;
            int pieceToRemoveIndex = to;
            //Remove piece at to location
            if (moveType == MoveAC.TYPE_EN_PASSANT) {
                pieceToRemove = (whiteToMove) ? (toBoard >>> 8) : (toBoard << 8);
                pieceToRemoveIndex = (whiteToMove) ? (to - 8) : (to + 8);
            }
            char pieceRemoved = getPieceAt(pieceToRemoveIndex);
            if (whiteToMove) { // captured a black
                blackPawns ^= pieceToRemove;
                blackKnights ^= pieceToRemove;
                blackBishops ^= pieceToRemove;
                blackRooks ^= pieceToRemove;
                blackQueens ^= pieceToRemove;
                blackKing ^= pieceToRemove;
            } else { // captured a white
                whitePawns ^= pieceToRemove;
                whiteKnights ^= pieceToRemove;
                whiteBishops ^= pieceToRemove;
                whiteRooks ^= pieceToRemove;
                whiteQueens ^= pieceToRemove;
                whiteKing ^= pieceToRemove;
            }
            key ^= Zobrist.getKeyPieceIndex(pieceToRemoveIndex, pieceRemoved);
            }
        //remove en passant from Zobrist, if it already exists.
        if (ePSquare != -1)
            key ^= Zobrist.passantColumn[ePSquare % 8];

        //reset en passant location
        ePSquare = -1;

        switch (piece) {
            case PAWN:
                fiftyMove = 0;
                //Check if we need to update en passant square.
                if (whiteToMove && (from << 16 & to) != 0) ePSquare = Long.numberOfTrailingZeros(from << 8);
                if (!whiteToMove && (from >>> 16 & to) != 0) ePSquare = Long.numberOfTrailingZeros(from >>> 8);
                //remove en passant column from key, if en passant is set.
                if (ePSquare != -1)
                    key ^= Zobrist.passantColumn[ePSquare % 8];
                //if promotion
                if (MoveAC.isPromotion(move)) {
                    if (whiteToMove) {
                        whitePawns ^= from;
                        key ^= Zobrist.getKeyPieceIndex(from, 'P');
                    } else {
                        blackPawns ^= from;
                        key ^= Zobrist.getKeyPieceIndex(from, 'p');
                    }
                    switch (moveType) {
                        case TYPE_PROMOTION_QUEEN:
                            if (whiteToMove) {
                                whiteQueens |= to;
                                key ^= Zobrist.getKeyPieceIndex(to, 'Q');
                            } else {
                                blackQueens |= to;
                                key ^= Zobrist.getKeyPieceIndex(to, 'q');
                            }
                            break;
                        case TYPE_PROMOTION_KNIGHT:
                            if (whiteToMove) {
                                whiteKnights |= to;
                                key ^= Zobrist.getKeyPieceIndex(to, 'N');
                            } else {
                                blackKnights |= to;
                                key ^= Zobrist.getKeyPieceIndex(to, 'n');

                            }
                            break;
                        case TYPE_PROMOTION_BISHOP:
                            if (whiteToMove) {
                                whiteBishops |= to;
                                key ^= Zobrist.getKeyPieceIndex(to, 'B');
                            } else {
                                blackBishops |= to;
                                key ^= Zobrist.getKeyPieceIndex(to, 'b');
                            }
                            break;
                        case TYPE_PROMOTION_ROOK:
                            if (whiteToMove) {
                                whiteRooks |= to;
                                key ^= Zobrist.getKeyPieceIndex(to, 'R');
                            } else {
                                blackRooks |= to;
                                key ^= Zobrist.getKeyPieceIndex(to, 'r');
                            }
                            break;
                    }
                } else {
                    //No promotion
                    if (whiteToMove) {
                        whitePawns ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'P');
                    } else {
                        blackPawns ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'p');
                    }
                }
                break;
            case KNIGHT:
                    if (whiteToMove) {
                        whiteKnights ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'N');
                    } else {
                        blackKnights ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'n');
                    }
                break;
            case BISHOP:
                    if (whiteToMove) {
                        whiteBishops ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'B');
                    } else {
                        blackBishops ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'b');
                    }
                break;
            case ROOK:
                    if (whiteToMove) {
                        whiteRooks ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'R');
                    } else {
                        blackRooks ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'r');
                    }
                break;
            case QUEEN:
                    if (whiteToMove) {
                        whiteQueens ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'Q');
                    } else {
                        blackQueens ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'q');
                    }
                break;
            case KING:
                long rookMask = 0;
                int rookToIndex = 0;
                int rookFromIndex = 0;
                //castling handled
                if (moveType == TYPE_KINGSIDE_CASTLING) {
                    if (whiteToMove) {
                        castleWhite = 0;
                        rookMask = 0xa0L;
                        rookFromIndex = 7;
                        rookToIndex = 5;
                        key ^= Zobrist.whiteKingSideCastling;
                    } else {
                        castleBlack = 0;
                        rookMask = 0xa000000000000000L;
                        rookFromIndex = 63;
                        rookToIndex = 61;
                        key ^= Zobrist.blackKingSideCastling;
                    }
                }
                if (moveType == TYPE_QUEENSIDE_CASTLING) {
                    if (whiteToMove) {
                        castleWhite = 0;
                        rookMask = 0x9L;
                        rookFromIndex = 0;
                        rookToIndex = 3;
                        key ^= Zobrist.whiteQueenSideCastling;
                    } else {
                        castleBlack = 0;
                        rookMask = 0x900000000000000L;
                        rookFromIndex = 56;
                        rookToIndex = 59;
                        key ^= Zobrist.blackQueenSideCastling;
                    }
                }
                if (rookMask != 0) {
                    if (whiteToMove) {
                        whiteRooks ^= rookMask;
                        key ^= Zobrist.getKeyForMove(rookFromIndex, rookToIndex, 'R');
                    } else {
                        blackRooks ^= rookMask;
                        key ^= Zobrist.getKeyForMove(rookFromIndex, rookToIndex, 'r');
                    }
                }
                    if (whiteToMove) {
                        whiteKing ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'K');
                    } else {
                        blackKing ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'k');
                    }
                break;
            default:
                throw new RuntimeException("Unreachable");
        }
        updateAggregateBitboards();
        if (isOwnKingAttacked()) {
            unmakeMove();
            return false;
        }
        endOfSearch++;
        whiteToMove = !whiteToMove;
        key ^= Zobrist.whiteMove;
        return true;
    }

    public void makeNullMove() {
        saveHistory(moveNumber);
        moveNumber++;
        if (ePSquare != -1) key ^= Zobrist.passantColumn[ePSquare % 8];
        ePSquare = -1;
        whiteToMove = !whiteToMove;
        key ^= Zobrist.whiteMove;
    }

    private void saveHistory(int move) {
        white_pawn_history[moveNumber] = whitePawns;
        white_knight_history[moveNumber] = whiteKnights;
        white_bishop_history[moveNumber] = whiteBishops;
        white_rook_history[moveNumber] = whiteRooks;
        white_queen_history[moveNumber] = whiteQueens;
        white_king_history[moveNumber] = whiteKing;
        black_pawn_history[moveNumber] = blackPawns;
        black_knight_history[moveNumber] = blackKnights;
        black_bishop_history[moveNumber] = blackBishops;
        black_rook_history[moveNumber] = blackRooks;
        black_queen_history[moveNumber] = blackQueens;
        black_king_history[moveNumber] = blackKing;
        white_pieces_history[moveNumber] = whitePieces;
        black_pieces_history[moveNumber] = blackPieces;
        all_pieces_history[moveNumber] = allPieces;
        whiteToMove_history[moveNumber] = whiteToMove;
        fiftyMoveRule_history[moveNumber] = fiftyMove;
        enPassant_history[moveNumber] = ePSquare;
        move_history[moveNumber] = move;
        white_castle_history[moveNumber] = castleWhite;
        black_castle_history[moveNumber] = castleBlack;
        key_history[moveNumber] = key;

    }

    public void unmakeMove() {
        unmakeMove(moveNumber - 1);
    }

    public void unmakeMove(int moveNumber) {
        if (moveNumber < 1 || moveNumber < initMoveNumber) return;

        whitePawns = white_pawn_history[moveNumber];
        whiteKnights = white_knight_history[moveNumber];
        whiteBishops = white_bishop_history[moveNumber];
        whiteRooks = white_rook_history[moveNumber];
        whiteQueens = white_queen_history[moveNumber];
        whiteKing = white_king_history[moveNumber];
        blackPawns = black_pawn_history[moveNumber];
        blackKnights = black_knight_history[moveNumber];
        blackBishops = black_bishop_history[moveNumber];
        blackRooks = black_rook_history[moveNumber];
        blackQueens = black_queen_history[moveNumber];
        blackKing = black_king_history[moveNumber];
        whitePieces = white_pieces_history[moveNumber];
        blackPieces = black_pieces_history[moveNumber];
        allPieces = all_pieces_history[moveNumber];
        whiteToMove = whiteToMove_history[moveNumber];
        fiftyMove = fiftyMoveRule_history[moveNumber];
        ePSquare = enPassant_history[moveNumber];
        castleWhite = white_castle_history[moveNumber];
        castleBlack = black_castle_history[moveNumber];
        key = key_history[moveNumber];
        this.moveNumber = moveNumber;

    }

    public int whitePieceMaterial() {
        return 325 * Long.bitCount(whiteKnights) + 325 * Long.bitCount(whiteBishops)
                + 500 * Long.bitCount(whiteRooks) + 975 * Long.bitCount(whiteQueens);
    }

    public int blackPieceMaterial() {
        return 325 * Long.bitCount(blackKnights) + 325 * Long.bitCount(blackBishops)
                + 500 * Long.bitCount(blackRooks) + 975 * Long.bitCount(blackQueens);
    }

    public int movingSidePieceMaterial() {
        return (whiteToMove) ? whitePieceMaterial() : blackPieceMaterial();
    }

    public boolean isOtherKingAttacked() {
        if (whiteToMove) return BitboardMagicAttacksAC.isSquareAttacked(this, blackKing, whiteToMove);
        return BitboardMagicAttacksAC.isSquareAttacked(this, whiteKing, whiteToMove);
    }

    public boolean isOwnKingAttacked() {
        if (whiteToMove) return BitboardMagicAttacksAC.isSquareAttacked(this, whiteKing, !whiteToMove);
        return BitboardMagicAttacksAC.isSquareAttacked(this, blackKing, !whiteToMove);
    }

    //Static Exchange Evaluator based on https://chessprogramming.wikispaces.com/SEE+-+The+Swap+Algorithm
    public int sEE(int move) {
        int capturedPiece = 0;
        long toBoard = MoveAC.getToSquare(move);

        if ((toBoard & (whiteKnights | blackKnights)) != 0) capturedPiece = KNIGHT;
        else if ((toBoard & (whiteBishops | blackBishops)) != 0) capturedPiece = BISHOP;
        else if ((toBoard & (whiteRooks | blackRooks)) != 0) capturedPiece = ROOK;
        else if ((toBoard & (whiteQueens | blackQueens)) != 0) capturedPiece = QUEEN;
        else if ((toBoard & (whitePawns | blackPawns)) != 0) capturedPiece = PAWN;
        return sEE(MoveAC.getFromIndex(move), MoveAC.getToIndex(move), MoveAC.getPieceMoved(move), capturedPiece);
    }

    public int sEE(int fromIndex, int toIndex, int pieceMoved, int capturedPiece) {
        int d = 0;

        long mayXray = (whitePawns | blackPawns) | (whiteKnights | blackKnights) | (whiteBishops | blackBishops) |
                (whiteRooks | blackRooks) | (whiteQueens | blackQueens);
        long fromSquare = MoveAC.getFromSquare(fromIndex);
        long all = allPieces;
        long attacks = BitboardMagicAttacksAC.getIndexAttacks(this, toIndex);
        long fromCandidates;

        seeGain[d] = SEE_PIECE_VALUES[capturedPiece];
        do {
            long side = (d % 2) == 0 ? getOpponentPieces() : getMyPieces();
            d++; // depth increased.

            // speculative score if defended.
            seeGain[d] = SEE_PIECE_VALUES[pieceMoved] - seeGain[d - 1];

            attacks ^= fromSquare; // reset bit in set to traverse
            all ^= fromSquare;  // reset bit in temporary occupancy (for x-Rays)

            if ((fromSquare & mayXray) != 0) attacks |= BitboardMagicAttacksAC.getXrayAttacks(this, toIndex, all);

            //Find the next attacker from least valuable to most valuable.

            //non-promoting pawn
            if ((fromCandidates = attacks & (whitePawns | blackPawns) & side) != 0 && ((toIndex / 8 != 1 & ((d % 2) == 0))
                    || ((toIndex / 8 != 8 & ((d % 2) != 0)))))
                pieceMoved = PAWN;
            else if ((fromCandidates = attacks & (whiteKnights | blackKnights) & side) != 0)
                pieceMoved = KNIGHT;
            else if ((fromCandidates = attacks & (whiteBishops | blackBishops) & side) != 0)
                pieceMoved = BISHOP;
            else if ((fromCandidates = attacks & (whiteRooks | blackRooks) & side) != 0)
                pieceMoved = ROOK;
                //promoting pawn(s) included
            else if ((fromCandidates = attacks & (whitePawns | blackPawns) & side) != 0)
                pieceMoved = PAWN;
            else if ((fromCandidates = attacks & (whiteQueens | blackQueens) & side) != 0)
                pieceMoved = QUEEN;
                //king will only capture if there are no more attackers left.
            else if ((fromCandidates = attacks & (whiteKing | blackKing) & side) != 0 && ((attacks & blackPieces) == 0
                    & ((d % 2) != 0) || (((attacks & blackPieces) == 0) & ((d % 2) == 0))))
                pieceMoved = KING;

            fromSquare = Long.lowestOneBit(fromCandidates);

        } while (fromSquare != 0);

        while (--d != 0) {
            seeGain[d - 1] = -Math.max(-seeGain[d - 1], seeGain[d]);
        }

        return seeGain[0];
    }


    public long getMyPieces() {
        return whiteToMove ? whitePieces : blackPieces;
    }

    public long getOpponentPieces() {
        return whiteToMove ? blackPieces : whitePieces;
    }

    /*public long getAllPieces(){
        return allPieces;
    }*/

}
