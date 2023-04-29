import java.io.*;
import java.net.Socket;
import java.util.stream.Collectors;

public class Client implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    Logger logger;

    public static int getPort() throws IOException {
        String filePath = "settings.txt";
        BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
        String content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        String[] splitString = content.split(" ");
        return Integer.parseInt(splitString[2]);
    }

    @Override
    public void run() {

        try {
            client = new Socket("localhost", getPort());
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            logger = new Logger();

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null){
                System.out.println(inMessage);
                logger.log(inMessage);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        done = true;
        try {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {

        }
    }

    class InputHandler implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    if (message.equals("/exit")) {
                        out.println(message);
                        inReader.close();
                        shutdown();
                    } else {
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {

        Client client = new Client();
        client.run();
    }
}
