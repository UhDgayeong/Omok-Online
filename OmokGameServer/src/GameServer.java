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
	
	// 새로운 참가자 accept() 하고 user thread를 새로 생성한다.
	class AcceptServer extends Thread {
		public void run() {
			while (true) {
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept();
					AppendText("새로운 참가자 from " + client_socket);
					// User당 하나씩 Thread 생성
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // 새로운 참가자 배열에 추가
					new_user.start(); // 만든 객체의 스레드 실행
					AppendText("현재 참가자 수 " + UserVec.size());
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
			AppendText("새로운 참가자 " + UserName + " 입장.");
			AppendText("현재 참가자 수 : " + UserVec.size());
			Msg mg = new Msg("Server", "200", "Sending room list");
			mg.roomTitleList = RoomTitleList;
			WriteOneObject(mg);
		}
		public void Logout() {
			AppendText("[" + UserName + "]님 퇴장.");
			UserVec.removeElement(this);
			AppendText("현재 참가자 수 : " + UserVec.size());
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
		
		public void UserSend(String uName, Msg mg) { // 해당 이름을 가진 유저에게 메세지 전송
			UserService user;
			for (int i = 0; i < UserVec.size(); i++) {
				user = (UserService) UserVec.elementAt(i);
				if (user.UserName.matches(uName)) {
					user.WriteOneObject(mg);
				}
			}
		}
		
		public void ShowRoomPlayers(Room room) {
			AppendText("-플레이어 목록-");
			for (int i = 0; i < room.players.size(); i++) {
				UserService user = (UserService) room.players.elementAt(i);
				AppendText(user.UserName);
			}
			AppendText("-관전자 목록-");
			for (int i = 0; i < room.viewers.size(); i++) {
				UserService user = (UserService) room.viewers.elementAt(i);
				AppendText(user.UserName);
			}
		}
		
		public void SendStone(Room room, Msg mg) {
			for (int i = 0; i < room.players.size(); i++) {
				room.players.elementAt(i).WriteOneObject(mg);
				//AppendText(room.players.elementAt(i).UserName + "에게 돌 전송..");
			}
			for (int i = 0; i < room.viewers.size(); i++) {
				room.viewers.elementAt(i).WriteOneObject(mg);
				//AppendText(room.viewers.elementAt(i).UserName + "에게 돌 전송..");
			}
		}
		
		public String leftCheck(Room room, int i, int j) { // 현재 놓으려는 위치의 왼쪽 칸이 무엇인지 체크
			int board[][] = room.board;
			String result;
			
			if (i == 0) { // 벽인 경우 
				result = "end";
				return result;
			}
			
			int left = board[i-1][j];
			if (board[i][j] == left) { // 내 돌인 경우
				result = "mine";
			}
			else { // 빈 칸이거나, 상대 돌인 경우
				if (left == 0)
					result = "empty";
				else
					result = "opp";
			}
			
			return result;
		}
		public String rightCheck(Room room, int i, int j) { // 현재 놓으려는 위치의 오른쪽 칸이 무엇인지 체크
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
			
			// 가로 체크.
			while (true) {
				if (leftCheck(room, i, j).matches("mine")) { // 왼쪽 먼저 체크
					cnt++;
					i--;
				}
				else {
					if (!(leftCheck(room, i, j).matches("empty")))
						blocked1 = true;
					
					i = pos_i;
					while (true) {
						if (rightCheck(room, i, j).matches("mine")) { // 오른쪽 체크
							cnt++;
							i++;
						}
						else { // 양 옆 체크 완료
							if (!(rightCheck(room, i, j).matches("empty")))
								blocked2 = true;
							
							if (cnt == 5) { // 우승
								state = "win";
								return state;
							}
							else if (cnt >= 6) { // 장목
								state = "long";
								return state;
							}
							else if (cnt == 3) { // 삼 조건 체크
								if (!blocked1 && !blocked2)
									three++;
								break;
							}
							else if (cnt == 4) { // 사 조건 체크
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
			} // 가로 체크 끝
			
			
			// 세로 체크.
			cnt = 1;
			i = pos_i;
			j = pos_j;
			blocked1 = false;
			blocked2 = false;
			while (true) {
				if (upCheck(room, i, j).matches("mine")) { // 위쪽 먼저 체크
					cnt++;
					j--;
				}
				else {
					if (!(upCheck(room, i, j).matches("empty")))
							blocked1 = true;
					
					j = pos_j;
					while (true) {
						if (downCheck(room, i, j).matches("mine")) { // 아래쪽 체크
							cnt++;
							j++;
						}
						else { // 위아래 체크 완료
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
							else if (cnt == 3) { // 삼 조건 체크
								if (!blocked1 && !blocked2)
									three++;
								break;
							}
							else if (cnt == 4) { // 사 조건 체크
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
			} // 세로 체크 끝
			
			// 대각선1(/방향) 체크.
			cnt = 1;
			i = pos_i;
			j = pos_j;
			blocked1 = false;
			blocked2 = false;
			while (true) {
				if (northeastCheck(room, i, j).matches("mine")) { // 위쪽 먼저 체크
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
						if (southwestCheck(room, i, j).matches("mine")) { // 아래쪽 체크
							cnt++;
							i--;
							j++;
						}
						else { // 위아래 체크 완료
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
							else if (cnt == 3) { // 삼 조건 체크
								if (!blocked1 && !blocked2)
									three++;
								break;
							}
							else if (cnt == 4) { // 사 조건 체크
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
			} // 대각선1 체크 끝
			
			// 대각선2(\방향) 체크.
			cnt = 1;
			i = pos_i;
			j = pos_j;
			blocked1 = false;
			blocked2 = false;
			while (true) {
				if (northwestCheck(room, i, j).matches("mine")) { // 위쪽 먼저 체크
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
						if (southeastCheck(room, i, j).matches("mine")) { // 아래쪽 체크
							cnt++;
							
							i++;
							j++;
						}
						else { // 위아래 체크 완료
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
							else if (cnt == 3) { // 삼 조건 체크
								if (!blocked1 && !blocked2)
									three++;
								break;
							}
							else if (cnt == 4) { // 사 조건 체크
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
			} // 대각선2 체크 끝
			
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
				else if (mg.code.matches("200")) { // 방 생성 요청 메세지.
					Room new_room = new Room(mg.userName); // 호스트는 자동으로 방에 입장됨
					new_room.roomTitle = mg.data;
					RoomObjList.add(new_room);
					RoomTitleList.add(new_room.roomTitle);
					
					AppendText(new_room.roomNum + "번째 방이 생성되었습니다.");
					AppendText("방 제목 : " + new_room.roomTitle);
					AppendText("참가 인원 : " + (new_room.players.size() + new_room.viewers.size()) + "명");
					AppendText("참가자 목록 : ");
					ShowRoomPlayers(new_room);
					
					Msg mg2 = new Msg("Server", "200", "Created");
					mg2.roomTitleList = RoomTitleList;
					WriteAllObject(mg2);
				}
				else if (mg.code.matches("300")) { // 플레이어로 방 입장 요청
					int roomNum = mg.roomNum;
					Room room = (Room) RoomObjList.elementAt(roomNum);
					UserService user;
					for (int i = 0; i < UserVec.size(); i++) {
						user = (UserService) UserVec.elementAt(i);
						if (user.UserName.matches(mg.userName)) {
							room.players.add(user); // 플레이어로 추가
							AppendText("플레이어 [" + mg.userName + "] " + roomNum + "번 방에 입장.");
							
							String str = mg.userName + "님이 입장하였습니다.";
							Msg mg2 = new Msg("Server", "900", str);
							WriteRoomObject(mg.roomNum, mg2);
							break;
						}
					}
					ShowRoomPlayers(room);
					
					if (room.players.size() == 2) { // 게임 시작. 호스트가 선
						Msg mg2 = new Msg("Server", "550", "your turn");
						String uName = room.players.elementAt(0).UserName;
						UserSend(uName, mg2);
					}
				}
				else if (mg.code.matches("310")) { // 관전자 입장 요청
					int roomNum = mg.roomNum;
					Room room = (Room) RoomObjList.elementAt(roomNum);
					UserService user;
					for (int i = 0; i < UserVec.size(); i++) {
						user = (UserService) UserVec.elementAt(i);
						if (user.UserName.matches(mg.userName)) {
							room.viewers.add(user);
							AppendText("관전자 [" + mg.userName + "] " + roomNum + "번 방에 입장.");
							
							String str = mg.userName + "님이 입장하였습니다.";
							Msg mg3 = new Msg("Server", "900", str);
							WriteRoomObject(mg.roomNum, mg3);
							break;
						}
					}
					ShowRoomPlayers(room);
				}
				else if (mg.code.matches("400")) { // 로그아웃 요청
					Logout();
					break;
				}
				else if (mg.code.matches("500")) { // 클라이언트가 화면 클릭
					AppendText("[" + mg.userName + "] 클릭 : (" + mg.clickedX + ", " + mg.clickedY + ")");
					
					String uName = mg.userName;
					int stoneX = 0;
					int stoneY = 0;
					Room room = (Room) RoomObjList.elementAt(mg.roomNum);
					boolean win = false;
					boolean send = true;
					
					for (int i = 1; i < 16; i++) { // 위치 재조정 및 돌 위치 정보 저장
						if ( mg.clickedX >= 25*i - 10 && mg.clickedX <= 25*i + 10) {
							stoneX = 25*i;
							AppendText("x위치 : " + stoneX);
							for (int j = 1; j < 16; j++) {
								if (mg.clickedY >= 25*j - 10 && mg.clickedY <= 25*j + 10) {
									stoneY = 25*j;
									AppendText("y위치 : " + stoneY);
									if (mg.stoneColor.matches("black")) {
										room.board[i-1][j-1] = 1;
										lastCol = i-1; lastRow = j-1;
										AppendText(StateCheck(room, i-1, j-1));
										if (StateCheck(room, i-1, j-1).matches("win")) { // 흑 승리
											AppendText("Black Win!");
											win = true;
										}
										else if (StateCheck(room, i-1, j-1).matches("long")) { // 장목 금지
											Msg mg2 = new Msg("Server", "510", "long");
											UserSend(uName, mg2);
											room.board[i-1][j-1] = 0;
											send = false;
										}
										else if (StateCheck(room, i-1, j-1).matches("doubleThree")) { // 삼삼 금지
											Msg mg2 = new Msg("Server", "530", "doubleThree");
											UserSend(uName, mg2);
											room.board[i-1][j-1] = 0;
											send = false;
										}
										else if (StateCheck(room, i-1, j-1).matches("doubleFour")) { // 사사 금지
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
										if (StateCheck(room, i-1, j-1).matches("win")) { // 백 승리
											AppendText("White Win!");
											win = true;
										}
										else if (StateCheck(room, i-1, j-1).matches("long")) { // 백은 장목 가능
											AppendText("White Win!");
											win = true;
										}
									} break;
								}
							} break;
						}
					} //for문 끝
					if (stoneX * stoneY != 0 && send) { // 재조정한 위치 전송
						Msg mg2 = new Msg(mg.userName, "500", mg.stoneColor);
						mg2.clickedX = stoneX;
						mg2.clickedY = stoneY;
						
						SendStone(room, mg2);
						AppendText("돌 위치 : (" + stoneX + ", " + stoneY + " )");
						
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
					
					// 승리 조건 달성시
					if (win) {
						room.gameEnd = true;
						Msg mg2 = new Msg("Server", "700", mg.userName);
						WriteRoomObject(mg.roomNum, mg2);
					}
				}
				else if (mg.code.matches("600")) { // 무르기 요청
					String uName = mg.userName;
					int rNum = mg.roomNum;
					
					Msg mg2 = new Msg(uName, "600", "back request");
					
					Room room = (Room) RoomObjList.elementAt(rNum);
					for (int i = 0; i < room.players.size(); i++) {
						if (!(room.players.elementAt(i).UserName.matches(uName))) {
							room.players.elementAt(i).WriteOneObject(mg);
							AppendText(room.players.elementAt(i).UserName + "에게 무르기 요청 전달");
						}
					}
				}
				else if (mg.code.matches("610")) { // 무르기 실행
					Room room = (Room) RoomObjList.elementAt(mg.roomNum);
					Msg mg2 = new Msg("Server", "610", "back");
					WriteRoomObject(mg.roomNum, mg2);
					room.board[lastCol][lastRow] = 0;
				}
				else if (mg.code.matches("800")) { // 방 퇴장 요청
					String uName = mg.userName;
					int rNum = mg.roomNum;
					Room room = (Room) RoomObjList.elementAt(rNum);
					
					// 해당 유저가 플레이어일 때
					for (int i = 0; i < room.players.size(); i++) { // 해당 방의 플레이어 목록에서 해당 유저 삭제
						if (room.players.elementAt(i).UserName.matches(uName)) {
							if (room.players.size() == 2 && !room.gameEnd) { // 플레이어의 기권
								room.players.removeElementAt(i);
								String name = room.players.elementAt(0).UserName;
								Msg mg2 = new Msg(uName, "810", "give up");
								UserSend(name, mg2);
								for (int j = 0; j < room.viewers.size(); j++) {
									Msg mg3 = new Msg(uName, "810", "give up");
									UserSend(room.viewers.elementAt(j).UserName, mg3);
								}
								
							}
							else { // 단순 퇴장
								room.players.removeElementAt(i);
							}
							
							if (room.players.size() == 0) {
								// 만약 플레이어가 0명이면 방 객체 없앰
								RoomObjList.removeElementAt(rNum);
								RoomTitleList.removeElementAt(rNum);
								AppendText(rNum + "번째 방 삭제.");
								Msg mg2 = new Msg("Server", "200", "Refresh Lobby");
								mg2.roomTitleList = RoomTitleList;
								WriteAllObject(mg2);
							}
							else {
								String str = uName + "님이 퇴장하였습니다.";
								Msg mg3 = new Msg("Server", "900", str);
								for (int j = 0; j < room.viewers.size(); j++) {
									UserSend(room.viewers.elementAt(j).UserName, mg3);
								}
							}
							break;
						}
					}
					
					// 해당 유저가 관전자일 때
					for (int i = 0; i < room.viewers.size(); i++) {
						if (room.viewers.elementAt(i).UserName.matches(uName)) {
							room.viewers.removeElementAt(i);
							String str = uName + "님이 퇴장하였습니다.";
							Msg mg3 = new Msg("Server", "900", str);
							WriteRoomObject(mg.roomNum, mg3);
							break;
						}
					}
					Msg mg2 = new Msg("Server", "800", "Exit room");
					UserSend(uName, mg2);
					AppendText(uName + "님이 " + rNum +"번 방 퇴장.");
					
				} // 800 끝
				else if (mg.code.matches("900")) { // 채팅
					String str = "[" + mg.userName + "] " + mg.data;
					AppendText(mg.roomNum +"번 방 : " + str);
					
					Msg mg2 = new Msg(mg.userName, "900", str);
					WriteRoomObject(mg.roomNum, mg2);
				}
			} // while문 끝
		} // run() 끝
	} //UserService 끝
	
	public class Room extends Thread {
		public String roomTitle;
		public int roomNum;
		public Vector<UserService> players = new Vector<UserService>();
		public Vector<UserService> viewers = new Vector<UserService>();
		public int[][] board = new int[15][15]; //0으로 초기화
		public boolean gameEnd = false;
		
		public Room(String hostName) {
			for (int i=0; i < UserVec.size(); i++) { // 호스트를 플레이어에 추가
				UserService user = (UserService) UserVec.elementAt(i);
				if (hostName.matches(user.UserName)) {
					players.add(user);
				}
			}
			roomNum = RoomObjList.size();
		}
		
		
	}
}
