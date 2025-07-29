import java.io.*;
import java.net.Socket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import pcd.util.TextIO4GUI;


/**
 * AuctionClient es una aplicación cliente simple que se comunica con un servidor mediante UDP.
 */
public class AuctionClient {

    // Número de puerto para la comunicación UDP con el servidor
    private static final int UDP_SERVER_PORT = 9876;

    // Número de puerto para la comunicación TCP con el servidor
    private static final int TCP_SERVER_PORT = 12345;

    // Tamaño del búfer utilizado para recibir mensajes
    private final static int BUFFER_SIZE = 65535;

    /**
     * MessageSender es responsable de enviar mensajes al servidor a intervalos regulares.
     */
    static class MessageSender implements Runnable {

        private DatagramSocket sock;  // DatagramSocket para enviar mensajes
        private String hostname;       // Nombre de host o dirección IP del servidor
        private Timer timer;           // Temporizador para programar el envío periódico de mensajes

        /**
         * Constructor para MessageSender.
         *
         * @param sock     DatagramSocket para enviar mensajes
         * @param hostname Nombre de host o dirección IP del servidor
         */
        MessageSender(DatagramSocket sock, String hostname) {
            this.sock = sock;
            this.hostname = hostname;
            this.timer = new Timer();
        }

        /**
         * Envía el mensaje especificado al servidor.
         *
         * @param s Mensaje a enviar
         * @throws Exception Si ocurre un error durante el envío del mensaje
         */
        private void sendMessage(String s) throws Exception {
            byte buf[] = s.getBytes();
            InetAddress address = InetAddress.getByName(hostname);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, UDP_SERVER_PORT);
            sock.send(packet);
        }

        /**
         * Método principal ejecutado cuando comienza el hilo.
         * Intenta enviar un mensaje inicial y luego programa el envío periódico de un mensaje "info".
         */
        public void run() {
            // Tarea del temporizador para enviar el mensaje "info" cada 5 segundos
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    try {
                        sendMessage("info");
                    } catch (IOException e) {
                    	TextIO4GUI.putln("Error de envío del paquete UDP.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 5000);
        }
    }

    /**
     * MessageReceiver es responsable de recibir mensajes del servidor y mostrarlos usando TextIO4GUI.
     */
    static class MessageReceiver implements Runnable {

        private DatagramSocket sock;  // DatagramSocket para recibir mensajes
        private byte buf[];           // Búfer para almacenar datos de mensajes recibidos

        /**
         * Constructor para MessageReceiver.
         *
         * @param s DatagramSocket para recibir mensajes
         */
        MessageReceiver(DatagramSocket s) {
            sock = s;
            buf = new byte[BUFFER_SIZE];
        }

        /**
         * Método principal ejecutado cuando comienza el hilo.
         * Recibe continuamente mensajes del servidor y los muestra usando TextIO4GUI.
         */
        public void run() {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    sock.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    TextIO4GUI.putln(received);
                } catch (Exception e) {
                    // Imprime excepciones que ocurran durante la recepción de mensajes
                	TextIO4GUI.putln("Error de recepción del paquete UDP.");
                }
            }
        }
    }

    /**
     * Método para manejar el input de los clientes.
     *
     * @param objectOutputStream Elemento introducido por el cliente que se enviará al servidor
     * @param username Nombre del usuario
     */
    private static void handleUserInput(ObjectOutputStream objectOutputStream, String username) {
        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.println("\n1. Enviar puja");
                System.out.println("2. Salir");
                System.out.print("Seleccione una opción: ");

                int choice = Integer.parseInt(userInput.readLine().trim());

                switch (choice) {
                    case 1:
                        System.out.print("Ingrese su puja: ");
                        int bidAmount = Integer.parseInt(userInput.readLine().trim());
                        AuctionItem bid = new AuctionItem(username, bidAmount);
                        objectOutputStream.writeObject(bid);
                        objectOutputStream.flush();
                        break;
                    case 2:
                        objectOutputStream.writeObject("exit");
                        objectOutputStream.flush();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Opción no válida");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para manejar los mensajes recibidos desde el servidor.
     *
     * @param objectInputStream Elemento enviado por el servidor que recibe el cliente
     * @throws EOFException Maneja la desconexión del cliente
     */
    private static void handleServerMessages(ObjectInputStream objectInputStream) throws EOFException {
        try {
            while (true) {
                Object receivedObject = objectInputStream.readObject();

                if (receivedObject instanceof String) {
                    String serverMessage = (String) receivedObject;
                    System.out.println("Mensaje del servidor: " + serverMessage);
                }
            }
        }  catch (EOFException eof) {
            System.out.println("Desconexión");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } 
    }

    /**
     * Método principal para la aplicación AuctionClient.
     *
     * @param args Argumentos de la línea de comandos (no utilizados en esta aplicación)
     * @throws Exception Si ocurre un error durante la ejecución del método principal
     */
    public static void main(String[] args) throws Exception {
        // Crea una instancia de TextIO4GUI para mostrar mensajes
        new TextIO4GUI("Auction Client");

        // Crea un DatagramSocket para la comunicación con el servidor
        DatagramSocket socket = new DatagramSocket();

        // Crea instancias de MessageReceiver y MessageSender
        MessageReceiver r = new MessageReceiver(socket);
        MessageSender s = new MessageSender(socket, "localhost");

        // Crea y comienza hilos para la recepción y el envío de mensajes
        Thread rt = new Thread(r);
        Thread st = new Thread(s);
        rt.start();
        st.start();

        //Crea un socket TCP para la conexión con el servidor
        try (Socket tcpSocket = new Socket("localhost", TCP_SERVER_PORT);

        //Crea streams de entrada y salida para la comunicación con el servidor y crea un BufferedReader para leer los datos de entrada del cliente
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(tcpSocket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(tcpSocket.getInputStream());
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

        //Le pide al usuario introducir un nombre
        System.out.print("Ingresa tu nombre de usuario: ");
        String username = userInput.readLine();
        System.out.println(username);

        // Crea y comienza hilos para la recepción y el envío de mensajes
        Thread inputThread = new Thread(() -> handleUserInput(objectOutputStream, username));
        Thread receiveThread = new Thread(() -> {
           try {
               handleServerMessages(objectInputStream);
           } catch (EOFException e) {
               e.printStackTrace();
           }
        });

        inputThread.start();
        receiveThread.start();

        inputThread.join();  // Espera a que el hilo de entrada termine
        receiveThread.join();  // Espera a que el hilo de recepción termine
        tcpSocket.close();  //Cierra la conexión TCP

       } catch (IOException | InterruptedException e) {
            e.printStackTrace();
       }
    }
}