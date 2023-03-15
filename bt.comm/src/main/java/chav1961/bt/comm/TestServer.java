package chav1961.bt.comm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class TestServer {

	public static void main(String[] args) throws IOException {
		 Selector selector = ComPortSelector.open();
		 ComPortChannel comPort = ComPortChannel.open("loopback");
	     comPort.configureBlocking(false);
	     comPort.register(selector, SelectionKey.OP_CONNECT);
	     ByteBuffer buffer = ByteBuffer.allocate(256);

	     for(;;) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                final SelectionKey key = iter.next();

                if (key.isConnectable()) {
                    register(selector, comPort);
                }

                if (key.isReadable()) {
                    answerWithEcho(buffer, key);
                }
                
                iter.remove();
            }
        }	
    }
	
	private static void answerWithEcho(ByteBuffer buffer, SelectionKey key) throws IOException {
 
        ComPortChannel client = (ComPortChannel) key.channel();
        client.read(buffer);
        if (new String(buffer.array()).trim().equals("assa")) {
            client.close();
            System.out.println("Not accepting client messages anymore");
        }
        else {
            buffer.flip();
            client.write(buffer);
            buffer.clear();
        }
    }

    private static void register(Selector selector, ComPortChannel serverSocket) throws IOException {
        ComPortChannel client = null;//
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    public static Process start() throws IOException, InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = TestServer.class.getCanonicalName();

        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className);

        return builder.start();
    }
}
