import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;


/**
 * A visual representation for the number of times various words occur in a given document.
 *
 * In a word cloud, the most common English words are usually ignored (e.g. the, a, of), 
 * since they do not give any useful information about the topic being visualized.
 *
 * @author Robert C Duvall
 * @author Christopher La Pilla
 */
public class WordCloud {
    // some basic defaults
    private static final int DEFAULT_NUM_GROUPS = 20;
    private static final int DEFAULT_MIN_FONT = 6;
    private static final int DEFAULT_INCREMENT = 4;
    private static final String DEFAULT_IGNORE_FILE = "common.txt";
    // regular expression that represents one or more digits or punctuation
    private static final String PUNCTUATION = "[\\d\\p{Punct}]+";

    // set of common words to ignore when displaying word cloud
    private Set<String> myCommonWords;
    // words and the number of times each appears in the file
    private List<Entry<String, Integer>> myTagWords;


    /**
     * Constructs an empty WordCloud.
     */
    public WordCloud (Scanner ignoreWords) {
        myTagWords = new ArrayList<>();
        myCommonWords = new HashSet<>();
        // create list of words that should not be included in final word counts
        while (ignoreWords.hasNext()) {
            myCommonWords.add(sanitize(ignoreWords.next()));
        }
    }

    /**
     * Create a word cloud from the given input.
     */
    public void makeCloud (Scanner input, int numWordsToKeep, int groupSize) {
        countWords(input);
        topWords(numWordsToKeep, groupSize);
    }

    /**
     * Convert each word to appropriately sized font based on its frequency.
     */
    @Override
    public String toString () {
        StringBuilder result = new StringBuilder();
        result.append(HTMLPage.startPage(DEFAULT_NUM_GROUPS, DEFAULT_MIN_FONT, DEFAULT_INCREMENT));
        for (Entry<String, Integer> word : myTagWords) {
            result.append(HTMLPage.formatWord(word.getKey(), word.getValue()));
        }
        result.append(HTMLPage.endPage());
        return result.toString();
    }

    // Reads given text file and counts non-common words it contains.
    // Each word read is converted to lower case with leading and trailing punctuation removed
    // before it is counted.
    private void countWords (Scanner input) {
        Map<String, Integer> wordCounts = new HashMap<>();
        while (input.hasNext()) {
            String word = sanitize(input.next());
            if (isTaggable(word)) {
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
        }
        myTagWords.addAll(wordCounts.entrySet());
    }

    // Sorts words alphabetically, keeping only those that appeared most often.
    private void topWords (int numWordsToKeep, int groupSize) {
        // sort from most frequent to least
        myTagWords.sort(new Comparator<Entry<String, Integer>>() {
            @Override
            public int compare (Entry<String, Integer> a, Entry<String, Integer> b) {
                return b.getValue().compareTo(a.getValue());
            }
        });
        // keep only the top ones
        myTagWords.subList(numWordsToKeep, myTagWords.size()).clear();
        // convert frequencies into groups
        for (int k = 0; k < myTagWords.size(); k++) {
            Entry<String, Integer> word = myTagWords.get(k);
            // Entry is immutable, so create a new one
            myTagWords.set(k,
                           new SimpleEntry<>(word.getKey(), word.getValue() / groupSize));
        }
        // sort alphabetically
        myTagWords.sort(new Comparator<Entry<String, Integer>>() {
            @Override
            public int compare (Entry<String, Integer> a, Entry<String, Integer> b) {
                return a.getKey().compareTo(b.getKey());
            }
        });
    }

    // Return true if the given word should be tagged
    private boolean isTaggable (String word) {
        return word.length() > 0 && !myCommonWords.contains(word);
    }

    // Return string with leading and trailing punctuation removed from the given word
    private String sanitize (String word) {
        return word.replaceFirst("^" + PUNCTUATION, "")
                   .replaceFirst(PUNCTUATION + "$", "")
                   .toLowerCase();
    }


    public static void main (String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: #words file");
        }
        else {
            WordCloud cloud = new WordCloud(new Scanner(WordCloud.class.getResourceAsStream(DEFAULT_IGNORE_FILE)));
            cloud.makeCloud(new Scanner(WordCloud.class.getResourceAsStream(args[1])), 
                            Integer.parseInt(args[0]),
                            DEFAULT_NUM_GROUPS);
            System.out.println(cloud);
        }
    }
}
