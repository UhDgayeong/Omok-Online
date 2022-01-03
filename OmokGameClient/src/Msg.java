import java.io.Serializable;
import java.util.Vector;

public class Msg implements Serializable {
	private static final long serialVersionUID = 1L;
	public String userName;
	public String code;
	public String data;
	public Vector<String> roomTitleList;
	public Vector<Object> roomObjList;
	public int roomNum;
	public int clickedX;
	public int clickedY;
	public String stoneColor;
	
	public Msg(String userName, String code, String data) {
		this.userName = userName;
		this.code = code;
		this.data = data;
	}
}
