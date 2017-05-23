package com.jdsystem.br.vendasmobile.RecyclerViewFastScroller;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Usuário on 04/05/2017.
 */

public interface ScrollProgressCalculator {

    /**
     * Calculates the scroll progress of a provided RecyclerView
     *
     * @param recyclerView for which to calculate scroll progress
     * @return fraction from [0 to 1] representing the scroll progress
     */
    float calculateScrollProgress(RecyclerView recyclerView);

}
