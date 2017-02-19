/*******************************
 Plattenberger, Joseph
 A-option
 *******************************/
import java.io.*;
import java.net.*;

public class UDPChatClient {
    
    public static void main(String args[]) throws Exception {
        
        BufferedReader inFromUser =
        new BufferedReader(new InputStreamReader(System.in));
        
        DatagramSocket socket = new DatagramSocket();
        
        InetAddress IPAddress = InetAddress.getByName("localhost");
        new Listener(IPAddress, socket).start();
        //send connection ping
        String ping = "UDP connection ping received";
        byte [] sendPing = ping.getBytes(); 
        DatagramPacket sendPingPacket = new DatagramPacket(sendPing, sendPing.length, IPAddress, 4445);
        socket.send(sendPingPacket);
        
        while(true){
            byte[] sendData = new byte[1024];
        
            String sentence = inFromUser.readLine();
            sendData = sentence.getBytes();
        
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4445);
            socket.send(sendPacket);
            
            if (sentence.equals("EXIT")) {
                break;
            }
            
        }
        socket.close();
    }
}

class Listener extends Thread {
    
    InetAddress IPAddress;
    DatagramSocket socket;
    
    public Listener(InetAddress IPAddress, DatagramSocket socket) {
        this.IPAddress = IPAddress;
        this.socket = socket;
    }

    public void run(){
            
        while(true){
            try{
                byte[] receiveData = new byte[1024];

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
        
                String modifiedSentence = new String(receivePacket.getData());
                
                System.out.println(modifiedSentence);
        
            }catch (Exception e){
                socket.close();
                e.printStackTrace();
                break;
            }
        }
    }
}