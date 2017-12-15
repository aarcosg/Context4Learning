package es.us.context4learning.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
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
        name = "contextApi",
        version = "v1",
        resource = "context",
        namespace = @ApiNamespace(
                ownerDomain = "backend.context4learning.us.es",
                ownerName = "backend.context4learning.us.es",
                packagePath = ""
        )
)
public class ContextEndpoint {

    private static final Logger logger = Logger.getLogger(ContextEndpoint.class.getName());
    private static final String MOODLE_NOTIFICATIONS_URL = "https://context4learning.cica.es/apiuse/notificaciones.php";
    private static final String MOODLE_STUDENT_PROGRESS_URL = "https://context4learning.cica.es/apiuse/seguimiento.php";
    private static final String MOODLE_STUDENT_PROGRESS_BY_COURSE_URL = "https://context4learning.cica.es/apiuse/seguimiento_curso.php";
    private static final String JSON_ERROR_CODE_KEY = "error-code";
    private static final int JSON_ERROR_UNAUTHORIZED_ACCESS = 1;
    private static final int JSON_ERROR_EMPTY_TASKS = 2;
    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Context.class);
        ObjectifyService.register(Device.class);
        ObjectifyService.register(Notification.class);
    }

    /**
     * Returns the {@link Context} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Context} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "context/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Context get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting Context with ID: " + id);
        Context context = OfyService.ofy().load().type(Context.class).id(id).now();
        if (context == null) {
            throw new NotFoundException("Could not find Context with ID: " + id);
        }
        return context;
    }

    /**
     * Inserts a new {@code Context}.
     */
    @ApiMethod(
            name = "insert",
            path = "context",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Context insert(Context context) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that context.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        context.setTime(new Date());
        OfyService.ofy().save().entity(context).now();
        logger.info("Created Context with ID = " + context.getId() +". Activity = " + context.getActivity());
        Context newContext = OfyService.ofy().load().entity(context).now();
        boolean isTimeRestricted = new TimeRestrictionEndpoint().isTimeRestricted(context.getDevice().getUser());
        boolean isLocationRestricted = new LocationRestrictionEndpoint().isLocationRestricted(context.getDevice().getUser(),context.getLocation());
        logger.info("Time restricted = " + isTimeRestricted + " Location restricted = " + isLocationRestricted);
        boolean isNotificationLimitReached = new NotificationCounterEndpoint().isNotificationLimitReached(context.getDevice().getUser().getId());
        logger.info("isNotificationLimitReached? " + isNotificationLimitReached);
        if(!isNotificationLimitReached && !isTimeRestricted && !isLocationRestricted){
            List<MoodleTask> pendingTasks = getMoodleNotifications(context.getDevice().getUser());
            if(!pendingTasks.isEmpty()){
                logger.info(pendingTasks.size() + " pending tasks available");
                MoodleTask task = pendingTasks.get(new Random().nextInt(pendingTasks.size()));
                List<MoodleCourse> courses = getMoodleStudentProgress(newContext.getDevice().getUser());
                MoodleCourse course = null;
                for(MoodleCourse c : courses){
                    if(task.getCourseId() == c.getId()){
                        course = c;
                        break;
                    }
                }
                if(course != null){
                    StringBuilder sb = new StringBuilder("Parece que est\u00E1s ");
                    if(context.getActivity().equalsIgnoreCase("vehiculo")){
                        sb.append("en veh\u00EDculo");
                    }else{
                        sb.append(context.getActivity().toLowerCase());
                    }
                    sb.append(".");
                    sb.append(" Has avanzado en '").append(course.getName().trim()).append("' un ").append(course.getProgress()).append("%").append(" y tus compa\u00F1eros un ").append(course.getAvg()).append("%. ");
                    if(course.getProgress() < course.getAvg()){
                        sb.append("\u00A1\u00C1nimo!");
                    }else{
                        sb.append("\u00A1Sigue as\u00ED!");
                    }
                    Notification notification = new Notification();
                    notification.setContext(newContext);
                    notification.setMessage(sb.toString());
                    Notification newNotification = new NotificationEndpoint().insert(notification);
                    new MessagingEndpoint().sendNotification(newNotification);
                }
            }
        }
        return newContext;
    }

    /**
     * Updates an existing {@code Context}.
     *
     * @param id      the ID of the entity to be updated
     * @param context the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Context}
     */
    @ApiMethod(
            name = "update",
            path = "context/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Context update(@Named("id") Long id, Context context) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        OfyService.ofy().save().entity(context).now();
        logger.info("Updated Context: " + context);
        return OfyService.ofy().load().entity(context).now();
    }

    /**
     * Deletes the specified {@code Context}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Context}
     */
    @ApiMethod(
            name = "remove",
            path = "context/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        OfyService.ofy().delete().type(Context.class).id(id).now();
        logger.info("Deleted Context with ID: " + id);
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
            path = "context",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Context> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Context> query = OfyService.ofy().load().type(Context.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Context> queryIterator = query.iterator();
        List<Context> contextList = new ArrayList<Context>(limit);
        while (queryIterator.hasNext()) {
            contextList.add(queryIterator.next());
        }
        return CollectionResponse.<Context>builder().setItems(contextList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            OfyService.ofy().load().type(Context.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Context with ID: " + id);
        }
    }

    private List<MoodleTask> getMoodleNotifications(User user) {
        logger.info("Calling getMoodleNotifications method");
        List<MoodleTask> tasks = new ArrayList<MoodleTask>();
        if(user != null){
            String parameters = "?user="+user.getUsername()
                    +"&pass="+user.getPassword();
            try {
                URL url = new URL(MOODLE_NOTIFICATIONS_URL + parameters);
                logger.info("getMoodleNotifications URL = " + url.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
                    StringBuilder sb = new StringBuilder();
                    int cp;
                    while ((cp = br.read()) != -1) {
                        sb.append((char) cp);
                    }
                    br.close();
                    String jsonResult = sb.toString();

                    JsonParser parser = new JsonParser();
                    JsonElement jsonElement = parser.parse(jsonResult);
                    if(jsonElement instanceof JsonObject
                            && ((JsonObject) jsonElement).has(JSON_ERROR_CODE_KEY)
                            && ((JsonObject) jsonElement).get(JSON_ERROR_CODE_KEY).getAsInt() == JSON_ERROR_EMPTY_TASKS){
                    /* Example: {"error-code":"2","descripcion":"Sin datos"}
                    * Do not do anything
                    * */
                    } else if (jsonElement instanceof JsonArray) {
                        JsonArray cursosArray = jsonElement.getAsJsonArray();
                        for(JsonElement curso : cursosArray){
                            JsonArray recursosArray = curso.getAsJsonArray();
                            for(JsonElement recurso : recursosArray){
                                MoodleTask task =  new Gson().fromJson(recurso,MoodleTask.class);
                                tasks.add(task);
                            }
                        }
                    }
                } else {
                    logger.info("Server returned HTTP error code:"+connection.getResponseCode());
                }
            } catch (MalformedURLException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }

        }else{
            logger.info("User is null");
        }
        return tasks;
    }

    private List<MoodleCourse> getMoodleStudentProgress(User user) {
        logger.info("Calling getMoodleStudentProgress method");
        List<MoodleCourse> courses = new ArrayList<MoodleCourse>();
        if(user != null){
            String parameters = "?user="+user.getUsername()
                    +"&pass="+user.getPassword();
            try {
                URL url = new URL(MOODLE_STUDENT_PROGRESS_URL + parameters);
                logger.info("getMoodleStudentProgress URL = " + url.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
                    StringBuilder sb = new StringBuilder();
                    int cp;
                    while ((cp = br.read()) != -1) {
                        sb.append((char) cp);
                    }
                    br.close();
                    String jsonResult = sb.toString();

                    JsonParser parser = new JsonParser();
                    JsonElement jsonElement = parser.parse(jsonResult);
                    if(jsonElement instanceof JsonObject
                            && ((JsonObject) jsonElement).has(JSON_ERROR_CODE_KEY)
                            && ((JsonObject) jsonElement).get(JSON_ERROR_CODE_KEY).getAsInt() == JSON_ERROR_EMPTY_TASKS){
                    /* Example: {"error-code":"2","descripcion":"Sin datos"}
                    * Do not do anything
                    * */
                    } else if (jsonElement instanceof JsonArray) {
                        Gson gson = new Gson();
                        JsonArray cursosArray = jsonElement.getAsJsonArray();
                        for(JsonElement curso : cursosArray){
                            MoodleCourse course = gson.fromJson(curso, MoodleCourse.class);
                            courses.add(course);
                        }
                    }
                } else {
                    logger.info("Server returned HTTP error code:"+connection.getResponseCode());
                }
            } catch (MalformedURLException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }

        }else{
            logger.info("User is null");
        }
        return courses;
    }

    private MoodleStudentProgress getMoodleStudentProgress(User user, Long courseId) {
        logger.info("Calling getMoodleStudentProgress method");
        MoodleStudentProgress studentProgress = null;
        if(user != null){
            String parameters =
                    "?user="+user.getUsername()
                    +"&pass="+user.getPassword()
                    +"&id_curso="+courseId;
            try {
                URL url = new URL(MOODLE_STUDENT_PROGRESS_BY_COURSE_URL + parameters);
                logger.info("getMoodleStudentProgress URL = " + url.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
                    StringBuilder sb = new StringBuilder();
                    int cp;
                    while ((cp = br.read()) != -1) {
                        sb.append((char) cp);
                    }
                    br.close();
                    String jsonResult = sb.toString();

                    JsonParser parser = new JsonParser();
                    JsonElement jsonElement = parser.parse(jsonResult);
                    if(jsonElement instanceof JsonObject
                            && ((JsonObject) jsonElement).has(JSON_ERROR_CODE_KEY)
                            && ((JsonObject) jsonElement).get(JSON_ERROR_CODE_KEY).getAsInt() == JSON_ERROR_EMPTY_TASKS){
                    /* Example: {"error-code":"2","descripcion":"Sin datos"}
                    * Do not do anything
                    * */
                    } else if (jsonElement instanceof JsonArray) {
                        Gson gson = new Gson();
                        JsonArray jsonArray = jsonElement.getAsJsonArray();
                        studentProgress = gson.fromJson(jsonArray.get(0), MoodleStudentProgress.class);
                    }
                } else {
                    logger.info("Server returned HTTP error code:"+connection.getResponseCode());
                }
            } catch (MalformedURLException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }

        }else{
            logger.info("User is null");
        }
        return studentProgress;
    }

    private boolean isBelowAvgProgressMoodle(User user) {
        logger.info("Calling getAvanceMoodle method");
        boolean belowAvg = false;
        if(user != null){
            String parameters = "?user="+user.getUsername()
                    +"&pass="+user.getPassword();
            try {
                URL url = new URL(MOODLE_STUDENT_PROGRESS_URL + parameters);
                logger.info("getSeguimiento URL = " + url.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
                    StringBuilder sb = new StringBuilder();
                    int cp;
                    while ((cp = br.read()) != -1) {
                        sb.append((char) cp);
                    }
                    br.close();
                    String jsonResult = sb.toString();

                    Gson gson = new Gson();
                    JsonParser parser = new JsonParser();
                    JsonElement jsonElement = parser.parse(jsonResult);
                    if(jsonElement instanceof JsonObject
                            && ((JsonObject) jsonElement).has(JSON_ERROR_CODE_KEY)
                            && ((JsonObject) jsonElement).get(JSON_ERROR_CODE_KEY).getAsInt() == JSON_ERROR_EMPTY_TASKS){
                    /* Example: {"error-code":"2","descripcion":"Sin datos"}
                    * Do not do anything
                    * */
                    } else if (jsonElement instanceof JsonArray) {
                        JsonArray cursosArray = jsonElement.getAsJsonArray();
                        for(JsonElement curso : cursosArray){
                            MoodleCourse mCourse = gson.fromJson(curso,MoodleCourse.class);
                            if(mCourse.getProgress() < mCourse.getAvg()){
                                belowAvg = true;
                                break;
                            }
                        }
                    }
                } else {
                    logger.info("Server returned HTTP error code:"+connection.getResponseCode());
                }
            } catch (MalformedURLException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }
        }else{
            logger.info("User is null");
        }
        return belowAvg;
    }
}