package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class startGUI extends JFrame {

	Client client;
	
	JTextField IPField;
	JTextField nameField;
	Box mainBox;
	final JFrame thisFrame = this;
	
	startGUI() {
		super("Welcome");

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		

		// Настраиваем вторую горизонтальную панель (для ввода имени)
		Box nameBox = Box.createHorizontalBox();
		JLabel nameLabel = new JLabel("Name:");
		nameField = new JTextField(15);
		nameBox.add(nameLabel);
		nameBox.add(Box.createHorizontalStrut(6));
		nameBox.add(nameField);
		
		// Настраиваем третью горизонтальную панель (с кнопками)
		Box buttonBox = Box.createHorizontalBox();
		JButton ok = new JButton("Join");
		JButton cancel = new JButton("Cancel");
		
		MyActionListener aListener = new MyActionListener();
		ok.addActionListener(aListener);
		cancel.addActionListener(aListener);
		
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(ok);
		buttonBox.add(Box.createHorizontalStrut(12));
		buttonBox.add(cancel);
		
		//nameLabel.setPreferredSize(IPLabel.getPreferredSize());
		
		
		// Размещаем три горизонтальные панели на одной вертикальной
		mainBox = Box.createVerticalBox();
		mainBox.setBorder(new EmptyBorder(12,12,12,12));
		//mainBox.add(IPBox);
		mainBox.add(Box.createVerticalStrut(12));
		mainBox.add(nameBox);
		mainBox.add(Box.createVerticalStrut(17));
		mainBox.add(buttonBox);
		setContentPane(mainBox);
		pack();
		setResizable(false);
		this.setVisible(true);
		this.setLocationRelativeTo(null);
		
	}
	//Обрабатываем сигналы нажатия кнопок
	class MyActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			switch (ae.getActionCommand())
			{
			case "Join": //нажатие на кнопку Join
				if (nameField.getText().equals(""))
				{
					JOptionPane.showMessageDialog(rootPane, "Empty fields", "Warning", JOptionPane.ERROR_MESSAGE);
				} else
				{
					client = new Client(nameField.getText());
					thisFrame.dispose();
				}
				break;
			case "Cancel": // нажатие на кнопку Cancel
				System.exit(0);
				break;
			}
		}
	}
}
