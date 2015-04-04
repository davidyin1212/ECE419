import java.io.Serializable;
import java.net.InetAddress;


public class ClientInfo implements Serializable{
	
	public String username;
	public InetAddress addr;
	public int portNo;
	
	public String toString() {
		return "<" + username +"> addr:" + addr.toString() + " portNo:" + portNo;
	}
	
	public boolean equals(Object o) {
		
		if (o instanceof ClientInfo) {
			ClientInfo ci = (ClientInfo) o;
			return this.username.equals(ci.username) &&
					this.addr.equals(ci.addr) &&
					this.portNo == ci.portNo;
		}
		
		return false;
	}
}
