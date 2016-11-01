/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

package io.smartspaces.time;

import java.util.concurrent.TimeUnit;

/**
 * A frequency with which things should be repeated.
 *
 * @author Keith M. Hughes
 */
public class TimeFrequency {

  /**
   * One second in milliseconds.
   */
  public static final double ONE_SECOND_IN_MILLISECONDS = 1000.0;

  /**
   * One minute in milliseconds.
   */
  public static final double ONE_MINUTE_IN_MILLISECONDS = 60.0 * ONE_SECOND_IN_MILLISECONDS;

  /**
   * One hour in milliseconds.
   */
  public static final double ONE_HOUR_IN_MILLISECONDS = 60.0 * ONE_MINUTE_IN_MILLISECONDS;

  /**
   * Get a frequency for times per second.
   *
   * @param timesPerSecond
   *          the times per second desired
   *
   * @return the frequency in times per second
   */
  public static TimeFrequency timesPerSecond(double timesPerSecond) {
    return new TimeFrequency((long) (ONE_SECOND_IN_MILLISECONDS / timesPerSecond),
        TimeUnit.MILLISECONDS);
  }

  /**
   * Get a frequency for times per minute.
   *
   * @param timesPerMinute
   *          the times per minute desired
   *
   * @return the frequency in times per minute
   */
  public static TimeFrequency timesPerMinute(double timesPerMinute) {
    return new TimeFrequency((long) (ONE_MINUTE_IN_MILLISECONDS / timesPerMinute),
        TimeUnit.MILLISECONDS);
  }

  /**
   * Get a frequency for times per hour.
   *
   * @param timesPerMinute
   *          the times per hour desired
   *
   * @return the frequency in times per hour
   */
  public static TimeFrequency timesPerHour(double timesPerHour) {
    return new TimeFrequency((long) (ONE_HOUR_IN_MILLISECONDS / timesPerHour),
        TimeUnit.MILLISECONDS);
  }

  /**
   * How frequently things should be repeated.
   */
  private final long period;

  /**
   * The time unit for the period.
   */
  private final TimeUnit unit;

  /**
   * Construct a new frequency.
   *
   * @param period
   *          how often things will be repeated
   * @param unit
   *          the time units for the period
   */
  public TimeFrequency(long period, TimeUnit unit) {
    this.period = period;
    this.unit = unit;
  }

  /**
   * Get the period.
   *
   * @return the period
   */
  public long getPeriod() {
    return period;
  }

  /**
   * Get the time unit of the period.
   *
   * @return the time unit
   */
  public TimeUnit getUnit() {
    return unit;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (period ^ (period >>> 32));
    result = prime * result + unit.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TimeFrequency other = (TimeFrequency) obj;
    if (period != other.period) {
      return false;
    }
    if (unit != other.unit) {
      return false;
    }

    return true;
  }
}
