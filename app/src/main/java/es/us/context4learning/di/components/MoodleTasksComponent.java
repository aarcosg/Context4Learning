package es.us.context4learning.di.components;

import dagger.Component;
import es.us.context4learning.ui.activity.MoodleTasksActivity;
import es.us.context4learning.di.modules.ActivityModule;
import es.us.context4learning.di.scopes.PerActivity;
import es.us.context4learning.ui.fragment.CompletedTasksFragment;
import es.us.context4learning.ui.fragment.PendingTasksFragment;

@PerActivity
@Component(
        dependencies = ApplicationComponent.class,
        modules = {ActivityModule.class})
public interface MoodleTasksComponent extends ActivityComponent{

    void inject(MoodleTasksActivity moodleTasksActivity);
    void inject(PendingTasksFragment pendingTasksFragment);
    void inject(CompletedTasksFragment completedTasksFragment);

}