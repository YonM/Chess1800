package com.chess1800.core.search;

import com.chess1800.core.board.Bitboard;
import com.chess1800.core.board.Chessboard;
import com.chess1800.core.board.Evaluator;
import com.chess1800.core.move.Move;

/**
 * Created by Yonathan on 21/12/2014.
 * Main search class, uses alpha-beta algorithm with Principal Variation Search, Iterative Deepening, Quiescence Search, Null Move Pruning and LMR.
 * Based on Winglet by Stef Luijten's Winglet Chess @
 * http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public abstract class PVS extends AbstractSearch{


    protected final String type;
    protected int initialPly;

    protected int score;
    protected int alpha;
    protected int beta;

    protected boolean moveFound;

    protected int[][] triangularArray;
    protected int[] triangularLength;
    protected final int MAX_DEPTH = 16;
    protected int nodes;

    protected SearchObserver observer;
    protected int[][] whiteHeuristics;
    protected int[][] blackHeuristics;
    protected int[] lastPV;
    protected boolean follow_pv;
    protected boolean null_allowed;
    protected static final boolean VERBOSE= true;

    private long bestMoveTime;
    private int globalBestMove;


    protected PVS(Chessboard b, String type){
        super(b);
        this.type=type;
    }

    protected final boolean nullAllowed(){
        if (!follow_pv && null_allowed)
            if (board.movingSideMaterial() > NULLMOVE_THRESHOLD) //TO prevent null in zugzwang situations.
                if (!board.isCheck()) return true; //Don't allow null move when under check.
        return false;
    }

    public final void findBestMove() throws SearchRunException {
        if (board.isEndOfGame()!= Evaluator.NOT_ENDED){
            finishRun();
        }
        System.out.println(type+"\n"+"last move: " + board.getLastMove());
        int currentDepth;
        for (currentDepth= 1; currentDepth <= MAX_DEPTH; currentDepth++) {
            triangularArray = new int[MAX_GAME_LENGTH][MAX_GAME_LENGTH];
            triangularLength = new int[MAX_GAME_LENGTH];
            follow_pv = true;
            null_allowed = true;
            score = PVS(0, currentDepth, alpha, beta);

            if(stopSearch){
                if(VERBOSE)System.out.println("Search stopped at depth:" +currentDepth);
                break;
            }


            if(VERBOSE)
                System.out.println("(" + currentDepth + ") "
                        + ( (System.currentTimeMillis() - startTime) / 1000.0) + "s ("
                        + Move.moveToString(lastPV[0]) + ") -- " + nodes
                        + " nodes evaluated.");
            // stop searching if the current depth leads to a forced mate:
            if ((score > (Chessboard.CHECKMATE - currentDepth)) || (score < -(Chessboard.CHECKMATE - currentDepth))) {
                if(VERBOSE) System.out.println("cut search");
                currentDepth = MAX_DEPTH;
            }
            if(moveFound) {
                if (currentDepth == depth) break;

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
        if(VERBOSE) {
            System.out.println("(" + currentDepth + ") "
                    + ((System.currentTimeMillis() - startTime) / 1000.0) + "s ("
                    + Move.moveToString(lastPV[0]) + ") -- " + nodes
                    + " nodes evaluated.");
            System.out.println(nodes + " positions evaluated. Move returned->" + lastPV[0]);
        }
        finishRun();
    }

    protected abstract int PVS(int i, int currentDepth, int alpha, int beta) throws SearchRunException;


    protected abstract int quiescenceSearch(int ply, int alpha, int beta) throws SearchRunException;



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
        if(globalBestMove != lastPV[0]){
            moveFound = true;
            bestMoveTime = System.currentTimeMillis() -startTime;
            globalBestMove = lastPV[0];
        }
    }


    @Override
    protected void setupRun() {
        startTime = System.currentTimeMillis();
        engineIsWhite = board.isWhiteToMove();
        if (engineIsWhite) System.out.println("engine is white");
        else System.out.println("engine is black");
        initialPly = board.getMoveNumber();
        moveFound = false;
        setSearchParameters();
        searching = true;
        globalBestMove = 0;
        nodes = 0;
        whiteHeuristics = new int[MAX_PLY][MAX_PLY];
        blackHeuristics = new int[MAX_PLY][MAX_PLY];
        lastPV = new int[MAX_PLY];
        alpha = -Chessboard.INFINITY;
        beta = Chessboard.INFINITY;
        stopSearch = false;
    }

    @Override
    protected void finishRun() throws SearchRunException{
        board.unmakeMove(initialPly);
        searching = false;
        if(observer != null){
            observer.bestMove(globalBestMove);
        }
        if(VERBOSE) displaySearchStats();

        throw new SearchRunException();

    }

    protected void notifyMoveFound(int bestMove, int bestScore, int alpha, int beta){

        AbstractSearchInfo info = new AbstractSearchInfo();
        if(observer!=null){
            observer.info(info);
        }

    }

    protected void clear(){

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




    @Override
    public void setObserver(SearchObserver observer){
        this.observer = observer;
    }



    @Override
    public final long getBestMoveTime() {
        return bestMoveTime;
    }




}
