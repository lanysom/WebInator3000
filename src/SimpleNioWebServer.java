import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class SimpleNioWebServer implements Server {

	private final static int PORT = 5555;
	private final static int BUFFER_SIZE = 4096;
	private static Selector selector;

	@Override
	public void start(int port) {
		// TODO Auto-generated method stub
		try {
			selector = Selector.open();

			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.bind(new InetSocketAddress(PORT));
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("Server started on port " + PORT);

			while (true) {
				selector.select();
				Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();

					if (key.isAcceptable()) {
						handleAccept(serverSocketChannel);
					} else if (key.isReadable()) {
						handleRead(key);
					}

					keyIterator.remove();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void handleAccept(ServerSocketChannel serverSocketChannel) throws IOException {
		SocketChannel clientChannel = serverSocketChannel.accept();
		if (clientChannel != null) {
			clientChannel.configureBlocking(false);
			clientChannel.register(selector, SelectionKey.OP_READ);
			System.out.println("Client connected: " + clientChannel.getRemoteAddress());
		}
	}

	private static void handleRead(SelectionKey key) throws IOException {
		SocketChannel clientChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		int bytesRead = clientChannel.read(buffer);

		if (bytesRead == -1) {
			clientChannel.close();
			return;
		}

		String request = new String(buffer.array()).trim();
		String[] requestLines = request.split("\n");
		if (requestLines.length > 0) {
			String[] requestLine = requestLines[0].split(" ");
			if (requestLine.length > 1 && requestLine[0].equals("GET")) {
				serveFile(clientChannel, requestLine[1]);
			}
		}
	}

	private static void serveFile(SocketChannel clientChannel, String path) throws IOException {
		try {
			path = path.startsWith("/") ? path.substring(1) : path;
			path = path.isEmpty() ? "index.html" : path;
			Path filePath = Paths.get(path);
			if (!Files.exists(filePath)) {
				sendNotFoundResponse(clientChannel);
				return;
			}

			ByteBuffer buffer = ByteBuffer.wrap(Files.readAllBytes(filePath));
			ByteBuffer headerBuffer = ByteBuffer.wrap(createHeader("200 OK", Files.size(filePath)).getBytes());

			clientChannel.write(new ByteBuffer[] { headerBuffer, buffer });
		} finally {
			clientChannel.close();
		}
	}

	private static String createHeader(String status, long length) {
		return "HTTP/1.1 " + status + "\r\n" + "Content-Length: " + length + "\r\n"
				+ "Content-Type: text/plain\r\n\r\n";
	}

	private static void sendNotFoundResponse(SocketChannel clientChannel) throws IOException {
		String header = createHeader("404 Not Found", 0);
		ByteBuffer buffer = ByteBuffer.wrap(header.getBytes());
		clientChannel.write(buffer);
	}

}
