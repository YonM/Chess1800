package search;

import board.Board;

/**
 * Created by Yonathan on 02/02/2015.
 * Simple Search interface.
 */
public interface Search {

    public int findBestMove(Board b, int depth, int timeLeft, int increment, int moveTime);
}
