import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;


/**
 * AuctionServer representa un servidor de subastas simple que maneja la comunicación con los clientes.
 */
public class AuctionServer {

	// Número de puerto para la comunicación UDP
    private static final int UDP_SERVER_PORT = 9876;
    // Tamaño del búfer utilizado para recibir mensajes UDP
    private final static int BUFFER_SIZE = 65535;
    // Producto a subastar
    private static Product product;
    // Variable para almacenar el tiempo de inicio de la subasta
    private static long startTime;
    // Duración de la subasta en milisegundos (1 minuto)
    private static final long AUCTION_DURATION = 60 * 1000;
    // Bandera que determina el fin de la subasta
    private static boolean flag = false;

    // Número de puerto para la comunicación TCP
    private static final int TCP_PORT = 12345;
    //Lista de handler de clientes
    private static List<ClientHandler> clientHandlers = new ArrayList<>();
    //Producto actual a subastar
    private static Product currentProduct;
    //Lista de pujas
    private static List<AuctionItem> bids;
    //Identifica si la subasta se ha iniciado
    private static boolean auctionOpen = false;
    

    /**
     * Establece la información del producto para la subasta.
     *
     * @param name          Nombre del producto
     * @param description   Descripción del producto
     * @param initialPrice  Precio inicial del producto para la subasta
     */
    public void setProduct(String name, String description, int initialPrice) {
        product = new Product(name, description, initialPrice);
    }

    /**
     * Agrega una oferta para un artículo de subasta.
     *
     * @param username Nombre del ofertante
     * @param price    Precio de la oferta
     * @return True si la oferta se agrega correctamente, false en caso contrario
     */
    public boolean addAuctionItem(String username, int price) {
        SortedSet<AuctionItem> bids = product.getAuctions();
        if ((price > bids.last().getPrice()) ||
                (bids.size() == 0 && price >= product.getInitialPrice()))
            return bids.add(new AuctionItem(username, price));
        else
            return false;
    }

    /**
     * Obtiene el artículo de subasta con la oferta máxima.
     *
     * @return AuctionItem con la oferta máxima
     */
    public AuctionItem getMaxBid() {
    	if(!product.getAuctions().isEmpty())
    		return product.getAuctions().last();
    	else
    		return null;
    }

    /**
     * Agrega una oferta para un artículo de subasta.
     *
     * @param bid Objeto de la puja (cantidad y nombre)
     */
    public static synchronized void addAuctionItem(AuctionItem bid) {
    	if(bid.getPrice() > bids.get(bids.size() - 1).getPrice()) {
    		bids.add(new AuctionItem(bid.getUsername(), bid.getPrice()));
    		System.out.println("La puja de "+bid.getUsername()+" por "+bid.getPrice()+" ha sido aceptada.");
    	}
    	else {
    		System.out.println("La puja de "+bid.getUsername()+" por "+bid.getPrice()+" NO ha sido aceptada.");
    	}
    }
    
    /**
     * Maneja automáticamente la subasta.
     *
     * @param product Producto que se subasta
     */
    private static void handleAuction(Product product) {
        while (true) {
            if (!auctionOpen && currentProduct != null) {
                startAuction(product);
            }
        }
    }
    
    /**
     * Inicia una subasta.
     *
     * @param product Producto que se subasta
     */
    private static synchronized void startAuction(Product product) {
        if (!auctionOpen) {
            currentProduct = product;
            bids.clear();
            auctionOpen = true;
            System.out.println("Subasta iniciada para el producto: " + product.getName());
        }
    }
    
    /**
     * Cierra la subasta y muestra por consola el ganador.
     */
    public void closeAuction() {
        flag = true;
        if(getMaxBid() != null)
        	System.out.println("GANADOR: " + getMaxBid().getUsername() + " con una cantidad de " + getMaxBid().getPrice() + "€");
    }

    /**
     * UDPClientHandler es responsable de manejar las solicitudes de clientes UDP.
     */
    public static class UDPClientHandler implements Runnable {

        private DatagramSocket udpSocket;
        private DatagramPacket receivePacket;
        private DatagramPacket sendPacket;

        /**
         * Constructor para UDPClientHandler.
         *
         * @param udpSocket     DatagramSocket para manejar la comunicación UDP
         * @param receivePacket DatagramPacket recibido del cliente
         */
        public UDPClientHandler(DatagramSocket udpSocket, DatagramPacket receivePacket) {
            this.udpSocket = udpSocket;
            this.receivePacket = receivePacket;
        }

        /**
         * Método principal ejecutado cuando comienza el hilo.
         * Maneja la solicitud del cliente, envía información de la subasta e imprime los detalles del cliente.
         */
        public void run() {
            InetAddress clientIPAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();

            System.out.println(clientIPAddress + ":" + clientPort + " solicitó información.");

            String message = "\n--------- INFORMACIÓN DE LA SUBASTA ---------\n" + bids.toString();

            byte[] buffer = message.getBytes();
            sendPacket = new DatagramPacket(buffer, buffer.length, clientIPAddress, clientPort);

            try {
                udpSocket.send(sendPacket);
            } catch (IOException e) {
            	System.out.print("Error de envío del paquete UDP.");
            }
        }
    }

    /**
     * Maneja la comunicación UDP con los clientes.
     *
     * @param udpSocket DatagramSocket para la comunicación UDP
     */
    public static void handleUDP(DatagramSocket udpSocket) {
        while (!flag) {
            byte[] buffer = new byte[BUFFER_SIZE];

            DatagramPacket udpPacket = new DatagramPacket(buffer, buffer.length);

            try {
                udpSocket.receive(udpPacket);
            } catch (IOException e) {
            	System.out.print("Error de recepción del paquete UDP.");
            }

            UDPClientHandler clientHandler = new UDPClientHandler(udpSocket, udpPacket);
            new Thread(clientHandler).start();
        }
    }

    /**
     * Maneja la conexión con los clientes y su interacción con el servidor
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                 ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream())) {

                // Lógica del servidor para manejar la conexión con el cliente
                while (true) {
                    try {
                        Object receivedObject = objectInputStream.readObject();

                        if (receivedObject instanceof AuctionItem) {
                            // Lógica para manejar pujas recibidas
                            AuctionItem receivedBid = (AuctionItem) receivedObject;
                            addAuctionItem(receivedBid);
                        } else if (receivedObject instanceof String && ((String) receivedObject).equalsIgnoreCase("exit")) {
                            // Cliente desea salir
                            System.out.println("Cliente desconectado");
                            clientHandlers.remove(this);
                            break;
                        }
                    } catch (EOFException eof) {
                        System.out.println("Cliente desconectado");
                        clientHandlers.remove(this);
                        break;
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Método principal para la aplicación AuctionServer.
     *
     * @param args Argumentos de la línea de comandos (no utilizados en esta aplicación)
     */
    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Ingrese la información del producto que se va a subastar:");
            System.out.print("Nombre: ");
            String name = userInput.readLine().trim();
            System.out.print("Descripción: ");
            String description = userInput.readLine().trim();
            System.out.print("Precio inicial: ");
            int initialPrice = Integer.parseInt(userInput.readLine());
            System.out.println("Subasta iniciada. Esperando ofertas...");
            
            userInput.close();

            // Establece la información del producto
            AuctionServer auctionServer = new AuctionServer();
            auctionServer.setProduct(name, description, initialPrice);
            AuctionItem item=new AuctionItem("Precio de salida", initialPrice);
            bids=new ArrayList<>();
            bids.add(item);
            startTime = System.currentTimeMillis();

            // Crear un nuevo hilo para manejar la subasta
            Thread handleAuction=new Thread(() -> handleAuction(product));
            handleAuction.start();
            
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    long remainingTime = AUCTION_DURATION - elapsedTime;

                    if (remainingTime <= 10 * 1000) {
                        // Muestra el tiempo restante cuando quedan 10 segundos o menos
                        System.out.println("Tiempo restante: " + (remainingTime / 1000) + " segundos");
                    }

                    if (remainingTime <= 0) {
                        auctionServer.closeAuction();
                        System.out.println("La subasta ha finalizado.");
                        // Notifica a los clientes conectados sobre el cierre de la subasta (implementación necesaria)
                        timer.cancel();  // Cancela el temporizador después de cerrar la subasta
                        System.out.println("El ganador de la subasta es "+bids.get(bids.size()-1).getUsername()+" con una puja de "+bids.get(bids.size()-1).getPrice()+". ¡Enhorabuena!");
                    }
                }
            }, 0, 1000);

            try {
                // Crea DatagramSocket para la comunicación con los clientes y comienza a manejar la comunicación UDP
                DatagramSocket udpSocket = new DatagramSocket(UDP_SERVER_PORT);
                new Thread(() -> handleUDP(udpSocket)).start();
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }

            //Maneja la conexión de múltiples usuarios
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente TCP conectado desde: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                Thread clientH=new Thread(clientHandler);
                clientH.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}