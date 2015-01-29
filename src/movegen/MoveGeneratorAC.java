package movegen;

import board.Board;
import definitions.Definitions;
import move.MoveAC;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Yonathan on 15/01/2015.
 * Based on Alberto Ruibal's Carballo. Source @ https://github.com/albertoruibal/carballo/ &
 * Ulysse Carion's Godot. Source @ https://github.com/ucarion
 */
public class MoveGeneratorAC implements Definitions {

    private static MoveGeneratorAC instance;
    public MoveGetter moveGetter;
    
    
    public MoveGeneratorAC(){
        moveGetter= MoveGetter.getInstance();
    }
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
    public int getAllMoves(Board b, int[] moves) {
        if (b.whiteToMove)
            return getAllWhiteMoves(b, moves);
        return getAllBlackMoves(b, moves);
    }

    public int getAllLegalMoves(Board b, int[] moves) {
        int lastIndex = getAllMoves(b, moves);
        int j = 0;
        for (int i = 0; i < lastIndex; i++) {
            if (b.makeMove(moves[i])) {
                moves[j++] = moves[i];
                b.unmakeMove();
            }
        }
        return j;
    }

    public int countAllLegalMoves(Board b){
        int[] moves = new int[MAX_MOVES];
        int lastIndex = getAllMoves(b, moves);
        int count = 0;
        for(int i = 0; i<lastIndex; i++){
            if(b.makeMove(moves[i])){
                count++;
                b.unmakeMove();
            }
        }
        return count;
    }

    private int getAllWhiteMoves(Board b, int[] moves) {
        int index = 0;
        index += moveGetter.getWhitePawnMoves(b, moves, index);
        index += moveGetter.getWhiteKnightMoves(b, moves, index);
        index += moveGetter.getWhiteKingMoves(b, moves, index);
        index += moveGetter.getWhiteRookMoves(b, moves, index);
        index += moveGetter.getWhiteBishopMoves(b, moves, index);
        index += moveGetter.getWhiteQueenMoves(b, moves, index);
        return index;
    }

    private int getAllBlackMoves(Board b, int[] moves) {
        int index = 0;
        index += moveGetter.getBlackPawnMoves(b, moves, index);
        index += moveGetter.getBlackKnightMoves(b, moves, index);
        index += moveGetter.getBlackKingMoves(b, moves, index);
        index += moveGetter.getBlackRookMoves(b, moves, index);
        index += moveGetter.getBlackBishopMoves(b, moves, index);
        index += moveGetter.getBlackQueenMoves(b, moves, index);
        return index;
    }

    // Pseudo-Legal capture generator.
    public int genCaptures(Board b, int[] captures) {
        int[] captureValues = new int[MAX_MOVES];
        int num_captures = getAllCaptures(b, captures);
        int val;
        int insertIndex;
        for(int i = 0; i < num_captures; i++){
            val = b.sEE(captures[i]);
            if(val< MINCAPTVAL){
                ArrayUtils.remove(captures, i);
                ArrayUtils.remove(captureValues, i);
                num_captures--;
                i--;
            }
            insertIndex = i;

            //Insert the capture into the correct position. Sorts the captures.
            while(insertIndex >= 0 && captureValues[i] > captureValues[insertIndex]){
                int tempCap = captures[i];
                captures[i] = captures[insertIndex];
                captures[insertIndex] = tempCap;

                int tempCapVal = captureValues[i];
                captureValues[i] = captureValues[insertIndex];
                captureValues[insertIndex] = tempCapVal;

                insertIndex--;
            }
        }
        return 0;
    }

    private int getAllCaptures(Board b, int[] captures) {
        int lastIndex= getAllMoves(b, captures);
        int num_captures=0;
        for(int i=0; i< lastIndex; i++){
            if(MoveAC.isPromotion(captures[i]) || MoveAC.isCapture(captures[i]))
                captures[num_captures++] = captures[i];
        }
        return num_captures;

    }

    public static MoveGeneratorAC getInstance() {
        if(instance==null){
            instance = new MoveGeneratorAC();
            return instance;
        }
        return instance;
    }
}
