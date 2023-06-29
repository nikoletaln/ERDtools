/**
 * Copyright (c) 2006-2012, JGraph Ltd */
package com.mxgraph.thesis.swing;

import java.awt.Color;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import org.w3c.dom.Document;
import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.shape.mxConnectorShape;
import com.mxgraph.shape.mxDoubleEllipseShape;
import com.mxgraph.shape.mxDoubleRectangleShape;
import com.mxgraph.shape.mxEllipseShape;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.thesis.swing.editor.BasicGraphEditor;
import com.mxgraph.thesis.swing.editor.EditorMenuBar;
import com.mxgraph.thesis.swing.editor.EditorPalette;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.canvas.mxGraphics2DCanvas;

public class GraphEditor extends BasicGraphEditor
{
	
	private static final long serialVersionUID = -4601740824088314699L;

	/**
	 * Holds the shared number formatter.
	 * 
	 * @see NumberFormat#getInstance()
	 */
	public static final NumberFormat numberFormat = NumberFormat.getInstance();

	/**
	 * Holds the URL for the icon to be used as a handle for creating new
	 * connections. This is currently unused.
	 */
	public static URL url = null;

	//GraphEditor.class.getResource("/com/mxgraph/thesis/swing/images/connector.gif");

	public GraphEditor()
	{
		this("ERD Tools", new CustomGraphComponent(new CustomGraph()));
	}
   
	public GraphEditor(String appTitle, mxGraphComponent component)
	{
		super(appTitle, component);
		//final mxGraph graph = graphComponent.getGraph();

		// Creates the shapes palette
		EditorPalette shapesPalette = insertPalette(mxResources.get("shapes"));
		
		final String SHAPE_DOUBLE_RHOMBUS = "doubleRh";
		// Registers the shape in the canvas shape registry
		mxGraphics2DCanvas.putShape(SHAPE_DOUBLE_RHOMBUS, new DoubleRhombusShape());

		final String SHAPE_DOUBLE_RECTANGLE = "doubleRect";
		// Registers the shape in the canvas shape registry
		mxGraphics2DCanvas.putShape(SHAPE_DOUBLE_RECTANGLE, new mxDoubleRectangleShape());
		final String D_CIRCLE = "d_circle";
		//Registers the shape in the canvas shape registry
		mxGraphics2DCanvas.putShape(D_CIRCLE, new mxEllipseShape());
		final String O_CIRCLE = "o_circle";
		//Registers the shape in the canvas shape registry
		mxGraphics2DCanvas.putShape(O_CIRCLE, new mxEllipseShape());
		final String U_CIRCLE = "U_circle";
		//Registers the shape in the canvas shape registry
		mxGraphics2DCanvas.putShape(U_CIRCLE, new mxEllipseShape());
		final String PARTIAL = "partial";
		//Registers the shape in the canvas shape registry
		mxGraphics2DCanvas.putShape(PARTIAL, new mxEllipseShape());
		final String PRIMARY = "primary";
		//Registers the shape in the canvas shape registry
		mxGraphics2DCanvas.putShape(PRIMARY, new mxEllipseShape());
		//Registers the shape in the canvas shape registry
		final String UNIQUE = "unique";
		mxGraphics2DCanvas.putShape(UNIQUE, new mxEllipseShape());

		final String DASHED = "dashed";
		mxGraphics2DCanvas.putShape(DASHED, new mxEllipseShape());
		//Registers the shape in the canvas shape registry
		final String UN_DL = "un_dl";
		mxGraphics2DCanvas.putShape(UN_DL, new mxDoubleEllipseShape());

		final String SHAPE_ARCLINE = "arcline";
		// Registers the shape in the canvas shape registry
		mxGraphics2DCanvas.putShape(SHAPE_ARCLINE, new ArcLineShape());
			
		
		// Adds some template cells for dropping into the graph
		shapesPalette
				.addTemplate(
					"Attribute",
					new ImageIcon(
							GraphEditor.class 
									.getResource("/com/mxgraph/thesis/swing/images/1.png")),
									"ellipse", 160, 60, "Attribute");

		shapesPalette
				.addTemplate(
					"Primary Key Attribute",
					new ImageIcon(
							GraphEditor.class 
									.getResource("/com/mxgraph/thesis/swing/images/1.png")),
									"primary", 160, 60, "PrimaryKey");

		shapesPalette
				.addTemplate(
					"Unique Key Attribute",
					new ImageIcon(
							GraphEditor.class 
									.getResource("/com/mxgraph/thesis/swing/images/1.png")),
									"unique", 160, 60, "UniqueKey");
		shapesPalette
			.addTemplate(
					"Partial Key Attribute",
					new ImageIcon(
							GraphEditor.class 
								.getResource("/com/mxgraph/thesis/swing/images/ellipse2.png")),
									"partial", 160, 60, "PartialKey");	
															
						
		shapesPalette
				.addTemplate(
						"Multivalued Attribute",
						new ImageIcon(
								GraphEditor.class
										.getResource("/com/mxgraph/thesis/swing/images/2.png")),
										"doubleEl", 160, 60, "Multivalued");
		shapesPalette
				.addTemplate(
					"Derived Attribute",
					new ImageIcon(
							GraphEditor.class
									.getResource("/com/mxgraph/thesis/swing/images/3.png")),
									"dashed", 160, 60, "Derived");
		shapesPalette
				.addTemplate(
					"Entity",
					new ImageIcon(
							GraphEditor.class
							.getResource("/com/mxgraph/thesis/swing/images/rectangle.png")),
							"rectangle", 120, 60, "Entity");
		shapesPalette
				.addTemplate(
					"Weak Entity",
					new ImageIcon(
							GraphEditor.class
									.getResource("/com/mxgraph/thesis/swing/images/doublerectangle.png")),
									"doubleRect", 120, 60, "Weak");
		shapesPalette
				.addTemplate(
					"Relationship",
					new ImageIcon(
							GraphEditor.class
							.getResource("/com/mxgraph/thesis/swing/images/rhombus.png")),
							"rhombus", 180, 106, "Relationship");
		shapesPalette
				.addTemplate(
					"Identifying Relationship",
					new ImageIcon(
							GraphEditor.class
							.getResource("/com/mxgraph/thesis/swing/images/doubleRhombus.png")),
							"doubleRh", 180, 106, "Identifying");
		
		shapesPalette
				.addTemplate(
					"Disjoint",
					new ImageIcon(
							GraphEditor.class
									.getResource("/com/mxgraph/thesis/swing/images/8.png")),
									"d_circle", 40, 40, "d");	

		shapesPalette
				.addTemplate(
					"Overlapping",
					new ImageIcon(
							GraphEditor.class
									.getResource("/com/mxgraph/thesis/swing/images/9.png")),
									"o_circle", 40, 40, "o");	

		shapesPalette
				.addTemplate(
					"Union",
					new ImageIcon(
							GraphEditor.class
									.getResource("/com/mxgraph/thesis/swing/images/10.png")),
									"U_circle", 40, 40, "U");				
									
	}

	
	public static class CustomGraphComponent extends mxGraphComponent
	{

		public CustomGraphComponent(mxGraph graph)
		{
			super(graph);
			// Sets switches typically used in an editor
			setPageVisible(true);
			setGridVisible(true);
			setToolTips(true);
			//edges aren't allowed to not have a source and/or target terminal defined.
			graph.setAllowDanglingEdges(false);
			//graph.setCellsEditable(false);
			//don't create a new vertex if no target was under the mouse for the new connection
			getConnectionHandler().setCreateTarget(false);
			//cannot resize cells
			//graph.setCellsResizable(false);
			//don't connect on drop
			setConnectable(false);
		
			// Loads the defalt stylesheet from an external file
			mxCodec codec = new mxCodec();
			Document doc = mxUtils.loadDocument(GraphEditor.class.getResource(
				"/com/mxgraph/thesis/swing/resources/default-style.xml").toString());
			codec.decode(doc.getDocumentElement(), graph.getStylesheet());

			// Sets the background to white
			getViewport().setOpaque(true);
			getViewport().setBackground(Color.WHITE);
		}

		
		/**
		 * Overrides drop behaviour to set the cell style if the target
		 * is not a valid drop target and the cells are of the same
		 * type (eg. both vertices or both edges). 
		 */
	//	public Object[] importCells(Object[] cells, double dx, double dy,
	//			Object target, Point location)
	//	{		
	//		if (target == null && cells.length == 1 && location != null)
		//	{
		//		target = getCellAt(location.x, location.y);

		//		if (target instanceof mxICell && cells[0] instanceof mxICell)
		//		{
		//			mxICell targetCell = (mxICell) target;
		///			mxICell dropCell = (mxICell) cells[0];

		//			if (targetCell.isVertex() == dropCell.isVertex()
			//				|| targetCell.isEdge() == dropCell.isEdge())
			//		{
			//			mxIGraphModel model = graph.getModel();
			//			model.setStyle(target, model.getStyle(cells[0]));
			//			graph.setSelectionCell(target);
//
			//			return null;
			//		}
				//	  if (targetCell.isVertex() == dropCell.isVertex()) {
                    // make target null, otherwise we create a group
                  //  cells = super.importCells(cells, dx, dy, null, location);

                //    Object parent = graph.getModel().getParent(target);
                    // we cloned it, so update the reference
                //    dropCell = (mxICell) cells[0];
                //    graph.insertEdge(parent, null, "", target, dropCell);
//
                 //   graph.setSelectionCell(dropCell);

                //    return null;
               // }

			//	}
		//	}

		//	return super.importCells(cells, dx, dy, target, location);
	//	}

	}

	

	/**
	 * A graph that creates new edges from a given template edge.
	 */
	public static class CustomGraph extends mxGraph
	{
		/**
		 * Holds the edge to be used as a template for inserting new edges.
		 */
		protected Object edgeTemplate;

		/**
		 * Custom graph that defines the alternate edge style to be used when
		 * the middle control point of edges is double clicked (flipped).
		 */
		public CustomGraph()
		{
			setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
		}

		
		/**
		 * Sets the edge template to be used to inserting edges.
		 */
		public void setEdgeTemplate(Object template)
		{
			edgeTemplate = template;
		}	

		//edges are not selectable
		public boolean isCellSelectable(Object cell) {
  		  if (getModel().isEdge(cell))
    		{
      		  return false;
    		}

 		   return super.isCellSelectable(cell);
	}
		/**
		 * Prints out some useful information about the cell in the tooltip.
		 */
		public String getToolTipForCell(Object cell)
		{
			String tip = "<html>";
			Object owner = ((mxCell) cell).getOwner();

			String cellStyle = ((mxCell) cell).getStyle();
			if (cellStyle.contains("ellipse")||cellStyle.contains("partial")||cellStyle.equals("primary")
				|| cellStyle.equals("unique")||cellStyle.equals("doubleEl")|| cellStyle.equals("un_dl")
				|| cellStyle.equals("dashed"))
				{
					tip +=  "data type = "+ ((mxCell) cell).getDataType();

					if (owner != null)
					{
						tip +=  "<br>owner = "+((mxCell) owner).getValue();
					}
				}
		
			tip += "</html>";
			return tip;
		}
		
		/**
		 * Overrides the method to use the currently selected edge template for new edges.
		 */
		public Object createEdge(Object parent, String id, Object value,
				Object source, Object target, String style)
		{
			if (edgeTemplate != null)
			{
				mxCell edge = (mxCell) cloneCells(new Object[] { edgeTemplate })[0];
				edge.setId(id);

				return edge;
			}

			return super.createEdge(parent, id, value, source, target, style);
		}

	}
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

		mxSwingConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
		mxConstants.W3C_SHADOWCOLOR = "#D3D3D3";

		GraphEditor editor = new GraphEditor();
		editor.createFrame(new EditorMenuBar(editor)).setVisible(true);
	}
}
