package com.yonathan.chess.core.search;

import com.yonathan.chess.core.board.Chessboard;

/**
 * Created by Yonathan on 24/03/2015.
 */
public abstract class AbstractSearch extends AbstractSearchInfo implements Search {
    protected boolean searching;
    protected boolean initialized;
    protected static Chessboard board;

    protected boolean stopSearch;

    protected AbstractSearch(Chessboard board) {
        initialized = false;
        if (AbstractSearch.board == null) {
            AbstractSearch.board = board;
            AbstractSearch.board.initialize();
        }
        initialized = true;
    }

    @Override
    public Chessboard getBoard() {
        return board;
    }

    @Override
    public final boolean isSearching() {
        return searching;
    }


    @Override
    public void findBestMove() throws SearchRunException {

    }

    @Override
    public void go() {
        if (!initialized) return;
        if (!isSearching()) {
            try {
                setupRun();
                run();
            } catch (Exception e) {

            }
        }
    }

    public void stop() {
        timeForMove = 0;
        thinkToDepth = 0;
        stopSearch = true;
    }

    protected abstract void setupRun();

    protected abstract void finishRun() throws SearchRunException;

}
