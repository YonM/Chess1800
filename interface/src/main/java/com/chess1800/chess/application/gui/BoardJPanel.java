package com.chess1800.chess.application.gui;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by Yonathan on 19/03/2015.
 */
public class BoardJPanel extends JPanel{
    JLayeredPane layeredPane;

    public JPanel getChessBoard() {
        return chessBoard;
    }

    JPanel chessBoard;
    PieceJLabel chessPiece;

    final int height = 75*8;
    final int width = 75*8;
    Chess1800View chess;

    public JLayeredPane getLayeredPane() {
        return layeredPane;
    }

    public BoardJPanel(Chess1800View chess) {
        Dimension d = new Dimension(width, height);

        layeredPane = new JLayeredPane();
        add(layeredPane);
        layeredPane.setPreferredSize( d );

        chessBoard = new JPanel();
        layeredPane.add(chessBoard, JLayeredPane.DEFAULT_LAYER);
        chessBoard.setLayout( new GridLayout(8, 8) );
        chessBoard.setPreferredSize(d);

        for (int i = 0; i < 64; i++) chessBoard.add(new SquareJPanel(i));

        chessBoard.setBounds(0, 0, width, height);
        layeredPane.setBounds(0, 0, width, height);
        setBounds(0, 0, width, height);

        this.chess = chess;

    }

    public void addListeners(MouseMotionListener mml, MouseListener ml){
        layeredPane.addMouseMotionListener(mml);
        layeredPane.addMouseListener(ml);
    }

}
