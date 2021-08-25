package com.ynsuper.slideshowver1.callback;

public interface FilterListener {
    void onFilterSelected(int filterEffect);

    default void onSelectPackageFilter(int position) {

    }

    void onBackToGroup();
}