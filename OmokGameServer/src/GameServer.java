import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JButton;

public class GameServer extends JFrame {

	private JPanel contentPane;
	private JTextField txtPortNumber;
	public JTextArea textArea;
	
	private ServerSocket socket;
	private Socket client_socket;
	private Vector UserVec = new Vector();
	private Vector RoomObjList = new Vector();
	private Vector RoomTitleList = new Vector();
	private int lastRow;
	private int lastCol;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GameServer frame = new GameServer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GameServer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 440);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 298);
		contentPane.add(scrollPane);
		
		textArea = new JTextArea();
		textArea.setBounds(13, 318, 87, 26);
		scrollPane.setViewportView(textArea);
		
		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(13, 318, 87, 26);
		contentPane.add(lblNewLabel);
		
		txtPortNumber = new JTextField();
		txtPortNumber.setText("30000");
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setBounds(112, 318, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);
		
		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.setBounds(12, 356, 300, 35);
		contentPane.add(btnServerStart);
		btnServerStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				AppendText("Chat Server Running..");
				btnServerStart.setText("Chat Server Running..");
				btnServerStart.setEnabled(false);
				txtPortNumber.setEnabled(false);
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
			
		});
	}
	
	// ���ο� ������ accept() �ϰ� user thread�� ���� �����Ѵ�.
	class AcceptServer extends Thread {
		public void run() {
			while (true) {
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept();
					AppendText("���ο� ������ from " + client_socket);
					// User�� �ϳ��� Thread ����
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // ���ο� ������ �迭�� �߰�
					new_user.start(); // ���� ��ü�� ������ ����
					AppendText("���� ������ �� " + UserVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void AppendText(String str) {
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}
	
	public synchronized void AppendObject(Msg mg) {
		textArea.append("userName = " + mg.userName + "\n");
		textArea.append("code = " + mg.code + "\n");
		textArea.append("data = " + mg.data + "\n");
	}
	
	class UserService extends Thread {
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		
		private Socket client_socket;
		private Vector user_vc;
		public String UserName = "";
		
		public UserService(Socket client_socket) {
			this.client_socket = client_socket;
			this.user_vc = UserVec;
			try {
				oos = new ObjectOutputStream(client_socket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(client_socket.getInputStream());
			} catch (IOException e) {
				AppendText("userService error");
			}
		}
		
		public void Login() {
			AppendText("���ο� ������ " + UserName + " ����.");
			AppendText("���� ������ �� : " + UserVec.size());
			Msg mg = new Msg("Server", "200", "Sending room list");
			mg.roomTitleList = RoomTitleList;
			WriteOneObject(mg);
		}
		public void Logout() {
			AppendText("[" + UserName + "]�� ����.");
			UserVec.removeElement(this);
			AppendText("���� ������ �� : " + UserVec.size());
		}
		
		public synchronized void WriteOneObject(Object ob) {
			try {
				oos.writeObject(ob);
				oos.reset();
			} catch (IOException e) {
				AppendText("oos.writeObject(ob) error");
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		public void WriteAllObject(Object ob) {
			for (int i = 0; i < UserVec.size(); i++) {
				UserService user = (UserService) UserVec.elementAt(i);
				user.WriteOneObject(ob);
			}
		}
		
		public void WriteRoomObject(int roomNum, Msg mg) {
			Room room = (Room) RoomObjList.elementAt(roomNum);
			for (int i = 0; i < room.players.size(); i++) {
				UserSend(room.players.elementAt(i).UserName, mg);
			}
			for (int i = 0; i < room.viewers.size(); i++) {
				UserSend(room.viewers.elementAt(i).UserName, mg);
			}
		}
		
		public void UserSend(String uName, Msg mg) { // �ش� �̸��� ���� �������� �޼��� ����
			UserService user;
			for (int i = 0; i < UserVec.size(); i++) {
				user = (UserService) UserVec.elementAt(i);
				if (user.UserName.matches(uName)) {
					user.WriteOneObject(mg);
				}
			}
		}
		
		public void ShowRoomPlayers(Room room) {
			AppendText("-�÷��̾� ���-");
			for (int i = 0; i < room.players.size(); i++) {
				UserService user = (UserService) room.players.elementAt(i);
				AppendText(user.UserName);
			}
			AppendText("-������ ���-");
			for (int i = 0; i < room.viewers.size(); i++) {
				UserService user = (UserService) room.viewers.elementAt(i);
				AppendText(user.UserName);
			}
		}
		
		public void SendStone(Room room, Msg mg) {
			for (int i = 0; i < room.players.size(); i++) {
				room.players.elementAt(i).WriteOneObject(mg);
				//AppendText(room.players.elementAt(i).UserName + "���� �� ����..");
			}
			for (int i = 0; i < room.viewers.size(); i++) {
				room.viewers.elementAt(i).WriteOneObject(mg);
				//AppendText(room.viewers.elementAt(i).UserName + "���� �� ����..");
			}
		}
		
		public String leftCheck(Room room, int i, int j) { // ���� �������� ��ġ�� ���� ĭ�� �������� üũ
			int board[][] = room.board;
			String result;
			
			if (i == 0) { // ���� ��� 
				result = "end";
				return result;
			}
			
			int left = board[i-1][j];
			if (board[i][j] == left) { // �� ���� ���
				result = "mine";
			}
			else { // �� ĭ�̰ų�, ��� ���� ���
				if (left == 0)
					result = "empty";
				else
					result = "opp";
			}
			
			return result;
		}
		public String rightCheck(Room room, int i, int j) { // ���� �������� ��ġ�� ������ ĭ�� �������� üũ
			int board[][] = room.board;
			String result;
			
			if (i == 14) {
				result = "end";
				return result;
			}
			
			int right = board[i+1][j];
			if (board[i][j] == right) {
				result = "mine";
			}
			else {
				if (right == 0)
					result = "empty";
				else
					result = "opp";
			}
			
			return result;
		}
		public String upCheck(Room room, int i, int j) {
			int board[][] = room.board;
			String result;
			
			if (j == 0) {
				result = "end";
				return result;
			}
			
			int up = board[i][j-1];
			if (board[i][j] == up) {
				result = "mine";
			}
			else {
				if (up == 0)
					result = "empty";
				else
					result = "opp";
			}
			
			return result;
		}
		public String downCheck(Room room, int i, int j) {
			int board[][] = room.board;
			String result;
			
			if (j == 14) {
				result = "end";
				return result;
			}
			
			int down = board[i][j+1];
			if (board[i][j] == down) {
				result = "mine";
			}
			else {
				if (down == 0)
					result = "empty";
				else
					result = "opp";
			}
			
			return result;
		}
		public String northwestCheck(Room room, int i, int j) {
			int board[][] = room.board;
			String result;
			
			if (i == 0 || j == 0) {
				result = "end";
				return result;
			}
			
			int northwest = board[i-1][j-1];
			if (board[i][j] == northwest) {
				result = "mine";
			}
			else {
				if (northwest == 0)
					result = "empty";
				else
					result = "opp";
			}
			
			return result;
		}
		public String southwestCheck(Room room, int i, int j) {
			int board[][] = room.board;
			String result;
			
			if (i == 0 || j == 14) {
				result = "end";
				return result;
			}
			
			int southwest = board[i-1][j+1];
			if (board[i][j] == southwest) {
				result = "mine";
			}
			else {
				if (southwest == 0)
					result = "empty";
				else
					result = "opp";
			}
			
			return result;
		}
		public String northeastCheck(Room room, int i, int j) {
			int board[][] = room.board;
			String result;
			
			if (i == 14 || j == 0) {
				result = "end";
				return result;
			}
			
			int northeast = board[i+1][j-1];
			if (board[i][j] == northeast) {
				result = "mine";
			}
			else {
				if (northeast == 0)
					result = "empty";
				else
					result = "opp";
			}
			
			return result;
		}
		public String southeastCheck(Room room, int i, int j) {
			int board[][] = room.board;
			String result;
			
			if (i == 14 || j == 14) {
				result = "end";
				return result;
			}
			
			int southeast = board[i+1][j+1];
			if (board[i][j] == southeast) {
				result = "mine";
			}
			else {
				if (southeast == 0)
					result = "empty";
				else
					result = "opp";
			}
			
			return result;
		}
		
		public String StateCheck(Room room, int i, int j) {
			int cnt = 1;
			int three = 0;
			int four = 0;
			int pos_i = i;
			int pos_j = j;
			String state;
			boolean blocked1 = false;
			boolean blocked2 = false;
			
			// ���� üũ.
			while (true) {
				if (leftCheck(room, i, j).matches("mine")) { // ���� ���� üũ
					cnt++;
					i--;
				}
				else {
					if (!(leftCheck(room, i, j).matches("empty")))
						blocked1 = true;
					
					i = pos_i;
					while (true) {
						if (rightCheck(room, i, j).matches("mine")) { // ������ üũ
							cnt++;
							i++;
						}
						else { // �� �� üũ �Ϸ�
							if (!(rightCheck(room, i, j).matches("empty")))
								blocked2 = true;
							
							if (cnt == 5) { // ���
								state = "win";
								return state;
							}
							else if (cnt >= 6) { // ���
								state = "long";
								return state;
							}
							else if (cnt == 3) { // �� ���� üũ
								if (!blocked1 && !blocked2)
									three++;
								break;
							}
							else if (cnt == 4) { // �� ���� üũ
								if (blocked1 && blocked2)
									break;
								four++;
								break;
							}
							else
								break;
						}
					} break;
				}
			} // ���� üũ ��
			
			
			// ���� üũ.
			cnt = 1;
			i = pos_i;
			j = pos_j;
			blocked1 = false;
			blocked2 = false;
			while (true) {
				if (upCheck(room, i, j).matches("mine")) { // ���� ���� üũ
					cnt++;
					j--;
				}
				else {
					if (!(upCheck(room, i, j).matches("empty")))
							blocked1 = true;
					
					j = pos_j;
					while (true) {
						if (downCheck(room, i, j).matches("mine")) { // �Ʒ��� üũ
							cnt++;
							j++;
						}
						else { // ���Ʒ� üũ �Ϸ�
							if (!(downCheck(room, i, j).matches("empty")))
									blocked2 = true;
							
							if (cnt == 5) {
								state = "win";
								return state;
							}
							else if (cnt >= 6) {
								state = "long";
								return state;
							}
							else if (cnt == 3) { // �� ���� üũ
								if (!blocked1 && !blocked2)
									three++;
								break;
							}
							else if (cnt == 4) { // �� ���� üũ
								if (blocked1 && blocked2)
									break;
								four++;
								break;
							}
							else
								break;
						}
					} break;
				}
			} // ���� üũ ��
			
			// �밢��1(/����) üũ.
			cnt = 1;
			i = pos_i;
			j = pos_j;
			blocked1 = false;
			blocked2 = false;
			while (true) {
				if (northeastCheck(room, i, j).matches("mine")) { // ���� ���� üũ
					cnt++;
					i++;
					j--;
				}
				else {
					if (!(northeastCheck(room, i, j).matches("empty")))
						blocked1 = true;
					
					i = pos_i;
					j = pos_j;
					while (true) {
						if (southwestCheck(room, i, j).matches("mine")) { // �Ʒ��� üũ
							cnt++;
							i--;
							j++;
						}
						else { // ���Ʒ� üũ �Ϸ�
							if (!(southwestCheck(room, i, j).matches("empty")))
								blocked2 = true;
							
							if (cnt == 5) {
								state = "win";
								return state;
							}
							else if (cnt >= 6) {
								state = "long";
								return state;
							}
							else if (cnt == 3) { // �� ���� üũ
								if (!blocked1 && !blocked2)
									three++;
								break;
							}
							else if (cnt == 4) { // �� ���� üũ
								if (blocked1 && blocked2)
									break;
								four++;
								break;
							}
							else
								break;
						}
					} break;
				}
			} // �밢��1 üũ ��
			
			// �밢��2(\����) üũ.
			cnt = 1;
			i = pos_i;
			j = pos_j;
			blocked1 = false;
			blocked2 = false;
			while (true) {
				if (northwestCheck(room, i, j).matches("mine")) { // ���� ���� üũ
					cnt++;
					i--;
					j--;
				}
				else {
					if (!(northwestCheck(room, i, j).matches("empty")))
						blocked1 = true;
					
					i = pos_i;
					j = pos_j;
					while (true) {
						if (southeastCheck(room, i, j).matches("mine")) { // �Ʒ��� üũ
							cnt++;
							
							i++;
							j++;
						}
						else { // ���Ʒ� üũ �Ϸ�
							if (!(southeastCheck(room, i, j).matches("empty")))
								blocked2 = true;
							
							if (cnt == 5) {
								state = "win";
								return state;
							}
							else if (cnt >= 6) {
								state = "long";
								return state;
							}
							else if (cnt == 3) { // �� ���� üũ
								if (!blocked1 && !blocked2)
									three++;
								break;
							}
							else if (cnt == 4) { // �� ���� üũ
								if (blocked1 && blocked2)
									break;
								four++;
								break;
							}
							else
								break;
						}
					} break;
				}
			} // �밢��2 üũ ��
			
			if (three >= 2) {
				state = "doubleThree";
				return state;
			}
			else if (four >= 2) {
				state = "doubleFour";
				return state;
			}
			
			state = "normal";
			return state;
		}
		
		public void run() {
			while (true) {
				Object obm = null;
				Msg mg = null;
				if (socket == null)
					break;
				try {
					obm = ois.readObject();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
					return;
				}
				if (obm == null)
					break;
				if (obm instanceof Msg) {
					mg = (Msg) obm;
					AppendObject(mg);
				} else
					continue;
				if (mg.code.matches("100")) {
					UserName = mg.userName;
					Login();
				}
				else if (mg.code.matches("200")) { // �� ���� ��û �޼���.
					Room new_room = new Room(mg.userName); // ȣ��Ʈ�� �ڵ����� �濡 �����
					new_room.roomTitle = mg.data;
					RoomObjList.add(new_room);
					RoomTitleList.add(new_room.roomTitle);
					
					AppendText(new_room.roomNum + "��° ���� �����Ǿ����ϴ�.");
					AppendText("�� ���� : " + new_room.roomTitle);
					AppendText("���� �ο� : " + (new_room.players.size() + new_room.viewers.size()) + "��");
					AppendText("������ ��� : ");
					ShowRoomPlayers(new_room);
					
					Msg mg2 = new Msg("Server", "200", "Created");
					mg2.roomTitleList = RoomTitleList;
					WriteAllObject(mg2);
				}
				else if (mg.code.matches("300")) { // �÷��̾�� �� ���� ��û
					int roomNum = mg.roomNum;
					Room room = (Room) RoomObjList.elementAt(roomNum);
					UserService user;
					for (int i = 0; i < UserVec.size(); i++) {
						user = (UserService) UserVec.elementAt(i);
						if (user.UserName.matches(mg.userName)) {
							room.players.add(user); // �÷��̾�� �߰�
							AppendText("�÷��̾� [" + mg.userName + "] " + roomNum + "�� �濡 ����.");
							
							String str = mg.userName + "���� �����Ͽ����ϴ�.";
							Msg mg2 = new Msg("Server", "900", str);
							WriteRoomObject(mg.roomNum, mg2);
							break;
						}
					}
					ShowRoomPlayers(room);
					
					if (room.players.size() == 2) { // ���� ����. ȣ��Ʈ�� ��
						Msg mg2 = new Msg("Server", "550", "your turn");
						String uName = room.players.elementAt(0).UserName;
						UserSend(uName, mg2);
					}
				}
				else if (mg.code.matches("310")) { // ������ ���� ��û
					int roomNum = mg.roomNum;
					Room room = (Room) RoomObjList.elementAt(roomNum);
					UserService user;
					for (int i = 0; i < UserVec.size(); i++) {
						user = (UserService) UserVec.elementAt(i);
						if (user.UserName.matches(mg.userName)) {
							room.viewers.add(user);
							AppendText("������ [" + mg.userName + "] " + roomNum + "�� �濡 ����.");
							
							String str = mg.userName + "���� �����Ͽ����ϴ�.";
							Msg mg3 = new Msg("Server", "900", str);
							WriteRoomObject(mg.roomNum, mg3);
							break;
						}
					}
					ShowRoomPlayers(room);
				}
				else if (mg.code.matches("400")) { // �α׾ƿ� ��û
					Logout();
					break;
				}
				else if (mg.code.matches("500")) { // Ŭ���̾�Ʈ�� ȭ�� Ŭ��
					AppendText("[" + mg.userName + "] Ŭ�� : (" + mg.clickedX + ", " + mg.clickedY + ")");
					
					String uName = mg.userName;
					int stoneX = 0;
					int stoneY = 0;
					Room room = (Room) RoomObjList.elementAt(mg.roomNum);
					boolean win = false;
					boolean send = true;
					
					for (int i = 1; i < 16; i++) { // ��ġ ������ �� �� ��ġ ���� ����
						if ( mg.clickedX >= 25*i - 10 && mg.clickedX <= 25*i + 10) {
							stoneX = 25*i;
							AppendText("x��ġ : " + stoneX);
							for (int j = 1; j < 16; j++) {
								if (mg.clickedY >= 25*j - 10 && mg.clickedY <= 25*j + 10) {
									stoneY = 25*j;
									AppendText("y��ġ : " + stoneY);
									if (mg.stoneColor.matches("black")) {
										room.board[i-1][j-1] = 1;
										lastCol = i-1; lastRow = j-1;
										AppendText(StateCheck(room, i-1, j-1));
										if (StateCheck(room, i-1, j-1).matches("win")) { // �� �¸�
											AppendText("Black Win!");
											win = true;
										}
										else if (StateCheck(room, i-1, j-1).matches("long")) { // ��� ����
											Msg mg2 = new Msg("Server", "510", "long");
											UserSend(uName, mg2);
											room.board[i-1][j-1] = 0;
											send = false;
										}
										else if (StateCheck(room, i-1, j-1).matches("doubleThree")) { // ��� ����
											Msg mg2 = new Msg("Server", "530", "doubleThree");
											UserSend(uName, mg2);
											room.board[i-1][j-1] = 0;
											send = false;
										}
										else if (StateCheck(room, i-1, j-1).matches("doubleFour")) { // ��� ����
											Msg mg2 = new Msg("Server", "540", "doubleFour");
											UserSend(uName, mg2);
											room.board[i-1][j-1] = 0;
											send = false;
										}
									}
									else if (mg.stoneColor.matches("white")) {
										room.board[i-1][j-1] = 2;
										lastCol = i-1; lastRow = j-1;
										AppendText(StateCheck(room, i-1, j-1));
										if (StateCheck(room, i-1, j-1).matches("win")) { // �� �¸�
											AppendText("White Win!");
											win = true;
										}
										else if (StateCheck(room, i-1, j-1).matches("long")) { // ���� ��� ����
											AppendText("White Win!");
											win = true;
										}
									} break;
								}
							} break;
						}
					} //for�� ��
					if (stoneX * stoneY != 0 && send) { // �������� ��ġ ����
						Msg mg2 = new Msg(mg.userName, "500", mg.stoneColor);
						mg2.clickedX = stoneX;
						mg2.clickedY = stoneY;
						
						SendStone(room, mg2);
						AppendText("�� ��ġ : (" + stoneX + ", " + stoneY + " )");
						
						for (int i = 0; i < room.players.size(); i++) {
							String name = room.players.elementAt(i).UserName;
							if (name.matches(uName)) {
								Msg mg3 = new Msg("Server", "560", "wait");
								UserSend(name, mg3);
							}
							else {
								Msg mg3 = new Msg("Server", "550", "your turn");
								UserSend(name, mg3);
							}
						}
					}
					
					// �¸� ���� �޼���
					if (win) {
						room.gameEnd = true;
						Msg mg2 = new Msg("Server", "700", mg.userName);
						WriteRoomObject(mg.roomNum, mg2);
					}
				}
				else if (mg.code.matches("600")) { // ������ ��û
					String uName = mg.userName;
					int rNum = mg.roomNum;
					
					Msg mg2 = new Msg(uName, "600", "back request");
					
					Room room = (Room) RoomObjList.elementAt(rNum);
					for (int i = 0; i < room.players.size(); i++) {
						if (!(room.players.elementAt(i).UserName.matches(uName))) {
							room.players.elementAt(i).WriteOneObject(mg);
							AppendText(room.players.elementAt(i).UserName + "���� ������ ��û ����");
						}
					}
				}
				else if (mg.code.matches("610")) { // ������ ����
					Room room = (Room) RoomObjList.elementAt(mg.roomNum);
					Msg mg2 = new Msg("Server", "610", "back");
					WriteRoomObject(mg.roomNum, mg2);
					room.board[lastCol][lastRow] = 0;
				}
				else if (mg.code.matches("800")) { // �� ���� ��û
					String uName = mg.userName;
					int rNum = mg.roomNum;
					Room room = (Room) RoomObjList.elementAt(rNum);
					
					// �ش� ������ �÷��̾��� ��
					for (int i = 0; i < room.players.size(); i++) { // �ش� ���� �÷��̾� ��Ͽ��� �ش� ���� ����
						if (room.players.elementAt(i).UserName.matches(uName)) {
							if (room.players.size() == 2 && !room.gameEnd) { // �÷��̾��� ���
								room.players.removeElementAt(i);
								String name = room.players.elementAt(0).UserName;
								Msg mg2 = new Msg(uName, "810", "give up");
								UserSend(name, mg2);
								for (int j = 0; j < room.viewers.size(); j++) {
									Msg mg3 = new Msg(uName, "810", "give up");
									UserSend(room.viewers.elementAt(j).UserName, mg3);
								}
								
							}
							else { // �ܼ� ����
								room.players.removeElementAt(i);
							}
							
							if (room.players.size() == 0) {
								// ���� �÷��̾ 0���̸� �� ��ü ����
								RoomObjList.removeElementAt(rNum);
								RoomTitleList.removeElementAt(rNum);
								AppendText(rNum + "��° �� ����.");
								Msg mg2 = new Msg("Server", "200", "Refresh Lobby");
								mg2.roomTitleList = RoomTitleList;
								WriteAllObject(mg2);
							}
							else {
								String str = uName + "���� �����Ͽ����ϴ�.";
								Msg mg3 = new Msg("Server", "900", str);
								for (int j = 0; j < room.viewers.size(); j++) {
									UserSend(room.viewers.elementAt(j).UserName, mg3);
								}
							}
							break;
						}
					}
					
					// �ش� ������ �������� ��
					for (int i = 0; i < room.viewers.size(); i++) {
						if (room.viewers.elementAt(i).UserName.matches(uName)) {
							room.viewers.removeElementAt(i);
							String str = uName + "���� �����Ͽ����ϴ�.";
							Msg mg3 = new Msg("Server", "900", str);
							WriteRoomObject(mg.roomNum, mg3);
							break;
						}
					}
					Msg mg2 = new Msg("Server", "800", "Exit room");
					UserSend(uName, mg2);
					AppendText(uName + "���� " + rNum +"�� �� ����.");
					
				} // 800 ��
				else if (mg.code.matches("900")) { // ä��
					String str = "[" + mg.userName + "] " + mg.data;
					AppendText(mg.roomNum +"�� �� : " + str);
					
					Msg mg2 = new Msg(mg.userName, "900", str);
					WriteRoomObject(mg.roomNum, mg2);
				}
			} // while�� ��
		} // run() ��
	} //UserService ��
	
	public class Room extends Thread {
		public String roomTitle;
		public int roomNum;
		public Vector<UserService> players = new Vector<UserService>();
		public Vector<UserService> viewers = new Vector<UserService>();
		public int[][] board = new int[15][15]; //0���� �ʱ�ȭ
		public boolean gameEnd = false;
		
		public Room(String hostName) {
			for (int i=0; i < UserVec.size(); i++) { // ȣ��Ʈ�� �÷��̾ �߰�
				UserService user = (UserService) UserVec.elementAt(i);
				if (hostName.matches(user.UserName)) {
					players.add(user);
				}
			}
			roomNum = RoomObjList.size();
		}
		
		
	}
}
