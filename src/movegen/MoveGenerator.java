package movegen;
import bitboard.BitboardAttacks;
import bitboard.BitboardMagicAttacks;
import board.Board;
import board.BoardUtils;
import definitions.Definitions;
import move.Move;

/**
 * Created by Yonathan on 15/12/2014.
 * PseudoLegal move generator.
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public class MoveGenerator implements Definitions {
    private static Board b;
    private static Move move;
    private static long tempPiece, tempMove, targets, freeSquares;

    private static boolean oppSide;
    private static int from, to, index;

    static {
        move = new Move();
        b = Board.getInstance();
    }

    public static int moveGen(int index) {
        MoveGenerator.index = index;
        oppSide = b.whiteToMove ? false : true;
        freeSquares = ~b.allPieces;
        if (b.whiteToMove) {
            //White's move
            targets = ~b.whitePieces;
            genWhitePawnMoves();
            genWhiteKnightMoves();
            genWhiteBishopMoves();
            genWhiteRookMoves();
            genWhiteQueenMoves();
            genWhiteKingMoves();

        } else {
            //Black's move
            targets = ~b.blackPieces;
            genBlackPawnMoves();
            genBlackKnightMoves();
            genBlackBishopMoves();
            genBlackRookMoves();
            genBlackQueenMoves();
            genBlackKingMoves();
        }
        return index;
    }

    private static void genBlackPawnMoves() {
        move.setPiece(BLACK_PAWN);
        tempPiece = b.blackPawns;
        while (tempPiece != EMPTY) {
            setFrom();
            tempMove = BitboardAttacks.BLACK_PAWN_MOVES[from] & freeSquares; // Add normal moves
            if (RANKS[from] == 1 && tempMove != 0)
                tempMove |= BitboardAttacks.BLACK_PAWN_MOVES[from] & freeSquares; // Add double moves
            tempMove |= BitboardAttacks.BLACK_PAWN_ATTACKS[from] & b.blackPieces; // Add captures
            while (tempMove != 0) {
                setToAndCapture();
                if (RANKS[to] == 7) { // Add promotions
                    move.setPromotion(BLACK_QUEEN);
                    b.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(BLACK_ROOK);
                    b.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(BLACK_BISHOP);
                    b.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(BLACK_KNIGHT);
                    b.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(EMPTY);
                } else {
                    b.moves[index++].moveInt = move.moveInt;
                }
                tempMove ^= BoardUtils.BITSET[to];
            }
            if (b.ePSquare != 0)       // Add en-passant captures
                if ((BitboardAttacks.BLACK_PAWN_ATTACKS[from] & BoardUtils.BITSET[b.ePSquare]) != 0) {
                    move.setPromotion(BLACK_PAWN);
                    move.setCapture(BLACK_PAWN);
                    move.setTo(b.ePSquare);
                    b.moves[index++].moveInt = move.moveInt;
                }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(EMPTY);
        }

    }

    private static void genBlackKnightMoves() {
        move.setPiece(BLACK_KNIGHT);
        tempPiece = b.blackKnights;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KNIGHT_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture();
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genBlackBishopMoves() {
        move.setPiece(BLACK_BISHOP);
        tempPiece = b.blackBishops;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.bishopMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }

    }

    private static void genBlackRookMoves() {
        move.setPiece(BLACK_ROOK);
        tempPiece = b.blackRooks;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.rookMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }

    }

    private static void genBlackQueenMoves() {
        move.setPiece(BLACK_QUEEN);
        tempPiece = b.blackQueens;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.QueenMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }

    }

    private static void genBlackKingMoves() {
        move.setPiece(BLACK_KING);
        tempPiece = b.blackKing;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KING_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture();
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            //00 Castling
            if ((b.castleBlack & CANCASTLEOO) != 0) {
                if ((BitboardAttacks.MASKFG[1] & b.allPieces) == 0) {
                    if (!isAttacked(BitboardAttacks.MASKEG[1], true)) {
                        b.moves[index++].moveInt = BitboardAttacks.BLACK_OO_CASTLE; //Pre generated castle move
                    }
                }
            }

            //OOO Castling
            if ((b.castleBlack & CANCASTLEOOO) != 0) {
                if ((BitboardAttacks.MASKBD[1] & b.allPieces) == 0) {
                    if (!isAttacked(BitboardAttacks.MASKCE[1], true)) {
                        b.moves[index++].moveInt = BitboardAttacks.BLACK_OOO_CASTLE; //Pre generated castle move
                    }
                }
            }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(EMPTY);
        }

    }


    private static void genWhitePawnMoves() {
        move.setPiece(WHITE_PAWN);
        tempPiece = b.whitePawns;
        while (tempPiece != EMPTY) {
            setFrom();
            tempMove = BitboardAttacks.WHITE_PAWN_MOVES[from] & freeSquares; // Add normal moves
            if (RANKS[from] == 1 && tempMove != 0)
                tempMove |= BitboardAttacks.WHITE_PAWN_MOVES[from] & freeSquares; // Add double moves
            tempMove |= BitboardAttacks.WHITE_PAWN_ATTACKS[from] & b.blackPieces; // Add captures
            while (tempMove != 0) {
                setToAndCapture();
                if (RANKS[to] == 7) { // Add promotions
                    move.setPromotion(WHITE_QUEEN);
                    b.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(WHITE_ROOK);
                    b.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(WHITE_BISHOP);
                    b.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(WHITE_KNIGHT);
                    b.moves[index++].moveInt = move.moveInt;
                    move.setPromotion(EMPTY);
                } else {
                    b.moves[index++].moveInt = move.moveInt;
                }
                tempMove ^= BoardUtils.BITSET[to];
            }
            if (b.ePSquare != 0)       // Add en-passant captures
                if ((BitboardAttacks.WHITE_PAWN_ATTACKS[from] & BoardUtils.BITSET[b.ePSquare]) != 0) {
                    move.setPromotion(WHITE_PAWN);
                    move.setCapture(BLACK_PAWN);
                    move.setTo(b.ePSquare);
                    b.moves[index++].moveInt = move.moveInt;
                }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(EMPTY);
        }
    }

    private static void genWhiteKnightMoves() {
        move.setPiece(WHITE_KNIGHT);
        tempPiece = b.whiteKnights;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KNIGHT_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture();
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genWhiteBishopMoves() {
        move.setPiece(WHITE_BISHOP);
        tempPiece = b.whiteBishops;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.bishopMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genWhiteRookMoves() {
        move.setPiece(WHITE_ROOK);
        tempPiece = b.whiteRooks;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.rookMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }

    }

    private static void genWhiteQueenMoves() {
        move.setPiece(WHITE_QUEEN);
        tempPiece = b.whiteQueens;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.QueenMoves(from);
            while (tempMove != 0) {
                setToAndCapture();
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genWhiteKingMoves() {
        move.setPiece(WHITE_KING);
        tempPiece = b.whiteKing;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KING_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture();
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            //00 Castling
            if ((b.castleWhite & CANCASTLEOO) != 0) {
                if ((BitboardAttacks.MASKFG[0] & b.allPieces) == 0) {
                    if (!isAttacked(BitboardAttacks.MASKEG[0], false)) {
                        b.moves[index++].moveInt = BitboardAttacks.WHITE_OO_CASTLE; //Pre generated castle move
                    }
                }
            }

            //OOO Castling
            if ((b.castleWhite & CANCASTLEOOO) != 0) {
                if ((BitboardAttacks.MASKBD[0] & b.allPieces) == 0) {
                    if (!isAttacked(BitboardAttacks.MASKCE[0], false)) {
                        b.moves[index++].moveInt = BitboardAttacks.WHITE_OOO_CASTLE; //Pre generated castle move
                    }
                }
            }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(EMPTY);
        }
    }

    /* Gets a bitboard of front-line attackers(both colours) for SEE.
    *  Does not include xray attackers.
    */
    public static long attacksTo(int target) {
        long attacks, attackBoard;

        //Rank & File attacks for Rooks & Queens
        attackBoard = BitboardMagicAttacks.rookMoves(target);
        attacks = (attackBoard & (b.blackQueens | b.whiteQueens | b.blackRooks | b.whiteRooks));


        return 0;
    }

    public static long revealXrayAttackers(long attackers, long nonRemoved, int target, int heading) {
        return 0;
    }


    public static int genCaptures(int i) {
        return 0;
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
    public static boolean isAttacked(long target, boolean white_to_move) {
        long tempTarget = target, slidingAttackers;
        int to;
        if (white_to_move) { //Test for attacks from WHITE to target;
            while (tempTarget != 0) {
                to = BoardUtils.getIndexFromBoard(tempTarget);
                if ((b.whitePawns & BitboardAttacks.BLACK_PAWN_ATTACKS[to]) != 0 || (b.whiteKnights & BitboardAttacks.KNIGHT_ATTACKS[to]) != 0
                        || (b.whiteKing & BitboardAttacks.KING_ATTACKS[to]) != 0)
                    return true;
                //File & rank attacks
                slidingAttackers = b.whiteQueens | b.whiteRooks;
                if (slidingAttackers != 0) {
                    if ((BitboardAttacks.RANK_ATTACKS[to][(int) ((b.allPieces & BitboardAttacks.RANKMASK[to]) >>> BitboardAttacks.RANKSHIFT[to])]
                            & slidingAttackers) != 0 ||
                            (BitboardAttacks.FILE_ATTACKS[from][(int) (((b.allPieces & BitboardAttacks.FILEMASK[from]) * BitboardMagicAttacks.FILEMAGIC[from]) >>> 57)] & slidingAttackers) != 0)
                        return true;
                }
                //Diagonal attacks
                slidingAttackers = b.whiteQueens | b.whiteBishops;
                if (slidingAttackers != 0) {
                    if ((BitboardAttacks.DIAGA8H1_ATTACKS[from][(int) (((b.allPieces & BitboardAttacks.DIAGA8H1MASK[from]) * BitboardMagicAttacks.DIAGA8H1MAGIC[from]) >>> 57)] & slidingAttackers) != 0
                            || (BitboardAttacks.DIAGA1H8_ATTACKS[from][(int) (((b.allPieces & BitboardAttacks.DIAGA1H8MASK[from]) * BitboardMagicAttacks.DIAGA1H8MAGIC[from]) >>> 57)] & slidingAttackers) != 0)
                        return true;
                }
                tempTarget ^= BoardUtils.BITSET[to];
            }

        } else {            //test for attacks from BLACK to target;
            while (tempTarget != 0) {
                to = BoardUtils.getIndexFromBoard(tempTarget);
                if ((b.blackPawns & BitboardAttacks.WHITE_PAWN_ATTACKS[to]) != 0 || (b.blackKnights & BitboardAttacks.KNIGHT_ATTACKS[to]) != 0
                        || (b.blackKing & BitboardAttacks.KING_ATTACKS[to]) != 0)
                    return true;
                //File & rank attacks
                slidingAttackers = b.blackQueens | b.blackRooks;
                if (slidingAttackers != 0) {
                    if ((BitboardAttacks.RANK_ATTACKS[to][(int) ((b.allPieces & BitboardAttacks.RANKMASK[to]) >>> BitboardAttacks.RANKSHIFT[to])]
                            & slidingAttackers) != 0 ||
                            (BitboardAttacks.FILE_ATTACKS[from][(int) (((b.allPieces & BitboardAttacks.FILEMASK[from]) * BitboardMagicAttacks.FILEMAGIC[from]) >>> 57)] & slidingAttackers) != 0)
                        return true;
                }
                //Diagonal attacks
                slidingAttackers = b.blackQueens | b.blackBishops;
                if (slidingAttackers != 0) {
                    if ((BitboardAttacks.DIAGA8H1_ATTACKS[from][(int) (((b.allPieces & BitboardAttacks.DIAGA8H1MASK[from]) * BitboardMagicAttacks.DIAGA8H1MAGIC[from]) >>> 57)] & slidingAttackers) != 0
                            || (BitboardAttacks.DIAGA1H8_ATTACKS[from][(int) (((b.allPieces & BitboardAttacks.DIAGA1H8MASK[from]) * BitboardMagicAttacks.DIAGA1H8MAGIC[from]) >>> 57)] & slidingAttackers) != 0)
                        return true;
                }
                tempTarget ^= BoardUtils.BITSET[to];
            }

        }
        return false;
    }


    private static void setToAndCapture() {
        to = BoardUtils.getIndexFromBoard(from);
        move.setTo(to);
        move.setCapture(b.square[to]);
    }

    private static void setFrom() {
        from = BoardUtils.getIndexFromBoard(tempPiece);
        move.setFrom(from);
    }


}
