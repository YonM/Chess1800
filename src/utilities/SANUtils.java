package utilities;

import board.Board;
import definitions.Definitions;
import move.MoveAC;
import movegen.MoveGeneratorAC;

/**
 * Created by Yonathan on 02/02/2015.
 * Mostly taken from Godot @ https://github.com/ucarion/godot/ by Ulysse Carion.
 */
public class SANUtils implements Definitions {

    private static SANUtils instance;
    private MoveGeneratorAC moveGen;

    public static SANUtils getInstance() {
        if (instance == null) {
            instance = new SANUtils();
            return instance;
        }
        return instance;
    }

    private SANUtils() {
        moveGen = MoveGeneratorAC.getInstance();
    }

    public String getSAN(Board b, int move) {
        String san;
        if (MoveAC.getMoveType(move) == TYPE_KINGSIDE_CASTLING) {
            san = "O-O";
            b.makeMove(move);
            if (b.isCheckMate())
                san += "#";
            else if (b.isCheck())
                san += "+";
            b.unmakeMove();
            return san;
        }
        if (MoveAC.getMoveType(move) == TYPE_QUEENSIDE_CASTLING) {
            san = "O-O-O";
            b.makeMove(move);
            if (b.isCheckMate())
                san += "#";
            else if (b.isCheck())
                san += "+";
            b.unmakeMove();
            return san;
        }
        int moveType = MoveAC.getMoveType(move);
        int piece = MoveAC.getPieceMoved(move);
        int fromIndex = MoveAC.getFromIndex(move);
        int toIndex = MoveAC.getToIndex(move);
        int from_file = fromIndex % 8;
        int from_rank = fromIndex / 8;

        boolean amb_file = false, amb_rank = false, amb_move = false;

        int[] moves = new int[Board.MAX_MOVES];
        int num_moves = moveGen.getAllLegalMoves(b, moves);

        for (int i = 0; i < num_moves; i++) {
            if (moves[i] == move || MoveAC.getToIndex(moves[i]) != toIndex)
                continue;

            if (MoveAC.isPromotion(MoveAC.getMoveType(move))) {
                if (MoveAC.getMoveType(moves[i]) != moveType)
                    continue;
            }

            int pieceX = MoveAC.getMoveType(moves[i]);
            if (pieceX != piece)
                continue;

            int sq = MoveAC.getFromIndex(moves[i]);

            int sq_file = sq % 8;
            int sq_rank = sq / 8;

            if (sq_file == from_file)
                amb_file = true;
            if (sq_rank == from_rank)
                amb_rank = true;

            amb_move = true;
        }

        san = moveToPieceString(move);

        // this is the technique Stockfish uses.
        if (amb_move) {
            if (!amb_file)
                san += intColToString(fromIndex);
            else if (!amb_rank)
                san += intRowToString(fromIndex);
            else
                san += intToAlgebraicLoc(fromIndex);
        }
        if (MoveAC.isCapture(move)) {
            if (MoveAC.getPieceMoved(move) == PAWN && !amb_rank)
                san += intColToString(fromIndex);
            san += "x";
        }
        san += intToAlgebraicLoc(toIndex);
        if (MoveAC.isPromotion(MoveAC.getMoveType(move))) {
            san += "=";
            switch (MoveAC.getMoveType(move)) {
                case TYPE_PROMOTION_BISHOP:
                    san += "B";
                    break;
                case TYPE_PROMOTION_KNIGHT:
                    san += "N";
                    break;
                case TYPE_PROMOTION_ROOK:
                    san += "R";
                    break;
                case TYPE_PROMOTION_QUEEN:
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

    private String moveToPieceString(int move) {
        switch (MoveAC.getPieceMoved(move)) {
                case KNIGHT:
                    return "N";
                case BISHOP:
                    return "B";
                case ROOK:
                    return "R";
                case QUEEN:
                    return "Q";
                case KING:
                    return "K";
            }
            return "";
    }


    public  String moveToString(int move) {
        String moveString= "";
        if(MoveAC.getMoveType(move) == TYPE_KINGSIDE_CASTLING)
            return "0-0";
        if(MoveAC.getMoveType(move) == TYPE_QUEENSIDE_CASTLING)
            return "0-0-0";
        switch (MoveAC.getPieceMoved(move)) {
            case KNIGHT:
                moveString += "N";
                break;
            case BISHOP:
                moveString += "B";
                break;
            case ROOK:
                moveString += "R";
                break;
            case QUEEN:
                moveString += "Q";
                break;
            case KING:
                moveString += "K";
                break;
            default:
                moveString = "";
                break;
        }
        moveString += intToAlgebraicLoc(MoveAC.getFromIndex(move));
        if (MoveAC.isCapture(move))
            moveString += "x";
        else
            moveString += "-";
        moveString += intToAlgebraicLoc(MoveAC.getToIndex(move));

        switch (MoveAC.getMoveType(move)) {
            case TYPE_EN_PASSANT:
                moveString += " e.p.";
                break;
            case TYPE_PROMOTION_BISHOP:
                moveString += "=B";
                break;
            case TYPE_PROMOTION_KNIGHT:
                moveString += "=N";
                break;
            case TYPE_PROMOTION_ROOK:
                moveString += "=R";
                break;
            case TYPE_PROMOTION_QUEEN:
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
    public String intToAlgebraicLoc(int loc) {
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
    public String intColToString(int loc) {
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
    public String intRowToString(int loc) {
        return (char) ( ( (loc / 8) + '1')) + "";
    }
}
