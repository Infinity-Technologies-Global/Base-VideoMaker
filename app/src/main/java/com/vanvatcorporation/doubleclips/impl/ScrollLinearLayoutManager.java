package com.vanvatcorporation.doubleclips.impl;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;

public class ScrollLinearLayoutManager extends LinearLayoutManager {
    private boolean isScrollEnabled = true;
    public ScrollLinearLayoutManager(Context context) {
        super(context);
    }
    public ScrollLinearLayoutManager(Context context, boolean isScrollEnabled) {
        super(context);

        this.isScrollEnabled = isScrollEnabled;
    }
    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }
    @Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }
    @Override
    public boolean canScrollHorizontally() {
        return isScrollEnabled && super.canScrollHorizontally();
    }
}