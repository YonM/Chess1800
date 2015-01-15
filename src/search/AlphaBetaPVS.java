package search;

import board.Board;
import definitions.Definitions;
import evaluation.Evaluator;
import move.MoveAC;
import movegen.MoveGenerator;
import zobrist.Zobrist;

import java.util.Arrays;

/**
 * Created by Yonathan on 21/12/2014.
 * Main search class, uses alpha-beta algorithm with Principal Variation Search.
 * Based on Winglet by Stef Luijten's Winglet Chess @
 * http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public class AlphaBetaPVS implements Definitions {

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
    //private static int lastPVLength;

    private static Evaluator evaluator;
    private final static Board b;

    static {
        triangularArray = new int[MAX_GAME_LENGTH][MAX_GAME_LENGTH];
        triangularLength = new int[MAX_GAME_LENGTH];
        b = Board.getInstance();
    }

    public static void setEvaluator(Evaluator eval) {
        evaluator = eval;
    }


    public static int findBestMove(Board b) {
        int currentDepth, score;
        legalMoves = 0;

        if (b.isEndOfGame()) return NULLMOVE;

        if (legalMoves == 1) return singleMove;

        whiteHeuristics = new int[Board.MAX_PLY][Board.MAX_PLY];
        blackHeuristics = new int[Board.MAX_PLY][Board.MAX_PLY];
        lastPV = new int[Board.MAX_PLY];
        //lastPVLength = 0;

        for (currentDepth = 1; currentDepth < MAX_DEPTH; currentDepth++) {
            Arrays.fill(b.moveBufLen, 0);
            Arrays.fill(b.moves, NULLMOVE);
            triangularArray = new int[MAX_GAME_LENGTH][MAX_GAME_LENGTH];
            triangularLength = new int[MAX_GAME_LENGTH];
            follow_pv = true;
            null_allowed = true;
            score = alphaBetaPVS(0, currentDepth, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
            if ((score > (Evaluator.CHECKMATE - currentDepth)) || (score < -(Evaluator.CHECKMATE - currentDepth)))
                currentDepth = MAX_DEPTH;
        }
        return lastPV[0];
    }

    private static int alphaBetaPVS(int ply, int depth, int alpha, int beta) {
        int i, j, movesFound, pvMovesFound, val;
        triangularLength[ply] = ply;
        if (depth <= 0) {
            follow_pv = false;
            return quiescenceSearch(ply, alpha, beta);
        }
        //Threefold repetition check
        if (b.repetitionCount() >= 3) return Evaluator.DRAWSCORE;

        //Try Null move
        if (!follow_pv && null_allowed) {
            if ((!b.whiteToMove && b.totalBlackPieces > NULLMOVE_LIMIT) || (b.whiteToMove && (b.totalWhitePieces > NULLMOVE_LIMIT))) {
                if (b.isOwnKingAttacked()) {
                    null_allowed = false;
                    b.whiteToMove = !b.whiteToMove;
                    val = -alphaBetaPVS(ply, depth - NULLMOVE_REDUCTION, -beta, -beta + 1);
                    b.key ^= Zobrist.whiteMove;
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
        b.moveBufLen[ply + 1] = MoveGenerator.moveGen(b, b.moveBufLen[ply]);

        for (i = b.moveBufLen[ply]; i < b.moveBufLen[ply + 1]; i++) {
            selectBestMoveFirst(ply, depth, i, b.whiteToMove);
            b.makeMove(b.moves[i]);
            if (!b.isOtherKingAttacked()) {
                movesFound++;
                if (pvMovesFound != 0) {
                    val = -alphaBetaPVS(ply + 1, depth - 1, -alpha - 1, -alpha);
                    if ((val > alpha) && (val < beta))
                        val = -alphaBetaPVS(ply + 1, depth - 1, -beta, -alpha); //Better move found, normal alpha-beta.
                } else {
                    val = -alphaBetaPVS(ply + 1, depth - 1, -beta, -alpha); // Normal alpha-beta
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

    private static void selectBestMoveFirst(int ply, int depth, int nextIndex, boolean whiteToMove) {
        int tempMove = 0;
        int best, bestIndex, i;
        // Re-orders the move list so that the PV is selected as the next move to try.
        if (follow_pv && depth > 1) {
            for (i = nextIndex; i < b.moveBufLen[ply + 1]; i++) {
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
            for (i = nextIndex + 1; i < b.moveBufLen[ply + 1]; i++) {
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
            for (i = nextIndex + 1; i < b.moveBufLen[ply + 1]; i++) {
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

    private static int quiescenceSearch(int ply, int alpha, int beta) {
        triangularLength[ply] = ply;

        //Check if we are in check.
        if (b.isOwnKingAttacked()) return alphaBetaPVS(ply, 1, alpha, beta);

        //Standing pat
        int val;
        val = evaluator.eval(b);
        if (val >= beta) return val;
        if (val > alpha) alpha = val;

        // generate captures & promotions:
        // genCaptures returns a sorted move list
        b.moveBufLen[ply + 1] = MoveGenerator.genCaptures(b, b.moveBufLen[ply]);

        for (int i = b.moveBufLen[ply]; i < b.moveBufLen[ply + 1]; i++) {
            b.makeMove(b.moves[i]);
            {
                if (!b.isOtherKingAttacked()) {
                    val = -quiescenceSearch(ply + 1, -beta, -alpha);
                    b.unmakeMove(b.moves[i]);

                    if (val >= beta) return val;

                    if (val > alpha) {
                        alpha = val;
                        triangularArray[ply][ply] = b.moves[i];
                        for (int j = ply + 1; j < triangularLength[ply + 1]; j++) {
                            triangularArray[ply][j] = triangularArray[ply + 1][j];
                        }
                        triangularLength[ply] = triangularLength[ply + 1];
                    }
                } else
                    b.unmakeMove(b.moves[i]);
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
