import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticHtmlFileWebServer implements Server {

	@Override
	public void start(int port) {

		// open server socket
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not start server: " + e);
			System.exit(-1);
		}
		System.out.println("FileServer accepting connections on port " + port);

		// request handler loop
		while (true) {
			Socket connection = null;
			try {
				// wait for request
				connection = socket.accept();

				InputStream inputStream = connection.getInputStream();
				OutputStream outputStream = connection.getOutputStream();

				BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
				OutputStream out = new BufferedOutputStream(outputStream);

				// read first line of request (we do not use the headers anyway)
				String request = in.lines().findFirst().get();
				System.out.println(request);

				// parse request and send response
				if (request.startsWith("GET") && request.contains("/") && request.length() >= 14) {

					// extract filename from request
					String filename = request.substring(request.indexOf("/") + 1, request.length() - 9);
					if (filename == "") { // defaults to index.html if no file was requested
						filename = "index.html";
					}

					System.out.println(filename);
					File file = new File(filename);

					if (filename.endsWith(".html") && file.exists()) {

						out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());

						try (InputStream fileInput = new FileInputStream(file)) {
							
							byte[] buffer = new byte[1000];
							
							while (fileInput.available() > 0) {
								out.write(buffer, 0, fileInput.read(buffer));
							}
						}

					} else {

						out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
					}

				} else {

					out.write(formatErrorBody("400", "Bad Request", "").getBytes());
				}

				out.flush();

			} catch (IOException e) {

				e.printStackTrace();
			}

			// close connection
			try {
				if (connection != null)
					connection.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	private static String formatErrorBody(String statusCode, String title, String msg) {

		return "HTTP/1.1 " + statusCode + " " + title + "\r\n\r\n<!DOCTYPE html><head><title>" + statusCode + " - "
				+ title + "</title></head>" + "<body><h1>" + title + "</h1><p>" + msg + "<p><hr></body></html>";
	}
}