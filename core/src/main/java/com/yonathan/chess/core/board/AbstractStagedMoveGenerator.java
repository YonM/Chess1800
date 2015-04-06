package com.yonathan.chess.core.board;

import com.yonathan.chess.core.move.Move;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Yonathan on 01/04/2015.
 * Staged Move Generator based largely off of Alberto Ruibal's Carballo (Source @ https://githucom/albertoruibal/carballo/) and Mediocre Chess by Jonatan Pettersson (Source @ http://sourceforge.net/projects/mediocrechess/).
 */
public abstract class AbstractStagedMoveGenerator extends AbstractBitboardMoveGenerator implements MoveGenerator{
    private int captureIndex;
    private int nonCaptureIndex;
    private int ttMove=0;
    // Move generation phases
    private int[] captures;
    private int[] captureScores;
    private int[] nonCaptures; // Stores non captures and underpromotions

    private long all;
    private long mines;
    private long others;
    private final int QUEEN_PROMOTION = 875; //975-100 (queen value - pawn value)


    public int generateCaptures(int[] moves, int startIndex, int ttMove) {
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
                    if(whiteToMove)
                        generatePawnCapturesOrGoodPromos(index,
                                (others | ePSquare) & whitePawn[index]//
                                        | (((square & b2_u) != 0) && (((square << 8) & all) == 0) ? (square << 8) : 0), // Pushes only if promotion
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

    public int genCaptures(int[] moves){
        captureScores = new int[MAX_MOVES*2];
        generateCaptures(moves,0,0);
        for (int i = 0; i < captureIndex; i++) {
            if(!Move.isCapture(moves[i])) captureScores[i] = QUEEN_PROMOTION;
            else {
                int val = sEE(moves[i]);
                captureScores[i] = val;
                if (val < MINCAPTVAL) {
                    moves = ArrayUtils.remove(moves, i);
                    captureScores = ArrayUtils.remove(captureScores, i);
                    captureIndex--;
                    i--;
                    continue;
                }
            }
        }
        sortCaptures(captureScores,moves,captureIndex);
        return captureIndex;
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
                    generateMovesFromAttacks(Move.BISHOP, index, getBishopAttacks(index, all) & ~all, false);
                } else if ((square & (whiteQueens| blackQueens)) != 0) { // Queen
                    generateMovesFromAttacks(Move.QUEEN, index, (getRookAttacks(index, all) | getBishopAttacks(index, all)) & ~all, false);
                } else if ((square & (whiteKing | blackKing)) != 0) { // King
                    generateMovesFromAttacks(Move.KING, index, king[index] & ~all, false);
                } else if ( (square & (whiteKnights | blackKnights))!= 0) { // Knight
                    generateMovesFromAttacks(Move.KNIGHT, index, knight[index] & ~all, false);
                }
                else if ((square & (whitePawns | blackPawns)) != 0) { // Pawns excluding the already generated promos
                    if (whiteToMove) {
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

    private void addMove(int pieceMoved, int fromIndex, long to, boolean capture, int moveType){
        int toIndex= square2Index(to);
        int tempMove = Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);
        if (tempMove==ttMove) return;
        if (capture || moveType == Move.TYPE_PROMOTION_QUEEN) captures[captureIndex++] = tempMove;
        else nonCaptures[nonCaptureIndex++] = tempMove;

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