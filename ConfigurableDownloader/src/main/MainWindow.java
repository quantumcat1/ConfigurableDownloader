package main;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

public class MainWindow extends JPanel implements ActionListener
{
    private static final long serialVersionUID = -371374960795143493L;
    private JFileChooser chooser;
    private JLabel directoryLabel;
    private JButton go;
    private File selectedDirectory;
    private List<FileVO> downloadList;

    public void initialise()
    {
    	new Thread()
    	{
    		@Override
    		public void run()
    		{
    			try {
					downloadList = readDownloads();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}.start();
    	selectedDirectory = null;
    	JLabel label1 = new JLabel("Navigate to download location");
    	add(label1);
    	directoryLabel = new JLabel("");
    	add(directoryLabel);
    	go = new JButton("Browse");
    	go.addActionListener(this);
    	add(go);
    }

    public MainWindow()
    {
        initialise();
    }

    private static void createWindow()
    {
    	setUIFont (new javax.swing.plaf.FontUIResource(new Font("Calibri",Font.PLAIN, 30)));
        JFrame frame = new JFrame("Download NTRPack");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(500, 250));

        MainWindow newContentPane = new MainWindow();
        newContentPane.setLayout(new BoxLayout(newContentPane, BoxLayout.PAGE_AXIS));
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args)
    {
        createWindow();
    }

    private List<FileVO> readDownloads() throws IOException
    {
    	BufferedReader reader = new BufferedReader(new FileReader("config.txt"));
    	String line = null;
    	FileVO file = null;
    	List<FileVO> list = new ArrayList<FileVO>();
    	while((line = reader.readLine()) != null)
    	{
    		String[] array1 = line.split(" ");
    		if(array1[0].equals("#"))
    		{
    			if(file != null) list.add(file);
    			file = new FileVO(array1[1], array1[2]);
    		}
    		else if(array1[0].equals("~"))
    		{
    			file.listDelete.add(array1[1]);
    		}
    		else if(array1[0].equals("*"))
    		{
    			file.mapRename.put(array1[1], array1[2]);
    		}
    	}
    	if(file != null) list.add(file);
    	reader.close();
    	return list;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (selectedDirectory == null)
        {
        	chooser = new JFileChooser();
        	chooser.setCurrentDirectory(new java.io.File("."));
        	chooser.setDialogTitle("Navigate to download location");
        	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        	chooser.setAcceptAllFileFilterUsed(false);
        	if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        	{
        		directoryLabel.setText("Selected directory: " + chooser.getSelectedFile().getAbsolutePath());
        		selectedDirectory = chooser.getSelectedFile();
        	}
        	go.setText("Download files");
        }
        else
        {
        	JFrame frame = new JFrame("Download progress");
            //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setPreferredSize(new Dimension(500, 600));

            StatusWindow status = new StatusWindow();
            status.setLayout(new BoxLayout(status, BoxLayout.PAGE_AXIS));
            status.setOpaque(true);
            frame.setContentPane(status);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            for(FileVO file : downloadList)
            {
            	if(!file.path.startsWith("/")) file.path = "/" + file.path;
            	file.path = selectedDirectory.getAbsolutePath() + file.path;
            	file.downloadTask = new DownloadTask(file, status);
            	file.downloadTask.addPropertyChangeListener(
        			new PropertyChangeListener()
        			{
        				public void propertyChange(PropertyChangeEvent evt)
        				{
        					if ("state".equals(evt.getPropertyName()))
        					{
        						if (evt.getNewValue() == SwingWorker.StateValue.DONE)
        						{
        							try
        							{
        								file.downloadTask.get();
        								if(!file.path.startsWith("/")) file.path = "/" + file.path;
        								for(String deleteItem : file.listDelete)
        								{
        									if(!deleteItem.startsWith("/")) deleteItem = "/" + deleteItem;
        									if(deleteItem.endsWith("/")) deleteItem = deleteItem.substring(0, deleteItem.length() -2);
        									File f = new File(selectedDirectory.getAbsolutePath() + deleteItem);
        									System.out.println("Deleting " + f.getName());
        								}
        								Iterator it = file.mapRename.entrySet().iterator();
        							    while (it.hasNext())
        							    {
        							        Map.Entry<String, String> pair = (Map.Entry<String, String>)it.next();
        							        String before = pair.getKey();
        							        String after = pair.getValue();
        							        if(!before.startsWith("/")) before = "/" + before;
        									if(before.endsWith("/")) before = before.substring(0, before.length() -2);
        									if(!after.startsWith("/")) after = "/" + after;
        									if(after.endsWith("/")) after = after.substring(0, after.length() -2);
        							        File f = new File(selectedDirectory.getAbsolutePath() + file.path + before);
        							        File f2 = new File(selectedDirectory.getAbsolutePath() + file.path + after);
        				                    System.out.println("Renaming " + f.getName() + " to " + f2.getName());
        				                    f.renameTo(f2);
        							    }
        							}
        							catch(Exception e) {e.printStackTrace();}
        						}
        					}
        				}
        			}
        		);
            }

            for(FileVO file : downloadList)
            {
            	file.downloadTask.execute();
            }

        }
    }
    private static void setUIFont(javax.swing.plaf.FontUIResource f)
    {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements())
        {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
            {
                UIManager.put(key, f);
            }
        }
    }
}
