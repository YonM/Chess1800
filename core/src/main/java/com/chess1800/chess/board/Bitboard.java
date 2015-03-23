package com.chess1800.chess.board;

import com.chess1800.chess.fen.FENValidator;
import com.chess1800.chess.zobrist.Zobrist;
import com.chess1800.chess.move.Move;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Yonathan on 08/12/2014.
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 * and Ulysse Carion's Godot @ https://github.com/ucarion/godot
 * and Alberto Alonso Ruibal's Carballo @ https://github.com/albertoruibal/carballo
 */
public class Bitboard extends AbstractBitboardEvaluator implements Chessboard {
    protected int material;

    protected int moveNumber;

    protected int fiftyMove;

    protected long[] key_history;
    protected long key; //Zobrist key#

    //For (un)make move
    private int from, to, piece, moveType;
    private long fromBoard, toBoard, fromToBoard;
    private boolean capture;


    //History attributes
    protected long[] white_pawn_history;
    protected long[] white_knight_history;
    protected long[] white_bishop_history;
    protected long[] white_rook_history;
    protected long[] white_queen_history;
    protected long[] white_king_history;
    protected long[] black_pawn_history;
    protected long[] black_knight_history;
    protected long[] black_bishop_history;
    protected long[] black_rook_history;
    protected long[] black_queen_history;
    protected long[] black_king_history;
    protected long[] white_pieces_history;
    protected long[] black_pieces_history;
    protected long[] all_pieces_history;
    protected boolean[] whiteToMove_history;
    protected int[] fiftyMoveRule_history;
    protected int[] enPassant_history;
    protected int[] move_history;
    protected int[] white_castle_history;
    protected int[] black_castle_history;

    //For Static Exchange Evaluator
    protected int[] seeGain;
    protected static final int[] SEE_PIECE_VALUES = {0, 100, 325, 325, 500, 975, 999999};

    public Bitboard() {
        generateAttacks();
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

    @Override
    public void initialize() {
        initializeFromFEN(START_FEN);
    }

    public boolean initializeFromFEN(String fen) {
        if (FENValidator.isValidFEN(fen)) {
            clearBitboards();
            StringTokenizer st = new StringTokenizer(fen, "/ ");
            ArrayList<String> arr = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                arr.add(st.nextToken());
            }
            // setup the board squares first.
            int up = 7;
            int out;
            for (int i = 0; i < 8; i++) {
                out = 0;
                for (char c : arr.get(i).toCharArray()) {
                    if (Character.isDigit(c)) {
                        for (int j = 0; j < Character.digit(c, 10); j++) {
                            out++;
                        }
                    }
                    else {
                        long square = getSquare[up * 8 + out];
                        switch (c) {
                            case 'p':
                                blackPawns |= square;
                                break;
                            case 'P':
                                whitePawns |= square;
                                break;
                            case 'n':
                                blackKnights |= square;
                                break;
                            case 'N':
                                whiteKnights |= square;
                                break;
                            case 'b':
                                blackBishops |= square;
                                break;
                            case 'B':
                                whiteBishops |= square;
                                break;
                            case 'r':
                                blackRooks |= square;
                                break;
                            case 'R':
                                whiteRooks |= square;
                                break;
                            case 'q':
                                blackQueens |= square;
                                break;
                            case 'Q':
                                whiteQueens |= square;
                                break;
                            case 'k':
                                blackKing |= square;
                                break;
                            case 'K':
                                whiteKing |= square;
                                break;
                        }
                        out++;
                    }
                }
                up--;
            }
            updateAggregateBitboards();
            // and now we deal with turn, ep, 50mr, and move number
            // turn
            whiteToMove = arr.get(8).equals("w");
            // castling
            if(arr.get(9).contains("K")) castleWhite += CANCASTLEOO;
            if(arr.get(9).contains("Q")) castleWhite += CANCASTLEOOO;
            if(arr.get(9).contains("k")) castleBlack += CANCASTLEOO;
            if(arr.get(9).contains("q")) castleBlack += CANCASTLEOOO;
            // en passant
            ePSquare = algebraicLocToInt(arr.get(10));
            // 50mr
            if(arr.size() >12) {
                fiftyMove = Integer.parseInt(arr.get(11));
                // move number
                moveNumber = Integer.parseInt(arr.get(12));
            }else{
                fiftyMove = 0;
                moveNumber = 1;
            }
            initMoveNumber = moveNumber;

            key = Zobrist.getKeyFromBoard(this);

            material = Long.bitCount(whitePawns) * PAWN_VALUE + Long.bitCount(whiteKnights) * KNIGHT_VALUE
                    + Long.bitCount(whiteBishops) * BISHOP_VALUE + Long.bitCount(whiteRooks) * ROOK_VALUE
                    + Long.bitCount(whiteQueens) * QUEEN_VALUE;
            material -= (Long.bitCount(blackPawns) * PAWN_VALUE + Long.bitCount(blackKnights) * KNIGHT_VALUE
                    + Long.bitCount(blackBishops) * BISHOP_VALUE + Long.bitCount(blackRooks) * ROOK_VALUE
                    + Long.bitCount(blackQueens) * QUEEN_VALUE);
//            System.out.println(this);
//            for(int s: fenSquares)
//            System.out.println(s);
            /*System.out.println("Turn: "+ whiteToMove);
            System.out.println("50-Move: "+ fiftyMove);
            System.out.println("White castling: " + castleWhite);
            System.out.println("Black castling: " + castleBlack);
            System.out.println("ePSquare: " + ePSquare);*/
            System.out.println(Long.toBinaryString(allPieces));
            return true;
        }// END if

        //if fen is not valid
        return false;
    }


    @Override
    public String getFEN() {
        StringBuilder sb = new StringBuilder();
        long i = A8;
        int j = 0;
        while (i != 0) {
            char p = getPieceAt(Long.numberOfTrailingZeros(i)^7);
            if (p == ' ') {
                j++;
            }
            if ((j != 0) && (p != ' ' || ((i & b_r) != 0))) {
                sb.append(j);
                j = 0;
            }
            if (p != ' ') {
                sb.append(p);
            }
            if ((i != 1) && (i & b_r) != 0) {
                sb.append("/");
            }
            i >>>= 1;
        }
        sb.append(" ");
        sb.append(isWhiteToMove()? "w" : "b");
        sb.append(" ");
        boolean castleAvailable=false;
        if ((castleWhite & CANCASTLEOO)!=0) {
            sb.append("K");
            castleAvailable=true;
        }
        if ((castleWhite & CANCASTLEOOO)!=0) {
            sb.append("Q");
            castleAvailable= true;
        }
        if ((castleBlack & CANCASTLEOO)!=0) {
            sb.append("k");
            castleAvailable = true;
        }
        if ((castleBlack & CANCASTLEOO)!=0) {
            sb.append("q");
            castleAvailable = true;
        }
        if(!castleAvailable)sb.append("-");
        sb.append(" ");
        sb.append((ePSquare != -1 ? index2Algebraic(ePSquare) : "-"));
        sb.append(" ");
        sb.append(fiftyMove);
        sb.append(" ");
        sb.append((moveNumber >> 1) + 1); // 0,1->1.. 2,3->2
        return sb.toString();
    }

    @Override
    public int getFiftyMove() {
        return fiftyMove;
    }

    public int getMoveNumber() {
        return moveNumber;
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
    public long getKey() {
        return key;
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
        saveHistory(moveNumber);

        from = Move.getFromIndex(move);
        to = Move.getToIndex(move);
        piece = Move.getPieceMoved(move);
        moveType = Move.getMoveType(move);
        capture = Move.isCapture(move);
        fromBoard = getSquare[from];
        toBoard = getSquare[to];
        fromToBoard = fromBoard | toBoard;

        fiftyMove++;
        moveNumber++;
        //key ^= Zobrist.getKeyPieceIndex(from, PIECENAMES[piece]) ^ Zobrist.getKeyPieceIndex(to, PIECENAMES[piece]);
        if ((fromBoard & getMyPieces()) == 0) {
            return false;
        }
        if (capture) {
            fiftyMove = 0;
            long pieceToRemove = toBoard;
            int pieceToRemoveIndex = to; //Remove piece at 'to' index.

            if (moveType == Move.TYPE_EN_PASSANT) { //Shift piece and index to remove if the move type is an en-passant.
                pieceToRemove = (whiteToMove) ? (toBoard >>> 8) : (toBoard << 8);
                pieceToRemoveIndex = (whiteToMove) ? (to - 8) : (to + 8);
            }
            char pieceRemoved = getPieceAt(pieceToRemoveIndex);
            if (whiteToMove) { // captured a black piece
                blackPawns &= ~pieceToRemove;
                blackKnights &= ~pieceToRemove;
                blackBishops &= ~pieceToRemove;
                blackRooks &= ~pieceToRemove;
                blackQueens &= ~pieceToRemove;
                blackKing &= ~pieceToRemove;
            } else { // captured a white piece
                whitePawns &= ~pieceToRemove;
                whiteKnights &= ~pieceToRemove;
                whiteBishops &= ~pieceToRemove;
                whiteRooks &= ~pieceToRemove;
                whiteQueens &= ~pieceToRemove;
                whiteKing &= ~pieceToRemove;
            }
            key ^= Zobrist.getKeyPieceIndex(pieceToRemoveIndex, pieceRemoved);
        }
        //remove en passant from Zobrist, if it already exists.
        if (ePSquare != -1)
            key ^= Zobrist.passantColumn[ePSquare % 8];

        //reset en passant location
        ePSquare = -1;

        switch (piece) {
            case Move.PAWN:
                fiftyMove = 0;
                //Check if we need to update en passant square.
                if (whiteToMove && (fromBoard << 16 & toBoard) != 0) ePSquare = Long.numberOfTrailingZeros(fromBoard << 8);
                if (!whiteToMove && (fromBoard >>> 16 & toBoard) != 0) ePSquare = Long.numberOfTrailingZeros(fromBoard >>> 8);
                //add en passant column to key, if en passant is set.
                if (ePSquare != -1)
                    key ^= Zobrist.passantColumn[ePSquare % 8];
                //if promotion
                if (Move.isPromotion(move)) {
                    if (whiteToMove) {
                        whitePawns ^= fromBoard;
                        key ^= Zobrist.getKeyPieceIndex(from, 'P');
                    } else {
                        blackPawns ^= fromBoard;
                        key ^= Zobrist.getKeyPieceIndex(from, 'p');
                    }
                    switch (moveType) {
                        case Move.TYPE_PROMOTION_QUEEN:
                            if (whiteToMove) {
                                whiteQueens |= toBoard;
                                key ^= Zobrist.getKeyPieceIndex(to, 'Q');
                            } else {
                                blackQueens |= toBoard;
                                key ^= Zobrist.getKeyPieceIndex(to, 'q');
                            }
                            break;
                        case Move.TYPE_PROMOTION_KNIGHT:
                            if (whiteToMove) {
                                whiteKnights |= toBoard;
                                key ^= Zobrist.getKeyPieceIndex(to, 'N');
                            } else {
                                blackKnights |= toBoard;
                                key ^= Zobrist.getKeyPieceIndex(to, 'n');

                            }
                            break;
                        case Move.TYPE_PROMOTION_BISHOP:
                            if (whiteToMove) {
                                whiteBishops |= toBoard;
                                key ^= Zobrist.getKeyPieceIndex(to, 'B');
                            } else {
                                blackBishops |= toBoard;
                                key ^= Zobrist.getKeyPieceIndex(to, 'b');
                            }
                            break;
                        case Move.TYPE_PROMOTION_ROOK:
                            if (whiteToMove) {
                                whiteRooks |= toBoard;
                                key ^= Zobrist.getKeyPieceIndex(to, 'R');
                            } else {
                                blackRooks |= toBoard;
                                key ^= Zobrist.getKeyPieceIndex(to, 'r');
                            }
                            break;
                    }
                } else {
                    //No promotion
                    //System.out.println("no promo");
                    if (whiteToMove) {
                        whitePawns ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'P');
                    } else {
                        blackPawns ^= fromToBoard;
                        key ^= Zobrist.getKeyForMove(from, to, 'p');
                    }
                }
                break;
            case Move.KNIGHT:
                if (whiteToMove) {

                    whiteKnights ^= fromToBoard;
                    key ^= Zobrist.getKeyForMove(from, to, 'N');
                } else {
                    blackKnights ^= fromToBoard;
                    key ^= Zobrist.getKeyForMove(from, to, 'n');
                }
                break;
            case Move.BISHOP:
                if (whiteToMove) {
                    whiteBishops ^= fromToBoard;
                    key ^= Zobrist.getKeyForMove(from, to, 'B');
                } else {
                    blackBishops ^= fromToBoard;
                    key ^= Zobrist.getKeyForMove(from, to, 'b');
                }
                break;
            case Move.ROOK:
                if (whiteToMove) {
                    whiteRooks ^= fromToBoard;
                    key ^= Zobrist.getKeyForMove(from, to, 'R');
                } else {
                    blackRooks ^= fromToBoard;
                    key ^= Zobrist.getKeyForMove(from, to, 'r');
                }
                break;
            case Move.QUEEN:
                if (whiteToMove) {
                    whiteQueens ^= fromToBoard;
                    key ^= Zobrist.getKeyForMove(from, to, 'Q');
                } else {
                    blackQueens ^= fromToBoard;
                    key ^= Zobrist.getKeyForMove(from, to, 'q');
                }
                break;
            case Move.KING:
                long rookMask = 0;
                int rookToIndex = 0;
                int rookFromIndex = 0;
                //castling handled
                if (moveType == Move.TYPE_KINGSIDE_CASTLING) {
                    if (whiteToMove) {
                        castleWhite &= ~CANCASTLEOO;
                        rookMask = 0xa0L;
                        rookFromIndex = 7;
                        rookToIndex = 5;
                        key ^= Zobrist.whiteKingSideCastling;
                    } else {
                        castleBlack &= ~CANCASTLEOO;
                        rookMask = 0xa000000000000000L;
                        rookFromIndex = 63;
                        rookToIndex = 61;
                        key ^= Zobrist.blackKingSideCastling;
                    }
                }
                if (moveType == Move.TYPE_QUEENSIDE_CASTLING) {
                    if (whiteToMove) {
                        castleWhite &= ~CANCASTLEOOO;
                        rookMask = 0x9L;
                        rookFromIndex = 0;
                        rookToIndex = 3;
                        key ^= Zobrist.whiteQueenSideCastling;
                    } else {
                        castleBlack &= ~CANCASTLEOOO;
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
                return false;
        }
        updateAggregateBitboards();
        if (whiteToMove) {
            if ((fromToBoard & 0x90L) != 0) { // 0x90 is e1 | h1 -- the king or
                // king's rook has moved
                castleWhite &= ~CANCASTLEOO;
                key ^= Zobrist.whiteKingSideCastling;
            }
            if ((fromToBoard & 0x11L) != 0) { // 0x11 is e1 | a1 -- the king or
                // queen's rook has moved
                castleWhite &= ~CANCASTLEOOO;
                key ^= Zobrist.whiteQueenSideCastling;
            }
        } else {
            if ((fromToBoard & 0x9000000000000000L) != 0) { // 0x90... is 0x90
                // <<'d to
                // black's side
                castleBlack &= ~CANCASTLEOO;
                key ^= Zobrist.blackKingSideCastling;
            }
            if ((fromToBoard & 0x1100000000000000L) != 0) { // 0x11... is 0x11
                // <<'d to
                // black's side
                castleBlack &= ~CANCASTLEOOO;
                key ^= Zobrist.blackQueenSideCastling;
            }
        }
        if (isOwnKingAttacked()) {
            //if(piece==BISHOP && whiteToMove) System.out.println("cant do this");
            unmakeMove();
            return false;
        }

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

    public void unmakeMove() {
        unmakeMove(moveNumber - 1);
    }


    public void unmakeMove(int moveNumber) {
        if (moveNumber < 0 || moveNumber < initMoveNumber) return;

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

    protected boolean isOwnKingAttacked() {
        if (whiteToMove) return isSquareAttacked(whiteKing, whiteToMove);
        return isSquareAttacked(blackKing, whiteToMove);
    }

    public int isEndOfGame() {
        int endGame= NOT_ENDED;
        if(!legalMovesAvailable())
            if(isCheck()){
                endGame= whiteToMove? BLACK_WIN : WHITE_WIN;
            }else{
                endGame=isDraw();
            }
        else endGame=isDraw();

        return endGame;
    }

    public int isDraw(){

        // Checks if the current position is a draw due to:
        // stalemate, insufficient material, 50-move rule or
        // threefold repetition.

        // Stalemate
        if (!legalMovesAvailable() && !isOwnKingAttacked()) return DRAW_BY_STALEMATE;

        // Evaluate for draw due to insufficient material
        int whitePawnsTotal, whiteKnightsTotal, whiteBishopsTotal, whiteRooksTotal, whiteQueensTotal, whiteTotalMat;
        int blackPawnsTotal, blackKnightsTotal, blackBishopsTotal, blackRooksTotal, blackQueensTotal, blackTotalMat;
        whitePawnsTotal = Long.bitCount(whitePawns);
        whiteKnightsTotal = Long.bitCount(whiteKnights);
        whiteBishopsTotal = Long.bitCount(whiteBishops);
        whiteRooksTotal = Long.bitCount(whiteRooks);
        whiteQueensTotal = Long.bitCount(whiteQueens);
        whiteTotalMat = 3 * whiteKnightsTotal + 3 * whiteBishopsTotal + 5 * whiteRooksTotal + 10 * whiteQueensTotal;


        blackPawnsTotal = Long.bitCount(blackPawns);
        blackKnightsTotal = Long.bitCount(blackKnights);
        blackBishopsTotal = Long.bitCount(blackBishops);
        blackRooksTotal = Long.bitCount(blackRooks);
        blackQueensTotal = Long.bitCount(blackQueens);
        blackTotalMat = 3 * blackKnightsTotal + 3 * blackBishopsTotal + 5 * blackRooksTotal + 10 * blackQueensTotal;

        // Check if it's a draw due to insufficient material.
        if (whitePawnsTotal == 0 && blackPawnsTotal == 0) {

            // king vs king
            if (whiteTotalMat + blackTotalMat == 0)
                return DRAW_BY_MATERIAL;

            // king and knight vs king
            if (((whiteTotalMat == 3) && (whiteKnightsTotal == 1) && (blackTotalMat == 0)) ||
                    ((blackTotalMat == 3)) && (blackKnightsTotal == 1) && (whiteTotalMat == 0))
                return DRAW_BY_MATERIAL;


            // 2 kings with one or more bishops and all bishops on the same colour
            if (whiteBishopsTotal + blackBishopsTotal > 0) {
                if (whiteKnightsTotal + whiteRooksTotal + whiteQueensTotal + blackKnightsTotal + blackRooksTotal + blackQueensTotal == 0) {
                    if ((((whiteBishops | blackBishops) & WHITE_SQUARES) == 0) ||
                            (((whiteBishops | blackBishops) & BLACK_SQUARES) == 0))
                            return DRAW_BY_MATERIAL;
                }
            }
        }

        //50Move rule
        if (fiftyMove >= 100) return DRAW_BY_FIFTYMOVE;

        //Three fold repetition
        if (repetitionCount() >= 3) return DRAW_BY_REP;

        return NOT_ENDED;
    }
    public int repetitionCount() {
        int i, lastI, rep = 1; // current position is at least 1 repetition
        lastI = moveNumber - fiftyMove;          // we don't need to go back all the way
        for (i = moveNumber - 2; i >= lastI; i -= 2)   // Only search for current side, skip opposite side
        {
            if (key_history[i] == key) rep++;
            if (rep >= 3) return 3;

        }
        return rep;
    }
    public boolean isCheckMate(){
        return isCheck() && !legalMovesAvailable();
    }

    public boolean isCheck(){
        return isSquareAttacked(whiteKing, true)
                || isSquareAttacked(blackKing, false);
    }

    public int movingSideMaterial() {
        return (whiteToMove) ? whitePieceMaterial() : blackPieceMaterial();
    }


    public int whitePieceMaterial() {
        return 325 * Long.bitCount(whiteKnights) + 325 * Long.bitCount(whiteBishops)
                + 500 * Long.bitCount(whiteRooks) + 975 * Long.bitCount(whiteQueens);
    }

    public int blackPieceMaterial() {
        return 325 * Long.bitCount(blackKnights) + 325 * Long.bitCount(blackBishops)
                + 500 * Long.bitCount(blackRooks) + 975 * Long.bitCount(blackQueens);
    }

    //Static Exchange Evaluator based on https://chessprogramming.wikispaces.com/SEE+-+The+Swap+Algorithm

    public int sEE(int move) {
        int capturedPiece = 0;
        long toBoard = Move.getToSquare(move);

        if ((toBoard & (whiteKnights | blackKnights)) != 0) capturedPiece = Move.KNIGHT;
        else if ((toBoard & (whiteBishops | blackBishops)) != 0) capturedPiece = Move.BISHOP;
        else if ((toBoard & (whiteRooks | blackRooks)) != 0) capturedPiece = Move.ROOK;
        else if ((toBoard & (whiteQueens | blackQueens)) != 0) capturedPiece = Move.QUEEN;
        else if ((toBoard & (whitePawns | blackPawns)) != 0) capturedPiece = Move.PAWN;
        return sEE(Move.getFromIndex(move), Move.getToIndex(move), Move.getPieceMoved(move), capturedPiece);
    }

    public int sEE(int fromIndex, int toIndex, int pieceMoved, int capturedPiece) {
        int d = 0;

        long mayXray = (whitePawns | blackPawns) | (whiteKnights | blackKnights) | (whiteBishops | blackBishops) |
                (whiteRooks | blackRooks) | (whiteQueens | blackQueens);
        long fromSquare = Move.getFromSquare(fromIndex);
        long all = allPieces;
        long attacks = getIndexAttacks(toIndex);
        long fromCandidates;

        seeGain[d] = SEE_PIECE_VALUES[capturedPiece];
        do {
            long side = (d % 2) == 0 ? getOpponentPieces() : getMyPieces(); //sides switched
            d++; // depth increased.


            seeGain[d] = SEE_PIECE_VALUES[pieceMoved] - seeGain[d - 1];// speculative score if defended.
            if (Math.max(-seeGain[d-1], seeGain[d]) <0) break; //prune
            attacks ^= fromSquare; // reset bit in set to traverse
            all ^= fromSquare;  // reset bit in temporary occupancy (for x-Rays)

            if ((fromSquare & mayXray) != 0) attacks |= getXrayAttacks(toIndex, all);

            //Find the next attacker from least valuable to most valuable.

            //non-promoting pawn
            if ((fromCandidates = attacks & (whitePawns | blackPawns) & side) != 0 && ((toIndex / 8 != 1 & ((d % 2) == 0))
                    || ((toIndex / 8 != 8 & ((d % 2) != 0)))))
                pieceMoved = Move.PAWN;
            else if ((fromCandidates = attacks & (whiteKnights | blackKnights) & side) != 0)
                pieceMoved = Move.KNIGHT;
            else if ((fromCandidates = attacks & (whiteBishops | blackBishops) & side) != 0)
                pieceMoved = Move.BISHOP;
            else if ((fromCandidates = attacks & (whiteRooks | blackRooks) & side) != 0)
                pieceMoved = Move.ROOK;
                //promoting pawn(s) included
            else if ((fromCandidates = attacks & (whitePawns | blackPawns) & side) != 0)
                pieceMoved = Move.PAWN;
            else if ((fromCandidates = attacks & (whiteQueens | blackQueens) & side) != 0)
                pieceMoved = Move.QUEEN;
                //king will only capture if there are no more attackers left.
            else if ((fromCandidates = attacks & (whiteKing | blackKing) & side) != 0 && ((attacks & blackPieces) == 0
                    & ((d % 2) != 0) || (((attacks & blackPieces) == 0) & ((d % 2) == 0))))
                pieceMoved = Move.KING;

            fromSquare = Long.lowestOneBit(fromCandidates);

        } while (fromSquare != 0);

        while (--d != 0) {
            seeGain[d - 1] = -Math.max(-seeGain[d - 1], seeGain[d]);
        }

        return seeGain[0];
    }


    @Override
    public String getSAN(int move) {
        String san;
        if (Move.getMoveType(move) == Move.TYPE_KINGSIDE_CASTLING) {
            san = "O-O";
            if(makeMove(move)) {
                if (isCheckMate())
                    san += "#";
                else if (isCheck())
                    san += "+";
                unmakeMove();
            }
            return san;
        }
        if (Move.getMoveType(move) == Move.TYPE_QUEENSIDE_CASTLING) {
            san = "O-O-O";
            if(makeMove(move)) {
                if (isCheckMate())
                    san += "#";
                else if (isCheck())
                    san += "+";
                unmakeMove();
            }
            return san;
        }
        int moveType = Move.getMoveType(move);
        int piece = Move.getPieceMoved(move);
        int fromIndex = Move.getFromIndex(move);
        int toIndex = Move.getToIndex(move);
        int from_file = fromIndex % 8;
        int from_rank = fromIndex / 8;

        boolean amb_file = false, amb_rank = false, amb_move = false;

        int[] moves = new int[MAX_MOVES];
        int num_moves = getAllLegalMoves(moves);

        //Check for ambiguity.
        for (int i = 0; i < num_moves; i++) {
            if (moves[i] == move || Move.getToIndex(moves[i]) != toIndex)
                continue;

            if (Move.isPromotion(Move.getMoveType(move))) {
                if (Move.getMoveType(moves[i]) != moveType)
                    continue;
            }

            int pieceX = Move.getMoveType(moves[i]);
            if (pieceX != piece)
                continue;

            int sq = Move.getFromIndex(moves[i]);

            int sq_file = sq % 8;
            int sq_rank = sq / 8;

            if (sq_file == from_file)
                amb_file = true;
            if (sq_rank == from_rank)
                amb_rank = true;

            amb_move = true;
        }

        san = moveToPieceString(move);

        if (amb_move) {
            if (!amb_file)
                san += intColToString(fromIndex);
            else if (!amb_rank)
                san += intRowToString(fromIndex);
            else
                san += intToAlgebraicLoc(fromIndex);
        }
        if (Move.isCapture(move)) {
            if (Move.getPieceMoved(move) == Move.PAWN && !amb_rank)
                san += intColToString(fromIndex);
            san += "x";
        }
        san += intToAlgebraicLoc(toIndex);
        if (Move.isPromotion(Move.getMoveType(move))) {
            san += "=";
            switch (Move.getMoveType(move)) {
                case Move.TYPE_PROMOTION_BISHOP:
                    san += "B";
                    break;
                case Move.TYPE_PROMOTION_KNIGHT:
                    san += "N";
                    break;
                case Move.TYPE_PROMOTION_ROOK:
                    san += "R";
                    break;
                case Move.TYPE_PROMOTION_QUEEN:
                    san += "Q";
                    break;
            }
        }

        if(makeMove(move)) {
            if (isCheckMate())
                san += "#";
            else if (isCheck())
                san += "+";
            unmakeMove();
        }

        return san;
    }

    private static String moveToPieceString(int move) {
        switch (Move.getPieceMoved(move)) {
            case Move.KNIGHT:
                return "N";
            case Move.BISHOP:
                return "B";
            case Move.ROOK:
                return "R";
            case Move.QUEEN:
                return "Q";
            case Move.KING:
                return "K";
        }
        return "";
    }

    /**
     * Converts an integer location in [0, 64) to a string in ['a', 'h']
     * representing its column (aka file).
     *
     * @param loc
     *            an int in [0, 64) representing a location.
     * @return the string representing the file of the location.
     */
    private static String intColToString(int loc) {
        return (char) ( ( (loc % 8) + 'a')) + "";
    }


    /**
     * Converts an integer location in [0, 64) to a string in
     * "algebraic notation" (ie. of the form 'a7', 'c5).
     *
     * @param loc
     *            an int in [0, 64) representing a location
     * @return the "algebraic notation" of the location
     */
    public static String intToAlgebraicLoc(int loc) {
        if (loc == -1)
            return "-";
        int out = loc % 8;
        int up = loc / 8;
        char outc = (char) (out + 'a');
        char upc = (char) (up + '1');
        System.out.println("Algebraic Location: "+ outc + "" + upc);
        return outc + "" + upc;
    }

    /**
     * Converts an integer location in [0, 64) to a string in ['a', 'h']
     * representing its rank (aka row).
     *
     * @param loc
     *            an int in [0, 64) representing a location.
     * @return the string representing the rank of the location.
     */
    private static String intRowToString(int loc) {
        return (char) ( ( (loc / 8) + '1')) + "";
    }
    /*public boolean isOtherKingAttacked() {
        if (whiteToMove) return BitboardMagicAttacksAC.isSquareAttacked(this, blackKing, !whiteToMove);
        return BitboardMagicAttacksAC.isSquareAttacked(this, whiteKing, !whiteToMove);
    }*/

    public  int algebraic2Index(String name) {
        if (name.equals("-"))
            return -1;
        int out = name.charAt(0) - 'a';
        int up = Integer.parseInt(name.charAt(1) + "") - 1;
        return up * 8 + out;
    }

    public String index2Algebraic(int index) {
        if (index == -1)
            return "-";
        System.out.println("index given: " + index);
        int out = index % 8;
        int up = index / 8;
        char outc = (char) (out + 'a');
        char upc = (char) (up + '1');
        System.out.println("Algebraic returned: "+ outc + "" + upc);
        return outc + "" + upc;
    }

    public boolean validateHashMove(int move) {
        if(!makeMove(move)) return false;
        unmakeMove(move);
        return true;
    }

    public final int getMoveFromString(String move, boolean legalityCheck) {
        int fromIndex;
        int toIndex;
        int moveType = 0;
        int pieceMoved = 0;

        // Ignore checks & captures indicators
        move = move.replace("+", "").replace("x", "").replace("-", "").replace("=", "").replace("#", "").replaceAll(" ", "").replaceAll("0", "o")
                .replaceAll("O", "o");

        System.out.println("Side to move: "+ (isWhiteToMove() ? "white": "black"));

        //castling move check
        if ("ooo".equalsIgnoreCase(move)) {
            if (isWhiteToMove()) move = "e1c1";
            else move = "e8c8";
        } else if ("oo".equalsIgnoreCase(move)) {
            if (isWhiteToMove()) move = "e1g1";
            else move = "e8g8";
        }

        //handle promotion moves
        char promo = move.charAt(move.length() - 1);
        switch (Character.toLowerCase(promo)) {
            case 'q':
                moveType = Move.TYPE_PROMOTION_QUEEN;
                break;
            case 'n':
                moveType = Move.TYPE_PROMOTION_KNIGHT;
                break;
            case 'r':
                moveType = Move.TYPE_PROMOTION_ROOK;
                break;
            case 'b':
                moveType = Move.TYPE_PROMOTION_BISHOP;
                break;
        }
        // If promotion, remove the last char
        if (moveType != 0){
            move = move.substring(0, move.length() - 1);
            System.out.println("promotion piece removed");
        }


        //To is always the last 2 characters.
        toIndex = algebraic2Index(move.substring(move.length() - 2, move.length()));
        long toBoard = 0X1L << toIndex;
        long fromBoard = 0;

        // Fills from with a mask of possible from values... may need to disambiguate, if it's not a pawn move.
        switch (move.charAt(0)) {
            case 'N':
                System.out.println("knights: " + (whiteKnights | blackKnights));
                fromBoard = (whiteKnights | blackKnights) & getMyPieces() & knight[toIndex];
                break;
            case 'K':
                fromBoard = (whiteKing | blackKing) & getMyPieces() & king[toIndex];
                break;
            case 'R':
                fromBoard = (whiteRooks | blackRooks) & getMyPieces() & getRookAttacks(toIndex, allPieces);
                break;
            case 'B':
                fromBoard = (whiteBishops | blackBishops) & getMyPieces() & getBishopAttacks(toIndex, allPieces);
                break;
            case 'Q':
                fromBoard = (whiteQueens | blackQueens) & getMyPieces()
                        & (getRookAttacks(toIndex, allPieces) | getBishopAttacks(toIndex, allPieces));
                break;
        }

        if (fromBoard != 0) { // remove the piece char
            System.out.println("remove the piece char");
            move = move.substring(1);
            System.out.println("move now: " +move);
        }else{
            //Pawn moves
            System.out.println("pawn piece moved");
            if (move.length() == 2) {
                System.out.println("pawn non-capture");
                if (isWhiteToMove()) {
                    fromBoard = (whitePawns | blackPawns) & getMyPieces() & ((toBoard >>> 8) | (((toBoard >>> 8) & allPieces) == 0 ? (toBoard >>> 16) : 0));
                } else {
                    System.out.println("pawn capture");
                    fromBoard = (whitePawns | blackPawns) & getMyPieces() & ((toBoard << 8) | (((toBoard << 8) & allPieces) == 0 ? (toBoard << 16) : 0));
                }
            }
            if (move.length() == 3) { // Pawn capture
                fromBoard = (whitePawns | blackPawns) & getMyPieces() & (isWhiteToMove() ? blackPawn[toIndex] : whitePawn[toIndex]);
            }
        }

        if (move.length() == 3) { // now disambiguate
            System.out.println("disambiguate");
            char disambiguate = move.charAt(0);
            int i = "abcdefgh".indexOf(disambiguate);
            if (i >= 0)
                fromBoard &= COLUMN[i];
            int j = "12345678".indexOf(disambiguate);
            if (j >= 0)
                fromBoard &= RANK[j];
        }

        if (move.length() == 4) { //UCI move
            System.out.println("UCI move");
            fromBoard = algebraic2Square(move.substring(0, 2));
        }

        if(fromBoard == 0){
            System.out.println("from board empty");
            return -1;
        }

        //Detects multiple froms and chooses the first legal move
        while (fromBoard != 0) {
            long myFrom = Long.lowestOneBit(fromBoard);
            fromBoard ^= myFrom;
            fromIndex = Long.numberOfTrailingZeros(myFrom);

            boolean capture = false;
            if((myFrom & (whitePawns | blackPawns)) != 0){
                pieceMoved = Move.PAWN;

                //passant capture check
                if((toIndex != (fromIndex -8)) && (toIndex != (fromIndex +8)) &&
                        (toIndex != (fromIndex -16)) && (toIndex != (fromIndex +16))){

                    if((toBoard & allPieces) == 0){
                        moveType = Move.TYPE_EN_PASSANT;
                        capture = true;
                    }

                }

                // Default promotion to queen if not specified
                if ((toBoard & (b_u | b_d)) != 0 && (moveType < Move.TYPE_PROMOTION_QUEEN)) {
                    moveType = Move.TYPE_PROMOTION_QUEEN;
                }
            }

            if ((myFrom & (whiteBishops | blackBishops)) != 0)
                pieceMoved = Move.BISHOP;
            else if ((myFrom & (whiteKnights | blackKnights)) != 0)
                pieceMoved = Move.KNIGHT;
            else if ((myFrom & (whiteRooks | blackRooks)) != 0)
                pieceMoved = Move.ROOK;
            else if ((myFrom & (whiteQueens | blackQueens)) != 0)
                pieceMoved = Move.QUEEN;
            else if ((myFrom & (whiteKing | blackKing)) != 0) {
                pieceMoved = Move.KING;
                if (fromIndex == 4 || fromIndex == 4 + (8 * 7)) {
                    if (toIndex == (fromIndex + 2))
                        moveType = Move.TYPE_QUEENSIDE_CASTLING;
                    if (toIndex == (fromIndex - 2))
                        moveType = Move.TYPE_KINGSIDE_CASTLING;
                }
            }

            // Now set captured piece flag
            if ((toBoard & (whitePieces | blackPieces)) != 0) {
                capture = true;
            }
            int moveInt= Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);
            if(legalityCheck){
                if(makeMove(moveInt)){
                    unmakeMove();
                    return moveInt;
                }
            }else{
                return moveInt;
            }

        }

        return 1;

    }

    @Override
    public boolean isMoveLegal(int move) {
        moves= new int[MAX_MOVES];
        int legalMovesCount=getAllLegalMoves(moves);
        for(int i=0; i<legalMovesCount; i++)
            if(move == moves[i]) return true;

        return false;
    }

    private long algebraic2Square(String name) {
        long aux = H1;
        for (int i = 0; i< 64; i++){
            if(name.equals(squareNames[i]))
                return aux;
            aux <<=1;
        }
        return 0;
    }

    /**
     * Converts a location in "algebraic notation" (ie. of the form 'a7', 'c5',
     * etc.) into its integer representation.
     *
     * <b>Note:</b> this method may throw a <code>NumberFormatException</code>
     * if the passed string is malformed-- no error checking occurs in this
     * method.
     *
     * @param loc
     *            a string representing a location
     * @return the integer representation of the string
     */
    private static int algebraicLocToInt(String loc) {
        if (loc.equals("-"))
            return -1;
        int out = loc.charAt(0) - 'a';
        int up = Integer.parseInt(loc.charAt(1) + "") - 1;
        return up * 8 + out;
    }





/*    public String toString() {
        String s = "     a   b   c   d   e   f   g   h\n";
        s += "   +---+---+---+---+---+---+---+---+\n 8 | ";

        for (int up = 7; up >= 0; up--) {
            for (int out = 0; out < 8; out++) {
                s += getPieceAt(up * 8 + out) + " | ";
            }
            s += (up + 1) + "\n   +---+---+---+---+---+---+---+---+";
            if (up != 0)
                s += "\n " + up + " | ";
        }

        s += "\n     a   b   c   d   e   f   g   h\n\n";

        s += "White to move: " + whiteToMove + "\n";
        s += "White: O-O: " + castleWhite + " -- O-O-O: " + castleWhite + "\n";
        s += "Black: O-O: " + castleBlack + " -- O-O-O: " + castleBlack + "\n";
        s +=
                "En Passant: " + ePSquare + " ("
                        + intToAlgebraicLoc(ePSquare) + ")\n";
        s += "50 move rule: " + fiftyMove + "\n";
        s += "Move number: " + moveNumber + "\n";
        return s;
    }*/
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        int j = 8;
        long i = A8;
        while (i != 0) {
            sb.append(getPieceAt(Long.numberOfTrailingZeros(i)^7));
            sb.append(" ");
            if ((i & b_r) != 0) {
                sb.append(j--);
                sb.append("\n");
            }
            i >>>= 1;
        }
        sb.append("a b c d e f g h  ");
        sb.append((whiteToMove ? "white move\n" : "blacks move\n"));
        return sb.toString();
    }
}
