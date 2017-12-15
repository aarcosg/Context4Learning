package es.us.context4learning.observable.appengine;

import android.util.Log;

import java.io.IOException;

import es.us.context4learning.backend.auditEventApi.AuditEventApi;
import es.us.context4learning.backend.auditEventApi.model.AuditEvent;
import rx.Observable;
import rx.Subscriber;

public class InsertAuditGAEObservable implements Observable.OnSubscribe<AuditEvent> {

    private static final String TAG = InsertAuditGAEObservable.class.getCanonicalName();

    private AuditEventApi mAuditEventApi;
    private AuditEvent mAuditEvent;

    public InsertAuditGAEObservable(AuditEventApi auditEventApi, AuditEvent auditEvent) {
        this.mAuditEventApi = auditEventApi;
        this.mAuditEvent = auditEvent;
    }

    @Override
    public void call(Subscriber<? super AuditEvent> subscriber) {
        try {
            AuditEvent newAuditEvent = mAuditEventApi.insert(mAuditEvent).execute();
            subscriber.onNext(newAuditEvent);
            subscriber.onCompleted();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            subscriber.onCompleted();
        }
    }
}