package search;

import board.Board;
import definitions.Definitions;
import evaluation.Evaluator;
import move.MoveAC;
import movegen.MoveGeneratorAC;
import utilities.SANUtils;

/**
 * Created by Yonathan on 21/12/2014.
 * Main search class, uses alpha-beta algorithm with Principal Variation Search, quiescence search and null move pruning.
 * Based on Winglet by Stef Luijten's Winglet Chess @
 * http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public class PVS implements Definitions, Search {

    private boolean failHard;
    private SANUtils sanUtils;
    private int[][] triangularArray;
    private int[] triangularLength;
    public int legalMoves;
    public int singleMove = 0;
    private final int MAX_DEPTH = 5;
    private MoveGeneratorAC moveGenerator;
    private Evaluator evaluator;
    private int evals;
    private int[][] whiteHeuristics;
    private int[][] blackHeuristics;
    private int[] lastPV;
    private boolean follow_pv;
//    private boolean pv_found;
    private boolean null_allowed;
    private static final boolean VERBOSE= true;

    // Time management
    private long startTime;
    private int timeForMove;
    private int nextTimeCheck;
    private final static int TIME_CHECK_INTERVAL=10000;
    private boolean useFixedDepth;
    private boolean stopSearch;

    private long bestMoveTime;





    public PVS(boolean failType)
    {
        evaluator = Evaluator.getInstance();
        moveGenerator = MoveGeneratorAC.getInstance();
        sanUtils = SANUtils.getInstance();
        failHard =failType;
    }

    public int findBestMove(Board b, int depth, int timeLeft, int increment, int moveTime) {
        startTime = System.currentTimeMillis();
        int score;
//        legalMoves = 0;

        if(moveTime==0) timeForMove = calculateTime(b,timeLeft,increment);
        else timeForMove = moveTime;
        nextTimeCheck = TIME_CHECK_INTERVAL;
        useFixedDepth = depth != 0;

        if (b.isEndOfGame()){
            return NULLMOVE;
        }

/*        if (legalMoves == 1) {
            return singleMove;
        }*/

        evals = 0;
        whiteHeuristics = new int[MAX_PLY][MAX_PLY];
        blackHeuristics = new int[MAX_PLY][MAX_PLY];
        lastPV = new int[MAX_PLY];
        int alpha = -INFINITY;
        int beta = INFINITY;
//        int reSearchAlphaCount = 0;
//        int reSearchBetaCount = 0;
        for (int currentDepth = 1; currentDepth <= MAX_DEPTH; currentDepth++) {
            triangularArray = new int[MAX_GAME_LENGTH][MAX_GAME_LENGTH];
            triangularLength = new int[MAX_GAME_LENGTH];
            follow_pv = true;
            null_allowed = true;
//            pv_found= false;
            score = alphaBetaPVS(b, 0, currentDepth, alpha, beta);

            if(stopSearch)
                break;

            if(VERBOSE)
                System.out.println("(" + currentDepth + ") "
                        + ( (System.currentTimeMillis() - startTime) / 1000.0) + "s ("
                        + sanUtils.moveToString(lastPV[0]) + ") -- " + evals
                        + " nodes evaluated.");
            // stop searching if the current depth leads to a forced mate:
            if ((score > (CHECKMATE - currentDepth)) || (score < -(CHECKMATE - currentDepth))) {
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
        bestMoveTime=System.currentTimeMillis() - startTime;
        return lastPV[0];
    }

    //Based on Mediocore Chess by Jonatan Pettersson. Source @ http://sourceforge.net/projects/mediocrechess/
    private int calculateTime(Board b, int timeLeft, int increment) {
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

        return timeForThisMove;
    }

    public long getBestMoveTime() {
        return bestMoveTime;
    }

    //Main search method using Principal Variation search.
    private int alphaBetaPVS(Board b, int ply, int depth, int alpha, int beta) {
        evals++;
        triangularLength[ply] = ply;
        // Check if time is up
        if(!useFixedDepth) {
            nextTimeCheck--;
            if(nextTimeCheck == 0) {
                nextTimeCheck= TIME_CHECK_INTERVAL;
                if(shouldWeStop()){
                    stopSearch = true;
                    return 0;
                }
            }
        }

        if (depth <= 0) {
            follow_pv = false;
            return quiescenceSearch(b, ply, alpha, beta);
        }

        // End of game check, evaluate the board if so to check if it's a draw or checkmate.
        int endGameCheck= evaluator.eval(b);
        if (endGameCheck == DRAWSCORE || endGameCheck == -CHECKMATE) {
            follow_pv = false;
            if(endGameCheck == DRAWSCORE) return endGameCheck;
            return endGameCheck +ply -1;
        }

        int score;
        // Try Null move
        if (!follow_pv && null_allowed) {
            if (b.movingSidePieceMaterial() > NULLMOVE_THRESHOLD) {
                if (!b.isCheck()) {
                    null_allowed = false;
                    b.makeNullMove();
                    score = -alphaBetaPVS(b, ply, depth - NULLMOVE_REDUCTION, -beta, -beta + 1);
                    b.unmakeMove();
                    null_allowed = true;
                    if (score >= beta) {
                        if(failHard) return beta; //Fail Hard
                        return score; //Fail Soft
                    }
                }
            }
        }

        null_allowed = true;
        int movesFound = 0;
        int pvMovesFound = 0;
        int[] moves = new int[MAX_MOVES];
        int num_moves = moveGenerator.getAllLegalMoves(b, moves);
        int bestScore = 0;
        selectBestMoveFirst(b, moves, num_moves, ply, depth, 0);

        //try the first legal move with an open window.
        int j,pvIndex=0;
        for (int i = 0;i<num_moves;i++) {
            if (b.makeMove(moves[i])) {
                pvIndex=i;
                movesFound++;
                bestScore = -alphaBetaPVS(b, ply + 1, depth - 1, -beta, -alpha);
                b.unmakeMove();
                if (bestScore > alpha) {
                    if (bestScore >= beta) { //beta cutoff
                        if (b.whiteToMove)
                            whiteHeuristics[MoveAC.getFromIndex(moves[i])][MoveAC.getToIndex(moves[i])] += depth * depth;
                        else
                            blackHeuristics[MoveAC.getFromIndex(moves[i])][MoveAC.getToIndex(moves[i])] += depth * depth;
                        if(failHard) return beta; //fail hard
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
            selectBestMoveFirst(b, moves, num_moves, ply, depth, i);

            if (b.makeMove(moves[i])) {
                movesFound++;
                //Late Move Reduction
                if (movesFound > LATEMOVE_THRESHOLD && depth > LATEMOVE_DEPTH_THRESHOLD && !b.isCheck() && !MoveAC.isCapture(moves[i])) {
                    score = -alphaBetaPVS(b, ply + 1, depth - 2, -alpha - 1, -alpha);
                } else {
                    score = -alphaBetaPVS(b, ply + 1, depth - 1, -alpha - 1, -alpha); // PVS Search
                }
                if ((score > alpha) && (score < beta)) {
                    score = -alphaBetaPVS(b, ply + 1, depth - 1, -beta, -alpha); //Better move found, normal alpha-beta.

                    if(!failHard) {
                        if (score > alpha)
                            alpha = score;
                    }
                }

                b.unmakeMove();
                if (failHard) {
                    if (score >= beta) {
                        if (b.whiteToMove)
                            whiteHeuristics[MoveAC.getFromIndex(moves[i])][MoveAC.getToIndex(moves[i])] += depth * depth;
                        else
                            blackHeuristics[MoveAC.getFromIndex(moves[i])][MoveAC.getToIndex(moves[i])] += depth * depth;
                        return beta;
                    }
                    if(score>alpha) {
                        pvMovesFound++;
                        triangularArray[ply][ply] = moves[i];    //save the move
                        for (j = ply + 1; j < triangularLength[ply + 1]; j++)
                            triangularArray[ply][j] = triangularArray[ply + 1][j];  //appends latest best PV from deeper plies

                        triangularLength[ply] = triangularLength[ply + 1];
                        if (ply == 0) rememberPV();
                    }

                }else{
                    if (score > bestScore) {
                        if (score >= beta) {
                            if (b.whiteToMove)
                                whiteHeuristics[MoveAC.getFromIndex(moves[i])][MoveAC.getToIndex(moves[i])] += depth * depth;
                            else
                                blackHeuristics[MoveAC.getFromIndex(moves[i])][MoveAC.getToIndex(moves[i])] += depth * depth;
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
        }
        if (pvMovesFound != 0) {
            if (b.whiteToMove)
                whiteHeuristics[MoveAC.getFromIndex(triangularArray[ply][ply])][MoveAC.getToIndex(triangularArray[ply][ply])] += depth * depth;
            else
                blackHeuristics[MoveAC.getFromIndex(triangularArray[ply][ply])][MoveAC.getToIndex(triangularArray[ply][ply])] += depth * depth;
        }

        if (b.fiftyMove >= 100) return DRAWSCORE;                 //Fifty-move rule

        if(failHard) return alpha; //Fail Hard
        return bestScore;  //Fail Soft
    }

    //Based on Mediocore Chess by Jonatan Pettersson. Source @ http://sourceforge.net/projects/mediocrechess/
    private boolean shouldWeStop() {
        if(System.currentTimeMillis()- startTime > timeForMove) return true;
        return false;
    }

    private void selectBestMoveFirst(Board b, int[] moves, int num_moves, int ply, int depth, int nextIndex) {
        int tempMove;
        int best, bestIndex, i;
        // Re-orders the move list so that the PV is selected as the next move to try.
        if (follow_pv && depth > 1) {
            for (i = nextIndex; i < num_moves; i++) {
                if (moves[i] == lastPV[ply]) {
                    if(moves[i]==0) System.out.println("oops");
                    tempMove = moves[i];
                    moves[i] = moves[nextIndex];
                    moves[nextIndex] = tempMove;
                    return;
                }
            }
        }

        if (b.whiteToMove) {
            best = whiteHeuristics[MoveAC.getFromIndex(moves[nextIndex])][MoveAC.getToIndex(moves[nextIndex])];
            bestIndex = nextIndex;
            for (i = nextIndex + 1; i < num_moves; i++) {
                if (whiteHeuristics[MoveAC.getFromIndex(moves[i])][MoveAC.getToIndex(moves[i])] > best) {
                    best = whiteHeuristics[MoveAC.getFromIndex(moves[i])][MoveAC.getToIndex(moves[i])];
                    bestIndex = i;
                }
            }
            if (bestIndex > nextIndex) {
                tempMove = moves[bestIndex];
                moves[bestIndex] = moves[nextIndex];
                moves[nextIndex] = tempMove;
            }

        } else {
            best = blackHeuristics[MoveAC.getFromIndex(moves[nextIndex])][MoveAC.getToIndex(moves[nextIndex])];
            bestIndex = nextIndex;
            for (i = nextIndex + 1; i < num_moves; i++) {
                if (blackHeuristics[MoveAC.getFromIndex(moves[i])][MoveAC.getToIndex(moves[i])] > best) {
                    best = blackHeuristics[MoveAC.getFromIndex(moves[i])][MoveAC.getToIndex(moves[i])];
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

    private int quiescenceSearch(Board b, int ply, int alpha, int beta) {
        triangularLength[ply] = ply;

        //Check if we are in check.
        if (b.isCheck()) return alphaBetaPVS(b, ply, 1, alpha, beta);

        //Standing pat
        int bestScore;
        bestScore = evaluator.eval(b);
        if (bestScore >= beta) {
            if(failHard) return beta;
            return bestScore;
        }
        if (bestScore > alpha) alpha = bestScore;

        // generate captures & promotions:
        // genCaptures returns a sorted move list
        int[] captures = new int[MAX_MOVES];
        int num_captures = moveGenerator.genCaptures(b, captures);

        int score;
        for (int i = 0; i < num_captures; i++) {
            b.makeMove(captures[i]);
            score = -quiescenceSearch(b, ply + 1, -beta, -alpha);
            b.unmakeMove(captures[i]);
            if (score > alpha) {
                if (score >= beta) {
                    if (failHard) return beta;
                    return score;
                }

                alpha = score;
                //Need to test if this is needed as if score>alpha then score>bestScore.
                if(failHard) {
                    //Fail Hard.
                    triangularArray[ply][ply] = captures[i];
                    for (int j = ply + 1; j < triangularLength[ply + 1]; j++) {
                        triangularArray[ply][j] = triangularArray[ply + 1][j];
                    }
                    triangularLength[ply] = triangularLength[ply + 1];

                }else{
                    //Fail Soft
                    if (score > bestScore) {
                        bestScore = score;
                        triangularArray[ply][ply] = captures[i];
                        for (int j = ply + 1; j < triangularLength[ply + 1]; j++) {
                            triangularArray[ply][j] = triangularArray[ply + 1][j];
                        }
                        triangularLength[ply] = triangularLength[ply + 1];
                    }
                }
            }

        }
        if(failHard) return alpha; //Fail Hard
        return bestScore;  //Fail Soft
    }


    private void rememberPV() {
        for (int i = 0; i < triangularLength[0]; i++) {
            lastPV[i] = triangularArray[0][i];
        }
    }
}
