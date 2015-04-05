package com.yonathan.chess.core.search;

import com.yonathan.chess.core.board.Chessboard;

/**
 * Created by Yonathan on 05/04/2015.
 */
public abstract class AbstractSearchThreaded extends AbstractSearch implements Runnable {

    protected AbstractSearchThreaded(Chessboard b){
        super(b);
    }
}
