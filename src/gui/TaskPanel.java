package gui;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import filetransmission.MainThread;
import filetransmission.FileThread;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;


public class TaskPanel extends JPanel implements Runnable{

	private JScrollPane taskListPanel;
	private JTable taskList;
	private JPanel buttonPanel;
	private Object[][] info;
	MainThread mainThread;
	boolean isClosed;

	public TaskPanel(MainThread mainThread) {
		super();
		this.mainThread = mainThread;
		this.isClosed=false;
		this.setLayout(new BorderLayout());

		taskListPanel = new JScrollPane();

		drawTaskListPanel();
		drawBottons();
		this.add(taskListPanel,BorderLayout.CENTER);
		this.add(buttonPanel,BorderLayout.SOUTH);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!isClosed) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			taskListPanel.remove(taskList);
			drawTaskListPanel();		
		}
		System.out.println("task panel closed");
	}

	public void drawTaskListPanel() {

		String[] title = { "fid", ""," Name "," Status ", " Size ", " Download Progress ", " Dwnload Amount ", " Upload Amount ",
				" File Location " };

		HashMap<Integer, FileThread> tasks = mainThread.getFileThreadList();
		info = new Object[tasks.size()][title.length];

		int i = 0;
		Iterator<HashMap.Entry<Integer, FileThread>> entries = tasks.entrySet().iterator();
		while (entries.hasNext()) {
			HashMap.Entry<Integer, FileThread> entry = entries.next();
			usage.TaskInfo taskInfo = new usage.TaskInfo(entry.getValue());
			info[i][0] = taskInfo.getFileID();
			info[i][1] = (i + 1);
			info[i][2] = " " + taskInfo.getTaskName() + " ";
			info[i][3] = " " + taskInfo.getStatus() + " ";
			info[i][4] = " " + taskInfo.getFileSize() + " ";
			info[i][5] = taskInfo.getDownloadPercent();
			info[i][6] = " " + taskInfo.getDownloadAmount() + " ";
			info[i][7] = " " + taskInfo.getUploadAmount() + " ";
			info[i][8] = " " + taskInfo.getFileLocation() + " ";
			i++;
		}

		taskList = new JTable(info, title){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // make the download progress progress bar
        taskList.getColumn(" Download Progress ").setCellRenderer(new DownloadProgressBar());
	
		// not select column, only select one row
		taskList.setColumnSelectionAllowed(false);
        taskList.setSelectionMode(SINGLE_SELECTION);
       
        
		// auto adjust the width of column 
        FitTableColumns(taskList);
		
        
		// hide column fid
        TableColumn tableColumn=taskList.getColumnModel().getColumn(0);
		tableColumn.setMaxWidth(0);
        tableColumn.setMinWidth(0);
        taskList.getTableHeader().getColumnModel().getColumn(0).setMaxWidth(0);
        taskList.getTableHeader().getColumnModel().getColumn(0).setMinWidth(0);
        taskList.getTableHeader().setReorderingAllowed(false);
		
        this.taskListPanel.getViewport().add(taskList);
        taskListPanel.revalidate();
		taskListPanel.repaint();
	}

	private void drawBottons() {
		buttonPanel = new JPanel();
		GridLayout layout = new GridLayout();
		layout.setRows(1);
		layout.setColumns(3);

		buttonPanel.setLayout(layout);

		JButton startButton = new JButton("start");
		startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(taskList.getSelectedColumn()!=-1) {
            		int fileID=Integer.parseInt(info[taskList.getSelectedRow()][0].toString());
            		mainThread.startFile(fileID);
            	}
            }
        });
		
		JButton stopButton = new JButton("stop");
		stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(taskList.getSelectedColumn()!=-1) {
            		int fileID=Integer.parseInt(info[taskList.getSelectedRow()][0].toString());
            		mainThread.stopFile(fileID);
            	}
            }
        });
		
		JButton removeButton = new JButton("remove");
		removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(taskList.getSelectedColumn()!=-1) {
            		int fileID=Integer.parseInt(info[taskList.getSelectedRow()][0].toString());
            		
            		// option
            		String [] options = {"Canel","No","Yes"};
            		int option =  JOptionPane.showOptionDialog(null,"Whether also remove the file on disk?",
            				"Warning",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
            		if(option!=0) {
            			taskListPanel.remove(taskList);
            			mainThread.removeTask(fileID, option==2);
            			drawTaskListPanel();
//            			taskListPanel.add(taskList,BorderLayout.CENTER);    			
//            			taskListPanel.revalidate();
//            			taskListPanel.repaint();
            		}        			
            	}
            }
        });

		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(removeButton);
	}

	public void FitTableColumns(JTable myTable) { // 設置table的列寬隨內容調整

		JTableHeader header = myTable.getTableHeader();
		int rowCount = myTable.getRowCount();
		Enumeration columns = myTable.getColumnModel().getColumns();

		while (columns.hasMoreElements()) {
			TableColumn column = (TableColumn) columns.nextElement();
			int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
			int width = (int) myTable.getTableHeader().getDefaultRenderer()
					.getTableCellRendererComponent(myTable, column.getIdentifier(), false, false, -1, col)
					.getPreferredSize().getWidth();
			for (int row = 0; row < rowCount; row++) {
				int preferedWidth = (int) myTable.getCellRenderer(row, col)
						.getTableCellRendererComponent(myTable, myTable.getValueAt(row, col), false, false, row, col)
						.getPreferredSize().getWidth();
				width = Math.max(width, preferedWidth);
			}

			header.setResizingColumn(column);
			column.setWidth(width + myTable.getIntercellSpacing().width);
		}
	}

	public void setIsClosed(boolean isClosed) {
		this.isClosed=isClosed;
	}
}
