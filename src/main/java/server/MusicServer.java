package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MusicServer {
    private List<ObjectOutputStream> clientOutputStreams;

    private class ClientHandler implements Runnable {
        Socket clientSocket;
        ObjectInputStream inputStream;

        public ClientHandler(Socket socket) {
            clientSocket = socket;
            try {
                inputStream = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            // two objects for message and the track file
            Object o2;
            Object o1;

            try {
                while ( (o1 = inputStream.readObject()) != null) {
                    o2 = inputStream.readObject();

                    System.out.println("Read 2 objects....");
                    tellEveryone(o1,o2);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    public void start() {
        clientOutputStreams = new ArrayList<>();

        try {
            ServerSocket serverSocket = new ServerSocket(4242);
            System.out.println("Music server started at port 4242");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientOutputStreams.add(
                        new ObjectOutputStream(clientSocket.getOutputStream())
                );

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();

                System.out.println("Got a connection");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tellEveryone(Object o1, Object o2) {
        Iterator<ObjectOutputStream> it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            ObjectOutputStream outputStream = (ObjectOutputStream) it.next();

            try {
                outputStream.writeObject(o1);
                outputStream.writeObject(o2);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
