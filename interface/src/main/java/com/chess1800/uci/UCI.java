package com.chess1800.uci;

import com.chess1800.core.board.Bitboard;
import com.chess1800.core.move.Move;
import com.chess1800.core.search.AbstractSearchInfo;
import com.chess1800.core.search.PVSSoft;
import com.chess1800.core.search.Search;
import com.chess1800.core.search.SearchObserver;

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
        search = new PVSSoft(new Bitboard());
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
                    System.out.println("id name " + NAME);
                    System.out.println("id author " + AUTHOR);
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

    }

    @Override
    public void info(AbstractSearchInfo info) {

    }
}