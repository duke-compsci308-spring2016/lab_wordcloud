import java.util.Map.Entry;


/**
 * A simple module to encapsulate using HTML tags.
 *
 * Note, makes use of CSS styling in an effort ease changing the style later.
 *
 * @author Robert C. Duvall
 */
public class HTMLPage {
    /**
     * @return tags to be included at top of HTML page
     */
    public static String startPage (int numSizes, int startSize, int increment) {
        StringBuilder style = new StringBuilder();
        for (int k = 0; k < numSizes; k++) {
            style.append("  .size-" + k + " { font-size: " + (startSize + increment * k) + "px; }");
        }
        return "<html><head><style>" + style + "</style></head><body><p>\n";
    }

    /**
     * @return tags to be included at bottom of HTML page
     */
    public static String endPage () {
        return "</p></body></html>";
    }

    /**
     * @return tags to display a single styled word
     */
    public static String formatWord (String word, long cssSize) {
        return "  <span class=\"size-" + cssSize + "\">" + word + "</span>\n";
    }

    /**
     * @return tags to display a single styled word
     */
    public static String formatWord (Entry<String, Long> wordCount) {
        return formatWord(wordCount.getKey(), wordCount.getValue());
    }
}
