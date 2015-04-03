package com.chess1800.chess.board;

import com.chess1800.chess.move.Move;
import com.chess1800.chess.search.Search;

/**
 * Created by Yonathan on 01/04/2015.
 * Staged Move Generator based largely off of Alberto Ruibal's Carballo (Source @ https://githucom/albertoruibal/carballo/) and Mediocre Chess by Jonatan Pettersson (Source @ http://sourceforge.net/projects/mediocrechess/).
 */
public abstract class MoveStagedGenerator extends AbstractBitboardMagicAttacks implements MoveGenerator{
    private int captureIndex;
    private int moveIndex;
    private int goodCaptureIndex;
    private int equalCaptureIndex;
    private int badCaptureIndex;
    private int nonCaptureIndex;
    public final static int GENERATE_ALL = 0;
    public final static int GENERATE_CAPTURES_PROMOS = 1;

    private int ttMove=0;
    private int movesToGenerate;
    // Move generation phases
//

    private int phase;
    //private static final int[] VICTIM_PIECE_VALUES = {0, 100, 325, 330, 500, 975, 10000};
    //private static final int[] AGGRESSOR_PIECE_VALUES = {0, 10, 32, 33, 50, 97, 99};
    private static final int SCORE_PROMOTION_QUEEN = 875;



    public final static int SEE_NOT_CALCULATED = Short.MAX_VALUE;

    private int[] captures = new int[Bitboard.MAX_MOVES*2];
    private int[] goodCaptures = new int[Bitboard.MAX_MOVES*2]; // Stores captures and queen promotions
    private int[] goodCapturesSee = new int[Bitboard.MAX_MOVES*2];
    private int[] goodCapturesScores = new int[Bitboard.MAX_MOVES*2];
    private int[] badCaptures = new int[Bitboard.MAX_MOVES*2]; // Stores captures and queen promotions
    private int[] badCapturesSee = new int[Bitboard.MAX_MOVES*2];
    private int[] badCapturesScores = new int[Bitboard.MAX_MOVES*2];
    private int[] equalCaptures = new int[Bitboard.MAX_MOVES*2]; // Stores captures and queen promotions
    private int[] equalCapturesSee = new int[Bitboard.MAX_MOVES*2];
    private int[] equalCapturesScores = new int[Bitboard.MAX_MOVES*2];
    private int[] nonCaptures = new int[Bitboard.MAX_MOVES*2]; // Stores non captures and underpromotions
    private int[] nonCapturesSee = new int[Bitboard.MAX_MOVES*2];
    private int[] nonCapturesScores = new int[Bitboard.MAX_MOVES*2];

    protected int[] moves;

    private long all;
    private long mines;
    private long others;


    protected boolean whiteToMove;
    protected int castleWhite;
    protected int castleBlack;
    protected long ePSquare;

    private int lastMoveSEE;
    private int move;

    protected int initMoveNumber;

    //Attack Information
    
    //King Indexes
    //private int myKingIndex;
    //public int otherKingIndex;

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
    private Search search;


    public void genMoves(int ttMove, Search search) {
        this.search = search;
        genMoves(ttMove, GENERATE_ALL, search);
    }
    public void genMoves(int ttMove){
        genMoves(ttMove, GENERATE_ALL);
    }
    public void genMoves(int ttMove, int movesToGenerate){
        this.ttMove = ttMove;
        this.movesToGenerate = movesToGenerate;
        lastMoveSEE = 0;
    }
    public void genMoves(int ttMove, int movesToGenerate, Search search) {
        if(movesToGenerate != GENERATE_ALL){
            this.search = search;
        }
        this.ttMove = ttMove;
        this.movesToGenerate = movesToGenerate;
        lastMoveSEE = 0;
    }

    private void initMoveGen() {

        initializeAttackInfo();
        captureIndex = 0;
        goodCaptureIndex = 0;
        badCaptureIndex = 0;
        equalCaptureIndex = 0;
        nonCaptureIndex = 0;
    }

    private void initializeAttackInfo() {
        mines = getMyPieces();
        others = getOpponentPieces();
        allPieces = getAllPieces();
        ePSquare = getEPSquare();
        ePSquare = ePSquare;
        //myKingIndex = square2Index(kings & myPieces);
        //otherKingIndex = square2Index(kings & opponentPieces);


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

    protected void updateAggregateBitboards() {
        whitePieces = whiteKing | whiteQueens | whiteRooks | whiteBishops | whiteKnights | whitePawns;
        blackPieces = blackKing | blackQueens | blackRooks | blackBishops | blackKnights | blackPawns;
        allPieces = whitePieces | blackPieces;
    }

    @Override
    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    /*Staged Move Generation.
    *  Order:
    *  1. TT move
    *  2. Good Captures (Ordered by SEE)
    *  3. Equal Captures
    *  4. Non-Captures (Ordered by History Heuristic)
    *  5. Bad Captures
     */
//    public int next() {
//        switch (phase){
//            case PHASE_TT:
//                phase++;
//                if(ttMove != Move.EMPTY){
//                    lastMoveSEE = Move.isCapture(ttMove) || Move.getMoveType(ttMove)==Move.TYPE_PROMOTION_QUEEN ? sEE(ttMove) : 0;
//                    if(movesToGenerate == GENERATE_ALL || Move.isPromotion(ttMove) || (movesToGenerate == GENERATE_CAPTURES_PROMOS && Move.isCapture(ttMove) && lastMoveSEE >= 0))
//                        return ttMove;
//                }
//            case PHASE_GEN_CAPTURES:
//                initMoveGen();
//                //generateCaptures();
//                phase++;
//            case PHASE_GOOD_CAPTURES_AND_PROMOS:
//                move = selectMove(goodCaptureIndex, goodCaptures, goodCapturesScores);
//                if(move!= Move.EMPTY) return move;
//
//                phase++;
//
//            case PHASE_EQUAL_CAPTURES:
//                move = selectMove(equalCaptureIndex, equalCaptures, equalCapturesScores);
//                if(move!= Move.EMPTY) return move;
//
//                phase++;
//            case PHASE_GEN_NON_CAPTURES:
//                if (movesToGenerate == GENERATE_CAPTURES_PROMOS) {
//                    phase = PHASE_END;
//                    return Move.EMPTY;
//                }
//                //generateNonCaptures();
//                phase++;
//
//            case PHASE_NON_CAPTURES:
//                move = selectMove(nonCaptureIndex, nonCaptures, nonCapturesScores);
//                if (move != Move.EMPTY) return move;
//
//                phase++;
//            case PHASE_BAD_CAPTURES:
//                move = selectMove(badCaptureIndex, badCaptures, badCapturesScores);
//                if (move != Move.EMPTY) return move;
//
//                phase = PHASE_END;
//                return Move.EMPTY;
//        }
//        return Move.EMPTY;
//    }

    public int getAllMoves(int[] moves) {
        this.moves = moves;
        moveIndex=0;
        all = getAllPieces();
        mines = getMyPieces();
        others = getOpponentPieces();

        int index = 0;
        long square = 0x1L;
        while (square != 0) {
            if (isWhiteToMove() == ((square & whitePieces) != 0)) {

                if ((square & (whiteRooks | blackRooks)) != 0) { // Rook
                    generateMovesFromAttacks(Move.ROOK, index, getRookAttacks(index, all) & ~mines);
                } else if ((square & (whiteBishops | blackBishops)) != 0) { // Bishop
                    generateMovesFromAttacks(Move.BISHOP, index, getBishopAttacks(index, all) & ~mines);
                } else if ((square & (whiteQueens | blackQueens)) != 0) { // Queen
                    generateMovesFromAttacks(Move.QUEEN, index, (getRookAttacks(index, all) | getBishopAttacks(index, all)) & ~mines);
                } else if ((square & (whiteKing | blackKing)) != 0) { // King
                    generateMovesFromAttacks(Move.KING, index, king[index] & ~mines);
                } else if ((square & (whiteKnights| blackKnights)) != 0) { // Knight
                    generateMovesFromAttacks(Move.KNIGHT, index, knight[index] & ~mines);
                } else if ((square & (whitePawns | blackPawns)) != 0) { // Pawns
                    if ((square & whitePieces) != 0) {
                        if (((square << 8) & all) == 0) {
                            addMoves(Move.PAWN, index, index + 8, false, 0);
                            // Two squares if it is in he first row
                            if (((square & b2_d) != 0) && (((square << 16) & all) == 0))
                                addMoves(Move.PAWN, index, index + 16, false, 0);
                        }
                        generatePawnCapturesFromAttacks(index, whitePawn[index], ePSquare );
                    } else {
                        if (((square >>> 8) & all) == 0) {
                            addMoves(Move.PAWN, index, index - 8, false, 0);
                            // Two squares if it is in he first row
                            if (((square & b2_u) != 0) && (((square >>> 16) & all) == 0))
                                addMoves(Move.PAWN, index, index - 16, false, 0);
                        }
                        generatePawnCapturesFromAttacks(index, blackPawn[index], ePSquare);
                    }
                }
            }
            square <<= 1;

            index++;
        }

        square = (whiteKing|blackKing) & mines; // my king
        int myKingIndex = -1;
        // Castling: disabled when in check or squares attacked
        if ((((all & (whiteToMove ? 0x06L : 0x0600000000000000L)) == 0 &&
                (whiteToMove ? ((castleWhite & CANCASTLEOO)!=0) : ((castleBlack & CANCASTLEOO)!=0))
        ))) {
            myKingIndex = square2Index(square);
            if (!isCheck() &&
                    !isIndexAttacked((byte) (myKingIndex - 1), isWhiteToMove())
                    && !isIndexAttacked((byte) (myKingIndex - 2), isWhiteToMove()))
                addMoves(Move.KING, myKingIndex, myKingIndex - 2, false, Move.TYPE_KINGSIDE_CASTLING);
        }
        if ((((all & (isWhiteToMove() ? 0x70L : 0x7000000000000000L)) == 0 &&
                (isWhiteToMove() ? ((castleWhite & CANCASTLEOOO)!=0) : ((castleBlack & CANCASTLEOOO)!=0))
        ))) {
            if (myKingIndex == -1) {
                myKingIndex = square2Index(square);
            }
            if (!isCheck() &&
                    !isIndexAttacked((byte) (myKingIndex + 1), isWhiteToMove())
                    && !isIndexAttacked((byte) (myKingIndex + 2), isWhiteToMove()))
                addMoves(Move.KING, myKingIndex, myKingIndex + 2, false, Move.TYPE_QUEENSIDE_CASTLING);
        }
        return moveIndex;
    }

    public int getAllLegalMoves(int[] moves) {
        int moveIndex= getAllMoves(moves);
        int j=0;
        for(int i=0; i<moveIndex; i++){
            if (makeMove(moves[i])) {
                moves[j++] = moves[i];
                unmakeMove();
            }
        }
        return j;
    }

    public boolean legalMovesAvailable(){
        int[] moves = new int[MAX_MOVES];
        int lastIndex = getAllMoves( moves);
        for(int i = 0; i<lastIndex; i++){
            if(makeMove(moves[i])){
                unmakeMove();
                return true;
            }
        }
        return false;
    }

    private void generateMovesFromAttacks(int pieceMoved, int fromIndex, long attacks) {
        while (attacks != 0) {
            long to = Long.lowestOneBit(attacks);
            addMoves(pieceMoved, fromIndex, square2Index(to), ((to & others) != 0), 0);
            attacks ^= to;
        }
    }

    private void generatePawnCapturesFromAttacks(int fromIndex, long attacks, long passant) {
        while (attacks != 0) {
            long to = Long.lowestOneBit(attacks);
            if ((to & others) != 0) {
                addMoves(Move.PAWN, fromIndex, square2Index(to), true, 0);
            } else if ((to & passant) != 0) {
                addMoves(Move.PAWN, fromIndex, square2Index(to), true, Move.TYPE_EN_PASSANT);
            }
            attacks ^= to;
        }
    }

    public long getEPSquare() {
        return ePSquare;
    }
    public long getAllPieces(){
        return (whitePieces | blackPieces);
    }
    public long getMyPieces() {
        return whiteToMove ? whitePieces : blackPieces;
    }
    public int genCaptures(int[] moves){return 0;}

    public int getCastleBlack() {
        return castleBlack;
    }


    public int getCastleWhite() {
        return castleWhite;
    }


    /**
     * Gets what is found at a particular location.
     *
     * @param loc an integer in [0, 63] representing a position.
     * @return a character representing the piece at the passed location.
     */
    public char getPieceAt(int loc) {
        long sq = getSquare[loc];
        if (((whitePawns | blackPawns) & sq) != 0L)
            return (whitePieces & sq) !=0 ? 'P': 'p';
        if (((whiteKnights | blackKnights) & sq) != 0L)
            return (whitePieces & sq) !=0 ? 'N': 'n';
        if (((whiteBishops | blackBishops) & sq) != 0L)
            return (whitePieces & sq) !=0 ? 'B': 'b';
        if (((whiteRooks | blackRooks) & sq) != 0L)
            return (whitePieces & sq) !=0 ? 'R': 'r';
        if (((whiteQueens | blackQueens) & sq) != 0L)
            return (whitePieces & sq) !=0 ? 'Q': 'q';
        if (((whiteKing | blackKing) & sq) != 0L)
            return (whitePieces & sq) !=0 ? 'K': 'k';
        return ' ';
    }

    public long getOpponentPieces() {
        return whiteToMove ? blackPieces : whitePieces;
    }

    public int generateCaptures(int[] moves,int startIndex, int ttMove) {
        this.ttMove = ttMove;
        this.captures = moves;
        captureIndex =startIndex;
        all = getAllPieces();
        mines = getMyPieces();
        others = getOpponentPieces();
        long square = H1;
        int index = 0;
        while (square != 0) {
            if ((square & mines) != 0) {
                if ((square & (whiteRooks | blackRooks)) != 0) { // Rook
                    generateMovesFromAttacks(Move.ROOK, index, getRookAttacks(index, all) & others, true);
                } else if ((square & (whiteBishops | blackBishops)) != 0) { // Bishop
                    generateMovesFromAttacks(Move.BISHOP, index, getBishopAttacks(index, all) & others, true);
                } else if ((square & (whiteQueens | blackQueens)) != 0) { // Queen
                    generateMovesFromAttacks(Move.QUEEN, index, (getRookAttacks(index, all) | getBishopAttacks(index, all)) & others, true);
                } else if ((square & (whiteKing | blackKing)) != 0) { // King
                    generateMovesFromAttacks(Move.KING, index, king[index] & others, true);
                } else if ((square & (whiteKnights | blackKnights)) != 0) { // Knight
                    generateMovesFromAttacks(Move.KNIGHT, index, knight[index] & others, true);
                } else if ((square & (whitePawns | blackPawns)) != 0) { // Pawns
                    if(isWhiteToMove())
                        generatePawnCapturesOrGoodPromos(index,
                                (others | ePSquare) & whitePawn[index]//
                                        | (((square & b2_u) != 0) && (((square << 8) & all) == 0) ? (square << 8) : 0),  // Pushes only if promotion
                                ePSquare);
                    else
                        generatePawnCapturesOrGoodPromos(index,
                                (others | ePSquare) & blackPawn[index] //
                                        | (((square & b2_d) != 0) && (((square >>> 8) & all) == 0) ? (square >>> 8) : 0), // Pushes only if promotion
                                ePSquare);

                }

            }
            index++;
            square <<= 1;
        }

        return captureIndex-startIndex;
    }

    public int generateNonCaptures(int[] moves, int startIndex, int ttMove) {
        this.ttMove = ttMove;
        this.nonCaptures= moves;
        nonCaptureIndex = startIndex;
        long square = H1;
        all = getAllPieces();
        mines = getMyPieces();
        others = getOpponentPieces();
        int index = 0;
        while (square != 0) {
            if ((square & mines) != 0) {
                if ((square & (whiteRooks | blackRooks)) != 0) { // Rook
                    generateMovesFromAttacks(Move.ROOK, index, getRookAttacks(index, all) & ~all, false);
                } else if ((square & (whiteBishops | blackBishops)) != 0) { // Bishop
                    generateMovesFromAttacks(Move.BISHOP, index,  getBishopAttacks(index, all) & ~all, false);
                } else if ((square & (whiteQueens| blackQueens)) != 0) { // Queen
                    generateMovesFromAttacks(Move.QUEEN, index, (getRookAttacks(index, all) | getBishopAttacks(index, all)) & ~all, false);
                } else if ((square & (whiteKing | blackKing)) != 0) { // King
                    generateMovesFromAttacks(Move.KING, index, king[index] & ~all, false);
                } else if ( (square & (whiteKnights | blackKnights))!= 0) { // Knight
                    generateMovesFromAttacks(Move.KNIGHT, index, knight[index] & ~all, false);
                }
                else if ((square & (whitePawns | blackPawns)) != 0) { // Pawns excluding the already generated promos
                    if (isWhiteToMove()) {
                        generatePawnNonCapturesAndBadPromos(index, (((square << 8) & all) == 0 ? (square << 8) : 0) //if the square ahead is empty, add pawn push square
                                | ((square & b2_d) != 0 && (((square << 8) | (square << 16)) & all) == 0 ? (square << 16) : 0)); //if the square ahead and 2nd square ahead is empty, add pawn double push square and we are in the second rank.
                    } else {
                        generatePawnNonCapturesAndBadPromos(index, (((square >>> 8) & all) == 0 ? (square >>> 8) : 0)
                                | ((square & b2_u) != 0 && (((square >>> 8) | (square >>> 16)) & all) == 0 ? (square >>> 16) : 0));
                    }
                }
            }
            index++;
            square <<= 1;
        }
        // Castling: disabled when in check or king route attacked
        square = (whiteKing | blackKing) & mines; // my king
        int myKingIndex = -1;
        // Castling: disabled when in check or squares attacked
        if ((((all & (whiteToMove ? 0x06L : 0x0600000000000000L)) == 0 &&
                (whiteToMove ? ((castleWhite & CANCASTLEOO)!=0) : ((castleBlack & CANCASTLEOO)!=0))
        ))) {
            myKingIndex = square2Index(square);
            if (!isCheck() &&
                    !isIndexAttacked((byte) (myKingIndex - 1), whiteToMove)
                    && !isIndexAttacked((byte) (myKingIndex - 2), whiteToMove))
                addMove(Move.KING, myKingIndex, getSquare[myKingIndex - 2], false, Move.TYPE_KINGSIDE_CASTLING);
        }
        if ((((all & (whiteToMove ? 0x70L : 0x7000000000000000L)) == 0 &&
                (whiteToMove ? ((castleWhite & CANCASTLEOOO)!=0) : ((castleBlack & CANCASTLEOOO)!=0))
        ))) {
            if (myKingIndex == -1) {
                myKingIndex = square2Index(square);
            }
            if (!isCheck() &&
                    !isIndexAttacked((byte) (myKingIndex + 1), whiteToMove)
                    && !isIndexAttacked((byte) (myKingIndex + 2), whiteToMove))
                addMove(Move.KING, myKingIndex, getSquare[myKingIndex + 2 ], false, Move.TYPE_QUEENSIDE_CASTLING);
        }
        return nonCaptureIndex-startIndex;
    }


    private void generatePawnCapturesOrGoodPromos(int fromIndex, long attacks, long passant) {
        while (attacks != 0) {
            long to = Long.lowestOneBit(attacks);
            if ((to & passant) != 0) {
                addMove(Move.PAWN, fromIndex, to, true, Move.TYPE_EN_PASSANT);
            } else {
                boolean capture = (to & others) != 0;
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
                addMove(Move.PAWN, fromIndex, to, false, Move.TYPE_PROMOTION_KNIGHT);
                addMove(Move.PAWN, fromIndex, to, false, Move.TYPE_PROMOTION_ROOK);
                addMove(Move.PAWN, fromIndex,to, false, Move.TYPE_PROMOTION_BISHOP);
            } else {
                addMove(Move.PAWN, fromIndex, to, false, 0);
            }
            attacks ^= to;
        }
    }
//
//
//    private int selectMove(int arrayLength, int[] arrayMoves, int[] arrayScores) {
//        if(arrayLength == 0) return Move.EMPTY;
//        int bestScore = SCORE_LOWEST;
//        int bestIndex = -1;
//        for (int i =0 ;i< arrayLength ; i++){
//            if(arrayScores[i] > bestScore){
//                bestScore = arrayScores[i];
//                bestIndex =i;
//            }
//        }
//        if (bestIndex != -1) {
//            int move = arrayMoves[bestIndex];
//            lastMoveSEE = arrayScores[bestIndex];
//            arrayScores[bestIndex] = SCORE_LOWEST;
//            return move;
//        } else {
//            return Move.EMPTY;
//        }
//    }

    private void addMove(int pieceMoved, int fromIndex, long to, boolean capture, int moveType){
        int toIndex= square2Index(to);
        int tempMove = Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);
        if (tempMove==ttMove) return;
        if (capture || moveType == Move.TYPE_PROMOTION_QUEEN) captures[captureIndex++]=tempMove;
        else nonCaptures[nonCaptureIndex++] = tempMove;


//        if (movesToGenerate != GENERATE_ALL && see<0) return;



//        if(capture && see < 0){
//            badCaptures[badCaptureIndex] = tempMove;
//            badCapturesScores[badCaptureIndex++] = see;
//            return;
//        }
//        boolean underPromotion = moveType == Move.TYPE_PROMOTION_KNIGHT || moveType == Move.TYPE_PROMOTION_ROOK || moveType == Move.TYPE_PROMOTION_BISHOP;
//        if((capture || moveType == Move.TYPE_PROMOTION_QUEEN) && !underPromotion){
//
//            if(see>0) {
//                goodCaptures[goodCaptureIndex] = tempMove;
//                goodCapturesScores[goodCaptureIndex++] = see;
//            }else{
//                equalCaptures[equalCaptureIndex] = tempMove;
//                equalCapturesScores[equalCaptureIndex++] = see;
//            }
//
//        }
//        else{
//            nonCaptures[nonCaptureIndex] = tempMove;
//            nonCapturesScores[nonCaptureIndex++] = see==SEE_NOT_CALCULATED? SEE_NOT_CALCULATED : SCORE_UNDERPROMOTION;
//            //search.getMoveScore(move)
//        }

    }

    protected void addMoves(int pieceMoved, int fromIndex, int toIndex, boolean capture, int moveType) {
        if (pieceMoved == Move.PAWN && (toIndex < 8 || toIndex >= 56)) {
            moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_QUEEN);
            moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_KNIGHT);
            moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_ROOK);
            moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_BISHOP);
        } else {
            moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);
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

    private void generateMovesFromAttacks(int pieceMoved, int fromIndex, long attacks, boolean capture) {
        while (attacks != 0) {
            long to = Long.lowestOneBit(attacks);
            addMove(pieceMoved, fromIndex, to, capture, 0);
            attacks ^= to;
        }
    }

    public final int getColumn(long square) {
        for (int column = 0; column < 8; column++) {
            if ((COLUMN[column] & square) != 0) {
                return column;
            }
        }
        return 0;
    }
}
