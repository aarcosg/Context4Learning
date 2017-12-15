package es.us.context4learning.di.components;

import dagger.Component;
import es.us.context4learning.di.modules.ActivityModule;
import es.us.context4learning.di.scopes.PerActivity;
import es.us.context4learning.ui.activity.MapActivity;

@PerActivity
@Component(
        dependencies = ApplicationComponent.class,
        modules = {ActivityModule.class})
public interface MapComponent extends ActivityComponent{

    void inject(MapActivity mapActivity);

}