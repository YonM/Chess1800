package com.chess1800.chess.application.gui;

import com.chess1800.chess.board.Chessboard;
import com.chess1800.chess.search.SearchObserver;

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
public class Chess1800Controller implements SearchObserver, ActionListener, MouseListener, MouseMotionListener {
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
    private boolean started;
    //BoardJPanel boardPanel;

    public Chess1800Controller(Chess1800Model model, Chess1800View view) {
        this.model = model;
        this.view = view;
        userToMove = true;
        flip = false;
        acceptInput = true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("restart".equals(e.getActionCommand())) {
            System.out.println("go go go");
            view.unHighlight();
            userToMove = true;
            model.stop();
            model.startPosition();
            checkUserToMove();
        } else if ("back".equals(e.getActionCommand())) {
            view.unHighlight();
            userToMove = true;
            model.stop();
            model.unmakeMove();
            update(false);
        }
        else if ("fen".equals(e.getActionCommand())) {
            view.unHighlight();
            userToMove = true;
            model.stop();
            model.initializeFromFEN(view.getFEN());
            update(false);
        }
        else if ("go".equals(e.getActionCommand())) {
            if (!model.isSearching()) checkUserToMove();
        }
        else if ("opponent".equals(e.getActionCommand())) {
            if (!model.isSearching()) checkUserToMove();
        }
        else if ("time".equals(e.getActionCommand()))
            model.setMoveTime(view.getMoveTime());
        else if ("flip".equals(e.getActionCommand())) {
            flip = !flip;
            view.unHighlight();
            view.setFEN(view.getLastFEN(), flip, true);
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
            if (!acceptInput) return;
            Component c = view.getChessBoard().findComponentAt(e.getX(), e.getY());

            if (c instanceof SquareJPanel) return;
            originComponent = (SquareJPanel) c.getParent();

            Point parentLocation = c.getParent().getLocation();
            xAdjustment = parentLocation.x - e.getX();
            yAdjustment = parentLocation.y - e.getY();
            chessPiece = (PieceJLabel) c;
            chessPiece.setLocation(e.getX() + xAdjustment, e.getY() + yAdjustment);
            chessPiece.setSize(chessPiece.getWidth(), chessPiece.getHeight());
            view.addPiece(chessPiece, JLayeredPane.DRAG_LAYER);

    }

    @Override
    public void mouseReleased(MouseEvent e) {
            if (!acceptInput) return;
            // Only if inside board
            if (chessPiece == null) return;

            chessPiece.setVisible(false);
            Component c = view.getChessBoard().findComponentAt(e.getX(), e.getY());
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
            int from = flip ? originComponent.getIndex() : 63 - originComponent.getIndex();
            int to= flip ? parent.getIndex() : 63 - parent.getIndex();
            int move = model.getMoveFromIndices(from, to);
        System.out.println("Move:" + move);
            if(userToMove)
                if(model.userMove(move)) {
                    model.notifyObservers();
                    update(true);
                    checkUserToMove();
                }

    }

    public void start(){
        if(!started) {
            started = true;
            checkUserToMove();
        }
    }
    private void checkUserToMove() {
        userToMove = false;

        switch(view.getGameType()) {
            case 0:
                if (!model.isWhiteToMove()) userToMove = true;
                break;
            case 1:
                if (model.isWhiteToMove()) userToMove = true;
                break;
            case 2:
                if (!model.isWhiteToMove()) userToMove = true;
                break;
            case 3:
                if (model.isWhiteToMove()) userToMove = true;
                break;
            default:
                break;
        }
        //view.setAcceptInput(userToMove);
        update(!userToMove);

        if (!userToMove && (model.isEndOfGame() == 0)){
            int gameType=view.getGameType();

            if(gameType== 0 | gameType == 1 ){  //AI1
                model.engine1Move();
            }
            else if(gameType == 2 | gameType == 3){ //AI2
                model.engine2Move();
            }else if(gameType ==4){                 //AI1 is White vs AI2
                if(model.isWhiteToMove()) model.engine1Move();
                else model.engine2Move();
            }else{                                  //AI1 is Black vs AI2
                if(!model.isWhiteToMove()) model.engine1Move();
                else model.engine2Move();
            }
        }

        System.out.println("checkUserToMove... userToMove="+userToMove);
    }

    private void update(boolean thinking) {
        view.setFEN(model.getFEN(), flip, false);
        view.setFENText(model.getFEN());
        System.out.println("value=" + model.eval());
        switch (model.isEndOfGame()) {
            case Chessboard.WHITE_WIN :
                view.setMessageText("White win");
                break;
            case Chessboard.BLACK_WIN:
                view.setMessageText("Black win");
                break;
            case Chessboard.DRAW_BY_MATERIAL:
                view.setMessageText("Draw by Material");
                break;
            case Chessboard.DRAW_BY_FIFTYMOVE:
                view.setMessageText("Draw by Fifty Move Rule");
                break;
            case Chessboard.DRAW_BY_REP:
                view.setMessageText("Draw by Threefold Repetition");
                break;
            case Chessboard.DRAW_BY_STALEMATE:
                view.setMessageText("Draw by Stalemate");
                break;
            default:
                if (model.getMoveNumber() == 0) view.setMessageText("Chess1800");
                if (model.isWhiteToMove()) view.setMessageText("White move" + (thinking ? " - Thinking..." : ""));
                else view.setMessageText("Black move" + (thinking ? " - Thinking..." : ""));
                break;
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

    @Override
    public void bestMove(int bestMove) {
        System.out.println("bestMove... userToMove="+userToMove);
        if(userToMove) return;
        view.unHighlight();
    }
}
