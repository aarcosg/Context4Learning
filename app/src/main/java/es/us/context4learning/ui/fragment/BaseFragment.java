package es.us.context4learning.ui.fragment;

import android.support.v4.app.Fragment;

import es.us.context4learning.di.HasComponent;

public abstract class BaseFragment extends Fragment {

    @SuppressWarnings("unchecked")
    protected <C> C getComponent(Class<C> componentType){
        return componentType.cast(((HasComponent<C>) getActivity()).getComponent());
    }
}