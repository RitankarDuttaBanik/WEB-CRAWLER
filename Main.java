import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.ParserDelegator;

public class Main {

    
    private static final String URL_PATTERN_REGEX =
        "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_PATTERN_REGEX);

    // Print links found at current depth level
    private static void printOutput(int currentLevel, Set<String> links) {
        System.out.println("============== START: DEPTH = " + currentLevel + " ===============");
        int index = 1;
        for (String link : links) {
            System.out.println(index++ + " .> " + link);
        }
        System.out.println("============== END: DEPTH = " + currentLevel + " =================");
    }

    // Fetches webpage content as a string
    private static String getWebPageContentAsString(String urlLink) {
        try {
            URL url = new URL(urlLink);
            URLConnection connection = url.openConnection();
            try (InputStream is = connection.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            System.err.println("Error fetching content from " + urlLink + ": " + e.getMessage());
            return null;
        }
    }

    // Extracts all href links from HTML content
    private static List<String> getAllLinksInString(String content) throws IOException {
        List<String> links = new ArrayList<>();
        Reader reader = new StringReader(content);
        HTMLEditorKit.Parser parser = new ParserDelegator();

        parser.parse(reader, new HTMLEditorKit.ParserCallback() {
            public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
                if (tag == HTML.Tag.A) {
                    Object href = attributes.getAttribute(HTML.Attribute.HREF);
                    if (href != null) {
                        String link = href.toString();
                        if (link.startsWith("http://") || link.startsWith("https://") || link.startsWith("www")) {
                            links.add(link);
                        }
                    }
                }
            }
        }, true);
        return links;
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("ENTER THE STARTING URL: ");
        String inputURL = sc.next();

        if (!URL_PATTERN.matcher(inputURL).matches()) {
            System.err.printf("'%s' is not a valid URL.\n", inputURL);
            return;
        }

        System.out.print("ENTER THE MAXIMUM DEPTH YOU WANT TO CRAWL: ");
        int maxLevel = sc.nextInt();
        if (maxLevel <= 0) {
            System.err.println("MAX LEVEL SHOULD BE GREATER THAN 0");
            return;
        }

        Set<String> currentLevelURLs = new HashSet<>();
        Set<String> nextLevelURLs = new HashSet<>();

        currentLevelURLs.add(inputURL);

        for (int currentLevel = 1; currentLevel <= maxLevel; currentLevel++) {
            for (String url : currentLevelURLs) {
                String content = getWebPageContentAsString(url);
                if (content == null) continue;

                List<String> links = getAllLinksInString(content);
                for (String link : links) {
                    if (!currentLevelURLs.contains(link)) {
                        nextLevelURLs.add(link);
                    }
                }
            }

            printOutput(currentLevel, nextLevelURLs);
            currentLevelURLs.clear();
            currentLevelURLs.addAll(nextLevelURLs);
            nextLevelURLs.clear();
        }

        System.out.println("------------ END OF PROGRAM ------------");
        sc.close();
    }
}
