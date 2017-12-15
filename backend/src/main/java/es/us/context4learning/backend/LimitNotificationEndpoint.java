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
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static es.us.context4learning.backend.OfyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "limitNotificationApi",
        version = "v1",
        resource = "limitNotification",
        namespace = @ApiNamespace(
                ownerDomain = "backend.context4learning.us.es",
                ownerName = "backend.context4learning.us.es",
                packagePath = ""
        )
)
public class LimitNotificationEndpoint {

    private static final Logger logger = Logger.getLogger(LimitNotificationEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;
    public static final int MAX_NOTIFICATIONS = 5;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(LimitNotification.class);
        ObjectifyService.register(User.class);
    }

    /**
     * Returns the {@link LimitNotification} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code LimitNotification} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "limitNotification/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public LimitNotification get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting LimitNotification with ID: " + id);
        LimitNotification limitNotification = ofy().load().type(LimitNotification.class).id(id).now();
        if (limitNotification == null) {
            throw new NotFoundException("Could not find LimitNotification with ID: " + id);
        }
        return limitNotification;
    }

    /**
     * Inserts a new {@code LimitNotification}.
     */
    @ApiMethod(
            name = "insert",
            path = "limitNotification",
            httpMethod = ApiMethod.HttpMethod.POST)
    public LimitNotification insert(LimitNotification limitNotification){
        LimitNotification newLimits = null;
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that limitNotification.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        LimitNotification record = ofy().load().type(LimitNotification.class).filter("user",limitNotification.getUser()).first().now();
        if(record != null){
            try{
                limitNotification.setId(record.getId());
                newLimits = update(record.getId(),limitNotification);
            }catch (NotFoundException e){
                logger.info(e.getMessage());
            }
        }else{
            ofy().save().entity(limitNotification).now();
            logger.info("Created LimitNotification with ID: " + limitNotification.getId());
            newLimits = ofy().load().entity(limitNotification).now();
        }

        return newLimits;
    }

    /**
     * Updates an existing {@code LimitNotification}.
     *
     * @param id                the ID of the entity to be updated
     * @param limitNotification the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code LimitNotification}
     */
    @ApiMethod(
            name = "update",
            path = "limitNotification/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public LimitNotification update(@Named("id") Long id, LimitNotification limitNotification) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExistsLimitNotification(id);
        ofy().save().entity(limitNotification).now();
        logger.info("Updated LimitNotification: " + limitNotification);
        return ofy().load().entity(limitNotification).now();
    }

    /**
     * Deletes the specified {@code LimitNotification}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code LimitNotification}
     */
    @ApiMethod(
            name = "remove",
            path = "limitNotification/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExistsLimitNotification(id);
        ofy().delete().type(LimitNotification.class).id(id).now();
        logger.info("Deleted LimitNotification with ID: " + id);
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
            path = "limitNotification",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<LimitNotification> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<LimitNotification> query = ofy().load().type(LimitNotification.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<LimitNotification> queryIterator = query.iterator();
        List<LimitNotification> limitNotificationList = new ArrayList<LimitNotification>(limit);
        while (queryIterator.hasNext()) {
            limitNotificationList.add(queryIterator.next());
        }
        return CollectionResponse.<LimitNotification>builder().setItems(limitNotificationList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "user",
            path = "user/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public LimitNotification userLimitNotifications(@Named("id") Long id) throws NotFoundException {
        checkExistsUser(id);
        User user = new User();
        user.setId(id);
        Ref<User> userKey = Ref.create(user);
        return ofy().load().type(LimitNotification.class).filter("user",userKey).first().now();
    }

    private void checkExistsLimitNotification(Long id) throws NotFoundException {
        try {
            ofy().load().type(LimitNotification.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find LimitNotification with ID: " + id);
        }
    }

    private void checkExistsUser(Long id) throws NotFoundException {
        try {
            ofy().load().type(User.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find User with ID: " + id);
        }
    }


}