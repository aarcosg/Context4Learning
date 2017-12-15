package es.us.context4learning.di.components;

import dagger.Component;
import es.us.context4learning.di.scopes.PerActivity;
import es.us.context4learning.ui.activity.MainActivity;
import es.us.context4learning.ui.fragment.CompletedTasksFragment;
import es.us.context4learning.ui.fragment.MoodleCoursesFragment;
import es.us.context4learning.di.modules.ActivityModule;
import es.us.context4learning.ui.fragment.PendingTasksFragment;

@PerActivity
@Component(
        dependencies = ApplicationComponent.class,
        modules = {ActivityModule.class})
public interface MainComponent extends ActivityComponent{

    void inject(MainActivity mainActivity);
    void inject(PendingTasksFragment pendingTasksFragment);
    void inject(MoodleCoursesFragment moodleCoursesFragment);
    void inject(CompletedTasksFragment completedTasksFragment);

}