package board;

/**
 * Created by Yonathan on 02/03/2015.
 */
public interface MoveGenerator {

    public int getAllMoves(int[] moves);

    public int getAllLegalMoves(int[] moves);

    public boolean legalMovesAvailable();

    public int genCaptures(int[] captures);

    //Maximum moves per position and max game length.
    public static final int MAX_GAME_LENGTH = 1024; // Maximum number of half-moves, if 50-move rule is obeyed.
    public static final int MAX_MOVES = 256;

}
