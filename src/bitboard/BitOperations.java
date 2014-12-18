package bitboard;

/**
 * Created by Yonathan on 08/12/2014.
 */
public class BitOperations {

    //Based from http://chessprogramming.wikispaces.com/Population+Count
    public static int popCount(long x) {
        final long K1 = 0x5555555555555555L;
        final long K2 = 0x3333333333333333L;
        final long K4 = 0x0f0f0f0f0f0f0f0fL;
        final long KF = 0x0101010101010101L;
        x = x - ((x >> 1) & K1); /* put count of each 2 bits into those 2 bits */
        x = (x & K2) + ((x >> 2) & K2); /* put count of each 4 bits into those 4 bits */
        x = (x + (x >> 4)) & K4; /* put count of each 8 bits into those 8 bits */
        x = (x * KF) >> 56; /* returns 8 most significant bits of x + (x<<8) + (x<<16) + (x<<24) + ...  */
        return (int) x;

    }

    //Works for bigger long numbers than accepted by the method lowestOneBit in the Java library.
    public static long lsb(long x) {
        return (x & -x);
    }

    public int getIndex(long board) {
        return Long.numberOfTrailingZeros(board);
    }


}
