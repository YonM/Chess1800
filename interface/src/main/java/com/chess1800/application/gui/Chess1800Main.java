package com.chess1800.application.gui;

import com.chess1800.core.board.Bitboard;
import com.chess1800.core.board.Chessboard;
import com.chess1800.core.search.PVSHard;
import com.chess1800.core.search.PVSSoft;
import com.chess1800.core.search.Search;

/**
 * Created by Yonathan on 19/03/2015.
 */
public class Chess1800Main {
    public static void main(String[] args) {
        Chessboard board = new Bitboard();
        Search pvsSoft= new PVSSoft(board);
        Search pvsHard = new PVSHard(board);
        Chess1800Model model = new Chess1800Model(board, pvsSoft, pvsHard);
        Chess1800View view = new Chess1800View();
        Chess1800Controller controller = new Chess1800Controller(model, view);
        view.addActionListener(controller);
        view.addBoardListener(controller, controller);
        controller.start();
    }
}
