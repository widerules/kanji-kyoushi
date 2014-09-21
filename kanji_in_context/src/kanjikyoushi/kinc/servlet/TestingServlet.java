package kanjikyoushi.kinc.servlet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kanjikyoushi.kinc.data.Sentence;
import kanjikyoushi.kinc.data.SentenceList;
import kanjikyoushi.kinc.logic.DisplayLogic;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class TestingServlet
 */
public class TestingServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(TestingServlet.class);
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        logger.info("Testing Servlet, do get");

        // String dataFile = "test_data.txt";
        String dataFile = "data_wb1_01.txt";

        BufferedReader in = new BufferedReader(new FileReader(
                getServletContext().getRealPath(dataFile)));

        SentenceList sentenceList = new SentenceList();
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            if (line.trim().isEmpty())
                continue;

            Sentence displaySentence = DisplayLogic.convertSentence(line);

            if (displaySentence != null)
                sentenceList.addSentence(displaySentence);
        }

        request.setAttribute("sentence_list", sentenceList);

        RequestDispatcher view = request.getRequestDispatcher("/testing.jsp");
        view.forward(request, response);

    }
}
