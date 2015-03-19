package com.chess1800.chess.application.gui;

import javax.swing.JLayeredPane;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by Yonathan on 18/03/2015.
 */
public class Chess1800Controller implements ActionListener, MouseListener, MouseMotionListener {
    private Chess1800Model model;
    private Chess1800View view;
    private SquareJPanel originComponent;
    private boolean userToMove;
    private boolean acceptInput;
    private int xAdjustment;
    private int yAdjustment;
    private String lastFen;
    private boolean flip;
    private PieceJLabel chessPiece;
    BoardJPanel boardJPanel;

    public Chess1800Controller(Chess1800Model model, Chess1800View view) {
        this.model = model;
        this.view = view;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if( e.getComponent() instanceof BoardJPanel ) {
            boardJPanel =(BoardJPanel) e.getComponent();
            if (!acceptInput) return;
            Component c = boardJPanel.getChessBoard().findComponentAt(e.getX(), e.getY());

            if (c instanceof SquareJPanel) return;
            originComponent = (SquareJPanel) c.getParent();

            Point parentLocation = c.getParent().getLocation();
            xAdjustment = parentLocation.x - e.getX();
            yAdjustment = parentLocation.y - e.getY();
            chessPiece = (PieceJLabel) c;
            chessPiece.setLocation(e.getX() + xAdjustment, e.getY() + yAdjustment);
            chessPiece.setSize(chessPiece.getWidth(), chessPiece.getHeight());
            boardJPanel.getLayeredPane().add(chessPiece, JLayeredPane.DRAG_LAYER);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if( e.getComponent() instanceof BoardJPanel ) {
            boardJPanel =(BoardJPanel) e.getComponent();
            if (!acceptInput) return;
            // Only if inside board
            if (chessPiece == null) return;

            chessPiece.setVisible(false);
            Component c = boardJPanel.getChessBoard().findComponentAt(e.getX(), e.getY());
            if (c == null) c = originComponent;

            SquareJPanel parent;
            if (c instanceof PieceJLabel) {
                parent = (SquareJPanel) c.getParent();
                parent.remove(0);
                parent.add(chessPiece);
            } else {
                parent = (SquareJPanel) c;
                parent.add(chessPiece);
            }
            chessPiece.setVisible(true);

            // notifies move
            chess.userMove(flip ? originComponent.getIndex() : 63 - originComponent.getIndex(), flip ? parent.getIndex() : 63 - parent.getIndex());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

        if (!acceptInput) return;
        if (chessPiece == null) return;
        chessPiece.setLocation(e.getX() + xAdjustment, e.getY() + yAdjustment);
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
