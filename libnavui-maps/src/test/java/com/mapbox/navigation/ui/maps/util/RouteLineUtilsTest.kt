package com.mapbox.navigation.ui.maps.util

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.navigation.ui.maps.util.RouteLineUtils.updatePrimaryRoute
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteLineUtilsTest {

    @Test
    fun updatePrimaryRoute() {
        val route1 = mockk<DirectionsRoute>()
        val route2 = mockk<DirectionsRoute>()
        val routes = listOf(route1, route2)

        val updatedRoutes = routes.updatePrimaryRoute(route2)

        assertEquals(route2, updatedRoutes.first())
        assertEquals(route1, updatedRoutes.last())
    }

    @Test
    fun updatePrimaryRoute_whenPrimaryRouteNotInCollection() {
        val route1 = mockk<DirectionsRoute>()
        val route2 = mockk<DirectionsRoute>()
        val route3 = mockk<DirectionsRoute>()
        val routes = listOf(route1, route2)

        val updatedRoutes = routes.updatePrimaryRoute(route3)

        assertEquals(route3, updatedRoutes.first())
        assertEquals(3, updatedRoutes.size)
    }
}
