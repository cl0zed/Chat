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
	
	//порт на котором будут работать клиент и сервер
	private int port = 34531;
	private ServerSocket ssocket;
	
	//список всех клиентов
	private List<Connection> connections = 
			Collections.synchronizedList(new ArrayList<Connection>());
	
	//список всех сообщений
	private List<String> history = 
			Collections.synchronizedList(new ArrayList<String>());
	
	Server()
	{
		try {
			//запуск сервера
			ssocket = new ServerSocket(port);
			
			while (true)
			{
				Socket socket = ssocket.accept(); //соединение с клиентом
				
				Connection connect = new Connection(socket); //создаем новый поток для нового клиента
				connections.add(connect);
				
				connect.start(); //запускаем поток для связи с клиетом
			}
		} catch (IOException e) {
			e.printStackTrace();
			//закрытие сервера
			serverStop();
		}
		
	}
	private void serverStop()
	{
		try{
			ssocket.close(); //закрываем соккет сервера 
			
			synchronized(connections) //синхронизируем потоки 
			{
				//отключение от всех клиентов при закрытии сервера
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
		//наследуем от класса Thread чтобы создать новый поток на сервере
		
		private BufferedReader in;
		private PrintWriter out;
		
		private Socket socket;
		
		private String name = "";
		private String msg = "";
		
		
		public Connection(Socket socket) //создание потока на сервере
		{
			this.socket = socket;
			
			try {
				//открытие потоков ввода-вывода
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
				close(); //закрытие соединения с клиентом при неудачной попытке соединиться
			}
		}
		public void run() //функция запуска нового потока
		{
			
			while (true)
			{
				try
				{
					String n = in.readLine(); //получение команды
					switch (n)
					{
					case "0": //команда приема имени
						name = in.readLine();//прием имени
						Iterator<String> hisIter = history.iterator();
						//отправка истории сообщений при подключении
						while (hisIter.hasNext())
						{
							out.println(hisIter.next());
						}
						break;
					case "1": //команда приема сообщения
						int len = in.read(); //прием количества частей
						msg = ""; 
						for (int i = 0; i < len; ++i)
						{
							msg += in.readLine() + "\n"; //прием каждой части сообщения и склеивание в одно
						}
						synchronized(connections) //синхронизируем потоки
						{
							Iterator<Connection> iterator = connections.iterator();
							//отправляем сообщение всем контктам
							while (iterator.hasNext())
							{
								//отправка сообщения в виде : "Имя отправителя [время отправления]: сообщение"
								Connection toSend = iterator.next();
								//добавляем время 
								String autor = name + "[" + new SimpleDateFormat("hh:mm:ss aaa").format(new Date(System.currentTimeMillis())) + "]:";
								//отправляем сообщение
								toSend.out.println(autor + "\r\n" + msg + "\r\n");
								//сохраняем отправленное сообщение в историю
								history.add(autor + "\r\n" + msg  + "\r\n");
							}
						}
						break;
					case "2": //команда на закрытие соединения
						close();//закрываем соединение с клиентом
					}
				}catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		public void close() //закрываем соединение с клиентом
		{
			try {
				//Закрываем потоки ввода-вывода
				out.close();
				in.close();
				
				//Закрываем  соединение
				socket.close();
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//удаляем из списка подключенных клиентов
			connections.remove(this);
			//проверка на отключение всех клиентов
			if (connections.size() == 0)
			{
				//удаление истории
				history.removeAll(history);
			}
			//останавливем поток
			this.stop();
		}
	}
}
