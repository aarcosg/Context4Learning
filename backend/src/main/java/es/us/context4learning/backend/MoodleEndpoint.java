package es.us.context4learning.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import static es.us.context4learning.backend.OfyService.ofy;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "moodleApi",
        version = "v1",
        resource = "moodle",
        namespace = @ApiNamespace(
                ownerDomain = "backend.context4learning.us.es",
                ownerName = "backend.context4learning.us.es",
                packagePath = ""
        )
)
public class MoodleEndpoint {

    private static final Logger logger = Logger.getLogger(MoodleEndpoint.class.getName());
    private static final String MOODLE_RECURSOS_URL = "https://context4learning.cica.es/apiuse/recursos.php";
    private static final String MOODLE_NOTIFICACIONES_URL = "https://context4learning.cica.es/apiuse/notificaciones.php";
    private static final String MOODLE_CURSOS_URL = "https://context4learning.cica.es/apiuse/cursos.php";

    @ApiMethod(
            name = "getRecursos",
            path = "recursos")
     public List<MoodleTask> getRecursos(@Named("userId") Long userId, @Named("actividad") String actividad, @Named("tiempo") Integer tiempo, @Named("accion") String accion ) {
        logger.info("Calling getRecursos method");
        List<MoodleTask> tasks = new ArrayList<MoodleTask>();
        User user = ofy().load().type(User.class).id(userId).now();
        if(user != null){
            String parameters = "?user="+user.getUsername()
                    +"&pass="+user.getPassword()
                    +"&actividad="+actividad
                    +"&tiempo="+tiempo
                    +"&accion="+accion;

            try {
                URL url = new URL(MOODLE_RECURSOS_URL + parameters);
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
                    JsonArray cursosArray = parser.parse(jsonResult).getAsJsonArray();
                    for(JsonElement curso : cursosArray){
                        JsonArray recursosArray = curso.getAsJsonArray();
                        for(JsonElement recurso : recursosArray){
                            MoodleTask task =  gson.fromJson(recurso,MoodleTask.class);
                            tasks.add(task);
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

        }
        return tasks;
    }

    @ApiMethod(
            name = "getNotificaciones",
            path = "notificaciones")
    public List<MoodleTask> getNotificaciones(@Named("userId") Long userId) {
        logger.info("Calling getNotificaciones method");
        List<MoodleTask> tasks = new ArrayList<MoodleTask>();
        User user = ofy().load().type(User.class).id(userId).now();
        if(user != null){
            String parameters = "?user="+user.getUsername()
                    +"&pass="+user.getPassword();
            try {
                URL url = new URL(MOODLE_NOTIFICACIONES_URL + parameters);
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
                    JsonArray cursosArray = parser.parse(jsonResult).getAsJsonArray();
                    for(JsonElement curso : cursosArray){
                        JsonArray recursosArray = curso.getAsJsonArray();
                        for(JsonElement recurso : recursosArray){
                            MoodleTask task =  gson.fromJson(recurso,MoodleTask.class);
                            tasks.add(task);
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
        }
        return tasks;
    }


}