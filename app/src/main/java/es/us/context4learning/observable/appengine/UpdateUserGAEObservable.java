package es.us.context4learning.observable.appengine;

import java.io.IOException;

import es.us.context4learning.backend.userApi.UserApi;
import es.us.context4learning.backend.userApi.model.User;
import es.us.context4learning.exception.UserNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;

public class UpdateUserGAEObservable implements Observable.OnSubscribe<User> {

    private static final String TAG = UpdateUserGAEObservable.class.getCanonicalName();

    private UserApi mGaeUserApi;
    private Long mUserId;
    private User mGaeUser;

    public UpdateUserGAEObservable(UserApi userApi, Long userId, User user) {
        this.mGaeUserApi = userApi;
        this.mUserId = userId;
        this.mGaeUser = user;
    }

    @Override
    public void call(Subscriber<? super User> subscriber) {
        try {
            User updatedUser = mGaeUserApi.update(mUserId,mGaeUser).execute();
            subscriber.onNext(updatedUser);
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(new UserNotFoundGAEException());
        }
    }
}