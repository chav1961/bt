package chav1961.bt.comm.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TestClient {
	private static ComPortChannel client;
    private static ByteBuffer buffer;
    private static TestClient instance;

    public static TestClient start() {
        if (instance == null)
            instance = new TestClient();

        return instance;
    }

    public static void stop() throws IOException {
        client.close();
        buffer = null;
    }

    private TestClient() {
        try {
            client = ComPortChannel.open("");
            buffer = ByteBuffer.allocate(256);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String msg) {
        buffer = ByteBuffer.wrap(msg.getBytes());
        String response = null;
        try {
            client.write(buffer);
            buffer.clear();
            client.read(buffer);
            response = new String(buffer.array()).trim();
            System.out.println("response=" + response);
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;

    }
}
