import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.awt.Color;

public class ClientLobby extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private Vector<String> roomTitleList = new Vector<String>();
	private JList<String> roomList = new JList<String>();
	private int selectedRoomIdx;
	
	private Socket socket;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	public ClientLobby lobby;
	public ClientRoom room;
	private String userName;

	public ClientLobby(String userName, String ip_addr, String port_no) {
		this.userName = userName;
		setTitle("���� �¶���");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 373, 391);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(160, 130, 90));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		JButton btnCreate = new JButton("�� �����");
		btnCreate.setBackground(new Color(193, 193, 142));
		btnCreate.setFont(new Font("��������", Font.PLAIN, 15));
		btnCreate.setBounds(22, 10, 99, 34);
		contentPane.add(btnCreate);
		
		roomList.setBackground(new Color(255, 255, 240));
		roomList.setFont(new Font("��������", Font.PLAIN, 17));
		roomList.setVisibleRowCount(10);
		roomList.setFixedCellWidth(100);
		
		JScrollPane scrollPane = new JScrollPane(roomList);
		scrollPane.setBounds(22, 55, 312, 244);
		contentPane.add(scrollPane);
		
		JButton btnEnterRoom = new JButton("�����ϱ�");
		btnEnterRoom.setBackground(new Color(193, 193, 142));
		btnEnterRoom.setFont(new Font("��������", Font.PLAIN, 15));
		btnEnterRoom.setBounds(22, 298, 158, 34);
		contentPane.add(btnEnterRoom);
		
		JButton btnWatch = new JButton("�����ϱ�");
		btnWatch.setBackground(new Color(193, 193, 142));
		btnWatch.setFont(new Font("��������", Font.PLAIN, 15));
		btnWatch.setBounds(176, 298, 158, 34);
		contentPane.add(btnWatch);
		
		JButton btnExit = new JButton("����");
		btnExit.setForeground(new Color(255, 255, 255));
		btnExit.setBackground(new Color(130, 80, 40));
		btnExit.setFont(new Font("��������", Font.PLAIN, 12));
		btnExit.setBounds(272, 10, 62, 34);
		contentPane.add(btnExit);
		
		lobby = this;
		
		setVisible(true);
		
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			
			Msg mg = new Msg(userName, "100", "Hello");
			SendObject(mg);
			
			ListenNetwork net = new ListenNetwork();
			net.start();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		
		// �׼Ǹ�����
		btnExit.addActionListener(new ActionListener() { // �α׾ƿ�

			@Override
			public void actionPerformed(ActionEvent e) {
				Msg mg = new Msg(userName, "400", "Bye");
				SendObject(mg);
				System.exit(0);
			}
			
		});
		btnCreate.addActionListener(new ActionListener() { // ���ο� �� �����ϸ� ����

			@Override
			public void actionPerformed(ActionEvent e) {
				int rNum = roomTitleList.size();
				System.out.println("rNum : " + rNum);
				String roomTitle = JOptionPane.showInputDialog("�� ������ �Է��ϼ���");	
				if (roomTitle != null) { // cancel ������ �� ���� ���
					Msg mg = new Msg(userName, "200", roomTitle); // �� ���� �޼��� ����
					SendObject(mg);
					
					room = new ClientRoom(lobby, userName, rNum);
					room.isPlayer = true;
					room.stoneColor = "black";
					room.textName.setText(userName);
					room.lblShowStone.setIcon(room.blackStone);
					
					AppendText(userName + "���� �����Ͽ����ϴ�.");
					AppendText("������ ��ٸ��� ��...");
					setVisible(false);
				}
			}
		});
		roomList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()) {
					selectedRoomIdx = roomList.getSelectedIndex();
					System.out.println("selected : " + selectedRoomIdx);
				}
			}
		});
		btnEnterRoom.addActionListener(new ActionListener() { // (�̹� ������ �ִ�) �� ���� ��û

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedRoomIdx >= 0) {
					room = new ClientRoom(lobby, userName, selectedRoomIdx);
					room.isPlayer = true;
					room.stoneColor = "white";
					room.textName.setText(userName);
					room.lblShowStone.setIcon(room.whiteStone);
					
					Msg mg = new Msg(userName, "300", "Enter player request");
					mg.roomNum = selectedRoomIdx;
					SendObject(mg);
					
					setVisible(false);
				}
			}
		});
		btnWatch.addActionListener(new ActionListener() { // �����ڷ� ���� ��û

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedRoomIdx >= 0) {
					room = new ClientRoom(lobby, userName, selectedRoomIdx);
					room.textName.setText(userName);
					
					Msg mg = new Msg(userName, "310", "Enter viewer request");
					mg.roomNum = selectedRoomIdx;
					SendObject(mg);
					
					setVisible(false);
				}
			}
			
		});
	} // ������ ��
	
	
	
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {
				Object obj = null;
				Msg mg;
				try {
					obj = ois.readObject();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
					break;
				}
				if (obj == null)
					break;
				if (obj instanceof Msg) {
					mg = (Msg) obj;
				} else
					continue;
				
				switch (mg.code) {
				case "200": // �κ� ȭ�� refresh
					roomTitleList = mg.roomTitleList;
					roomList.setListData(roomTitleList);
					repaint();
					break;
				case "500":
					int startX = mg.clickedX - 10;
					int startY = mg.clickedY - 10;
					room.setStone(mg.data, startX, startY);
					break;
				case "510": // ���
					JOptionPane.showMessageDialog(room.contentPane, "6�� �̻��� �� �� �����ϴ�.", "��� ����", JOptionPane.PLAIN_MESSAGE);
					break;
				case "530": // ���
					JOptionPane.showMessageDialog(room.contentPane, "33���� �� �� �����ϴ�.", "33 ����", JOptionPane.PLAIN_MESSAGE);
					break;
				case "540": // ���
					JOptionPane.showMessageDialog(room.contentPane, "44�� �� �� �����ϴ�.", "44 ����", JOptionPane.PLAIN_MESSAGE);
					break;
				case "550": // �� ����
					room.myTurn = true;
					break;
				case "560" : // ��� ����
					room.myTurn = false;
					break;
				case "600": // ������ ��û
					if (!(userName.matches(mg.userName))) {
						int result = JOptionPane.showConfirmDialog(room.contentPane, "������ �����⸦ ��û�Ͽ����ϴ�.\n����Ͻðڽ��ϱ�?", "������ ��û", JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.YES_OPTION) {
							Msg mg2 = new Msg(userName, "610", "request accepted");
							mg2.roomNum = room.roomNum;
							SendObject(mg2);
						}
					}
					break;
				case "610":
					room.backStone();
					break;
				case "700": // �¸� or �й�. ���� ����
					if (room.isPlayer) {
						if (mg.data.matches(userName)) {
							JOptionPane.showMessageDialog(room.contentPane, "�̰���ϴ�!", "�¸�!", JOptionPane.INFORMATION_MESSAGE, room.imgDialog);
						}
						else {
							JOptionPane.showMessageDialog(room.contentPane, "�й��Ͽ����ϴ�.", "�й�", JOptionPane.INFORMATION_MESSAGE, room.imgDialog);
						}
					}
					else { // �����ڿ��Ե� ���� ���� ����
						JOptionPane.showMessageDialog(room.contentPane, mg.data +"���� �¸��Ͽ����ϴ�.", "���� ����", JOptionPane.INFORMATION_MESSAGE, room.imgDialog);
					}
					
					Msg mg2 = new Msg(userName, "800", "Leaving room");
					mg2.roomNum = room.roomNum;
					SendObject(mg2);
					room.Logout();
					setVisible(true);
					break;
				case "800": // �濡�� ������ �ٽ� �κ��
					setVisible(true);
					break;
				case "810": // ��� ���
					if (room.isPlayer)
						JOptionPane.showMessageDialog(room.contentPane, "������ ����Ͽ����ϴ�.", "�¸�!", JOptionPane.INFORMATION_MESSAGE, room.imgDialog);
					else // �����ڿ��Ե� ��� ����
						JOptionPane.showMessageDialog(room.contentPane, mg.userName + "���� ����Ͽ����ϴ�.", "���� ����", JOptionPane.INFORMATION_MESSAGE, room.imgDialog);
					room.Logout();
					setVisible(true);
					break;
				case "900": // ä��
					AppendText(mg.data);
				}
			}
		}
	}
	
	public void AppendText(String msg) {
		msg = msg.trim();
		room.textArea.append( msg + "\n");
		room.textArea.setCaretPosition(room.textArea.getText().length());
	}
	
	public void SendObject(Object ob) {
		try {
			oos.writeObject(ob);
			oos.reset();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("�޼��� �۽� ����!!\n");
		}
	}
}
