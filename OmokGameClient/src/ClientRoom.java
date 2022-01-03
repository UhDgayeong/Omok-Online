import java.awt.Color;
import java.awt.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

public class ClientRoom extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JPanel contentPane;
	private JTextField txtInput;
	private JButton btnExit;
	private JButton btnSend;
	private JPanel panel;
	public JTextArea textName;
	public JTextArea textArea;
	public JLabel lblShowStone;

	private String userName;
	public int roomNum;
	public String stoneColor;
	public ClientLobby lobby;
	
	public boolean isPlayer = false;
	private Vector<JLabel> stoneVec = new Vector<JLabel>();
	public boolean myTurn = false;
	
	public ImageIcon blackStone = new ImageIcon("src/black.png");
	public Image imgBlack = blackStone.getImage();
	public ImageIcon whiteStone = new ImageIcon("src/white.png");
	public Image imgWhite = whiteStone.getImage();
	public ImageIcon imgDialog = new ImageIcon("src/dialog_img3.png");
	
	public ClientRoom(ClientLobby lobby, String userName, int roomNum) {
		this.lobby = lobby;
		this.userName = userName;
		this.roomNum = roomNum;
		
		setTitle("오목 온라인");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 650, 438);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(210, 180, 140));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		ImageIcon board = new ImageIcon("src/board_3.png");
		Image boardImg = board.getImage();
		
		panel = new JPanel() {
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
				g.drawImage(boardImg, 0, 0, null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};
		panel.setSize(400, 400);
		contentPane.add(panel);
		
		MyMouseEvent mouseEvent = new MyMouseEvent();
		panel.addMouseListener(mouseEvent);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(400, 30, 236, 234);
		contentPane.add(scrollPane);
		
		textArea = new JTextArea();
		textArea.setFont(new Font("나눔명조", Font.PLAIN, 13));
		textArea.setBackground(new Color(255, 255, 240));
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		txtInput = new JTextField();
		txtInput.setFont(new Font("나눔명조", Font.PLAIN, 13));
		txtInput.setBackground(new Color(255, 255, 240));
		txtInput.setBounds(400, 264, 175, 31);
		getContentPane().add(txtInput);
		txtInput.setColumns(10);
		
		btnSend = new JButton("전송");
		btnSend.setFont(new Font("나눔명조", Font.PLAIN, 12));
		btnSend.setForeground(new Color(0, 0, 0));
		btnSend.setBackground(new Color(193, 193, 142));
		btnSend.setBounds(575, 264, 61, 31);
		contentPane.add(btnSend);
		
		JButton btnBack = new JButton("무르기 요청");
		btnBack.setFont(new Font("나눔명조", Font.PLAIN, 12));
		btnBack.setForeground(new Color(0, 0, 0));
		btnBack.setBackground(new Color(193, 193, 142));
		btnBack.setBounds(422, 310, 195, 37);
		contentPane.add(btnBack);
		
		btnBack.addActionListener(new ActionListener() { // 무르기 요청

			@Override
			public void actionPerformed(ActionEvent e) {
				Msg mg = new Msg(userName, "600", "back request");
				mg.roomNum = roomNum;
				lobby.SendObject(mg);
			}
		});
		
		btnExit = new JButton("나가기");
		btnExit.setFont(new Font("나눔명조", Font.BOLD, 15));
		btnExit.setBackground(new Color(153, 153, 102));
		btnExit.setBounds(422, 345, 195, 37);
		contentPane.add(btnExit);
		
		textName = new JTextArea();
		textName.setFont(new Font("나눔명조", Font.PLAIN, 14));
		textName.setText("텍스트");
		textName.setBackground(new Color(210, 180, 140));
		textName.setBounds(412, 8, 175, 18);
		textName.setEditable(false);
		
		contentPane.add(textName);
		
		lblShowStone = new JLabel("");
		lblShowStone.setBounds(610, 5, 20, 20);
		contentPane.add(lblShowStone);
		MyButtonListener btnListener = new MyButtonListener();
		btnExit.addActionListener(btnListener);
		
		TextSendAction textAction = new TextSendAction();
		btnSend.addActionListener(textAction);
		txtInput.addActionListener(textAction);
		
		setVisible(true);
		
		
	} // 생성자 끝
	
	class TextSendAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String str = null;
				str = txtInput.getText();
				Msg mg = new Msg(userName, "900", str);
				mg.roomNum = roomNum;
				lobby.SendObject(mg);
				txtInput.setText("");
				txtInput.requestFocus();
			}
			
		}
	}
	
	class MyButtonListener implements ActionListener { // 방에서 나가기 -> 로비 화면으로

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == btnExit) {
				Logout();
				
			}	
		}
	}
	
	class MyMouseEvent implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (isPlayer && myTurn) {
				Msg mg = new Msg(userName, "500", "Mouse clicked");
				mg.clickedX = e.getX();
				mg.clickedY = e.getY();
				mg.roomNum = roomNum;
				mg.stoneColor = stoneColor;
				mg.userName = userName;
				lobby.SendObject(mg);
			}
		}
		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	
	public void Logout() {
		Msg mg = new Msg(userName, "800", "Leaving room");
		mg.roomNum = roomNum;
		lobby.SendObject(mg);
		setVisible(false);
	}
	
	public void setStone(String color, int startX, int startY) {
		JLabel label = new JLabel("");
		if (color.matches("black"))
			label.setIcon(blackStone);
		else if (color.matches("white"))
			label.setIcon(whiteStone);
		label.setBounds(startX, startY, 20, 20);
		stoneVec.add(label);
		panel.add(label);
		contentPane.repaint();
		System.out.println("The stone is located.");
	}
	
	public void backStone() {
		panel.remove(stoneVec.size()-1);
		stoneVec.removeElementAt(stoneVec.size()-1);
		contentPane.repaint();
	}
}
