package search;

import board.Board;
import definitions.Definitions;
import evaluation.Evaluator;
import move.MoveAC;
import movegen.MoveGeneratorAC;
import transposition_table.TranspositionTable;
import utilities.SANUtils;

/**
 * Created by Yonathan on 02/02/2015.
 * MTD(f) based A.I for the secondary AI. Not Functioning.
 */
public class MTDF implements Search, Definitions{
    private static MTDF instance;
    private SANUtils sanUtils;
    public Integer legalMoves;
    public int singleMove = 0;
    private final int MAX_DEPTH = 5;
    private MoveGeneratorAC moveGenerator;
    private Evaluator evaluator;
    private TranspositionTable transpositionTable;

    // Number of  nodes evaluated
    private int evals;

    // History Heuristic
    private int[][] whiteHeuristics;
    private int[][] blackHeuristics;

    // PV related, lastPV[0] will hold the best move.
    private int[] lastPV;
    private boolean follow_pv;

    // Null move
    private boolean null_allowed;

    // For printing extra information to the command line.
    private static final boolean VERBOSE= false;

    // MTD(f)
    private int firstGuess;
    private int prevGuess;

    // Timing information
    private long startTime;
    private int timeForMove;
    private int nextTimeCheck;
    private static int TIME_CHECK_INTERVAL=10000;
    private boolean useFixedDepth;
    private boolean stopSearch;

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
        transpositionTable = new TranspositionTable(64);
    }

    public void setEvaluator(Evaluator eval) {
        evaluator = eval;
    }


    public int findBestMove(Board b, int depth, int timeLeft, int increment, int moveTime) {
        startTime = System.currentTimeMillis();

        legalMoves = 0;

        if (b.isEndOfGame()) return NULLMOVE;

        if (legalMoves == 1) return singleMove;

        if(moveTime==0) timeForMove = calculateTime(b,timeLeft,increment);
        useFixedDepth = depth != 0;
        evals = 0;
        whiteHeuristics = new int[MAX_PLY][MAX_PLY];
        blackHeuristics = new int[MAX_PLY][MAX_PLY];
        lastPV = new int[MAX_PLY];
        long start = System.currentTimeMillis();

        for (int currentDepth = 1; currentDepth <= MAX_DEPTH; currentDepth++) {
            follow_pv = true;
            null_allowed = true;
            firstGuess = memoryEnhancedTestDriver(b, currentDepth, firstGuess);
           /* if(firstGuess == prevGuess) break;
            prevGuess = firstGuess;*/
            if(VERBOSE)
                System.out.println("(" + currentDepth + ") "
                        + ( (System.currentTimeMillis() - start) / 1000.0) + "s ("
                        + sanUtils.moveToString(lastPV[0]) + ") -- " + evals
                        + " nodes evaluated.");
            if(useFixedDepth) {
                if (currentDepth==depth || firstGuess == -(CHECKMATE+6)) break;
            }
            /*if ((firstGuess > (CHECKMATE - currentDepth)) || (firstGuess < -(CHECKMATE - currentDepth)))
                currentDepth = MAX_DEPTH;*/
        }
        if(VERBOSE)
            System.out.println(evals + " positions evaluated.");
        return firstGuess;
    }

    private int calculateTime(Board b, int timeLeft, int increment) {

        return 0;
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

        }while(lowerBound < upperBound);
        return g;
    }

    private int alphaBetaM(Board b, int ply, int depth, int alpha, int beta) {
        int eval_type = HASH_ALPHA;
        int bestScore = -INFINITY;
        evals++;
        int score;

        //Check if the hash table value exists and is stored at the same or higher depth.
        if(transpositionTable.entryExists(b.key) && transpositionTable.getDepth(b.key)>=depth){
            if(transpositionTable.getFlag(b.key) == HASH_EXACT) return transpositionTable.getEval(b.key); // should never be called
            if(transpositionTable.getFlag(b.key) == HASH_ALPHA && transpositionTable.getEval(b.key)<= alpha) return transpositionTable.getEval(b.key);
            else if(transpositionTable.getFlag(b.key) == HASH_BETA && transpositionTable.getEval(b.key)>= beta) return transpositionTable.getEval(b.key);
            if(alpha>=beta) return transpositionTable.getEval(b.key);
            alpha= Integer.max(alpha,transpositionTable.getEval(b.key));
        }
        if (depth <= 0) {
            follow_pv = false;
            score= quiescenceSearch(b, ply, alpha, beta);
            if(score<= alpha){
                transpositionTable.record(b.key, depth, HASH_ALPHA, score, 0 );
            } else if(score>=beta)
                transpositionTable.record(b.key, depth, HASH_BETA, score, 0 );
            else
                transpositionTable.record(b.key, depth, HASH_EXACT, score, 0 ); //should never be called
        }

        if (b.isEndOfGame()) {
            follow_pv = false;
            score = evaluator.eval(b);
            if(score<= alpha){
                transpositionTable.record(b.key, depth, HASH_ALPHA, score, 0 );
            } else if(score>=beta)
                transpositionTable.record(b.key, depth, HASH_BETA, score, 0 );
            else
                transpositionTable.record(b.key, depth, HASH_EXACT, score, 0 ); // should never be called
            if(score == DRAWSCORE) return score;
            return score +ply -1;
        }



        //Try Null move
        if (!follow_pv && null_allowed) {
            if (b.movingSidePieceMaterial() > NULLMOVE_THRESHOLD) {
                if (!b.isCheck()) {
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
        int hashMove=transpositionTable.getMove(b.key);
        //if(hashMove != 0 && !b.validateHashMove(hashMove)) hashMove = 0;
        int movesFound = 0;
        int pvMovesFound = 0;

        //Try hash move
        if(hashMove !=0 ) {
            if(b.makeMove(hashMove)){
                movesFound++;
                score = -alphaBetaM(b, ply + 1, depth - 1, -alpha - 1, -alpha);
                b.unmakeMove();
                // TODO
                if(score > bestScore) return score;
            }else
                hashMove = 0;
        }
        int[] moves = new int[MAX_MOVES];
        int num_moves = moveGenerator.getAllMoves(b, moves);



        for (int i = 0; i < num_moves; i++) {
            selectBestMoveFirst(b, moves, num_moves, ply, depth, i);
            if (hashMove != moves[i]) {
                if (b.makeMove(moves[i])) {
                    movesFound++;
                    //Late Move Reduction
                    if (movesFound >= LATEMOVE_THRESHOLD && depth > LATEMOVE_DEPTH_THRESHOLD && !b.isCheck() && !MoveAC.isCapture(moves[i])) {
                        score = -alphaBetaM(b, ply + 1, depth - 2, -alpha - 1, -alpha);
                    } else {
                        score = -alphaBetaM(b, ply + 1, depth - 1, -alpha - 1, -alpha); // PVS Search
                    }
                    if (score > alpha)
                        alpha = score;
                    b.unmakeMove();
                    if (score > bestScore) {
                        if (score >= beta) {
                            transpositionTable.record(b.key, depth, HASH_BETA, score, moves[i]);
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
        }
        /*if (pvMovesFound != 0) {
            if (b.whiteToMove)
                whiteHeuristics[MoveAC.getFromIndex(triangularArray[ply][ply])][MoveAC.getToIndex(triangularArray[ply][ply])] += depth * depth;
            else
                blackHeuristics[MoveAC.getFromIndex(triangularArray[ply][ply])][MoveAC.getToIndex(triangularArray[ply][ply])] += depth * depth;
        }*/


        if (b.fiftyMove >= 100) return DRAWSCORE;                 //Fifty-move rule


        return bestScore;
    }

    private void selectBestMoveFirst(Board b, int[] moves, int num_moves, int ply, int depth, int nextIndex) {
        int tempMove;
        int best, bestIndex, i;
        // Re-orders the move list so that the PV is selected as the next move to try.
//        if (follow_pv && depth > 1) {
//            for (i = nextIndex; i < num_moves; i++) {
//                if (moves[i] == lastPV[ply]) {
//                    tempMove = moves[i];
//                    moves[i] = moves[nextIndex];
//                    moves[nextIndex] = tempMove;
//                    return;
//                }
//            }
//        }

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
        if (b.isCheck()) return alphaBetaM(b, ply, 1, alpha, beta);

        //Standing pat
        int bestScore;
        bestScore = evaluator.eval(b);
        if (bestScore >= beta) return bestScore;
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

            if (score >= beta) return score;

            if(score > alpha) alpha=score;
            if (score > bestScore) {
                bestScore = score;
                /*triangularArray[ply][ply] = captures[i];
                for (int j = ply + 1; j < triangularLength[ply + 1]; j++) {
                    triangularArray[ply][j] = triangularArray[ply + 1][j];
                }
                triangularLength[ply] = triangularLength[ply + 1];*/
            }


        }
        return bestScore;
    }


    private void rememberPV() {
        int i;
        //lastPVLength = triangularLength[0];
        /*for (i = 0; i < triangularLength[0]; i++) {
            lastPV[i] = triangularArray[0][i];
        }*/
    }

}
