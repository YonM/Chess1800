package com.chess1800.chess.application.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.HeadlessException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Yonathan on 18/03/2015.
 */
public class Chess1800View extends JFrame implements Observer {
    String timeString[] = {"1 second", "2 seconds", "5 seconds", "15 seconds", "30 seconds", "60 seconds"};
    int timeValues[] = {1000, 2000, 5000, 15000, 30000, 60000};
    int timeDefaultIndex = 0;
    JPanel global, control;
    private char[] boardPieces;
    JTextField fenField;

    public Chess1800View() throws HeadlessException {
    }

    @Override
    public void update(Observable o, Object arg) {
        invalidate();
        validate();
        repaint();
    }

    public void setBoardData(char[] pieces){
        boardPieces = pieces;
    }

    public void userMove(int i, int i1) {

    }
}
