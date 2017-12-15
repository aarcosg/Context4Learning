package es.us.context4learning.data.api.google.firebase;

import es.us.context4learning.data.api.google.firebase.entity.Message;
import es.us.context4learning.data.api.google.firebase.entity.response.SendMessageResponse;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rx.Observable;

public interface FirebaseApi {

    String SERVICE_ENDPOINT = "https://fcm.googleapis.com/";

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AIzaSyCAjRXATpZFzNfyIRhIv2MZ8QGlK3SyRwk"
    })
    @POST("fcm/send")
    Observable<Response<SendMessageResponse>> sendMessage(@Body Message message);

}
