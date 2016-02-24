import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
    // key regular expressions
    private static final String PUNCTUATION = "[\\d\\p{Punct}]+";
    private static final String END_OF_FILE = "\\z";
    private static final String WHITESPACE = "\\s";

    // set of common words to ignore when displaying word cloud
    private Set<String> myCommonWords;
    // words and the number of times each appears in the file
    private List<Entry<String, Long>> myTagWords;


    /**
     * Constructs an empty WordCloud.
     */
    public WordCloud (Scanner ignoreWords) {
        // this value should never be null
        myTagWords = new ArrayList<>();
        // create list of words that should not be included in final word counts
        myCommonWords = new HashSet<>(readWords(ignoreWords));
    }

    /**
     * Create a word cloud from the given input.
     */
    public void makeCloud (Scanner input, int numWordsToKeep, int groupSize) {
        myTagWords = topWords(countWords(input), numWordsToKeep, groupSize);
    }

    /**
     * Convert each word to appropriately sized font based on its frequency.
     */
    @Override
    public String toString () {
        return Stream.of(HTMLPage.startPage(DEFAULT_NUM_GROUPS, DEFAULT_MIN_FONT, DEFAULT_INCREMENT),
                         myTagWords.stream()
                                   .map(HTMLPage::formatWord)
                                   .collect(Collectors.joining(" ")),
                         HTMLPage.endPage())
                     .collect(Collectors.joining("\n"));
    }

    // Reads given text file and counts non-common words it contains.
    // Each word read is converted to lower case with leading and trailing punctuation removed
    // before it is counted.
    private List<Entry<String, Long>> countWords (Scanner input) {
        final Map<String, Long> wordCounts = new HashMap<>();
        readWords(input).forEach(w -> {
            if (isTaggable(w)) {
                wordCounts.put(w, wordCounts.getOrDefault(w, 0L) + 1);
            }
        });
        return new ArrayList<>(wordCounts.entrySet());
    }

    // Sorts words alphabetically, keeping only those that appeared most often.
    private List<Entry<String, Long>> topWords (List<Entry<String, Long>> tagWords,
                                                   int numWordsToKeep,
                                                   int groupSize) {
        // sort from most frequent to least
        tagWords.sort(Comparator.comparing(Entry<String, Long>::getValue).reversed());
        // keep only the top ones
        tagWords.subList(numWordsToKeep, tagWords.size()).clear();
        // convert frequencies into groups (Entry is immutable, so create a new one)
        tagWords = tagWords.stream()
                           .map(w -> new SimpleEntry<>(w.getKey(), w.getValue() / groupSize))
                           .collect(Collectors.toList());
        // sort alphabetically
        tagWords.sort(Comparator.comparing(Entry<String, Long>::getKey));
        return tagWords;
    }

    // Return true if the given word should be tagged
    private boolean isTaggable (String word) {
        return word.length() > 0 && !myCommonWords.contains(word);
    }

    // Remove the leading and trailing punctuation from the given word
    private static String sanitize (String word) {
        return word.replaceFirst("^" + PUNCTUATION, "")
                   .replaceFirst(PUNCTUATION + "$", "")
                   .toLowerCase();
    }

    // Read given input and returns its entire contents as a list of words
    private List<String> readWords (Scanner input) {
        return Arrays.stream(input.useDelimiter(END_OF_FILE).next().split(WHITESPACE))
                     .map(WordCloud::sanitize)
                     .collect(Collectors.toList());
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
