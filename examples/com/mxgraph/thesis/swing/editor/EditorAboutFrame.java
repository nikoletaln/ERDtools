package com.mxgraph.thesis.swing.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;

public class EditorAboutFrame extends JDialog {

	public EditorAboutFrame(Frame owner) {
		super(owner);
		setTitle("About ERDT");
		setLayout(new BorderLayout());

		// Creates the gradient panel
		JPanel panel = new JPanel(new BorderLayout()) {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);

				// Paint gradient background
				Graphics2D g2d = (Graphics2D) g;
				g2d.setPaint(new GradientPaint(0, 0, Color.WHITE, getWidth(), 0, getBackground()));
				g2d.fillRect(0, 0, getWidth(), getHeight());
			}
		};

		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
				BorderFactory.createEmptyBorder(8, 8, 12, 8)));

		// Adds title
		JLabel titleLabel = new JLabel("ER diagrams design and automatic SQL code creation software");
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		titleLabel.setOpaque(false);

		panel.add(titleLabel, BorderLayout.NORTH);

		// Adds optional subtitle
		JLabel subtitleLabel = new JLabel("Based on jgraph library https://github.com/jgraph/jgraphx/");
		subtitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 18, 0, 0));
		subtitleLabel.setOpaque(false);
		panel.add(subtitleLabel, BorderLayout.CENTER);

		getContentPane().add(panel, BorderLayout.NORTH);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		JLabel name = new JLabel("Created by: Nikoleta Lavdaria");
		name.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		content.add(name);

		String address = "nikoletaln@gmail.com";
		JLabel label = new JLabel("<html><font size=2><a href=#>" + address + "</a></font></html>");
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().mail(new URI("mailto:" + address + "?subject=Hello"));
				} catch (URISyntaxException | IOException ex) {
				}
			}
		});
		content.add(label);

		content.add(Box.createVerticalStrut(10));

		JLabel supervisor = new JLabel("Supervised by: Michael Vassilakopoulos");
		supervisor.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		content.add(supervisor);

		content.add(Box.createVerticalStrut(10));

		content.add(new JLabel("ERD tools is a powerful application developed as part of my thesis project."));
		content.add(Box.createVerticalStrut(10));
		content.add(new JLabel("It provides:"));
		content.add(new JLabel("  - A user-friendly interface for creating (Enhanced) Entity-Relationship Diagrams (ERDs)"));
		content.add(new JLabel("  - Error Checking that analyzes the diagram and provides feedback"));
		content.add(new JLabel("  - Relational Schema Generation that corresponds to the created ER diagram"));
		content.add(new JLabel("  - SQL Code Generation that creates the entire relational model and eliminates the need for manual coding"));

		content.add(Box.createVerticalStrut(10));

		content.add(new JLabel("Free for personal and educational use"));
		content.add(new JLabel("Hellenic Open University 2022-2023"));
		content.add(new JLabel("Master in Information Systems"));
		content.add(Box.createVerticalStrut(4));

		// Create the logo label
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/com/mxgraph/thesis/swing/images/images.png"));
        JLabel logoLabel = new JLabel(logoIcon);
       // logoLabel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
         // Add the logo label to the content panel
        content.add(logoLabel);

		try {
			content.add(Box.createVerticalStrut(10));
		} catch (Exception e) {
			// ignore
		}

		getContentPane().add(content, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
				BorderFactory.createEmptyBorder(16, 8, 8, 8)));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		// Adds OK button to close window
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		buttonPanel.add(closeButton);

		// Sets default button for enter key
		getRootPane().setDefaultButton(closeButton);

		setResizable(false);
		setSize(600, 600);
	}

	/**
	 * Overrides {@link JDialog#createRootPane()} to return a root pane that hides the window when the user presses
	 * the ESCAPE key.O
	 */
	protected JRootPane createRootPane() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				setVisible(false);
			}
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		return rootPane;
	}

}
