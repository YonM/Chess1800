package fen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Yonathan on 03/12/2014.
 * This class is to check if the user inputted FEN is valid. This code was provided by Jaco Van Niekerk
 *
 * @ http://vicki-chess.blogspot.co.uk/2013_03_01_archive.html
 * @author Jaco Van Niekerk
 */
public class FENValidator {
    private final static boolean VERBOSE = false;
    /*
    * Method checks if the string provided is a valid FEN, no legality checking is made.
    * Just that the string contains the correct piece types, two kings and each rank has 8 squares.*/
    public static boolean isValidFEN(String fen) {
        Pattern pattern = Pattern.compile("((([prnbqkPRNBQK12345678]*/){7})([prnbqkPRNBQK12345678]*)) (w|b) ((K?Q?k?q?)|\\-) (([abcdefgh][36])|\\-)( ((\\d*) (\\d*))||\n)");
        Matcher matcher = pattern.matcher(fen);
        if (!matcher.matches()) {
            if(VERBOSE) System.out.println("matcher fail");
            return false;
        }
        String[] ranks = matcher.group(2).split("/");

        for (String rank : ranks) {
            if (!verifyRank(rank)) {
                if(VERBOSE) System.out.println("rank fail: " +rank);
                return false;
            }
        }
        if (!verifyRank(matcher.group(4))) {
            return false;
        }

        // Check two kings.
        if (!matcher.group(1).contains("k") || !matcher.group(1).contains("K")) {
            return false;
        }
        return true;
    }

    /*
    * This method returns true if the rank provided contains 8 squares (either empty or occupied).
    * Assumes string contains valid pieces and the digits 1-8.
    * */
    private static boolean verifyRank(String rank) {
        int count = 0;
        for (int i = 0; i < rank.length(); i++) {
            if (rank.charAt(i) >= '1' && rank.charAt(i) <= '8') {
                count += (rank.charAt(i) - '0');
            } else {
                count++;
            }
        }
        return count == 8;
    }
}
