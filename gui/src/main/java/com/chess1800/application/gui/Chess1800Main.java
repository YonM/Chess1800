package com.chess1800.application.gui;

import com.yonathan.chess.core.board.Bitboard;
import com.yonathan.chess.core.board.Chessboard;
import com.yonathan.chess.core.search.*;

/**
 * Created by Yonathan on 19/03/2015.
 */
public class Chess1800Main {
    public static void main(String[] args) {
        Chessboard board = new Bitboard();
        Search AI1= new AI1Threaded(board);
        Search AI2 = new AI2Threaded(board);
        Chess1800Model model = new Chess1800Model(board, AI1, AI2);
        Chess1800View view = new Chess1800View();
        Chess1800Controller controller = new Chess1800Controller(model, view);
        view.addActionListener(controller);
        view.addBoardListener(controller, controller);
        controller.start();
    }
}