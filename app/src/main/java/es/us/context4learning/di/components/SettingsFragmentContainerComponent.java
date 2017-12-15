package es.us.context4learning.di.components;

import dagger.Component;
import es.us.context4learning.di.modules.ActivityModule;
import es.us.context4learning.di.scopes.PerActivity;
import es.us.context4learning.ui.activity.SettingsFragmentContainerActivity;
import es.us.context4learning.ui.fragment.LimitNotificationsFragment;
import es.us.context4learning.ui.fragment.LocationRestrictionFragment;
import es.us.context4learning.ui.fragment.TimeRestrictionFragment;

@PerActivity
@Component(
        dependencies = ApplicationComponent.class,
        modules = {ActivityModule.class})
public interface SettingsFragmentContainerComponent extends ActivityComponent{

    void inject(SettingsFragmentContainerActivity settingsFragmentContainerActivity);
    void inject(TimeRestrictionFragment timeRestrictionFragment);
    void inject(LocationRestrictionFragment locationRestrictionFragment);
    void inject(LimitNotificationsFragment limitNotificationsFragment);

}