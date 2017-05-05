package com.jdsystem.br.vendasmobile.RecyclerViewFastScroller;

import android.view.MotionEvent;

/**
 * Created by Usuário on 04/05/2017.
 */

public abstract class VerticalScrollProgressCalculator implements TouchableScrollProgressCalculator {

    private final VerticalScrollBoundsProvider mScrollBoundsProvider;

    public VerticalScrollProgressCalculator(VerticalScrollBoundsProvider scrollBoundsProvider) {
        mScrollBoundsProvider = scrollBoundsProvider;
    }

    @Override
    public float calculateScrollProgress(MotionEvent event) {
        float y = event.getY();

        if (y <= mScrollBoundsProvider.getMinimumScrollY()) {
            return 0;
        } else if (y >= mScrollBoundsProvider.getMaximumScrollY()) {
            return 1;
        } else {
            return y / mScrollBoundsProvider.getMaximumScrollY();
        }
    }
}
