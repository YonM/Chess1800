package com.yonathan.chess.core.search;

/**
 * Created by Yonathan on 24/03/2015.
 * Separate search parameters from the Search. Based on Alberto Ruibal's Carballo. Source @ https://githucom/albertoruibal/carballo/
 */
public abstract class AbstractSearchParameters implements SearchParameters {

    // UCI parameters
    // Remaining time
    protected int wTime, bTime;
    // Time increment per move
    protected int wInc, bInc;
    // Moves to the next time control
    protected int movesToGo;
    // Analize x plyes only
    protected int depth = Integer.MAX_VALUE;
    // Search only this number of nodes
    protected int nodes = Integer.MAX_VALUE;
    protected int thinkToDepth;
    // Search movetime seconds
    protected int moveTime = Integer.MAX_VALUE;

    // Think infinite
    protected boolean infinite;
    protected long startTime;

    //Time Management
    //protected int nextTimeCheck;
    protected int timeForMove;
    //protected final static int TIME_CHECK_INTERVAL=10000; //Check every 10,000 nodes.
    protected boolean engineIsWhite;
    protected boolean useFixedDepth;


    @Override
    public void setWTime(int wTime) {
        this.wTime = wTime;
    }

    @Override
    public void setWInc(int wInc) {
        this.wInc = wInc;
    }

    @Override
    public void setBTime(int bTime) {
        this.bTime = bTime;
    }

    @Override
    public void setBInc(int bInc) {
        this.bInc = bInc;
    }

    @Override
    public void setMovesToGo(int movesToGo) {
        this.movesToGo = movesToGo;
    }


    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    @Override
    public void setMoveTime(int moveTime) {
        this.moveTime = moveTime;
    }

    @Override
    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }


    @Override
    public void resetMoveTime(int moveTime) {
        setDefaultParameters();
        setMoveTime(moveTime);
    }


    protected void setSearchParameters() {
        if (moveTime == Integer.MAX_VALUE & !infinite) calculateTime(engineIsWhite);
        else timeForMove = moveTime;
        if (depth != Integer.MAX_VALUE) useFixedDepth = true;
        else useFixedDepth = false;
        thinkToDepth=depth;
        //nextTimeCheck = TIME_CHECK_INTERVAL;
    }


    //Based on Mediocre Chess by Jonatan Pettersson. Source @ http://sourceforge.net/projects/mediocrechess/
    protected void calculateTime(boolean isEngineWhite) {
        int timeLeft = isEngineWhite ? wTime : bTime;
        int increment = isEngineWhite ? wInc : bInc;
        int tempTimeForMove; //Max time allowed for the move.
        int percent = 40; //Percentage of time that will be used. percent=20 -> 5% percent=40 ->2.5%
        tempTimeForMove = timeLeft / percent + increment; //use percent + increment for the move.

        //if the increment addition puts us above the timeleft, set the time for this move to timeleft-0.5s.
        if (tempTimeForMove >= timeLeft) {
            tempTimeForMove = timeLeft - 500;
            //if the timeleft was less than 0.5s then set tempTimeForMove to 0.1 seconds.
            if (tempTimeForMove < 0)
                tempTimeForMove = 100;
        }
        timeForMove = tempTimeForMove;
    }

    public void setDefaultParameters() {
        wTime = 0;
        bTime = 0;
        wInc = 0;
        bInc = 0;
        movesToGo = 0;
        depth = Integer.MAX_VALUE;
        nodes = Integer.MAX_VALUE;
        moveTime = Integer.MAX_VALUE;
        infinite = false;
    }
}