package MoveGen;

import Board.Board;

/**
 * Created by Yonathan on 15/12/2014.
 * PseudoLegal move generator.
 * Inspired by Stef Luijten's Winglet Chess @ http://web.archive.org/web/20120621100214/http://www.sluijten.com/winglet/
 */
public class MoveGenerator {
    private Board board = Board.getInstance();


    public int moveGen() {
        byte oppSide;
        int from, to;
        long tempPiece, tempMove, targets, freeSquares;
        int move = 0;

        oppSide = board.whiteToMove ?;
        if (board.whiteToMove) {
            //White's move

        } else {
            //Black's move
        }
        return -1;
    }
}
