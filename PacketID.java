/*******************************
 Plattenberger, Joseph
 A-option
 *******************************/
import java.net.*;
public class PacketID {
    InetAddress IPAddress;
    int port;
    
    public PacketID(InetAddress IPAddress, int port){
        this.IPAddress = IPAddress;
        this.port = port;
    }
    public InetAddress getPacketAddress(){
        return IPAddress;
    }
    public int getPacketPort(){
        return port;
    }
}