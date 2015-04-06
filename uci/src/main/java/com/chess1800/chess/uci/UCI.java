package com.chess1800.chess.uci;

import com.yonathan.chess.core.board.Bitboard;
import com.yonathan.chess.core.move.Move;
import com.yonathan.chess.core.search.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Yonathan on 29/03/2015.
 * UCI Interface
 */
public class UCI implements SearchObserver {
    static final String NAME = "Chess 1800";
    static final String AUTHOR = "Yonathan Maalo";

    Search search;


    public UCI (){
        search = new AI1Threaded(new Bitboard());
        search.setObserver(this);
    }

    public static void main(String[] args) {
        UCI uci = new UCI();
        uci.loop();
    }

    private void loop() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try{
            while (true) {
                String in = reader.readLine();
                String[] tokens = in.split(" ");
                int index = 0;
                String command = tokens[index++].toLowerCase();
                if ("uci".equals(command)) {
                    System.out.println("id name " + NAME +" USA");
                    System.out.println("id author " + AUTHOR);
                    System.out.println("uciok");
                } else if ("isready".equals(command)) {
                    while (search.isSearching()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                        }
                    }
                    System.out.println("readyok");
                } else if ("quit".equals(command)) System.exit(0);
                else if ("go".equals(command)) {
                    search.setDefaultParameters();
                    while (index < tokens.length) {
                        String arg = tokens[index++];
                        if ("wtime".equals(arg))
                            search.setWTime(Integer.parseInt(tokens[index++]));
                        else if ("btime".equals(arg))
                            search.setBTime(Integer.parseInt(tokens[index++]));
                        else if ("winc".equals(arg))
                            search.setWInc(Integer.parseInt(tokens[index++]));
                        else if ("binc".equals(arg))
                            search.setBInc(Integer.parseInt(tokens[index++]));
                        else if ("depth".equals(arg))
                            search.setDepth(Integer.parseInt(tokens[index++]));
                        else if ("movetime".equals(arg))
                            search.setMoveTime(Integer.parseInt(tokens[index++]));

                    }
                    search.go();
                } else if ("stop".equals(command))
                    search.stop();
                else if ("ucinewgame".equals(command))
                    search.getBoard().initialize();
                else if ("position".equals(command)) {
                    if (index < tokens.length) {
                        String arg = tokens[index++];
                        if ("startpos".equals(arg)) {
                            search.getBoard().initialize();
                        } else if ("fen".equals(arg)) {
                            // FEN string may have spaces
                            StringBuilder fenSb = new StringBuilder();
                            while (index < tokens.length) {
                                if ("moves".equals(tokens[index])) {
                                    break;
                                }
                                fenSb.append(tokens[index++]);
                                if (index < tokens.length) {
                                    fenSb.append(" ");
                                }
                            }
                            search.getBoard().initializeFromFEN(fenSb.toString());
                        }
                    }
                    if (index < tokens.length) {
                        String arg1 = tokens[index++];
                        if ("moves".equals(arg1)) {
                            while (index < tokens.length) {
                                int move = search.getBoard().getMoveFromString(tokens[index++], true);
                                search.getBoard().makeMove(move);
                            }
                        }
                    }


                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void bestMove(int bestMove) {
        String move = "bestmove ";
        move += Move.toString(bestMove, search.getBoard());
        System.out.println(move);
        System.out.flush();

    }

    @Override
    public void info(AbstractSearchInfo info) {
        System.out.print("info ");
        System.out.println(info.toString());

    }
}