package es.us.context4learning.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "notificationApi",
        version = "v1",
        resource = "notification",
        namespace = @ApiNamespace(
                ownerDomain = "backend.context4learning.us.es",
                ownerName = "backend.context4learning.us.es",
                packagePath = ""
        )
)
public class NotificationEndpoint {

    private static final Logger logger = Logger.getLogger(NotificationEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Notification.class);
        ObjectifyService.register(Device.class);
        ObjectifyService.register(Context.class);
    }

    /**
     * Returns the {@link Notification} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Notification} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "notification/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Notification get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting Notification with ID: " + id);
        Notification notification = ofy().load().type(Notification.class).id(id).now();
        if (notification == null) {
            throw new NotFoundException("Could not find Notification with ID: " + id);
        }
        return notification;
    }

    /**
     * Inserts a new {@code Notification}.
     */
    @ApiMethod(
            name = "insert",
            path = "notification",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Notification insert(Notification notification) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that notification.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        notification.setSentTime(new Date());
        ofy().save().entity(notification).now();
        logger.info("Created Notification with ID: " + notification.getId());
        return ofy().load().entity(notification).now();
    }

    /**
     * Updates an existing {@code Notification}.
     *
     * @param id           the ID of the entity to be updated
     * @param notification the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Notification}
     */
    @ApiMethod(
            name = "update",
            path = "notification/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Notification update(@Named("id") Long id, Notification notification) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(notification).now();
        logger.info("Updated Notification: " + notification);
        return ofy().load().entity(notification).now();
    }

    /**
     * Deletes the specified {@code Notification}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Notification}
     */
    @ApiMethod(
            name = "remove",
            path = "notification/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(Notification.class).id(id).now();
        logger.info("Deleted Notification with ID: " + id);
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
            path = "notification",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Notification> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Notification> query = ofy().load().type(Notification.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Notification> queryIterator = query.iterator();
        List<Notification> notificationList = new ArrayList<Notification>(limit);
        while (queryIterator.hasNext()) {
            notificationList.add(queryIterator.next());
        }
        return CollectionResponse.<Notification>builder().setItems(notificationList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(Notification.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Notification with ID: " + id);
        }
    }

    private Notification findRecord(String notificationId) {
        return OfyService.ofy().load().type(Notification.class).filter("id", notificationId).first().now();
    }
}