package com.chess1800.application.gui;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by Yonathan on 19/03/2015.
 */
public class BoardJPanel extends JPanel{
    private String lastFEN;
    JLayeredPane layeredPane;
    JPanel chessBoard;
    PieceJLabel chessPiece;
    private boolean flip;
    final int height = 75*8;
    final int width = 75*8;
    public BoardJPanel() {
        Dimension d = new Dimension(width, height);
        layeredPane = new JLayeredPane();
        add(layeredPane);
        layeredPane.setPreferredSize(d);
        chessBoard = new JPanel();
        layeredPane.add(chessBoard, JLayeredPane.DEFAULT_LAYER);
        chessBoard.setLayout( new GridLayout(8, 8) );
        chessBoard.setPreferredSize(d);
        for (int i = 0; i < 64; i++) chessBoard.add(new SquareJPanel(i));
        chessBoard.setBounds(0, 0, width, height);
        layeredPane.setBounds(0, 0, width, height);
        setBounds(0, 0, width, height);
    }
    public void addMouseListeners(MouseMotionListener mml, MouseListener ml){
        layeredPane.addMouseMotionListener(mml);
        layeredPane.addMouseListener(ml);
    }
    public JPanel getChessBoard() {
        return chessBoard;
    }
    public JLayeredPane getLayeredPane() {
        return layeredPane;
    }
    public void setFEN(String fen, boolean flip, boolean redraw){
        if (fen == null) return;
        this.flip = flip;
        lastFEN = fen;
        int i = 0;
        int j = 0;
        while (i < fen.length()) {
            char p = fen.charAt(i++);
            if (p != '/') {
                int number = 0;
                try {
                    number = Integer.parseInt(String.valueOf(p));
                } catch (Exception ignored) {}
                for (int k = 0; k < (number == 0 ? 1 : number); k++) {
                    SquareJPanel panel = (SquareJPanel) chessBoard.getComponent(flip ? (63 - j++): (j++));
                    try {
                        PieceJLabel label = (PieceJLabel) panel.getComponent(0);
                        if (label.getPiece() != p || redraw) {
                            label.setVisible(false);
                            panel.remove(0);
                            throw new Exception();
                        }
                    } catch (Exception e) {
                        if (number == 0) panel.add(new PieceJLabel(p));
                    }
                    if (j>=64) {
                        return; // security
                    }
                }
            }
        }
    }
    public void unHighlight() {
        for (int i = 0; i< 64; i++) ((SquareJPanel) chessBoard.getComponent(i)).setHighlighted(false);
    }
    public void highlight(int from, int to) {
        SquareJPanel squareFrom = (SquareJPanel) chessBoard.getComponent(flip ? (from) : (63 - from));
        SquareJPanel squareTo = (SquareJPanel) chessBoard.getComponent(flip ? (to) : (63 - to));
        squareFrom.setHighlighted(true);
        squareTo.setHighlighted(true);
    }
    public String getLastFEN() {
        return lastFEN;
    }
    public void addPiece(PieceJLabel chessPiece, Integer dragLayer) {
        layeredPane.add(chessPiece, dragLayer);
    }
}