
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TSVFormatter {

    private static final String UTF_16_LE = "UTF-16LE";
    private static final String UTF_8 = "UTF-8";
    private static final String DELIMITER = "\\t";
    BufferedReader reader = null;
    BufferedWriter writer = null;

    public static void main(String[] args) {
        String dir = System.getProperty("user.dir");

        TSVFormatter formatter = new TSVFormatter();
        String inputFile = dir + File.separator + "data.tsv";
        String outputFile = dir + File.separator + "dataOut.tsv";
        File file = new File(inputFile);
        if (!file.exists()) {
            System.out.println("Input file do not exist");
            System.exit(1);
        }
        int result = formatter.processTSVFile(inputFile, outputFile);

        if (result == 0) {
            System.out.println("Formatting Success");
        } else {
            System.out.println("Formatting Failure");
        }

    }

    /**
     * Cleans and formats TSV file.
     * 
     * @param inputFile
     *            File to be formatted.
     * @param outputFile
     *            Formatted file.
     * 
     * @return 0 - success. 1 - failure.
     */
    public int processTSVFile(String inputFile, String outputFile) {

        InputStream stream = null;
        try {

            stream = new FileInputStream(inputFile);
            reader = new BufferedReader(new InputStreamReader(stream, Charset.forName(UTF_16_LE)));
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), UTF_8));

            processLines();

            return 0;

        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        } finally {
            try {
                stream.close();
                writer.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

    }

    /**
     * Process all lines of input TSV file and formats them.
     * 
     * @throws IOException
     */
    protected void processLines() throws IOException {

        String currentLine = null;
        String escapeNewLine = "\'\\n\'";
        boolean previousLineEndsWithTab = false;
        List<String> tokens = new ArrayList<String>();
        while ((currentLine = reader.readLine()) != null) {

            int lastPositionOfDelimiter = currentLine.lastIndexOf('\t') + 1;
            int currentLineLength = currentLine.length();

            // size will be 0 at processing the first line or if previous line is completely processed.
            if (tokens.size() == 0) {
                tokens.addAll(Arrays.asList(currentLine.split(DELIMITER)));
            } else { // previous line is not completely processed.

                String[] currentLineTokens = currentLine.split(DELIMITER);
                if (currentLineTokens.length > 0) {
                    if (previousLineEndsWithTab) {
                        // escape new line in first token of current line.
                        tokens.add(escapeNewLine + currentLineTokens[0]);
                    } else {
                        // escape new line in last token of previous line.
                        tokens.set(tokens.size() - 1,
                                (tokens.get(tokens.size() - 1) + escapeNewLine + currentLineTokens[0]));
                    }
                    for (int i = 1; i < currentLineTokens.length; i++) {
                        tokens.add(currentLineTokens[i]);
                    }
                }
            }

            if (tokens.size() == 5) {
                writeLine(tokens);
                // Clear the list if current line contains 5 tokens.
                tokens.subList(0, 5).clear();
            }

            previousLineEndsWithTab = (lastPositionOfDelimiter == currentLineLength) ? true : false;
        }

    }

    /**
     * Appends formatted line to Buffered Writer
     * 
     * @param line
     *            List of tokens in a single line.
     * @throws IOException
     */
    protected void writeLine(List<String> line) throws IOException {

        for (String token : line) {
            writer.append(token);
            writer.append('\t');
        }
        writer.newLine();

    }
}
