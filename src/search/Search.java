package search;

import bitboard.BitboardUtilsAC;
import board.Board;
import definitions.Definitions;
import evaluation.Evaluator;
import move.Move;
import move.MoveAC;
import movegen.MoveGeneratorAC;

/**
 * Created by Yonathan on 21/12/2014.
 * Main search class, uses alpha-beta algorithm with Principal Variation Search, quiescence search and null move pruning.
 * Based on Winglet by Stef Luijten's Winglet Chess @
 * http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public class Search implements Definitions {

    private static Search instance;
    private int[][] triangularArray;
    private int[] triangularLength;
    public Integer legalMoves;
    public int singleMove = 0;
    private final int MAX_DEPTH = 5;
    private MoveGeneratorAC moveGenerator;
    private Evaluator evaluator;
    private int evals;
    private int[][] whiteHeuristics;
    private int[][] blackHeuristics;
    private int[] lastPV;
    private boolean follow_pv;
    private boolean null_allowed;
    private static final boolean VERBOSE= false;
    //private int lastPVLength;



    public static Search getInstance() {
        if(instance==null){
            instance = new Search();
            return instance;
        }
        return instance;
    }


    public Search()
    {
        triangularArray = new int[MAX_GAME_LENGTH][MAX_GAME_LENGTH];
        triangularLength = new int[MAX_GAME_LENGTH];
        evaluator = Evaluator.getInstance();
        moveGenerator = MoveGeneratorAC.getInstance();
    }

    public void setEvaluator(Evaluator eval) {
        evaluator = eval;
    }


    public int findBestMove(Board b) {
        int score;
        legalMoves = 0;

        if (b.isEndOfGame()) return NULLMOVE;

        if (legalMoves == 1) return singleMove;

        evals = 0;
        whiteHeuristics = new int[MAX_PLY][MAX_PLY];
        blackHeuristics = new int[MAX_PLY][MAX_PLY];
        lastPV = new int[MAX_PLY];
        //lastPVLength = 0;
        long start = System.currentTimeMillis();
        int alpha = Integer.MIN_VALUE + 1;
        int beta = Integer.MAX_VALUE - 1;
        int reSearchAlphaCount = 0;
        int reSearchBetaCount = 0;
        for (int currentDepth = 1; currentDepth < MAX_DEPTH;) {
            triangularArray = new int[MAX_GAME_LENGTH][MAX_GAME_LENGTH];
            triangularLength = new int[MAX_GAME_LENGTH];
            follow_pv = true;
            null_allowed = true;
            score = alphaBetaPVS(b, 0, currentDepth, alpha, beta);
            if (score <= alpha){
                if(reSearchAlphaCount == 0){
                    alpha -=100;
                    reSearchAlphaCount++;
                }
                else if(reSearchAlphaCount == 1){
                    alpha -= 200;
                    reSearchAlphaCount++;
                }else{
                        alpha = Integer.MIN_VALUE + 1;
                        reSearchAlphaCount++;
                }
                continue;
            }else if(score >= beta){
                if(reSearchBetaCount == 0){
                    beta += 100;
                    reSearchBetaCount++;
                }
                else if(reSearchBetaCount == 1){
                    beta += 200;
                    reSearchBetaCount++;
                }else{
                    beta = Integer.MAX_VALUE - 1;
                    reSearchBetaCount++;
                }
                continue;
            } else if(score == 0){
                alpha = Integer.MIN_VALUE + 1;
                beta = Integer.MAX_VALUE - 1;
                continue;
            }
            if(VERBOSE)
                System.out.println("(" + currentDepth + ") "
                        + ( (System.currentTimeMillis() - start) / 1000.0) + "s ("
                        + BitboardUtilsAC.moveToString(lastPV[0]) + ") -- " + evals
                        + " nodes evaluated.");
            if ((score > (CHECKMATE - currentDepth)) || (score < -(CHECKMATE - currentDepth)))
                currentDepth = MAX_DEPTH;
        }
        if(VERBOSE)
            System.out.println(evals + " positions evaluated.");
        return lastPV[0];
    }

    private int alphaBetaPVS(Board b, int ply, int depth, int alpha, int beta) {
        evals++;
        triangularLength[ply] = ply;
        if (depth <= 0) {
            follow_pv = false;
            return quiescenceSearch(b, ply, alpha, beta);
        }

        if (b.isEndOfGame()) {
            follow_pv = false;
            return evaluator.eval(b);
        }

        int score;
        //Try Null move
        if (!follow_pv && null_allowed) {
            if (b.movingSidePieceMaterial() > NULLMOVE_LIMIT) {
                if (!b.isOwnKingAttacked()) {
                    null_allowed = false;
                    b.makeNullMove();
                    score = -alphaBetaPVS(b, ply, depth - NULLMOVE_REDUCTION, -beta, -beta + 1);
                    b.unmakeMove();
                    null_allowed = true;
                    if (score >= beta) {
                        return score;
                    }
                }
            }
        }

        null_allowed = true;
        int movesFound = 0;
        int pvMovesFound = 0;
        int[] moves = new int[MAX_MOVES];
        int num_moves = moveGenerator.getAllMoves(b, moves);
        int bestScore = 0;
        selectBestMoveFirst(b, moves, num_moves, ply, depth, 0);
        //try the first move with unchanged window.
        int j;
        if(b.makeMove(moves[0])){
            movesFound++;
            bestScore = -alphaBetaPVS(b, ply+1, depth-1, -beta,-alpha);
            b.unmakeMove();
            if(bestScore > alpha){
                if(bestScore >= beta)
                    return bestScore; // fail soft
                pvMovesFound++;
                triangularArray[ply][ply] = moves[0];    //save the move
                for (j = ply + 1; j < triangularLength[ply + 1]; j++)
                    triangularArray[ply][j] = triangularArray[ply + 1][j];  //appends latest best PV from deeper plies

                triangularLength[ply] = triangularLength[ply + 1];
                if (ply == 0) rememberPV();

                alpha = bestScore;  // alpha improved
            }
        }
        for (int i = 1; i < num_moves; i++) {
            selectBestMoveFirst(b, moves, num_moves, ply, depth, i);

            if (b.makeMove(moves[i])) {
                movesFound++;
                //Late Move Reduction
                if(movesFound>=LATEMOVE_THRESHOLD && depth>LATEMOVE_DEPTH_THRESHOLD && !b.isCheck()&& !MoveAC.isCapture(moves[i])){
                    score= -alphaBetaPVS(b, ply + 1, depth - 2, -alpha - 1, -alpha);
                }
                else if (pvMovesFound != 0) {
                    score = -alphaBetaPVS(b, ply + 1, depth - 1, -alpha - 1, -alpha); // PVS Search
                    if ((score > alpha) && (score < beta))
                        score = -alphaBetaPVS(b, ply + 1, depth - 1, -beta, -alpha); //Better move found, normal alpha-beta.
                        if(score > alpha)
                            alpha = score;
                } else {
                    score = -alphaBetaPVS(b, ply + 1, depth - 1, -beta, -alpha); // Normal alpha-beta
                    //System.out.println("shouldn't get to this window");
                }
                b.unmakeMove();
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
        if (pvMovesFound != 0) {
            if (b.whiteToMove)
                whiteHeuristics[MoveAC.getFromIndex(triangularArray[ply][ply])][MoveAC.getToIndex(triangularArray[ply][ply])] += depth * depth;
            else
                blackHeuristics[MoveAC.getFromIndex(triangularArray[ply][ply])][MoveAC.getToIndex(triangularArray[ply][ply])] += depth * depth;
        }

        if (b.fiftyMove >= 100) return DRAWSCORE;                 //Fifty-move rule

        /*if (movesFound == 0) {
            if (b.isOwnKingAttacked()) return -CHECKMATE + ply - 1; //Checkmate
            return DRAWSCORE;                                 //Stalemate
        }*/

        return bestScore;
    }

    private void selectBestMoveFirst(Board b, int[] moves, int num_moves, int ply, int depth, int nextIndex) {
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
        if (b.isOwnKingAttacked()) return alphaBetaPVS(b, ply, 1, alpha, beta);

        //Standing pat
        int val;
        val = evaluator.eval(b);
        if (val >= beta) return val;
        if (val > alpha) alpha = val;

        // generate captures & promotions:
        // genCaptures returns a sorted move list
        int[] captures = new int[MAX_MOVES];
        int num_captures = moveGenerator.genCaptures(b, captures);

        for (int i = 0; i < num_captures; i++) {
            b.makeMove(captures[i]);
            val = -quiescenceSearch(b, ply + 1, -beta, -alpha);
            b.unmakeMove(captures[i]);

            if (val >= beta) return val;

            if (val > alpha) {
                alpha = val;
                triangularArray[ply][ply] = captures[i];
                for (int j = ply + 1; j < triangularLength[ply + 1]; j++) {
                    triangularArray[ply][j] = triangularArray[ply + 1][j];
                }
                triangularLength[ply] = triangularLength[ply + 1];
            }


        }
        return alpha;
    }


    private void rememberPV() {
        int i;
        //lastPVLength = triangularLength[0];
        for (i = 0; i < triangularLength[0]; i++) {
            lastPV[i] = triangularArray[0][i];
        }
    }

}
