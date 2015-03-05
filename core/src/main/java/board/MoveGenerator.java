package board;

/**
 * Created by Yonathan on 02/03/2015.
 */
public interface MoveGenerator {

    public int getAllMoves(int[] moves);

    public int getAllLegalMoves(int[] moves);

    public boolean legalMovesAvailable();

    public int genCaptures(int[] captures);



}
