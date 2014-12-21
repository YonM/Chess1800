package movegen;
import bitboard.BitboardAttacks;
import bitboard.BitboardMagicAttacks;
import board.Board;
import board.BoardUtils;
import move.Move;

/**
 * Created by Yonathan on 15/12/2014.
 * PseudoLegal move generator.
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public class MoveGenerator {
    private Board board;
    private Move move;
    private static MoveGenerator instance;
    private long tempPiece, tempMove, targets, freeSquares;

    private boolean oppSide;
    private int from, to, index;

    public MoveGenerator() {
        move = new Move();
        board = Board.getInstance();
    }

    public static MoveGenerator getInstance() {
        if (instance == null)
            instance = new MoveGenerator();
        return instance;
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
            genWhiteBishopMoves();
            genWhiteRookMoves();
            genWhiteQueenMoves();
            genWhiteKingMoves();

        } else {
            //Black's move
            targets = ~board.blackPieces;
            genBlackPawnMoves();
            genBlackKnightMoves();
            genBlackBishopMoves();
            genBlackRookMoves();
            genBlackQueenMoves();
            genBlackKingMoves();
        }
        return index;
    }

    private void genBlackPawnMoves() {
        move.setPiece(BoardUtils.BLACK_PAWN);
        tempPiece = board.blackPawns;
        while (tempPiece != BoardUtils.EMPTY) {
            setFrom();
            tempMove = BitboardAttacks.BLACK_PAWN_MOVES[from] & freeSquares; // Add normal moves
            if (BoardUtils.RANKS[from] == 1 && tempMove != 0)
                tempMove |= BitboardAttacks.BLACK_PAWN_MOVES[from] & freeSquares; // Add double moves
            tempMove |= BitboardAttacks.BLACK_PAWN_ATTACKS[from] & board.blackPieces; // Add captures
            while (tempMove != 0) {
                setToAndCapture();
                if (BoardUtils.RANKS[to] == 7) { // Add promotions
                    move.setPromotion(BoardUtils.BLACK_QUEEN);
                    board.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(BoardUtils.BLACK_ROOK);
                    board.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(BoardUtils.BLACK_BISHOP);
                    board.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(BoardUtils.BLACK_KNIGHT);
                    board.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(BoardUtils.EMPTY);
                } else {
                    board.moves[index++].moveInt = move.moveInt;
                }
                tempMove ^= BoardUtils.BITSET[to];
            }
            if (board.ePSquare != 0)       // Add en-passant captures
                if ((BitboardAttacks.BLACK_PAWN_ATTACKS[from] & BoardUtils.BITSET[board.ePSquare]) != 0) {
                    move.setPromotion(BoardUtils.BLACK_PAWN);
                    move.setCapture(BoardUtils.BLACK_PAWN);
                    move.setTo(board.ePSquare);
                    board.moves[index++].moveInt = move.moveInt;
                }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(BoardUtils.EMPTY);
        }

    }

    private void genBlackKnightMoves() {
        move.setPiece(BoardUtils.BLACK_KNIGHT);
        tempPiece = board.blackKnights;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KNIGHT_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private void genBlackBishopMoves() {
        move.setPiece(BoardUtils.BLACK_BISHOP);
        tempPiece = board.blackBishops;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.bishopMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }

    }

    private void genBlackRookMoves() {
        move.setPiece(BoardUtils.BLACK_ROOK);
        tempPiece = board.blackRooks;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.rookMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }

    }

    private void genBlackQueenMoves() {
        move.setPiece(BoardUtils.BLACK_QUEEN);
        tempPiece = board.blackQueens;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.QueenMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }

    }

    private void genBlackKingMoves() {
        move.setPiece(BoardUtils.BLACK_KING);
        tempPiece = board.blackKing;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KING_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            //00 Castling
            if ((board.castleBlack & board.CANCASTLEOO) != 0) {
                if ((BitboardAttacks.MASKFG[1] & board.allPieces) == 0) {
                    if (!isAttacked(BitboardAttacks.MASKEG[1], true)) {
                        board.moves[index++].moveInt = BitboardAttacks.BLACK_OO_CASTLE; //Pre generated castle move
                    }
                }
            }

            //OOO Castling
            if ((board.castleBlack & board.CANCASTLEOOO) != 0) {
                if ((BitboardAttacks.MASKBD[1] & board.allPieces) == 0) {
                    if (!isAttacked(BitboardAttacks.MASKCE[1], true)) {
                        board.moves[index++].moveInt = BitboardAttacks.BLACK_OOO_CASTLE; //Pre generated castle move
                    }
                }
            }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(BoardUtils.EMPTY);
        }

    }


    private void genWhitePawnMoves() {
        move.setPiece(BoardUtils.WHITE_PAWN);
        tempPiece = board.whitePawns;
        while (tempPiece != BoardUtils.EMPTY) {
            setFrom();
            tempMove = BitboardAttacks.WHITE_PAWN_MOVES[from] & freeSquares; // Add normal moves
            if (BoardUtils.RANKS[from] == 1 && tempMove != 0)
                tempMove |= BitboardAttacks.WHITE_PAWN_MOVES[from] & freeSquares; // Add double moves
            tempMove |= BitboardAttacks.WHITE_PAWN_ATTACKS[from] & board.blackPieces; // Add captures
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
            if (board.ePSquare != 0)       // Add en-passant captures
                if ((BitboardAttacks.WHITE_PAWN_ATTACKS[from] & BoardUtils.BITSET[board.ePSquare]) != 0) {
                    move.setPromotion(BoardUtils.WHITE_PAWN);
                    move.setCapture(BoardUtils.BLACK_PAWN);
                    move.setTo(board.ePSquare);
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
            tempMove = BitboardAttacks.KNIGHT_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private void genWhiteBishopMoves() {
        move.setPiece(BoardUtils.WHITE_BISHOP);
        tempPiece = board.whiteBishops;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.bishopMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private void genWhiteRookMoves() {
        move.setPiece(BoardUtils.WHITE_ROOK);
        tempPiece = board.whiteRooks;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.rookMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }

    }

    private void genWhiteQueenMoves() {
        move.setPiece(BoardUtils.WHITE_QUEEN);
        tempPiece = board.whiteQueens;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.QueenMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private void genWhiteKingMoves() {
        move.setPiece(BoardUtils.WHITE_KING);
        tempPiece = board.whiteKing;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KING_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture();
                board.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            //00 Castling
            if ((board.castleWhite & board.CANCASTLEOO) != 0) {
                if ((BitboardAttacks.MASKFG[0] & board.allPieces) == 0) {
                    if (!isAttacked(BitboardAttacks.MASKEG[0], false)) {
                        board.moves[index++].moveInt = BitboardAttacks.WHITE_OO_CASTLE; //Pre generated castle move
                    }
                }
            }

            //OOO Castling
            if ((board.castleWhite & board.CANCASTLEOOO) != 0) {
                if ((BitboardAttacks.MASKBD[0] & board.allPieces) == 0) {
                    if (!isAttacked(BitboardAttacks.MASKCE[0], false)) {
                        board.moves[index++].moveInt = BitboardAttacks.WHITE_OOO_CASTLE; //Pre generated castle move
                    }
                }
            }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(BoardUtils.EMPTY);
        }
    }

    //     ===========================================================================
//     isAttacked is used mainly as a move legality test to see if targetBitmap is
//     attacked by white or black.
//  Returns true at the first attack found, and returns false if no attack is found.
//  It can be used for:
//   - check detection, and
//   - castling legality: test to see if the king passes through, or ends up on,
//     a square that is attacked
//     ============================================================================
    public boolean isAttacked(long target, boolean white_to_move) {
        long tempTarget = target, slidingAttackers;
        int to;
        if (white_to_move) { //Test for attacks from WHITE to target;
            while (tempTarget != 0) {
                to = BoardUtils.getIndexFromBoard(tempTarget);
                if ((board.whitePawns & BitboardAttacks.BLACK_PAWN_ATTACKS[to]) != 0 || (board.whiteKnights & BitboardAttacks.KNIGHT_ATTACKS[to]) != 0
                        || (board.whiteKing & BitboardAttacks.KING_ATTACKS[to]) != 0)
                    return true;
                //File & rank attacks
                slidingAttackers = board.whiteQueens | board.whiteRooks;
                if (slidingAttackers != 0) {
                    if ((BitboardAttacks.RANK_ATTACKS[to][(int) ((board.allPieces & BitboardAttacks.RANKMASK[to]) >>> BitboardAttacks.RANKSHIFT[to])]
                            & slidingAttackers) != 0 ||
                            (BitboardAttacks.FILE_ATTACKS[from][(int) (((board.allPieces & BitboardAttacks.FILEMASK[from]) * BitboardMagicAttacks.FILEMAGIC[from]) >>> 57)] & slidingAttackers) != 0)
                        return true;
                }
                //Diagonal attacks
                slidingAttackers = board.whiteQueens | board.whiteBishops;
                if (slidingAttackers != 0) {
                    if ((BitboardAttacks.DIAGA8H1_ATTACKS[from][(int) (((board.allPieces & BitboardAttacks.DIAGA8H1MASK[from]) * BitboardMagicAttacks.DIAGA8H1MAGIC[from]) >>> 57)] & slidingAttackers) != 0
                            || (BitboardAttacks.DIAGA1H8_ATTACKS[from][(int) (((board.allPieces & BitboardAttacks.DIAGA1H8MASK[from]) * BitboardMagicAttacks.DIAGA1H8MAGIC[from]) >>> 57)] & slidingAttackers) != 0)
                        return true;
                }
                tempTarget ^= BoardUtils.BITSET[to];
            }

        } else {            //test for attacks from BLACK to target;
            while (tempTarget != 0) {
                to = BoardUtils.getIndexFromBoard(tempTarget);
                if ((board.blackPawns & BitboardAttacks.WHITE_PAWN_ATTACKS[to]) != 0 || (board.blackKnights & BitboardAttacks.KNIGHT_ATTACKS[to]) != 0
                        || (board.blackKing & BitboardAttacks.KING_ATTACKS[to]) != 0)
                    return true;
                //File & rank attacks
                slidingAttackers = board.blackQueens | board.blackRooks;
                if (slidingAttackers != 0) {
                    if ((BitboardAttacks.RANK_ATTACKS[to][(int) ((board.allPieces & BitboardAttacks.RANKMASK[to]) >>> BitboardAttacks.RANKSHIFT[to])]
                            & slidingAttackers) != 0 ||
                            (BitboardAttacks.FILE_ATTACKS[from][(int) (((board.allPieces & BitboardAttacks.FILEMASK[from]) * BitboardMagicAttacks.FILEMAGIC[from]) >>> 57)] & slidingAttackers) != 0)
                        return true;
                }
                //Diagonal attacks
                slidingAttackers = board.blackQueens | board.blackBishops;
                if (slidingAttackers != 0) {
                    if ((BitboardAttacks.DIAGA8H1_ATTACKS[from][(int) (((board.allPieces & BitboardAttacks.DIAGA8H1MASK[from]) * BitboardMagicAttacks.DIAGA8H1MAGIC[from]) >>> 57)] & slidingAttackers) != 0
                            || (BitboardAttacks.DIAGA1H8_ATTACKS[from][(int) (((board.allPieces & BitboardAttacks.DIAGA1H8MASK[from]) * BitboardMagicAttacks.DIAGA1H8MAGIC[from]) >>> 57)] & slidingAttackers) != 0)
                        return true;
                }
                tempTarget ^= BoardUtils.BITSET[to];
            }

        }
        return false;
    }

    private void setToAndCapture() {
        to = BoardUtils.getIndexFromBoard(from);
        move.setTo(to);
        move.setCapture(board.square[to]);
    }

    private void setFrom() {
        from = BoardUtils.getIndexFromBoard(tempPiece);
        move.setFrom(from);
    }

}
