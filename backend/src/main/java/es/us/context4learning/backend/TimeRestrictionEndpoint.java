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

import static es.us.context4learning.backend.OfyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "timeRestrictionApi",
        version = "v1",
        resource = "timeRestriction",
        namespace = @ApiNamespace(
                ownerDomain = "backend.context4learning.us.es",
                ownerName = "backend.context4learning.us.es",
                packagePath = ""
        )
)
public class TimeRestrictionEndpoint {

    private static final Logger logger = Logger.getLogger(TimeRestrictionEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(TimeRestriction.class);
        ObjectifyService.register(User.class);
    }

    /**
     * Returns the {@link TimeRestriction} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code TimeRestriction} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "timeRestriction/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public TimeRestriction get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting TimeRestriction with ID: " + id);
        TimeRestriction timeRestriction = ofy().load().type(TimeRestriction.class).id(id).now();
        if (timeRestriction == null) {
            throw new NotFoundException("Could not find TimeRestriction with ID: " + id);
        }
        return timeRestriction;
    }

    /**
     * Inserts a new {@code TimeRestriction}.
     */
    @ApiMethod(
            name = "insert",
            path = "timeRestriction",
            httpMethod = ApiMethod.HttpMethod.POST)
    public TimeRestriction insert(TimeRestriction timeRestriction) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that timeRestriction.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        ofy().save().entity(timeRestriction).now();
        logger.info("Created TimeRestriction with startTime: " + timeRestriction.getStartTime().toString());
        logger.info("Created TimeRestriction with endTime: " + timeRestriction.getEndTime().toString());
        logger.info("Created TimeRestriction with ID: " + timeRestriction.getId());
        return ofy().load().entity(timeRestriction).now();
    }

    /**
     * Updates an existing {@code TimeRestriction}.
     *
     * @param id              the ID of the entity to be updated
     * @param timeRestriction the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code TimeRestriction}
     */
    @ApiMethod(
            name = "update",
            path = "timeRestriction/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public TimeRestriction update(@Named("id") Long id, TimeRestriction timeRestriction) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(timeRestriction).now();
        logger.info("Updated TimeRestriction: " + timeRestriction);
        return ofy().load().entity(timeRestriction).now();
    }

    /**
     * Deletes the specified {@code TimeRestriction}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code TimeRestriction}
     */
    @ApiMethod(
            name = "remove",
            path = "timeRestriction/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(TimeRestriction.class).id(id).now();
        logger.info("Deleted TimeRestriction with ID: " + id);
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
            path = "timeRestriction",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<TimeRestriction> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<TimeRestriction> query = ofy().load().type(TimeRestriction.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<TimeRestriction> queryIterator = query.iterator();
        List<TimeRestriction> timeRestrictionList = new ArrayList<TimeRestriction>(limit);
        while (queryIterator.hasNext()) {
            timeRestrictionList.add(queryIterator.next());
        }
        return CollectionResponse.<TimeRestriction>builder().setItems(timeRestrictionList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "user",
            path = "user/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<TimeRestriction> userTimeRestrictions(@Named("id") Long id) {
        User user = new User();
        user.setId(id);
        Ref<User> userKey = Ref.create(user);
        return ofy().load().type(TimeRestriction.class).filter("user",userKey).list();
    }

    protected boolean isTimeRestricted(User user){
        boolean restricted = false;
        Ref<User> userKey = Ref.create(user);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.MONTH,0);
        calendar.set(Calendar.YEAR,1111);
        logger.info("calendar.getTime() = " + calendar.getTime());
        List<TimeRestriction> restrictions = ofy().load().type(TimeRestriction.class)
                                                .filter("user", userKey)
                                                .filter("startTime <", calendar.getTime()).list();
        for (TimeRestriction restriction : restrictions){
            if(restriction.getEndTime().after(calendar.getTime())){
                restricted = true;
                break;
            }
        }
        return restricted;
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(TimeRestriction.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find TimeRestriction with ID: " + id);
        }
    }
}