package com.mxgraph.thesis.swing.editor;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.MenuContainer;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.List;

//import javax.lang.model.element.Element;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
//import javax.swing.text.Document;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;

public class BasicGraphEditor extends JPanel
{

	private static final long serialVersionUID = -6561623072112577140L;

	/**
	 * Adds required resources
	 */
	static
	{
		try
		{
			mxResources.add("com/mxgraph/thesis/swing/resources/editor");
		}
		catch (Exception e)
		{
		}
	}

	protected mxGraphComponent graphComponent;
	protected mxGraphOutline graphOutline;
	protected JTabbedPane libraryPane;
	protected mxUndoManager undoManager;
	protected String appTitle;
	protected JLabel statusBar;
	protected File currentFile;

	//the edge's source
	mxCell source; 
	//the edge's target
	mxCell target;



	//getter
	public mxCell getSource() {
		return source;
	}
	//setter
	public void setSource(mxCell c) {
		this.source = c;
		System.out.println("source=" +source);
		System.out.println("source style=" +((mxCell) c).getStyle());
	}



	public void setOwner(mxCell source, mxCell target)
	{
		mxIGraphModel model = graphComponent.getGraph().getModel();
		model.beginUpdate();
		if(!source.hasOwner()){
			source.setOwner(target);
			ConnectShapes(target);	
		}

		else{
			
			final mxGraph graph = graphComponent.getGraph();
			//get the previous owner
			Object prevOwner = source.getOwner();
			Object[] currentEdge =  graph.getEdgesBetween(source,prevOwner); 
			// get the edge between the shapes 
			Object currEdge = currentEdge[0];
			//delete the edge
			graph.removeCells(new Object[] { currEdge });
			//set the new target and connect
			source.setOwner(target);
			ConnectShapes(target);
		}
		model.endUpdate();
	}

	public void removeOwner(mxCell source) {
		final mxGraph graph = graphComponent.getGraph();
		mxIGraphModel model = graphComponent.getGraph().getModel();
		model.beginUpdate();
		if(source.hasOwner()){
		Object prevOwner = source.getOwner();
		Object[] currentEdge=  graph.getEdgesBetween(source,prevOwner); 
		System.out.println(currentEdge);
		Object currEdge = currentEdge[0];
		//delete the edge
		graph.removeCells(new Object[] { currEdge });
		//set owner at null
		source.setOwner(null);
		model.endUpdate();
		}
	}

	public void setTopOwner(mxCell source, mxCell target)
	{
		mxIGraphModel model = graphComponent.getGraph().getModel();
		
		if(source.hasTopOwner()==false){
			model.beginUpdate();
			source.setTopOwner(target);
			ConnectShapes(target);	
		}
		else{
			model.beginUpdate();
			//final mxGraph graph = graphComponent.getGraph();
			//mxIGraphModel model = graphComponent.getGraph().getModel();
			//get the previous owner
		//	Object prevOwner = source.getTopOwner();
			//Object[] currentEdge =  graph.getEdgesBetween(source,prevOwner); 
			// get the edge between the shapes 
		//	Object currEdge = currentEdge[0];
			//model.beginUpdate();
			//if(connect==2.1) {
			//	model.setStyle(currEdge, "strokewidth=1");
			//	model.setValue(currEdge, "N");
			//}
			//model.endUpdate();

			//delete the edge
		//	graph.removeCells(new Object[] { currEdge });
		    removeTopOwner(source);
			//set the new target and connect
			source.setTopOwner(target);
			ConnectShapes(target);
		}
		model.endUpdate();
	}

	public void removeTopOwner(mxCell source) {
		mxIGraphModel model = graphComponent.getGraph().getModel();
		model.beginUpdate();
		final mxGraph graph = graphComponent.getGraph();
		if(source.hasTopOwner()==true){
		Object prevOwner = source.getTopOwner();
		System.out.println("top owner="+prevOwner );
		Object[] currentEdge=  graph.getEdgesBetween(source,prevOwner); 
		System.out.println(currentEdge);
		Object currEdge = currentEdge[0];
		//delete the edge
		graph.removeCells(new Object[] { currEdge });
		//set owner at null
		source.setTopOwner(null);
		}
		model.endUpdate();
	}



	public void setLeftOwner(mxCell source, mxCell target)
	{
		if(!source.hasLeftOwner()){

			source.setLeftOwner(target);
			ConnectShapes(target);	
		}
		
		else{
			
			final mxGraph graph = graphComponent.getGraph();
			//get the previous owner
			Object prevOwner = source.getLeftOwner();
			Object[] currentEdge =  graph.getEdgesBetween(source,prevOwner); 
			// get the edge between the shapes 
			Object currEdge = currentEdge[0];
			//delete the edge
			graph.removeCells(new Object[] { currEdge });
			//set the new target and connect
			source.setLeftOwner(target);
			ConnectShapes(target);
		}
	}

	public void removeLeftOwner(mxCell source) {
		final mxGraph graph = graphComponent.getGraph();
		if(source.hasLeftOwner()){
		Object prevOwner = source.getLeftOwner();
		System.out.println("top owner="+prevOwner );
		Object[] currentEdge=  graph.getEdgesBetween(source,prevOwner); 
		System.out.println(currentEdge);
		Object currEdge = currentEdge[0];
		//delete the edge
		graph.removeCells(new Object[] { currEdge });
		//set owner at null
		source.setLeftOwner(null);
		}
		else return;
	}

	public void setRightOwner(mxCell source, mxCell target)
	{
		if(!source.hasRightOwner()){

			source.setRightOwner(target);
			ConnectShapes(target);	
		}

		else{
			
			final mxGraph graph = graphComponent.getGraph();
			//get the previous owner
			Object prevOwner = source.getRightOwner();
			Object[] currentEdge =  graph.getEdgesBetween(source,prevOwner); 
			// get the edge between the shapes 
			Object currEdge = currentEdge[0];
			//delete the edge
			graph.removeCells(new Object[] { currEdge });
			//set the new target and connect
			source.setRightOwner(target);
			ConnectShapes(target);
		}
	}

	public void removeRightOwner(mxCell source) {
		final mxGraph graph = graphComponent.getGraph();
		if(source.hasRightOwner()){
		Object prevOwner = source.getRightOwner();
		System.out.println("top owner="+prevOwner );
		Object[] currentEdge=  graph.getEdgesBetween(source,prevOwner); 
		System.out.println(currentEdge);
		Object currEdge = currentEdge[0];
		//delete the edge
		graph.removeCells(new Object[] { currEdge });
		//set owner at null
		source.setRightOwner(null);
		}
		else return;
	}

	public void setBottomOwner(mxCell source, mxCell target)
	{
		if(!source.hasBottomOwner()){

			source.setBottomOwner(target);
			ConnectShapes(target);	
		}

		else{
			
			final mxGraph graph = graphComponent.getGraph();
			//get the previous owner
			Object prevOwner = source.getBottomOwner();
			Object[] currentEdge =  graph.getEdgesBetween(source,prevOwner); 
			// get the edge between the shapes 
			Object currEdge = currentEdge[0];
			//delete the edge
			graph.removeCells(new Object[] { currEdge });
			//set the new target and connect
			source.setBottomOwner(target);
			ConnectShapes(target);
		}
	}

	public void removeBottomOwner(mxCell source) {
		final mxGraph graph = graphComponent.getGraph();
		if(source.hasBottomOwner()){
		Object prevOwner = source.getBottomOwner();
		System.out.println("top owner="+prevOwner );
		Object[] currentEdge=  graph.getEdgesBetween(source,prevOwner); 
		System.out.println(currentEdge);
		Object currEdge = currentEdge[0];
		//delete the edge
		graph.removeCells(new Object[] { currEdge });
		//set owner at null
		source.setBottomOwner(null);
		}
		else return;
	}

	public void setSuperSub(mxCell source, mxCell target)
	{
		mxIGraphModel model = graphComponent.getGraph().getModel();
		model.beginUpdate();			
			source.setSS(target);
			ConnectShapes(target);
		model.endUpdate();
	}

	public void removeSuperSub(mxCell source, mxCell target) {
		final mxGraph graph = graphComponent.getGraph();
		mxIGraphModel model = graphComponent.getGraph().getModel();
		model.beginUpdate();
		if(!source.ssList.isEmpty()){
		Object prevSS = source.getSS(target);
		Object[] currentEdge=  graph.getEdgesBetween(source,prevSS); 
		System.out.println(currentEdge);
		Object currEdge = currentEdge[0];
		//delete the edge
		graph.removeCells(new Object[] { currEdge });
		source.removeSS(target);
		model.endUpdate();
		}
	}



	double connect = 0;
	//getter
	public double getConnect() {
		return connect;
	}
	//setter
	public void setConnect(double connect) {
		this.connect = connect;
		System.out.println("connect=" +connect);
	}


	//Flag indicating whether the current graph has been modified 
	protected boolean modified = false;

	protected mxRubberband rubberband;
	protected mxKeyboardHandler keyboardHandler;

	protected mxIEventListener undoHandler = new mxIEventListener()
	{
		public void invoke(Object source, mxEventObject evt)
		{
			undoManager.undoableEditHappened((mxUndoableEdit) evt.getProperty("edit"));
		}
	};

	protected mxIEventListener changeTracker = new mxIEventListener()
	{
		public void invoke(Object source, mxEventObject evt)
		{
			setModified(true);
		}
	};

	public BasicGraphEditor(String appTitle, mxGraphComponent component)
	{
		// Stores and updates the frame title
		this.appTitle = appTitle;

		// Stores a reference to the graph and creates the command history
		graphComponent = component;
		final mxGraph graph = graphComponent.getGraph();
		undoManager = createUndoManager();


		// Add a resize listener to the graph
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
   			private double initialWidth;
   			private double initialHeight;

  		  @Override
   		 public void mousePressed(MouseEvent e) {
       		 Object[] cells = graphComponent.getGraph().getSelectionCells();
       		 if (cells != null && cells.length == 1) {
          	  mxCell cell = (mxCell) cells[0];
           	 mxGeometry geometry = cell.getGeometry();

           	 // Store the initial width and height of the cell
           	 initialWidth = geometry.getWidth();
           	 initialHeight = geometry.getHeight();
			 
       		}
    	 }


       @Override
       public void mouseReleased(MouseEvent e) {

		
        Object[] cells = graphComponent.getGraph().getSelectionCells();
          if (cells != null && cells.length == 1) {
            mxCell cell = (mxCell) cells[0];
            mxGeometry geometry = cell.getGeometry();

            // Calculate the scaling factor based on the change in mouse position
            double scaleFactor = geometry.getWidth() / initialWidth;

            // Adjust the dimensions of the geometry to resize the cell
            double newWidth = initialWidth * scaleFactor;
            double newHeight = initialHeight * scaleFactor;

            geometry.setWidth(newWidth);
            geometry.setHeight(newHeight);

            // Update the cell's geometry in the graph model
            graphComponent.getGraph().getModel().setGeometry(cell, geometry);
			graph.refresh();
         }
       }  
    });

		// Enable cell resizing
		graph.setCellsResizable(true);
		graph.setCellsEditable(false);
				
		
		//Specifies if cell sizes should be automatically updated after a label change. 
		//graph.setAutoSizeCells (true);
		//  graph.addListener(mxEvent.LABEL_CHANGED, new mxIEventListener() {
		//  	@Override
		//  	public void invoke(Object sender, mxEventObject evt) {
		//  	  mxCell cell = (mxCell) sender ;
		//  	String label = graph.convertValueToString(cell.getValue()); // get the cell's label

		//  	// measure the label size using the default font
		// 	mxGraphics2DCanvas canvas = new mxGraphics2DCanvas();
		//  	Font font = ((MenuContainer) canvas).getFont();
		//  	FontMetrics metrics = canvas.getGraphics().getFontMetrics(font);
		//  	int labelWidth = SwingUtilities.computeStringWidth(metrics, label);
		//  	int labelHeight = metrics.getHeight();

		//  	// check if the label size is bigger than the cell size
		//  	if (labelWidth > cell.getGeometry().getWidth() ) {
    	 		
		// 		//graph.setAutoSizeCells (true);

		// 		  // Update the cell size to match the label
		// 		  mxGeometry geometry = (mxGeometry) cell.getGeometry().clone();
		// 		  geometry.setWidth(cell.getGeometry().getWidth() + 20); // add some padding to the label size
		// 		 //  geometry.setHeight(labelHeight + 20);
		// 		  cell.setGeometry(geometry);
		// 		  // Refresh the graph to reflect the changes
		// 		  graph.refresh();
				  
		// 	}
		//    }
		   
		//  });  
		  
		// Do not change the scale and translation after files have been loaded
		graph.setResetViewOnRootChange(false);

		// Updates the modified flag if the graph model changes
		graph.getModel().addListener(mxEvent.CHANGE, changeTracker);

		// Adds the command history to the model and view
		graph.getModel().addListener(mxEvent.UNDO, undoHandler);
		graph.getView().addListener(mxEvent.UNDO, undoHandler);

		// Keeps the selection in sync with the command history
		mxIEventListener undoHandler = new mxIEventListener()
		{
			public void invoke(Object source, mxEventObject evt)
			{
				List<mxUndoableChange> changes = ((mxUndoableEdit) evt
						.getProperty("edit")).getChanges();
				graph.setSelectionCells(graph
						.getSelectionCellsForChanges(changes));
			}
		};

		undoManager.addListener(mxEvent.UNDO, undoHandler);
		undoManager.addListener(mxEvent.REDO, undoHandler);

		// Creates the graph outline component
		graphOutline = new mxGraphOutline(graphComponent);

		// Creates the library pane that contains the tabs with the palettes
		libraryPane = new JTabbedPane();

		// Creates the inner split pane that contains the library with the
		// palettes and the graph outline on the left side of the window
		JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT,libraryPane, graphOutline);
		inner.setDividerLocation(330);
		inner.setResizeWeight(1);
		inner.setDividerSize(6);
		inner.setBorder(null);

		// Creates the outer split pane that contains the inner split pane and
		// the graph component on the right side of the window
		JSplitPane outer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inner,graphComponent);
		//outer.setOneTouchExpandable(true);
		outer.setDividerLocation(150);
		outer.setDividerSize(4);
		outer.setBorder(null);

		// Creates the status bar
		statusBar = createStatusBar();

		// Display some useful information about repaint events
		installRepaintListener();

		// Puts everything together
		setLayout(new BorderLayout());
		add(outer, BorderLayout.CENTER);
		//add(statusBar, BorderLayout.SOUTH);
		installToolBar();

		// Installs rubberband selection and handling for some special
		// keystrokes such as F2, Control-C, -V, X, A etc.
		installHandlers();
		installListeners();
		updateTitle();
	}

	protected mxUndoManager createUndoManager()
	{
		return new mxUndoManager();
	}

	protected void installHandlers()
	{
		rubberband = new mxRubberband(graphComponent);
		keyboardHandler = new EditorKeyboardHandler(graphComponent);
	}

	protected void installToolBar()
	{
		add(new EditorToolBar(this, JToolBar.HORIZONTAL), BorderLayout.NORTH);
	}

	protected JLabel createStatusBar()
	{
		JLabel statusBar = new JLabel(mxResources.get("ready"));
		statusBar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

		return statusBar;
	}

	protected void installRepaintListener()
	{
		graphComponent.getGraph().addListener(mxEvent.REPAINT,
				new mxIEventListener()
				{
					public void invoke(Object source, mxEventObject evt)
					{
						String buffer = (graphComponent.getTripleBuffer() != null) ? ""
								: " (unbuffered)";
						mxRectangle dirty = (mxRectangle) evt.getProperty("region");

						if (dirty == null)
						{
							status("Repaint all" + buffer);
						}
						else
						{
							status("Repaint: x=" + (int) (dirty.getX()) + " y="
									+ (int) (dirty.getY()) + " w="
									+ (int) (dirty.getWidth()) + " h="
									+ (int) (dirty.getHeight()) + buffer);
						}
					}
				});
	}

	public EditorPalette insertPalette(String title)
	{
		final EditorPalette palette = new EditorPalette();
		final JScrollPane scrollPane = new JScrollPane(palette);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		libraryPane.add(title, scrollPane);

		// Updates the widths of the palettes if the container size changes
		libraryPane.addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				int w = scrollPane.getWidth()
						- scrollPane.getVerticalScrollBar().getWidth();
				palette.setPreferredWidth(w);
			}
		});
		return palette;
	}

	protected void mouseWheelMoved(MouseWheelEvent e)
	{
		if (e.getWheelRotation() < 0)
		{
			graphComponent.zoomIn();
		}
		else
		{
			graphComponent.zoomOut();
		}

		status(mxResources.get("scale") + ": "
				+ (int) (100 * graphComponent.getGraph().getView().getScale())
				+ "%");
	}

	protected void showOutlinePopupMenu(MouseEvent e)
	{
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),graphComponent);
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(mxResources.get("magnifyPage"));
		item.setSelected(graphOutline.isFitPage());
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				graphOutline.setFitPage(!graphOutline.isFitPage());
				graphOutline.repaint();
			}
		});

		JPopupMenu menu = new JPopupMenu();
		menu.add(item);
		menu.show(graphComponent, pt.x, pt.y);

		e.consume();
	}

	
	protected void showGraphPopupMenu(MouseEvent e)
	{
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), graphComponent);
   
		Object c = (mxCell) graphComponent.getCellAt(e.getX(), e.getY());
		EditorPopupMenu menu = new EditorPopupMenu(BasicGraphEditor.this, graphComponent, c);
		mxGraph graph = graphComponent.getGraph();				
				if (c != null && (!graph.getModel().isEdge(c))){
					menu.show(graphComponent, pt.x, pt.y);
				}
		e.consume();
	}

	
	protected void mouseLocationChanged(MouseEvent e)
	{
		status(e.getX() + ", " + e.getY());
	}

	/**
	 * 
	 */
	protected void installListeners()
	{
		// Installs mouse wheel listener for zooming
		MouseWheelListener wheelTracker = new MouseWheelListener()
		{
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				if (e.getSource() instanceof mxGraphOutline
						|| e.isControlDown())
				{
					BasicGraphEditor.this.mouseWheelMoved(e);
				}
			}

		};

		// Handles mouse wheel events in the outline and graph component
		graphOutline.addMouseWheelListener(wheelTracker);
		graphComponent.addMouseWheelListener(wheelTracker);

		// Installs the popup menu in the outline
		graphOutline.addMouseListener(new MouseAdapter()
		{

			public void mousePressed(MouseEvent e)
			{
				// Handles context menu on the Mac where the trigger is on mousepressed
				mouseReleased(e);
			}

			public void mouseReleased(MouseEvent e)
			{
			

				if (e.isPopupTrigger())
				{
					showOutlinePopupMenu(e);
				}
				
			}
			

		});

		// Installs the popup menu in the graph component
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter()
		{

			public void mousePressed(MouseEvent e)
			{
				// Handles context menu on the Mac where the trigger is on mousepressed
				mouseReleased(e);
			}

		
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showGraphPopupMenu(e);
				}
			}

		});



		//handles left click on a shape
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) { 
				if(e.getButton() == MouseEvent.BUTTON1) {
					mxCell c = (mxCell) graphComponent.getCellAt(e.getX(), e.getY());
					if (c!= null&& c!=source){
						if ( connect==1 && 
							(((mxCell) c).getStyle().contains("rectangle")
							||((mxCell) c).getStyle().contains("doubleRect") 
							||((mxCell) c).getStyle().contains("rhombus")
							||((mxCell) c).getStyle().contains("doubleRh")
							||((!source.getStyle().contains("doubleEl")||!source.getStyle().contains("un_dl"))
							&&(((mxCell) c).getStyle().contains("ellipse")||((mxCell) c).getStyle().contains("ellipse;fontSize")||((mxCell) c).getStyle().contains("primary")||((mxCell) c).getStyle().contains("doubleEl")
							||((mxCell) c).getStyle().contains("un_dl")||((mxCell) c).getStyle().contains ("unique"))))) {
							target = c;
							setOwner(source, target);
							//ConnectShapes(target);
						}
						
						else if ( connect==1.3 && ((mxCell) c).getStyle().contains("rectangle")) {
							target = c;
							setOwner(source, target);
						}

						else if( connect==1.1 && 
						(((mxCell) c).getStyle().contains("rectangle")
						||((mxCell) c).getStyle().contains("doubleRect"))) {
							target = c;
							setOwner(source, target);
						}

						else if( connect==1.2 && 
						(((mxCell) c).getStyle().contains("rectangle")
						||((mxCell) c).getStyle().contains("doubleRect"))) {
							target = c;
							setOwner(source, target);
						}

						else if( (connect==2||connect==2.1||connect==2.2||connect==2.3||connect==2.4 )&& (((mxCell) c).getStyle().contains("rectangle")
						||((mxCell) c).getStyle().contains("doubleRect"))) {

							target = c;
							setTopOwner(source, target);
							//ConnectShapes(target);
				   	    }

						else if( (connect==3||connect==3.1||connect==3.2||connect==3.3||connect==3.4) && (((mxCell) c).getStyle().contains("rectangle")
						   ||((mxCell) c).getStyle().contains("doubleRect"))) {
   
							   target = c;
							   setLeftOwner(source, target);
							   //ConnectShapes(target);
						}

						
						else if( (connect==4||connect==4.1||connect==4.2||connect==4.3||connect==4.4) && (((mxCell) c).getStyle().contains("rectangle")
						   ||((mxCell) c).getStyle().contains("doubleRect"))) {
   
							   target = c;
							   setRightOwner(source, target);
							   //ConnectShapes(target);
						}

						
						else if( (connect==5||connect==5.1||connect==5.2||connect==5.3||connect==5.4) && (((mxCell) c).getStyle().contains("rectangle")
							||((mxCell) c).getStyle().contains("doubleRect"))) {
   
							   target = c;
							   setBottomOwner(source, target);
							   //ConnectShapes(target);
						}
						else if( (connect==6 ||connect==6.1||connect==6.2 ||connect==6.3 ||connect==6.4) && (((mxCell) c).getStyle().contains("rectangle")
							||((mxCell) c).getStyle().contains("doubleRect"))) {

							target = c;
							setTopOwner(source, target);
							//ConnectShapes(target);
				   	    }
						else if( (connect==7 ||connect==7.1||connect==7.2 ||connect==7.3 ||connect==7.4) && (((mxCell) c).getStyle().contains("rectangle")
						   ||((mxCell) c).getStyle().contains("doubleRect"))) {
   
							   target = c;
							   setLeftOwner(source, target);
							   //ConnectShapes(target);
						}
						else if( (connect==8 ||connect==8.1||connect==8.2 ||connect==8.3 ||connect==8.4) && (((mxCell) c).getStyle().contains("rectangle")
							||((mxCell) c).getStyle().contains("doubleRect"))) {

							target = c;
							setRightOwner(source, target);
							//ConnectShapes(target);
				   	    }
						else if( (connect==9 ||connect==9.1||connect==9.2 ||connect==9.3 ||connect==9.4) && (((mxCell) c).getStyle().contains("rectangle")
						   ||((mxCell) c).getStyle().contains("doubleRect"))) {
   
							   target = c;
							   setBottomOwner(source, target);
							   //ConnectShapes(target);
						}

						else if ((connect==10||connect==10.2 )&&(((mxCell) c).getStyle().contains("rectangle") ||((mxCell) c).getStyle().contains("doubleRect"))) {
							  target = c;
							  setSuperSub(source, target);
						}
						else if ((connect==11||connect==11.1 )&&(((mxCell) c).getStyle().contains("rectangle") ||((mxCell) c).getStyle().contains("doubleRect"))) {
							target = c;
							setOwner(source, target);
					  }
						else if(connect==10.1 &&(((mxCell) c).getStyle().contains("rectangle")||((mxCell) c).getStyle().contains("doubleRect"))) {
							target = c;
							removeSuperSub(source,target);
						}
						else if(connect==10.4 &&(((mxCell) c).getStyle().contains("rectangle")||((mxCell) c).getStyle().contains("doubleRect"))) {
							target = c;
							final mxGraph graph = graphComponent.getGraph();
							Object[] currentEdge =  graph.getEdgesBetween(source,target); 
               				 // get the edge between the shapes 
             			     Object currEdge = currentEdge[0];
							  mxIGraphModel model = graphComponent.getGraph().getModel();
							  model.beginUpdate();
						      String input = JOptionPane.showInputDialog("Defining Criteria:  ");
							  if(input!=null) {	
								mxCell cEdge = (mxCell) currEdge;
								cEdge.setValue(input);
								graph.refresh();
							}
							model.endUpdate();
						}

						else return;
				  }
				} 
				else return;
			}
		});

		
		// Installs a mouse motion listener to display the mouse location
		graphComponent.getGraphControl().addMouseMotionListener(
				new MouseMotionListener()
				{

					public void mouseDragged(MouseEvent e)
					{
						mouseLocationChanged(e);
					}

					public void mouseMoved(MouseEvent e)
					{
						mouseDragged(e);
					}

				});
	}


	public void ConnectShapes(mxCell target) {
			
		final mxGraph graph = graphComponent.getGraph();
		Object parent = graph.getDefaultParent();
		if(connect==1 ||connect==1.1) {
			graph.insertEdge(parent, null, "", source, target);
		}

		//subclass total
		if(connect==1.2) {
			graph.insertEdge(parent, null, "", source, target, "strokeWidth=5;");
		}

		//superclass partial
		if(connect==1.3) {
			graph.insertEdge(parent, null, "", source, target, "startArrow=open");
		}
		

		//relationship top
		else if(connect==2) {
			graph.insertEdge(parent, null, "1", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=0;exitPerimeter=1");
		}

		//relationship top
		else if(connect==2.1) {
			graph.insertEdge(parent, null, "N", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=0;exitPerimeter=1");
		}

		//relationship top
		else if(connect==2.2) {
			graph.insertEdge(parent, null, "M", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=0;exitPerimeter=1");
		}

		//relationship top
		else if(connect==2.3) {
			graph.insertEdge(parent, null, "K", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=0;exitPerimeter=1");
		}

		//relationship top
		else if(connect==2.4) {
			graph.insertEdge(parent, null, "L", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=0;exitPerimeter=1");
		}



		//relationship left
		else if(connect==3) {
			graph.insertEdge(parent, null, "1", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0;exitY=0.5;exitPerimeter=1");
		}
		//relationship left
		else if(connect==3.1) {
			graph.insertEdge(parent, null, "N", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0;exitY=0.5;exitPerimeter=1");
		}
		//relationship left
		else if(connect==3.2) {
			graph.insertEdge(parent, null, "M", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0;exitY=0.5;exitPerimeter=1");
		}
		//relationship left
		else if(connect==3.3) {
			graph.insertEdge(parent, null, "K", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0;exitY=0.5;exitPerimeter=1");
		}
		//relationship left
		else if(connect==3.4) {
			graph.insertEdge(parent, null, "L", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0;exitY=0.5;exitPerimeter=1");
		}


		//relationship right 
		else if(connect==4) {
			graph.insertEdge(parent, null, "1", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=1;exitY=0.5;exitPerimeter=1" );
		}
		//relationship right 
		else if(connect==4.1) {
			graph.insertEdge(parent, null, "N", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=1;exitY=0.5;exitPerimeter=1" );
		}
		//relationship right 
		else if(connect==4.2) {
			graph.insertEdge(parent, null, "M", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=1;exitY=0.5;exitPerimeter=1" );
		}
		//relationship right 
		else if(connect==4.3) {
			graph.insertEdge(parent, null, "K", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=1;exitY=0.5;exitPerimeter=1" );
		}
		//relationship right 
		else if(connect==4.4) {
			graph.insertEdge(parent, null, "L", source, target,"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=1;exitY=0.5;exitPerimeter=1" );
		}


		//relationship bottom 
		else if(connect==5) {
			graph.insertEdge(parent, null, "1", source, target, "spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=1;exitPerimeter=1");
		}
		//relationship bottom 
		else if(connect==5.1) {
			graph.insertEdge(parent, null, "N", source, target, "spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=1;exitPerimeter=1");
		}
		//relationship bottom 
		else if(connect==5.2) {
			graph.insertEdge(parent, null, "M", source, target, "spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=1;exitPerimeter=1");
		}
		//relationship bottom 
		else if(connect==5.3) {
			graph.insertEdge(parent, null, "K", source, target, "spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=1;exitPerimeter=1");
		}
		//relationship bottom 
		else if(connect==5.4) {
			graph.insertEdge(parent, null, "L", source, target, "spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=1;exitPerimeter=1");
		}

		//relationship top total
		else if(connect==6) {
			graph.insertEdge(parent, null, "1", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"
			+ "exitX=0.5;exitY=0;exitPerimeter=1");
		}

       //relationship top total
		else if(connect==6.1) {
			graph.insertEdge(parent, null, "N", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=0;exitPerimeter=1");
		}

		//relationship top total
		else if(connect==6.2) {
			graph.insertEdge(parent, null, "M", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=0;exitPerimeter=1");
		}

		//relationship top total
		else if(connect==6.3) {
			graph.insertEdge(parent, null, "K", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=0;exitPerimeter=1");
		}

		//relationship top total
		else if(connect==6.4) {
			graph.insertEdge(parent, null, "L", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=0;exitPerimeter=1");
		}


		//relationship left total
		else if(connect==7) {
			graph.insertEdge(parent, null, "1", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0;exitY=0.5;exitPerimeter=1");
		}
		//relationship left total
		else if(connect==7.1) {
			graph.insertEdge(parent, null, "N", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0;exitY=0.5;exitPerimeter=1");
		}
		//relationship left total
		else if(connect==7.2) {
			graph.insertEdge(parent, null, "M", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0;exitY=0.5;exitPerimeter=1");
		}
		//relationship left total
		else if(connect==7.3) {
			graph.insertEdge(parent, null, "K", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0;exitY=0.5;exitPerimeter=1");
		}
		//relationship left total
		else if(connect==7.4) {
			graph.insertEdge(parent, null, "L", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0;exitY=0.5;exitPerimeter=1");
		}

		//relationship right total
		else if(connect==8) {
			graph.insertEdge(parent, null, "1", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=1;exitY=0.5;exitPerimeter=1" );	
		}
		//relationship right total
		else if(connect==8.1) {
			graph.insertEdge(parent, null, "N", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=1;exitY=0.5;exitPerimeter=1" );	
		}
		//relationship right total
		else if(connect==8.2) {
			graph.insertEdge(parent, null, "M", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=1;exitY=0.5;exitPerimeter=1" );	
		}
		//relationship right total
		else if(connect==8.3) {
			graph.insertEdge(parent, null, "K", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=1;exitY=0.5;exitPerimeter=1" );	
		}
		//relationship right total
		else if(connect==8.4) {
			graph.insertEdge(parent, null, "L", source, target,"strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=1;exitY=0.5;exitPerimeter=1" );	
		}

		//relationship bottom total
		else if(connect==9) {
			graph.insertEdge(parent, null, "1", source, target,"strokeWidth=5;"+ "spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=1;exitPerimeter=1");	
		}
		else if(connect==9.1) {
			graph.insertEdge(parent, null, "N", source, target,"strokeWidth=5;"+ "spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=1;exitPerimeter=1");	
		}
		else if(connect==9.2) {
			graph.insertEdge(parent, null, "M", source, target,"strokeWidth=5;"+ "spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=1;exitPerimeter=1");	
		}
		else if(connect==9.3) {
			graph.insertEdge(parent, null, "K", source, target,"strokeWidth=5;"+ "spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=1;exitPerimeter=1");	
		}
		else if(connect==9.4) {
			graph.insertEdge(parent, null, "L", source, target,"strokeWidth=5;"+ "spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=1;exitPerimeter=1");	
		}
		else if(connect==10 ) {
			graph.insertEdge(parent, null, "", source, target);
		}
		else if(connect==10.2 || connect==11) {
			graph.insertEdge(parent, null, "", source, target,"endArrow=open");
		}
		else if( connect == 11.1) {
			graph.insertEdge(parent, null, "", source, target,"strokeWidth=5;"+"endArrow=open");
		}


		//setOwner(source, target);
		setConnect(0);
		System.out.println("Left click connection!");
    }


	
	public void setCurrentFile(File file)
	{
		File oldValue = currentFile;
		currentFile = file;

		firePropertyChange("currentFile", oldValue, file);

		if (oldValue != file)
		{
			updateTitle();
		}
	}

	public File getCurrentFile()
	{
		return currentFile;
	}

	public void setModified(boolean modified)
	{
		boolean oldValue = this.modified;
		this.modified = modified;

		firePropertyChange("modified", oldValue, modified);

		if (oldValue != modified)
		{
			updateTitle();
		}
	}

	/**
	 * 
	 * @return whether or not the current graph has been modified
	 */
	public boolean isModified()
	{
		return modified;
	}

	public mxGraphComponent getGraphComponent()
	{
		return graphComponent;
	}

	public mxGraphOutline getGraphOutline()
	{
		return graphOutline;
	}
	
	public JTabbedPane getLibraryPane()
	{
		return libraryPane;
	}

	public mxUndoManager getUndoManager()
	{
		return undoManager;
	}

	/**
	 * 
	 * @param name
	 * @param action
	 * @return a new Action bound to the specified string name
	 */
	public Action bind(String name, final Action action)
	{
		return bind(name, action, null);
	}

	/**
	 * 
	 * @param name
	 * @param action
	 * @return a new Action bound to the specified string name and icon
	 */
	@SuppressWarnings("serial")
	public Action bind(String name, final Action action, String iconUrl)
	{
		AbstractAction newAction = new AbstractAction(name, (iconUrl != null) ? new ImageIcon(
				BasicGraphEditor.class.getResource(iconUrl)) : null)
		{
			public void actionPerformed(ActionEvent e)
			{
				action.actionPerformed(new ActionEvent(getGraphComponent(), e
						.getID(), e.getActionCommand()));
			}
		};
		
		newAction.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.SHORT_DESCRIPTION));
		
		return newAction;
	}

	
	public void status(String msg)
	{
		statusBar.setText(msg);
	}

	public void updateTitle()
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null)
		{
			String title = (currentFile != null) ? currentFile
					.getAbsolutePath() : mxResources.get("newDiagram");

			if (modified)
			{
				title += "*";
			}

			frame.setTitle(title + " - " + appTitle);
		}
	}

	public void about()
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null)
		{
			EditorAboutFrame about = new EditorAboutFrame(frame);
			about.setModal(true);

			// Centers inside the application frame
			int x = frame.getX() + (frame.getWidth() - about.getWidth()) / 2;
			int y = frame.getY() + (frame.getHeight() - about.getHeight()) / 2;
			about.setLocation(x, y);

			// Shows the modal dialog and waits
			about.setVisible(true);
		}
	}

	public void exit()
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null)
		{
			frame.dispose();
		}
	}

	public void setLookAndFeel(String clazz)
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null)
		{
			try
			{
				UIManager.setLookAndFeel(clazz);
				SwingUtilities.updateComponentTreeUI(frame);

				// Needs to assign the key bindings again
				keyboardHandler = new EditorKeyboardHandler(graphComponent);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
	}

	

	public JFrame createFrame(JMenuBar menuBar)
	{
		JFrame frame = new JFrame();
		frame.getContentPane().add(this);
		//ask before closing the window
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we)
			{ 
				String ObjButtons[] = {"Yes","No"};
				int PromptResult = JOptionPane.showOptionDialog(null,"Are you sure you want to exit?","Exit",
																JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,ObjButtons,ObjButtons[1]);
				if(PromptResult==JOptionPane.YES_OPTION)
				{
					System.exit(0);
				}
			}
		});
		frame.setJMenuBar(menuBar);
		frame.setSize(870, 640);

		// Updates the frame title
		updateTitle();

		return frame;
	}
}
