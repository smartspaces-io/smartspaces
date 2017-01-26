package io.smartspaces.api.osgi.service;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A collection of OSGi service trackers that can notify when all services have been located.
 * 
 * @author Keith M. Hughes
 */
public class OsgiServiceTrackerCollection {

  /**
   * The bundle context the collection is associated with.
   */
  private final BundleContext bundleContext;
  
  /**
   * The listener for when all services have been found.
   */
  private final OsgiServiceTrackerCollectionListener listener;

  /**
   * All service trackers we have.
   */
  private final Map<String, MyServiceTracker<?>> serviceTrackers = new HashMap<>();

  /**
   * Object to give lock for checking for all tracked services.
   */
  private final Object serviceLock = new Object();

  /**
   * Construct a new collection.
   * 
   * @param bundleContext
   *          the bundle context the collection is associated with
   * @param listener
   *          the listener for when all services have been found
   */
  public OsgiServiceTrackerCollection(BundleContext bundleContext, OsgiServiceTrackerCollectionListener listener) {
    this.bundleContext = bundleContext;
    this.listener = listener;
  }

  /**
   * Create a new service tracker.
   *
   * @param serviceName
   *          name of the service class
   * @param <T>
   *          class being tracked by the service tracker
   *
   * @return the service tracker
   */
  public <T> MyServiceTracker<T> newMyServiceTracker(String serviceName) {
    MyServiceTracker<T> tracker = new MyServiceTracker<T>(bundleContext, serviceName);

    serviceTrackers.put(serviceName, tracker);

    return tracker;
  }

  /**
   * Start all registered trackers.
   */
  public void startTracking() {
    // Open all the trackers.
    for (MyServiceTracker<?> tracker : serviceTrackers.values()) {
      tracker.open();
    }
  }

  /**
   * Stop all tracking.
   */
  public void stopTracking() {
    // Close all the trackers.
    for (MyServiceTracker<?> tracker : serviceTrackers.values()) {
      tracker.close();
    }
    serviceTrackers.clear();
  }

  /**
   * Got another reference from an OSGi service tracker.
   */
  private void gotAnotherReference() {
    synchronized (serviceLock) {
      // If missing any of our needed services, punt.
      for (MyServiceTracker<?> tracker : serviceTrackers.values()) {
        if (tracker.getMyService() == null) {
          return;
        }
      }

      listener.handleAllRequiredServicesAvailable();
    }
  }

  /**
   * An OSGi service tracking class.
   *
   * @param <T>
   *          the class of the service being tracked
   *
   * @author Keith M. Hughes
   */
  public class MyServiceTracker<T> extends ServiceTracker {

    /**
     * The reference for the service object being waited for.
     */
    private AtomicReference<T> serviceReference = new AtomicReference<T>();

    /**
     * Construct a service tracker.
     *
     * @param context
     *          bundle context the tracker is running under
     * @param serviceName
     *          the name of the service
     */
    public MyServiceTracker(BundleContext context, String serviceName) {
      super(context, serviceName, null);
    }

    @Override
    public Object addingService(ServiceReference reference) {
      @SuppressWarnings("unchecked")
      T service = (T) super.addingService(reference);

      if (serviceReference.compareAndSet(null, service)) {
        gotAnotherReference();
      }

      return service;
    }

    /**
     * Get the service needed.
     *
     * @return the service, or {@code null} if it hasn't been obtained yet.
     */
    public T getMyService() {
      return serviceReference.get();
    }
  }
  
  /**
   * A listener for notifying when all services have become available.
   * 
   * @author Keith M. Hughes
   */
  public interface OsgiServiceTrackerCollectionListener {
    
    /**
     * All required services are now available.
     */
    void handleAllRequiredServicesAvailable();
  }
}
