package com.jdsystem.br.vendasmobile.RecyclerViewFastScroller;

/**
 * Created by Usuário on 04/05/2017.
 */

public class VerticalScrollBoundsProvider {

    private final float mMinimumScrollY;
    private final float mMaximumScrollY;

    public VerticalScrollBoundsProvider(float minimumScrollY, float maximumScrollY) {
        mMinimumScrollY = minimumScrollY;
        mMaximumScrollY = maximumScrollY;
    }

    public float getMinimumScrollY() {
        return mMinimumScrollY;
    }

    public float getMaximumScrollY() {
        return mMaximumScrollY;
    }
}
