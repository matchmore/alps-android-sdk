package io.matchmore.sdk

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import io.matchmore.config.SdkConfigTest
import io.matchmore.sdk.api.ApiClient
import net.jodah.concurrentunit.Waiter
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLocationManager
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, shadows = [ShadowLocationManager::class])
abstract class BaseTest {

    // unfortunately we can't move that method to @BeforeClass because robolectric RuntimeEnvironment.application is still null there
    fun initAndStartUsingMainDevice() {
        init()
        Matchmore.instance.startUsingMainDevice({ _ ->
            waiter.assertEquals(1, Matchmore.instance.devices.findAll().size)
            waiter.resume()
        }, waiter::fail)
        waiter.await(SdkConfigTest.TIMEOUT)
    }

    fun init() {
        if (!Matchmore.isConfigured()) {
            ApiClient.config.callbackInUIThread = false
            Matchmore.config(RuntimeEnvironment.application, SdkConfigTest.API_KEY, true)
        }
        removeSubscriptions()
        removePublications()
        removeDevices()
    }

    private fun removePublications() {
        Matchmore.instance.publications.deleteAll({
            waiter.assertEquals(0, Matchmore.instance.publications.findAll().size)
            waiter.resume()
        }, waiter::fail)
        waiter.await(SdkConfigTest.TIMEOUT)
    }

    private fun removeSubscriptions() {
        Matchmore.instance.subscriptions.deleteAll({
            waiter.assertEquals(0, Matchmore.instance.subscriptions.findAll().size)
            waiter.resume()
        }, waiter::fail)
        waiter.await(SdkConfigTest.TIMEOUT)
    }

    private fun removeDevices() {
        Matchmore.instance.devices.deleteAll({
            waiter.assertEquals(0, Matchmore.instance.devices.findAll().size)
            waiter.resume()
        }, waiter::fail)
        waiter.await(SdkConfigTest.TIMEOUT)
    }

    companion object {
        val waiter = Waiter()

        @BeforeClass
        @JvmStatic
        fun setUp() {
            ShadowLog.stream = System.out
        }

        @JvmStatic
        fun mockLocation() {
            Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            val locationManager = RuntimeEnvironment.application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val shadowLocationManager = Shadows.shadowOf(locationManager)
            val location = Location(LocationManager.GPS_PROVIDER).apply {
                latitude = 54.414663
                longitude = 18.625499
                time = System.currentTimeMillis()
            }
            shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, location)
        }
    }
}