package com.mapbox.navigation.ui.maps.util

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.navigation.core.directions.session.RoutesObserver

/**
 * Extension functions and utilities for route line related operations.
 */
object RouteLineUtils {

    /**
     * An extension function to reorder a collection of [DirectionsRoute] objects so that the
     * individual route provided is the first in the collection indicating it is the primary route.
     *
     * This function can be useful when the user is selecting alternate routes. The order of
     * operations would be to:
     *
     * Find the route selected via [MapboxRouteLineApi.findClosestRoute].
     * Use this function to reorder the routes from [MapboxRouteLineApi.getRoutes].
     * Then call [MapboxNavigation.setRoutes] to update the navigation core.
     *
     * Note: In many cases a [RoutesObserver] will be utilized in an activity or fragment which
     * will include a call to [MapboxcRouteLineApi.setRoutes]. This call will update the state
     * of the [MapboxcRouteLineApi] and rendering the result will update the route lines displayed
     * on the map.
     *
     * If your application is not using a [RoutesObserver] to set the routes in [MapboxcRouteLineApi]
     * then you should call [MapboxcRouteLineApi.setRoutes] in your activity or fragment with the
     * collection returned by this function.
     *
     * @param route the route that should appear first in the collection indicating it is the
     * primary route. If the route is not already in the collection it will be added as the first
     * route in the collection.
     */
    @JvmStatic
    fun List<DirectionsRoute>.updatePrimaryRoute(route: DirectionsRoute): List<DirectionsRoute> {
        return this.filter { it != route }.toMutableList().also {
            it.add(0, route)
        }
    }
}
