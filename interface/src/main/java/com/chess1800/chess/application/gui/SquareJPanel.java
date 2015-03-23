package com.chess1800.chess.application.gui;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Created by Yonathan on 18/03/2015.
 */
public class SquareJPanel extends JPanel {

    private static BufferedImage imgWhite;
    private static BufferedImage imgBlack;

    static {
        imgWhite = new BufferedImage(75, 75, BufferedImage.TYPE_4BYTE_ABGR);
        imgBlack = new BufferedImage(75, 75, BufferedImage.TYPE_4BYTE_ABGR);
        ImageIcon icon = new ImageIcon(SquareJPanel.class.getClass().getResource("/blue.png"));
        imgWhite.getGraphics().drawImage(icon.getImage(), -75, 0, null);
        imgBlack.getGraphics().drawImage(icon.getImage(), 0, 0, null);

    }

    private int index;
    private boolean highlighted;

    private boolean color;

    public SquareJPanel(int index) {
        super(new BorderLayout());
        this.index = index;
        this.highlighted = false;
        setVisible(true);
        Dimension size = new Dimension(75, 75);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);

        int row = (index / 8) % 2;
        if (row == 0) {
            color = index % 2 == 0;
        } else {
            color = index % 2 != 0;
        }
    }

    public void paintComponent(Graphics g) {
        g.drawImage(color ? imgWhite : imgBlack, 0, 0, 75, 75, null);
        if (highlighted) {
            g.setColor(Color.yellow);
            //g.drawRect(0, 0, 74, 74);
            g.drawRect(1, 1, 72, 72);
            g.drawRect(2, 2, 70, 70);
            g.drawRect(3, 3, 68, 68);
            g.drawRect(4, 4, 66, 66);
        }
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public int getIndex() {
        return index;
    }
}
