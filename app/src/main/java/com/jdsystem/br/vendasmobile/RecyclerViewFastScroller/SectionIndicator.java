package com.jdsystem.br.vendasmobile.RecyclerViewFastScroller;

/**
 * Created by Usuário on 04/05/2017.
 */

public interface SectionIndicator<T> {

    /**
     * Sets the progress of the indicator
     *
     * @param progress fraction from [0 to 1] representing progress scrolled through a RecyclerView
     */
    void setProgress(float progress);

    /**
     * Allows the setting of section types in the indicator. The indicator should appropriately handle the section type
     *
     * @param section the current section to which the list is scrolled
     */
    void setSection(T section);

    /**
     * Method for animating the alpha of the indicator
     *
     * @param targetAlpha alpha to animate towards
     */
    void animateAlpha(float targetAlpha);
}
