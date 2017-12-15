package es.us.context4learning.backend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

/**
 * Objectify service wrapper so we can statically register our persistence classes
 * More on Objectify here : https://code.google.com/p/objectify-appengine/
 */
public class OfyService {

    static {
        factory().register(User.class);
        factory().register(Device.class);
        factory().register(Context.class);
        factory().register(TimeRestriction.class);
        factory().register(LocationRestriction.class);
        factory().register(LimitNotification.class);
        factory().register(NotificationCounter.class);
        factory().register(Notification.class);
        factory().register(AuditEvent.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
