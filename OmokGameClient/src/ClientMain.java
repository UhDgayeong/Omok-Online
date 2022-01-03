import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.SystemColor;

public class ClientMain extends JFrame {

	private JPanel contentPane;
	private JTextField txtName;
	private JTextField txtIpAddr;
	private JTextField txtPort;
	
	ImageIcon blackStone = new ImageIcon("src/mainBlack.png");
	ImageIcon whiteStone = new ImageIcon("src/mainWhite.png");

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientMain frame = new ClientMain();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ClientMain() {
		setTitle("¿À¸ñ ¿Â¶óÀÎ");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(160, 130, 90));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		JLabel lblGameTitle = new JLabel("¿À¸ñ");
		lblGameTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblGameTitle.setFont(new Font("³ª´®¸íÁ¶", Font.BOLD, 50));
		lblGameTitle.setBounds(134, 38, 167, 64);
		contentPane.add(lblGameTitle);
		
		JLabel lblName = new JLabel("´Ð³×ÀÓ");
		lblName.setHorizontalAlignment(SwingConstants.CENTER);
		lblName.setFont(new Font("³ª´®¸íÁ¶", Font.BOLD, 15));
		lblName.setBounds(72, 149, 60, 25);
		contentPane.add(lblName);
		
		txtName = new JTextField();
		txtName.setFont(new Font("³ª´®¸íÁ¶", Font.PLAIN, 12));
		txtName.setBackground(new Color(255, 250, 240));
		txtName.setBounds(134, 149, 176, 25);
		contentPane.add(txtName);
		txtName.setColumns(10);
		
		JButton btnEnter = new JButton("ÀÔÀå");
		btnEnter.setBackground(new Color(193, 193, 142));
		btnEnter.setFont(new Font("³ª´®¸íÁ¶", Font.BOLD, 12));
		btnEnter.setBounds(180, 184, 86, 30);
		contentPane.add(btnEnter);
		
		JLabel lblIpAddr = new JLabel("IP ÁÖ¼Ò");
		lblIpAddr.setFont(new Font("³ª´®½ºÄù¾î", Font.PLAIN, 12));
		lblIpAddr.setForeground(SystemColor.info);
		lblIpAddr.setHorizontalAlignment(SwingConstants.RIGHT);
		lblIpAddr.setBounds(278, 214, 50, 15);
		contentPane.add(lblIpAddr);
		
		txtIpAddr = new JTextField();
		txtIpAddr.setFont(new Font("³ª´®½ºÄù¾î", Font.PLAIN, 12));
		txtIpAddr.setForeground(SystemColor.info);
		txtIpAddr.setHorizontalAlignment(SwingConstants.CENTER);
		txtIpAddr.setText("127.0.0.1");
		txtIpAddr.setBackground(new Color(160, 130, 90));
		txtIpAddr.setBounds(340, 211, 84, 21);
		contentPane.add(txtIpAddr);
		txtIpAddr.setColumns(10);
		
		JLabel lblPort = new JLabel("Æ÷Æ® ¹øÈ£");
		lblPort.setFont(new Font("³ª´®½ºÄù¾î", Font.PLAIN, 12));
		lblPort.setForeground(SystemColor.info);
		lblPort.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPort.setBounds(257, 239, 71, 15);
		contentPane.add(lblPort);
		
		txtPort = new JTextField();
		txtPort.setFont(new Font("³ª´®½ºÄù¾î", Font.PLAIN, 12));
		txtPort.setForeground(SystemColor.info);
		txtPort.setHorizontalAlignment(SwingConstants.CENTER);
		txtPort.setText("30000");
		txtPort.setBackground(new Color(160, 130, 90));
		txtPort.setBounds(340, 236, 84, 21);
		contentPane.add(txtPort);
		txtPort.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setBounds(12, 219, 35, 35);
		lblNewLabel.setIcon(blackStone);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("");
		lblNewLabel_1.setBounds(50, 219, 35, 35);
		lblNewLabel_1.setIcon(whiteStone);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Online");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setFont(new Font("Palace Script MT", Font.PLAIN, 50));
		lblNewLabel_2.setBounds(235, 87, 105, 36);
		contentPane.add(lblNewLabel_2);
		
		Myaction action = new Myaction();
		btnEnter.addActionListener(action);
		txtName.addActionListener(action);
	}
	
	class Myaction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String userName = txtName.getText().trim();
			String ip_addr = txtIpAddr.getText().trim();
			String port_no = txtPort.getText().trim();
			ClientLobby lobby = new ClientLobby(userName, ip_addr, port_no);
			setVisible(false);
		}
	}
}
