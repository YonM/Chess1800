package gui;

import board.Board;
import definitions.Definitions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by Yonathan on 29/01/2015.
 * The main class of the project.
 */
public class Chess1800 extends JFrame implements Definitions, ActionListener{


    private static final int LOWER_BUFFER = 51;
    private static final int SIDE_BUFFER = 5;

    private static final boolean PLAYER_IS_WHITE = true;
    private static final int NUM_MINUTES = 1;
    private static final boolean CAN_UNDO = true;

    private BoardModel model;
    private BoardView view;

    private Timer timer;

    private int fromx;
    private int fromy;
    private int tox;
    private int toy;

    private boolean player_turn;

    private int player_time;
    private int engine_time;

    public Chess1800() {
        super("Godot chess GUI v.0.1");
        setResizable(false);
        setSize(WIDTH * 8 + SIDE_BUFFER, WIDTH * 8 + LOWER_BUFFER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Board b = new Board();
        b.initializeFromFEN(START_FEN);
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
        i = new JMenuItem("Godot");
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
                fromx = e.getX();
                fromy = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                tox = e.getX();
                toy = e.getY();

                model.makeMove(fromx / WIDTH, tox / WIDTH, fromy / WIDTH, toy / WIDTH);

                if (fromx / WIDTH != tox / WIDTH || fromy / WIDTH != toy / WIDTH)
                    player_turn = false;

                //view.setLastMove(fromx / WIDTH, fromy / WIDTH, tox / WIDTH, toy / WIDTH);
            }
        });

        player_time = 60 * NUM_MINUTES * 1000;
        engine_time = 60 * NUM_MINUTES * 1000;

        player_turn = PLAYER_IS_WHITE;

        showTimesOnTitlebar();

        timer.start();
    }

    private void showTimesOnTitlebar() {

    }


    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
