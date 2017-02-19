/*******************************
 Plattenberger, Joseph
 A-option
*******************************/
import java.io.*;
import java.net.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;

public class ChatServer {
    
    public static void main(String argv[]) throws Exception {
        StartServer myChatServer = new StartServer();
    }
}
class StartServer {
    public List <DataOutputStream> slist;
    public List <PacketID> UDPList;
    public DatagramSocket UDPSocket;
    
    public StartServer() throws Exception{
        new ConnectClient(this).start();
        new UDPListener(this).start();
    }
}
class UDPListener extends Thread{
    StartServer myChatServer;
    protected BufferedReader in;
    
    public UDPListener(StartServer myChatServer) throws IOException {
        this.myChatServer = myChatServer;
    	myChatServer.UDPSocket = new DatagramSocket (4445);
        myChatServer.UDPList = new ArrayList <PacketID>();
    }
    
    public void run(){
        while (true){
            try{
                byte[] receiveData = new byte[1024];
                byte[] sendData = new byte[1024];
               synchronized (this) {
                DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                
                myChatServer.UDPSocket.receive(receivedPacket);
                String sentence = new String(receivedPacket.getData());
                
                PacketID myPacket = new PacketID(receivedPacket.getAddress(), receivedPacket.getPort());
                int clientNum = 0;
                int i = 0;
                boolean containsFlag = false;
                for (i = 0; i < myChatServer.UDPList.size(); i++){
                    if (myChatServer.UDPList.get(i).getPacketAddress().equals(myPacket.getPacketAddress()) && myChatServer.UDPList.get(i).getPacketPort() == (myPacket.getPacketPort())){
                        containsFlag = true;
                        break;
                    }else{
                        containsFlag = false;
                    }
                }
                clientNum = i;
                if (containsFlag == false){
                    myChatServer.UDPList.add(myPacket);
                    
                    System.out.println("-----UDP Client " + clientNum + " Connected-----");
                    String greeting;
                    
                    for (i = 0; i < myChatServer.UDPList.size(); i++){
                        if (myChatServer.UDPList.get(i) == myPacket){
                            greeting = "SERVER: You have connected to the chat room\n";
                            sendData = greeting.getBytes();
                        }else{
                            greeting = "SERVER: UDP Client " + clientNum + " has connected to the chat room\n";
                            sendData = greeting.getBytes();
                        }
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, myChatServer.UDPList.get(i).IPAddress, myChatServer.UDPList.get(i).port);
                        myChatServer.UDPSocket.send(sendPacket);
                    }
                    for (i = 0; i < myChatServer.slist.size(); i++){
                        myChatServer.slist.get(i).writeBytes("SERVER: UDP Client " + clientNum + " has connected to the chat room\n");
                    }
                    
                }
                
                System.out.println("UDP Client " + clientNum + ": " + sentence);
                sentence = "UDP Client " + clientNum + ": " + sentence + '\n';
                sendData = sentence.getBytes();
                for (i = 0; i < myChatServer.UDPList.size(); i++){
                    if (myChatServer.UDPList.get(i).getPacketAddress().equals(myPacket.getPacketAddress()) && myChatServer.UDPList.get(i).getPacketPort() == (myPacket.getPacketPort())){
                    }else{
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, myChatServer.UDPList.get(i).IPAddress, myChatServer.UDPList.get(i).port);
                        myChatServer.UDPSocket.send(sendPacket);
                    }
                }
                for (i = 0; i < myChatServer.slist.size(); i++){
                    myChatServer.slist.get(i).writeBytes(sentence);
                }
               }

            }catch (IOException e){
                myChatServer.UDPSocket.close();
                e.printStackTrace();
            }
        }
    }
}
class ConnectClient extends Thread{
	StartServer myChatServer;
    public ServerSocket welcomeSocket;
    public Responder h;
    public int threadID;

    public ConnectClient(StartServer myChatServer) throws Exception {
        // TCP socket
    	this.myChatServer = myChatServer;
        welcomeSocket = new ServerSocket(6789);

        h = new Responder();

        myChatServer.slist = new ArrayList <DataOutputStream>();

        threadID = 0;
        // server runs for infinite time and
        // wait for clients to connect
    }
    public void run(){
        while (true) {
            try{
                // waiting..
                Socket connectionSocket = welcomeSocket.accept();
                synchronized(this){
                	// on connection establishment start a new thread for each client
                	Thread t = new Thread(new MyServer(h, connectionSocket, threadID, myChatServer));
                	// start thread
                	t.start();
                	threadID++;
                }
            }catch (IOException e){
                e.printStackTrace();
                break;
            }
        }
    }
}

class MyServer implements Runnable {
	StartServer myChatServer;
	Responder h;
	Socket connectionSocket;
	int threadID;
	
    public MyServer(Responder h, Socket connectionSocket, int threadID, StartServer myChatServer) {
    	this.myChatServer = myChatServer;
    	this.h = h;
    	this.connectionSocket = connectionSocket;
    	this.threadID = threadID;
    }
    
    @Override
    public void run() {
        try {
        	DataOutputStream outToClient;
        	synchronized (this){
            outToClient = new DataOutputStream(
                             connectionSocket.getOutputStream());
            myChatServer.slist.add(outToClient);
            System.out.println("-----TCP Client " + threadID + " Connected-----");
            for (int i = 0; i < myChatServer.slist.size(); i++){
                if (myChatServer.slist.get(i) != outToClient){
                    myChatServer.slist.get(i).writeBytes("SERVER: TCP Client " + threadID + " has connected to the chat room\n");
                }else{
                    myChatServer.slist.get(i).writeBytes("SERVER: You have connected to the chat room\n");
                }
            }
            String greeting = "SERVER: TCP Client " + threadID + " has connected to the chat room\n";
            byte[] sendData = greeting.getBytes();
            for (int i = 0; i < myChatServer.UDPList.size(); i++){
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, myChatServer.UDPList.get(i).IPAddress, myChatServer.UDPList.get(i).port);
                myChatServer.UDPSocket.send(sendPacket);
            }

        	}
        while (h.responderMethod(connectionSocket, threadID, outToClient, myChatServer)) {
            
        }
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            connectionSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(MyServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}

class Responder {
    
    String serverSentence;
    
    // on client process termination or
    // client sends EXIT then to return false to close connection
    // else return true to keep connection alive
    // and continue conversation
    public boolean responderMethod(Socket connectionSocket, int threadID, DataOutputStream outToClient, StartServer myChatServer) {
        try {
            BufferedReader inFromClient =
            new BufferedReader(
                               new InputStreamReader(
                                                     connectionSocket.getInputStream()));
            String clientSentence = inFromClient.readLine();
            synchronized (this){
            
            // if client process terminates it get null, so close connection
            if (clientSentence == null || clientSentence.equals("EXIT")) {
                myChatServer.slist.remove(outToClient);
                for (int i = 0; i < myChatServer.slist.size(); i++){
                    if (myChatServer.slist.get(i) != outToClient){
                        myChatServer.slist.get(i).writeBytes("SERVER: TCP Client " + threadID + " has disconnected from the chat room");
                    }
                }

                return false;
            }
            
            if (clientSentence != null) {
                System.out.println("TCP Client " + threadID + " : " + clientSentence);
            }
            
            serverSentence = "TCP Client " + threadID + " : " + clientSentence + '\n';
            
            for (int i = 0; i < myChatServer.slist.size(); i++){
                if (myChatServer.slist.get(i) != outToClient){
                    myChatServer.slist.get(i).writeBytes(serverSentence);
                }
            }
            
            byte[] sendData = serverSentence.getBytes();
            for (int i = 0; i < myChatServer.UDPList.size(); i++){
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, myChatServer.UDPList.get(i).IPAddress, myChatServer.UDPList.get(i).port);
                myChatServer.UDPSocket.send(sendPacket);
            }

            
            return true;
          } 
        } catch (SocketException e) {
            System.out.println("Disconnected");
            myChatServer.slist.remove(outToClient);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            myChatServer.slist.remove(outToClient);
            return false;
        }
    }
}