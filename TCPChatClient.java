/*******************************
 Plattenberger, Joseph
 A-option
 *******************************/
import java.io.*;
import java.net.*;
public class TCPChatClient {
    
    public static void main(String argv[]) throws Exception {
        String sentence;
        
        BufferedReader inFromUser =
        new BufferedReader(
                           new InputStreamReader(System.in));
        
        Socket clientSocket = new Socket("localhost", 6789);
        
        TCPListener l = new TCPListener(clientSocket);
        l.start();
        
        while (true) {
            DataOutputStream outToServer =
            new DataOutputStream(
                                 clientSocket.getOutputStream());
            
            sentence = inFromUser.readLine();
            
            outToServer.writeBytes(sentence + '\n');
            
            if (sentence.equals("EXIT")) {
                break;
            }
            
        }
        clientSocket.close();
    }
}

class TCPListener extends Thread {
    
    Socket clientSocket;
    
    public TCPListener(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    public void run() {
        String modifiedSentence;
        while(true){
            try{
                BufferedReader inFromServer =
                    new BufferedReader(
                           new InputStreamReader(
                                        clientSocket.getInputStream()));
                modifiedSentence = inFromServer.readLine();
        
                System.out.println(modifiedSentence);
            } catch (Exception e){
                e.printStackTrace();
                break;
            }
        }
    }

}