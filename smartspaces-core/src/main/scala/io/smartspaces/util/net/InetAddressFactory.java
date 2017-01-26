/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.smartspaces.util.net;

import io.smartspaces.SmartSpacesException;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A factory for inet addresses.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class InetAddressFactory {
  
  public static final String LOOPBACK = "127.0.0.1";
  
  public static final String LOCALHOST = "localhost";

  private static boolean isIpv4(InetAddress address) {
    return address.getAddress().length == 4;
  }

  private static Collection<InetAddress> getAllInetAddresses() {
    List<NetworkInterface> networkInterfaces;
    try {
      networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
    } catch (SocketException e) {
      throw new SmartSpacesException("Can't get all inet addresses", e);
    }
    List<InetAddress> inetAddresses = Lists.newArrayList();
    for (NetworkInterface networkInterface : networkInterfaces) {
      try {
        if (networkInterface.isUp()) {
          inetAddresses.addAll(Collections.list(networkInterface.getInetAddresses()));
        }
      } catch (SocketException e) {
        throw new SmartSpacesException("Can't get all inet addresses", e);
      }
    }
    return inetAddresses;
  }

  private static InetAddress filterInetAddresses(Collection<InetAddress> inetAddresses) {
    for (InetAddress address : inetAddresses) {
      // IPv4 only for now.
      if (!address.isLoopbackAddress() && isIpv4(address)) {
        return address;
      }
    }
    throw new SmartSpacesException("No non-loopback interface found.");
  }

  public static InetAddress newNonLoopback() {
    return filterInetAddresses(getAllInetAddresses());
  }

  public static InetAddress newNonLoopbackForNetworkInterface(NetworkInterface networkInterface) {
    return filterInetAddresses(Collections.list(networkInterface.getInetAddresses()));
  }

  private static Collection<InetAddress> getAllInetAddressesByName(String host) {
    InetAddress[] allAddressesByName;
    try {
      allAddressesByName = org.xbill.DNS.Address.getAllByName(host);
    } catch (UnknownHostException unused) {
      try {
        allAddressesByName = InetAddress.getAllByName(host);
      } catch (UnknownHostException e) {
        throw new SmartSpacesException("Can't get all inet addresses by name", e);
      }
    }
    return Arrays.asList(allAddressesByName);
  }

  /**
   * Creates an {@link InetAddress} with both an IP and a host set so that no
   * further resolving will take place.
   *
   * If an IP address string is specified, this method ensures that it will be
   * used in place of a host name.
   *
   * If a host name other than {@code Address.LOCALHOST} is specified, this
   * method trys to find a non-loopback IP associated with the supplied host
   * name.
   *
   * If the specified host name is {@code Address.LOCALHOST}, this method
   * returns a loopback address.
   *
   * @param host
   * @return an {@link InetAddress} with both an IP and a host set (no further
   *         resolving will take place)
   */
  public static InetAddress newFromHostString(String host) {
    try {
      if (InetAddresses.isInetAddress(host)) {
        return InetAddress.getByAddress(host, InetAddresses.forString(host).getAddress());
      }
      if (host.equals(LOCALHOST)) {
        return InetAddress.getByAddress(LOCALHOST, InetAddresses.forString(LOOPBACK).getAddress());
      }
    } catch (UnknownHostException e) {
      throw new SmartSpacesException("Can't get new host from host name " + host, e);
    }
    Collection<InetAddress> allAddressesByName = getAllInetAddressesByName(host);
    // First, try to find a non-loopback IPv4 address.
    for (InetAddress address : allAddressesByName) {
      if (!address.isLoopbackAddress() && isIpv4(address)) {
        return address;
      }
    }
    // Return a loopback IPv4 address as a last resort.
    for (InetAddress address : allAddressesByName) {
      if (isIpv4(address)) {
        return address;
      }
    }
    throw new SmartSpacesException("Unable to construct InetAddress for host: " + host);
  }

  public static InetAddress newLoopback() {
    return newFromHostString(LOOPBACK);
  }

}
