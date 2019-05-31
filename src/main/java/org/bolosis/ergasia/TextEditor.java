package org.bolosis.ergasia;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

import javax.swing.JButton;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.apache.commons.lang3.StringUtils;

public class TextEditor implements ActionListener, MenuConstants {

	JFrame frame;
	JTextArea textArea;
	JLabel statusBar;
	JTextField field = new JTextField(50);

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

	TextEditor() {

		frame = new JFrame(fileName + " - " + applicationName);

		frame.add(field, BorderLayout.NORTH);

		textArea = new JTextArea(30, 60);

		statusBar = new JLabel("||       Ln 1, Col 1  ", JLabel.RIGHT);

		frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

		frame.add(statusBar, BorderLayout.SOUTH);

		frame.add(new JLabel("  "), BorderLayout.EAST);

		frame.add(new JLabel("  "), BorderLayout.WEST);

		createMenuBar(frame);

		frame.pack();

		frame.setLocation(100, 50);

		frame.setVisible(true);

		frame.setLocation(150, 50);

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		fileHandler = new FileOperation(this);

		frame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent we) {
				if (fileHandler.confirmSave()) {
					int result = JOptionPane.showConfirmDialog(frame, "Do you want to Exit ?", "Exit Confirmation : ",
							JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION)
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					else if (result == JOptionPane.NO_OPTION)
						frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				}
			}
		});

		textArea.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				int lineNumber = 0, column = 0, pos = 0;

				try {
					pos = textArea.getCaretPosition();
					lineNumber = textArea.getLineOfOffset(pos);
					column = pos - textArea.getLineStartOffset(lineNumber);
				} catch (Exception excp) {
				}
				if (textArea.getText().length() == 0) {
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
		textArea.getDocument().addDocumentListener(myListener);

		WindowListener frameClose = new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if (fileHandler.confirmSave()) {
					if (fileHandler.confirmExit()) {
						frame.dispose();
					}
				}
			}
		};

	}

	void goTo() {
		int lineNumber = 0;
		try {
			lineNumber = textArea.getLineOfOffset(textArea.getCaretPosition()) + 1;
			String tempStr = JOptionPane.showInputDialog(frame, "Enter Line Number:", "" + lineNumber);
			if (tempStr == null) {
				return;
			}
			lineNumber = Integer.parseInt(tempStr);
			textArea.setCaretPosition(textArea.getLineStartOffset(lineNumber - 1));
		} catch (Exception e) {
		}
	}

	public void actionPerformed(ActionEvent ev) {
		String cmdText = ev.getActionCommand();

		if (cmdText.equals(New)) {
			fileHandler.newFile();
			field.setText("");
		}
		else if (cmdText.equals(Open)) {
			File file = fileHandler.openFile();
			try {
				field.setText(file.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		else if (cmdText.equals(Save))
			fileHandler.saveThisFile();

		else if (cmdText.equals(SaveAs))
			fileHandler.saveAsFile();

		else if (cmdText.equals(Exit)) {
			if (fileHandler.confirmSave()) {
				if (fileHandler.confirmExit()) {
					frame.dispose();
				}
			}

		}

		else if (cmdText.equals(cut))
			textArea.cut();

		else if (cmdText.equals(copy))
			textArea.copy();

		else if (cmdText.equals(paste))
			textArea.paste();

		else if (cmdText.equals(clear))
			textArea.replaceSelection("");

		else if (cmdText.equals(find)) {
			if (TextEditor.this.textArea.getText().length() == 0)
				return; // text box have no text
			if (findReplaceDialog == null)
				findReplaceDialog = new FindDialog(TextEditor.this.textArea);
			findReplaceDialog.showDialog(TextEditor.this.frame, true);
		}

		else if (cmdText.equals(findNext)) {
			if (TextEditor.this.textArea.getText().length() == 0)
				return; // text box have no text

			if (findReplaceDialog == null)
				statusBar.setText("Use Find option of Edit Menu first !!!!");
			else
				findReplaceDialog.findNextWithSelection();
		}

		else if (cmdText.equals(replace)) {
			if (TextEditor.this.textArea.getText().length() == 0)
				return; // text box have no text

			if (findReplaceDialog == null)
				findReplaceDialog = new FindDialog(TextEditor.this.textArea);
			findReplaceDialog.showDialog(TextEditor.this.frame, false);// replace
		}

		else if (cmdText.equals(goTo)) {
			if (TextEditor.this.textArea.getText().length() == 0)
				return; // text box have no text
			goTo();
		}

		else if (cmdText.equals(selectAll))
			textArea.selectAll();

		else if (cmdText.equals(timeDate))
			textArea.insert(new Date().toString(), textArea.getSelectionStart());

		else if (cmdText.equals(formatFont)) {
			if (fontDialog == null)
				fontDialog = new FontChooser(textArea.getFont());

			if (fontDialog.showDialog(TextEditor.this.frame, "Choose a font"))
				TextEditor.this.textArea.setFont(fontDialog.createFont());
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
			String fileName = frame.getTitle();
			String title = " - Text Editor - Tasos Bolosis";
			fileName = fileName.replace(title, " ").replaceAll("\\s+", " ");

			try {

				File file = new File(fileName);

				Scanner sc = new Scanner(file);
				Scanner cs = new Scanner(file);

				long lines = 0l;

				long charactersWithGaps = 0l;

				long charactersWithoutGaps = 0l;

				while (sc.hasNextLine()) {
					lines = lines + 1;

					charactersWithGaps = charactersWithGaps + sc.nextLine().length();

				}

				while (cs.hasNextLine()) {
					charactersWithoutGaps = charactersWithoutGaps
							+ StringUtils.deleteWhitespace(cs.nextLine()).length();
				}

				double bytes = (double) file.length() / 1024;

				long words = (long) FileOperation.getNumberOfWords(fileName);

				JOptionPane.showMessageDialog(TextEditor.this.frame,
						FileOperation.invastigationText(words, charactersWithGaps, charactersWithoutGaps, lines, bytes),
						"Results...", JOptionPane.INFORMATION_MESSAGE);
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
			backgroundDialog = JColorChooser.createDialog(TextEditor.this.frame, formatBackground, false, bcolorChooser,
					new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							TextEditor.this.textArea.setBackground(bcolorChooser.getColor());
						}
					}, null);

		backgroundDialog.setVisible(true);
	}

	void showForegroundColorDialog() {
		if (fcolorChooser == null)
			fcolorChooser = new JColorChooser();
		if (foregroundDialog == null)
			foregroundDialog = JColorChooser.createDialog(TextEditor.this.frame, formatForeground, false, fcolorChooser,
					new ActionListener() {
						public void actionPerformed(ActionEvent evvv) {
							TextEditor.this.textArea.setForeground(fcolorChooser.getColor());
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
	
	JButton createButton(String nameButton, ActionListener action) {
		 
		JButton button = new JButton(nameButton);
		
		button.addActionListener(action);
		
		return button;
	}

	void createMenuBar(JFrame frame) {

		JMenuBar mb = new JMenuBar();

		JButton openButton = createButton("Open", new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				File file = fileHandler.openFile();
				try {
					field.setText(file.getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});


		mb.add(openButton);
		
		JButton clearButton = createButton("Clear",new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				textArea.setText("");
			}
		});

		mb.add(clearButton);
		
		JButton saveButton = createButton("Save", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileHandler.saveThisFile();
			}			
		});
		
		mb.add(saveButton);
		
		JButton copyButton = createButton("Copy", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.selectAll();
				textArea.copy();
			}			
		});
		
		mb.add(copyButton);

		JMenu fileMenu = createMenu(fileText, KeyEvent.VK_F, mb);
		JMenu editMenu = createMenu(editText, KeyEvent.VK_E, mb);
		JMenu statisticsMenu = createMenu(statistics, KeyEvent.VK_H, mb);
		JMenu formatMenu = createMenu(formatText, KeyEvent.VK_O, mb);
		JMenu viewMenu = createMenu(viewText, KeyEvent.VK_V, mb);

		createMenuItem(New, KeyEvent.VK_N, fileMenu, KeyEvent.VK_N, this);
		createMenuItem(Open, KeyEvent.VK_O, fileMenu, KeyEvent.VK_O, this);
		createMenuItem(Save, KeyEvent.VK_S, fileMenu, KeyEvent.VK_S, this);
		createMenuItem(SaveAs, KeyEvent.VK_A, fileMenu, this);

		fileMenu.addSeparator();
		createMenuItem(Exit, KeyEvent.VK_X, fileMenu, this);

		editMenu.addSeparator();

		cutItem = createMenuItem(cut, KeyEvent.VK_T, editMenu, KeyEvent.VK_X, this);

		copyItem = createMenuItem(copy, KeyEvent.VK_C, editMenu, KeyEvent.VK_C, this);

		createMenuItem(paste, KeyEvent.VK_P, editMenu, KeyEvent.VK_V, this);

		clearItem = createMenuItem(clear, KeyEvent.VK_L, editMenu, this);

		clearItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLEAR, 0));

		editMenu.addSeparator();

		findItem = createMenuItem(find, KeyEvent.VK_F, editMenu, KeyEvent.VK_F, this);

		findNextItem = createMenuItem(findNext, KeyEvent.VK_N, editMenu, this);

		findNextItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

		replaceItem = createMenuItem(replace, KeyEvent.VK_R, editMenu, KeyEvent.VK_H, this);

		gotoItem = createMenuItem(goTo, KeyEvent.VK_G, editMenu, KeyEvent.VK_G, this);

		editMenu.addSeparator();

		selectAllItem = createMenuItem(selectAll, KeyEvent.VK_A, editMenu, KeyEvent.VK_A, this);
		createMenuItem(timeDate, KeyEvent.VK_D, editMenu, this)
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

		createMenuItem(formatFont, KeyEvent.VK_F, formatMenu, this);
		formatMenu.addSeparator();
		createMenuItem(formatForeground, KeyEvent.VK_T, formatMenu, this);
		createMenuItem(formatBackground, KeyEvent.VK_P, formatMenu, this);

		createCheckBoxMenuItem(viewStatusBar, KeyEvent.VK_S, viewMenu, this).setSelected(true);

		LookAndFeelMenu.createLookAndFeelMenuItem(viewMenu, this.frame);

		createMenuItem(aboutStatistics, KeyEvent.VK_A, statisticsMenu, this);

		MenuListener editMenuListener = new MenuListener() {
			public void menuSelected(MenuEvent evvvv) {
				if (TextEditor.this.textArea.getText().length() == 0) {
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
				if (TextEditor.this.textArea.getSelectionStart() == textArea.getSelectionEnd()) {
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
		frame.setJMenuBar(mb);
	}
}
