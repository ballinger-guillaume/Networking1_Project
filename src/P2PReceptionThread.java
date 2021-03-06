import java.io.IOException;
import java.net.DatagramPacket;


public class P2PReceptionThread implements Runnable {
    private final Clientinfo client;
    private final P2P_Window window;
    public boolean stay = true;

    public P2PReceptionThread(P2P_Window p2P_window, Clientinfo client) {
        this.client = client;
        this.window = p2P_window;
    }


    @Override
    public void run() {
        byte[] buffer = new byte[2048];
        while (stay) {
            //if the socket is closed then it should exit the windows and exit the programm.
            if (client.getSocket().isClosed()) {
                System.exit(0);
            }
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                client.getSocket().receive(packet);
            } catch (IOException e) {
                //TODO Make a timeout and catch the exception. In the exception we should check if the client is still connected or not
                //TODO every  10 second - 1 min a handshake should be performed. Time should be random
                continue;
            }
            String recieve = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Recieved " + recieve);
            String keyword = recieve.split("\\s+")[0];
            if(recieve.split("\\s+").length == 2 && recieve.split("\\s+")[1].toUpperCase().equals("STOP")){
                keyword = "MESSAGE STOP";
            }
            Clientinfo peer;
            switch (keyword) {
                case "MESSAGE STOP":
                    client.setListenMode(true);
                    window.listenMode(client.searchpeers(packet.getAddress(),packet.getPort()));
                    break;
                case "/username":
                    peer = client.searchpeers(packet.getAddress(), packet.getPort());
                    if (peer != null) {
                        StringBuilder peerName = new StringBuilder();
                        for (int i = 1; i < recieve.split("\\s+").length; i++) {
                            peerName.append(recieve.split("\\s+")[i]).append(" ");
                        }
                        if(peerName.toString().length() <= 25){
                            peer.setUsername(peerName.toString());
                        }
                        window.updateUsername();
                    }
                    break;
                case "/quit":
                    //quit removes the peer from the peer list
                    Clientinfo tempClient = new Clientinfo(packet.getAddress(), packet.getPort());
                    client.getPeers().remove(tempClient);
                    window.updateUsername();
                    break;
                case "STOP":
                    peer = client.searchpeers(packet.getAddress(), packet.getPort());
                    if (peer != null) {
                        peer.setConnected(false);
                        window.updateUsername();
                    }
                    break;
                case "RECONNECTION":
                    peer = client.searchpeers(packet.getAddress(), packet.getPort());
                    if (peer != null) {
                        System.out.println("Reconnection got");
                        peer.setListenMode(false);
                        peer.setConnected(true);
                        window.updateUsername();
                    }
                    break;
                case "MESSAGE":
                    ReceptionThreadMessage message = new ReceptionThreadMessage(packet, window, client,recieve.split("\\s{2}")[1]);
                    new Thread(message).start();

                    //MESSAGE add message to the messages list and actualise the P2P_Window
                    break;
                case "FILE":
                    // FILE open another thread for this such that it doesn't stop the client
                    //creates the Reception for the files Thread
                    ReceptionOfFileThread files = new ReceptionOfFileThread(packet, client, recieve.split("\\s{2}")[1]);
                    new Thread(files).start();
                    window.messagesUpdate();
                default:
                    break;

            }


        }
    }
}
