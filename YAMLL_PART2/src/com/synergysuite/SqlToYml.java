package com.synergysuite;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SqlToYml extends JFrame {

	private JPanel contentPane;
	private JButton btnLoadFile;
	private JLabel filePath;
	private JButton btnSelectDestionation;
	private JButton btnOk;
	private JFileChooser fc;
	private JLabel fileDestination;
	private JCheckBox chckbxLineYesNo;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SqlToYml frame = new SqlToYml();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public SqlToYml() {
		setTitle("SQL to Yml");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 481, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.add(getBtnLoadFile());
		contentPane.add(getFilePath());
		contentPane.add(getBtnSelectDestionation());
		contentPane.add(getBtnOk());
		contentPane.add(getFileDestination());
		contentPane.add(getChckbxLineYesNo());
	}

	private JButton getBtnLoadFile() {
		if (btnLoadFile == null) {
			btnLoadFile = new JButton("Load SQL File");
			btnLoadFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fc = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter("SQL FILES", "sql", "text");
					fc.setFileFilter(filter);
					fc.setAcceptAllFileFilterUsed(false);
					if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
						filePath.setText(fc.getSelectedFile().toString());
						fc.setEnabled(false);
					}
				}
			});
			btnLoadFile.setBounds(138, 23, 135, 23);
		}
		return btnLoadFile;
	}

	private JLabel getFilePath() {
		if (filePath == null) {
			filePath = new JLabel("");
			filePath.setHorizontalAlignment(SwingConstants.CENTER);
			filePath.setBounds(0, 57, 434, 14);
		}
		return filePath;
	}

	private JButton getBtnSelectDestionation() {
		if (btnSelectDestionation == null) {
			btnSelectDestionation = new JButton("Destination Folder");
			btnSelectDestionation.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fc = new JFileChooser();
					if (filePath.getText() != "") {
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						fc.setAcceptAllFileFilterUsed(false);
						if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
							fileDestination.setText(fc.getSelectedFile().toString());
							fc.setVisible(false);
						}
					} else {
						JOptionPane.showMessageDialog(null, "You have to choose an sql file.");
						return;
					}
				}
			});
			btnSelectDestionation.setBounds(126, 99, 159, 23);
		}
		return btnSelectDestionation;
	}

	private JButton getBtnOk() {
		if (btnOk == null) {
			btnOk = new JButton("Export");
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (filePath.getText() != "" && fileDestination.getText() != "") {
						fc = new JFileChooser();
						try {
							URL resource2 = new File(filePath.getText()).toURI().toURL();
							System.out.println(resource2);
							List<String> lines = Files.readAllLines(new File(resource2.toURI()).toPath());
							Map<String, List<Row>> data = new HashMap<>();
							for (String line : lines) {
								if (!line.toLowerCase().trim().startsWith("insert")) {
									continue;
								}
								String tableName = line
										.substring(line.toLowerCase().indexOf("into") + 4, line.indexOf("(")).trim()
										.toUpperCase();
								if (!data.containsKey(tableName)) {
									data.put(tableName, new ArrayList<>(100));
								}
								int firstParenth = line.indexOf("(") + 1;
								int firstClosedParenth = line.indexOf(")") + 1;
								String[] fields = line.substring(firstParenth, firstClosedParenth - 1).split(",");
								String[] values = line.substring(line.indexOf("(", firstParenth) + 1,
										line.indexOf(")", firstClosedParenth)).split(",");
								Row newRow = new Row();

								if (chckbxLineYesNo.isSelected()) {
									for (int i = 0; i < fields.length; i++) {

										if (fields[i].contains("_")) {
											String[] undr = fields[i].split("_");
											String all = "";
											String all2 = "";
											for (String a : undr) {
												String b = a.substring(0, 1).toUpperCase() + a.substring(1);
												all += b;

											}
											all2 += all.substring(0, 1).toUpperCase() + all.substring(1);
											fields[i] = all2.replaceAll(" ", "");
										}
									}

								} 
									for (int i = 0; i < fields.length; i++) {
										newRow.addColumn(new KeyValuePair().withName(fields[i].trim())
												.withValue(values[i].trim().replaceAll("'", "")));
									}
									System.out.println(Arrays.asList(fields) + " vs " + Arrays.asList(values));

								data.get(tableName).add(newRow);
							}
							for (Map.Entry<String, List<Row>> table : data.entrySet()) {
								StringBuilder tableSB = new StringBuilder(table.getKey() + ":\n");
								List<Row> rows = table.getValue();
								for (Row row : rows) {
									KeyValuePair firstColumn = row.getColumns().get(0);
									tableSB.append(
											" - " + firstColumn.getName() + ": " + firstColumn.getValue() + "\r");
									for (int i = 1; i < row.getColumns().size(); i++) {
										KeyValuePair column = row.getColumns().get(i);
										tableSB.append("    " + column.getName() + ": " + column.getValue() + "\r");
									}
								}
								File fileToWrite = new File(
										fileDestination.getText() + "/" + table.getKey().toLowerCase() + ".yml");
								fileToWrite.createNewFile();
								Files.write(fileToWrite.toPath(), tableSB.toString().getBytes(),
										StandardOpenOption.TRUNCATE_EXISTING);
								System.out.println("Wrote " + fileToWrite.getAbsolutePath());

							}
							JOptionPane.showMessageDialog(null, "Successfully exported.");
							filePath.setText("");
							fileDestination.setText("");
							chckbxLineYesNo.setSelected(false);
						} catch (ArrayIndexOutOfBoundsException ee) {
							JOptionPane.showMessageDialog(null, "You have to choose an sql file.");

						} catch (URISyntaxException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(null, "You must load an SQL file and select directory!",
								"Information", JOptionPane.WARNING_MESSAGE);
					}
				}
			});
			btnOk.setBounds(164, 182, 89, 23);
		}
		return btnOk;
	}

	private JLabel getFileDestination() {
		if (fileDestination == null) {
			fileDestination = new JLabel("");
			fileDestination.setHorizontalAlignment(SwingConstants.CENTER);
			fileDestination.setBounds(0, 145, 424, 14);
		}
		return fileDestination;
	}

	private void createUIComponents() {
		// TODO: place custom component creation code here
	}

	public static class Row {
		List<KeyValuePair> columns;

		public Row() {
			this.columns = new ArrayList<>(100);
		}

		public List<KeyValuePair> getColumns() {
			return columns;
		}

		public void addColumn(KeyValuePair pair) {
			this.columns.add(pair);
		}
	}

	public static class KeyValuePair implements Serializable {
		private String name;
		private String value;

		public KeyValuePair() {
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public KeyValuePair withName(String name) {
			this.setName(name);
			return this;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

		public KeyValuePair withValue(String value) {
			this.setValue(value);
			return this;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			if (this.getName() != null) {
				sb.append("Name: ").append(this.getName()).append(",");
			}

			if (this.getValue() != null) {
				sb.append("Value: ").append(this.getValue());
			}
			sb.append("}");
			return sb.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			KeyValuePair that = (KeyValuePair) o;
			return Objects.equals(name, that.name) && Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, value);
		}
	}

	private JCheckBox getChckbxLineYesNo() {
		if (chckbxLineYesNo == null) {
			chckbxLineYesNo = new JCheckBox("Export without underscore");
			chckbxLineYesNo.setBounds(259, 182, 200, 23);
		}
		return chckbxLineYesNo;
	}
}
