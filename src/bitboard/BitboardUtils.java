package bitboard;

/**
 * Created by Yonathan on 07/12/2014.
 */
public class BitboardUtils {
    public static final long A8 = 0x8000000000000000L;
    public static final long H1 = 0x0000000000000001L;

    // Board borders
    public static final long b_d = 0x00000000000000ffL; // down
    public static final long b_u = 0xff00000000000000L; // up
    public static final long b_r = 0x0101010101010101L; // right
    public static final long b_l = 0x8080808080808080L; // left

    // Board borders (2 squares),for the knight
    public static final long b2_d = 0x000000000000ffffL; // down
    public static final long b2_u = 0xffff000000000000L; // up
    public static final long b2_r = 0x0303030303030303L; // right
    public static final long b2_l = 0xC0C0C0C0C0C0C0C0L; // left

    //Board borders (3 squares)
    public static final long b3_d = 0x0000000000ffffffL; // down
    public static final long b3_u = 0xffffff0000000000L; // up

    public static final long r2_d = 0x000000000000ff00L; // rank 2 down
    public static final long r2_u = 0x00ff000000000000L; // up

    public static final long r3_d = 0x0000000000ff0000L; // rank 3 down
    public static final long r3_u = 0x0000ff0000000000L; // up

    // Board centers (for evaluation)
    public static final long c4 = 0x0000001818000000L; // center (4 squares)
    public static final long c16 = 0x00003C3C3C3C0000L; // center (16 squares)
    public static final long c36 = 0x007E7E7E7E7E7E00L; // center (36 squares)

    public static final long r4 = 0xC3C300000000C3C3L; // corners (4 squares)
    public static final long r9 = 0xE7E7E70000E7E7E7L; // corners (9 squares)


}
