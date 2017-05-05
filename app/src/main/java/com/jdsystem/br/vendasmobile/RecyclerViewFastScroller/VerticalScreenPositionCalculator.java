package com.jdsystem.br.vendasmobile.RecyclerViewFastScroller;

/**
 * Created by Usuário on 04/05/2017.
 */

public class VerticalScreenPositionCalculator {

    private final VerticalScrollBoundsProvider mVerticalScrollBoundsProvider;

    public VerticalScreenPositionCalculator(VerticalScrollBoundsProvider scrollBoundsProvider) {
        mVerticalScrollBoundsProvider = scrollBoundsProvider;
    }

    public float getYPositionFromScrollProgress(float scrollProgress) {
        return Math.max(
                mVerticalScrollBoundsProvider.getMinimumScrollY(),
                Math.min(
                        scrollProgress * mVerticalScrollBoundsProvider.getMaximumScrollY(),
                        mVerticalScrollBoundsProvider.getMaximumScrollY()
                )
        );
    }

}
