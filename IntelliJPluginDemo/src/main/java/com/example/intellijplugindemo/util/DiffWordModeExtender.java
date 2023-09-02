package com.example.intellijplugindemo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiffWordModeExtender {

    private static final String WORD_BOUND = "\\W";

    /** Modified copy of diff_lineMode from original diff-match-patch documentation */
    public static List<diff_match_patch.Diff> diff_wordMode(diff_match_patch dmp, String text1, String text2){
        var wordsToCharsResult = diff_wordsToChars(text1, text2);
        var wordText1 = wordsToCharsResult.chars1;
        var wordText2 = wordsToCharsResult.chars2;
        var wordArray = wordsToCharsResult.wordArray;
        var diffs = dmp.diff_main(wordText1, wordText2, false);
        dmp.diff_charsToLines(diffs, wordArray);
        return diffs;
    }

    /** Modified copy of diff_linesToChars from original diff-match-patch */
    private static WordsToCharsResult diff_wordsToChars(String text1, String text2) {
        List<String> wordArray = new ArrayList<String>();
        Map<String, Integer> wordHash = new HashMap<String, Integer>();

        // "\x00" is a valid character, but various debuggers don't like it.
        // So we'll insert a junk entry to avoid generating a null character.
        wordArray.add("");

        // Allocate 2/3rds of the space for text1, the rest for text2.
        String chars1 = diff_wordsToCharsMunge(text1, wordArray, wordHash, 40000);
        String chars2 = diff_wordsToCharsMunge(text2, wordArray, wordHash, 65535);
        return new WordsToCharsResult(chars1, chars2, wordArray);
    }
    private static String diff_wordsToCharsMunge(String text, List<String> wordArray,
                                                 Map<String, Integer> wordHash, int maxWords) {
        int wordStart = 0;
        int wordEnd = -1;
        StringBuilder chars = new StringBuilder();
        // Walk the text, pulling out a substring for each word.
        // text.split('\n') would would temporarily double our memory footprint.
        // Modifying text would create many large strings to garbage collect.
        while (wordEnd < text.length() - 1) {
            var foundIndex = indexOfRegex(WORD_BOUND, text, wordStart);
            if (foundIndex < 0){
                wordEnd = text.length();
                var word = text.substring(wordStart, wordEnd);
                wordEnd = diff_addToken(text, wordArray, wordHash, maxWords, wordStart, wordEnd, word, chars);
                break;
            }
            else {
                wordEnd = foundIndex;
                if(wordStart != wordEnd){
                    var word = text.substring(wordStart, wordEnd);
                    wordEnd = diff_addToken(text, wordArray, wordHash, maxWords, wordStart, wordEnd, word, chars);
                }
                var punct = text.charAt(wordEnd);
                wordEnd = diff_addToken(text, wordArray, wordHash, maxWords, wordStart, wordEnd, String.valueOf(punct), chars);
                wordStart = wordEnd + 1;
            }
        }
        return chars.toString();
    }

    private static int indexOfRegex(String regex, CharSequence text, int startIndex){
        Pattern p = Pattern.compile(regex);  // insert your pattern here
        Matcher m = p.matcher(text.subSequence(startIndex, text.length()));
        if (m.find()) {
            return m.start() + startIndex;
        }
        return -1;
    }

    private static int diff_addToken(String text, List<String> wordArray,
                                     Map<String, Integer> wordHash, int maxWords,
                                     int wordStart, int wordEnd,
                                     String tokenText, StringBuilder chars) {
        if (wordHash.containsKey(tokenText)) {
            chars.append(String.valueOf((char) (int) wordHash.get(tokenText)));
        } else {
            if (wordArray.size() == maxWords) {
                // Bail out at 65535 because
                // String.valueOf((char) 65536).equals(String.valueOf(((char) 0)))
                tokenText = text.substring(wordStart);
                wordEnd = text.length();
            }
            wordArray.add(tokenText);
            wordHash.put(tokenText, wordArray.size() - 1);
            chars.append(String.valueOf((char) (wordArray.size() - 1)));
        }
        return wordEnd;
    }

    private static class WordsToCharsResult{
        private String chars1;
        private String chars2;
        private List<String> wordArray;

        public WordsToCharsResult(String chars1, String chars2, List<String> wordArray) {
            this.chars1 = chars1;
            this.chars2 = chars2;
            this.wordArray = wordArray;
        }

        public String getChars1() {
            return chars1;
        }

        public String getChars2() {
            return chars2;
        }

        public List<String> getWordArray() {
            return wordArray;
        }
    }
}
