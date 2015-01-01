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
    private static Move move;
    private static long tempPiece, tempMove, targets, freeSquares;

    private static boolean oppSide;
    private static int from, to, index;

    private static int oldIndex;

    static {
        move = new Move();
    }

    public static int moveGen(Board b, int index) {
        MoveGenerator.index = index;
        oppSide = !b.whiteToMove;
        freeSquares = ~b.allPieces;
        if (b.whiteToMove) {
            //White's move
            targets = ~b.whitePieces;
            genWhitePawnMoves(b);
            genWhiteKnightMoves(b);
            genWhiteBishopMoves(b);
            genWhiteRookMoves(b);
            genWhiteQueenMoves(b);
            genWhiteKingMoves(b);

        } else {
            //Black's move
            targets = ~b.blackPieces;
            genBlackPawnMoves(b);
            genBlackKnightMoves(b);
            genBlackBishopMoves(b);
            genBlackRookMoves(b);
            genBlackQueenMoves(b);
            genBlackKingMoves(b);
        }
        return MoveGenerator.index;
    }

    private static void genBlackPawnMoves(Board b) {
        move.setPiece(BLACK_PAWN);
        tempPiece = b.blackPawns;
        while (tempPiece != EMPTY) {
            setFrom();
            tempMove = BitboardAttacks.BLACK_PAWN_MOVES[from] & freeSquares; // Add normal moves
            if (RANKS[from] == 6 && tempMove != 0)
                tempMove |= BitboardAttacks.BLACK_PAWN_DOUBLE_MOVES[from] & freeSquares; // Add double moves
            tempMove |= BitboardAttacks.BLACK_PAWN_ATTACKS[from] & b.whitePieces; // Add captures
            while (tempMove != 0) {
                setToAndCapture(b);
                if (RANKS[to] == 1) { // Add promotions
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
                    if ((b.whitePawns & BoardUtils.BITSET[b.ePSquare + 8]) != 0) {
                        move.setPromotion(BLACK_PAWN);
                        move.setCapture(WHITE_PAWN);
                        move.setTo(b.ePSquare);
                        b.moves[index++].moveInt = move.moveInt;
                    }
                }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(EMPTY);
        }

    }

    private static void genBlackKnightMoves(Board b) {
        move.setPiece(BLACK_KNIGHT);
        tempPiece = b.blackKnights;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KNIGHT_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genBlackBishopMoves(Board b) {
        move.setPiece(BLACK_BISHOP);
        tempPiece = b.blackBishops;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.bishopMoves(b, from);
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }

    }

    private static void genBlackRookMoves(Board b) {
        move.setPiece(BLACK_ROOK);
        tempPiece = b.blackRooks;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.rookMoves(b, from);
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }

    }

    private static void genBlackQueenMoves(Board b) {
        move.setPiece(BLACK_QUEEN);
        tempPiece = b.blackQueens;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.QueenMoves(b, from);
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }

    }

    private static void genBlackKingMoves(Board b) {
        move.setPiece(BLACK_KING);
        tempPiece = b.blackKing;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KING_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            //00 Castling
            if ((b.castleBlack & CANCASTLEOO) != 0) {
                if ((BitboardAttacks.MASKFG[1] & b.allPieces) == 0) {
                    if (!isAttacked(b, BitboardAttacks.MASKEG[1], true)) {
                        b.moves[index++].moveInt = BLACK_OO_CASTLE; //Pre generated castle move
                    }
                }
            }

            //OOO Castling
            if ((b.castleBlack & CANCASTLEOOO) != 0) {
                if ((BitboardAttacks.MASKBD[1] & b.allPieces) == 0) {
                    if (!isAttacked(b, BitboardAttacks.MASKCE[1], true)) {
                        b.moves[index++].moveInt = BLACK_OOO_CASTLE; //Pre generated castle move
                    }
                }
            }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(EMPTY);
        }

    }


    private static void genWhitePawnMoves(Board b) {
        move.setPiece(WHITE_PAWN);
        tempPiece = b.whitePawns;
        while (tempPiece != EMPTY) {
            setFrom();
            tempMove = BitboardAttacks.WHITE_PAWN_MOVES[from] & freeSquares; // Add normal moves
//            System.out.println(tempMove + " <=temp Moves");
            if (RANKS[from] == 1 && tempMove != 0)
                tempMove |= BitboardAttacks.WHITE_PAWN_DOUBLE_MOVES[from] & freeSquares; // Add double moves
            tempMove |= BitboardAttacks.WHITE_PAWN_ATTACKS[from] & b.blackPieces; // Add captures
            while (tempMove != 0) {
                setToAndCapture(b);
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
                    if ((b.blackPawns & BoardUtils.BITSET[b.ePSquare - 8]) != 0) {
                        move.setPromotion(WHITE_PAWN);
                        move.setCapture(BLACK_PAWN);
                        move.setTo(b.ePSquare);
                        b.moves[index++].moveInt = move.moveInt;
                    }
                }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(EMPTY);
        }
    }

    private static void genWhiteKnightMoves(Board b) {
        move.setPiece(WHITE_KNIGHT);
        tempPiece = b.whiteKnights;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KNIGHT_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genWhiteBishopMoves(Board b) {
        move.setPiece(WHITE_BISHOP);
        tempPiece = b.whiteBishops;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.bishopMoves(b, from);
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genWhiteRookMoves(Board b) {
        move.setPiece(WHITE_ROOK);
        tempPiece = b.whiteRooks;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.rookMoves(b, from);
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genWhiteQueenMoves(Board b) {
        move.setPiece(WHITE_QUEEN);
        tempPiece = b.whiteQueens;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.QueenMoves(b, from);
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genWhiteKingMoves(Board b) {
        move.setPiece(WHITE_KING);
        tempPiece = b.whiteKing;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KING_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index++].moveInt = move.moveInt;
                tempMove ^= BoardUtils.BITSET[to];
            }
            //00 Castling
            if ((b.castleWhite & CANCASTLEOO) != 0) {
                if ((BitboardAttacks.MASKFG[0] & b.allPieces) == 0) {
                    if (!isAttacked(b, BitboardAttacks.MASKEG[0], false)) {
                        b.moves[index++].moveInt = BitboardAttacks.WHITE_OO_CASTLE; //Pre generated castle move
                    }
                }
            }

            //OOO Castling
            if ((b.castleWhite & CANCASTLEOOO) != 0) {
                if ((BitboardAttacks.MASKBD[0] & b.allPieces) == 0) {
                    if (!isAttacked(b, BitboardAttacks.MASKCE[0], false)) {
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
    public static long getAttacksTo(Board b, int target) {
        long attacks, attackBoard;

        //Rank & File attacks for Rooks & Queens
        attackBoard = BitboardMagicAttacks.rookMoves(b, target);
        attacks = (attackBoard & (b.blackQueens | b.whiteQueens | b.blackRooks | b.whiteRooks));

        //Diagonal attacks for Bishops & Queens
        attackBoard = BitboardMagicAttacks.bishopMoves(b, target);
        attacks |= (attackBoard & (b.blackQueens | b.whiteQueens | b.blackBishops | b.whiteBishops));

        //Knight attacks
        attackBoard = BitboardMagicAttacks.KNIGHT_ATTACKS[target];
        attacks |= (attackBoard & (b.blackKnights | b.whiteKnights));

        //White pawn attacks (except En passant)
        attackBoard = BitboardMagicAttacks.BLACK_PAWN_ATTACKS[target];
        attacks |= (attackBoard & (b.whitePawns));

        //Black pawn attacks (except En passant)
        attackBoard = BitboardMagicAttacks.WHITE_PAWN_ATTACKS[target];
        attacks |= (attackBoard & (b.blackPawns));

        //King attacks
        attackBoard = BitboardMagicAttacks.KING_ATTACKS[target];
        attacks |= (attackBoard & (b.whiteKing | b.blackKing));

        return attacks;
    }
    // getXrayAttackers returns X-ray attackers after an attacker has been removed.

    public static long getXrayAttackers(Board b, long attackers, long nonRemoved, int target, int heading) {
        int state;
        switch (heading) {
            case EAST:
                targets = BoardUtils.RAY_E[target] & ((b.whiteRooks | b.whiteQueens | b.blackRooks | b.blackQueens) & nonRemoved);
                if (targets != 0) {
                    state = (int) ((b.allPieces & nonRemoved & BitboardAttacks.RANKMASK[target]) >> RANKSHIFT[target]);
                    targets = BitboardMagicAttacks.RANK_ATTACKS[target][state] & targets;
                    return (attackers | targets);
                }
                return attackers;

            case NORTHWEST:
                targets = BoardUtils.RAY_NW[target] & ((b.whiteBishops | b.whiteQueens | b.blackBishops | b.blackQueens) & nonRemoved);
                if (targets != 0) {
                    state = (int) ((b.allPieces & nonRemoved & BitboardAttacks.DIAGA8H1MASK[target]) * BitboardMagicAttacks.DIAGA8H1MAGIC[target]) >> 57;
                    targets = BitboardMagicAttacks.DIAGA8H1_ATTACKS[target][state] & targets;
                    return (attackers | targets);
                }
                return attackers;

            case NORTH:
                targets = BoardUtils.RAY_N[target] & ((b.whiteRooks | b.whiteQueens | b.blackRooks | b.blackQueens) & nonRemoved);
                if (targets != 0) {
                    state = (int) ((b.allPieces & nonRemoved & BitboardAttacks.FILEMASK[target]) * BitboardMagicAttacks.FILEMAGIC[target]) >> 57;
                    targets = BitboardMagicAttacks.FILE_ATTACKS[target][state] & targets;
                    return (attackers | targets);
                }
                return attackers;

            case NORTHEAST:
                targets = BoardUtils.RAY_NE[target] & ((b.whiteBishops | b.whiteQueens | b.blackBishops | b.blackQueens) & nonRemoved);
                if (targets != 0) {
                    state = (int) ((b.allPieces & nonRemoved & BitboardAttacks.DIAGA1H8MASK[target]) * BitboardMagicAttacks.DIAGA1H8MAGIC[target]) >> 57;
                    targets = BitboardMagicAttacks.DIAGA1H8_ATTACKS[target][state] & targets;
                    return (attackers | targets);
                }
                return attackers;

            case WEST:
                targets = BoardUtils.RAY_W[target] & ((b.whiteRooks | b.whiteQueens | b.blackRooks | b.blackQueens) & nonRemoved);
                if (targets != 0) {
                    state = (int) ((b.allPieces & nonRemoved & BitboardAttacks.RANKMASK[target]) >> RANKSHIFT[target]);
                    targets = BitboardMagicAttacks.RANK_ATTACKS[target][state] & targets;
                    return (attackers | targets);
                }
                return attackers;

            case SOUTHEAST:
                targets = BoardUtils.RAY_SE[target] & ((b.whiteBishops | b.whiteQueens | b.blackBishops | b.blackQueens) & nonRemoved);
                if (targets != 0) {
                    state = (int) ((b.allPieces & nonRemoved & BitboardAttacks.DIAGA8H1MASK[target]) * BitboardMagicAttacks.DIAGA8H1MAGIC[target]) >> 57;
                    targets = BitboardMagicAttacks.DIAGA8H1_ATTACKS[target][state] & targets;
                    return (attackers | targets);
                }
                return attackers;

            case SOUTH:
                targets = BoardUtils.RAY_S[target] & ((b.whiteRooks | b.whiteQueens | b.blackRooks | b.blackQueens) & nonRemoved);
                if (targets != 0) {
                    state = (int) ((b.allPieces & nonRemoved & BitboardAttacks.FILEMASK[target]) * BitboardMagicAttacks.FILEMAGIC[target]) >> 57;
                    targets = BitboardMagicAttacks.FILE_ATTACKS[target][state] & targets;
                    return (attackers | targets);
                }
                return attackers;

            case SOUTHWEST:
                targets = BoardUtils.RAY_SW[target] & ((b.whiteBishops | b.whiteQueens | b.blackBishops | b.blackQueens) & nonRemoved);
                if (targets != 0) {
                    state = (int) ((b.allPieces & nonRemoved & BitboardAttacks.DIAGA1H8MASK[target]) * BitboardMagicAttacks.DIAGA1H8MAGIC[target]) >> 57;
                    targets = BitboardMagicAttacks.DIAGA1H8_ATTACKS[target][state] & targets;
                    return (attackers | targets);
                }
                return attackers;


        }
        return attackers;
    }

    /*  Generates pseudo-legal captures & promotions. Parameter passed is the index of the first free slot in the
    *   moves array of the board and the new first free locations is returned.
    *   Used for SEE, sorts the move list (by SEE) and removes bad captures.
    */

    public static int genCaptures(Board b, int i) {
        oldIndex = i;
        MoveGenerator.index = i;
        move = new Move(0);
        oppSide = !b.whiteToMove;
        freeSquares = ~b.allPieces;

        if (b.whiteToMove) {
            //White's move
            targets = b.blackPieces; //Ensures only captures
            genWhitePawnCaps(b);
            genWhiteKnightCaps(b);
            genWhiteBishopCaps(b);
            genWhiteRookCaps(b);
            genWhiteQueenCaps(b);
            genWhiteKingCaps(b);
        } else {
            targets = b.whitePieces;
            genBlackPawnCaps(b);
            genBlackKnightCaps(b);
            genBlackBishopCaps(b);
            genBlackRookCaps(b);
            genBlackQueenCaps(b);
            genBlackKingCaps(b);
        }
        return 0;
    }

    private static void genBlackPawnCaps(Board b) {
        move.setPiece(BLACK_PAWN);
        tempPiece = b.blackPawns;
        while (tempPiece != EMPTY) {
            setFrom();
            tempMove = BitboardAttacks.BLACK_PAWN_ATTACKS[from] & targets; // Add captures
            if (RANKS[from] == 1)
                tempMove |= BitboardAttacks.BLACK_PAWN_MOVES[from] & freeSquares; // Add double moves
            while (tempMove != 0) {
                setToAndCapture(b);
                if (RANKS[to] == 0) { // Add promotions
                    move.setPromotion(BLACK_QUEEN);
                    b.moves[index].moveInt = move.moveInt;
                    addCaptureScore(b);
                    index++;
                    move.setPromotion(BLACK_ROOK);
                    b.moves[index].moveInt = move.moveInt;
                    addCaptureScore(b);
                    index++;
                    move.setPromotion(BLACK_BISHOP);
                    b.moves[index].moveInt = move.moveInt;
                    addCaptureScore(b);
                    index++;
                    move.setPromotion(BLACK_KNIGHT);
                    b.moves[index].moveInt = move.moveInt;
                    addCaptureScore(b);
                    index++;
                    move.setPromotion(EMPTY);
                } else {
                    b.moves[index].moveInt = move.moveInt;
                    addCaptureScore(b);
                    index++;
                }
                tempMove ^= BoardUtils.BITSET[to];
            }
            if (b.ePSquare != 0)       // Add en-passant captures
                if ((BitboardAttacks.BLACK_PAWN_ATTACKS[from] & BoardUtils.BITSET[b.ePSquare]) != 0) {
                    move.setPromotion(BLACK_PAWN);
                    move.setCapture(WHITE_PAWN);
                    move.setTo(b.ePSquare);
                    b.moves[index].moveInt = move.moveInt;
                    addCaptureScore(b);
                    index++;
                }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(EMPTY);
        }
    }

    private static void genBlackKnightCaps(Board b) {
        move.setPiece(BLACK_KNIGHT);
        tempPiece = b.blackKnights;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KNIGHT_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index].moveInt = move.moveInt;
                addCaptureScore(b);
                index++;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genBlackBishopCaps(Board b) {
        move.setPiece(BLACK_BISHOP);
        tempPiece = b.blackBishops;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.bishopMoves(b, from);
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index].moveInt = move.moveInt;
                addCaptureScore(b);
                index++;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genBlackRookCaps(Board b) {
        move.setPiece(BLACK_ROOK);
        tempPiece = b.blackRooks;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.rookMoves(b, from);
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index].moveInt = move.moveInt;
                addCaptureScore(b);
                index++;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genBlackQueenCaps(Board b) {
        move.setPiece(BLACK_QUEEN);
        tempPiece = b.blackQueens;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.QueenMoves(b, from);
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index].moveInt = move.moveInt;
                addCaptureScore(b);
                index++;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genBlackKingCaps(Board b) {
        move.setPiece(BLACK_KING);
        tempPiece = b.blackKing;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KING_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index].moveInt = move.moveInt;
                addCaptureScore(b);
                index++;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(EMPTY);
        }
    }

    private static void genWhitePawnCaps(Board b) {
        move.setPiece(WHITE_PAWN);
        tempPiece = b.whitePawns;
        while (tempPiece != EMPTY) {
            setFrom();
            tempMove = BitboardAttacks.WHITE_PAWN_ATTACKS[from] & targets; // Add captures
            if (RANKS[from] == 6)
                tempMove |= BitboardAttacks.WHITE_PAWN_MOVES[from] & freeSquares; // Add double moves
            while (tempMove != 0) {
                setToAndCapture(b);
                if (RANKS[to] == 7) { // Add promotions
                    move.setPromotion(WHITE_QUEEN);
                    b.moves[index].moveInt = move.moveInt;
                    addCaptureScore(b);
                    index++;
                    move.setPromotion(WHITE_ROOK);
                    b.moves[index].moveInt = move.moveInt;
                    addCaptureScore(b);
                    index++;
                    move.setPromotion(WHITE_BISHOP);
                    b.moves[index].moveInt = move.moveInt;
                    addCaptureScore(b);
                    index++;
                    move.setPromotion(WHITE_KNIGHT);
                    b.moves[index].moveInt = move.moveInt;
                    addCaptureScore(b);
                    index++;
                    move.setPromotion(EMPTY);
                } else {
                    b.moves[index].moveInt = move.moveInt;
                    addCaptureScore(b);
                    index++;
                }
                tempMove ^= BoardUtils.BITSET[to];
            }
            if (b.ePSquare != 0)       // Add en-passant captures
                if ((BitboardAttacks.WHITE_PAWN_ATTACKS[from] & BoardUtils.BITSET[b.ePSquare]) != 0) {
                    move.setPromotion(WHITE_PAWN);
                    move.setCapture(BLACK_PAWN);
                    move.setTo(b.ePSquare);
                    b.moves[index].moveInt = move.moveInt;
                    addCaptureScore(b);
                    index++;
                }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(EMPTY);
        }
    }

    private static void genWhiteKnightCaps(Board b) {
        move.setPiece(WHITE_KNIGHT);
        tempPiece = b.whiteKnights;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KNIGHT_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index].moveInt = move.moveInt;
                addCaptureScore(b);
                index++;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genWhiteBishopCaps(Board b) {
        move.setPiece(WHITE_BISHOP);
        tempPiece = b.whiteBishops;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.bishopMoves(b, from);
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index].moveInt = move.moveInt;
                addCaptureScore(b);
                index++;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }


    private static void genWhiteRookCaps(Board b) {
        move.setPiece(WHITE_ROOK);
        tempPiece = b.whiteRooks;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.rookMoves(b, from);
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index].moveInt = move.moveInt;
                addCaptureScore(b);
                index++;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genWhiteQueenCaps(Board b) {
        move.setPiece(WHITE_QUEEN);
        tempPiece = b.whiteQueens;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardMagicAttacks.QueenMoves(b, from);
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index].moveInt = move.moveInt;
                addCaptureScore(b);
                index++;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
        }
    }

    private static void genWhiteKingCaps(Board b) {
        move.setPiece(WHITE_KING);
        tempPiece = b.whiteKing;
        while (tempPiece != 0) {
            setFrom();
            tempMove = BitboardAttacks.KING_ATTACKS[from] & targets;
            while (tempMove != 0) {
                setToAndCapture(b);
                b.moves[index].moveInt = move.moveInt;
                addCaptureScore(b);
                index++;
                tempMove ^= BoardUtils.BITSET[to];
            }
            tempPiece ^= BoardUtils.BITSET[from];
            move.setPromotion(EMPTY);
        }
    }

    private static void addCaptureScore(Board b) {
        int pos, val;
        Move capt;

        capt = b.moves[index];

        val = b.see(capt);

        if (val < MINCAPTVAL) {
            index--;
            return;
        }

        //Insert move into the sorted list, into the correct position.
        pos = index - 1;
        while (pos > oldIndex - 1 && val > b.moves[pos + OFFSET].moveInt) pos--;  //find the correct position
        System.arraycopy(b.moves, pos + 1, b.moves, pos + 2, (index - pos - 1));
        System.arraycopy(b.moves, pos + 1 + OFFSET, b.moves, pos + 2, (index - pos - 1));
        b.moves[pos + 1].moveInt = capt.moveInt;
        b.moves[pos + 1 + OFFSET].moveInt = val;
        return;
    }

    /*     ===========================================================================
    *     isAttacked is used mainly as a move legality test to see if targetBitmap is
    *     attacked by white or black.
    *      Returns true at the first attack found, and returns false if no attack is found.
    *      It can be used for:
    *      - check detection, and
    *      - castling legality: test to see if the king passes through, or ends up on,
    *      a square that is attacked
    *     ============================================================================
    */
    public static boolean isAttacked(Board b, long target, boolean white_to_move) {
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
                    if ((BitboardAttacks.RANK_ATTACKS[to][(int) ((b.allPieces & BitboardAttacks.RANKMASK[to]) >>> RANKSHIFT[to])]
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
                    if ((BitboardAttacks.RANK_ATTACKS[to][(int) ((b.allPieces & BitboardAttacks.RANKMASK[to]) >>> RANKSHIFT[to])]
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


    private static void setToAndCapture(Board b) {
        to = BoardUtils.getIndexFromBoard(tempMove);
        move.setTo(to);
        move.setCapture(b.square[to]);
    }

    private static void setFrom() {
        from = BoardUtils.getIndexFromBoard(tempPiece);
        move.setFrom(from);
    }


}
