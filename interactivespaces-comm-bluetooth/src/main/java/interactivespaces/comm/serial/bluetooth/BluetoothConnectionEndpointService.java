/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.comm.serial.bluetooth;

/**
 * A service for bluetooth connections.
 * 
 * @author Keith M. Hughes
 */
public interface BluetoothConnectionEndpointService {

	/**
	 * Get a bluetooth endpoint which uses separate ports for sending and
	 * receiving.
	 * 
	 * @param address
	 *            the bluetooth address
	 * @param WII_REMOTE_RECEIVE_PORT
	 *            the port for sending
	 * @param WII_REMOTE_SEND_PORT
	 *            the port for receiving
	 * 
	 * @return the dual port
	 */
	BluetoothConnectionEndpoint newDualEndpoint(String address,
			int receivePort, int sendPort);
}