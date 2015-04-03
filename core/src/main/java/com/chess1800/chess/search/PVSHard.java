package com.chess1800.chess.search;

import com.chess1800.chess.board.Chessboard;
import com.chess1800.chess.board.Evaluator;
import com.chess1800.chess.board.MoveGenerator;
import com.chess1800.chess.move.Move;
import com.chess1800.chess.transposition_table.TranspositionTable;

import java.util.ArrayList;

/**
 * Created by Yonathan on 21/12/2014.
 * Main search class, uses alpha-beta algorithm with Principal Variation Search, Iterative Deepening, Quiescence Search, Null Move Pruning and LMR.
 * Based on Winglet by Stef Luijten's Winglet Chess @
 * http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
/**
 * Created by Yonathan on 02/03/2015.
 * PVS with Fail Hard.
 */
public class PVSHard extends PVS {



    public PVSHard(Chessboard b) {
        super(b);
    }

    protected int PVS(int nodeType, int ply, int depth, int alpha, int beta) throws SearchRunException {
        nodes++;
        triangularLength[ply] = ply;
        int distanceToInitialPly = board.getMoveNumber() - initialPly;
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

        if(nodeType == NODE_PV || nodeType == NODE_ROOT){
            if(distanceToInitialPly > selDepth){
                selDepth = distanceToInitialPly;
            }
        }
        if (depth <= 0) {
            return quiescenceSearch(nodeType, ply, alpha, beta);
        }

        // Draw check, evaluate the board if so to check if it's a draw.
        int endGameCheck = board.isDraw();
        if (endGameCheck != Chessboard.NOT_ENDED) {
            if(endGameCheck == Chessboard.DRAW_BY_REP)
            return Evaluator.DRAWSCORE;
        }

        /*// Mate distance pruning
        alpha = Math.max(valueMatedIn(distanceToInitialPly), alpha);
        beta = Math.min(valueMateIn(distanceToInitialPly + 1), beta);
        if(alpha>=beta) return alpha;*/

        //Check Transposition Table
        int hashMove=Move.EMPTY;

        ttProbe++;
        if(transpositionTable.entryExists(board.getKey())){ //if position in transposition table.
            if(transpositionTable.getDepth()>=depth)
                if (nodeType != NODE_ROOT)
                    if (transpositionTable.getFlag() == TranspositionTable.HASH_EXACT) {
                        ttPvHit++;
                        return transpositionTable.getScore();
                    }
                    else if (transpositionTable.getFlag() == TranspositionTable.HASH_ALPHA && transpositionTable.getScore() <= alpha) {
                        ttLBHit++;
                        return transpositionTable.getScore();
                    }
                    else if (transpositionTable.getFlag() == TranspositionTable.HASH_BETA && transpositionTable.getScore() >= beta) {
                        ttUBHit++;
                        return transpositionTable.getScore();
                    }
            hashMove = transpositionTable.getMove();

        }

        int bestScore= -Chessboard.INFINITY;
        int bestMove =Move.EMPTY;
        int score;
        // Try Null move
        if (nullAllowed(nodeType)) {
            null_allowed = false;
            board.makeNullMove();
            score = -PVS(NODE_NULL, ply, depth - NULLMOVE_REDUCTION, -beta, -beta + 1);
            board.unmakeMove();
            null_allowed = true;
            if (score >= beta) {
                return score; //Fail Hard
            }
        }

        null_allowed = true;
        int movesFound = 0;
        //int pvMovesFound = 0;
        //int startIndex=0;
        int[] nonCaptures = new int[MoveGenerator.MAX_MOVES * 2];
        int[] nonCapturesScores = new int[MoveGenerator.MAX_MOVES * 2];
        int[] captures = new int[MoveGenerator.MAX_MOVES * 2];
        int[] goodCaptures = new int [MoveGenerator.MAX_MOVES * 2]; //for good & equal captures as rated by SEE/ queen promotions
        int[] badCaptures = new int [MoveGenerator.MAX_MOVES * 2]; //for bad captures as rated by SEE, also for underPromotions
        int nonCapturesCount=0;
        int capturesCount;
        int goodCaptureCount=0;
        int[] goodCapturesScores= new int [MoveGenerator.MAX_MOVES * 2];
        int[] badCapturesScores = new int [MoveGenerator.MAX_MOVES * 2];
        int badCaptureCount=0;
        int move = Move.EMPTY;
        int generationState = PHASE_HASH;
        while(generationState < PHASE_END) {
            switch (generationState) {
                case PHASE_HASH:
                    generationState++;
                    if (hashMove != Move.EMPTY) {
                        move = hashMove;
                        break;
                    }
                case PHASE_GOOD_CAPTURES_AND_PROMOS:
                    capturesCount = board.generateCaptures(captures, 0, hashMove);
                    for (int i=0; i < capturesCount; i++){
                        int sEEScore= board.sEE(captures[i]);
                        if(Move.isUnderPromotion(captures[i])){
                            nonCaptures[nonCapturesCount] = captures[i];
                            nonCapturesScores[nonCapturesCount++] = SCORE_UNDERPROMOTION;
                        }
                        else if(sEEScore>=0){
                            goodCaptures[goodCaptureCount] = captures[i];
                            goodCapturesScores[goodCaptureCount++] = sEEScore;
                        }else{
                            badCaptures[badCaptureCount] = captures[i];
                            badCapturesScores[badCaptureCount++] = sEEScore;
                        }
                    }
                    move= sortMoves(goodCaptures, goodCaptureCount, goodCapturesScores);
                    if (move != Move.EMPTY) break;
                    generationState++;
                    //END PHASE_GOOD_CAPTURES_AND_PROMOS
                case PHASE_NON_CAPTURES:
                    int initialNonCaptureCount = nonCapturesCount; //Don't include underPromotion captures in the scoring of non-captures.
                    nonCapturesCount= board.generateNonCaptures(nonCaptures, nonCapturesCount, hashMove);
                    for(int i=initialNonCaptureCount; i< nonCapturesCount; i++){
                        if(board.isWhiteToMove())
                            nonCapturesScores[i]= whiteHeuristics[Move.getFromIndex(nonCaptures[i])] [Move.getToIndex(nonCaptures[i])];
                        else
                            nonCapturesScores[i]= blackHeuristics[Move.getFromIndex(nonCaptures[i])] [Move.getToIndex(nonCaptures[i])];
                    }
                    move = sortMoves(nonCaptures, nonCapturesCount, nonCapturesScores);
                    if(move!=Move.EMPTY) {
                        break;
                    }
                    generationState++;
                 //END PHASE_NON_CAPTURES
                case PHASE_BAD_CAPTURES:
                    move = sortMoves(badCaptures, badCaptureCount, badCapturesScores);
                    if (move != Move.EMPTY) break;
                    else generationState++;
                    move=Move.EMPTY;


            }
            if(move!=Move.EMPTY) {
                if (board.makeMove(move)) {
                    movesFound++;
                    int lowBound = (alpha > bestScore ? alpha : bestScore);
                    if (movesFound == 1 && (nodeType == NODE_PV || nodeType == NODE_ROOT))
                        score = -PVS(NODE_PV, ply + 1, depth - 1, -beta, -lowBound); //PV move search
                    else {
                        if (movesFound > LATEMOVE_THRESHOLD && depth > LATEMOVE_DEPTH_THRESHOLD && !board.isCheck() && !Move.isCapture(move) && !Move.isPromotion(move))
                            score = -PVS(NODE_NULL, ply + 1, depth - 2, -lowBound - 1, -lowBound); //LMR
                        else
                            score = -PVS(NODE_NULL, ply + 1, depth - 1, -lowBound - 1, -lowBound); // Null Window Search}

                        if ((score > alpha) && (score < beta)) {
                            score = -PVS(NODE_PV, ply + 1, depth - 1, -beta, -alpha); //Better move found, normal alpha-beta.
                        }
                    }
                    board.unmakeMove();

                    if (score > bestScore) {
                        bestMove = move;
                        bestScore = score;
                        if (nodeType == NODE_ROOT) {
                            globalBestMove = move;
                            globalBestScore = score;
                            globalBestMoveTime = System.currentTimeMillis() - startTime;
                            moveFound = true;
                            if (depth > 6) {
                                notifyMoveFound(move, score);
                            }
                        }
                    }

                    // alpha/beta cut (fail high)
                    if (score >= beta) {
                        break;
                    }
                }
            }else break; //no more moves


        }

        //try the first legal move with an open window.
//        int j, pvIndex = 0;
//        for (int i = 0; i < num_moves; i++) {
//            selectBestMoveFirst(moves, num_moves, ply, depth, i);
//            if (board.makeMove(moves[i])) {
//                pvIndex = i;
//                movesFound++;
//                score = -PVS(NODE_PV, ply + 1, depth - 1, -beta, -alpha);
//                board.unmakeMove();
//                if (score > alpha) {
//                    if (score >= beta) { //beta cutoff
//                        transpositionTable.record(board.getKey(), depth, alpha, beta, score, bestMove );
//                        if (!Move.isCapture(moves[i])) //Non Capture save to History Array
//                            if (board.isWhiteToMove())
//                                whiteHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])] += depth * depth;
//                            else
//                                blackHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])] += depth * depth;
//                        return beta; //fail hard
//                    }
//                    bestMove=moves[i];
//                    pvMovesFound++;
//                    triangularArray[ply][ply] = moves[i];    //save the move
//                    for (j = ply + 1; j < triangularLength[ply + 1]; j++)
//                        triangularArray[ply][j] = triangularArray[ply + 1][j];  //appends latest best PV from deeper plies
//
//                    triangularLength[ply] = triangularLength[ply + 1];
//                    if (ply == 0) rememberPV();
//                    alpha = score;  // alpha improved
//                }
//                break; //First legal move only.
//            }
//        }
//        for (int i = pvIndex; i < num_moves; i++) {
//            selectBestMoveFirst(moves, num_moves, ply, depth, i);
//
//            if (board.makeMove(moves[i])) {
//                movesFound++;
//                //Late Move Reduction
//                if (movesFound > LATEMOVE_THRESHOLD && depth > LATEMOVE_DEPTH_THRESHOLD && !board.isCheck() && !Move.isCapture(moves[i]) && !Move.isPromotion(moves[i])) {
//                    score = -PVS(NODE_NULL, ply + 1, depth - 2, -alpha - 1, -alpha);
//                } else {
//                    score = -PVS(NODE_PV, ply + 1, depth - 1, -alpha - 1, -alpha); // PVS Search
//                }
//                if ((score > alpha) && (score < beta)) {
//                    score = -PVS(NODE_PV, ply + 1, depth - 1, -beta, -alpha); //Better move found, normal alpha-beta.
//                }
//
//                board.unmakeMove();
//
//                if (score >= beta) {
//                    if (!Move.isCapture(moves[i]))
//                        if (board.isWhiteToMove())
//                            whiteHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])] += depth * depth;
//                        else
//                            blackHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])] += depth * depth;
//                    return beta;
//                }
//                if (score > alpha) {
//                    bestMove = moves[i];
//                    alpha = score;
//                    pvMovesFound++;
//                    triangularArray[ply][ply] = moves[i];    //save the move
//                    for (j = ply + 1; j < triangularLength[ply + 1]; j++)
//                        triangularArray[ply][j] = triangularArray[ply + 1][j];  //appends latest best PV from deeper plies
//
//                    triangularLength[ply] = triangularLength[ply + 1];
//                    if (ply == 0) rememberPV();
//                }
//
//
//            }
//        }
//        if (pvMovesFound != 0) {
//            if (board.isWhiteToMove())
//                whiteHeuristics[Move.getFromIndex(triangularArray[ply][ply])][Move.getToIndex(triangularArray[ply][ply])] += depth * depth;
//            else
//                blackHeuristics[Move.getFromIndex(triangularArray[ply][ply])][Move.getToIndex(triangularArray[ply][ply])] += depth * depth;
//        }

        if (board.getFiftyMove() >= 100) bestScore = Evaluator.DRAWSCORE;                 //Fifty-move rule
        if (movesFound == 0){
            if(board.isCheck()) bestScore =-Evaluator.CHECKMATE +ply-1;
            else bestScore = Evaluator.DRAWSCORE;
        }
        if(bestScore >= beta){
            if(!Move.isCapture(bestMove) && !Move.isPromotion(bestMove)) {
                if (board.isWhiteToMove())
                    whiteHeuristics[Move.getFromIndex(bestMove)][Move.getToIndex(bestMove)] += depth * depth;
                else blackHeuristics[Move.getFromIndex(bestMove)][Move.getToIndex(bestMove)] += depth * depth;
            }
        }

        if(bestMove!=Move.EMPTY)
            transpositionTable.record(board.getKey(), depth, alpha, beta, bestScore, bestMove );
        return bestScore; //Fail Hard
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

    protected int quiescenceSearch(int nodeType, int ply, int alpha, int beta) throws SearchRunException {
        triangularLength[ply] = ply;
        int distanceToInitialPly = board.getMoveNumber() - initialPly;
        //Check if we are in check.
        if (board.isCheck()){
            null_allowed = false;
            return PVS(nodeType, ply, 1, alpha, beta);
        }

        int hashMove=Move.EMPTY;
        boolean foundTT= transpositionTable.entryExists(board.getKey());
        ttProbe++;
        if(foundTT && !(beta - alpha > 1)){ //not pv node
                    if (transpositionTable.getFlag() == TranspositionTable.HASH_EXACT) {
                        ttPvHit++;
                        return transpositionTable.getScore();
                    }
                    else if (transpositionTable.getFlag() == TranspositionTable.HASH_ALPHA && transpositionTable.getScore() <= alpha) {
                        ttLBHit++;
                        return transpositionTable.getScore();
                    }
                    else if (transpositionTable.getFlag() == TranspositionTable.HASH_BETA && transpositionTable.getScore() >= beta) {
                        ttUBHit++;
                        return transpositionTable.getScore();
                    }
            hashMove = transpositionTable.getMove();

        }
        //Standing pat
        int bestScore;
        bestScore = board.eval();
        if (bestScore >= beta) {
            if(!foundTT) transpositionTable.record(board.getKey(), 0, alpha, beta, bestScore, Move.EMPTY);
            return bestScore;
        }
        bestScore = alpha;
        int score;
        int [] captures = new int[MoveGenerator.MAX_MOVES];
        int [] goodCapturesScores= new int [MoveGenerator.MAX_MOVES];
        int [] goodCaptures = new int [MoveGenerator.MAX_MOVES];
        int move=Move.EMPTY;
        int bestMove=Move.EMPTY;
        int captureCount= board.generateCaptures(captures,0,hashMove);
        int goodCaptureCount=0;
        for(int i =0; i<captureCount; i++){
            int sEEScore= board.sEE(captures[i]);
            if(sEEScore>=0){
                goodCaptures[goodCaptureCount] = captures[i];
                goodCapturesScores[goodCaptureCount++] = sEEScore;
            }
        }

        int generationState = PHASE_HASH;
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
            if(move !=Move.EMPTY) {
                if (board.makeMove(move)) {
                    nodes++;
                    score = -quiescenceSearch(nodeType, ply + 1, -beta, -bestScore);
                    board.unmakeMove();
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = move;
                    }
                    if (score >= beta) {
                        //Fail Soft
                        break;
                    }


                }
            }else{
                break;
            }


        }
        if(bestMove!= Move.EMPTY)transpositionTable.record(board.getKey(), 0, alpha, beta, bestScore, bestMove);

        // generate captures & promotions:
        // genCaptures returns a sorted move list
        /*int[] captures = new int[MoveGenerator.MAX_MOVES];
        int num_captures = board.genCaptures(captures);

        for (int i = 0; i < num_captures; i++) {
            if (board.makeMove(captures[i])) {
                score = -quiescenceSearch(nodeType, ply + 1, -beta, -alpha);
                board.unmakeMove();
                if (score > alpha) {
                    if (score >= beta) return score;


                    alpha = score;

                    triangularArray[ply][ply] = captures[i];
                    for (int j = ply + 1; j < triangularLength[ply + 1]; j++) {
                        triangularArray[ply][j] = triangularArray[ply + 1][j];
                    }
                    triangularLength[ply] = triangularLength[ply + 1];

                }

            }
        }*/
        return bestScore; //Fail Soft
    }

    protected void notifyMoveFound(int bestMove, int bestScore) {
        infoNodes = nodes;
        hashFull = transpositionTable.getHashFull();
        setPV(bestMove);
        if (observer != null) {
            observer.info(this);
        }

    }
    protected void setPV(int firstMove){
        int i = 1;
        StringBuilder sb = new StringBuilder();
        ArrayList<Long> keys = new ArrayList<Long>(); // To not repeat keys
        sb.append(Move.toString(firstMove,board));
        board.makeMove(firstMove);
            while (i < 256) {
                if (transpositionTable.entryExists(board.getKey())) {
                    if (transpositionTable.getMove() == Move.EMPTY || keys.contains(board.getKey()))
                        break;

                    keys.add(board.getKey());
                    sb.append(" ");
                    sb.append(Move.toString(transpositionTable.getMove(), board));
                    board.makeMove(transpositionTable.getMove());
                    i++;
                    if (board.isCheckMate()) break;
                }else{
                    break;
                }
            }
        //unmake the Moves
        for (int j = 0; j < i; j++) board.unmakeMove();
        pV =sb.toString();

    }
}