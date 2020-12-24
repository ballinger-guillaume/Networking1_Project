import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class P2P_Window extends JFrame {
    private final Clientinfo clientinfo;
    private final JTextField username;
    private final JPanel leftPanel;
    private final JPanel MiddlePanel;
    private final JPanel MessagePanel;
    private final JTextArea MessageArea;
    private final JPanel RightPanel;
    private final JButton connectedButton;
    private final JScrollPane MessagePane;
    private final JScrollPane UsernamePane;

    public P2P_Window(Clientinfo clientinfo) {
        this.clientinfo = clientinfo;

        this.setResizable(false);
        this.setSize(1920, 1080);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        //Sets the Panels
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        leftPanel = new JPanel();
        GridBagLayout gridleft = new GridBagLayout();
        MiddlePanel = new JPanel(new GridLayout(0, 1));
        messagesUpdate();
        MessagePane = new JScrollPane(MiddlePanel);
        MessagePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        MessagePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        //MESSAGES AREA settings
        MessageArea = new JTextArea();
        MessageArea.setLineWrap(true);
        MessageArea.setOpaque(false);
        MessageArea.setRows(10);
        MessageArea.setColumns(10);
        MessageArea.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.GRAY),
                BorderFactory.createMatteBorder(2, 2, 2, 2, Color.GRAY)));
        MessageArea.setPreferredSize(new Dimension(500, 200));
        MessageArea.setDragEnabled(true);
        MessagePanel = new JPanel(new BorderLayout());
        MessagePanel.add(MessageArea);


        RightPanel = new JPanel(new GridLayout(0, 1));
        updateUsername();
        UsernamePane = new JScrollPane(RightPanel);
        UsernamePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        UsernamePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        this.setLayout(gb);

        //ADDING them to the main frame
        c.weightx = 0.2;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        this.add(leftPanel, c);
        c.weightx = 1;
        c.gridx = 1;
        c.gridheight = 1;
        this.add(MessagePane, c);
        c.gridheight = 2;
        c.weightx = 0.5;
        c.gridx = 2;
        this.add(UsernamePane, c);
        c.gridheight = 1;
        c.gridwidth = 0;
        c.weightx = 0;
        c.gridx = 1;
        c.gridy = 2;
        c.weighty = 0.2;
        c.fill = GridBagConstraints.BOTH;
        this.add(MessagePanel, c);


        //Set's the username
        c.fill = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.05;
        c.gridwidth = 3;
        leftPanel.setLayout(gridleft);
        this.username = new JTextField(clientinfo.getUsername());
        this.username.setBackground(null);
        this.username.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        this.username.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 50));
        this.username.setCaretColor(username.getBackground());
        leftPanel.add(username, c);
        username.setOpaque(false);

        //finish the leftPanel
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        connectedButton = new JButton("Connected");
        leftPanel.add(connectedButton, c);
        connectedButton.setVisible(true);
        connected();


        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        JButton send = new JButton("Send");
        leftPanel.add(send, c);

        //drop and drag
        MessageArea.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> droppedFiles;
                    droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (droppedFiles.size() > 1) {
                        JOptionPane.showMessageDialog(MessageArea, "Sorry...can't handle more than one files together.");
                    }
                    else if(!clientinfo.isConnected()){
                        JOptionPane.showMessageDialog(MessageArea, "You must be connected sorry");
                    }
                    else {
                        File droppedFile = droppedFiles.get(0);
                        if (droppedFile.getName().matches("[\\w]+\\.[A-Za-z]{3,5}")) {
                            JOptionPane.showMessageDialog(MessageArea, "File is being send: " + droppedFile.getName());
                            for (Clientinfo client : clientinfo.getPeers()) {
                                SendingThread s = new SendingThread(droppedFile, client);
                                s.setUsername(clientinfo.getUsername());
                                new Thread(s).start();
                            }
                        } else {
                            JOptionPane.showMessageDialog(MessageArea, "Sorry...not a valid file. Make sure your filename has no spaces or special characters.");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //sets the ButtonListener
        connectedButton.addActionListener(e -> {
            if (connectedButton.getText().equals("Connected")) {
                disconnect();
                sendToPeers("STOP");

            } else {
                reconnected();
                sendToPeers("RECONNECTION");
            }
        });

        //sets the SendingListener
        send.addActionListener(e -> {
            if(!clientinfo.isConnected()){
                return;
            }
            for (Clientinfo ignore : clientinfo.getPeers()) {
                SendingThread s = new SendingThread(MessageArea.getText(), ignore);
                s.setUsername(clientinfo.getUsername());
                new Thread(s).start();
            }
            MessageArea.setText("");
        });

        //In case the user press the exit button
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendToPeers("/quit");
                super.windowClosed(e);
            }
        });


        //creates the ReceptionThread

        P2PReceptionThread thread = new P2PReceptionThread(this, clientinfo);
        new Thread(thread).start();

        //create listener  for username changes
        //TODO restrict up to 25 and Change GUI Font to match the actual size
        username.addActionListener(e -> sendToPeers("/username " + username.getText()));


        this.setVisible(true);
    }

    private void sendToPeers(String message) {
        //adding comment to wake git up
        for (Clientinfo client : clientinfo.getPeers()) {
            try {
                clientinfo.getSocket().send(new DatagramPacket(
                        message.getBytes(StandardCharsets.UTF_8),
                        message.getBytes(StandardCharsets.UTF_8).length,
                        client.getAddress(),
                        client.getPort()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void connected() {
        this.clientinfo.setConnected(true);
        connectedButton.setBackground(new Color(11, 102, 35));
        connectedButton.setText("Connected");
        connectedButton.setForeground(new Color(255, 255, 255));
        connectedButton.setFont(new Font(Font.SERIF, Font.BOLD, 60));
        connectedButton.repaint();

    }

    public void disconnect() {
        this.clientinfo.setConnected(false);
        connectedButton.setBackground(new Color(128, 0, 0));
        connectedButton.setText("Disconnected");
        connectedButton.repaint();
    }

    public void reconnected() {
        connected();
    }

    public void messagesUpdate() {

        ArrayList<Component> toRemove = new ArrayList<>();
        for (Component comp : MiddlePanel.getComponents()) {
            if (comp instanceof JLabel) {
                toRemove.add(comp);
            }
        }
        //Removes Labels avoiding runtime error
        for (Component comp : toRemove) {
            MiddlePanel.remove(comp);
        }


        MiddlePanel.setVisible(true);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.CENTER;
        int counter = 0;
        for (Messages m : clientinfo.getMessages()) {
            c.gridx = counter;
            JLabel label = new JLabel("<html>" + m.getMessage() + "<br><br>" + m.getUsername() + "</html>");
            counter++;
            label.setBorder(new CompoundBorder( // sets two borders
                    BorderFactory.createMatteBorder(5, 5, 5, 5, Color.GRAY), // outer border
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            MiddlePanel.add(label, c);
        }
    }


    public void updateUsername() {
        //finds component that are Labels and remove them
        ArrayList<Component> toRemove = new ArrayList<>();
        for (Component comp : RightPanel.getComponents()) {
            if (comp instanceof JLabel) {
                toRemove.add(comp);
            }
        }
        //Removes Labels avoiding runtime error
        for (Component comp : toRemove) {
            RightPanel.remove(comp);
        }
        RightPanel.setVisible(true);
        //RightPanel.setLayout(new GridBagLayout());
        RightPanel.setLayout(new BoxLayout(RightPanel, BoxLayout.Y_AXIS));
        //GridBagConstraints c = new GridBagConstraints();

        for (int i = 0; i < clientinfo.getPeers().size(); i++) {

            JLabel label = new JLabel(clientinfo.getPeers().get(i).getUsername());
            label.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 30));
            label.setForeground(clientinfo.getPeers().get(i).isConnected() ? new Color(11, 102, 35) : new Color(128, 0, 0));


            RightPanel.add(label);
        }
        RightPanel.revalidate();
        RightPanel.repaint();

    }
    //TODO make a message actualiser that will update the scrolling pane
    //TODO make a textarea with a keylistener for enter that will send the message.
    //TODO keylistener should then send the message  to all the peers.
    //TODO text area should also allow for drag and drop
    //TODO methode that actualises the name on the right scrolling pane
    //TODO methode that actualises the clients connection button.
    //TODO add closing listener that will send /quit to each peers
    //TODO add the usernames in a scrolling pane with the color of their respectiv connection

}
