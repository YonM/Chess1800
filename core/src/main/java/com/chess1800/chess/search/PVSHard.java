package com.chess1800.chess.search;

import com.chess1800.chess.board.Chessboard;
import com.chess1800.chess.board.Evaluator;
import com.chess1800.chess.board.MoveGenerator;
import com.chess1800.chess.board.MoveSorter;
import com.chess1800.chess.move.Move;
import com.chess1800.chess.transposition_table.TranspositionTable;

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
        super(b, "HARD");
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

        if (depth <= 0) {
            follow_pv = false;
            return quiescenceSearch(nodeType, ply, alpha, beta);
        }

        // Draw check, evaluate the board if so to check if it's a draw.
        int endGameCheck = board.isDraw();
        if (endGameCheck != Chessboard.NOT_ENDED) {
            follow_pv = false;
            return Evaluator.DRAWSCORE;
        }

        /*// Mate distance pruning
        alpha = Math.max(valueMatedIn(distanceToInitialPly), alpha);
        beta = Math.min(valueMateIn(distanceToInitialPly + 1), beta);
        if(alpha>=beta) return alpha;*/

        //Check Transposition Table
        int hashMove=Move.EMPTY;
        boolean foundTT= transpositionTable.entryExists(board.getKey());
        if(foundTT){
            if(transpositionTable.getDepth()>=depth)
                if (nodeType != NODE_ROOT)
                    if (transpositionTable.getFlag() == TranspositionTable.HASH_EXACT)
                        return transpositionTable.getScore();
                    else if (transpositionTable.getFlag() == TranspositionTable.HASH_ALPHA && transpositionTable.getScore() <= alpha)
                        return transpositionTable.getScore();
                    else if (transpositionTable.getFlag() == TranspositionTable.HASH_BETA && transpositionTable.getScore() >= beta)
                        return transpositionTable.getScore();
            hashMove = transpositionTable.getMove();

        }

        int bestScore= -Evaluator.CHECKMATE;
        int bestMove =Move.EMPTY;
        int score = 0;
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
        int pvMovesFound = 0;
        int[] moves = new int[MoveGenerator.MAX_MOVES];
        int num_moves = board.getAllMoves(moves);
        int move = Move.EMPTY;
        MoveSorter moveSorter = moveSorters[distanceToInitialPly];
        moveSorter.genMoves(hashMove, this);
        while((move= moveSorter.next())!=Move.EMPTY){
            if (board.makeMove(move)) {
                movesFound++;
                int lowBound = (alpha > bestScore ? alpha : bestScore);
                if(movesFound==1 && (nodeType == NODE_PV || nodeType == NODE_ROOT)) score = -PVS(NODE_PV, ply + 1, depth - 1, -beta, -lowBound); //PV move search
                else {
                    if (movesFound > LATEMOVE_THRESHOLD && depth > LATEMOVE_DEPTH_THRESHOLD && !board.isCheck() && !Move.isCapture(move) && !Move.isPromotion(move))
                        score = -PVS(NODE_NULL, ply + 1, depth - 2, -lowBound - 1, -lowBound); //LMR
                    else score = -PVS(NODE_NULL, ply + 1, depth - 1, -lowBound - 1, -lowBound); // Null Window Search}

                    if ((score > alpha) && (score < beta)) {
                        score = -PVS(NODE_PV, ply + 1, depth - 1, -beta, -alpha); //Better move found, normal alpha-beta.
                    }
                }
                    board.unmakeMove();

                    if(score >bestScore){
                        bestMove = move;
                        bestScore = score;
                        if(nodeType == NODE_ROOT){
                            globalBestMove = move;
                            //globalBestScore = score;
                            globalBestMoveTime = System.currentTimeMillis() - startTime;
                            moveFound = true;
                        }
                    }

                    // alpha/beta cut (fail high)
                    if (score >= beta) {
                        break;
                    }
                }

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

        if(bestMove!=Move.EMPTY)
            transpositionTable.record(board.getKey(), depth, alpha, beta, bestScore, bestMove );
        return bestScore; //Fail Hard
    }

    protected int quiescenceSearch(int nodeType, int ply, int alpha, int beta) throws SearchRunException {
        triangularLength[ply] = ply;
        int distanceToInitialPly = board.getMoveNumber() - initialPly;
        //Check if we are in check.
        if (board.isCheck()) return PVS(nodeType, ply, 1, alpha, beta);

        int hashMove=Move.EMPTY;
        boolean foundTT= transpositionTable.entryExists(board.getKey());
        if(foundTT && !(beta - alpha > 1)){ //not pv node
                    if (transpositionTable.getFlag() == TranspositionTable.HASH_EXACT)
                        return transpositionTable.getScore();
                    else if (transpositionTable.getFlag() == TranspositionTable.HASH_ALPHA && transpositionTable.getScore() <= alpha)
                        return transpositionTable.getScore();
                    else if (transpositionTable.getFlag() == TranspositionTable.HASH_BETA && transpositionTable.getScore() >= beta)
                        return transpositionTable.getScore();
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
        MoveSorter moveSorter = moveSorters[distanceToInitialPly];
        moveSorter.genMoves(hashMove, MoveSorter.GENERATE_CAPTURES_PROMOS, this);
        int move=Move.EMPTY;
        int bestMove=Move.EMPTY;
        while((move= moveSorter.next())!=Move.EMPTY){
            if (board.makeMove(move)) {
                score = -quiescenceSearch(nodeType, ply + 1, -beta, -alpha);
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


        }
        transpositionTable.record(board.getKey(), 0, alpha, beta, bestScore, bestMove);

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
}