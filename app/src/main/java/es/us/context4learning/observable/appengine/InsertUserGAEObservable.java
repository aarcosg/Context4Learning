package es.us.context4learning.observable.appengine;

import java.io.IOException;

import es.us.context4learning.backend.userApi.UserApi;
import es.us.context4learning.backend.userApi.model.User;
import es.us.context4learning.exception.UserNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;

public class InsertUserGAEObservable implements Observable.OnSubscribe<User> {

    private UserApi mGaeUserApi;
    private User mGaeUser;

    public InsertUserGAEObservable(UserApi userApi, User user) {
        this.mGaeUserApi = userApi;
        this.mGaeUser = user;
    }

    @Override
    public void call(Subscriber<? super User> subscriber) {
        try {
            User newUser = mGaeUserApi.insert(mGaeUser).execute();
            subscriber.onNext(newUser);
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(new UserNotFoundGAEException());
        }
    }
}