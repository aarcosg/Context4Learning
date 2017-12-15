package es.us.context4learning.di.components;

import android.app.Activity;

import dagger.Component;
import es.us.context4learning.di.modules.ActivityModule;
import es.us.context4learning.di.scopes.PerActivity;

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    Activity getActivity();

}