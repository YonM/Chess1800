package com.chess1800.chess.search;

import com.chess1800.chess.board.Chessboard;
import com.chess1800.chess.board.Evaluator;
import com.chess1800.chess.board.MoveGenerator;
import com.chess1800.chess.move.Move;
import com.chess1800.chess.search.AbstractSearch;
import com.chess1800.chess.transposition_table.TranspositionTable;


/**
 * Created by Yonathan on 02/02/2015.
 * MTD(f) based A.I for the secondary AI. Not Functioning.
 */
public class MTDF extends AbstractSearch implements Search {
    private static MTDF instance;
    public Integer legalMoves;
    public int singleMove = 0;
    private final int MAX_DEPTH = 5;

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
    private static final boolean VERBOSE = false;

    // MTD(f)
    private int firstGuess;
    private int prevGuess;

    // Timing information
    private long startTime;
    private int timeForMove;
    private int nextTimeCheck;
    private static int TIME_CHECK_INTERVAL = 10000;
    private boolean useFixedDepth;
    private boolean stopSearch;
    private long bestMoveTime;

    private SearchObserver observer;
    private boolean searching;

    public MTDF(Chessboard b) {
        super(b);
        transpositionTable = new TranspositionTable(64);
    }


    @Override
    public long getBestMoveTime() {
        return bestMoveTime;
    }

    @Override
    public void setObserver(SearchObserver observer) {
        this.observer = observer;
    }

    @Override
    protected void setupRun() {

    }

    @Override
    protected void finishRun() throws SearchRunException {

    }

    public int findBestMove(Chessboard board, int depth, int timeLeft, int increment, int moveTime) {
        startTime = System.currentTimeMillis();

        legalMoves = 0;

        if (board.isEndOfGame() != Evaluator.NOT_ENDED) return NULLMOVE;

        if (legalMoves == 1) return singleMove;

        if (moveTime == 0) timeForMove = calculateTime(board, timeLeft, increment);
        useFixedDepth = depth != 0;
        evals = 0;
        whiteHeuristics = new int[MAX_PLY][MAX_PLY];
        blackHeuristics = new int[MAX_PLY][MAX_PLY];
        lastPV = new int[MAX_PLY];
        long start = System.currentTimeMillis();

        for (int currentDepth = 1; currentDepth <= MAX_DEPTH; currentDepth++) {
            follow_pv = true;
            null_allowed = true;
            firstGuess = memoryEnhancedTestDriver(board, currentDepth, firstGuess);
           /* if(firstGuess == prevGuess) break;
            prevGuess = firstGuess;*/
            if (VERBOSE)
                System.out.println("(" + currentDepth + ") "
                        + ((System.currentTimeMillis() - start) / 1000.0) + "s ("
                        + Move.moveToString(lastPV[0]) + ") -- " + evals
                        + " nodes evaluated.");
            if (useFixedDepth) {
                if (currentDepth == depth || firstGuess == -(Chessboard.CHECKMATE + 6)) break;
            }
            /*if ((firstGuess > (CHECKMATE - currentDepth)) || (firstGuess < -(CHECKMATE - currentDepth)))
                currentDepth = MAX_DEPTH;*/
        }
        if (VERBOSE)
            System.out.println(evals + " positions evaluated.");
        bestMoveTime = System.currentTimeMillis() - startTime;
        return firstGuess;
    }

    private int calculateTime(Chessboard board, int timeLeft, int increment) {

        return 0;
    }

    private int memoryEnhancedTestDriver(Chessboard board, int currentDepth, int first) {
        int g = first;
        int upperBound = Chessboard.INFINITY;
        int lowerBound = -Chessboard.INFINITY;
        int beta;
        do {
            if (g == lowerBound) beta = g + 1;
            else beta = g;
            g = alphaBetaM(board, 0, currentDepth, beta - 1, beta);
            if (g < beta) upperBound = g;
            else lowerBound = g;

        } while (lowerBound < upperBound);
        return g;
    }

    private int alphaBetaM(Chessboard board, int ply, int depth, int alpha, int beta) {
        int eval_type = TranspositionTable.HASH_ALPHA;
        int bestScore = -Chessboard.INFINITY;
        evals++;
        int score;

        //Check if the hash table value exists and is stored at the same or higher depth.
        long key = board.getKey();
        if (transpositionTable.entryExists(key) && transpositionTable.getDepth() >= depth) {
            if (transpositionTable.getFlag() == TranspositionTable.HASH_EXACT)
                return transpositionTable.getScore(); // should never be called
            if (transpositionTable.getFlag() == TranspositionTable.HASH_ALPHA && transpositionTable.getScore() <= alpha)
                return transpositionTable.getScore();
            else if (transpositionTable.getFlag() == TranspositionTable.HASH_BETA && transpositionTable.getScore() >= beta)
                return transpositionTable.getScore();
            if (alpha >= beta) return transpositionTable.getScore();
            alpha = Integer.max(alpha, transpositionTable.getScore());
        }
        if (depth <= 0) {
            follow_pv = false;
            score = quiescenceSearch(board, ply, alpha, beta);
            /*if (score <= alpha) {
                transpositionTable.record(board.getKey(), depth, TranspositionTable.HASH_ALPHA, score, 0);
            } else if (score >= beta)
                transpositionTable.record(board.getKey(), depth, TranspositionTable.HASH_BETA, score, 0);
            else
                transpositionTable.record(board.getKey(), depth, TranspositionTable.HASH_EXACT, score, 0); //should never be called
        */}

        if (board.isEndOfGame() != Evaluator.NOT_ENDED) {
            follow_pv = false;
            score = board.eval();
            /*if (score <= alpha) {
                transpositionTable.record(board.getKey(), depth, TranspositionTable.HASH_ALPHA, score, 0);
            } else if (score >= beta)
                transpositionTable.record(board.getKey(), depth, TranspositionTable.HASH_BETA, score, 0);
            else
                transpositionTable.record(board.getKey(), depth, TranspositionTable.HASH_EXACT, score, 0); // should never be called
            if (score == Chessboard.DRAWSCORE) return score;
            */return score + ply - 1;
        }


        //Try Null move
        if (!follow_pv && null_allowed) {
            if (board.movingSideMaterial() > NULLMOVE_THRESHOLD) {
                if (!board.isCheck()) {
                    null_allowed = false;
                    board.makeNullMove();
                    score = -alphaBetaM(board, ply, depth - NULLMOVE_REDUCTION, -beta, -beta + 1);
                    board.unmakeMove();
                    null_allowed = true;
                    if (score >= beta) {
                        return score;
                    }
                }
            }
        }

        null_allowed = true;
        int hashMove = transpositionTable.getMove();
        //if(hashMove != 0 && !b.validateHashMove(hashMove)) hashMove = 0;
        int movesFound = 0;
        int pvMovesFound = 0;

        //Try hash move
        if (hashMove != 0) {
            if (board.makeMove(hashMove)) {
                movesFound++;
                score = -alphaBetaM(board, ply + 1, depth - 1, -alpha - 1, -alpha);
                board.unmakeMove();
                // TODO
                if (score > bestScore) return score;
            } else
                hashMove = 0;
        }
        int[] moves = new int[MoveGenerator.MAX_MOVES];
        int num_moves = board.getAllMoves(moves);


        for (int i = 0; i < num_moves; i++) {
            selectBestMoveFirst(board, moves, num_moves, ply, depth, i);
            if (hashMove != moves[i]) {
                if (board.makeMove(moves[i])) {
                    movesFound++;
                    //Late Move Reduction
                    if (movesFound >= LATEMOVE_THRESHOLD && depth > LATEMOVE_DEPTH_THRESHOLD && !board.isCheck() && !Move.isCapture(moves[i])) {
                        score = -alphaBetaM(board, ply + 1, depth - 2, -alpha - 1, -alpha);
                    } else {
                        score = -alphaBetaM(board, ply + 1, depth - 1, -alpha - 1, -alpha); // PVS Search
                    }
                    if (score > alpha)
                        alpha = score;
                    board.unmakeMove();
                    if (score > bestScore) {
                        if (score >= beta) {
                            //transpositionTable.record(board.getKey(), depth, TranspositionTable.HASH_BETA, score, moves[i]);
                            if (board.isWhiteToMove())
                                whiteHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])] += depth * depth;
                            else
                                blackHeuristics[Move.getFromIndex(moves[i])][Move.getToIndex(moves[i])] += depth * depth;
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


        if (board.getFiftyMove() >= 100) return Chessboard.DRAWSCORE;                 //Fifty-move rule


        return bestScore;
    }

    private void selectBestMoveFirst(Chessboard board, int[] moves, int num_moves, int ply, int depth, int nextIndex) {
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

    private int quiescenceSearch(Chessboard board, int ply, int alpha, int beta) {
        //triangularLength[ply] = ply;

        //Check if we are in check.
        if (board.isCheck()) return alphaBetaM(board, ply, 1, alpha, beta);

        //Standing pat
        int bestScore;
        bestScore = board.eval();
        if (bestScore >= beta) return bestScore;
        if (bestScore > alpha) alpha = bestScore;

        // generate captures & promotions:
        // genCaptures returns a sorted move list
        int[] captures = new int[MoveGenerator.MAX_MOVES];
        int num_captures = board.genCaptures(captures);

        int score;
        for (int i = 0; i < num_captures; i++) {
            board.makeMove(captures[i]);
            score = -quiescenceSearch(board, ply + 1, -beta, -alpha);
            board.unmakeMove();

            if (score >= beta) return score;

            if (score > alpha) alpha = score;
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

    @Override
    public void run() {

    }
}