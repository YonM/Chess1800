package com.yonathan.chess.core.search;

import com.yonathan.chess.core.board.Chessboard;
import com.yonathan.chess.core.board.Evaluator;
import com.yonathan.chess.core.board.MoveGenerator;
import com.yonathan.chess.core.move.Move;
import com.yonathan.chess.core.transposition_table.TranspositionTable;

import java.util.ArrayList;

/**
 * Created by Yonathan on 02/03/2015.
 * PVS with Fail Hard.
 */
public class AI2 extends PVS {

    public AI2(Chessboard b) {
        super(b);
    }

    protected int PVS(int nodeType, int ply, int depthRemaining, int alpha, int beta) throws SearchRunException {
        nodes++;
        triangularLength[ply] = ply;
        // Check if time is up
        if (!useFixedDepth) {
            //nextTimeCheck--;
            //if(nextTimeCheck == 0) {
            // nextTimeCheck= TIME_CHECK_INTERVAL;
            if (System.currentTimeMillis() - startTime > timeForMove && moveFound && nodeType!=NODE_ROOT) {
                finishRun();
            }
        }
        //}

        if (depthRemaining <= 0) {
            follow_pv = false;
            return quiescenceSearch(nodeType, ply, alpha, beta);
        }

        /*// Draw check, evaluate the board if so to check if it's a draw.
        int drawCheck = board.isDraw();
        if (drawCheck != Chessboard.NOT_ENDED) {
            follow_pv = false;
            return Evaluator.DRAWSCORE;
        }*/

        int endGameCheck = board.isEndOfGame();
        if (endGameCheck != Chessboard.NOT_ENDED) {
            follow_pv = false;
            if (endGameCheck != Chessboard.WHITE_WIN && endGameCheck != Chessboard.BLACK_WIN)
                return Evaluator.DRAWSCORE; //if draw
            return -Evaluator.CHECKMATE + ply - 1; //if checkmate
        }

        int score;
        // Try Null move
        if (nullAllowed()) {
            null_allowed = false;
            board.makeNullMove();
            score = -PVS(NODE_NULL, ply, depthRemaining - NULLMOVE_REDUCTION, -beta, -beta + 1);
            board.unmakeMove();
            null_allowed = true;
            if (score >= beta) {
                return beta; //Fail Hard
            }
        }

        null_allowed = true;
        int movesFound = 0;
        int hashMove=0;
        int j;
        int[] nonCaptures = new int[MoveGenerator.MAX_MOVES * 3];
        int[] nonCapturesScores = new int[MoveGenerator.MAX_MOVES * 3];
        int[] captures = new int[MoveGenerator.MAX_MOVES * 3];
        int[] goodCaptures = new int [MoveGenerator.MAX_MOVES * 3]; //for good & equal captures as rated by SEE/ queen promotions
        int[] badCaptures = new int [MoveGenerator.MAX_MOVES * 3]; //for bad captures as rated by SEE, also for underPromotions
        int nonCapturesCount=0;
        int capturesCount;
        int goodCaptureCount=0;
        int[] goodCapturesScores= new int [MoveGenerator.MAX_MOVES * 3];
        int[] badCapturesScores = new int [MoveGenerator.MAX_MOVES * 3];
        int badCaptureCount=0;
        int move = Move.EMPTY;
        int generationState = PHASE_GOOD_CAPTURES_AND_PROMOS;
        while(generationState < PHASE_END) {
            switch (generationState) {
                case PHASE_GOOD_CAPTURES_AND_PROMOS:
                    capturesCount = board.generateCaptures(captures, 0, hashMove);
                    for (int i = 0; i < capturesCount; i++) {
                        int sEEScore = board.sEE(captures[i]);
                        if (Move.isUnderPromotion(captures[i])) {
                            nonCaptures[nonCapturesCount] = captures[i];
                            nonCapturesScores[nonCapturesCount++] = SCORE_UNDERPROMOTION;
                        } else if (sEEScore >= 0) {
                            goodCaptures[goodCaptureCount] = captures[i];
                            goodCapturesScores[goodCaptureCount++] = sEEScore;
                        } else {
                            badCaptures[badCaptureCount] = captures[i];
                            badCapturesScores[badCaptureCount++] = sEEScore;
                        }
                    }
                    move = sortMoves(goodCaptures, goodCaptureCount, goodCapturesScores);
                    if (move != Move.EMPTY) break;
                    generationState++;
                    //END PHASE_GOOD_CAPTURES_AND_PROMOS
                case PHASE_NON_CAPTURES:
                    int initialNonCaptureCount = nonCapturesCount; //Don't include underPromotion captures in the scoring of non-captures.
                    nonCapturesCount = board.generateNonCaptures(nonCaptures, nonCapturesCount, hashMove);
                    for (int i = initialNonCaptureCount; i < nonCapturesCount; i++) {
                        if (board.isWhiteToMove())
                            nonCapturesScores[i] = whiteHeuristics[Move.getFromIndex(nonCaptures[i])][Move.getToIndex(nonCaptures[i])];
                        else
                            nonCapturesScores[i] = blackHeuristics[Move.getFromIndex(nonCaptures[i])][Move.getToIndex(nonCaptures[i])];
                    }
                    move = sortMoves(nonCaptures, nonCapturesCount, nonCapturesScores);
                    if (move != Move.EMPTY) {
                        break;
                    }
                    generationState++;
                    //END PHASE_NON_CAPTURES
                case PHASE_BAD_CAPTURES:
                    move = sortMoves(badCaptures, badCaptureCount, badCapturesScores);
                    if (move != Move.EMPTY) break;
                    else generationState++;
                    move = Move.EMPTY;


            }
            if (move != Move.EMPTY) {
                if (board.makeMove(move)) {
                    movesFound++;
                    if (movesFound == 1 && (nodeType == NODE_PV || nodeType == NODE_ROOT))
                        score = -PVS(NODE_PV, ply + 1, depthRemaining - 1, -beta, -alpha); //PV move search
                    else {
                        if (movesFound > LATEMOVE_THRESHOLD && depthRemaining > LATEMOVE_DEPTH_THRESHOLD && !board.isCheck() && !Move.isCapture(move) && !Move.isPromotion(move))
                            score = -PVS(NODE_NULL, ply + 1, depthRemaining - 2, -alpha - 1, -alpha); //LMR
                        else
                            score = -PVS(NODE_NULL, ply + 1, depthRemaining - 1, -alpha - 1, -alpha); // Null Window Search}

                        if ((score > alpha) && (score < beta)) {
                            score = -PVS(NODE_PV, ply + 1, depthRemaining - 1, -beta, -alpha); //Better move found, normal alpha-beta.
                        }
                    }
                    if (score > alpha) {
                        if (score >= beta) {
                            if(!Move.isCapture(move) && !Move.isPromotion(move)) {
                                if (board.isWhiteToMove())
                                    whiteHeuristics[Move.getFromIndex(move)][Move.getToIndex(move)] += depthRemaining * depthRemaining;
                                else
                                    blackHeuristics[Move.getFromIndex(move)][Move.getToIndex(move)] += depthRemaining * depthRemaining;
                            }
                            return score;
                        }
                        alpha= score;
                        triangularArray[ply][ply] = move; //save the move
                        for (j = ply + 1; j < triangularLength[ply + 1]; j++)
                            triangularArray[ply][j] = triangularArray[ply + 1][j]; //appends latest best PV from deeper plies

                        triangularLength[ply] = triangularLength[ply + 1];
                        if (ply == 0) rememberPV();
                    }
                    board.unmakeMove();
                }
            }else break; //no more moves
        }//end while loop

        if (board.getFiftyMove() >= 100) return Evaluator.DRAWSCORE;                 //Fifty-move rule
/*
        if (movesFound == 0){
            if(board.isCheck()) return -Evaluator.CHECKMATE +ply-1;
            return Evaluator.DRAWSCORE;
        }*/
        return alpha; //Fail Hard
    }

    protected int quiescenceSearch(int nodeType, int ply, int alpha, int beta) throws SearchRunException {
        triangularLength[ply] = ply;

        //Check if we are in check.
        if (board.isCheck()) return PVS(nodeType, ply, 1, alpha, beta);

        //Standing pat
        int score;
        score = board.eval();
        if (score >= beta) {
            return beta; // fail hard
        }
        if (score > alpha) alpha = score;

        int hashMove =0;
        // generate captures & promotions:
        // generateCaptures returns a sorted move list
        int [] captures = new int[MoveGenerator.MAX_MOVES];
        int [] goodCapturesScores= new int [MoveGenerator.MAX_MOVES];
        int [] goodCaptures = new int [MoveGenerator.MAX_MOVES];
        int move=Move.EMPTY;
        int captureCount= board.generateCaptures(captures,0,hashMove);
        int goodCaptureCount=0;
        for(int i =0; i<captureCount; i++){
            int sEEScore= board.sEE(captures[i]);
            if(sEEScore>=0){
                goodCaptures[goodCaptureCount] = captures[i];
                goodCapturesScores[goodCaptureCount++] = sEEScore;
            }
        }
        int generationState = PHASE_GOOD_CAPTURES_AND_PROMOS;
        while(generationState < PHASE_END){
            switch(generationState){
                case PHASE_HASH:
                    if(hashMove!= Move.EMPTY){
                        if (Move.isCapture(hashMove) && board.sEE(hashMove) >=0){
                            move= hashMove;
                            break;
                        }
                    }
                    generationState=PHASE_GOOD_CAPTURES_AND_PROMOS;
                case PHASE_GOOD_CAPTURES_AND_PROMOS:
                    move=sortMoves(goodCaptures,goodCaptureCount, goodCapturesScores);
                    if(move != Move.EMPTY){
                        break;
                    }
                    generationState=PHASE_END;
            }
            if (move != Move.EMPTY) {
                if (board.makeMove(move)) {
                    score = -quiescenceSearch(nodeType, ply + 1, -beta, -alpha);
                    board.unmakeMove();
                    if (score > alpha) {
                        if (score >= beta) return beta;


                        alpha = score;

                        triangularArray[ply][ply] = move; //save the move
                        for (int j = ply + 1; j < triangularLength[ply + 1]; j++) {
                            triangularArray[ply][j] = triangularArray[ply + 1][j];
                        }
                        triangularLength[ply] = triangularLength[ply + 1];

                    }

                }
            }else break;
        }
        return alpha; //Fail Hard
    }

    //Not used currently
    @Override
    protected void notifyMoveFound(int bestMove, int bestScore) {
        infoNodes = nodes;
        //hashFull = transpositionTable.getHashFull();
        setPV(bestMove);
        if (observer != null) {
            observer.info(this);
        }

    }
    //Not used currently
    protected void setPV(int firstMove){
        int i = 1;
        StringBuilder sb = new StringBuilder();
        ArrayList<Long> keys = new ArrayList<Long>(); // To not repeat keys
        sb.append(Move.toString(firstMove,board));
        board.makeMove(firstMove);
        while (i < 256) {
//            if (transpositionTable.entryExists(board.getKey())) {
//                if (transpositionTable.getMove() == Move.EMPTY || keys.contains(board.getKey()))
//                    break;
//
//                keys.add(board.getKey());
//                sb.append(" ");
//                sb.append(Move.toString(transpositionTable.getMove(), board));
//                board.makeMove(transpositionTable.getMove());
                i++;
                if (board.isCheckMate()) break;
//            }else{
//                break;
//            }
        }
        //unmake the Moves
//        for (int j = 0; j < i; j++) board.unmakeMove();
//        pV =sb.toString();

    }
    private int sortMoves(int[] moves, int arrayLength, int[] moveScores) {
        if(arrayLength == 0){
            return Move.EMPTY;
        }
        int bestScore = SCORE_LOWEST;
        int bestIndex = -1;
        for (int i =0 ; i< arrayLength ; i++){
            if(moves[i] > bestScore){
                bestScore = moveScores[i];
                bestIndex =i;
            }
        }
        if (bestIndex != -1) {
            int move = moves[bestIndex];
            moveScores[bestIndex] = SCORE_LOWEST;
            return move;
        } else {
            return Move.EMPTY;
        }
    }
}

