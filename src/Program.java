import java.io.IOException;

public class Program {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		Server server = new SimpleNioWebServer();
//		Server server = new StaticHtmlFileWebServer();
//		Server server = new SuperSimpleWebServer();
		server.start(5555); 
	}

}
