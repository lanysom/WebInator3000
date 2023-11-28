import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SuperSimpleWebServer implements Server {

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
			
				// read request
				String request = in.lines().findFirst().get();
				System.out.println(request);

				// send response
				out.write("HTTP/1.0 200 OK\r\n".getBytes());		
				out.write("Content-Type: text/html\r\n".getBytes());
				out.write("\r\n".getBytes());				
				out.write("<!DOCTYPE html><html><head><title>Example</title></head><body><p>Hello world</p></body></html>".getBytes());
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
}
