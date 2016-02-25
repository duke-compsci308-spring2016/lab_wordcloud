import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
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
    private static final int DEFAULT_MIN_FONT = 12;
    private static final int DEFAULT_INCREMENT = 6;
    private static final String DEFAULT_IGNORE_FILE = "common.txt";
    // key regular expressions
    private static final String PUNCTUATION = "[\\d\\p{Punct}]+";
    private static final String END_OF_FILE = "\\z";
    private static final String WHITESPACE = "\\s+";

    // how to decide what words to ignore when displaying word cloud
    private Predicate<String> mySelector;
    // words and the number of times each appears in the file
    private List<Entry<String, Long>> myTagWords;


    /**
     * Constructs an empty WordCloud.
     */
    public WordCloud (Predicate<String> select) {
        // this value should never be null
        myTagWords = new ArrayList<>();
        mySelector = select;
    }

    /**
     * Create a word cloud from the given input.
     */
    public WordCloud makeCloud (Scanner input, int numWordsToKeep, int groupSize) {
        return countWords(input).topWords(numWordsToKeep, groupSize);
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
    private WordCloud countWords (Scanner input) {
        myTagWords.addAll(readWords(input, WordCloud::sanitize, mySelector).stream()
                          // create a map from word to word frequency
                          .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                          // stream the set of word to word frequency mappings
                          .entrySet());
        return this;
    }

    // Sorts words alphabetically, keeping only those that appeared most often.
    private WordCloud topWords (int numWordsToKeep, int groupSize) {
        myTagWords = myTagWords.stream()
                               // sort from most frequent to least
                               // TODO: add secondary comparison alphabetically based on word
                               .sorted(Comparator.comparing(Entry<String, Long>::getValue).reversed().thenComparing(Entry<String, Long>::getKey))
                               // keep only the top ones
                               .limit(numWordsToKeep)
                               // convert frequencies into groups (Entry is immutable, so create a new one)
                               .map(w -> new SimpleEntry<String, Long>(w.getKey(), w.getValue() / groupSize))
                               // sort alphabetically
                               .sorted(Comparator.comparing(Entry<String, Long>::getKey))
                               .collect(Collectors.toList());
        return this;
    }

    // Returns a function that returns true if the given word should be tagged
    private static Predicate<String> isTaggable (Scanner ignoreWords) {
        // set of common words to ignore when displaying word cloud
        final Set<String> commonWords = new HashSet<>(readWords(ignoreWords,
                                                                WordCloud::sanitize,
                                                                x -> true));
        return (w -> w.length() > 0 && !commonWords.contains(w));
    }

    // Remove the leading and trailing punctuation from the given word
    private static String sanitize (String word) {
        return word.replaceFirst("^" + PUNCTUATION, "")
                   .replaceFirst(PUNCTUATION + "$", "")
                   .toLowerCase();
    }

    // Read given input and returns its entire contents as a list of words
    private static List<String> readWords (Scanner input,
                                           UnaryOperator<String> xform,
                                           Predicate<String> select) {
        List<String> contents = Arrays.stream(input.useDelimiter(END_OF_FILE).next().split(WHITESPACE))
                                      // TODO: add map and filter calls using parameters
        							  .map(xform)
        							  .filter(select)
                                      .collect(Collectors.toList());
        input.close();
        return contents;
    }


    public static void main (String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: #words file");
        }
        else {
            WordCloud cloud = new WordCloud(isTaggable(new Scanner(WordCloud.class.getResourceAsStream(DEFAULT_IGNORE_FILE))))
                                   .makeCloud(new Scanner(WordCloud.class.getResourceAsStream(args[1])),
                                              Integer.parseInt(args[0]),
                                              DEFAULT_NUM_GROUPS);
            System.out.println(cloud);
        }
    }
}
