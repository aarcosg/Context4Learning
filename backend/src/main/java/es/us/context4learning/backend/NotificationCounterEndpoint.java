package es.us.context4learning.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "notificationCounterApi",
        version = "v1",
        resource = "notificationCounter",
        namespace = @ApiNamespace(
                ownerDomain = "backend.context4learning.us.es",
                ownerName = "backend.context4learning.us.es",
                packagePath = ""
        )
)
public class NotificationCounterEndpoint {

    private static final Logger logger = Logger.getLogger(NotificationCounterEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;
    public static final int DAY_MORNING = 1;
    public static final int DAY_EVENING = 2;
    public static final int DAY_NIGHT = 3;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(NotificationCounter.class);
        ObjectifyService.register(User.class);
    }

    /**
     * Returns the {@link NotificationCounter} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code NotificationCounter} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "notificationCounter/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public NotificationCounter get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting NotificationCounter with ID: " + id);
        NotificationCounter notificationCounter = OfyService.ofy().load().type(NotificationCounter.class).id(id).now();
        if (notificationCounter == null) {
            throw new NotFoundException("Could not find NotificationCounter with ID: " + id);
        }
        return notificationCounter;
    }

    /**
     * Inserts a new {@code NotificationCounter}.
     */
    @ApiMethod(
            name = "insert",
            path = "notificationCounter",
            httpMethod = ApiMethod.HttpMethod.POST)
    public NotificationCounter insert(@Named("userId") Long userId) {
        NotificationCounter newCounter = null;
        User user = new User();
        user.setId(userId);
        Ref<User> userKey = Ref.create(user);
        NotificationCounter record = OfyService.ofy().load().type(NotificationCounter.class).filter("user",userKey).first().now();
        if(record != null){
            try{
                addNotificationToCounter(record);
                newCounter = update(record.getId(),record);
            }catch (NotFoundException e){
                logger.info(e.getMessage());
            }
        }else{
            newCounter = new NotificationCounter();
            newCounter.setUser(user);
            addNotificationToCounter(newCounter);
            OfyService.ofy().save().entity(newCounter).now();
            logger.info("Created NotificationCounter with ID: " + newCounter.getId());
            newCounter = OfyService.ofy().load().entity(newCounter).now();
        }
        return newCounter;
    }

    /**
     * Updates an existing {@code NotificationCounter}.
     *
     * @param id                  the ID of the entity to be updated
     * @param notificationCounter the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code NotificationCounter}
     */
    @ApiMethod(
            name = "update",
            path = "notificationCounter/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public NotificationCounter update(@Named("id") Long id, NotificationCounter notificationCounter) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        OfyService.ofy().save().entity(notificationCounter).now();
        logger.info("Updated NotificationCounter: " + notificationCounter);
        return OfyService.ofy().load().entity(notificationCounter).now();
    }

    /**
     * Deletes the specified {@code NotificationCounter}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code NotificationCounter}
     */
    @ApiMethod(
            name = "remove",
            path = "notificationCounter/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        OfyService.ofy().delete().type(NotificationCounter.class).id(id).now();
        logger.info("Deleted NotificationCounter with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "list",
            path = "notificationCounter",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<NotificationCounter> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<NotificationCounter> query = OfyService.ofy().load().type(NotificationCounter.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<NotificationCounter> queryIterator = query.iterator();
        List<NotificationCounter> notificationCounterList = new ArrayList<NotificationCounter>(limit);
        while (queryIterator.hasNext()) {
            notificationCounterList.add(queryIterator.next());
        }
        return CollectionResponse.<NotificationCounter>builder().setItems(notificationCounterList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            OfyService.ofy().load().type(NotificationCounter.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find NotificationCounter with ID: " + id);
        }
    }

    private int getTimeOfDay(){
        int timeOfDay = 0;
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        if(hour >= 6 && hour < 14){
            timeOfDay = DAY_MORNING;
        }else if(hour >= 14 && hour < 22){
            timeOfDay = DAY_EVENING;
        }else if (hour >= 22 || hour < 6){
            timeOfDay = DAY_NIGHT;
        }
        return timeOfDay;
    }

    private void addNotificationToCounter(NotificationCounter notificationCounter){
        switch (getTimeOfDay()){
            case DAY_MORNING:
                notificationCounter.setMorning(notificationCounter.getMorning()+1);
                break;
            case DAY_EVENING:
                notificationCounter.setEvening(notificationCounter.getEvening()+1);
                break;
            case DAY_NIGHT:
                notificationCounter.setNight(notificationCounter.getNight()+1);
                break;
        }
    }

    protected boolean isNotificationLimitReached(Long userId){
        boolean limitReached = false;
        User user = new User();
        user.setId(userId);
        Ref<User> userKey = Ref.create(user);
        LimitNotification limits = OfyService.ofy().load().type(LimitNotification.class).filter("user",userKey).first().now();
        NotificationCounter counters = OfyService.ofy().load().type(NotificationCounter.class).filter("user",userKey).first().now();
        if(counters != null){
            switch (getTimeOfDay()){
                case DAY_MORNING:
                    if(counters.getMorning() >= limits.getMorning()){
                        limitReached = true;
                        logger.info("Morning notification limit reached");
                    }
                    break;
                case DAY_EVENING:
                    if(counters.getEvening() >= limits.getEvening()){
                        limitReached = true;
                        logger.info("Evening notification limit reached");
                    }
                    break;
                case DAY_NIGHT:
                    if(counters.getNight() >= limits.getNight()){
                        limitReached = true;
                        logger.info("Night notification limit reached");
                    }
                    break;
            }
        }
        return limitReached;
    }
}