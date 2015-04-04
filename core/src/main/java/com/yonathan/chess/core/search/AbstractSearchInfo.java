package com.yonathan.chess.core.search;

import com.yonathan.chess.core.board.Chessboard;

/**
 * Created by Yonathan on 24/03/2015.
 */
public class AbstractSearchInfo extends AbstractSearchParameters implements SearchInfo {
    String currMove;
    int currMoveNumber;
    protected int nodes;
    protected int infoNodes; //node count for debugging/testing
    // Transposition Table
    protected long ttProbe = 0;
    protected long ttPvHit = 0;
    protected long ttLBHit = 0;
    protected long ttUBHit = 0;
    protected long hashFull=0;

    protected int selDepth;
    protected String pV;
    protected int currentDepth;
    protected long globalBestMoveTime;
    protected int globalBestMove;
    protected int globalBestScore;


    @Override
    public void displaySearchStats() {
        System.out.println("nodes searched: " + nodes);


    }@Override
     public String toString(){
        String info="";
        if (currentDepth != 0) {
            info+="depth ";
            info+=currentDepth;
        }
        if (selDepth != 0) {
            info+= " seldepth ";
            info+=selDepth;
        }
        if((globalBestScore> (Chessboard.CHECKMATE - currentDepth)) || (globalBestScore < -(Chessboard.CHECKMATE - currentDepth))){
            info+= " score mate ";
        }else{
            info += " score cp ";
            info += globalBestScore;
        }
        if(infoNodes!= 0){
            info+=" nodes ";
            info+=infoNodes;
            info += " nps ";
            if(globalBestMoveTime<1000){
                info += infoNodes;
            }else{
                info += nodes/globalBestMoveTime;
            }
        }
        if(hashFull!=0){
            info += " hashfull ";
            info+= hashFull;
        }
        if(pV !=null){
            info+= " pv ";
            info += pV;
        }
        return info;
    }
}