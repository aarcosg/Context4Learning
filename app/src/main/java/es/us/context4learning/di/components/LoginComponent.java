package es.us.context4learning.di.components;

import dagger.Component;
import es.us.context4learning.di.scopes.PerActivity;
import es.us.context4learning.di.modules.ActivityModule;
import es.us.context4learning.ui.activity.LoginActivity;

@PerActivity
@Component(
        dependencies = ApplicationComponent.class,
        modules = {ActivityModule.class})
public interface LoginComponent extends ActivityComponent{

    void inject(LoginActivity loginActivity);

}