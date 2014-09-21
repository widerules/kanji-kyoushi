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
 * Servlet implementation class AdminServlet
 */
public class AdminServlet extends HttpServlet {

    private static enum Action {
        load_data
    }

    private static final Logger logger = Logger.getLogger(AdminServlet.class);
    private static final String PARAM_ACTION = "action";
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        logger.debug("goGet()");

        if (request.getParameter(PARAM_ACTION) == null) {
        } else {
            try {
                switch (Action.valueOf(request.getParameter(PARAM_ACTION))) {
                    case load_data:
                        SentenceLogic logic = new SentenceLogic();
                        logic.loadSentences(getServletContext());
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }

        RequestDispatcher view = request.getRequestDispatcher("/admin.jsp");
        view.forward(request, response);

    }

}
