import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;


public class NaiveBayesTextClassifierUI extends JFrame implements ActionListener {

	private JButton trainButton;
	private JButton classifyButton;
	private JScrollPane scrollPane;
	private JTextArea textArea;
	private JLabel label;

	private KnowledgeRepository repository;
	private FileParser fileParser;
	private final TrainingModule trainer;
	private final ClassificationModule classifier;

	public NaiveBayesTextClassifierUI() {
		super("Naive-Bayes Text Classifier");
		this.setLayout(null);
		this.setSize(790, 500);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.trainButton = new JButton(CommonConstants.TRAIN_BUTTON_CAPTION_TRAIN);
		this.trainButton.setBounds(20, 20, 100, 30);
		this.trainButton.addActionListener(this);
		this.add(trainButton);

		this.classifyButton = new JButton(CommonConstants.CLASSIFY_BUTTON_CAPTION_CLASSIFY);
		this.classifyButton.setBounds(140, 20, 100, 30);
		this.classifyButton.addActionListener(this);
		this.add(classifyButton);

		this.textArea = new JTextArea();
		this.textArea.setEditable(false);
		this.scrollPane = new JScrollPane(this.textArea,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.scrollPane.setBounds(20, 70, 750, 380);
		this.add(scrollPane);

		this.label = new JLabel();
		this.label.setBounds(260, 0, 600, 60);
		this.add(label);

		this.repository = new KnowledgeRepository();
		this.fileParser = new FileParser();

		this.trainer = new TrainingModule(repository, fileParser, this);
		this.classifier = new ClassificationModule(repository, fileParser, this);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(this.trainButton)) {

			if(this.trainButton.getText().equals(CommonConstants.TRAIN_BUTTON_CAPTION_TRAIN)) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileHidingEnabled(true);
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogType(JFileChooser.OPEN_DIALOG);

				int state = chooser.showDialog(null, "Select Directory");

				if (state == JFileChooser.APPROVE_OPTION) {
					final File ngDirectory = chooser.getSelectedFile();

					if (ngDirectory != null && ngDirectory.exists()) {

						final NaiveBayesTextClassifierUI gui = this;
						new Thread() {
							public void run() {

								trainButton.setText(CommonConstants.TRAIN_BUTTON_CAPTION_ABORT);
								classifyButton.setEnabled(false);

								try {
									trainer.trainKnowledgebase(ngDirectory);
								} catch (FileNotFoundException e1) {
									e1.printStackTrace();
									JOptionPane.showMessageDialog(gui, "Error during Training -> " + e1.getMessage());
								} catch (IOException e2) {
									e2.printStackTrace();
									JOptionPane.showMessageDialog(gui, "Error during Training -> " + e2.getMessage());
								}

								trainButton.setText(CommonConstants.TRAIN_BUTTON_CAPTION_TRAIN);
								classifyButton.setEnabled(true);
							}
						}.start();
					}
				}
			} else {
				trainer.abortTraining();
			}
		} else if(e.getSource().equals(this.classifyButton)) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileHidingEnabled(true);
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Files", new String[] { "txt" }));

			int state = chooser.showDialog(null, "Select File");

			if (state == JFileChooser.APPROVE_OPTION) {
				final File inputFile = chooser.getSelectedFile();
				if (inputFile.exists()) {
					final NaiveBayesTextClassifierUI gui = this;
					new Thread() {
						public void run() {

							trainButton.setEnabled(false);

							try {
								classifier.categorize(inputFile);
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(gui, "Error during Classification -> " + e1.getMessage());
							} catch (IOException e2) {
								e2.printStackTrace();
								JOptionPane.showMessageDialog(gui, "Error during Classification -> " + e2.getMessage());
							}

							trainButton.setEnabled(true);
						}
					}.start();
				}
			}
		}
	}

	public void updateTrainingStats(int trainedCategories, int remainingCategories, int totalCategories) {
		String textPrefix = "<html><body>";
		String textSuffix = "</body></html>";
		String lineBreak = "<br/>";

		String fullText = textPrefix + Integer.toString(trainedCategories) + " out of " + Integer.toString(totalCategories) +
			" categories are already trained ..." + lineBreak +
			Integer.toString(remainingCategories) + " out of " + Integer.toString(totalCategories) +
			" categories are remaining ..." + textSuffix;
		this.label.setText(fullText);
		this.label.repaint();
		this.label.revalidate();
	}

	public void clearTrainingStats() {
		this.label.setText("");
		this.label.repaint();
		this.label.revalidate();
	}

	public void addToStatus(String appendText) {

		this.textArea.append(appendText + "\r\n");
		this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
		if(this.textArea.getLineCount() > CommonConstants.SCREEN_LINES_UPPER_BOUND) {
			try {
				this.textArea.select(
					this.textArea.getLineStartOffset(0),
					this.textArea.getLineEndOffset(CommonConstants.SCREEN_LINES_UPPER_BOUND - CommonConstants.SCREEN_LINES_LOWER_BOUND));
				this.textArea.replaceSelection(null);
			}
			catch (BadLocationException e) {
			}
		}

		this.textArea.repaint();
		this.textArea.revalidate();
	}

	public void clearStatus() {
		this.textArea.setText("");
		this.textArea.repaint();
		this.textArea.revalidate();
	}

	public static void main(String args[]) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ex) {

		}

		JFrame ui = new NaiveBayesTextClassifierUI();
		ui.setVisible(true);
	}

}
