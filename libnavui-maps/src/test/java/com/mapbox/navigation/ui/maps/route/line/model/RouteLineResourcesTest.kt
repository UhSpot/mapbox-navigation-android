package com.mapbox.navigation.ui.maps.route.line.model

import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.navigation.testing.BuilderTest
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.reflect.KClass

class RouteLineResourcesTest : BuilderTest<RouteLineResources, RouteLineResources.Builder>() {

    override fun getImplementationClass(): KClass<RouteLineResources> = RouteLineResources::class

    override fun getFilledUpBuilder(): RouteLineResources.Builder {
        val backFill = listOf<String>("foobar")
        val routeLineExpression = mockk<Expression>()
        val routeCasingExpression = mockk<Expression>()
        val trafficExpression = mockk<Expression>()
        val routeLineColorResources = RouteLineColorResources.Builder()
            .routeLineTraveledColor(1)
            .routeLineTraveledCasingColor(2)
            .routeUnknownTrafficColor(3)
            .routeDefaultColor(4)
            .routeLowCongestionColor(5)
            .routeModerateColor(6)
            .routeHeavyColor(7)
            .routeSevereColor(8)
            .routeCasingColor(9)
            .alternativeRouteUnknownTrafficColor(10)
            .alternativeRouteDefaultColor(11)
            .alternativeRouteLowColor(12)
            .alternativeRouteModerateColor(13)
            .alternativeRouteHeavyColor(14)
            .alternativeRouteSevereColor(15)
            .alternativeRouteCasingColor(16)
            .build()

        return RouteLineResources.Builder()
            .routeLineColorResources(routeLineColorResources)
            .roundedLineCap(false)
            .originWaypointIcon(17)
            .destinationWaypointIcon(18)
            .trafficBackfillRoadClasses(backFill)
            .routeLineScaleExpression(routeLineExpression)
            .routeCasingLineScaleExpression(routeCasingExpression)
            .routeTrafficLineScaleExpression(trafficExpression)
            .alternativeRouteLineScaleExpression(routeCasingExpression)
            .alternativeRouteCasingLineScaleExpression(routeCasingExpression)
            .alternativeRouteTrafficLineScaleExpression(routeCasingExpression)
    }

    override fun trigger() {
        //
    }

    @Test
    fun withRoundedLineCap() {
        val resources = RouteLineResources.Builder().roundedLineCap(false).build()

        assertFalse(resources.roundedLineCap)
    }

    @Test
    fun withOriginWaypointIcon() {
        val resources = RouteLineResources.Builder()
            .originWaypointIcon(5)
            .build()

        assertEquals(5, resources.originWaypointIcon)
    }

    @Test
    fun withDestinationWaypointIcon() {
        val resources = RouteLineResources.Builder()
            .destinationWaypointIcon(5)
            .build()

        assertEquals(5, resources.destinationWaypointIcon)
    }

    @Test
    fun withTrafficBackfillRoadClasses() {
        val roadClasses = listOf<String>()

        val resources = RouteLineResources.Builder()
            .trafficBackfillRoadClasses(roadClasses)
            .build()

        assertEquals(roadClasses, resources.trafficBackfillRoadClasses)
    }

    @Test
    fun withRouteLineScaleValues() {
        val expression = Expression.ExpressionBuilder("").build()

        val resources = RouteLineResources.Builder()
            .routeLineScaleExpression(expression)
            .build()

        assertEquals(expression, resources.routeLineScaleExpression)
    }

    @Test
    fun withRouteLineCasingScaleValues() {
        val expression = Expression.ExpressionBuilder("").build()

        val resources = RouteLineResources.Builder()
            .routeCasingLineScaleExpression(expression)
            .build()

        assertEquals(expression, resources.routeCasingLineScaleExpression)
    }

    @Test
    fun withRouteLineTrafficScaleValues() {
        val expression = Expression.ExpressionBuilder("").build()

        val resources = RouteLineResources.Builder()
            .routeTrafficLineScaleExpression(expression)
            .build()

        assertEquals(expression, resources.routeTrafficLineScaleExpression)
    }

    @Test
    fun toBuilder() {
        val backFill = listOf<String>()
        val routeLineExpression = mockk<Expression>()
        val routeCasingExpression = mockk<Expression>()
        val trafficExpression = mockk<Expression>()
        val routeLineColorResources = RouteLineColorResources.Builder().build()

        val result = RouteLineResources.Builder()
            .routeLineColorResources(routeLineColorResources)
            .roundedLineCap(false)
            .originWaypointIcon(17)
            .destinationWaypointIcon(18)
            .trafficBackfillRoadClasses(backFill)
            .routeLineScaleExpression(routeLineExpression)
            .routeCasingLineScaleExpression(routeCasingExpression)
            .routeTrafficLineScaleExpression(trafficExpression)
            .alternativeRouteLineScaleExpression(trafficExpression)
            .alternativeRouteCasingLineScaleExpression(trafficExpression)
            .alternativeRouteTrafficLineScaleExpression(trafficExpression)
            .build()
            .toBuilder()
            .build()

        assertFalse(result.roundedLineCap)
        assertEquals(17, result.originWaypointIcon)
        assertEquals(18, result.destinationWaypointIcon)
        assertEquals(backFill, result.trafficBackfillRoadClasses)
        assertEquals(routeLineExpression, result.routeLineScaleExpression)
        assertEquals(routeCasingExpression, result.routeCasingLineScaleExpression)
        assertEquals(trafficExpression, result.routeTrafficLineScaleExpression)
    }
}
