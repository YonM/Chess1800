package MoveGen;

import BitBoard.BitOperations;
import BitBoard.BitboardMagicAttacks;
import Board.Board;
import Board.BoardUtils;
import Move.Move;

/**
 * Created by Yonathan on 15/12/2014.
 * PseudoLegal move generator.
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public class MoveGenerator {
    private Board board;
    private Move move;

    private long tempPiece, tempMove, targets, freeSquares;

    private boolean oppSide;
    private BitboardMagicAttacks magicAttacks;
    private int from, to, index;

    public MoveGenerator() {
        move = new Move();
        board = Board.getInstance();
        magicAttacks = BitboardMagicAttacks.getInstance();
    }

    public int moveGen(int index) {
        this.index = index;
        oppSide = board.whiteToMove ? false : true;
        freeSquares = ~board.allPieces;
        if (board.whiteToMove) {
            //White's move
            targets = ~board.whitePieces;
            genWhitePawnMoves();
            genWhiteKnightMoves();
            generateWhiteBishopMoves();
            generateWhiteRookMoves();
            generateWhiteQueenMoves();
            generateWhiteKingMoves();

        } else {
            //Black's move
        }
        return index;
    }


    private void genWhitePawnMoves() {
        move.setPiece(BoardUtils.WHITE_PAWN);
        tempPiece = board.whitePawns;
        while (tempPiece != BoardUtils.EMPTY) {
            setFrom();
            tempMove = magicAttacks.WHITE_PAWN_MOVES[from] & freeSquares; // Add normal moves
            if (BoardUtils.RANKS[from] == 1 && tempMove != 0)
                tempMove |= magicAttacks.WHITE_PAWN_MOVES[from] & freeSquares; // Add double moves
            tempMove |= magicAttacks.WHITE_PAWN_ATTACKS[from] & board.blackPieces; // Add captures
            while (tempMove != 0) {
                setToAndCapture();
                if (BoardUtils.RANKS[to] == 7) { // Add promotions
                    move.setPromotion(BoardUtils.WHITE_QUEEN);
                    board.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(BoardUtils.WHITE_ROOK);
                    board.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(BoardUtils.WHITE_BISHOP);
                    board.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(BoardUtils.WHITE_KNIGHT);
                    board.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(BoardUtils.EMPTY);
                } else {
                    board.moves[index++].moveInt = move.moveInt;
                }
                tempMove ^= BoardUtils.BITSET[to];
            }
            if (board.epSquare != 0)       // Add en-passant captures
                if ((magicAttacks.WHITE_PAWN_ATTACKS[from] & BoardUtils.BITSET[board.epSquare]) != 0) {
                    move.setPromotion(BoardUtils.WHITE_PAWN);
                    move.setCapture(BoardUtils.BLACK_PAWN);
                    move.setTo(board.epSquare);
                    board.moves[index++].moveInt = move.moveInt;
                }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(BoardUtils.EMPTY);
        }

    }

    private void genWhiteKnightMoves() {
        move.setPiece(BoardUtils.WHITE_KNIGHT);
        tempPiece = board.whiteKnights;
        while (tempPiece != 0) {
            setFrom();
            tempMove = magicAttacks.KNIGHT_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private void generateWhiteBishopMoves() {
        move.setPiece(BoardUtils.WHITE_BISHOP);
        tempPiece = board.whiteBishops;
        while (tempPiece != 0) {
            setFrom();
            tempMove = magicAttacks.bishopMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private void generateWhiteRookMoves() {
        move.setPiece(BoardUtils.WHITE_ROOK);
        tempPiece = board.whiteRooks;
        while (tempPiece != 0) {
            setFrom();
            tempMove = magicAttacks.rookMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }

    }

    private void generateWhiteQueenMoves() {
        move.setPiece(BoardUtils.WHITE_QUEEN);
        tempPiece = board.whiteQueens;
        while (tempPiece != 0) {
            setFrom();
            tempMove = magicAttacks.QueenMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private void generateWhiteKingMoves() {
        move.setPiece(BoardUtils.WHITE_KING);
        tempPiece = board.whiteKing;
        while (tempPiece != 0) {
            setFrom();
            tempMove = magicAttacks.KING_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            //00 Castling
            if ((board.castleWhite & board.CANCASTLEOO) != 0) {
                if ((magicAttacks.MASKFG[0] & board.allPieces) == 0) {
                    if (isAttacked(magicAttacks.MASKEG[0], false)) {
                        board.moves[index++].moveInt = magicAttacks.WHITE_OO_CASTLE; //pre generated castle move
                    }
                }
            }

            //OOO Castling
            if ((board.castleWhite & board.CANCASTLEOOO) != 0) {
                if ((magicAttacks.MASKBD[0] & board.allPieces) == 0) {
                    if (!isAttacked(magicAttacks.MASKCE[0], false)) {
                        board.moves[index++].moveInt = magicAttacks.WHITE_OOO_CASTLE; //pre generated castle move
                    }
                }
            }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(BoardUtils.EMPTY);
        }
    }

    private boolean isAttacked(long target, boolean white_to_move) {
        long tempTarget = target, slidingAttackers;
        int to;
        if (white_to_move) {

        } else {

        }
        return false;
    }

    private void setToAndCapture() {
        to = (int) BitOperations.lsb(from);
        move.setTo(to);
        move.setCapture(board.square[to]);
    }

    private void setFrom() {
        from = (int) BitOperations.lsb(tempPiece);
        move.setFrom(from);
    }

}
