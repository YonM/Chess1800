package com.yonathan.chess.core;

import com.yonathan.chess.core.board.Bitboard;
import com.yonathan.chess.core.search.Search;

/**
 * Created by Yonathan on 05/04/2015.
 */
public class BT2450EloTest {

    Bitboard b;
    Search search;
    private int testPosition(String fen, String moveString, int timeLimit) {
        System.out.println("hello test called");
        int time=0;
        b=new Bitboard();
        if(b.initializeFromFEN(fen)){
            String moveStringArray[] = moveString.split(" ");
            int moves[] = new int[moveStringArray.length];
            System.out.println("move string: " +moveStringArray[0]);
            for(int i=0; i<moves.length; i++){
                moves[i] = b.getMoveFromString(moveStringArray[i], true);
            }
            boolean found = false;
            int moveFound=0;
            for(int move: moves){
                //moveFound=search.findBestMove();
                System.out.println("best move: " +move + " Found move: " + moveFound);
                if(move==moveFound){
                    time+=search.getGlobalBestMoveTime();
                    found = true;
                }
            }
            if(!found)
                return timeLimit;
        }else{
            System.out.println("error in fen string: " + fen);
            return -1;
        }
        return time;
    }
}
