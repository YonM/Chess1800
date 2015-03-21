package com.chess1800.chess.application.gui;

import com.chess1800.chess.search.PVSHard;
import com.chess1800.chess.search.PVSSoft;
import com.chess1800.chess.search.Search;

/**
 * Created by Yonathan on 19/03/2015.
 */
public class Chess1800Main {
    public static void main(String[] args) {
        Search pvsSoft= new PVSSoft();
        Search pvsHard = new PVSHard();
        Chess1800Model model = new Chess1800Model(pvsSoft,pvsHard);
        Chess1800View view = new Chess1800View();
        Chess1800Controller controller = new Chess1800Controller(model, view);
        view.addActionListener(controller);
        view.addBoardListener(controller, controller);
        controller.start();
    }
}
