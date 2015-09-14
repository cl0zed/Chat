package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Server {
	
	//���� �� ������� ����� �������� ������ � ������
	private int port = 34531;
	private ServerSocket ssocket;
	
	//������ ���� ��������
	private List<Connection> connections = 
			Collections.synchronizedList(new ArrayList<Connection>());
	
	//������ ���� ���������
	private List<String> history = 
			Collections.synchronizedList(new ArrayList<String>());
	
	Server()
	{
		try {
			//������ �������
			ssocket = new ServerSocket(port);
			
			while (true)
			{
				Socket socket = ssocket.accept(); //���������� � ��������
				
				Connection connect = new Connection(socket); //������� ����� ����� ��� ������ �������
				connections.add(connect);
				
				connect.start(); //��������� ����� ��� ����� � �������
			}
		} catch (IOException e) {
			e.printStackTrace();
			//�������� �������
			serverStop();
		}
		
	}
	private void serverStop()
	{
		try{
			ssocket.close(); //��������� ������ ������� 
			
			synchronized(connections) //�������������� ������ 
			{
				//���������� �� ���� �������� ��� �������� �������
				Iterator<Connection> iterator = connections.iterator();
				while(iterator.hasNext()) 
				{
					((Connection) iterator.next()).close();
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	private class Connection extends Thread{
		//��������� �� ������ Thread ����� ������� ����� ����� �� �������
		
		private BufferedReader in;
		private PrintWriter out;
		
		private Socket socket;
		
		private String name = "";
		private String msg = "";
		
		
		public Connection(Socket socket) //�������� ������ �� �������
		{
			this.socket = socket;
			
			try {
				//�������� ������� �����-������
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
				close(); //�������� ���������� � �������� ��� ��������� ������� �����������
			}
		}
		public void run() //������� ������� ������ ������
		{
			
			while (true)
			{
				try
				{
					String n = in.readLine(); //��������� �������
					switch (n)
					{
					case "0": //������� ������ �����
						name = in.readLine();//����� �����
						Iterator<String> hisIter = history.iterator();
						//�������� ������� ��������� ��� �����������
						while (hisIter.hasNext())
						{
							out.println(hisIter.next());
						}
						break;
					case "1": //������� ������ ���������
						int len = in.read(); //����� ���������� ������
						msg = ""; 
						for (int i = 0; i < len; ++i)
						{
							msg += in.readLine() + "\n"; //����� ������ ����� ��������� � ���������� � ����
						}
						synchronized(connections) //�������������� ������
						{
							Iterator<Connection> iterator = connections.iterator();
							//���������� ��������� ���� ��������
							while (iterator.hasNext())
							{
								//�������� ��������� � ���� : "��� ����������� [����� �����������]: ���������"
								Connection toSend = iterator.next();
								//��������� ����� 
								String autor = name + "[" + new SimpleDateFormat("hh:mm:ss aaa").format(new Date(System.currentTimeMillis())) + "]:";
								//���������� ���������
								toSend.out.println(autor + "\r\n" + msg + "\r\n");
								//��������� ������������ ��������� � �������
								history.add(autor + "\r\n" + msg  + "\r\n");
							}
						}
						break;
					case "2": //������� �� �������� ����������
						close();//��������� ���������� � ��������
					}
				}catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		public void close() //��������� ���������� � ��������
		{
			try {
				//��������� ������ �����-������
				out.close();
				in.close();
				
				//���������  ����������
				socket.close();
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//������� �� ������ ������������ ��������
			connections.remove(this);
			//�������� �� ���������� ���� ��������
			if (connections.size() == 0)
			{
				//�������� �������
				history.removeAll(history);
			}
			//������������ �����
			this.stop();
		}
	}
}
