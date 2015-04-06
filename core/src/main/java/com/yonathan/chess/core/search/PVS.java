package com.yonathan.chess.core.search;

import com.yonathan.chess.core.board.Chessboard;
import com.yonathan.chess.core.board.Evaluator;
import com.yonathan.chess.core.move.Move;
//import com.yonathan.chess.core.transposition_table.TranspositionTable;

/**
 * Created by Yonathan on 21/12/2014.
 * Main search class, uses alpha-beta algorithm with Principal Variation Search, Iterative Deepening, Quiescence Search, Null Move Pruning and LMR.
 * Based on Winglet by Stef Luijten's Winglet Chess @
 * http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public abstract class PVS extends AbstractSearch {

    public static final int NODE_ROOT = 0;
    public static final int NODE_PV = 1;
    public static final int NODE_NULL = 2;

    protected static final int SCORE_LOWEST = Integer.MIN_VALUE;
    protected static final int SCORE_UNDERPROMOTION = Integer.MIN_VALUE + 1;

    public final static int PHASE_HASH = 0;
    public final static int PHASE_GEN_CAPTURES = 1;
    public final static int PHASE_GOOD_CAPTURES_AND_PROMOS = 2;
    //public final static int PHASE_EQUAL_CAPTURES = 3;
    public final static int PHASE_GEN_NON_CAPTURES = 3;
    public final static int PHASE_NON_CAPTURES = 4;
    public final static int PHASE_BAD_CAPTURES = 5;
    public final static int PHASE_END = 6;

    protected int initialPly;

    protected int rootScore;
    protected int alpha;
    protected int beta;

    protected boolean moveFound;

    protected int[][] triangularArray;
    protected int[] triangularLength;
    protected final int MAX_DEPTH = 16;


    protected SearchObserver observer;
    protected int[][] whiteHeuristics; //history heuristics for white and black pieces indexed by from and to of a move.
    protected int[][] blackHeuristics;
    protected int[] lastPV;
    protected boolean follow_pv;
    protected boolean null_allowed;
    protected static final boolean VERBOSE = true;

    //protected TranspositionTable transpositionTable;


    protected PVS(Chessboard b) {
        super(b);
    }

    protected final boolean nullAllowed() {
        if (!follow_pv && null_allowed)
            if (board.movingSideMaterial() > NULLMOVE_THRESHOLD) //TO prevent null in zugzwang situations.
                if (!board.isCheck()) return true; //Don't allow null move when under check.
        return false;
    }

    public final void findBestMove() throws SearchRunException {
        if (board.isEndOfGame() != Evaluator.NOT_ENDED) {
            System.out.println("finish before started..");
            finishRun();
        }
        for (currentDepth = 1; currentDepth <= MAX_DEPTH; currentDepth++) {
            triangularArray = new int[MAX_GAME_LENGTH][MAX_GAME_LENGTH];
            triangularLength = new int[MAX_GAME_LENGTH];
            follow_pv = true;
            null_allowed = true;
            rootScore = PVS(NODE_ROOT, 0, currentDepth, alpha, beta);
            if (stopSearch) {
                if (VERBOSE) System.out.println("Search stopped at depth:" + currentDepth);
                break;
            }

            if (VERBOSE)
                System.out.println("(" + currentDepth + ") "
                        + ((System.currentTimeMillis() - startTime) / 1000.0) + "s ("
                        + Move.moveToString(lastPV[0], board) + ") -- " + nodes
                        + " nodes evaluated.");

            // stop searching if the current depth leads to a forced mate:
            if ((rootScore > (Chessboard.CHECKMATE - currentDepth)) || (rootScore < -(Chessboard.CHECKMATE - currentDepth))) {
                if (VERBOSE) System.out.println("cut search");
                currentDepth = MAX_DEPTH;
            }
            if (moveFound) {
                if (currentDepth >= thinkToDepth){
                    break;
                }

                else if (moveTime != Integer.MAX_VALUE) {
                    if (System.currentTimeMillis() - startTime > timeForMove) {
                        if (VERBOSE) System.out.println("TimeForMove exceeded. Higher depth search prevented.");
                        break;
                    }
                } else {
                    if (System.currentTimeMillis() - startTime > timeForMove * 0.8) {
                        if (VERBOSE) System.out.println("80% of timeForMove exceeded. Higher depth search prevented.");
                        break;
                    }

                }
            }

        }
        if (VERBOSE) {
            System.out.println("(" + currentDepth + ") "
                    + ((System.currentTimeMillis() - startTime) / 1000.0) + "s ("
                    + Move.moveToString(lastPV[0], board) + ") -- " + nodes
                    + " nodes evaluated.");
            System.out.println(nodes + " positions evaluated. Move returned->" + lastPV[0]);
        }

        finishRun();
    }

    protected abstract int PVS(int nodeType, int ply, int depthRemaining, int alpha, int beta) throws SearchRunException;


    protected abstract int quiescenceSearch(int nodeType, int ply, int alpha, int beta) throws SearchRunException;


    protected final void selectBestMoveFirst(int[] moves, int num_moves, int ply, int depth, int nextIndex) {
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

        //Selects the next best move from the history heuristic. Swapping the moves from the "best index" to the "next index".
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

    protected final void rememberPV() {
        for (int i = 0; i < triangularLength[0]; i++) {
            lastPV[i] = triangularArray[0][i];
        }
        if (globalBestMove != lastPV[0]) {
            globalBestMoveTime = System.currentTimeMillis() - startTime;
            moveFound = true;
            globalBestMove = lastPV[0];
        }
    }


    @Override
    protected void setupRun() {
        startTime = System.currentTimeMillis();
        engineIsWhite = board.isWhiteToMove();
        initialPly = board.getMoveNumber();
        moveFound = false;
        setSearchParameters();
        searching = true;
        globalBestMove = Move.EMPTY;
        nodes = 0;
        whiteHeuristics = new int[MAX_PLY][MAX_PLY];
        blackHeuristics = new int[MAX_PLY][MAX_PLY];
        lastPV = new int[MAX_PLY];
        alpha = -Chessboard.INFINITY;
        beta = Chessboard.INFINITY;
        stopSearch = false;
    }

    @Override
    protected void finishRun() throws SearchRunException {
        System.out.println("do i get here?");
        board.unmakeMove(initialPly);
        searching = false;
        if(globalBestMove!= lastPV[0]) System.out.println("best move discrepancy");
        if (observer != null) {
            System.out.println("sent best move");
            observer.bestMove(globalBestMove);
        }
        if (VERBOSE) displaySearchStats();

        throw new SearchRunException();

    }

    protected int evaluateEndgame(int distanceToInitialPly) {
        if (board.isCheck()) {
            return valueMatedIn(distanceToInitialPly);
        } else {
            return Evaluator.DRAWSCORE;
        }
    }

    protected void betaCutOff(int move, int depth, boolean whiteToMove){
        if(whiteToMove) whiteHeuristics[Move.getFromIndex(move)] [Move.getToIndex(move)] += depth * depth;
        else blackHeuristics[Move.getFromIndex(move)] [Move.getToIndex(move)] += depth * depth;
    }

    protected int valueMatedIn(int distanceToInitialPly) {
        return -Evaluator.CHECKMATE + distanceToInitialPly;
    }

    protected int valueMateIn(int distanceToInitialPly) {
        return Evaluator.CHECKMATE - distanceToInitialPly;
    }


    protected void clear() {

    }

    //Get the Chessboard, the engine is working with.
    public Chessboard getBoard() {
        return board;
    }


    public final void run() {
        try {
            while (true) {
                findBestMove();
            }
        } catch (SearchRunException ignored) {
        }
    }
    abstract protected void notifyMoveFound(int bestMove, int bestScore);

    abstract  protected void setPV(int firstMove);

    @Override
    public void setObserver(SearchObserver observer) {
        this.observer = observer;
    }


    @Override
    public final long getGlobalBestMoveTime() {
        return globalBestMoveTime;
    }

    @Override
    public int getGlobalBestMove(){
        return globalBestMove;
    }


}
