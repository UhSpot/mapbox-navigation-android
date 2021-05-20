package com.mapbox.navigation.core

import com.mapbox.navigation.base.options.RoutingTilesOptions

/**
 *  An interface which enables listening to navigation tiles version switch.
 */
interface NavigationVersionSwitchObserver {

    /**
     * Invoked as soon as navigation switched to a fallback tiles version.
     *
     * @param tilesVersion tiles version used for navigation.
     */
    fun onSwitchToFallbackVersion(tilesVersion: String)

    /**
     * Invoked as soon as navigation switched to a tiles version specified in [RoutingTilesOptions]
     */
    fun onSwitchToTargetVersion()
}
