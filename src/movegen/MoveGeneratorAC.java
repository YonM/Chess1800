package movegen;

import board.Board;
import definitions.Definitions;

/**
 * Created by Yonathan on 15/01/2015.
 */
public class MoveGeneratorAC implements Definitions {

    /**
     * Gets all <i>pseudo-legal</i> moves available for the side to move. If the
     * generated moves need to be legal (and not simply pseudo-legal), then
     * <code>MoveGenerator.getAllLegalMoves</code> should be used instead.
     *
     * @param b     the board to consider
     * @param moves the integer array to write onto
     * @return the number of <i>pseudo-legal</i> moves generated, with the
     * actual moves written onto the passed array.
     */
    public static int getAllMoves(Board b, int[] moves) {
        if (b.whiteToMove)
            return getAllWhiteMoves(b, moves);
        return getAllBlackMoves(b, moves);
    }

    private static int getAllWhiteMoves(Board b, int[] moves) {
        int index = 0;
        index += MoveGetter.getWhitePawnMoves(b, moves, index);
        index += MoveGetter.getWhiteKnightMoves(b, moves, index);
        index += MoveGetter.getWhiteKingMoves(b, moves, index);
        index += MoveGetter.getWhiteRookMoves(b, moves, index);
        index += MoveGetter.getWhiteBishopMoves(b, moves, index);
        index += MoveGetter.getWhiteQueenMoves(b, moves, index);
        return index;
    }

    private static int getAllBlackMoves(Board b, int[] moves) {
        int index = 0;
        index += MoveGetter.getBlackPawnMoves(b, moves, index);
        index += MoveGetter.getBlackKnightMoves(b, moves, index);
        index += MoveGetter.getBlackKingMoves(b, moves, index);
        index += MoveGetter.getBlackRookMoves(b, moves, index);
        index += MoveGetter.getBlackBishopMoves(b, moves, index);
        index += MoveGetter.getBlackQueenMoves(b, moves, index);
        return index;
    }

    public static int genCaptures(Board b, int[] captures) {
        int[] captureValues = new int[MAX_MOVES];
        int num_captures = getAllCaptures(b, captures);
        return 0;
    }

    private static int getAllCaptures(Board b, int[] captures) {
        return 0;
    }
}
