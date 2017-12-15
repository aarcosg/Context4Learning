/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package es.us.context4learning.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiNamespace;
import com.googlecode.objectify.ObjectifyService;

import java.util.List;
import java.util.logging.Logger;

import static es.us.context4learning.backend.OfyService.ofy;

/**
 * An endpoint to send messages to devices registered with the backend
 * <p/>
 * For more information, see
 * https://developers.google.com/appengine/docs/java/endpoints/
 * <p/>
 * NOTE: This endpoint does not use any form of authorization or
 * authentication! If this app is deployed, anyone can access this endpoint! If
 * you'd like to add authentication, take a look at the documentation.
 */
@Api(
        name = "cron",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.context4learning.us.es",
                ownerName = "backend.context4learning.us.es",
                packagePath = ""
        )
)
public class CronEndpoint {
    private static final Logger logger = Logger.getLogger(CronEndpoint.class.getName());

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(NotificationCounter.class);
    }
    public void resetNotificationCounters(){
        List<NotificationCounter> countersList = ofy().load().type(NotificationCounter.class).list();
        for(NotificationCounter counter : countersList){
            counter.setMorning(0);
            counter.setEvening(0);
            counter.setNight(0);
        }
        ofy().save().entities(countersList).now();
    }
}
