package es.us.context4learning.backend;


import com.googlecode.objectify.ObjectifyService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CronServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(CronEndpoint.class.getName());
    private static final long serialVersionUID = 1L;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(NotificationCounter.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resetNotificationCounters();
    }

    public void resetNotificationCounters() {
        List<NotificationCounter> countersList = OfyService.ofy().load().type(NotificationCounter.class).list();
        for(NotificationCounter counter : countersList){
            counter.setMorning(0);
            counter.setEvening(0);
            counter.setNight(0);
        }
        OfyService.ofy().save().entities(countersList).now();
    }

}
