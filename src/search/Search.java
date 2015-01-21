package search;

import board.Board;
import definitions.Definitions;
import evaluation.Evaluator;
import move.MoveAC;
import movegen.MoveGeneratorAC;

/**
 * Created by Yonathan on 21/12/2014.
 * Main search class, uses alpha-beta algorithm with Principal Variation Search, quiescence search and null move pruning.
 * Based on Winglet by Stef Luijten's Winglet Chess @
 * http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public class Search implements Definitions {

    private static int[][] triangularArray;
    private static int[] triangularLength;
    public static Integer legalMoves;
    public static int singleMove = 0;
    private static final int MAX_DEPTH = 4;
    private static int[][] whiteHeuristics;
    private static int[][] blackHeuristics;
    private static int[] lastPV;
    private static boolean follow_pv;
    private static boolean null_allowed;

    public static int num_moves;
    public static int[] moves;
    //private static int lastPVLength;

    private static Evaluator evaluator;

    static {
        triangularArray = new int[MAX_GAME_LENGTH][MAX_GAME_LENGTH];
        triangularLength = new int[MAX_GAME_LENGTH];
    }

    public static void setEvaluator(Evaluator eval) {
        evaluator = eval;
    }


    public static int findBestMove(Board b) {
        int score;
        legalMoves = 0;

        if (b.isEndOfGame()) return NULLMOVE;

        if (legalMoves == 1) return singleMove;

        whiteHeuristics = new int[MAX_PLY][MAX_PLY];
        blackHeuristics = new int[MAX_PLY][MAX_PLY];
        lastPV = new int[MAX_PLY];
        //lastPVLength = 0;

        for (int currentDepth = 1; currentDepth < MAX_DEPTH; currentDepth++) {
            triangularArray = new int[MAX_GAME_LENGTH][MAX_GAME_LENGTH];
            triangularLength = new int[MAX_GAME_LENGTH];
            follow_pv = true;
            null_allowed = true;
            score = alphaBetaPVS(b, 0, currentDepth, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
            if ((score > (Evaluator.CHECKMATE - currentDepth)) || (score < -(Evaluator.CHECKMATE - currentDepth)))
                currentDepth = MAX_DEPTH;
        }
        return lastPV[0];
    }

    private static int alphaBetaPVS(Board b, int ply, int depth, int alpha, int beta) {
        int i, j, movesFound, pvMovesFound, val;
        triangularLength[ply] = ply;
        if (depth <= 0) {
            follow_pv = false;
            return quiescenceSearch(b, ply, alpha, beta);
        }
        //Threefold repetition check
        if (b.repetitionCount() >= 3) return Evaluator.DRAWSCORE;

        //Try Null move
        if (!follow_pv && null_allowed) {
            if (b.movingSidePieceMaterial() > NULLMOVE_LIMIT) {
                if (!b.isOwnKingAttacked()) {
                    null_allowed = false;
                    b.makeNullMove();
                    val = -alphaBetaPVS(b, ply, depth - NULLMOVE_REDUCTION, -beta, -beta + 1);
                    b.unmakeMove();
                    null_allowed = true;
                    if (val >= beta) {
                        return val;
                    }
                }
            }
        }

        null_allowed = true;
        movesFound = 0;
        pvMovesFound = 0;
        int[] moves = new int[MAX_MOVES];
        num_moves = MoveGeneratorAC.getAllMoves(b, moves);

        for (i = 0; i < num_moves; i++) {
            selectBestMoveFirst(b, ply, depth, i, b.whiteToMove);
            b.makeMove(b.moves[i]);
            if (!b.isOtherKingAttacked()) {
                movesFound++;
                if (pvMovesFound != 0) {
                    val = -alphaBetaPVS(b, ply + 1, depth - 1, -alpha - 1, -alpha);
                    if ((val > alpha) && (val < beta))
                        val = -alphaBetaPVS(b, ply + 1, depth - 1, -beta, -alpha); //Better move found, normal alpha-beta.
                } else {
                    val = -alphaBetaPVS(b, ply + 1, depth - 1, -beta, -alpha); // Normal alpha-beta
                }
                b.unmakeMove(b.moves[i]);
                if (val >= beta) {
                    if (b.whiteToMove)
                        whiteHeuristics[MoveAC.getFromIndex(b.moves[i])][MoveAC.getToIndex(b.moves[i])] += depth * depth;
                    else
                        blackHeuristics[MoveAC.getFromIndex(b.moves[i])][MoveAC.getToIndex(b.moves[i])] += depth * depth;
                    return beta;
                }
                if (val > alpha) {
                    alpha = val;
                    pvMovesFound++;
                    triangularArray[ply][ply] = b.moves[i];    //save the move
                    for (j = ply + 1; j < triangularLength[ply + 1]; j++)
                        triangularArray[ply][j] = triangularArray[ply + 1][j];  //appends latest best PV from deeper plies

                    triangularLength[ply] = triangularLength[ply + 1];
                }
            } else {
                b.unmakeMove(b.moves[i]);
            }

        }
        if (pvMovesFound != 0) {
            if (b.whiteToMove)
                whiteHeuristics[MoveAC.getFromIndex(triangularArray[ply][ply])][MoveAC.getToIndex(triangularArray[ply][ply])] += depth * depth;
            else
                blackHeuristics[MoveAC.getFromIndex(triangularArray[ply][ply])][MoveAC.getToIndex(triangularArray[ply][ply])] += depth * depth;
        }

        if (b.fiftyMove >= 100) return Evaluator.DRAWSCORE;                 //Fifty-move rule

        if (movesFound == 0) {
            if (b.isOwnKingAttacked()) return -Evaluator.CHECKMATE + ply - 1; //Checkmate
            return Evaluator.DRAWSCORE;                                 //Stalemate
        }

        return alpha;
    }

    private static void selectBestMoveFirst(Board b, int ply, int depth, int nextIndex, boolean whiteToMove) {
        int tempMove = 0;
        int best, bestIndex, i;
        // Re-orders the move list so that the PV is selected as the next move to try.
        if (follow_pv && depth > 1) {
            for (i = nextIndex; i < num_moves; i++) {
                if (b.moves[i] == lastPV[ply]) {
                    tempMove = b.moves[i];
                    b.moves[i] = b.moves[nextIndex];
                    b.moves[nextIndex] = tempMove;
                    return;
                }
            }
        }

        if (whiteToMove) {
            best = whiteHeuristics[MoveAC.getFromIndex(b.moves[nextIndex])][MoveAC.getToIndex(b.moves[nextIndex])];
            bestIndex = nextIndex;
            for (i = nextIndex + 1; i < num_moves; i++) {
                if (whiteHeuristics[MoveAC.getFromIndex(b.moves[i])][MoveAC.getToIndex(b.moves[i])] > best) {
                    best = whiteHeuristics[MoveAC.getFromIndex(b.moves[i])][MoveAC.getToIndex(b.moves[i])];
                    bestIndex = i;
                }
            }
            if (bestIndex > nextIndex) {
                tempMove = b.moves[bestIndex];
                b.moves[bestIndex] = b.moves[nextIndex];
                b.moves[nextIndex] = tempMove;
            }

        } else {
            best = blackHeuristics[MoveAC.getFromIndex(b.moves[nextIndex])][MoveAC.getToIndex(b.moves[nextIndex])];
            bestIndex = nextIndex;
            for (i = nextIndex + 1; i < num_moves; i++) {
                if (blackHeuristics[MoveAC.getFromIndex(b.moves[i])][MoveAC.getToIndex(b.moves[i])] > best) {
                    best = blackHeuristics[MoveAC.getFromIndex(b.moves[i])][MoveAC.getToIndex(b.moves[i])];
                    bestIndex = i;
                }
            }
            if (bestIndex > nextIndex) {
                tempMove = b.moves[bestIndex];
                b.moves[bestIndex] = b.moves[nextIndex];
                b.moves[nextIndex] = tempMove;
            }
        }
    }

    private static int quiescenceSearch(Board b, int ply, int alpha, int beta) {
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
        int num_captures = MoveGeneratorAC.genCaptures(b, captures);

        for (int i = 0; i < num_captures; i++) {
            b.makeMove(captures[i]);
            {
                if (!b.isOtherKingAttacked()) {
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
                } else
                    b.unmakeMove(captures[i]);
            }
        }
        return alpha;
    }


    private static void rememberPV() {
        int i;
        //lastPVLength = triangularLength[0];
        for (i = 0; i < triangularLength[0]; i++) {
            lastPV[i] = triangularArray[0][i];
        }
    }
}
