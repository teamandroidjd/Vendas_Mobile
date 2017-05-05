package com.jdsystem.br.vendasmobile.RecyclerViewFastScroller;

import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Usuário on 04/05/2017.
 */

class FastScrollerTouchListener implements View.OnTouchListener {

    private final AbsRecyclerViewFastScroller mFastScroller;

    /**
     * @param fastScroller {@link xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller} for this listener to scroll
     */
    public FastScrollerTouchListener(AbsRecyclerViewFastScroller fastScroller) {
        mFastScroller = fastScroller;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        SectionIndicator sectionIndicator = mFastScroller.getSectionIndicator();
        showOrHideIndicator(sectionIndicator, event);

        float scrollProgress = mFastScroller.getScrollProgress(event);
        mFastScroller.scrollTo(scrollProgress, true);
        mFastScroller.moveHandleToPosition(scrollProgress);
        return true;
    }

    private void showOrHideIndicator(@Nullable SectionIndicator sectionIndicator, MotionEvent event) {
        if (sectionIndicator == null) {
            return;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                sectionIndicator.animateAlpha(1f);
                return;
            case MotionEvent.ACTION_UP:
                sectionIndicator.animateAlpha(0f);
        }
    }

}
