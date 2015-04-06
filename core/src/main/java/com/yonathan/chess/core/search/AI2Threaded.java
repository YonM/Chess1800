package com.yonathan.chess.core.search;

import com.yonathan.chess.core.board.Chessboard;

/**
 * Created by Yonathan on 06/04/2015.
 */
public class AI2Threaded extends AI2 {
    protected Thread thread;
    public AI2Threaded(Chessboard b) {
        super(b);
    }

    @Override
    public void go() {
        if (!initialized) return;
        if (!isSearching()) {
            try {
                setupRun();
                thread = new Thread(this);
                thread.start();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void stop(){
        super.stop();
        while (isSearching()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {

            }
        }
    }
}
