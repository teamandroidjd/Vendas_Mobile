package com.jdsystem.br.vendasmobile.RecyclerViewFastScroller;

import android.view.MotionEvent;

/**
 * Created by Usuário on 04/05/2017.
 */

public interface TouchableScrollProgressCalculator extends ScrollProgressCalculator {

    /**
     * Calculates the scroll progress of a RecyclerView based on a motion event from a scroller
     * @param event for which to calculate scroll progress
     * @return fraction from [0 to 1] representing the scroll progress
     */
    public float calculateScrollProgress(MotionEvent event);

}
