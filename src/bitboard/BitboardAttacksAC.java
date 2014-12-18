package bitboard;

/**
 * Created by Yonathan on 07/12/2014.
 */
public class BitboardAttacksAC {

    public static long[] rook;
    public static long[] bishop;
    public static long[] blackPawn;
    public static long[] whitePawn;
    public static long[] knight;
    public static long[] king;

    public BitboardAttacksAC() {
        rook = new long[64];
        bishop = new long[64];
        blackPawn = new long[64];
        whitePawn = new long[64];
        knight = new long[64];
        king = new long[64];

        long square = 1;
        byte i = 0;
    }
}