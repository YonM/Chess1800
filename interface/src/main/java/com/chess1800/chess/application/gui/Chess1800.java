package com.chess1800.chess.application.gui;

import com.chess1800.chess.board.Bitboard;
import com.chess1800.chess.board.Chessboard;

import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by Yonathan on 29/01/2015.
 * The main class of the project.
 * Based on Ulysse Carion's Godot. Source @ https://github.com/ucarion
 */
public class Chess1800 extends JFrame implements ActionListener{

    private static final int WIDTH = 50;
    private static final int LOWER_BUFFER = 51;
    private static final int SIDE_BUFFER = 5;

    private static final boolean PLAYER_IS_WHITE = true;
    private static final int NUM_MINUTES = 10;
    private static final boolean CAN_UNDO = true;

    private BoardModel model;
    private BoardView view;

    private Timer timer;

    private int fromX;
    private int fromY;
    private int toX;
    private int toY;

    private boolean player_turn;

    private int player_time;
    private int engine_time;

/*    private enum STATE{
        MENU,
        GAME
    };
    private STATE State = STATE.MENU;*/

    public Chess1800() {
        super("Chess 1800");
        setResizable(false);
        setSize(WIDTH * 8 + SIDE_BUFFER, WIDTH * 8 + LOWER_BUFFER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Bitboard b = new Bitboard();
        b.initializeFromFEN(Chessboard.START_FEN);
        view = new BoardView();
        model = new BoardModel(b, view);
        model.addObserver(view);
        view.update(model, null);
        view.repaint();
        Container c = getContentPane();
        c.add(view);

        timer = new Timer(100, this);

        JMenuBar mBar = new JMenuBar();
        JMenu m;
        JMenuItem i;

        m = new JMenu("Game");
        i = new JMenuItem("Undo move");
        i.addActionListener(this);
        m.add(i);
        mBar.add(m);

        m = new JMenu("Engine");
        i = new JMenuItem("Chess 1800");
        i.addActionListener(this);
        m.add(i);
        mBar.add(m);

        setJMenuBar(mBar);

        c.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                fromX = e.getX();
                fromY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                toX = e.getX();
                toY = e.getY();

                model.makeMove(fromX / WIDTH, toX / WIDTH, fromY / WIDTH, toY / WIDTH);

                if (fromX / WIDTH != toX / WIDTH || fromY / WIDTH != toY / WIDTH)
                    player_turn = false;
            }
        });

        player_time = 60 * NUM_MINUTES * 1000;
        engine_time = 60 * NUM_MINUTES * 1000;

        player_turn = PLAYER_IS_WHITE;

        showTimesOnTitleBar();

        timer.start();
    }

    public static void main(String[] args) {
        Chess1800 ch = new Chess1800();
        ch.setVisible(true);
    }

    private void showTimesOnTitleBar() {
        if (PLAYER_IS_WHITE)
            setTitle("White: " + (player_time / 1000.0) + " -- Black: "
                    + (engine_time / 1000.0));
        else
            setTitle("White: " + (engine_time / 1000.0) + " -- Black: "
                    + (player_time / 1000.0));

    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            if (player_time < 0) {
                JOptionPane.showMessageDialog(null, "Player lost on time.");
                System.exit(0);
            }
            else if (engine_time < 0) {
                JOptionPane.showMessageDialog(null, "Engine lost on time.");
                System.exit(0);
            }

            showTimesOnTitleBar();

            if (player_turn) {
                player_time -= 100;
            }
            else {
                timer.stop();
                long start = System.currentTimeMillis();
                model.makeEngineMove(engine_time);
                long stop = System.currentTimeMillis();
                if(stop-start < 10000) engine_time+=10000 -(stop-start);
                engine_time -= (stop - start);
                player_turn = true;
                timer.start();
            }
            if (model.whiteWins()) {
                if (PLAYER_IS_WHITE) {
                    JOptionPane.showMessageDialog(null, "Player wins by checkmate.");
                    System.exit(0);
                }
                else {
                    JOptionPane.showMessageDialog(null, "Engine wins by checkmate.");
                    System.exit(0);
                }
            }
            else if (model.blackWins()) {
                if (PLAYER_IS_WHITE) {
                    JOptionPane.showMessageDialog(null, "Engine wins by checkmate.");
                    System.exit(0);
                }
                else {
                    JOptionPane.showMessageDialog(null, "Player wins by checkmate.");
                    System.exit(0);
                }
            }
            else if (model.isDraw()) {
                JOptionPane.showMessageDialog(null, "Draw");
                System.exit(0);
            }
        }
        else if (e.getActionCommand().equals("Undo move") && CAN_UNDO)
            model.unmakeMove();

    }
}
