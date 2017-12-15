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
        name = "auditEventApi",
        version = "v1",
        resource = "auditEvent",
        namespace = @ApiNamespace(
                ownerDomain = "backend.context4learning.us.es",
                ownerName = "backend.context4learning.us.es",
                packagePath = ""
        )
)
public class AuditEventEndpoint {

    private static final Logger logger = Logger.getLogger(AuditEventEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(AuditEvent.class);
        ObjectifyService.register(User.class);
    }

    /**
     * Returns the {@link AuditEvent} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code AuditEvent} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "auditEvent/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public AuditEvent get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting AuditEvent with ID: " + id);
        AuditEvent auditEvent = ofy().load().type(AuditEvent.class).id(id).now();
        if (auditEvent == null) {
            throw new NotFoundException("Could not find AuditEvent with ID: " + id);
        }
        return auditEvent;
    }

    /**
     * Inserts a new {@code AuditEvent}.
     */
    @ApiMethod(
            name = "insert",
            path = "auditEvent",
            httpMethod = ApiMethod.HttpMethod.POST)
    public AuditEvent insert(AuditEvent auditEvent) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that auditEvent.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        auditEvent.setTime(new Date());
        ofy().save().entity(auditEvent).now();
        logger.info("Created AuditEvent with ID: " + auditEvent.getId());

        return ofy().load().entity(auditEvent).now();
    }

    /**
     * Updates an existing {@code AuditEvent}.
     *
     * @param id         the ID of the entity to be updated
     * @param auditEvent the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code AuditEvent}
     */
    @ApiMethod(
            name = "update",
            path = "auditEvent/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public AuditEvent update(@Named("id") Long id, AuditEvent auditEvent) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(auditEvent).now();
        logger.info("Updated AuditEvent: " + auditEvent);
        return ofy().load().entity(auditEvent).now();
    }

    /**
     * Deletes the specified {@code AuditEvent}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code AuditEvent}
     */
    @ApiMethod(
            name = "remove",
            path = "auditEvent/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(AuditEvent.class).id(id).now();
        logger.info("Deleted AuditEvent with ID: " + id);
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
            path = "auditEvent",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<AuditEvent> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<AuditEvent> query = ofy().load().type(AuditEvent.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<AuditEvent> queryIterator = query.iterator();
        List<AuditEvent> auditEventList = new ArrayList<AuditEvent>(limit);
        while (queryIterator.hasNext()) {
            auditEventList.add(queryIterator.next());
        }
        return CollectionResponse.<AuditEvent>builder().setItems(auditEventList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(AuditEvent.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find AuditEvent with ID: " + id);
        }
    }
}