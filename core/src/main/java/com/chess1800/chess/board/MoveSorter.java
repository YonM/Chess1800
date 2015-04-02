package com.chess1800.chess.board;

import com.chess1800.chess.move.Move;
import com.chess1800.chess.search.Search;

/**
 * Created by Yonathan on 01/04/2015.
 */
public class MoveSorter extends AbstractBitboardMagicAttacks {
    private int goodCaptureIndex;
    private int equalCaptureIndex;
    private int badCaptureIndex;
    private int nonCaptureIndex;
    public final static int GENERATE_ALL = 0;
    public final static int GENERATE_CAPTURES_PROMOS = 1;
    private int ttMove;
    private int movesToGenerate;
    // Move generation phases
//
    public final static int PHASE_TT = 0;
    public final static int PHASE_GEN_CAPTURES = 1;
    public final static int PHASE_GOOD_CAPTURES_AND_PROMOS = 2;
    public final static int PHASE_EQUAL_CAPTURES = 3;
    public final static int PHASE_GEN_NON_CAPTURES = 4;
    public final static int PHASE_NON_CAPTURES = 5;
    public final static int PHASE_BAD_CAPTURES = 6;
    public final static int PHASE_END = 7;
    private int phase;
    //private static final int[] VICTIM_PIECE_VALUES = {0, 100, 325, 330, 500, 975, 10000};
    //private static final int[] AGGRESSOR_PIECE_VALUES = {0, 10, 32, 33, 50, 97, 99};
    private static final int SCORE_PROMOTION_QUEEN = 875;
    private static final int SCORE_UNDERPROMOTION = Integer.MIN_VALUE + 1;
    private static final int SCORE_LOWEST = Integer.MIN_VALUE;

    public final static int SEE_NOT_CALCULATED = Short.MAX_VALUE;

    private int[] goodCaptures = new int[Bitboard.MAX_MOVES]; // Stores captures and queen promotions
    private int[] goodCapturesSee = new int[Bitboard.MAX_MOVES];
    private int[] goodCapturesScores = new int[Bitboard.MAX_MOVES];
    private int[] badCaptures = new int[Bitboard.MAX_MOVES]; // Stores captures and queen promotions
    private int[] badCapturesSee = new int[Bitboard.MAX_MOVES];
    private int[] badCapturesScores = new int[Bitboard.MAX_MOVES];
    private int[] equalCaptures = new int[Bitboard.MAX_MOVES]; // Stores captures and queen promotions
    private int[] equalCapturesSee = new int[Bitboard.MAX_MOVES];
    private int[] equalCapturesScores = new int[Bitboard.MAX_MOVES];
    private int[] nonCaptures = new int[Bitboard.MAX_MOVES]; // Stores non captures and underpromotions
    private int[] nonCapturesSee = new int[Bitboard.MAX_MOVES];
    private int[] nonCapturesScores = new int[Bitboard.MAX_MOVES];


    private Bitboard board;

    private int lastMoveSEE;
    private int move;
    
    //pieces
    private long myPieces;
    private long opponentPieces;
    private long allPieces;
    private long pawns;
    private long knights;
    private long bishops;
    private long rooks;
    private long queens;
    private long kings;

    //castling
    private int castleWhite;
    private int castleBlack;

    //Attack Information
    
    //King Indexes
    private int myKingIndex;
    public int otherKingIndex;

    //Bishops/Rooks that can attack kings
    private long bishopAttacksMyKing;
    private long rookAttacksMyKing;
    private long bishopAttacksOtherKing;
    private long rookAttacksOtherKing;
    //private long mayPin; // bot my pieces than can discover an attack and the opponent pieces pinned, that is any piece attacked by a slider
    private long piecesGivingCheck;
    private long interposeCheckSquares;

    private long attacksFromSquare[] = new long[64];
    private long attackedSquares[] = {0, 0}; //indexed by color; white =0, black =1;
    private int ePIndex;
    private int ePSquare;
    private Search search;


    public void genMoves(int ttMove, Search search) {
        this.search = search;
        this.board = (Bitboard) search.getBoard();
        genMoves(ttMove, GENERATE_ALL, search);
    }
    public void genMoves(int ttMove, int movesToGenerate, Search search) {
        if(movesToGenerate != GENERATE_ALL){
            this.search = search;
            this.board = (Bitboard) search.getBoard();
        }
        this.ttMove = ttMove;
        this.movesToGenerate = movesToGenerate;
        phase = PHASE_TT;
    }

    private void initMoveGen() {

        initializeAttackInfo();

        goodCaptureIndex = 0;
        badCaptureIndex = 0;
        equalCaptureIndex = 0;
        nonCaptureIndex = 0;
    }

    private void initializeAttackInfo() {
        myPieces = board.getMyPieces();
        opponentPieces = board.getOpponentPieces();
        allPieces = board.getAllPieces();
        pawns = board.getWhitePawns()| board.getBlackPawns();
        knights = board.getWhiteKnights()| board.getBlackKnights();
        bishops = board.getWhiteBishops()| board.getBlackBishops();
        rooks = board.getWhiteRooks()| board.getBlackRooks();
        queens = board.getWhiteQueens()| board.getBlackQueens();
        kings = board.getWhiteKing()| board.getBlackKing();
        castleWhite = board.getCastleWhite();
        castleBlack = board.getCastleBlack();
        ePIndex = board.getEPIndex();
        ePSquare = ePIndex ==-1? 0 : getColumn(ePIndex);
        myKingIndex = square2Index(kings & myPieces);
        otherKingIndex = square2Index(kings & opponentPieces);


        /*bishopAttacksMyKing = getBishopAttacks(myKingIndex, allPieces);
        rookAttacksMyKing = getRookAttacks(myKingIndex, allPieces);

        bishopAttacksOtherKing = getBishopAttacks(otherKingIndex, allPieces);
        
        rookAttacksOtherKing = getRookAttacks(otherKingIndex, allPieces);
        long pieceAttacks;
        long square = H1;
        for (int index = 0; index < 64; index++) {
            if ((square & allPieces) != 0) {
                boolean isWhite = (((board.isWhiteToMove()? myPieces: opponentPieces) & square) != 0);
                int color = (isWhite ? 0 : 1);

                pieceAttacks = 0;
                if ((square & pawns) != 0) {
                    pieceAttacks = (isWhite ? whitePawn[index] : blackPawn[index]);
                } else if ((square & knights) != 0) {
                    pieceAttacks = knight[index];
                } else if ((square & kings) != 0) {
                    pieceAttacks = king[index];
                } else {
                    if ((square & (bishops| queens)) != 0) {
                        long sliderAttacks = getBishopAttacks(index, allPieces);
                        if ((square & myPieces) == 0 && (sliderAttacks & (kings & myPieces)) != 0) {
                            interposeCheckSquares |= sliderAttacks & bishopAttacksMyKing; // And with only the diagonal attacks to the king
                        }
                        pieceAttacks |= sliderAttacks;
                    }
                    if ((square & (rooks|queens)) != 0) {
                        long sliderAttacks = getRookAttacks(index, allPieces);
                        if ((square & myPieces) == 0 && (sliderAttacks & (kings & myPieces)) != 0) {
                            interposeCheckSquares |= sliderAttacks & rookAttacksMyKing; // And with only the rook attacks to the king
                        }
                        pieceAttacks |= sliderAttacks;
                    }
                    //mayPin |= all & pieceAttacks;
                }

                attackedSquares[color] |= pieceAttacks;
                attacksFromSquare[index] = pieceAttacks;

                if ((square & myPieces) == 0 && (pieceAttacks & (myPieces & kings)) != 0) {
                    piecesGivingCheck |= square;
                }
            } else {
                attacksFromSquare[index] = 0;
            }
            square <<= 1;
        }*/
    }

    /*Staged Move Generation.
    *  Order:
    *  1. TT move
    *  2. Good Captures (Ordered by SEE)
    *  3. Equal Captures
    *  4. Non-Captures (Ordered by History Heuristic)
    *  5. Bad Captures
     */
    public int next() {
        switch (phase){
            case PHASE_TT:
                phase++;
                if(ttMove != Move.EMPTY){
                    lastMoveSEE = Move.isCapture(ttMove) || Move.getMoveType(ttMove)==Move.TYPE_PROMOTION_QUEEN ? board.sEE(ttMove) : 0;
                    if(movesToGenerate == GENERATE_ALL || Move.isPromotion(ttMove) || (movesToGenerate == GENERATE_CAPTURES_PROMOS && Move.isCapture(ttMove) && lastMoveSEE >= 0))
                        return ttMove;
                }
            case PHASE_GEN_CAPTURES:
                initMoveGen();
                generateCaptures();
                phase++;
            case PHASE_GOOD_CAPTURES_AND_PROMOS:
                move = selectMove(goodCaptureIndex, goodCaptures, goodCapturesScores);
                if(move!= Move.EMPTY) return move;

                phase++;

            case PHASE_EQUAL_CAPTURES:
                move = selectMove(equalCaptureIndex, equalCaptures, equalCapturesScores);
                if(move!= Move.EMPTY) return move;

                phase++;
            case PHASE_GEN_NON_CAPTURES:
                if (movesToGenerate == GENERATE_CAPTURES_PROMOS) {
                    phase = PHASE_END;
                    return Move.EMPTY;
                }
                generateNonCaptures();
                phase++;

            case PHASE_NON_CAPTURES:
                move = selectMove(nonCaptureIndex, nonCaptures, nonCapturesScores);
                if (move != Move.EMPTY) return move;

                phase++;
            case PHASE_BAD_CAPTURES:
                move = selectMove(badCaptureIndex, badCaptures, badCapturesScores);
                if (move != Move.EMPTY) return move;

                phase = PHASE_END;
                return Move.EMPTY;
        }
        return Move.EMPTY;
    }

    private void generateCaptures() {
        long square = H1;
        int index = 0;
        while (square != 0) {
            if ((square & myPieces) != 0) {
                if ((square & rooks) != 0) { // Rook
                    generateMovesFromAttacks(Move.ROOK, index, square, opponentPieces, true);
                } else if ((square & bishops) != 0) { // Bishop
                    generateMovesFromAttacks(Move.BISHOP, index, square,  opponentPieces, true);
                } else if ((square & queens) != 0) { // Queen
                    generateMovesFromAttacks(Move.QUEEN, index, square, opponentPieces, true);
                } else if ((square & kings) != 0) { // King
                    generateMovesFromAttacks(Move.KING, index, square, opponentPieces, true);
                } else if ((square & knights) != 0) { // Knight
                    generateMovesFromAttacks(Move.KNIGHT, index, square, opponentPieces, true);
                } else if ((square & pawns) != 0) { // Pawns
                    if(board.isWhiteToMove())
                        generatePawnCapturesOrGoodPromos(index,
                                (opponentPieces | ePSquare) //
                                        | (((square & b2_u) != 0) && (((square << 8) & allPieces) == 0) ? (square << 8) : 0), // Pushes only if promotion
                                ePSquare);
                    else
                        generatePawnCapturesOrGoodPromos(index,
                                (opponentPieces | ePSquare) //
                                        | (((square & b2_d) != 0) && (((square >>> 8) & allPieces) == 0) ? (square >>> 8) : 0), // Pushes only if promotion
                                ePSquare);

                }

            }
            index++;
            square <<= 1;
        }

    }

    public void generateNonCaptures() {
        long square = H1;
        int index = 0;
        while (square != 0) {
            if ((square & myPieces) != 0) {
                if ((square & rooks) != 0) { // Rook
                    generateMovesFromAttacks(Move.ROOK, index, square, ~allPieces, false);
                } else if ((square & bishops) != 0) { // Bishop
                    generateMovesFromAttacks(Move.BISHOP, index, square, ~allPieces, false);
                } else if ((square & queens) != 0) { // Queen
                    generateMovesFromAttacks(Move.QUEEN, index, square, ~allPieces, false);
                } else if ((square & kings) != 0) { // King
                    generateMovesFromAttacks(Move.KING, index, square, ~allPieces, false);
                } else if ( (square & knights)!= 0) { // Knight
                    generateMovesFromAttacks(Move.KNIGHT, index, square, ~allPieces, false);
                }
                if ((square & pawns) != 0) { // Pawns excluding the already generated promos
                    if (board.isWhiteToMove()) {
                        generatePawnNonCapturesAndBadPromos(index, (((square << 8) & allPieces) == 0 ? (square << 8) : 0)
                                | ((square & b2_d) != 0 && (((square << 8) | (square << 16)) & allPieces) == 0 ? (square << 16) : 0));
                    } else {
                        generatePawnNonCapturesAndBadPromos(index, (((square >>> 8) & allPieces) == 0 ? (square >>> 8) : 0)
                                | ((square & b2_u) != 0 && (((square >>> 8) | (square >>> 16)) & allPieces) == 0 ? (square >>> 16) : 0));
                    }
                }
            }
            index++;
            square <<= 1;
        }
        // Castling: disabled when in check or king route attacked
        square = (kings) & myPieces; // my king
        int myKingIndex = -1;
        // Castling: disabled when in check or squares attacked
        if ((((allPieces & (board.isWhiteToMove() ? 0x06L : 0x0600000000000000L)) == 0 &&
                (board.isWhiteToMove() ? ((castleWhite & CANCASTLEOO)!=0) : ((castleBlack & CANCASTLEOO)!=0))
        ))) {
            myKingIndex = square2Index(square);
            if (!board.isCheck() &&
                    !isIndexAttacked((byte) (myKingIndex - 1), board.isWhiteToMove())
                    && !isIndexAttacked((byte) (myKingIndex - 2), board.isWhiteToMove()))
                addMove(Move.KING, myKingIndex, myKingIndex - 2, false, Move.TYPE_KINGSIDE_CASTLING);
        }
        if ((((allPieces & (board.isWhiteToMove() ? 0x70L : 0x7000000000000000L)) == 0 &&
                (board.isWhiteToMove() ? ((castleWhite & CANCASTLEOOO)!=0) : ((castleBlack & CANCASTLEOOO)!=0))
        ))) {
            if (myKingIndex == -1) {
                myKingIndex = square2Index(square);
            }
            if (!board.isCheck() &&
                    !isIndexAttacked((byte) (myKingIndex + 1), board.isWhiteToMove())
                    && !isIndexAttacked((byte) (myKingIndex + 2), board.isWhiteToMove()))
                addMove(Move.KING, myKingIndex, myKingIndex + 2, false, Move.TYPE_QUEENSIDE_CASTLING);
        }
    }


    private void generatePawnCapturesOrGoodPromos(int fromIndex, long attacks, long passant) {
        while (attacks != 0) {
            long to = Long.lowestOneBit(attacks);
            if ((to & passant) != 0) {
                addMove(Move.PAWN, fromIndex, to, true, Move.TYPE_EN_PASSANT);
            } else {
                boolean capture = (to & opponentPieces) != 0;
                if ((to & (b_u | b_d)) != 0) {
                    addMove(Move.PAWN, fromIndex, to, capture, Move.TYPE_PROMOTION_QUEEN);
                    // If it is a capture, we must add the under-promotions
                    if (capture) {
                        addMove(Move.PAWN, fromIndex, to, true, Move.TYPE_PROMOTION_KNIGHT);
                        addMove(Move.PAWN, fromIndex,to, true, Move.TYPE_PROMOTION_ROOK);
                        addMove(Move.PAWN, fromIndex, to, true, Move.TYPE_PROMOTION_BISHOP);
                    }
                } else if (capture) {
                    addMove(Move.PAWN, fromIndex, to, true, 0);
                }
            }
            attacks ^= to;
        }
    }

    private void generatePawnNonCapturesAndBadPromos(int fromIndex, long attacks) {
        while (attacks != 0) {
            long to = Long.lowestOneBit(attacks);
            if ((to & (b_u | b_d)) != 0) {
                addMove(Move.PAWN, fromIndex,to, false, Move.TYPE_PROMOTION_KNIGHT);
                addMove(Move.PAWN, fromIndex, to, false, Move.TYPE_PROMOTION_ROOK);
                addMove(Move.PAWN, fromIndex,to, false, Move.TYPE_PROMOTION_BISHOP);
            } else {
                addMove(Move.PAWN, fromIndex, to, false, 0);
            }
            attacks ^= to;
        }
    }


    private int selectMove(int arrayLength, int[] arrayMoves, int[] arrayScores) {
        if(arrayLength == 0) return Move.EMPTY;
        int bestScore = SCORE_LOWEST;
        int bestIndex = -1;
        for (int i =0 ;i< arrayLength ; i++){
            if(arrayScores[i] > bestScore){
                bestScore = arrayScores[i];
                bestIndex =i;
            }
        }
        if (bestIndex != -1) {
            int move = arrayMoves[bestIndex];
            arrayScores[bestIndex] = SCORE_LOWEST;
            return move;
        } else {
            return Move.EMPTY;
        }
    }

    private void addMove(int pieceMoved, int fromIndex, long to, boolean capture, int moveType){
        int pieceCaptured = board.getPieceCaptured(move);
        int see = SEE_NOT_CALCULATED;
        int toIndex= square2Index(to);
        if (capture || moveType == Move.TYPE_PROMOTION_QUEEN) see = board.sEE(fromIndex, toIndex, pieceMoved, pieceCaptured);


        int tempMove = Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);

        if (tempMove==ttMove) return;
        if (movesToGenerate != GENERATE_ALL && see<0) return;



        if(capture && see < 0){
            badCaptures[badCaptureIndex] = tempMove;
            badCapturesScores[badCaptureIndex++] = see;
            return;
        }
        boolean underPromotion = moveType == Move.TYPE_PROMOTION_KNIGHT || moveType == Move.TYPE_PROMOTION_ROOK || moveType == Move.TYPE_PROMOTION_BISHOP;
        if((capture || moveType == Move.TYPE_PROMOTION_QUEEN) && !underPromotion){

            if(see>0) {
                goodCaptures[goodCaptureIndex] = tempMove;
                goodCapturesScores[goodCaptureIndex++] = see;
            }else{
                equalCaptures[equalCaptureIndex] = tempMove;
                equalCapturesScores[equalCaptureIndex++] = see;
            }

        }
        else{
            nonCaptures[nonCaptureIndex] = tempMove;
            nonCapturesScores[nonCaptureIndex++] = see==SEE_NOT_CALCULATED? search.getMoveScore(move) : SCORE_UNDERPROMOTION;
        }

    }

    private void sortCaptures(int[] captureValues,int[] captures,int num_captures) {
        //Insertion sort of captures.
        for(int i=1;i<num_captures;i++){
            int tempVal = captureValues[i];
            int tempCapture = captures[i];
            int j;
            for(j = i-1; j>=0 && tempVal > captureValues[j];j--){
                captureValues[j+1] = captureValues[j];
                captures[j+1] = captures [j];
            }
            captureValues[j+1] = tempVal;
            captures[j+1] = tempCapture;
        }
    }

    private void generateMovesFromAttacks(int pieceMoved, int fromIndex, long from, long attacks, boolean capture) {
        while (attacks != 0) {
            long to = Long.lowestOneBit(attacks);
            addMove(pieceMoved, fromIndex, to, capture, 0);
            attacks ^= to;
        }
    }
}
