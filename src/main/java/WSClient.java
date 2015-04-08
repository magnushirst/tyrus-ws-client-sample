import java.io.IOException;

import java.io.InputStream;
import java.net.URI;
import java.nio.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Properties;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.container.grizzly.client.GrizzlyClientSocket;
import org.glassfish.tyrus.core.Base64Utils;

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
            // load properties file
            InputStream inStream = WSClient.class.getClassLoader().getResourceAsStream("settings.properties");
            if (inStream == null) {
                throw new IllegalArgumentException("Can't read the properties file you specified");
            }
            final Properties prop = new Properties();
            prop.load(inStream);
            inStream.close();

            // TLS
            final ClientEndpointConfig configuration = ClientEndpointConfig.Builder.create().build();
            ClientManager client = ClientManager.createClient();
            final SSLContextConfigurator defaultConfig = new SSLContextConfigurator();
            SSLEngineConfigurator sslEngineConfigurator =
                    new SSLEngineConfigurator(defaultConfig, true, false, false);
            client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR,
                    sslEngineConfigurator);

            // proxy
            client.getProperties().put(ClientManager.WLS_PROXY_HOST,     prop.getProperty("PROXY_HOST"));
            client.getProperties().put(ClientManager.WLS_PROXY_PORT,     Integer.parseInt(prop.getProperty("PROXY_PORT")));
            client.getProperties().put(ClientManager.WLS_PROXY_USERNAME, prop.getProperty("PROXY_USERNAME"));
            client.getProperties().put(ClientManager.WLS_PROXY_PASSWORD, prop.getProperty("PROXY_PASSWORD"));

            // websocket connection
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