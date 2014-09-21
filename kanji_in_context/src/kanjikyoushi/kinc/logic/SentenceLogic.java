package kanjikyoushi.kinc.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.servlet.ServletContext;

import kanjikyoushi.kinc.data.Sentence;
import kanjikyoushi.kinc.db.DbHandler;

import org.apache.log4j.Logger;

public class SentenceLogic {

    private static final Logger logger = Logger.getLogger(SentenceLogic.class);
    private DbHandler db;

    public SentenceLogic() throws NamingException, SQLException {
        db = new DbHandler();
    }

    public Sentence getSentence() {
        Sentence sentence = null;

        try {
            sentence = db.getRandomSentence();
        } catch (SQLException e) {
            logger.error("", e);
        }

        return sentence;
    }

    private static final String dataFileFormat = "data_wb(\\d+)_(\\d+).txt";

    public void loadSentences(ServletContext servletContext)
        throws IOException, NamingException, SQLException {
        db.deleteAllSentences();

        // String dataFile = "test_data.txt";
        // String dataFile = "data/data_wb1_01.txt";

        File dataDirectory = new File(servletContext.getRealPath("data"));
        File[] dataFiles = dataDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(dataFileFormat);
            }
        });

        Pattern p = Pattern.compile(dataFileFormat);

        for (File dataFile : dataFiles) {
            int workbook;
            int lesson;

            logger.debug("reading " + dataFile.getName());

            Matcher m = p.matcher(dataFile.getName());
            if (m.matches()) {

                workbook = Integer.parseInt(m.group(1));
                lesson = Integer.parseInt(m.group(2));

            } else {

                logger.warn("file '" + dataFile.getName()
                    + "' doesn't match data file pattern, skipping");
                continue;

            }

            BufferedReader in = new BufferedReader(new FileReader(dataFile));

            for (String line = in.readLine(); line != null; line =
                in.readLine()) {
                if (line.trim().isEmpty())
                    continue;

                Sentence displaySentence = Sentence.createSentence(line);
                displaySentence.setWorkbook(workbook);
                displaySentence.setLesson(lesson);
                displaySentence = db.addSentence(displaySentence);
                logger.debug(displaySentence);

            }
        }

    }
}
