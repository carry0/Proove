package com.proove.smart.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

public abstract class BaseFragment<T extends ViewBinding> extends Fragment {
    protected final String TAG = this.getClass().getSimpleName();

    protected T binding;

    /**
     * 是否执行赖加载
     */
    private boolean isLoaded = false;

    private boolean isVisibleToUser = false;

    private boolean isCallResume = false;

    private boolean isCallUserVisibleHint = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = getViewBinding(inflater, container);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
        initListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        isCallResume = true;
        if (!isCallUserVisibleHint) isVisibleToUser = !isHidden();

    }

    private void judgeLazyInit() {
        if (!isLoaded && isVisibleToUser && isCallResume) {
            lazyInit();
            isLoaded = true;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        isVisibleToUser = !hidden;
        judgeLazyInit();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        isCallUserVisibleHint = true;
        judgeLazyInit();
    }

    protected abstract T getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void initListener();

    protected abstract void lazyInit();


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isLoaded = false;
        isVisibleToUser = false;
        isCallUserVisibleHint = false;
        isCallResume = false;
        binding = null;

    }
}
