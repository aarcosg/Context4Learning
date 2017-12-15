package es.us.context4learning.data.api.moodle;

import java.util.List;

import es.us.context4learning.data.api.moodle.entity.Course;
import es.us.context4learning.data.api.moodle.entity.StudentProgress;
import es.us.context4learning.data.api.moodle.entity.Task;
import es.us.context4learning.data.api.moodle.entity.response.AuthTokenResponse;
import es.us.context4learning.data.api.moodle.entity.response.MoodleCourse;
import es.us.context4learning.data.api.moodle.entity.response.SiteInfoResponse;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface MoodleApi {

    String REST_SERVICE_MOBILE_APP = "moodle_mobile_app";
    String SERVER_NAME = "context4learning.cica.es";
    String SERVICE_ENDPOINT = "https://context4learning.cica.es/";
    //String SERVER_NAME = "10.141.0.178/moodle_usevilla";
    //String SERVICE_ENDPOINT = "http://10.141.0.178/moodle_usevilla/";
    String WS_BASE_URL = "/webservice/rest/server.php";
    String MOODLE_TASK_ACCESS_URL = SERVICE_ENDPOINT + "apiuse/acceso_recurso.php";
    String FIELD_TOKEN = "wstoken";
    String FIELD_FUNCTION = "wsfunction";
    String FIELD_USERID = "userid";
    String FIELD_RESTFORMAT = "moodlewsrestformat";
    String RESPONSE_FORMAT = "json";

    @FormUrlEncoded
    @POST("login/token.php")
    Observable<Response<AuthTokenResponse>> getAuthToken(
            @Field("username") String username,
            @Field("password") String password,
            @Field("service") String service);

    @FormUrlEncoded
    @POST(WS_BASE_URL + "?" + FIELD_FUNCTION + "=moodle_webservice_get_siteinfo"
            + "&" + FIELD_RESTFORMAT + "=" + RESPONSE_FORMAT)
    Observable<Response<SiteInfoResponse>> getSiteInfo(@Field(FIELD_TOKEN) String token);

    @FormUrlEncoded
    @POST(WS_BASE_URL + "?" + FIELD_FUNCTION + "=moodle_enrol_get_users_courses"
            + "&" + FIELD_RESTFORMAT + "=" + RESPONSE_FORMAT)
    Observable<Response<List<MoodleCourse>>> getCourses(
            @Field(FIELD_TOKEN) String token,
            @Field(FIELD_USERID) Integer userid);


    @FormUrlEncoded
    @POST("apiuse/recursos.php")
    Observable<Response<List<Task>>> getTasks(
            @Field("user") String user,
            @Field("pass") String pass,
            @Field("actividad") String activity,
            @Field("tiempo") Integer time,
            @Field("accion") String action);

    @FormUrlEncoded
    @POST("apiuse/recursos_curso.php")
    Observable<Response<List<Task>>> getTasksByCourse(
            @Field("user") String user,
            @Field("pass") String pass,
            @Field("id_curso") Long courseId,
            @Field("actividad") String activity,
            @Field("tiempo") Integer time,
            @Field("accion") String action);

    @FormUrlEncoded
    @POST("apiuse/recursos_finalizados.php")
    Observable<Response<List<Task>>> getFinishedTasks(
            @Field("user") String user,
            @Field("pass") String pass,
            @Field("id_curso") Long courseId,
            @Field("actividad") String activity,
            @Field("tiempo") Integer time,
            @Field("accion") String action);

    @FormUrlEncoded
    @POST("apiuse/cursos.php")
    Observable<Response<List<Course>>> getCourses(
            @Field("user") String user,
            @Field("pass") String pass);

    @FormUrlEncoded
    @POST("apiuse/seguimiento.php")
    Observable<Response<List<Course>>> getStudentProgress(
            @Field("user") String user,
            @Field("pass") String pass);

    @FormUrlEncoded
    @POST("apiuse/seguimiento.php")
    Observable<Response<StudentProgress>> getStudentProgressByCourse(
            @Field("user") String user,
            @Field("pass") String pass,
            @Field("id_curso") Long courseId);

    @FormUrlEncoded
    @POST("apiuse/notificaciones.php")
    Observable<Response<List<Task>>> getNotifications(
            @Field("user") String user,
            @Field("pass") String pass);

    @FormUrlEncoded
    @POST("apiuse/existe_usuario.php")
    Observable<Response<Boolean>> existsUser(
            @Field("user") String user,
            @Field("pass") String pass);

}
