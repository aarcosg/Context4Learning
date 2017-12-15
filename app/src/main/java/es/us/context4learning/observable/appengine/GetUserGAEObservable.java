package es.us.context4learning.observable.appengine;

import java.io.IOException;

import es.us.context4learning.backend.userApi.UserApi;
import es.us.context4learning.backend.userApi.model.User;
import es.us.context4learning.exception.UserNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;

public class GetUserGAEObservable implements Observable.OnSubscribe<User> {

    private static final String TAG = GetUserGAEObservable.class.getCanonicalName();

    private UserApi mGaeUserApi;
    private Long mUserId;

    public GetUserGAEObservable(UserApi userApi, Long userId) {
        this.mGaeUserApi = userApi;
        this.mUserId = userId;
    }

    @Override
    public void call(Subscriber<? super User> subscriber) {
        try {
            User user = mGaeUserApi.get(mUserId).execute();
            subscriber.onNext(user);
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(new UserNotFoundGAEException());
        }
    }
}