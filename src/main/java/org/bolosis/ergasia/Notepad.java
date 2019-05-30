package org.bolosis.ergasia;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Date;
import java.util.Scanner;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.commons.lang3.StringUtils;


public class Notepad implements ActionListener, MenuConstants {

	JFrame f;
	JTextArea ta;
	JLabel statusBar;

	private String fileName = "Untitled";
	
	String applicationName = "Tasos";

	String searchString, replaceString;
	int lastSearchIndex;

	FileOperation fileHandler;
	FontChooser fontDialog = null;
	FindDialog findReplaceDialog = null;
	JColorChooser bcolorChooser = null;
	JColorChooser fcolorChooser = null;
	JDialog backgroundDialog = null;
	JDialog foregroundDialog = null;
	JMenuItem cutItem, copyItem, clearItem, findItem, findNextItem, replaceItem, gotoItem, selectAllItem;

	
	Notepad() {
		f = new JFrame(fileName + " - " + applicationName);
		ta = new JTextArea(30, 60);
		statusBar = new JLabel("||       Ln 1, Col 1  ", JLabel.RIGHT);
		f.add(new JScrollPane(ta), BorderLayout.CENTER);
		f.add(statusBar, BorderLayout.SOUTH);
		f.add(new JLabel("  "), BorderLayout.EAST);
		f.add(new JLabel("  "), BorderLayout.WEST);
		createMenuBar(f);
		
		f.pack();
		f.setLocation(100, 50);
		f.setVisible(true);
		f.setLocation(150, 50);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		fileHandler = new FileOperation(this);


		ta.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				int lineNumber = 0, column = 0, pos = 0;

				try {
					pos = ta.getCaretPosition();
					lineNumber = ta.getLineOfOffset(pos);
					column = pos - ta.getLineStartOffset(lineNumber);
				} catch (Exception excp) {
				}
				if (ta.getText().length() == 0) {
					lineNumber = 0;
					column = 0;
				}
				statusBar.setText("||       Ln " + (lineNumber + 1) + ", Col " + (column + 1));
			}
		});
		
		DocumentListener myListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				fileHandler.saved = false;
			}

			public void removeUpdate(DocumentEvent e) {
				fileHandler.saved = false;
			}

			public void insertUpdate(DocumentEvent e) {
				fileHandler.saved = false;
			}
		};
		ta.getDocument().addDocumentListener(myListener);
		
		WindowListener frameClose = new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if (fileHandler.confirmSave())
					System.exit(0);
			}
		};
		f.addWindowListener(frameClose);
		
	}

	
	void goTo() {
		int lineNumber = 0;
		try {
			lineNumber = ta.getLineOfOffset(ta.getCaretPosition()) + 1;
			String tempStr = JOptionPane.showInputDialog(f, "Enter Line Number:", "" + lineNumber);
			if (tempStr == null) {
				return;
			}
			lineNumber = Integer.parseInt(tempStr);
			ta.setCaretPosition(ta.getLineStartOffset(lineNumber - 1));
		} catch (Exception e) {
		}
	}

	
	public void actionPerformed(ActionEvent ev) {
		String cmdText = ev.getActionCommand();
		
		if (cmdText.equals(fileNew))
			fileHandler.newFile();
		else if (cmdText.equals(fileOpen))
			fileHandler.openFile();
		
		else if (cmdText.equals(fileSave))
			fileHandler.saveThisFile();
		
		else if (cmdText.equals(fileSaveAs))
			fileHandler.saveAsFile();
		
		else if (cmdText.equals(fileExit)) {
			if (fileHandler.confirmSave())
				System.exit(0);
		}
		
		else if (cmdText.equals(editCut))
			ta.cut();
		
		else if (cmdText.equals(editCopy))
			ta.copy();
		
		else if (cmdText.equals(editPaste))
			ta.paste();
		
		else if (cmdText.equals(editClear))
			ta.replaceSelection("");
		
		else if (cmdText.equals(editFind)) {
			if (Notepad.this.ta.getText().length() == 0)
				return; // text box have no text
			if (findReplaceDialog == null)
				findReplaceDialog = new FindDialog(Notepad.this.ta);
			findReplaceDialog.showDialog(Notepad.this.f, true);
		}
		
		else if (cmdText.equals(editFindNext)) {
			if (Notepad.this.ta.getText().length() == 0)
				return; // text box have no text

			if (findReplaceDialog == null)
				statusBar.setText("Use Find option of Edit Menu first !!!!");
			else
				findReplaceDialog.findNextWithSelection();
		}
		
		else if (cmdText.equals(editReplace)) {
			if (Notepad.this.ta.getText().length() == 0)
				return; // text box have no text

			if (findReplaceDialog == null)
				findReplaceDialog = new FindDialog(Notepad.this.ta);
			findReplaceDialog.showDialog(Notepad.this.f, false);// replace
		}
		
		else if (cmdText.equals(editGoTo)) {
			if (Notepad.this.ta.getText().length() == 0)
				return; // text box have no text
			goTo();
		}
		
		else if (cmdText.equals(editSelectAll))
			ta.selectAll();
		
		else if (cmdText.equals(editTimeDate))
			ta.insert(new Date().toString(), ta.getSelectionStart());
		
		
		else if (cmdText.equals(formatFont)) {
			if (fontDialog == null)
				fontDialog = new FontChooser(ta.getFont());

			if (fontDialog.showDialog(Notepad.this.f, "Choose a font"))
				Notepad.this.ta.setFont(fontDialog.createFont());
		}
		
		else if (cmdText.equals(formatForeground))
			showForegroundColorDialog();
		
		else if (cmdText.equals(formatBackground))
			showBackgroundColorDialog();
		

		else if (cmdText.equals(viewStatusBar)) {
			JCheckBoxMenuItem temp = (JCheckBoxMenuItem) ev.getSource();
			statusBar.setVisible(temp.isSelected());
		}
		
		else if (cmdText.equals(aboutStatistics)) {
			String fileName = f.getTitle();
			String title = " - Text Editor - Tasos Bolosis";
			fileName = fileName.replace(title, " ").replaceAll("\\s+", " ");
			
			try {
				
				File file =new File(fileName);
				
				Scanner sc = new Scanner(file);
				Scanner cs = new Scanner(file);
				
				long lines = 0l;
				
				long charactersWithGaps = 0l;
				
				long charactersWithoutGaps = 0l;
				
				while(sc.hasNextLine()) {
					lines = lines + 1;
					
					charactersWithGaps = charactersWithGaps + sc.nextLine().length();
					
					
				}
				
				while(cs.hasNextLine()) {
					charactersWithoutGaps = charactersWithoutGaps + StringUtils.deleteWhitespace(cs.nextLine()).length();
				}
				
				double bytes = (double) file.length()/1024;
				
				long words = (long) FileOperation.getNumberOfWords(fileName);
				
				JOptionPane.showMessageDialog(Notepad.this.f, FileOperation.invastigationText(words, charactersWithGaps, 
						charactersWithoutGaps, lines, bytes), "Results...", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else
			statusBar.setText("This " + cmdText + " command is yet to be implemented");
	}// action Performed
		

	void showBackgroundColorDialog() {
		if (bcolorChooser == null)
			bcolorChooser = new JColorChooser();
		if (backgroundDialog == null)
			backgroundDialog = JColorChooser.createDialog(Notepad.this.f, formatBackground, false, bcolorChooser,
					new ActionListener() {
						public void actionPerformed(ActionEvent evvv) {
							Notepad.this.ta.setBackground(bcolorChooser.getColor());
						}
					}, null);

		backgroundDialog.setVisible(true);
	}

	
	void showForegroundColorDialog() {
		if (fcolorChooser == null)
			fcolorChooser = new JColorChooser();
		if (foregroundDialog == null)
			foregroundDialog = JColorChooser.createDialog(Notepad.this.f, formatForeground, false, fcolorChooser,
					new ActionListener() {
						public void actionPerformed(ActionEvent evvv) {
							Notepad.this.ta.setForeground(fcolorChooser.getColor());
						}
					}, null);

		foregroundDialog.setVisible(true);
	}

	
	JMenuItem createMenuItem(String s, int key, JMenu toMenu, ActionListener al) {
		JMenuItem temp = new JMenuItem(s, key);
		temp.addActionListener(al);
		toMenu.add(temp);

		return temp;
	}

	
	JMenuItem createMenuItem(String s, int key, JMenu toMenu, int aclKey, ActionListener al) {
		JMenuItem temp = new JMenuItem(s, key);
		temp.addActionListener(al);
		temp.setAccelerator(KeyStroke.getKeyStroke(aclKey, ActionEvent.CTRL_MASK));
		toMenu.add(temp);

		return temp;
	}

	
	JCheckBoxMenuItem createCheckBoxMenuItem(String s, int key, JMenu toMenu, ActionListener al) {
		JCheckBoxMenuItem temp = new JCheckBoxMenuItem(s);
		temp.setMnemonic(key);
		temp.addActionListener(al);
		temp.setSelected(false);
		toMenu.add(temp);

		return temp;
	}

	
	JMenu createMenu(String s, int key, JMenuBar toMenuBar) {
		JMenu temp = new JMenu(s);
		temp.setMnemonic(key);
		toMenuBar.add(temp);
		return temp;
	}

	
	void createMenuBar(JFrame f) {
		JMenuBar mb = new JMenuBar();

		JMenu fileMenu = createMenu(fileText, KeyEvent.VK_F, mb);
		JMenu editMenu = createMenu(editText, KeyEvent.VK_E, mb);
		JMenu statisticsMenu = createMenu(statistics, KeyEvent.VK_H, mb);
		JMenu formatMenu = createMenu(formatText, KeyEvent.VK_O, mb);
		JMenu viewMenu = createMenu(viewText, KeyEvent.VK_V, mb);		

		createMenuItem(fileNew, KeyEvent.VK_N, fileMenu, KeyEvent.VK_N, this);
		createMenuItem(fileOpen, KeyEvent.VK_O, fileMenu, KeyEvent.VK_O, this);
		createMenuItem(fileSave, KeyEvent.VK_S, fileMenu, KeyEvent.VK_S, this);
		createMenuItem(fileSaveAs, KeyEvent.VK_A, fileMenu, this);
		
		fileMenu.addSeparator();
		createMenuItem(fileExit, KeyEvent.VK_X, fileMenu, this);

		
		editMenu.addSeparator();
		cutItem = createMenuItem(editCut, KeyEvent.VK_T, editMenu, KeyEvent.VK_X, this);
		copyItem = createMenuItem(editCopy, KeyEvent.VK_C, editMenu, KeyEvent.VK_C, this);
		createMenuItem(editPaste, KeyEvent.VK_P, editMenu, KeyEvent.VK_V, this);
		clearItem = createMenuItem(editClear, KeyEvent.VK_L, editMenu, this);
		clearItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLEAR, 0));
		editMenu.addSeparator();
		findItem = createMenuItem(editFind, KeyEvent.VK_F, editMenu, KeyEvent.VK_F, this);
		findNextItem = createMenuItem(editFindNext, KeyEvent.VK_N, editMenu, this);
		findNextItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		replaceItem = createMenuItem(editReplace, KeyEvent.VK_R, editMenu, KeyEvent.VK_H, this);
		gotoItem = createMenuItem(editGoTo, KeyEvent.VK_G, editMenu, KeyEvent.VK_G, this);
		editMenu.addSeparator();
		selectAllItem = createMenuItem(editSelectAll, KeyEvent.VK_A, editMenu, KeyEvent.VK_A, this);
		createMenuItem(editTimeDate, KeyEvent.VK_D, editMenu, this)
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));


		createMenuItem(formatFont, KeyEvent.VK_F, formatMenu, this);
		formatMenu.addSeparator();
		createMenuItem(formatForeground, KeyEvent.VK_T, formatMenu, this);
		createMenuItem(formatBackground, KeyEvent.VK_P, formatMenu, this);

		createCheckBoxMenuItem(viewStatusBar, KeyEvent.VK_S, viewMenu, this).setSelected(true);
		
		LookAndFeelMenu.createLookAndFeelMenuItem(viewMenu, this.f);

		createMenuItem(aboutStatistics, KeyEvent.VK_A, statisticsMenu, this);

		MenuListener editMenuListener = new MenuListener() {
			public void menuSelected(MenuEvent evvvv) {
				if (Notepad.this.ta.getText().length() == 0) {
					findItem.setEnabled(false);
					findNextItem.setEnabled(false);
					replaceItem.setEnabled(false);
					selectAllItem.setEnabled(false);
					gotoItem.setEnabled(false);
				} else {
					findItem.setEnabled(true);
					findNextItem.setEnabled(true);
					replaceItem.setEnabled(true);
					selectAllItem.setEnabled(true);
					gotoItem.setEnabled(true);
				}
				if (Notepad.this.ta.getSelectionStart() == ta.getSelectionEnd()) {
					cutItem.setEnabled(false);
					copyItem.setEnabled(false);
					clearItem.setEnabled(false);
				} else {
					cutItem.setEnabled(true);
					copyItem.setEnabled(true);
					clearItem.setEnabled(true);
				}
			}

			public void menuDeselected(MenuEvent e) {
				
			}

			public void menuCanceled(MenuEvent e) {
				
			}


		};
		editMenu.addMenuListener(editMenuListener);
		f.setJMenuBar(mb);
	}
}
