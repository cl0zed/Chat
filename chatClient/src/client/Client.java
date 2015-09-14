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

	//порт на котором работает и IP сервера(будет подключаться к серверу только на вашем компьютере)
	private int port = 34531;
	private String IP = "127.0.0.1";
	
	private BufferedReader in;
	private PrintWriter out;
	
	private Recipient recipient;
	
	private Socket socket;
	
	private GUI userInterface;
	
	//подключаемся к серверу с именем, которое задали
	public Client(String name) {
		try 
		{
			//соединение с сервером
			socket = new Socket(this.IP, port);
			//открытие потоков ввода-вывода		
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			
			out.println("0"); //предупреждение об отправке имени
			
			out.println(name); //отправляем имя под которым будем находиться в чате
			
			//создание графического интерфейса
			userInterface = new GUI();
			
			//поток, который отвечает за получение сообщений
			recipient = new Recipient();
			recipient.start(); //запуск потока
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//функция отправки сообщений
	public void sendMessage(String message)
	{
		String[] msgs = message.split("\n"); //разбиваем сообщение по переносам строки (так как функция передает построчно)
		out.println("1"); //предупреждение об отправке сообщения
		out.write(msgs.length); //отправка количество частей
		for (int i = 0; i < msgs.length; ++i)
		{
			out.println(msgs[i]); //отправка сообщения
		}
	}
	//фукция закрытия приложения
	public void close()
	{
		
		try {
			//предупреждение сервера о закрытии соединения
			out.println("2");
			
			//закрытие потоков ввода-вывода
			out.close();
			in.close();
			
			//закрытие соединения
			socket.close();
			recipient.stopRecipient();
			recipient.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//создание нового класса, работающего в отдельном потоке, отвещающего за прием и вывод сообщений
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
					String msg = in.readLine(); //получение сообщений
					msg += "\r\n"; //вывод следующих сообщений с новой строки
					userInterface.messages.append(msg);//отображение в GUI 				
					
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	//создадим здесь GUI 
	private class GUI extends JFrame
	{
		JTextArea messages;
		JTextArea myMessage;
		JButton send;
		GUI()
		{
			super("Chat");//задаем заголовок окна
			//добавляем обработку закрытия окна (остальные функции отвечающие за обработку открытия, свертывания,
			// развертывания оставляем стандартными)
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
			//создаем поле, где отображаются полученные сообщения
			
			//Неактивное окошко для сообщений
			messages = new JTextArea(20, 30);
			messages.setEditable(false);
			//Автоматические переносы
			messages.setLineWrap(true);
			messages.setWrapStyleWord(true);
			
			//Панель вертикальной прокрутки
			JScrollPane msgsPane = new JScrollPane(messages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
															JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			msgsPane.setBorder(BorderFactory.createLineBorder(Color.black)); //создаем границы
			
			Box msgs = Box.createHorizontalBox();
			msgs.add(Box.createHorizontalGlue());
			msgs.add(Box.createHorizontalStrut(20));
			msgs.add(msgsPane);
			msgs.add(Box.createHorizontalStrut(20));
			msgs.add(Box.createHorizontalGlue());
			
			//поля для ввода сообщения и кнопка отправки
			
			//окошко для ввода сообщения
			myMessage = new JTextArea(3, 20);
			myMessage.setLineWrap(true);
			myMessage.setWrapStyleWord(true);
			
			
			JScrollPane myMsgPane = new JScrollPane(myMessage, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			
			//кнопка и обработка ее нажатия
			send = new JButton("Send");
			send.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae)
				{
					sendMessage(myMessage.getText());
					myMessage.setText("");
				}
			});
			
			
			//группировка окна для ввода и кнопки
			Box myMsg = Box.createHorizontalBox();
			myMsg.add(Box.createHorizontalGlue());
			myMsg.add(myMsgPane);
			myMsg.add(Box.createHorizontalStrut(25));
			myMsg.add(send);
			myMsg.add(Box.createHorizontalGlue());
			
			
			//компануем все элементы и выводимих на экран
			Box result = Box.createVerticalBox();
			result.add(Box.createVerticalGlue());
			result.add(Box.createVerticalStrut(20));
			result.add(msgs);
			result.add(Box.createVerticalStrut(5));
			result.add(myMsg);
			result.add(Box.createVerticalStrut(20));
			result.add(Box.createVerticalGlue());
			
			//добавление всех элементов к окну
			this.add(result);
			pack();
			this.setLocationRelativeTo(null);
			this.setVisible(true);
			this.setResizable(false);
		}
	}

}
