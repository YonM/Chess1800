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

    private static PVS instance;
    private SANUtils sanUtils;
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
//    private boolean pv_found;
    private boolean null_allowed;
    private static final boolean VERBOSE= true;

    public static PVS getInstance() {
        if(instance==null){
            instance = new PVS();
            return instance;
        }
        return instance;
    }

    private PVS()
    {
        triangularArray = new int[MAX_GAME_LENGTH][MAX_GAME_LENGTH];
        triangularLength = new int[MAX_GAME_LENGTH];
        evaluator = Evaluator.getInstance();
        moveGenerator = MoveGeneratorAC.getInstance();
        sanUtils = SANUtils.getInstance();
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
        long start = System.currentTimeMillis();
        int alpha = -INFINITY;
        int beta = INFINITY;
        int reSearchAlphaCount = 0;
        int reSearchBetaCount = 0;
        for (int currentDepth = 1; currentDepth <= MAX_DEPTH; currentDepth++) {
            triangularArray = new int[MAX_GAME_LENGTH][MAX_GAME_LENGTH];
            triangularLength = new int[MAX_GAME_LENGTH];
            follow_pv = true;
            null_allowed = true;
//            pv_found= false;
            score = alphaBetaPVS(b, 0, currentDepth, alpha, beta);

            if(VERBOSE) {
                if (score == alpha) System.out.println("alpha baby");
                if (score == beta) System.out.println("beta baby");
            }

            /*if(pv_found) {

                if (score <= alpha) {
                    if (VERBOSE)
                        System.out.println("reSearchAlphaCount: " + reSearchAlphaCount + "\n" + "alpha: " + alpha + "\n" + "reSearchBetaCount: " + reSearchBetaCount + "\n" + "beta: " + beta);

                    if (reSearchAlphaCount == 0) {
                        alpha -= 100;
                        reSearchAlphaCount++;
                    } else if (reSearchAlphaCount == 1) {
                        alpha -= 200;
                        reSearchAlphaCount++;
                    } else {
                        alpha = -INFINITY;
                        reSearchAlphaCount++;
                    }
                    continue;
                }
                else if (score >= beta) {
                    if (VERBOSE)
                        System.out.println("reSearchBetaCount: " + reSearchBetaCount + "\n" + "beta: " + beta + "\n" + "reSearchAlphaCount: " + reSearchAlphaCount + "\n" + "alpha: " + alpha);

                    if (reSearchBetaCount == 0) {
                        beta += 100;
                        reSearchBetaCount++;
                    } else if (reSearchBetaCount == 1) {
                        beta += 200;
                        reSearchBetaCount++;
                    } else {
                        beta = INFINITY;
                        reSearchBetaCount++;
                    }
                    continue;
                }
                else if (score == 0) {
                    alpha = -INFINITY;
                    beta = INFINITY;
                    continue;
                }
                alpha = score - 60;
                beta = score + 60;
                reSearchAlphaCount = 0;
                reSearchBetaCount = 0;
                if (alpha < -INFINITY) alpha = -INFINITY;
                if (beta > INFINITY) beta = INFINITY;
            }*/

            if(VERBOSE)
                System.out.println("(" + currentDepth + ") "
                        + ( (System.currentTimeMillis() - start) / 1000.0) + "s ("
                        + sanUtils.moveToString(lastPV[0]) + ") -- " + evals
                        + " nodes evaluated.");
            // stop searching if the current depth leads to a forced mate:
            if ((score > (CHECKMATE - currentDepth)) || (score < -(CHECKMATE - currentDepth))) {
                currentDepth = MAX_DEPTH;
            }
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

        //End of game check, evaluate the board if so to check if it's a draw or checkmate.
        if (b.isEndOfGame()) {
            follow_pv = false;
            int eval = evaluator.eval(b);
            if(eval == DRAWSCORE) return eval;
            return eval +ply -1;
        }

        int score;
        //Try Null move
        if (!follow_pv && null_allowed) {
            if (b.movingSidePieceMaterial() > NULLMOVE_THRESHOLD) {
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
        if(num_moves != 0) {
            if (b.makeMove(moves[0])) {
                movesFound++;
                bestScore = -alphaBetaPVS(b, ply + 1, depth - 1, -beta, -alpha);
                b.unmakeMove();
                if (bestScore > alpha) {

                    if (bestScore >= beta){ //beta cutoff

                        if (b.whiteToMove)
                            whiteHeuristics[MoveAC.getFromIndex(moves[0])][MoveAC.getToIndex(moves[0])] += depth * depth;
                        else
                            blackHeuristics[MoveAC.getFromIndex(moves[0])][MoveAC.getToIndex(moves[0])] += depth * depth;
                        return bestScore;  // fail soft
                    }

                    pvMovesFound++;
//                    pv_found = true;
                    triangularArray[ply][ply] = moves[0];    //save the move
                    for (j = ply + 1; j < triangularLength[ply + 1]; j++)
                        triangularArray[ply][j] = triangularArray[ply + 1][j];  //appends latest best PV from deeper plies

                    triangularLength[ply] = triangularLength[ply + 1];
                    if (ply == 0) rememberPV();
                    alpha = bestScore;  // alpha improved
                }
            }
        }
        else{
            System.out.println("no moves");
        }
        for (int i = 1; i < num_moves; i++) {
            selectBestMoveFirst(b, moves, num_moves, ply, depth, i);

            if (b.makeMove(moves[i])) {
                movesFound++;
                //Late Move Reduction
                /*if(movesFound>LATEMOVE_THRESHOLD && depth>LATEMOVE_DEPTH_THRESHOLD && !b.isCheck()&& !MoveAC.isCapture(moves[i])){
                    score= -alphaBetaPVS(b, ply + 1, depth - 2, -alpha - 1, -alpha);
                }*/
//                if (pvMovesFound != 0) {
//                    if(movesFound==1)System.out.println("pv found & " + movesFound);

                    score = -alphaBetaPVS(b, ply + 1, depth - 1, -alpha - 1, -alpha); // PVS Search
                    if ((score > alpha) && (score < beta))
                        score = -alphaBetaPVS(b, ply + 1, depth - 1, -beta, -alpha); //Better move found, normal alpha-beta.
                        if(score > alpha)
                            alpha = score;
//                }
                /* else {
                    score = -alphaBetaPVS(b, ply + 1, depth - 1, -beta, -alpha); // Normal alpha-beta
                    //System.out.println("shouldn't get to this window");
                }*/
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
//                    pv_found= true;
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
        return val;
    }


    private void rememberPV() {
        for (int i = 0; i < triangularLength[0]; i++) {
            lastPV[i] = triangularArray[0][i];
        }
    }
}
