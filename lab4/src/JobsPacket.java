package dict.attack;
import java.io.Serializable;


public class JobsPacket implements Serializable {
    public static final int DEFAULT = 0;
    public static final int STATUS = 100;
    public static final int REQUEST = 101;
    public static final int QUERY = 102;
    public static final int PROGRESS = 103;
    public static final int QUIT = 104;
    public static final int REPLY = 105;
    public static final int COMPLETE = 106;

    public int type;
    public String hash;
    public String result;
}