package com.chess1800.chess.search;

import com.chess1800.chess.board.Chessboard;
import com.chess1800.chess.board.Evaluator;
import com.chess1800.chess.board.MoveGenerator;
import com.chess1800.chess.move.Move;

/**
 * Created by Yonathan on 02/03/2015.
 * PVS with Fail Soft.
 */
public class PVSSoft extends PVS {

    public PVSSoft(Chessboard b) {
        super(b);
    }

    //Fail Soft implementation
    protected int PVS(int nodeType,int ply, int depth, int alpha, int beta) throws SearchRunException {
        nodes++;
        triangularLength[ply] = ply;
        // Check if time is up
        if (!useFixedDepth) {
            //nextTimeCheck--;
            //if(nextTimeCheck == 0) {
            // nextTimeCheck= TIME_CHECK_INTERVAL;
            if (System.currentTimeMillis() - startTime > timeForMove && moveFound && nodeType != NODE_ROOT) {
                finishRun();
            }
        }
        //}

        if (depth <= 0) {
            follow_pv = false;
            return quiescenceSearch(nodeType, ply, alpha, beta);
        }

        // End of game check, evaluate the board if so to check if it's a draw or checkmate.
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
            score = -PVS(NODE_NULL, ply, depth - NULLMOVE_REDUCTION, -beta, -beta + 1);
            board.unmakeMove();
            null_allowed = true;
            if (score >= beta) return score; //Fail Soft

        }

        null_allowed = true;
        int movesFound = 0;
        int pvMovesFound = 0;
        int[] moves = new int[MoveGenerator.MAX_MOVES];
        int num_moves = board.getAllMoves(moves);
        int bestScore = 0;


        //try the first legal move with an open window.
        int j, pvIndex = 0;
        for (int i = 0; i < num_moves; i++) {
            selectBestMoveFirst(moves, num_moves, ply, depth, i);
            if (board.makeMove(moves[i])) {
                pvIndex = i;
                movesFound++;
                bestScore = -PVS(NODE_PV, ply + 1, depth - 1, -beta, -alpha);
                board.unmakeMove();
                if (bestScore > alpha) {
                    if (bestScore >= beta) { //beta cutoff
                        if (board.isWhiteToMove())
                            whiteHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])] += depth * depth;
                        else
                            blackHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])] += depth * depth;
                        return bestScore;  // fail soft
                    }
                    pvMovesFound++;
                    triangularArray[ply][ply] = moves[i];    //save the move
                    for (j = ply + 1; j < triangularLength[ply + 1]; j++)
                        triangularArray[ply][j] = triangularArray[ply + 1][j];  //appends latest best PV from deeper plies

                    triangularLength[ply] = triangularLength[ply + 1];
                    if (ply == 0) rememberPV();
                    alpha = bestScore;  // alpha improved
                }
                break; //First legal move only.
            }
        }
        for (int i = pvIndex; i < num_moves; i++) {
            selectBestMoveFirst(moves, num_moves, ply, depth, i);

            if (board.makeMove(moves[i])) {
                movesFound++;
                //Late Move Reduction
                if (movesFound > LATEMOVE_THRESHOLD && depth > LATEMOVE_DEPTH_THRESHOLD && !board.isCheck() && !Move.isCapture(moves[i])) {
                    score = -PVS(NODE_NULL, ply + 1, depth - 2, -alpha - 1, -alpha);
                } else {
                    score = -PVS(NODE_NULL, ply + 1, depth - 1, -alpha - 1, -alpha); // PVS Search
                }
                if ((score > alpha) && (score < beta)) {
                    score = -PVS(NODE_PV, ply + 1, depth - 1, -beta, -alpha); //Better move found, re-search with Open Window
                    if (score > alpha)
                        alpha = score;
                }
                board.unmakeMove();
                if (score > bestScore) {
                    if (score >= beta) {
                        if (board.isWhiteToMove())
                            whiteHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])] += depth * depth;
                        else
                            blackHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])] += depth * depth;
                        return score;
                    }
                    bestScore = score;
                    pvMovesFound++;
                    triangularArray[ply][ply] = moves[i];    //save the move
                    for (j = ply + 1; j < triangularLength[ply + 1]; j++)
                        triangularArray[ply][j] = triangularArray[ply + 1][j];  //appends latest best PV from deeper plies

                    triangularLength[ply] = triangularLength[ply + 1];
                    if (ply == 0) rememberPV();
                }

            }
        }
        if (pvMovesFound != 0) {
            if (board.isWhiteToMove())
                whiteHeuristics[Move.getFromIndex(triangularArray[ply][ply])][Move.getToIndex(triangularArray[ply][ply])] += depth * depth;
            else
                blackHeuristics[Move.getFromIndex(triangularArray[ply][ply])][Move.getToIndex(triangularArray[ply][ply])] += depth * depth;
        }

        if (board.getFiftyMove() >= 100) return Evaluator.DRAWSCORE;                 //Fifty-move rule

        return bestScore;  //Fail Soft
    }


    protected int quiescenceSearch(int nodeType, int ply, int alpha, int beta) throws SearchRunException {
        triangularLength[ply] = ply;

        //Check if we are in check.
        if (board.isCheck()) return PVS(nodeType, ply, 1, alpha, beta);


        //Standing pat
        int bestScore;
        bestScore = board.eval();
        if (bestScore >= beta) {
            return bestScore;
        }
        if (bestScore > alpha) alpha = bestScore;

        // generate captures & promotions:
        // genCaptures gives a sorted move list
        int[] captures = new int[MoveGenerator.MAX_MOVES];
        int num_captures = board.genCaptures(captures);

        int score;
        for (int i = 0; i < num_captures; i++) {
            if (board.makeMove(captures[i])) {
                score = -quiescenceSearch(nodeType, ply + 1, -beta, -alpha);
                board.unmakeMove();
                if (score > alpha) {
                    if (score >= beta) {
                        //Fail Soft
                        return score;
                    }

                    alpha = score;
                    if (score > bestScore) {
                        bestScore = score;
                        triangularArray[ply][ply] = captures[i]; //save the best capture
                        for (int j = ply + 1; j < triangularLength[ply + 1]; j++) {
                            triangularArray[ply][j] = triangularArray[ply + 1][j];
                        }
                        triangularLength[ply] = triangularLength[ply + 1];
                    }
                }
            }
        }
        return bestScore;  //Fail Soft
    }

    @Override
    protected void notifyMoveFound(int bestMove, int bestScore) {

    }

    @Override
    protected void setPV(int firstMove) {

    }
}