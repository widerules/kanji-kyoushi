package kanjikyoushi.kinc.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import kanjikyoushi.kinc.data.Question;
import kanjikyoushi.kinc.data.Sentence;

import org.apache.log4j.Logger;

public class DbHandler {

    private static DataSource datasource = null;
    private static final Logger logger = Logger.getLogger(DbHandler.class);

    public static synchronized void freeConnection(Connection connection) {

        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            logger.error("error closing a database connection", e);
        }

    }

    private static Connection getConnection() throws SQLException {

        return datasource.getConnection();

    }

    private static void init() throws NamingException {

        if (datasource == null) {
            InitialContext initialContext = new InitialContext();
            datasource =
                (DataSource) initialContext.lookup("java:/comp/env/jdbc/postgres");
            if (datasource == null) {
                String message = "Could not find DataSource.";
                logger.error(message);
            }
        }

    }

    public DbHandler() throws NamingException, SQLException {

        init();

        /*
         * test connection
         */
        Connection db = getConnection();
        freeConnection(db);

    }

    public void deleteAllSentences() throws SQLException {
        logger.debug("deleting all sentences");

        Connection db = null;
        try {
            db = getConnection();

            String sql = "DELETE FROM sentence";
            db.createStatement().execute(sql);

        } finally {
            freeConnection(db);
        }

    }

    public Sentence addSentence(Sentence sentence) throws SQLException {
        logger.debug("adding sentence; " + sentence);

        Connection db = null;
        try {
            db = getConnection();

            {
                String sql =
                    "INSERT INTO sentence (sentence_text, workbook, lesson) "
                        + "VALUES (?, ?, ?) RETURNING sentence_id";
                PreparedStatement ps = db.prepareStatement(sql);

                ps.setString(1, sentence.getText());
                ps.setInt(2, sentence.getWorkbook());
                ps.setInt(3, sentence.getLesson());

                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    sentence.setSentenceId(rs.getInt("sentence_id"));

                ps.close();
            }

            {
                String sql =
                    "INSERT INTO question (sentence_id, question, answer) "
                        + "VALUES (?, ?, ?) RETURNING question_id";
                PreparedStatement ps = db.prepareStatement(sql);

                for (Question q : sentence.getQuestions()) {
                    ps.setInt(1, sentence.getSentenceId());
                    ps.setString(2, q.getQuestion());
                    ps.setString(3, q.getAnswer());
                    ResultSet rs = ps.executeQuery();

                    while (rs.next())
                        q.setQuestionId(rs.getInt("question_id"));
                }

                ps.close();
            }
        } finally {
            freeConnection(db);
        }

        return sentence;
    }

    public Sentence getRandomSentence() throws SQLException {
        logger.debug("getting random sentence");

        Sentence sentence = null;

        Connection db = null;
        try {
            db = getConnection();

            String sql =
                "SELECT s.sentence_id, s.sentence_text, s.workbook, s.lesson, "
                    + "q.question_id, q.question, q.answer "
                    + "FROM sentence AS s JOIN question AS q "
                    + "ON s.sentence_id = q.sentence_id "
                    + "WHERE s.sentence_id = "
                    + "(SELECT sentence_id FROM sentence "
                    + "ORDER BY RANDOM() LIMIT 1)";
            PreparedStatement ps = db.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (sentence == null) {
                    sentence =
                        new Sentence(rs.getInt("sentence_id"),
                            rs.getString("sentence_text"));
                    sentence.setWorkbook(rs.getInt("workbook"));
                    sentence.setLesson(rs.getInt("lesson"));
                }

                sentence.addQuestion(new Question(rs.getInt("question_id"),
                    rs.getString("question"), rs.getString("answer")));
            }

        } finally {
            freeConnection(db);
        }

        return sentence;
    }

}
