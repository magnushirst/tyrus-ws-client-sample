import java.io.IOException;

import java.net.URI;
import java.nio.*;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;

public class WSClient {
    public static final String WS_SERVER_URI = "wss://echo.websocket.org";
    private Session session;

    public static void main (String args[]) {
        WSClient client = new WSClient();
        client.connect();
        try {
            client.sendMessage("hello world");
            byte[] array = new byte[]{0x01, 0x02, 0x03, 0x04};
            ByteBuffer buffer2 = ByteBuffer.wrap(array);
            client.sendMessage(buffer2);
            while (true) {}
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            final ClientEndpointConfig configuration = ClientEndpointConfig.Builder.create().build();
            ClientManager client = ClientManager.createClient();
            final SSLContextConfigurator defaultConfig = new SSLContextConfigurator();
            SSLEngineConfigurator sslEngineConfigurator =
                    new SSLEngineConfigurator(defaultConfig, true, false, false);
            client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR,
                    sslEngineConfigurator);
            client.connectToServer(
                    new Endpoint() {
                        @Override
                        public void onOpen(Session session, EndpointConfig config) {
                            WSClient.this.session = session;
                            WSClient.this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                                @Override
                                public void onMessage(String message) {
                                    System.out.println("Received message: " + message);
                                }
                            });
                            WSClient.this.session.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {
                                @Override
                                public void onMessage(ByteBuffer message) {
                                    System.out.println("Received message: " + message);
                                }
                            });
                        }
                    },
                    configuration, new URI(WS_SERVER_URI));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException, InterruptedException{
        System.out.println("Sent message: " + message);
        session.getBasicRemote().sendText(message);
    }

    public void sendMessage(ByteBuffer message) throws IOException, InterruptedException{
        System.out.println("Sent message: " + message);
        session.getBasicRemote().sendBinary(message);
    }

}