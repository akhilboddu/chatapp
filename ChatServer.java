import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
// import java.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ChatServer {

    private static final int PORT = 9001;

    private static HashSet<String> names = new HashSet<String>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    // deals with single client and broadcasts the message
    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void readinput(String input){ //application protocol
            String arr [] = input.split(","); 
            String cmd = arr[0].trim();
            String users = arr[1].trim();
            String msg = arr[2].trim();

            String tosend []= users.split(";");

            System.out.println(cmd +users+ msg);
            // System.out.println("before");
            for (int p=0; p<tosend.length ;p++ ) {
                System.out.println(tosend[p]);
            }

            // System.out.println("after");
            ArrayList<String> newNames  = new ArrayList<String>();

            for(String name : names){
                newNames.add(name); 
            }

            switch (Integer.parseInt(cmd)){
                case 0 :

                    int i = 0;
                    for (PrintWriter writer : writers) {
                            for(int j=0; j<tosend.length; j++){ 

                                if (newNames.get(i).equals(tosend[j]) ){ 
                                    System.out.println("receiver: "+newNames.get(i));
                                    System.out.println("MESSAGE " + name + ": " + msg);
                                    writer.println("MESSAGE " + name + ": " + msg); 
                                    continue;

                                }

                            }

                            i++;
                                
                        }
                
                    break; 

                case 1 :
                    System.out.println("bleeh");
                    break;             

            }

        }
        
        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                        else{
                            System.out.println("Name already exists. Try another one.");
                        }
                    }
                }

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                writers.add(out);

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }

                    readinput(input); // this is where input is processed for app protocol
                    
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}