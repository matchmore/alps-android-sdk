package io.matchmore.sdk;

import io.matchmore.CollectionFormats.*;



import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;

import io.matchmore.sdk.models.APIError;
import io.matchmore.sdk.models.Subscription;
import io.matchmore.sdk.models.Subscriptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface SubscriptionApi {
  /**
   * Create a subscription for a device
   * 
   * @param deviceId The id (UUID) of the device.  (required)
   * @param subscription Subscription to create on a device.  (required)
   * @return Call&lt;Subscription&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("devices/{deviceId}/subscriptions")
  Call<Subscription> createSubscription(
    @retrofit2.http.Path("deviceId") String deviceId, @retrofit2.http.Body Subscription subscription
  );

  /**
   * Delete a Subscription
   * 
   * @param deviceId The id (UUID) of the device. (required)
   * @param subscriptionId The id (UUID) of the subscription. (required)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @DELETE("devices/{deviceId}/subscriptions/{subscriptionId}")
  Call<Void> deleteSubscription(
    @retrofit2.http.Path("deviceId") String deviceId, @retrofit2.http.Path("subscriptionId") String subscriptionId
  );

  /**
   * Info about a subscription on a device
   * 
   * @param deviceId The id (UUID) of the device. (required)
   * @param subscriptionId The id (UUID) of the subscription. (required)
   * @return Call&lt;Subscription&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @GET("devices/{deviceId}/subscriptions/{subscriptionId}")
  Call<Subscription> getSubscription(
    @retrofit2.http.Path("deviceId") String deviceId, @retrofit2.http.Path("subscriptionId") String subscriptionId
  );

  /**
   * Get all subscriptions for a device
   * 
   * @param deviceId The id (UUID) of the device. (required)
   * @return Call&lt;Subscriptions&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @GET("devices/{deviceId}/subscriptions")
  Call<Subscriptions> getSubscriptions(
    @retrofit2.http.Path("deviceId") String deviceId
  );

}
