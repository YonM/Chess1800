package utilities;

import board.Bitboard;
import board.Chessboard;
import move.Move;

/**
 * Created by Yonathan on 02/02/2015.
 * Mostly taken from Godot @ https://github.com/ucarion/godot/ by Ulysse Carion.
 */
public class SANUtils{

    public static String getSAN(Bitboard b, int move) {
        String san;
        if (Move.getMoveType(move) == Move.TYPE_KINGSIDE_CASTLING) {
            san = "O-O";
            b.makeMove(move);
            if (b.isCheckMate())
                san += "#";
            else if (b.isCheck())
                san += "+";
            b.unmakeMove();
            return san;
        }
        if (Move.getMoveType(move) == Move.TYPE_QUEENSIDE_CASTLING) {
            san = "O-O-O";
            b.makeMove(move);
            if (b.isCheckMate())
                san += "#";
            else if (b.isCheck())
                san += "+";
            b.unmakeMove();
            return san;
        }
        int moveType = Move.getMoveType(move);
        int piece = Move.getPieceMoved(move);
        int fromIndex = Move.getFromIndex(move);
        int toIndex = Move.getToIndex(move);
        int from_file = fromIndex % 8;
        int from_rank = fromIndex / 8;

        boolean amb_file = false, amb_rank = false, amb_move = false;

        int[] moves = new int[Bitboard.MAX_MOVES];
        int num_moves = b.getAllLegalMoves(moves);

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

        b.makeMove(move);
        if (b.isCheckMate())
            san += "#";
        else if (b.isCheck())
            san += "+";
        b.unmakeMove();

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


    public static String moveToString(int move) {
        String moveString= "";
        if(Move.getMoveType(move) == Move.TYPE_KINGSIDE_CASTLING)
            return "0-0";
        if(Move.getMoveType(move) == Move.TYPE_QUEENSIDE_CASTLING)
            return "0-0-0";
        switch (Move.getPieceMoved(move)) {
            case Move.KNIGHT:
                moveString += "N";
                break;
            case Move.BISHOP:
                moveString += "B";
                break;
            case Move.ROOK:
                moveString += "R";
                break;
            case Move.QUEEN:
                moveString += "Q";
                break;
            case Move.KING:
                moveString += "K";
                break;
            default:
                moveString = "";
                break;
        }
        moveString += intToAlgebraicLoc(Move.getFromIndex(move));
        if (Move.isCapture(move))
            moveString += "x";
        else
            moveString += "-";
        moveString += intToAlgebraicLoc(Move.getToIndex(move));

        switch (Move.getMoveType(move)) {
            case Move.TYPE_EN_PASSANT:
                moveString += " e.p.";
                break;
            case Move.TYPE_PROMOTION_BISHOP:
                moveString += "=B";
                break;
            case Move.TYPE_PROMOTION_KNIGHT:
                moveString += "=N";
                break;
            case Move.TYPE_PROMOTION_ROOK:
                moveString += "=R";
                break;
            case Move.TYPE_PROMOTION_QUEEN:
                moveString += "=Q";
                break;
        }
        return moveString;
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
        return outc + "" + upc;
    }

    /**
     * Converts an integer location in [0, 64) to a string in ['a', 'h']
     * representing its column (aka file).
     *
     * @param loc
     *            an int in [0, 64) representing a location.
     * @return the string representing the file of the location.
     */
    public static String intColToString(int loc) {
        return (char) ( ( (loc % 8) + 'a')) + "";
    }

    /**
     * Converts an integer location in [0, 64) to a string in ['a', 'h']
     * representing its rank (aka row).
     *
     * @param loc
     *            an int in [0, 64) representing a location.
     * @return the string representing the rank of the location.
     */
    public static String intRowToString(int loc) {
        return (char) ( ( (loc / 8) + '1')) + "";
    }
}
