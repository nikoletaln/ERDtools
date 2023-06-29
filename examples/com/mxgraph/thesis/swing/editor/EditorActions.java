/*
 * Copyright (c) 2001-2012, JGraph Ltd
 */
package com.mxgraph.thesis.swing.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.w3c.dom.Document;
import com.mxgraph.analysis.mxDistanceCostFunction;
import com.mxgraph.analysis.mxGraphAnalysis;
import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.canvas.mxSvgCanvas;
import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxGdCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.shape.mxStencilShape;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.swing.view.mxCellEditor;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxCellRenderer.CanvasFactory;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.util.png.mxPngTextDecoder;
import com.mxgraph.view.mxGraph;
import org.w3c.dom.Element;


public class EditorActions
{
	/**
	 * 
	 * @param e
	 * @return Returns the graph for the given action event.
	 */
	public static final BasicGraphEditor getEditor(ActionEvent e)
	{
		if (e.getSource() instanceof Component)
		{
			Component component = (Component) e.getSource();

			while (component != null
					&& !(component instanceof BasicGraphEditor))
			{
				component = component.getParent();
			}

			return (BasicGraphEditor) component;
		}

		return null;
	}

	
	@SuppressWarnings("serial")
	public static class ToggleRulersItem extends JCheckBoxMenuItem
	{
		
		public ToggleRulersItem(final BasicGraphEditor editor, String name)
		{
			super(name);
			setSelected(editor.getGraphComponent().getColumnHeader() != null);

			addActionListener(new ActionListener()
			{
				
				public void actionPerformed(ActionEvent e)
				{
					mxGraphComponent graphComponent = editor
							.getGraphComponent();

					if (graphComponent.getColumnHeader() != null)
					{
						graphComponent.setColumnHeader(null);
						graphComponent.setRowHeader(null);
					}
					else
					{
						graphComponent.setColumnHeaderView(new EditorRuler(
								graphComponent,
								EditorRuler.ORIENTATION_HORIZONTAL));
						graphComponent.setRowHeaderView(new EditorRuler(
								graphComponent,
								EditorRuler.ORIENTATION_VERTICAL));
					}
				}
			});
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleGridItem extends JCheckBoxMenuItem
	{
		
		public ToggleGridItem(final BasicGraphEditor editor, String name)
		{
			super(name);
			setSelected(true);

			addActionListener(new ActionListener()
			{
				
				public void actionPerformed(ActionEvent e)
				{
					mxGraphComponent graphComponent = editor
							.getGraphComponent();
					mxGraph graph = graphComponent.getGraph();
					boolean enabled = !graph.isGridEnabled();

					graph.setGridEnabled(enabled);
					graphComponent.setGridVisible(enabled);
					graphComponent.repaint();
					setSelected(enabled);
				}
			});
		}
	}


	@SuppressWarnings("serial")
	public static class ToggleOutlineItem extends JCheckBoxMenuItem
	{
		
		public ToggleOutlineItem(final BasicGraphEditor editor, String name)
		{
			super(name);
			setSelected(true);

			addActionListener(new ActionListener()
			{
				
				public void actionPerformed(ActionEvent e)
				{
					final mxGraphOutline outline = editor.getGraphOutline();
					outline.setVisible(!outline.isVisible());
					outline.revalidate();

					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							if (outline.getParent() instanceof JSplitPane)
							{
								if (outline.isVisible())
								{
									((JSplitPane) outline.getParent())
											.setDividerLocation(editor
													.getHeight() - 300);
									((JSplitPane) outline.getParent())
											.setDividerSize(6);
								}
								else
								{
									((JSplitPane) outline.getParent())
											.setDividerSize(0);
								}
							}
						}
					});
				}
			});
		}
	}

	
	@SuppressWarnings("serial")
	public static class ExitAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			BasicGraphEditor editor = getEditor(e);
		// if we have created something ask before exit
			if (editor != null)
			{
				if (!editor.isModified()
						|| JOptionPane.showConfirmDialog(editor,
								mxResources.get("loseChanges")) == JOptionPane.YES_OPTION)

				if (editor != null)
				{
					editor.exit();
				}
		    }
		}
	}

	@SuppressWarnings("serial")
	public static class StylesheetAction extends AbstractAction
	{
		protected String stylesheet;

		public StylesheetAction(String stylesheet)
		{
			this.stylesheet = stylesheet;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				mxGraph graph = graphComponent.getGraph();
				mxCodec codec = new mxCodec();
				Document doc = mxUtils.loadDocument(EditorActions.class
						.getResource(stylesheet).toString());

				if (doc != null)
				{
					codec.decode(doc.getDocumentElement(),
							graph.getStylesheet());
					graph.refresh();
				}
			}
		}
	}


	@SuppressWarnings("serial")
	public static class ZoomPolicyAction extends AbstractAction
	{
		protected int zoomPolicy;

		public ZoomPolicyAction(int zoomPolicy)
		{
			this.zoomPolicy = zoomPolicy;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				graphComponent.setPageVisible(true);
				graphComponent.setZoomPolicy(zoomPolicy);
			}
		}
	}

	@SuppressWarnings("serial")
	public static class GridStyleAction extends AbstractAction
	{
		protected int style;
		public GridStyleAction(int style)
		{
			this.style = style;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				graphComponent.setGridStyle(style);
				graphComponent.repaint();
			}
		}
	}

	@SuppressWarnings("serial")
	public static class ScaleAction extends AbstractAction
	{	
		protected double scale;
		public ScaleAction(double scale)
		{
			this.scale = scale;
		}
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				double scale = this.scale;

				if (scale == 0)
				{
					String value = (String) JOptionPane.showInputDialog(
							graphComponent, mxResources.get("value"),
							mxResources.get("scale") + " (%)",
							JOptionPane.PLAIN_MESSAGE, null, null, "");

					if (value != null)
					{
						scale = Double.parseDouble(value.replace("%", "")) / 100;
					}
				}

				if (scale > 0)
				{
					graphComponent.zoomTo(scale, graphComponent.isCenterZoom());
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class PageSetupAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				PrinterJob pj = PrinterJob.getPrinterJob();
				PageFormat format = pj.pageDialog(graphComponent
						.getPageFormat());

				if (format != null)
				{
					graphComponent.setPageFormat(format);
					graphComponent.zoomAndCenter();
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class PrintAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				PrinterJob pj = PrinterJob.getPrinterJob();

				if (pj.printDialog())
				{
					PageFormat pf = graphComponent.getPageFormat();
					Paper paper = new Paper();
					double margin = 0;
					paper.setImageableArea(margin, margin, pf.getWidth(), pf.getHeight());
					pf.setPaper(paper);
					pj.setPrintable(graphComponent, pf);
			
					try
					{
						pj.print();
					}
					catch (PrinterException e2)
					{
						System.out.println(e2);
					}
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class SaveAction extends AbstractAction
	{
		protected boolean showDialog;
		protected String lastDir = null;
		public SaveAction(boolean showDialog)
		{
			this.showDialog = showDialog;
		}

		 //Saves XML-PNG format.
		protected void savePng(BasicGraphEditor editor, String filename,
				Color bg) throws IOException
		{
			mxGraphComponent graphComponent = editor.getGraphComponent();
			mxGraph graph = graphComponent.getGraph();

			// Creates the image for the PNG file
			BufferedImage image = mxCellRenderer.createBufferedImage(graph,
					null, 1, bg, graphComponent.isAntiAlias(), null,
					graphComponent.getCanvas());

			// Creates the URL-encoded XML data
			mxCodec codec = new mxCodec();
			String xml = URLEncoder.encode(
					mxXmlUtils.getXml(codec.encode(graph.getModel())), "UTF-8");
			mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);
			param.setCompressedText(new String[] { "xml", xml });

			// Saves as a PNG file
			FileOutputStream outputStream = new FileOutputStream(new File(
					filename));
			try
			{
				mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream,
						param);

				if (image != null)
				{
					encoder.encode(image);

					editor.setModified(false);
					editor.setCurrentFile(new File(filename));
				}
				else
				{
					JOptionPane.showMessageDialog(graphComponent,
							mxResources.get("noImageData"));
				}
			}
			finally
			{
				outputStream.close();
			}
		}

		public void actionPerformed(ActionEvent e)
		{
			BasicGraphEditor editor = getEditor(e);

			if (editor != null)
			{
				mxGraphComponent graphComponent = editor.getGraphComponent();
				mxGraph graph = graphComponent.getGraph();
				FileFilter selectedFilter = null;

				// Adds xml format
				DefaultFileFilter erdtFilter = new DefaultFileFilter(".erdt",
        		"ERDT " + mxResources.get("file") + " (.erdt)");
				
				String filename = null;
				boolean dialogShown = false;

				if (showDialog || editor.getCurrentFile() == null)
				{
					String wd;

					if (lastDir != null)
					{
						wd = lastDir;
					}
					else if (editor.getCurrentFile() != null)
					{
						wd = editor.getCurrentFile().getParent();
					}
					else
					{
						wd = System.getProperty("user.dir");
					}


					JFileChooser fc = new JFileChooser(wd);

					fc.setAcceptAllFileFilterUsed(false);

					// Adds the default file format
					FileFilter defaultFilter = erdtFilter;
					fc.addChoosableFileFilter(defaultFilter);
						

					int rc = fc.showDialog(null, mxResources.get("save"));
					dialogShown = true;

					if (rc != JFileChooser.APPROVE_OPTION)
					{
						return;
					}
					else
					{
						lastDir = fc.getSelectedFile().getParent();
					}

					filename = fc.getSelectedFile().getAbsolutePath();
					selectedFilter = fc.getFileFilter();

					if (selectedFilter instanceof DefaultFileFilter) {
						String ext = ((DefaultFileFilter) selectedFilter).getExtension();
					
						if (!filename.toLowerCase().endsWith(ext)) {
							filename += ext;
						}
					}

					if (new File(filename).exists()
							&& JOptionPane.showConfirmDialog(graphComponent,
									mxResources.get("overwriteExistingFile")) != JOptionPane.YES_OPTION)
					{
						return;
					}
				}
				else
				{
					filename = editor.getCurrentFile().getAbsolutePath();
				}

				try
				{
					String ext = filename.substring(filename.lastIndexOf('.') + 1);

					if (ext.equalsIgnoreCase("erdt")) {
						mxCodec codec = new mxCodec();
						String xml = mxXmlUtils.getXml(codec.encode(graph.getModel()));
					
						mxUtils.writeFile(xml, filename);
					
						editor.setModified(false);
						editor.setCurrentFile(new File(filename));
					}
								
				}	
				catch (Throwable ex)
				{
					ex.printStackTrace();
					JOptionPane.showMessageDialog(graphComponent,
							ex.toString(), mxResources.get("error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}


	@SuppressWarnings("serial")
	public static class ExportAction extends AbstractAction{

		
		protected boolean showDialog;
		protected String lastDir = null;
		protected double marginPercentage = 0.01;
		public ExportAction(boolean showDialog)
		{
			this.showDialog = showDialog;
		}

		 //Saves XML-PNG format.
		protected void savePng(BasicGraphEditor editor, String filename,
				Color bg) throws IOException
		{
			mxGraphComponent graphComponent = editor.getGraphComponent();
			mxGraph graph = graphComponent.getGraph();

			// Creates the image for the PNG file
			BufferedImage image = mxCellRenderer.createBufferedImage(graph,
					null, 1, bg, graphComponent.isAntiAlias(), null,
					graphComponent.getCanvas());

			 // Calculates the margin based on the image size
			 int marginX = (int) (image.getWidth() * marginPercentage);
			 int marginY = (int) (image.getHeight() * marginPercentage);
			 
			 // Creates the image with margins
			 BufferedImage imageWithMargin = new BufferedImage(image.getWidth() + 2 * marginX, image.getHeight() + 2 * marginY, BufferedImage.TYPE_INT_RGB);
			 Graphics2D g2 = imageWithMargin.createGraphics();
			 g2.setColor(bg);
			 g2.fillRect(0, 0, imageWithMargin.getWidth(), imageWithMargin.getHeight());
			 g2.drawImage(image, marginX, marginY, null);
			 g2.dispose();		

			// Saves as a PNG file
			FileOutputStream outputStream = new FileOutputStream(new File(filename));
			try {
				if (imageWithMargin != null) {
					ImageIO.write(imageWithMargin, "PNG", outputStream);
	
					editor.setModified(false);
					editor.setCurrentFile(new File(filename));
				} else {
					JOptionPane.showMessageDialog(graphComponent, mxResources.get("noImageData"));
				}
			}
			finally
			{
				outputStream.close();
			}
		}

		public void actionPerformed(ActionEvent e)
		{
			BasicGraphEditor editor = getEditor(e);

			if (editor != null)
			{
				mxGraphComponent graphComponent = editor.getGraphComponent();
				mxGraph graph = graphComponent.getGraph();
				FileFilter selectedFilter = null;
				
				String filename = null;
				boolean dialogShown = false;

				if (showDialog || editor.getCurrentFile() == null)
				{
					String wd;
					if (lastDir != null)
					{
						wd = lastDir;
					}
					else if (editor.getCurrentFile() != null)
					{
						wd = editor.getCurrentFile().getParent();
					}
					else
					{
						wd = System.getProperty("user.dir");
					}

					JFileChooser fc = new JFileChooser(wd);
					fc.setAcceptAllFileFilterUsed(false);

					// Adds special vector graphics format
					fc.addChoosableFileFilter(new DefaultFileFilter(".svg",
							"SVG " + mxResources.get("file") + " (.svg)"));

					// Adds png format
					fc.addChoosableFileFilter(new DefaultFileFilter(".png",
							"PNG " + mxResources.get("file") + " (.png)"));								

					int rc = fc.showDialog(null, mxResources.get("save"));
					dialogShown = true;

					if (rc != JFileChooser.APPROVE_OPTION)
					{
						return;
					}
					else
					{
						lastDir = fc.getSelectedFile().getParent();
					}

					filename = fc.getSelectedFile().getAbsolutePath();
					selectedFilter = fc.getFileFilter();

					if (selectedFilter instanceof DefaultFileFilter)
					{
						String ext = ((DefaultFileFilter) selectedFilter)
								.getExtension();

						if (!filename.toLowerCase().endsWith(ext))
						{
							filename += ext;
						}
					}

					if (new File(filename).exists()
							&& JOptionPane.showConfirmDialog(graphComponent,
									mxResources.get("overwriteExistingFile")) != JOptionPane.YES_OPTION)
					{
						return;
					}
				}
				else
				{
					filename = editor.getCurrentFile().getAbsolutePath();
				}

				try
				{
					String ext = filename.substring(filename.lastIndexOf('.') + 1);

					if (ext.equalsIgnoreCase("svg")) {
						Color bg = graphComponent.getBackground();
						mxSvgCanvas canvas = (mxSvgCanvas) mxCellRenderer.drawCells(graph, null, 1, null, new CanvasFactory() {
							public mxICanvas createCanvas(int width, int height) {
								mxSvgCanvas canvas = new mxSvgCanvas(mxDomUtils.createSvgDocument(width, height));
								canvas.setEmbedded(true);
	
								return canvas;
							}
						});
	
						try {
							Document svgDocument = canvas.getDocument();
	
							// Set background color in the SVG document
							Element root = svgDocument.getDocumentElement();
							root.setAttribute("style", "background-color: " + toHex(bg));
	
							FileWriter fileWriter = new FileWriter(filename);
							PrintWriter printWriter = new PrintWriter(fileWriter);
							printWriter.print(mxXmlUtils.getXml(svgDocument));
							printWriter.close();
						} catch (IOException ea) {
							ea.printStackTrace();
						}
					}
					
					
								
					
					else
					{
						Color bg = null;
						bg = graphComponent.getBackground();

					    if (ext.equalsIgnoreCase("png")){
							savePng(editor, filename, bg);
					    }

						else
						{
							BufferedImage image = mxCellRenderer
									.createBufferedImage(graph, null, 1, bg,
										graphComponent.isAntiAlias(), null,
										graphComponent.getCanvas());

							if (image != null)
							{
								ImageIO.write(image, ext, new File(filename));
							}
							else
							{
								JOptionPane.showMessageDialog(graphComponent,
										mxResources.get("noImageData"));
							}
						}
				    }
				}
				catch (Throwable ex)
				{
					ex.printStackTrace();
					JOptionPane.showMessageDialog(graphComponent,
							ex.toString(), mxResources.get("error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}


// Helper method to convert Color to hex string
private static String toHex(Color color) {
	return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
}


	@SuppressWarnings("serial")
	public static class ToggleConnectModeAction extends AbstractAction
	{
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				mxConnectionHandler handler = graphComponent
						.getConnectionHandler();
				handler.setHandleEnabled(!handler.isHandleEnabled());
			}
		}
	}

	
	@SuppressWarnings("serial")
	public static class ToggleCreateTargetItem extends JCheckBoxMenuItem
	{
		public ToggleCreateTargetItem(final BasicGraphEditor editor, String name)
		{
			super(name);
			setSelected(true);

			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					mxGraphComponent graphComponent = editor
							.getGraphComponent();

					if (graphComponent != null)
					{
						mxConnectionHandler handler = graphComponent
								.getConnectionHandler();
						handler.setCreateTarget(!handler.isCreateTarget());
						setSelected(handler.isCreateTarget());
					}
				}
			});
		}
	}

	@SuppressWarnings("serial")
	public static class PromptPropertyAction extends AbstractAction
	{
		protected Object target;
		protected String fieldname, message;
		public PromptPropertyAction(Object target, String message)
		{
			this(target, message, message);
		}

		public PromptPropertyAction(Object target, String message,
				String fieldname)
		{
			this.target = target;
			this.message = message;
			this.fieldname = fieldname;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof Component)
			{
				try
				{
					Method getter = target.getClass().getMethod(
							"get" + fieldname);
					Object current = getter.invoke(target);

					// TODO: Support other atomic types
					if (current instanceof Integer)
					{
						Method setter = target.getClass().getMethod(
								"set" + fieldname, new Class[] { int.class });

						String value = (String) JOptionPane.showInputDialog(
								(Component) e.getSource(), "Value", message,
								JOptionPane.PLAIN_MESSAGE, null, null, current);

						if (value != null)
						{
							setter.invoke(target, Integer.parseInt(value));
						}
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}

			// Repaints the graph component
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				graphComponent.repaint();
			}
		}
	}

	@SuppressWarnings("serial")
	public static class TogglePropertyItem extends JCheckBoxMenuItem
	{
		public TogglePropertyItem(Object target, String name, String fieldname)
		{
			this(target, name, fieldname, false);
		}

		public TogglePropertyItem(Object target, String name, String fieldname,
				boolean refresh)
		{
			this(target, name, fieldname, refresh, null);
		}

		public TogglePropertyItem(final Object target, String name,
				final String fieldname, final boolean refresh,
				ActionListener listener)
		{
			super(name);
			// Since action listeners are processed last to first we add the given
			// listener here which means it will be processed after the one below
			if (listener != null)
			{
				addActionListener(listener);
			}

			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					execute(target, fieldname, refresh);
				}
			});

			PropertyChangeListener propertyChangeListener = new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent evt)
				{
					if (evt.getPropertyName().equalsIgnoreCase(fieldname))
					{
						update(target, fieldname);
					}
				}
			};

			if (target instanceof mxGraphComponent)
			{
				((mxGraphComponent) target)
						.addPropertyChangeListener(propertyChangeListener);
			}
			else if (target instanceof mxGraph)
			{
				((mxGraph) target)
						.addPropertyChangeListener(propertyChangeListener);
			}

			update(target, fieldname);
		}

		public void update(Object target, String fieldname)
		{
			if (target != null && fieldname != null)
			{
				try
				{
					Method getter = target.getClass().getMethod(
							"is" + fieldname);

					if (getter != null)
					{
						Object current = getter.invoke(target);

						if (current instanceof Boolean)
						{
							setSelected(((Boolean) current).booleanValue());
						}
					}
				}
				catch (Exception e)
				{
					// ignore
				}
			}
		}

		public void execute(Object target, String fieldname, boolean refresh)
		{
			if (target != null && fieldname != null)
			{
				try
				{
					Method getter = target.getClass().getMethod(
							"is" + fieldname);
					Method setter = target.getClass().getMethod(
							"set" + fieldname, new Class[] { boolean.class });

					Object current = getter.invoke(target);

					if (current instanceof Boolean)
					{
						boolean value = !((Boolean) current).booleanValue();
						setter.invoke(target, value);
						setSelected(value);
					}

					if (refresh)
					{
						mxGraph graph = null;

						if (target instanceof mxGraph)
						{
							graph = (mxGraph) target;
						}
						else if (target instanceof mxGraphComponent)
						{
							graph = ((mxGraphComponent) target).getGraph();
						}

						graph.refresh();
					}
				}
				catch (Exception e)
				{
					// ignore
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class HistoryAction extends AbstractAction
	{
		protected boolean undo;

		public HistoryAction(boolean undo)
		{
			this.undo = undo;
		}

		public void actionPerformed(ActionEvent e)
		{
			BasicGraphEditor editor = getEditor(e);

			if (editor != null)
			{
				if (undo)
				{
					editor.getUndoManager().undo();
				}
				else
				{
					editor.getUndoManager().redo();
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class NewAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			BasicGraphEditor editor = getEditor(e);

			if (editor != null)
			{
				if (!editor.isModified()
						|| JOptionPane.showConfirmDialog(editor,
								mxResources.get("loseChanges")) == JOptionPane.YES_OPTION)
				{
					mxGraph graph = editor.getGraphComponent().getGraph();

					// Check modified flag and display save dialog
					mxCell root = new mxCell();
					root.insert(new mxCell());
					graph.getModel().setRoot(root);

					editor.setModified(false);
					editor.setCurrentFile(null);
					editor.getGraphComponent().zoomAndCenter();
				}
			}
		}
	}


	@SuppressWarnings("serial")
	public static class OpenAction extends AbstractAction
	{
		protected String lastDir;
		protected void resetEditor(BasicGraphEditor editor)
		{
			editor.setModified(false);
			editor.getUndoManager().clear();
			editor.getGraphComponent().zoomAndCenter();
		}
		
		protected void openPng(BasicGraphEditor editor, File file)
				throws IOException
		{
			Map<String, String> text = mxPngTextDecoder
					.decodeCompressedText(new FileInputStream(file));

			if (text != null)
			{
				String value = text.get("xml");

				if (value != null)
				{
					Document document = mxXmlUtils.parseXml(URLDecoder.decode(
							value, "UTF-8"));
					mxCodec codec = new mxCodec(document);
					codec.decode(document.getDocumentElement(), editor
							.getGraphComponent().getGraph().getModel());
					editor.setCurrentFile(file);
					resetEditor(editor);

					return;
				}
			}
			JOptionPane.showMessageDialog(editor,
					mxResources.get("imageContainsNoDiagramData"));
		}

		/**
		 * @throws IOException
		 *
		 */
		protected void openGD(BasicGraphEditor editor, File file,
				String gdText)
		{
			mxGraph graph = editor.getGraphComponent().getGraph();

			// Replaces file extension with .xml
			String filename = file.getName();
			filename = filename.substring(0, filename.length() - 4) + ".xml";

			if (new File(filename).exists()
					&& JOptionPane.showConfirmDialog(editor,
							mxResources.get("overwriteExistingFile")) != JOptionPane.YES_OPTION)
			{
				return;
			}

			((mxGraphModel) graph.getModel()).clear();
			mxGdCodec.decode(gdText, graph);
			editor.getGraphComponent().zoomAndCenter();
			editor.setCurrentFile(new File(lastDir + "/" + filename));
		}

		public void actionPerformed(ActionEvent e)
		{
			BasicGraphEditor editor = getEditor(e);

			if (editor != null)
			{
				if (!editor.isModified()
						|| JOptionPane.showConfirmDialog(editor,
								mxResources.get("loseChanges")) == JOptionPane.YES_OPTION)
				{
					mxGraph graph = editor.getGraphComponent().getGraph();

					if (graph != null)
					{
						String wd = (lastDir != null) ? lastDir : System
								.getProperty("user.dir");

						JFileChooser fc = new JFileChooser(wd);
						fc.setAcceptAllFileFilterUsed(false);
						
						// Adds file filter for supported file format
						DefaultFileFilter defaultFilter = new DefaultFileFilter(".erdt",
						"ERDT" + mxResources.get("file") + " (.erdt)");						
						fc.addChoosableFileFilter(defaultFilter);
						fc.setFileFilter(defaultFilter);

						int rc = fc.showDialog(null,
								mxResources.get("openFile"));

						if (rc == JFileChooser.APPROVE_OPTION)
						{
							lastDir = fc.getSelectedFile().getParent();

							try
							{
									Document document = mxXmlUtils
											.parseXml(mxUtils.readFile(fc
													.getSelectedFile()
													.getAbsolutePath()));

									mxCodec codec = new mxCodec(document);
									codec.decode(
											document.getDocumentElement(),
											graph.getModel());
									editor.setCurrentFile(fc
											.getSelectedFile());

									resetEditor(editor);
								}
							
							catch (IOException ex)
							{
								ex.printStackTrace();
								JOptionPane.showMessageDialog(
										editor.getGraphComponent(),
										ex.toString(),
										mxResources.get("error"),
										JOptionPane.ERROR_MESSAGE);
							}
						}
					}
	           }
			}	
	    }		   
	}


	@SuppressWarnings("serial")
	public static class ToggleAction extends AbstractAction
	{
		protected String key;
		protected boolean defaultValue;

		/**
		 * 
		 * @param key
		 */
		public ToggleAction(String key)
		{
			this(key, false);
		}

		/**
		 * 
		 * @param key
		 */
		public ToggleAction(String key, boolean defaultValue)
		{
			this.key = key;
			this.defaultValue = defaultValue;
		}

		public void actionPerformed(ActionEvent e)
		{
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null)
			{
				graph.toggleCellStyles(key, defaultValue);
			}
		}
	}

	@SuppressWarnings("serial")
	public static class SetLabelPositionAction extends AbstractAction
	{
		protected String labelPosition, alignment;

		public SetLabelPositionAction(String labelPosition, String alignment)
		{
			this.labelPosition = labelPosition;
			this.alignment = alignment;
		}

		public void actionPerformed(ActionEvent e)
		{
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty())
			{
				graph.getModel().beginUpdate();
				try
				{
					// Checks the orientation of the alignment to use the correct constants
					if (labelPosition.equals(mxConstants.ALIGN_LEFT)
							|| labelPosition.equals(mxConstants.ALIGN_CENTER)
							|| labelPosition.equals(mxConstants.ALIGN_RIGHT))
					{
						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION,
								labelPosition);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, alignment);
					}
					else
					{
						graph.setCellStyles(
								mxConstants.STYLE_VERTICAL_LABEL_POSITION,
								labelPosition);
						graph.setCellStyles(mxConstants.STYLE_VERTICAL_ALIGN,
								alignment);
					}
				}
				finally
				{
					graph.getModel().endUpdate();
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class SetStyleAction extends AbstractAction
	{
		protected String value;

		public SetStyleAction(String value)
		{
			this.value = value;
		}

		public void actionPerformed(ActionEvent e)
		{
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty())
			{
				graph.setCellStyle(value);
			}
		}
	}


	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class FontStyleAction extends AbstractAction
	{
		/**
		 * 
		 */
		protected int underlined;
		mxCell cell;
		public boolean dottedUnder ;
		/**
		 * 
		 */
		public FontStyleAction(int underlined, boolean dottedUnder, mxCell cell)
		{
			this.underlined = underlined;
			this.dottedUnder = dottedUnder;
			this.cell= cell;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				Component editorComponent = null;
				final mxGraph graph = graphComponent.getGraph();
				if (graphComponent.getCellEditor() instanceof mxCellEditor)
				{
					editorComponent = ((mxCellEditor) graphComponent
							.getCellEditor()).getEditor();
				}

				if (editorComponent instanceof JEditorPane)
				{
					JEditorPane editorPane = (JEditorPane) editorComponent;
					int start = editorPane.getSelectionStart();
					int ende = editorPane.getSelectionEnd();
					String text = editorPane.getSelectedText();

					if (text == null)
					{
						text = "";
					}

					//try
				//	{
				//		HTMLEditorKit editorKit = new HTMLEditorKit();
				//		HTMLDocument document = (HTMLDocument) editorPane.getDocument();
				//		document.remove(start, (ende - start));
				//		editorKit.insertHTML(document, start, ((underlined) ? "<u>"
				//				: "<u><b>") + text + ((underlined) ? "</u>" : "</u></b>" ),
				//				0, 0, (underlined) ? HTML.Tag.U : HTML.Tag.B  );
				//	}
				//	catch (Exception ex)
				//	{
				//		ex.printStackTrace();
					//}

					editorPane.requestFocus();
					editorPane.select(start, ende);
				}
			//else
				{
					mxIGraphModel model = graphComponent.getGraph().getModel();
					model.beginUpdate();
					
					try
					{
						graphComponent.stopEditing(false);
						
						if(underlined == 1) {
							graphComponent.getGraph().toggleCellStyleFlags(
								mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_UNDERLINE );
						}

						if(underlined  == 2) {
							graphComponent.getGraph().toggleCellStyleFlags(
								mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_UNDERLINE );
							graphComponent.getGraph().toggleCellStyleFlags(
								mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD );
						}
						if(dottedUnder == true) {
							
							Object cellValue =  cell.getValue();
							System.out.println("celllength="+graph.convertValueToString(cell).length() );
							int length = graph.convertValueToString(cell).length();	
							// create a StringBuilder object with a String pass as parameter
							StringBuilder dottedSeparator= new StringBuilder("........................................." );
							dottedSeparator.setLength(2*length);
							cellValue = cellValue + System.lineSeparator() + dottedSeparator.toString() ; 
							model.setValue(cell, cellValue);

						}

					
					}
					finally
					{
						model.endUpdate();
					}
				}
			}
		}
	}
//νο νεεδ
	public static class dottedUnderlineAction extends AbstractAction{
		mxCell cell;
		public boolean dottedUnder ;
	public dottedUnderlineAction(boolean dottedUnder, mxCell cell ){
			this.dottedUnder = dottedUnder;
			this.cell= cell;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				mxIGraphModel model = graphComponent.getGraph().getModel();
				final mxGraph graph = graphComponent.getGraph();
				model.beginUpdate();
				Object cellValue =  cell.getValue();
				Object newCellValue;
				if(dottedUnder == true) {
					System.out.println("celllength="+graph.convertValueToString(cell).length() );
					int length = graph.convertValueToString(cell).length();	
					// create a StringBuilder object with a String pass as parameter
					StringBuilder dottedSeparator= new StringBuilder("........................................." );
					dottedSeparator.setLength(2*length);
					newCellValue = cellValue + System.lineSeparator() + dottedSeparator.toString() ; 
					model.setValue(cell, newCellValue);
				}
				if (dottedUnder == false){
					newCellValue = cellValue;
				}
				model.endUpdate();
			}
		}
	}

	@SuppressWarnings("serial")
	public static class KeyValueAction extends AbstractAction
	{
     	protected String key, value;

		public KeyValueAction(String key)
		{
			this(key, null);
		}

		public KeyValueAction(String key, String value)
		{
			this.key = key;
			this.value = value;
		}

		public void actionPerformed(ActionEvent e)
		{
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty())
			{
				graph.setCellStyles(key, value);
			}
		}
	}

	@SuppressWarnings("serial")
	public static class PromptValueAction extends AbstractAction
	{
		protected String key, message;

		public PromptValueAction(String key, String message)
		{
			this.key = key;
			this.message = message;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof Component)
			{
				mxGraph graph = mxGraphActions.getGraph(e);

				if (graph != null && !graph.isSelectionEmpty())
				{
					String value = (String) JOptionPane.showInputDialog(
							(Component) e.getSource(),
							mxResources.get("value"), message,
							JOptionPane.PLAIN_MESSAGE, null, null, "");

					if (value != null)
					{
						if (value.equals(mxConstants.NONE))
						{
							value = null;
						}

						graph.setCellStyles(key, value);
					}
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class AlignCellsAction extends AbstractAction
	{
		protected String align;

		public AlignCellsAction(String align)
		{
			this.align = align;
		}

		public void actionPerformed(ActionEvent e)
		{
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty())
			{
				graph.alignCells(align);
			}
		}
	}

	@SuppressWarnings("serial")
	public static class AutosizeAction extends AbstractAction
	{

		public void actionPerformed(ActionEvent e)
		{
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty())
			{
				Object[] cells = graph.getSelectionCells();
				mxIGraphModel model = graph.getModel();

				model.beginUpdate();
				try
				{
					for (int i = 0; i < cells.length; i++)
					{
						graph.updateCellSize(cells[i]);
					}
				}
				finally
				{
					model.endUpdate();
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class ColorAction extends AbstractAction
	{
		protected String name, key;

		public ColorAction(String name, String key)
		{
			this.name = name;
			this.key = key;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				mxGraph graph = graphComponent.getGraph();

				if (!graph.isSelectionEmpty())
				{
					Color newColor = JColorChooser.showDialog(graphComponent,
							name, null);

					if (newColor != null)
					{
						graph.setCellStyles(key, mxUtils.hexString(newColor));
					}
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class StyleAction extends AbstractAction
	{
	
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				mxGraph graph = graphComponent.getGraph();
				String initial = graph.getModel().getStyle(
						graph.getSelectionCell());
				String value = (String) JOptionPane.showInputDialog(
						graphComponent, mxResources.get("style"),
						mxResources.get("style"), JOptionPane.PLAIN_MESSAGE,
						null, null, initial);

				if (value != null)
				{
					graph.setCellStyle(value);
				}
			}
		}
	}
}
