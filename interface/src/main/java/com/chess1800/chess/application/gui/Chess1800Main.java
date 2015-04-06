package com.chess1800.chess.application.gui;

import com.chess1800.chess.board.Board;
import com.chess1800.chess.board.Chessboard;
import com.chess1800.chess.search.AI2;
import com.chess1800.chess.search.AI1;
import com.chess1800.chess.search.Search;

/**
 * Created by Yonathan on 19/03/2015.
 */
public class Chess1800Main {
    public static void main(String[] args) {
        Chessboard board = new Board();
        Search pvsSoft= new AI1(board);
        Search pvsHard = new AI2(board);
        Chess1800Model model = new Chess1800Model(board, pvsSoft, pvsHard);
        Chess1800View view = new Chess1800View();
        Chess1800Controller controller = new Chess1800Controller(model, view);
        view.addActionListener(controller);
        view.addBoardListener(controller, controller);
        controller.start();
    }
}
