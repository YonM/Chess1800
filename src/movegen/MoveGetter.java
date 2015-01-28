package movegen;

import bitboard.BitboardMagicAttacksAC;
import bitboard.BitboardUtilsAC;
import board.Board;
import definitions.Definitions;
import move.MoveAC;

/**
 * Created by Yonathan on 15/01/2015.
 * Based on Ulysse Carion's Godot. Source @ https://github.com/ucarion
 */
public class MoveGetter implements Definitions {
    private static MoveGetter instance;
    private BitboardMagicAttacksAC magics;
    private final long[] kingMoves =
            {0x303L, 0x707L, 0xe0eL, 0x1c1cL, 0x3838L, 0x7070L, 0xe0e0L, 0xc0c0L, 0x30303L, 0x70707L, 0xe0e0eL, 0x1c1c1cL, 0x383838L, 0x707070L, 0xe0e0e0L, 0xc0c0c0L, 0x3030300L, 0x7070700L, 0xe0e0e00L, 0x1c1c1c00L, 0x38383800L, 0x70707000L, 0xe0e0e000L, 0xc0c0c000L, 0x303030000L, 0x707070000L, 0xe0e0e0000L, 0x1c1c1c0000L, 0x3838380000L, 0x7070700000L, 0xe0e0e00000L, 0xc0c0c00000L, 0x30303000000L, 0x70707000000L, 0xe0e0e000000L, 0x1c1c1c000000L, 0x383838000000L, 0x707070000000L, 0xe0e0e0000000L, 0xc0c0c0000000L, 0x3030300000000L, 0x7070700000000L, 0xe0e0e00000000L, 0x1c1c1c00000000L, 0x38383800000000L, 0x70707000000000L, 0xe0e0e000000000L, 0xc0c0c000000000L, 0x303030000000000L, 0x707070000000000L, 0xe0e0e0000000000L, 0x1c1c1c0000000000L, 0x3838380000000000L, 0x7070700000000000L, 0xe0e0e00000000000L, 0xc0c0c00000000000L, 0x303000000000000L, 0x707000000000000L, 0xe0e000000000000L, 0x1c1c000000000000L, 0x3838000000000000L, 0x7070000000000000L, 0xe0e0000000000000L, 0xc0c0000000000000L};
    private final long[] knightMoves =
            {0x20400L, 0x50800L, 0xa1100L, 0x142200L, 0x284400L, 0x508800L, 0xa01000L, 0x402000L, 0x2040004L, 0x5080008L, 0xa110011L, 0x14220022L, 0x28440044L, 0x50880088L, 0xa0100010L, 0x40200020L, 0x204000402L, 0x508000805L, 0xa1100110aL, 0x1422002214L, 0x2844004428L, 0x5088008850L, 0xa0100010a0L, 0x4020002040L, 0x20400040200L, 0x50800080500L, 0xa1100110a00L, 0x142200221400L, 0x284400442800L, 0x508800885000L, 0xa0100010a000L, 0x402000204000L, 0x2040004020000L, 0x5080008050000L, 0xa1100110a0000L, 0x14220022140000L, 0x28440044280000L, 0x50880088500000L, 0xa0100010a00000L, 0x40200020400000L, 0x204000402000000L, 0x508000805000000L, 0xa1100110a000000L, 0x1422002214000000L, 0x2844004428000000L, 0x5088008850000000L, 0xa0100010a0000000L, 0x4020002040000000L, 0x400040200000000L, 0x800080500000000L, 0x1100110a00000000L, 0x2200221400000000L, 0x4400442800000000L, 0x8800885000000000L, 0x100010a000000000L, 0x2000204000000000L, 0x4020000000000L, 0x8050000000000L, 0x110a0000000000L, 0x22140000000000L, 0x44280000000000L, 0x88500000000000L, 0x10a00000000000L, 0x20400000000000L};

    public static MoveGetter getInstance() {
        if(instance==null){
            instance = new MoveGetter();
            return instance;
        }
        return instance;
    }
    public MoveGetter(){
        magics = BitboardMagicAttacksAC.getInstance();
    }
    private long pseudoLegalKnightMoveDestinations(int loc, long targets) {
        return knightMoves[loc] & targets;
    }

    private long pseudoLegalKingMoveDestinations(int loc, long targets) {
        return kingMoves[loc] & targets;
    }
    

    public int getWhiteKingMoves(Board b, int[] moves, int index) {
        long king = b.whiteKing;
        int num_moves_generated = 0;
        int fromIndex = Long.numberOfTrailingZeros(king);
        long movelocs = pseudoLegalKingMoveDestinations(fromIndex, ~b.whitePieces);
        while (movelocs != 0) {
            long toBoard = Long.lowestOneBit(movelocs);
            int toIndex = Long.numberOfTrailingZeros(toBoard);
            boolean capt = (toBoard & b.blackPieces) != 0;
            int move = MoveAC.genMove(fromIndex, toIndex, KING, capt, 0);
            moves[index + num_moves_generated] = move;
            num_moves_generated++;
            movelocs &= ~toBoard;
        }
        if ((b.castleWhite & CANCASTLEOO) != 0) {
            if ((b.whiteKing << 1 & b.allPieces) == 0
                    && (b.whiteKing << 2 & b.allPieces) == 0) {
                if ((b.whiteKing << 3 & b.whiteRooks) != 0) {
                    if (!magics.isSquareAttacked(b, b.whiteKing, true)
                            && !magics.isSquareAttacked(b, b.whiteKing << 1,
                            true)
                            && !magics.isSquareAttacked(b, b.whiteKing << 2,
                            true)) {
                        moves[index + num_moves_generated] =
                                MoveAC.genMove(fromIndex, fromIndex + 2, KING,
                                        false, MoveAC.TYPE_KINGSIDE_CASTLING);
                        num_moves_generated++;
                    }
                }
            }
        }
        if ((b.castleWhite & CANCASTLEOOO) != 0) {
            if ((b.whiteKing >>> 1 & b.allPieces) == 0
                    && (b.whiteKing >>> 2 & b.allPieces) == 0
                    && (b.whiteKing >>> 3 & b.allPieces) == 0) {
                if ((b.whiteKing >>> 4 & b.whiteRooks) != 0) {
                    if (!magics.isSquareAttacked(b, b.whiteKing, true)
                            && !magics.isSquareAttacked(b, b.whiteKing >>> 1,
                            true)
                            && !magics.isSquareAttacked(b, b.whiteKing >>> 2,
                            true)) {
                        moves[index + num_moves_generated] =
                                MoveAC.genMove(fromIndex, fromIndex - 2, KING,
                                        false, MoveAC.TYPE_QUEENSIDE_CASTLING);
                        num_moves_generated++;
                    }
                }
            }
        }
        return num_moves_generated;
    }

    public int getBlackKingMoves(Board b, int[] moves, int index) {
        long king = b.blackKing;
        int num_moves_generated = 0;
        int fromIndex = Long.numberOfTrailingZeros(king);
        long movelocs = pseudoLegalKingMoveDestinations(fromIndex, ~b.blackPieces);
        while (movelocs != 0) {
            long toBoard = Long.lowestOneBit(movelocs);
            int toIndex = Long.numberOfTrailingZeros(toBoard);
            boolean capt = (toBoard & b.whitePieces) != 0;
            int move = MoveAC.genMove(fromIndex, toIndex, KING, capt, 0);
            moves[index + num_moves_generated] = move;
            num_moves_generated++;
            movelocs &= ~toBoard;
        }
        if ((b.castleBlack & CANCASTLEOO) != 0) {
            if ((b.blackKing << 1 & b.allPieces) == 0
                    && (b.blackKing << 2 & b.allPieces) == 0) {
                if ((b.blackKing << 3 & b.blackRooks) != 0) {
                    if (!magics.isSquareAttacked(b, b.blackKing, false)
                            && !magics.isSquareAttacked(b, b.blackKing << 1,
                            false)
                            && !magics.isSquareAttacked(b, b.blackKing << 2,
                            false)) {
                        moves[index + num_moves_generated] =
                                MoveAC.genMove(fromIndex, fromIndex + 2, KING,
                                        false, MoveAC.TYPE_KINGSIDE_CASTLING);
                        num_moves_generated++;
                    }
                }
            }
        }
        if ((b.castleBlack & CANCASTLEOOO) != 0) {
            if ((b.blackKing >>> 1 & b.allPieces) == 0
                    && (b.blackKing >>> 2 & b.allPieces) == 0
                    && (b.blackKing >>> 3 & b.allPieces) == 0) {
                if ((b.blackKing >>> 4 & b.blackRooks) != 0) {
                    if (!magics.isSquareAttacked(b, b.blackKing, false)
                            && !magics.isSquareAttacked(b, b.blackKing >>> 1,
                            false)
                            && !magics.isSquareAttacked(b, b.blackKing >>> 2,
                            false)) {
                        moves[index + num_moves_generated] =
                                MoveAC.genMove(fromIndex, fromIndex - 2, KING,
                                        false, MoveAC.TYPE_QUEENSIDE_CASTLING);
                        num_moves_generated++;
                    }
                }
            }
        }
        return num_moves_generated;
    }

    public int getWhiteKnightMoves(Board b, int[] moves, int index) {
        long knights = b.whiteKnights;
        int num_moves_generated = 0;
        while (knights != 0) {
            long fromBoard = Long.lowestOneBit(knights);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs = pseudoLegalKnightMoveDestinations(fromIndex, ~b.whitePieces);
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & b.blackPieces) != 0;
                int move =
                        MoveAC.genMove(fromIndex, toIndex, KNIGHT, capt,
                                0);
                moves[index + num_moves_generated] = move;
                num_moves_generated++;
                movelocs &= ~to;
            }
            knights &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getBlackKnightMoves(Board b, int[] moves, int index) {
        long knights = b.blackKnights;
        int num_moves_generated = 0;
        while (knights != 0) {
            long fromBoard = Long.lowestOneBit(knights);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs = pseudoLegalKnightMoveDestinations(fromIndex, ~b.blackPieces);
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & b.whitePieces) != 0;
                int move =
                        MoveAC.genMove(fromIndex, toIndex, KNIGHT, capt,
                                0);
                moves[index + num_moves_generated] = move;
                num_moves_generated++;
                movelocs &= ~to;
            }
            knights &= ~fromBoard;
        }
        return num_moves_generated;
    }

    /*
    * OK, this can be done the following way:
    * (1) Check what rank the pawn is on.
    * (2a) If the pawn is on the 7th rank (2nd for black), we can forget about
    * double jump and we must consider promotions.
    * (2b) If the pawn isn't on the 7th, then we proceed normally.
    *
    * Special note: If the pawn is on the a-file, it cannot capture left; the
    * same goes for an h-file pawn and capturing right.
    */
    public int getWhitePawnMoves(Board b, int[] moves, int index) {
        long pawns = b.whitePawns;
        int num_moves_generated = 0;
        while (pawns != 0) {
            long fromBoard = Long.lowestOneBit(pawns);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            if ((fromBoard & BitboardUtilsAC.RANK[RANK_7]) != 0) {
                // promos are possible, no en passant
                if ((fromBoard & BitboardUtilsAC.b_r) == 0 && (fromBoard << 7 & b.blackPieces) != 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 7);
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_QUEEN);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_KNIGHT);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_ROOK);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_BISHOP);
                    num_moves_generated++;
                }
                if ((fromBoard & BitboardUtilsAC.b_l) == 0 && (fromBoard << 9 & b.blackPieces) != 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 9);
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_QUEEN);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_KNIGHT);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_ROOK);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_BISHOP);
                    num_moves_generated++;
                }
                if ((fromBoard << 8 & b.allPieces) == 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 8);
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, false,
                                    MoveAC.TYPE_PROMOTION_QUEEN);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, false,
                                    MoveAC.TYPE_PROMOTION_KNIGHT);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, false,
                                    MoveAC.TYPE_PROMOTION_ROOK);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, false,
                                    MoveAC.TYPE_PROMOTION_BISHOP);
                    num_moves_generated++;
                }
            } else {
                // no promos to worry about, but there is en passant
                if (((fromBoard & BitboardUtilsAC.b_r) == 0)
                        && (((fromBoard << 7 & b.blackPieces) != 0) || Long.numberOfTrailingZeros(fromBoard << 7) == b.ePSquare)) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 7);
                    if (Long.numberOfTrailingZeros(fromBoard << 7) == b.ePSquare)
                        moves[index + num_moves_generated] =
                                MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                        MoveAC.TYPE_EN_PASSANT);
                    else
                        moves[index + num_moves_generated] =
                                MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                        0);
                    num_moves_generated++;
                }
                if (((fromBoard & BitboardUtilsAC.b_l) == 0)
                        && (((fromBoard << 9 & b.blackPieces) != 0) || Long.numberOfTrailingZeros(fromBoard << 9) == b.ePSquare)) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 9);
                    if (Long.numberOfTrailingZeros(fromBoard << 9) == b.ePSquare)
                        moves[index + num_moves_generated] =
                                MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                        MoveAC.TYPE_EN_PASSANT);
                    else
                        moves[index + num_moves_generated] =
                                MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                        0);
                    num_moves_generated++;
                }
                boolean one_square_ahead_clear = false;
                if ((fromBoard << 8 & b.allPieces) == 0) {
                    one_square_ahead_clear = true;
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 8);
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, false,
                                    0);
                    num_moves_generated++;
                }
                if ((fromBoard & BitboardUtilsAC.RANK[RANK_2]) != 0
                        && one_square_ahead_clear && (fromBoard << 16 & b.allPieces) == 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard << 16);
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, false,
                                    0);
                    num_moves_generated++;
                }
            }
            pawns &= ~fromBoard;
        }
        return num_moves_generated;
    }

    /*
    * See description of getWhitePawnMoves for an explanation of logic.
    */
    public int getBlackPawnMoves(Board b, int[] moves, int index) {
        long pawns = b.blackPawns;
        int num_moves_generated = 0;
        while (pawns != 0) {
            long fromBoard = Long.lowestOneBit(pawns);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            if ((fromBoard & BitboardUtilsAC.RANK[RANK_2]) != 0) {
                // promos are possible, no en passant
                if ((fromBoard & BitboardUtilsAC.b_l) == 0 && (fromBoard >>> 7 & b.whitePieces) != 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 7);
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_QUEEN);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_KNIGHT);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_ROOK);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_BISHOP);
                    num_moves_generated++;
                }
                if ((fromBoard & BitboardUtilsAC.b_r) == 0
                        && (fromBoard >>> 9 & b.whitePieces) != 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 9);
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_QUEEN);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_KNIGHT);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_ROOK);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                    MoveAC.TYPE_PROMOTION_BISHOP);
                    num_moves_generated++;
                }
                if ((fromBoard >>> 8 & b.allPieces) == 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 8);
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, false,
                                    MoveAC.TYPE_PROMOTION_QUEEN);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, false,
                                    MoveAC.TYPE_PROMOTION_KNIGHT);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, false,
                                    MoveAC.TYPE_PROMOTION_ROOK);
                    num_moves_generated++;
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, false,
                                    MoveAC.TYPE_PROMOTION_BISHOP);
                    num_moves_generated++;
                }
            } else {
                // no promos to worry about, but there is en passant
                if (((fromBoard & BitboardUtilsAC.b_l) == 0)
                        && (((fromBoard >>> 7 & b.whitePieces) != 0) || Long.numberOfTrailingZeros(fromBoard >>> 7) == b.ePSquare)) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 7);
                    if (Long.numberOfTrailingZeros(fromBoard >>> 7) == b.ePSquare)
                        moves[index + num_moves_generated] =
                                MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                        MoveAC.TYPE_EN_PASSANT);
                    else
                        moves[index + num_moves_generated] =
                                MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                        0);
                    num_moves_generated++;
                }
                if (((fromBoard & BitboardUtilsAC.b_r) == 0)
                        && (((fromBoard >>> 9 & b.whitePieces) != 0) || Long.numberOfTrailingZeros(fromBoard >>> 9) == b.ePSquare)) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 9);
                    if (Long.numberOfTrailingZeros(fromBoard >>> 9) == b.ePSquare)
                        moves[index + num_moves_generated] =
                                MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                        MoveAC.TYPE_EN_PASSANT);
                    else
                        moves[index + num_moves_generated] =
                                MoveAC.genMove(fromIndex, toIndex, PAWN, true,
                                        0);
                    num_moves_generated++;
                }
                boolean one_square_ahead_clear = false;
                if ((fromBoard >>> 8 & b.allPieces) == 0) {
                    one_square_ahead_clear = true;
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 8);
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, false,
                                    0);
                    num_moves_generated++;
                }
                if ((fromBoard & BitboardUtilsAC.RANK[RANK_7]) != 0
                        && one_square_ahead_clear && (fromBoard >>> 16 & b.allPieces) == 0) {
                    int toIndex = Long.numberOfTrailingZeros(fromBoard >>> 16);
                    moves[index + num_moves_generated] =
                            MoveAC.genMove(fromIndex, toIndex, PAWN, false,
                                    0);
                    num_moves_generated++;
                }
            }
            pawns &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getWhiteBishopMoves(Board b, int[] moves, int index) {
        long bishops = b.whiteBishops;
        int num_moves_generated = 0;
        while (bishops != 0) {
            long fromBoard = Long.lowestOneBit(bishops);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs =
                    magics.getBishopAttacks(fromIndex, b.allPieces & ~fromBoard);
            movelocs &= ~b.whitePieces;
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & b.blackPieces) != 0;
                int move =
                        MoveAC.genMove(fromIndex, toIndex, BISHOP, capt,
                                0);
                moves[index + num_moves_generated] = move;
                num_moves_generated++;
                movelocs &= ~to;
            }
            bishops &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getBlackBishopMoves(Board b, int[] moves, int index) {
        long bishops = b.blackBishops;
        int num_moves_generated = 0;
        while (bishops != 0) {
            long fromBoard = Long.lowestOneBit(bishops);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs =
                    magics.getBishopAttacks(fromIndex, b.allPieces & ~fromBoard);
            movelocs &= ~b.blackPieces;
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & b.whitePieces) != 0;
                int move =
                        MoveAC.genMove(fromIndex, toIndex, BISHOP, capt,
                                0);
                moves[index + num_moves_generated] = move;
                num_moves_generated++;
                movelocs &= ~to;
            }
            bishops &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getWhiteRookMoves(Board b, int[] moves, int index) {
        long rooks = b.whiteRooks;
        int num_moves_generated = 0;
        while (rooks != 0) {
            long fromBoard = Long.lowestOneBit(rooks);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs = magics.getRookAttacks(fromIndex, b.allPieces & ~fromBoard);
            movelocs &= ~b.whitePieces;
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & b.blackPieces) != 0;
                int move =
                        MoveAC.genMove(fromIndex, toIndex, ROOK, capt, 0);
                moves[index + num_moves_generated] = move;
                num_moves_generated++;
                movelocs &= ~to;
            }
            rooks &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getBlackRookMoves(Board b, int[] moves, int index) {
        long rooks = b.blackRooks;
        int num_moves_generated = 0;
        while (rooks != 0) {
            long fromBoard = Long.lowestOneBit(rooks);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs = magics.getRookAttacks(fromIndex, b.allPieces & ~fromBoard);
            movelocs &= ~b.blackPieces;
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & b.whitePieces) != 0;
                int move =
                        MoveAC.genMove(fromIndex, toIndex, ROOK, capt, 0);
                moves[index + num_moves_generated] = move;
                num_moves_generated++;
                movelocs &= ~to;
            }
            rooks &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getWhiteQueenMoves(Board b, int[] moves, int index) {
        long queens = b.whiteQueens;
        int num_moves_generated = 0;
        while (queens != 0) {
            long fromBoard = Long.lowestOneBit(queens);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs =
                    magics.getQueenAttacks(fromIndex, b.allPieces & ~fromBoard);
            movelocs &= ~b.whitePieces;
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & b.blackPieces) != 0;
                int move =
                        MoveAC.genMove(fromIndex, toIndex, QUEEN, capt,
                                0);
                moves[index + num_moves_generated] = move;
                num_moves_generated++;
                movelocs &= ~to;
            }
            queens &= ~fromBoard;
        }
        return num_moves_generated;
    }

    public int getBlackQueenMoves(Board b, int[] moves, int index) {
        long queens = b.blackQueens;
        int num_moves_generated = 0;
        while (queens != 0) {
            long fromBoard = Long.lowestOneBit(queens);
            int fromIndex = Long.numberOfTrailingZeros(fromBoard);
            long movelocs =
                    magics.getQueenAttacks(fromIndex, b.allPieces & ~fromBoard);
            movelocs &= ~b.blackPieces;
            while (movelocs != 0) {
                long to = Long.lowestOneBit(movelocs);
                int toIndex = Long.numberOfTrailingZeros(to);
                boolean capt = (to & b.whitePieces) != 0;
                int move =
                        MoveAC.genMove(fromIndex, toIndex, QUEEN, capt,
                                0);
                moves[index + num_moves_generated] = move;
                num_moves_generated++;
                movelocs &= ~to;
            }
            queens &= ~fromBoard;
        }
        return num_moves_generated;
    }

}