package com.chess1800.chess.search;

import com.chess1800.chess.board.Chessboard;
import com.chess1800.chess.board.Evaluator;
import com.chess1800.chess.move.Move;

/**
 * Created by Yonathan on 21/12/2014.
 * Main search class, uses alpha-beta algorithm with Principal Variation Search, quiescence search and null move pruning.
 * Based on Winglet by Stef Luijten's Winglet Chess @
 * http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public abstract class PVS implements Search {

    protected int[][] triangularArray;
    protected int[] triangularLength;
    protected final int MAX_DEPTH = 16;
    protected int evals;
    protected boolean searching;
    protected SearchObserver observer;
    protected int[][] whiteHeuristics;
    protected int[][] blackHeuristics;
    protected int[] lastPV;
    protected boolean follow_pv;
//    private boolean pv_found;
    protected boolean null_allowed;
    protected static final boolean VERBOSE= true;

    // Time management
    private long startTime;
    private int timeForMove;
    protected int nextTimeCheck;
    protected final static int TIME_CHECK_INTERVAL=10000;
    protected boolean useFixedDepth;
    protected boolean stopSearch;
    private long bestMoveTime;
    private int globalBestMove;

    public final int findBestMove(Chessboard board, int depth, int timeLeft, int increment, int moveTime) {
        startTime = System.currentTimeMillis();
        int score;
        globalBestMove = 0;
//        legalMoves = 0;

        if(moveTime==0) timeForMove = calculateTime(timeLeft,increment);
        else timeForMove = moveTime;
        nextTimeCheck = TIME_CHECK_INTERVAL;
        useFixedDepth = depth != 0;

        if (board.isEndOfGame()!= Evaluator.NOT_ENDED){
            return NULLMOVE;
        }

        evals = 0;
        whiteHeuristics = new int[MAX_PLY][MAX_PLY];
        blackHeuristics = new int[MAX_PLY][MAX_PLY];
        lastPV = new int[MAX_PLY];
        int alpha = -Chessboard.INFINITY;
        int beta = Chessboard.INFINITY;
        stopSearch = false;
        for (int currentDepth = 1; currentDepth <= MAX_DEPTH; currentDepth++) {
            triangularArray = new int[MAX_GAME_LENGTH][MAX_GAME_LENGTH];
            triangularLength = new int[MAX_GAME_LENGTH];
            follow_pv = true;
            null_allowed = true;
            score = PVS(board, 0, currentDepth, alpha, beta);

            if(stopSearch){
                if(VERBOSE)System.out.println("Search stopped at depth:" +currentDepth);
                break;
            }


            if(VERBOSE)
                System.out.println("(" + currentDepth + ") "
                        + ( (System.currentTimeMillis() - startTime) / 1000.0) + "s ("
                        + Move.moveToString(lastPV[0]) + ") -- " + evals
                        + " nodes evaluated.");
            // stop searching if the current depth leads to a forced mate:
            if ((score > (Chessboard.CHECKMATE - currentDepth)) || (score < -(Chessboard.CHECKMATE - currentDepth))) {
                System.out.println("cut search");
                currentDepth = MAX_DEPTH;
            }
            if(useFixedDepth){
                if(currentDepth == depth) break;
            }else if(moveTime!=0){
                if(System.currentTimeMillis() - startTime > timeForMove) {
                    if(VERBOSE) System.out.println("TimeForMove exceeded. Higher depth search prevented.");
                    break;
                }
            }else{
                if(System.currentTimeMillis() - startTime > timeForMove*0.8){
                    if(VERBOSE) System.out.println("80% of timeForMove exceeded. Higher depth search prevented.");
                    break;
                }

            }

        }
        if(VERBOSE)
            System.out.println(evals + " positions evaluated. Move returned->" + lastPV[0]);
        if(observer!= null)observer.bestMove(lastPV[0]);
        return lastPV[0];
    }

    protected abstract int PVS(Chessboard board, int i, int currentDepth, int alpha, int beta);

    @Override
    public void setObserver(SearchObserver observer){
        this.observer = observer;
    }

    //Based on Mediocre Chess by Jonatan Pettersson. Source @ http://sourceforge.net/projects/mediocrechess/
    private int calculateTime(int timeLeft, int increment) {
        int timeForThisMove; //Max time allowed for the move.
        int percent = 40; //Percentage of time that will be used. percent=20 -> 5% percent=40 ->2.5%
        timeForThisMove = timeLeft/percent + increment; //use percent + increment for the move.

        //if the increment addition puts us above the timeleft, set the time for this move to timeleft-0.5s.
        if(timeForThisMove>=timeLeft) {
            timeForThisMove=timeLeft -500;
            //if the timeleft was less than 0.5s then set timeForThisMove to 0.1 seconds.
            if(timeForThisMove<0)
                timeForThisMove=100;
        }
        System.out.println("Time for this move: " + timeForThisMove);
        return timeForThisMove;
    }

    @Override
    public final long getBestMoveTime() {
        return bestMoveTime;
    }


    //Based on Mediocre Chess by Jonatan Pettersson. Source @ http://sourceforge.net/projects/mediocrechess/
    protected final boolean shouldWeStop() {
        if(System.currentTimeMillis()- startTime > timeForMove) return true;
        return false;
    }

    protected final void selectBestMoveFirst(Chessboard board, int[] moves, int num_moves, int ply, int depth, int nextIndex) {
        int tempMove;
        int best, bestIndex, i;
        // Re-orders the move list so that the PV is selected as the next move to try.
        if (follow_pv && depth > 1) {
            for (i = nextIndex; i < num_moves; i++) {
                if (moves[i] == lastPV[ply]) {
                    tempMove = moves[i];
                    moves[i] = moves[nextIndex];
                    moves[nextIndex] = tempMove;
                    return;
                }
            }
        }

        if (board.isWhiteToMove()) {
            best = whiteHeuristics[Move.getFromIndex(moves[nextIndex])][Move.getToIndex(moves[nextIndex])];
            bestIndex = nextIndex;
            for (i = nextIndex + 1; i < num_moves; i++) {
                if (whiteHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])] > best) {
                    best = whiteHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])];
                    bestIndex = i;
                }
            }
            if (bestIndex > nextIndex) {
                tempMove = moves[bestIndex];
                moves[bestIndex] = moves[nextIndex];
                moves[nextIndex] = tempMove;
            }

        } else {
            best = blackHeuristics[Move.getFromIndex(moves[nextIndex])][Move.getToIndex(moves[nextIndex])];
            bestIndex = nextIndex;
            for (i = nextIndex + 1; i < num_moves; i++) {
                if (blackHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])] > best) {
                    best = blackHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])];
                    bestIndex = i;
                }
            }
            if (bestIndex > nextIndex) {
                tempMove = moves[bestIndex];
                moves[bestIndex] = moves[nextIndex];
                moves[nextIndex] = tempMove;
            }
        }
    }

    protected abstract int quiescenceSearch(Chessboard board, int ply, int alpha, int beta);

    public boolean isSearching(){
        return searching;
    }

    protected final void rememberPV() {
        System.out.println("Length of triangularLength[0]: " + triangularLength[0]);
        for (int i = 0; i < triangularLength[0]; i++) {
            lastPV[i] = triangularArray[0][i];
        }
        if(globalBestMove != lastPV[0]){
            globalBestMove = lastPV[0];
            bestMoveTime = System.currentTimeMillis() -startTime;
        }
    }
}
