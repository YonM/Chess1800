package com.yonathan.chess.core.search;

/**
 * Created by Yonathan on 19/03/2015.
 */
public interface SearchObserver {

    public void bestMove(int bestMove);

    void info(AbstractSearchInfo info);
}
