package io.smartspaces.service.comm.network;

import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpoint;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpointListener;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpointService;
import io.smartspaces.service.comm.network.client.internal.netty.NettyTcpClientNetworkCommunicationEndpointService;
import io.smartspaces.service.comm.network.server.TcpServerClientConnection;
import io.smartspaces.service.comm.network.server.TcpServerNetworkCommunicationEndpoint;
import io.smartspaces.service.comm.network.server.TcpServerNetworkCommunicationEndpointListener;
import io.smartspaces.service.comm.network.server.TcpServerNetworkCommunicationEndpointService;
import io.smartspaces.service.comm.network.server.TcpServerRequest;
import io.smartspaces.service.comm.network.server.internal.netty.NettyTcpServerNetworkCommunicationEndpointService;
import io.smartspaces.system.StandaloneSmartSpacesEnvironment;
import io.smartspaces.util.SmartSpacesUtilities;

import com.google.common.base.Charsets;

import java.net.InetAddress;

public class Test {
  public static void main(String[] args) throws Exception {
    StandaloneSmartSpacesEnvironment spaceEnvironment = StandaloneSmartSpacesEnvironment.newStandaloneSmartSpacesEnvironment();
    try {
   
    spaceEnvironment.registerAndStartService(new NettyTcpClientNetworkCommunicationEndpointService());
    spaceEnvironment.registerAndStartService(new NettyTcpServerNetworkCommunicationEndpointService());
    
    TcpClientNetworkCommunicationEndpointService clientService = spaceEnvironment.getServiceRegistry().getRequiredService(TcpClientNetworkCommunicationEndpointService.SERVICE_NAME);
    TcpServerNetworkCommunicationEndpointService serverService = spaceEnvironment.getServiceRegistry().getRequiredService(TcpServerNetworkCommunicationEndpointService.SERVICE_NAME);

    byte[][] delimiters = new byte[][] { new byte[] { '\n' }};
    TcpServerNetworkCommunicationEndpoint<String> server = serverService.newStringServer(delimiters, Charsets.UTF_8, 8099, spaceEnvironment.getLog());
    server.addListener(new TcpServerNetworkCommunicationEndpointListener<String>() {
      
      @Override
      public void onNewTcpConnection(TcpServerNetworkCommunicationEndpoint<String> endpoint,
          TcpServerClientConnection<String> connection) {
        System.out.println("Server has new connection " + connection.getConnectionId());
        System.out.println(connection);
        connection.writeMessage("Hello, " + connection.getConnectionId() + ". I am SERVER.\n");
      }
      
      @Override
      public void onCloseTcpConnection(TcpServerNetworkCommunicationEndpoint<String> endpoint,
          TcpServerClientConnection<String> connection) {
        System.out.println("Client has closed connection " + connection.getConnectionId());
       }

      @Override
      public void onTcpRequest(TcpServerNetworkCommunicationEndpoint<String> endpoint,
          TcpServerRequest<String> request) {
        String message = request.getMessage();
        System.out.println("Server received " + message);
        
        request.writeMessage(message.toUpperCase() + "\n");
        
      }
    });
    TcpClientNetworkCommunicationEndpoint<String> client = clientService.newStringClient(delimiters, Charsets.UTF_8, InetAddress.getLocalHost(), 8099, spaceEnvironment.getLog());
    client.addListener(new TcpClientNetworkCommunicationEndpointListener<String>() {
      
      @Override
      public void
          onTcpClientConnectionSuccess(TcpClientNetworkCommunicationEndpoint<String> endpoint) {
        System.out.println("Client sez server connection successful");
      }

      @Override
      public void
          onTcpClientConnectionClose(TcpClientNetworkCommunicationEndpoint<String> endpoint) {
        System.out.println("Client sez server connection closed");
      }

      @Override
      public void onNewTcpClientMessage(TcpClientNetworkCommunicationEndpoint<String> endpoint,
          String message) {
        System.out.println("Client received " + message);
      }
    });
    
    spaceEnvironment.addManagedResource(server);
    
    SmartSpacesUtilities.delay(1000);
    
    spaceEnvironment.addManagedResource(client);
    
    SmartSpacesUtilities.delay(1000);
    
    client.write("Hi there\n");
    
    SmartSpacesUtilities.delay(500);
    server.writeMessageAllConnections("A message to all clients\n");
    
    SmartSpacesUtilities.delay(500);
    client.shutdown();
    
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}
