package search;

import board.Board;
import definitions.Definitions;
import evaluation.Evaluator;
import move.MoveAC;
import movegen.MoveGeneratorAC;
import utilities.SANUtils;

/**
 * Created by Yonathan on 02/02/2015.
 * MTD(f) based A.I for the secondary AI.
 */
public class MTDF implements Search, Definitions{
    private static MTDF instance;
    private SANUtils sanUtils;
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
    private int firstGuess;

    public static MTDF getInstance() {
        if(instance==null){
            instance = new MTDF();
            return instance;
        }
        return instance;
    }


    private MTDF()
    {
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

        for (int currentDepth = 1; currentDepth < MAX_DEPTH;) {
            follow_pv = true;
            null_allowed = true;
            firstGuess = memoryEnhancedTestDriver(b, currentDepth, firstGuess);
            currentDepth++;
            if(VERBOSE)
                System.out.println("(" + currentDepth + ") "
                        + ( (System.currentTimeMillis() - start) / 1000.0) + "s ("
                        + sanUtils.moveToString(lastPV[0]) + ") -- " + evals
                        + " nodes evaluated.");
            /*if ((score > (CHECKMATE - currentDepth)) || (score < -(CHECKMATE - currentDepth)))
                currentDepth = MAX_DEPTH;*/
        }
        if(VERBOSE)
            System.out.println(evals + " positions evaluated.");
        return firstGuess;
    }

    private int memoryEnhancedTestDriver(Board b, int currentDepth, int first) {
        int g = first;
        int upperBound = INFINITY;
        int lowerBound = -INFINITY;
        int beta;
        do{
            if(g == lowerBound) beta = g+1;
            else beta = g;
            g = alphaBetaM(b, 0, currentDepth, beta-1, beta);
            if(g<beta) upperBound = g;
            else lowerBound = g;

        }while(lowerBound <= upperBound);
        return g;
    }

    private int alphaBetaM(Board b, int ply, int depth, int alpha, int beta) {
        evals++;
        if (depth <= 0) {
            follow_pv = false;
            return quiescenceSearch(b, ply, alpha, beta);
        }

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
                    score = -alphaBetaM(b, ply, depth - NULLMOVE_REDUCTION, -beta, -beta + 1);
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
            bestScore = -alphaBetaM(b, ply + 1, depth - 1, -beta, -alpha);
            b.unmakeMove();
            if(bestScore > alpha){
                if(bestScore >= beta)
                    return bestScore; // fail soft
                pvMovesFound++;
               /* triangularArray[ply][ply] = moves[0];    //save the move
                for (j = ply + 1; j < triangularLength[ply + 1]; j++)
                    triangularArray[ply][j] = triangularArray[ply + 1][j];  //appends latest best PV from deeper plies

                triangularLength[ply] = triangularLength[ply + 1];*/
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
                    score= -alphaBetaM(b, ply + 1, depth - 2, -alpha - 1, -alpha);
                }
                else if (pvMovesFound != 0) {
                    score = -alphaBetaM(b, ply + 1, depth - 1, -alpha - 1, -alpha); // PVS Search
                    if ((score > alpha) && (score < beta))
                        score = -alphaBetaM(b, ply + 1, depth - 1, -beta, -alpha); //Better move found, normal alpha-beta.
                    if(score > alpha)
                        alpha = score;
                } else {
                    score = -alphaBetaM(b, ply + 1, depth - 1, -beta, -alpha); // Normal alpha-beta
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
                    /*triangularArray[ply][ply] = moves[i];    //save the move
                    for (j = ply + 1; j < triangularLength[ply + 1]; j++)
                        triangularArray[ply][j] = triangularArray[ply + 1][j];  //appends latest best PV from deeper plies

                    triangularLength[ply] = triangularLength[ply + 1];*/
                    if (ply == 0) rememberPV();


                }

            }
        }
        /*if (pvMovesFound != 0) {
            if (b.whiteToMove)
                whiteHeuristics[MoveAC.getFromIndex(triangularArray[ply][ply])][MoveAC.getToIndex(triangularArray[ply][ply])] += depth * depth;
            else
                blackHeuristics[MoveAC.getFromIndex(triangularArray[ply][ply])][MoveAC.getToIndex(triangularArray[ply][ply])] += depth * depth;
        }*/

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
        //triangularLength[ply] = ply;

        //Check if we are in check.
        if (b.isOwnKingAttacked()) return alphaBetaM(b, ply, 1, alpha, beta);

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
                /*triangularArray[ply][ply] = captures[i];
                for (int j = ply + 1; j < triangularLength[ply + 1]; j++) {
                    triangularArray[ply][j] = triangularArray[ply + 1][j];
                }
                triangularLength[ply] = triangularLength[ply + 1];*/
            }


        }
        return val;
    }


    private void rememberPV() {
        int i;
        //lastPVLength = triangularLength[0];
        /*for (i = 0; i < triangularLength[0]; i++) {
            lastPV[i] = triangularArray[0][i];
        }*/
    }

}
