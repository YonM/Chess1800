package com.chess1800.chess.application.gui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


/**
 * Created by Yonathan on 18/03/2015.
 * Reworked interface based on Alberto Ruibal's Carballo. Source @ https://githucom/albertoruibal/carballo/ &
 */
public class Chess1800View extends JFrame{
    String timeString[] = {"1 second", "2 seconds", "5 seconds", "15 seconds", "30 seconds", "60 seconds", "No Time Limit"};
    int timeValues[] = {1000, 2000, 5000, 15000, 30000, 60000, 0};
    int timeDefaultIndex = 0;
    JPanel global, control;
    JComboBox comboOpponent, comboTime;
    String opponentString[] = {"AI1 Whites", "AI1 Blacks", "AI2 Whites", "AI2 Blacks", "AI 1 vs AI2", "AI2 vs A1"};
    int opponentDefaultIndex = 1;
    private char[] boardPieces;
    private JTextField fenField;
    private JButton button;
    private JLabel label, message;
    BoardJPanel boardPanel;
    public Chess1800View() throws HeadlessException {
        control = new JPanel();
        control.setLayout(new GridLayout(17,1));
        setSize(800, 800);
        setResizable(true);
        label = new JLabel("Game");
        control.add(label);
        button = new JButton("New Game");
        button.setActionCommand("restart");
        control.add(button);
        button = new JButton("Undo Move");
        button.setActionCommand("back");
        control.add(button);
        button = new JButton("Go");
        button.setActionCommand("go");
        control.add(button);
        label = new JLabel("Engine");
        control.add(label);
        comboOpponent = new JComboBox(opponentString);
        comboOpponent.setActionCommand("opponent");
        control.add(comboOpponent);
        comboTime = new JComboBox(timeString);
        comboTime.setActionCommand("time");
        control.add(comboTime);
        button = new JButton("Flip Board");
        button.setActionCommand("flip");
        control.add(button);
        fenField = new JTextField();
        fenField.setColumns(15);
        control.add(fenField);
        button = new JButton("Set FEN");
        button.setActionCommand("fen");
        control.add(button);
        message = new JLabel();
        control.add(message);
        boardPanel = new BoardJPanel();
        global = new JPanel();
        global.setBackground(Color.BLUE);
        global.setLayout(new BorderLayout());
        JPanel control2 = new JPanel();
        control2.setLayout(new FlowLayout());
        control2.add(control);
        global.add("East", control2);
        global.add("Center", boardPanel);
        add(global);
        comboOpponent.setSelectedIndex(opponentDefaultIndex);
        comboTime.setSelectedIndex(timeDefaultIndex);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }
    public void addActionListener(ActionListener al){
        for(Component c: control.getComponents()){
            if(c instanceof JButton){
                ((JButton)c).addActionListener(al);
            }
            else if(c instanceof JComboBox) {
                ((JComboBox) c).addActionListener(al);
            }
        }
    }
    public void addBoardListener(MouseMotionListener mml, MouseListener ml){
        boardPanel.addMouseListeners(mml, ml);
    }
    public void update(){
        invalidate();
        validate();
        repaint();
    }
    public void setBoardData(char[] pieces){
        boardPieces = pieces;
    }
    public void userMove(int i, int i1) {
    }
    public void setFENText(String fen){
        fenField.setText(fen);
    }
    public void setMessageText(String text){
        message.setText(text);
    }
    public int getGameType() {
        return comboOpponent.getSelectedIndex();
    }
    public void unHighlight() {
        boardPanel.unHighlight();
    }
    public void highlight(int fromIndex, int toIndex) {
        boardPanel.highlight(fromIndex, toIndex);
    }
    public String getFEN() {
        return fenField.getText();
    }
    public int getMoveTime() {
        return timeValues[comboTime.getSelectedIndex()];
    }
    public void setFEN(String fen, boolean flip, boolean redraw) {
        boardPanel.setFEN(fen, flip, redraw);
    }
    public String getLastFEN() {
        return boardPanel.getLastFEN();
    }
    public JPanel getChessBoard() {
        return boardPanel.getChessBoard();
    }
    public void addPiece(PieceJLabel chessPiece, Integer dragLayer) {
        boardPanel.addPiece(chessPiece, dragLayer);
    }
}