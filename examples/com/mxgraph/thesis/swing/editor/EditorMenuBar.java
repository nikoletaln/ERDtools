package com.mxgraph.thesis.swing.editor;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import com.mxgraph.analysis.StructuralException;
import com.mxgraph.analysis.mxGraphProperties.GraphType;
import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.analysis.mxGraphProperties;
import com.mxgraph.analysis.mxGraphStructure;
import com.mxgraph.analysis.mxTraversal;
import com.mxgraph.costfunction.mxCostFunction;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.thesis.swing.editor.EditorActions.ColorAction;
import com.mxgraph.thesis.swing.editor.EditorActions.ExitAction;
import com.mxgraph.thesis.swing.editor.EditorActions.ExportAction;
import com.mxgraph.thesis.swing.editor.EditorActions.HistoryAction;
import com.mxgraph.thesis.swing.editor.EditorActions.KeyValueAction;
import com.mxgraph.thesis.swing.editor.EditorActions.NewAction;
import com.mxgraph.thesis.swing.editor.EditorActions.OpenAction;
import com.mxgraph.thesis.swing.editor.EditorActions.PageSetupAction;
import com.mxgraph.thesis.swing.editor.EditorActions.PrintAction;
import com.mxgraph.thesis.swing.editor.EditorActions.PromptPropertyAction;
import com.mxgraph.thesis.swing.editor.EditorActions.PromptValueAction;
import com.mxgraph.thesis.swing.editor.EditorActions.SaveAction;
import com.mxgraph.thesis.swing.editor.EditorActions.ScaleAction;
import com.mxgraph.thesis.swing.editor.EditorActions.SetLabelPositionAction;
import com.mxgraph.thesis.swing.editor.EditorActions.SetStyleAction;
import com.mxgraph.thesis.swing.editor.EditorActions.StylesheetAction;
import com.mxgraph.thesis.swing.editor.EditorActions.ToggleAction;
import com.mxgraph.thesis.swing.editor.EditorActions.ToggleGridItem;
import com.mxgraph.thesis.swing.editor.EditorActions.ToggleOutlineItem;
import com.mxgraph.thesis.swing.editor.EditorActions.TogglePropertyItem;
import com.mxgraph.thesis.swing.editor.EditorActions.ToggleRulersItem;
import com.mxgraph.thesis.swing.editor.EditorActions.ZoomPolicyAction;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;


public class EditorMenuBar extends JMenuBar
{
	private static final long serialVersionUID = 4060203894740766714L;

	public enum AnalyzeType
	{
		IS_CONNECTED, IS_SIMPLE, COMPLEMENTARY, REGULARITY, COMPONENTS, MAKE_CONNECTED, MAKE_SIMPLE,  GET_CUT_VERTEXES, GET_CUT_EDGES, GET_SOURCES, GET_SINKS, PLANARITY, IS_BICONNECTED, GET_BICONNECTED}

	public EditorMenuBar(final BasicGraphEditor editor)
	{
		final mxGraphComponent graphComponent = editor.getGraphComponent();
		final mxGraph graph = graphComponent.getGraph();
		mxAnalysisGraph aGraph = new mxAnalysisGraph();

		JMenu menu = null;
		JMenu submenu = null;

		// Creates the file menu
		menu = add(new JMenu(mxResources.get("file")));
		menu.add(editor.bind(mxResources.get("new"), new NewAction(), "/com/mxgraph/thesis/swing/images/new.gif"));
		menu.add(editor.bind(mxResources.get("openFile"), new OpenAction(), "/com/mxgraph/thesis/swing/images/open.gif"));
		menu.addSeparator();
		menu.add(editor.bind(mxResources.get("save"), new SaveAction(false), "/com/mxgraph/thesis/swing/images/save.gif"));
		menu.add(editor.bind(mxResources.get("saveAs"), new SaveAction(true), "/com/mxgraph/thesis/swing/images/saveas.gif"));
		menu.addSeparator();
		menu.add(editor.bind("Export", new ExportAction(true), "/com/mxgraph/thesis/swing/images/saveas.gif"));
		menu.addSeparator();
		menu.add(editor.bind(mxResources.get("pageSetup"), new PageSetupAction(), "/com/mxgraph/thesis/swing/images/pagesetup.gif"));
		menu.add(editor.bind(mxResources.get("print"), new PrintAction(), "/com/mxgraph/thesis/swing/images/print.gif"));
		menu.addSeparator();
		menu.add(editor.bind(mxResources.get("exit"), new ExitAction()));

		// Creates the edit menu
		menu = add(new JMenu(mxResources.get("edit")));
		menu.add(editor.bind(mxResources.get("undo"), new HistoryAction(true), "/com/mxgraph/thesis/swing/images/undo.gif"));
		menu.add(editor.bind(mxResources.get("redo"), new HistoryAction(false), "/com/mxgraph/thesis/swing/images/redo.gif"));
			
		// Creates the view menu
		menu = add(new JMenu(mxResources.get("view")));

		JMenuItem item = menu.add(new TogglePropertyItem(graphComponent, mxResources.get("pageLayout"), "PageVisible", true,
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						if (graphComponent.isPageVisible() && graphComponent.isCenterPage())
						{
							graphComponent.zoomAndCenter();
						}
						else
						{
							graphComponent.getGraphControl().updatePreferredSize();
						}
					}
				}));

		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (e.getSource() instanceof TogglePropertyItem)
				{
					final mxGraphComponent graphComponent = editor.getGraphComponent();
					TogglePropertyItem toggleItem = (TogglePropertyItem) e.getSource();

					if (toggleItem.isSelected())
					{
						// Scrolls the view to the center
						SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
							{
								graphComponent.scrollToCenter(true);
								graphComponent.scrollToCenter(false);
							}
						});
					}
					else
					{
						// Resets the translation of the view
						mxPoint tr = graphComponent.getGraph().getView().getTranslate();

						if (tr.getX() != 0 || tr.getY() != 0)
						{
							graphComponent.getGraph().getView().setTranslate(new mxPoint());
						}
					}
				}
			}
		});

		menu.add(new ToggleGridItem(editor, mxResources.get("grid")));
	
		menu.addSeparator();

		menu.add(new ToggleOutlineItem(editor, mxResources.get("outline")));

		menu.addSeparator();
		
		submenu = (JMenu) menu.add(new JMenu("Colors"));
	 
		JRadioButton  jRadioButton1 = new JRadioButton( editor.bind(mxResources.get("defaultStyle"),
		new StylesheetAction("/com/mxgraph/thesis/swing/resources/default-style.xml"))); 
		jRadioButton1.setText(mxResources.get("defaultStyle"));
		jRadioButton1.setSelected(true);

		JRadioButton  jRadioButton2 = new JRadioButton(editor.bind(mxResources.get("colorsStyle"),
		new StylesheetAction("/com/mxgraph/thesis/swing/resources/colors-style.xml")));
		
		JRadioButton  jRadioButton3 = new JRadioButton(editor.bind(mxResources.get("detailedStyle"),
		new StylesheetAction("/com/mxgraph/thesis/swing/resources/detailed-style.xml")));

		//Group the radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(jRadioButton1);
		group.add(jRadioButton2);
		group.add(jRadioButton3);

		//add the buttons to the colors submenu
		submenu.add(jRadioButton1);
		submenu.add(jRadioButton2);
		submenu.add(jRadioButton3);

		// Creates the options menu
		menu = add(new JMenu("Options"));
		item = menu.add(new JMenuItem("Check for errors"));
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new CheckErrors(graphComponent,graph);

			}
		});
		item = menu.add(new JMenuItem("Convert to Relational schema"));
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Convertor(graphComponent,graph,false);

			}
		});
	  	
		item = menu.add(new JMenuItem("Convert to SQL"));
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Convertor(graphComponent,graph,true);

			}
		});


		// Creates the help menu
		menu = add(new JMenu(mxResources.get("help")));

		item = menu.add(new JMenuItem("About ERDT"));
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				editor.about();
			}
		});
	}
	/**
	 * Adds menu items to the given format menu. This is factored out because
	 * the format menu appears in the menubar and also in the popupmenu.
	 */
	public static void populateFormatMenu(JMenu menu, BasicGraphEditor editor)
	{
		JMenu submenu = (JMenu) menu.add(new JMenu(mxResources.get("label")));

		submenu.add(editor.bind(mxResources.get("bottom"), new SetLabelPositionAction(mxConstants.ALIGN_BOTTOM, mxConstants.ALIGN_TOP)));
		 
		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("wordWrap"), new KeyValueAction(mxConstants.STYLE_WHITE_SPACE, "wrap")));

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("line")));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("orthogonal"), new ToggleAction(mxConstants.STYLE_ORTHOGONAL)));
		submenu.add(editor.bind(mxResources.get("dashed"), new ToggleAction(mxConstants.STYLE_DASHED)));

	}



	public static class AnalyzeGraph extends AbstractAction
	{
	
		private static final long serialVersionUID = 6926170745240507985L;

		mxAnalysisGraph aGraph;

		protected AnalyzeType analyzeType;

		//Examples for calling analysis methods from mxGraphStructure 
		public AnalyzeGraph(AnalyzeType analyzeType, mxAnalysisGraph aGraph)
		{
			this.analyzeType = analyzeType;
			this.aGraph = aGraph;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				mxGraph graph = graphComponent.getGraph();
				aGraph.setGraph(graph);

					boolean isConnected = mxGraphStructure.isConnected(aGraph);

					if (isConnected)
					{
						System.out.println("The graph is connected");
					}
					else
					{
						System.out.println("The graph is not connected");
					}
			//	}
				
			}
		}
	};
};