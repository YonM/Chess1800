package com.chess1800.chess.transposition_table;

import com.chess1800.chess.board.Board;
import com.chess1800.chess.move.Move;

import java.util.Arrays;

/**
 * Created by Yonathan on 02/02/2015.
 * Used for MTDF, specifically in the alphaBetaM call of the class.
 * Based on Mediocre Chess by Jonatan Pettersson sources @ http://sourceforge.net/projects/mediocrechess/
 */
public class TranspositionTable {
    //For Transposition table
    public static final int HASH_EXACT = 0;
    public static final int HASH_ALPHA = 1;
    public static final int HASH_BETA = 2;

    public static final int HASH_MASK = 0x3f;
    public static final int HASH_SHIFT = 2;

    public static final int DEPTH_SHIFT = 5; //Max search depth is 16, so shift is 5 bits.
    public static final int DEPTH_MASK = 0x1F;
    private int entriesOccupied;
    public long[] keys; //for zobrist keys
    public long[] infos;

    private int size;
    private long info;
    private int sizeBits;
    public static final int SLOTS = 4;
    private int score;

    public TranspositionTable(int sizeMb) {
        sizeBits = Board.square2Index(sizeMb) + 16;
        size = 1 << sizeBits;
        keys = new long[size];
        infos = new long[size];
        entriesOccupied = 0;
    }

    /**
     * Clears the transposition table
     */
    public void clear() {
        entriesOccupied = 0;
        Arrays.fill(keys, 0);
    } // END clear()


    /**
     * Records the entry if the spot is empty or new position has deeper depth
     * or old position has wrong ancientNodeSwitch
     *
     * @param zobrist
     * @param depth
     * @param lowerBound
     * @param score
     * @param move
     */
    public void record(long zobrist, int depth, int lowerBound, int upperBound, int score, int move) {
        // Always replace scheme
        int flag;
        if (score <= lowerBound) flag = HASH_ALPHA;
        else if (score >= upperBound) flag = HASH_BETA;
        else flag = HASH_EXACT;
        int index =(int) zobrist>>> (64-sizeBits);
        keys[index] = zobrist;
        info = (move & Move.MOVE_MASK) | ((flag & HASH_MASK) << Move.MOVE_SHIFT) | (depth << (HASH_SHIFT + Move.MOVE_SHIFT)) |(long) (score<< (HASH_SHIFT + Move.MOVE_SHIFT + DEPTH_SHIFT));
        infos[index] = info;


    }

    /**
     * Returns true if the entry at the right index is 0 which means we have an
     * entry stored
     *
     * @param zobrist
     */
    public boolean entryExists(long zobrist) {
        info = 0;
        score = 0;
        int index= (int) zobrist >>> (64 - sizeBits);
        if(keys[index]==zobrist){
            info = infos[index];
            score = (int)(info >>> (HASH_SHIFT + Move.MOVE_SHIFT + DEPTH_SHIFT));
            return true;
        }
        return false;
    } // END entryExists

    /**
     * Returns the eval at the right index if the zobrist matches
     *
     *
     */
    public int getScore() {
        return score;
    } // END getScore

    /**
     * Returns the flag at the right index if the zobrist matches
     *
     *
     */
    public int getFlag() {
        return (int) (info>>>Move.MOVE_SHIFT) & HASH_MASK;
    } // END getFlag

    /**
     * Returns the move at the right index if the zobrist matches
     *
     *
     */
    public int getMove() {
        return (int) info & Move.MOVE_MASK;
    } // END getMove

    /**
     * Returns the depth at the right index if the zobrist matches
     *
     *
     */
    public int getDepth() {
        return (int) (info >>>(HASH_SHIFT + Move.MOVE_SHIFT) & DEPTH_MASK );
    } // END getDepth

}