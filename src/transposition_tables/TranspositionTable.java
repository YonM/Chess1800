package transposition_tables;

import board.Board;
import definitions.Definitions;

/**
 * Created by Yonathan on 02/02/2015.
 * Used for MTDF, specifically in the alphaBetaM call of the class.
 * Based on Mediocre Chess by Jonatan Pettersson sources @ http://sourceforge.net/projects/mediocrechess/
 */
public class TranspositionTable implements Definitions{
    public int[] hashTable; // Used for transposition table
    public final int HASHSIZE; // The number of slots either table will have

    public static final int SLOTS = 4;
    public TranspositionTable(int sizeInMb) {
        this.HASHSIZE = sizeInMb * 1024 * 1024 * 8 / 32 / SLOTS;
        hashTable = new int[HASHSIZE * SLOTS];
    }

    /**
     * Clears the transposition table
     */
    public void clear() {
        hashTable = new int[HASHSIZE * SLOTS];
    } // END clear()


    /**
     * Records the entry if the spot is empty or new position has deeper depth
     * or old position has wrong ancientNodeSwitch
     *
     * @param zobrist
     * @param depth
     * @param flag
     * @param eval
     * @param move
     */
    public void record(long zobrist, int depth, int flag, int eval, int move) {
        // Always replace scheme
        int hashKey = (int) (zobrist % HASHSIZE) * SLOTS;
        hashTable[hashKey] = 0 | (eval + 0x1FFFF)
                | ((1) << 18) | (flag << 20)
                | (depth << 22);
        hashTable[hashKey + 1] = move;
        hashTable[hashKey + 2] = (int) (zobrist >> 32);
        hashTable[hashKey + 3] = (int) (zobrist & 0xFFFFFFFF);
    }

    /**
     * Returns true if the entry at the right index is 0 which means we have an
     * entry stored
     *
     * @param zobrist
     */
    public boolean entryExists(long zobrist) {
        int hashKey = (int) (zobrist % HASHSIZE) * SLOTS;
        return hashTable[hashKey + 2] == (int) (zobrist >> 32) && hashTable[hashKey + 3] == (int) (zobrist & 0xFFFFFFFF) &&
                hashTable[hashKey] != 0;
    } // END entryExists

    /**
     * Returns the eval at the right index if the zobrist matches
     *
     * @param zobrist
     */
    public int getEval(long zobrist) {
        int hashKey = (int) (zobrist % HASHSIZE) * SLOTS;
        if (hashTable[hashKey + 2] == (int) (zobrist >> 32) && hashTable[hashKey + 3] == (int) (zobrist & 0xFFFFFFFF))
            return ((hashTable[hashKey] & 0x3FFFF) - 0x1FFFF);
        return 0;
    } // END getEval

    /**
     * Returns the flag at the right index if the zobrist matches
     *
     * @param zobrist
     */
    public int getFlag(long zobrist) {
        int hashKey = (int) (zobrist % HASHSIZE) * SLOTS;
        if (hashTable[hashKey + 2] == (int) (zobrist >> 32) && hashTable[hashKey + 3] == (int) (zobrist & 0xFFFFFFFF))
            return ((hashTable[hashKey] >> 20) & 3);
        return 0;
    } // END getFlag

    /**
     * Returns the move at the right index if the zobrist matches
     *
     * @param zobrist
     */
    public int getMove(long zobrist) {
        int hashKey = (int) (zobrist % HASHSIZE) * SLOTS;
        if (hashTable[hashKey + 2] == (int) (zobrist >> 32) && hashTable[hashKey + 3] == (int) (zobrist & 0xFFFFFFFF))
            return hashTable[hashKey + 1];
        return 0;
    } // END getMove

    /**
     * Returns the depth at the right index if the zobrist matches
     *
     * @param zobrist
     */
    public int getDepth(long zobrist) {
        int hashKey = (int) (zobrist % HASHSIZE) * SLOTS;
        if (hashTable[hashKey + 2] == (int) (zobrist >> 32) && hashTable[hashKey + 3] == (int) (zobrist & 0xFFFFFFFF))
            return (hashTable[hashKey] >> 22);
        return 0;
    } // END getDepth

    /**
     * Collects the principal variation starting from the position on the board
     *
     * @param b
     * The position to collect pv from
//     * @param current_depth
     * How deep the pv goes (avoids situations where keys point to
     * each other infinitely)
     * @return collectString The moves in a string
     */
    public int[] collectPV(Board b) {
        int[] arrayPV = new int[MAX_PLY];
        int move = getMove(b.key);
        int i = 20;
        int index = 0;
        int pv_error=0;
        while (i > 0) {
            if (move == 0 || !b.validateHashMove(move))
                break;
            arrayPV[index] = move;
            if (b.makeMove(move)) {
                move = getMove(b.key);
                i--;
                index++;
            }else{
                if(++pv_error==1)System.out.println("pv error");
            }

        }
        // Unmake the moves
        for (i = index - 1; i >= 0; i--) {
            b.unmakeMove(arrayPV[i]);
        }
        return arrayPV;
    } // END collectPV()
}
