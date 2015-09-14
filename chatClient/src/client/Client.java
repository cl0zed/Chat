package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class Client {

	//���� �� ������� �������� � IP �������(����� ������������ � ������� ������ �� ����� ����������)
	private int port = 34531;
	private String IP = "127.0.0.1";
	
	private BufferedReader in;
	private PrintWriter out;
	
	private Recipient recipient;
	
	private Socket socket;
	
	private GUI userInterface;
	
	//������������ � ������� � ������, ������� ������
	public Client(String name) {
		try 
		{
			//���������� � ��������
			socket = new Socket(this.IP, port);
			//�������� ������� �����-������		
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			
			out.println("0"); //�������������� �� �������� �����
			
			out.println(name); //���������� ��� ��� ������� ����� ���������� � ����
			
			//�������� ������������ ����������
			userInterface = new GUI();
			
			//�����, ������� �������� �� ��������� ���������
			recipient = new Recipient();
			recipient.start(); //������ ������
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//������� �������� ���������
	public void sendMessage(String message)
	{
		String[] msgs = message.split("\n"); //��������� ��������� �� ��������� ������ (��� ��� ������� �������� ���������)
		out.println("1"); //�������������� �� �������� ���������
		out.write(msgs.length); //�������� ���������� ������
		for (int i = 0; i < msgs.length; ++i)
		{
			out.println(msgs[i]); //�������� ���������
		}
	}
	//������ �������� ����������
	public void close()
	{
		
		try {
			//�������������� ������� � �������� ����������
			out.println("2");
			
			//�������� ������� �����-������
			out.close();
			in.close();
			
			//�������� ����������
			socket.close();
			recipient.stopRecipient();
			recipient.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//�������� ������ ������, ����������� � ��������� ������, ����������� �� ����� � ����� ���������
	private class Recipient extends Thread
	{
		private boolean stopped = false;
		
		public void stopRecipient()
		{
			stopped = true;
		}
		
		public void run()
		{
			try
			{
				while (!stopped)
				{
					String msg = in.readLine(); //��������� ���������
					msg += "\r\n"; //����� ��������� ��������� � ����� ������
					userInterface.messages.append(msg);//����������� � GUI 				
					
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	//�������� ����� GUI 
	private class GUI extends JFrame
	{
		JTextArea messages;
		JTextArea myMessage;
		JButton send;
		GUI()
		{
			super("Chat");//������ ��������� ����
			//��������� ��������� �������� ���� (��������� ������� ���������� �� ��������� ��������, �����������,
			// ������������� ��������� ������������)
			this.addWindowListener(new WindowListener(){
				public void windowClosed(WindowEvent arg0) {
				}

				public void windowActivated(WindowEvent arg0) {
				}

				@Override
				public void windowClosing(WindowEvent arg0) {	
					close();
					System.exit(0);
				}

				@Override
				public void windowDeactivated(WindowEvent arg0) {
				}

				@Override
				public void windowDeiconified(WindowEvent arg0) {
				}

				@Override
				public void windowIconified(WindowEvent arg0) {
				}

				@Override
				public void windowOpened(WindowEvent arg0) {
				}
			});
			//������� ����, ��� ������������ ���������� ���������
			
			//���������� ������ ��� ���������
			messages = new JTextArea(20, 30);
			messages.setEditable(false);
			//�������������� ��������
			messages.setLineWrap(true);
			messages.setWrapStyleWord(true);
			
			//������ ������������ ���������
			JScrollPane msgsPane = new JScrollPane(messages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
															JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			msgsPane.setBorder(BorderFactory.createLineBorder(Color.black)); //������� �������
			
			Box msgs = Box.createHorizontalBox();
			msgs.add(Box.createHorizontalGlue());
			msgs.add(Box.createHorizontalStrut(20));
			msgs.add(msgsPane);
			msgs.add(Box.createHorizontalStrut(20));
			msgs.add(Box.createHorizontalGlue());
			
			//���� ��� ����� ��������� � ������ ��������
			
			//������ ��� ����� ���������
			myMessage = new JTextArea(3, 20);
			myMessage.setLineWrap(true);
			myMessage.setWrapStyleWord(true);
			
			
			JScrollPane myMsgPane = new JScrollPane(myMessage, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			
			//������ � ��������� �� �������
			send = new JButton("Send");
			send.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae)
				{
					sendMessage(myMessage.getText());
					myMessage.setText("");
				}
			});
			
			
			//����������� ���� ��� ����� � ������
			Box myMsg = Box.createHorizontalBox();
			myMsg.add(Box.createHorizontalGlue());
			myMsg.add(myMsgPane);
			myMsg.add(Box.createHorizontalStrut(25));
			myMsg.add(send);
			myMsg.add(Box.createHorizontalGlue());
			
			
			//��������� ��� �������� � ��������� �� �����
			Box result = Box.createVerticalBox();
			result.add(Box.createVerticalGlue());
			result.add(Box.createVerticalStrut(20));
			result.add(msgs);
			result.add(Box.createVerticalStrut(5));
			result.add(myMsg);
			result.add(Box.createVerticalStrut(20));
			result.add(Box.createVerticalGlue());
			
			//���������� ���� ��������� � ����
			this.add(result);
			pack();
			this.setLocationRelativeTo(null);
			this.setVisible(true);
			this.setResizable(false);
		}
	}

}
