package kanjikyoushi.kinc.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kanjikyoushi.kinc.logic.SentenceLogic;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class QuizServlet
 */
public class QuizServlet extends HttpServlet {

    private static enum Action {
        get_sentence
    }

    private static final Logger logger = Logger.getLogger(QuizServlet.class);
    private static final String PARAM_ACTION = "action";
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        logger.debug("doGet()");

        RequestDispatcher view = request.getRequestDispatcher("/quiz.jsp");
        view.forward(request, response);

    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        logger.debug("doPost()");

        try {
            switch (Action.valueOf(request.getParameter(PARAM_ACTION))) {
                case get_sentence:
                    SentenceLogic logic = new SentenceLogic();

                    request.setAttribute("sentence", logic.getSentence());

                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        RequestDispatcher view = request
                .getRequestDispatcher("/template/sentence.jsp");
        view.forward(request, response);

    }

}
