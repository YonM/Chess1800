package com.chess1800.core.search;

import com.chess1800.core.board.Chessboard;

/**
 * Created by Yonathan on 24/03/2015.
 */
public abstract class AbstractSearch extends AbstractSearchInfo implements Search{
    protected boolean searching;
    protected boolean initialized;
    protected Thread thread;
    protected int depth;
    protected static Chessboard board;


    protected boolean stopSearch;

    protected AbstractSearch(Chessboard board) {
        initialized= false;
        if(AbstractSearch.board== null){
            AbstractSearch.board = board;
            AbstractSearch.board.initialize();
        }
        initialized= true;
    }

    @Override
    public Chessboard getBoard(){
        return board;
    }

    @Override
    public final boolean isSearching(){
        return searching;
    }


    @Override
    public void findBestMove() throws SearchRunException {

    }

    @Override
    public void go(){
        if(!initialized) return;
        if(!isSearching()){
            try{
                setupRun();
                thread = new Thread(this);
                thread.start();
            }catch (Exception e){

            }
        }
    }

    public final void stop(){
        timeForMove =0;
        depth =0;
        stopSearch = true;
        while(isSearching()){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){

            }
        }
    }

    protected abstract void setupRun();
    protected abstract void finishRun() throws SearchRunException;

}
