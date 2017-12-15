package es.us.context4learning.backend;

import com.beoui.geocell.GeocellManager;
import com.beoui.geocell.model.BoundingBox;
import com.beoui.geocell.model.Point;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.code.geocoder.model.LatLng;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;

import java.io.IOException;
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
        name = "locationRestrictionApi",
        version = "v1",
        resource = "locationRestriction",
        namespace = @ApiNamespace(
                ownerDomain = "backend.context4learning.us.es",
                ownerName = "backend.context4learning.us.es",
                packagePath = ""
        )
)
public class LocationRestrictionEndpoint {

    private static final Logger logger = Logger.getLogger(LocationRestrictionEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    private static final double EARTH_RADIUS = 6371.01; //kilometers
    private static final double DEFAULT_DISTANCE = 0.2; //kilometers

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(LocationRestriction.class);
        ObjectifyService.register(User.class);
    }

    /**
     * Returns the {@link LocationRestriction} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code LocationRestriction} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "locationRestriction/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public LocationRestriction get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting LocationRestriction with ID: " + id);
        LocationRestriction locationRestriction = ofy().load().type(LocationRestriction.class).id(id).now();
        if (locationRestriction == null) {
            throw new NotFoundException("Could not find LocationRestriction with ID: " + id);
        }
        return locationRestriction;
    }

    /**
     * Inserts a new {@code LocationRestriction}.
     */
    @ApiMethod(
            name = "insert",
            path = "locationRestriction",
            httpMethod = ApiMethod.HttpMethod.POST)
    public LocationRestriction insert(LocationRestriction locationRestriction) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that locationRestriction.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        if(locationRestriction.getAddress() == null || locationRestriction.getAddress().length()==0){
            LatLng latLng = new LatLng(
                     Float.valueOf(locationRestriction.getLocation().getLatitude()).toString()
                    ,Float.valueOf(locationRestriction.getLocation().getLongitude()).toString());
            final Geocoder geocoder = new Geocoder();
            GeocoderRequest geocoderRequest = new GeocoderRequestBuilder()
                    .setLocation(latLng)
                    .setLanguage("es")
                    .getGeocoderRequest();
            try {
                GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
                if(GeocoderStatus.OK.equals(geocoderResponse.getStatus())){
                    List<GeocoderResult> results = geocoderResponse.getResults();
                    if(!results.isEmpty()){
                        locationRestriction.setAddress(results.get(0).getFormattedAddress());
                        logger.info("Address changed to: " + results.get(0).getFormattedAddress());
                    }
                } else if (GeocoderStatus.OVER_QUERY_LIMIT.equals(geocoderResponse.getStatus())){
                    logger.info("Geocoder daily limit of requests reached");
                }
            }catch (IOException e){
                logger.info("Geocode IOException");
            }
        }
        ofy().save().entity(locationRestriction).now();
        logger.info("Created LocationRestriction with ID: " + locationRestriction.getId());

        return ofy().load().entity(locationRestriction).now();
    }

    /**
     * Updates an existing {@code LocationRestriction}.
     *
     * @param id                  the ID of the entity to be updated
     * @param locationRestriction the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code LocationRestriction}
     */
    @ApiMethod(
            name = "update",
            path = "locationRestriction/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public LocationRestriction update(@Named("id") Long id, LocationRestriction locationRestriction) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(locationRestriction).now();
        logger.info("Updated LocationRestriction: " + locationRestriction);
        return ofy().load().entity(locationRestriction).now();
    }

    /**
     * Deletes the specified {@code LocationRestriction}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code LocationRestriction}
     */
    @ApiMethod(
            name = "remove",
            path = "locationRestriction/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(LocationRestriction.class).id(id).now();
        logger.info("Deleted LocationRestriction with ID: " + id);
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
            path = "locationRestriction",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<LocationRestriction> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<LocationRestriction> query = ofy().load().type(LocationRestriction.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<LocationRestriction> queryIterator = query.iterator();
        List<LocationRestriction> locationRestrictionList = new ArrayList<LocationRestriction>(limit);
        while (queryIterator.hasNext()) {
            locationRestrictionList.add(queryIterator.next());
        }
        return CollectionResponse.<LocationRestriction>builder().setItems(locationRestrictionList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "user",
            path = "user/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<LocationRestriction> userLocationRestrictions(@Named("id") Long id) {
        User user = new User();
        user.setId(id);
        Ref<User> userKey = Ref.create(user);
        return ofy().load().type(LocationRestriction.class).filter("user",userKey).list();
    }

    protected boolean isLocationRestricted(User user, GeoPt location){
        boolean restricted = false;
        if(location != null){
            Ref<User> userKey = Ref.create(user);
            List<LocationRestriction> restrictions = ofy().load().type(LocationRestriction.class).filter("user", userKey).list();

            GeoLocation specificPointLocation = GeoLocation.fromDegrees(location.getLatitude(), location.getLongitude());
            GeoLocation[] bc = specificPointLocation.boundingCoordinates(DEFAULT_DISTANCE,EARTH_RADIUS);

            // Transform this to a bounding box
            BoundingBox bb = new BoundingBox((float) bc[0].getLatitudeInDegrees(),
                    (float) bc[1].getLongitudeInDegrees(),
                    (float) bc[1].getLatitudeInDegrees(),
                    (float) bc[0].getLongitudeInDegrees());

            // Calculate the geocells list to be used in the queries (optimize
            // list of cells that complete the given bounding box)
            List<String> cells = GeocellManager.bestBboxSearchCells(bb, null);

            for(LocationRestriction restriction : restrictions){
                // calculate geocells of your model class instance
                Point point = new Point(restriction.getLocation().getLatitude(),restriction.getLocation().getLongitude());
                List <String> modelCells = GeocellManager.generateGeoCell(point);

                // matching
                for (String c : cells) {
                    if (modelCells.contains(c)) {
                        // success, do sth with it
                        logger.info("User inside location restriction");
                        restricted = true;
                        break;
                    }
                }
                if(restricted){
                    break;
                }
            }
        }
        return restricted;
    }


    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(LocationRestriction.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find LocationRestriction with ID: " + id);
        }
    }
}