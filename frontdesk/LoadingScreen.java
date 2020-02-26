package frontdesk;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.border.EtchedBorder;

import sun.misc.JavaLangAccess;
 
/**
 * LoadingScreen is to display the progression to load files.
 * @author elias
 *
 */
public class LoadingScreen extends JWindow{
	JProgressBar progressBar = new JProgressBar();
	Container container = getContentPane();
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	public LoadingScreen() {
		container = getContentPane();

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder());
		panel.setLayout(new BorderLayout());
		container.add(panel, BorderLayout.CENTER);

		JLabel labelTop = new JLabel("  CEB Front Desk Clerk Application  ");
		JLabel labelBottom = new JLabel("               Loading Data.....               ");
		labelTop.setFont(new Font("Verdana", Font.BOLD, 20));
		labelBottom.setFont(new Font("Verdana", Font.BOLD, 20));
		panel.add(labelTop, BorderLayout.NORTH);
		panel.add(labelBottom, BorderLayout.SOUTH);
		
		
		progressBar.setSize(progressBar.getWidth(), progressBar.getHeight()*2);
		progressBar.setStringPainted(true);
		container.add(progressBar, BorderLayout.SOUTH);
		pack();
		setVisible(true);
		int widthSize = screenSize.width/2 - getSize().width/2;
		int heightSize = screenSize.height/2 - getSize().height/2;
		setLocation(widthSize, heightSize);
	}
}

class progressBar extends Thread {
	final private int DELAY = 100;
	private FrontDesk fd;
	JProgressBar pb;
	public progressBar(JProgressBar pb, FrontDesk fd) {
		this.pb = pb;
		this.fd = fd;
	}
	
	
	void startProgram() {
		FrontDesk.loadingScreen.dispose();
		fd.showUp();
	}
	
	public void run() {
		int pMAX;
		try {
			pMAX = ProgressBarHelper.getMaximumNumberForProgessBar();
		int value = ProgressBarHelper.getCurrentNumberForProgessBar();
		pb.setMaximum(pMAX);
		while(pMAX != value) {
			try {
				value = ProgressBarHelper.getCurrentNumberForProgessBar();
				pb.setValue(value);
				Thread.sleep(DELAY);
			} catch (InterruptedException ignoredException) {
			}
		}
		startProgram();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
