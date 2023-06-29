package com.mxgraph.thesis.swing.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.thesis.swing.editor.EditorActions.FontStyleAction;
import com.mxgraph.view.mxGraph;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


public class EditorPopupMenu extends JPopupMenu
{
	
	String[] dataTypes = { "BOOLEAN", "SMALLINT", "INTEGER", "BIGINT", "REAL", "DOUBLE PRECISION", 
		"DECIMAL(p,s)", "CHAR(n)", "VARCHAR(n)", "BIT(n)", "BIT VARYING(n)", "DATE", "TIME", "TIMESTAMP", "INTERVAL", "XML" };
	
	public EditorPopupMenu(BasicGraphEditor editor, mxGraphComponent graphComponent, Object c)
	{	
		boolean selected = !editor.getGraphComponent().getGraph().isSelectionEmpty();
		mxCell cell = (mxCell) c;
		final mxGraph graph = graphComponent.getGraph();
		JMenuItem rename = new JMenuItem("Rename");
		
		rename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mxIGraphModel model = graphComponent.getGraph().getModel();
				model.beginUpdate();
				String input = JOptionPane.showInputDialog("Rename:  ");
				if(input!=null) {
					if (input.endsWith(" ")) {
						input = input.substring(0, input.length() - 1);
					}
					if (input.contains(" ")) {
						input = input.replace(" ", "_");
					}
					cell.setValue(input);
					graph.refresh();
				}
				model.endUpdate();
			}		
		});
		add(rename);
	   
		String cellStyle = cell.getStyle();

     	if(cellStyle.contains("ellipse")||cellStyle.contains("doubleEl")||cellStyle.contains("dashed")
		   || cellStyle.contains("rectangle")||cellStyle.contains("doubleRect")|| cellStyle.contains("rhombus")
		   || cellStyle.contains("doubleRh")||cellStyle.contains("primary")||cellStyle.contains("unique")||cellStyle.contains("partial")||cellStyle.contains("ellipse;fontStyle=0") 
		   || cellStyle.contains("un_dl")||cellStyle.contains("ellipse;fontSize")){
	
		}

		if(cellStyle.contains("ellipse")||cellStyle.contains("ellipse;fontSize") ||cellStyle.contains("primary")||cellStyle.contains("unique")||cellStyle.contains("partial")
			||cellStyle.contains("doubleEl")||cellStyle.contains("dashed")
			||cellStyle.contains("un_dl")) {
			addSeparator();	
							
			JMenu bMenu = new JMenu("Belongs");
			JMenuItem menuItem = new JMenuItem("set Owner");
			JMenuItem menuItem1 = new JMenuItem("remove Owner");

			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//set connect to 1 meaning attribute-entity connection
					editor.setConnect(1);
					editor.setSource(cell);
				}
			});
			menuItem1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//editor.setSource(cell);
					editor.removeOwner(cell);
				}
			});


			bMenu.add(menuItem);
			bMenu.add(menuItem1);
			add(bMenu);
		}

		if(cellStyle.contains("ellipse") || cellStyle.contains("doubleEl") ||cellStyle.contains("primary")
		   ||cellStyle.contains("unique")||cellStyle.contains("partial")
		   ||cellStyle.contains("un_dl")||  cellStyle.contains("doubleEl")){

			addSeparator();	
			JMenu dTypes = new JMenu("Data Types");
            JRadioButton  jrb1 = new JRadioButton("BOOLEAN");
			JRadioButton  jrb2 = new JRadioButton("SMALLINT");	
			JRadioButton  jrb3 = new JRadioButton("INTEGER");
			JRadioButton  jrb4 = new JRadioButton("BIGINT");	
			JRadioButton  jrb5 = new JRadioButton("REAL");
			JRadioButton  jrb6 = new JRadioButton("DOUBLE PRECISION");	
			JRadioButton  jrb7 = new JRadioButton("DECIMAL(p,s)");
			JRadioButton  jrb8 = new JRadioButton("CHAR(n)");
			JRadioButton  jrb9 = new JRadioButton("VARCHAR(n)");
			JRadioButton  jrb10 = new JRadioButton("BIT(n)");	
			JRadioButton  jrb11 = new JRadioButton("BIT VARYING(n)");
			JRadioButton  jrb12 = new JRadioButton("DATE");	
			JRadioButton  jrb13 = new JRadioButton("TIME");
			JRadioButton  jrb14 = new JRadioButton("TIMESTAMP");	
			JRadioButton  jrb15 = new JRadioButton("INTERVAL");
			JRadioButton  jrb16 = new JRadioButton("XML");
			JRadioButton  jrb17 = new JRadioButton("OTHER (add type)");

			ButtonGroup group = new ButtonGroup();
			group.add(jrb1);group.add(jrb2);group.add(jrb3);group.add(jrb4);
			group.add(jrb5);group.add(jrb6);group.add(jrb7);group.add(jrb8);
			group.add(jrb9);group.add(jrb10);group.add(jrb11);group.add(jrb12);
			group.add(jrb13);group.add(jrb14);group.add(jrb15);group.add(jrb16);group.add(jrb17);

			dTypes.add(jrb1);dTypes.add(jrb2);dTypes.add(jrb3);dTypes.add(jrb4);
			dTypes.add(jrb5);dTypes.add(jrb6);dTypes.add(jrb7);dTypes.add(jrb8);
			dTypes.add(jrb9);dTypes.add(jrb10);dTypes.add(jrb11);dTypes.add(jrb12);
			dTypes.add(jrb13);dTypes.add(jrb14);dTypes.add(jrb15);dTypes.add(jrb16);dTypes.add(jrb17);
			add(dTypes);

			if (cell.getDataType().equals("BOOLEAN")) {
				jrb1.setSelected(true);
			}
			else if(cell.getDataType().equals("SMALLINT")) {
				jrb2.setSelected(true);
			}
			if (cell.getDataType().equals("INTEGER")) {
				jrb3.setSelected(true);
			}
			else if(cell.getDataType().equals("BIGINT")) {
				jrb4.setSelected(true);
			}
			if (cell.getDataType().equals("REAL")) {
				jrb5.setSelected(true);
			}
			else if(cell.getDataType().equals("DOUBLE PRECISION")) {
				jrb6.setSelected(true);
			}
			else if(cell.getDataType().contains("DECIMAL")) {
				jrb7.setSelected(true);
			}
			if (cell.getDataType().contains("CHAR")) {
				jrb8.setSelected(true);
			}
			else if(cell.getDataType().contains("VARCHAR")) {
				jrb9.setSelected(true);
			}
			if (cell.getDataType().contains("BIT")) {
				jrb10.setSelected(true);
			}
			else if(cell.getDataType().contains("BIT VARYING")) {
				jrb11.setSelected(true);
			}
			else if(cell.getDataType().equals("DATE")) {
				jrb12.setSelected(true);
			}
			else if(cell.getDataType().equals("TIME")) {
				jrb13.setSelected(true);
			}
			else if (cell.getDataType().equals("TIMESTAMP")) {
				jrb14.setSelected(true);
			}
			else if(cell.getDataType().equals("INTERVAL")) {
				jrb15.setSelected(true);
			}
			if (cell.getDataType().equals("XML")) {
				jrb16.setSelected(true);
			}
			else if(cell.getDataType().contains("OTHER")) {
				jrb17.setSelected(true);
			}
			
			
			jrb1.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb1) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							 cell.setDataType("BOOLEAN");
						}	
					}
					
					
					// if the state of checkbox is changed
					if (e.getSource() == jrb7) {
						model.beginUpdate();
						int nInput = 10;
						if (e.getStateChange() == 1){
							JFrame frame = new JFrame();
							String decimal = JOptionPane.showInputDialog(frame,"Please enter a value for n:  ", 10);
							frame.setVisible(true);
							if(decimal!=null) {
								 nInput = (int) Math.floor(Double.parseDouble(decimal));
								if (nInput<=0) {
									nInput=10;
								}
							}
							cell.setDataType("DECIMAL(" +nInput+")"); 	
						}	
					}
					// if the state of checkbox is changed
					if (e.getSource() == jrb8) {
						model.beginUpdate();
						int nInput = 10;
						if (e.getStateChange() == 1){
							String input = JOptionPane.showInputDialog("Please enter a value for n:  ", 10);
							if(input!=null) {
								 nInput = (int) Math.floor(Double.parseDouble(input));
								if (nInput<=0) {
									nInput=10;
								}
							}
							cell.setDataType("CHAR(" +nInput+")"); 	
						}		
					}

					model.endUpdate();
				}
			});

			jrb2.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb2) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							 cell.setDataType("SMALLINT");
							 jrb2.setSelected(true);
						}
					//	else
					//	cell.setDataType("INTEGER");
					}
					model.endUpdate();
				}
			});

			jrb3.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb3) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							 cell.setDataType("INTEGER");
						}
					//	else
					//	cell.setDataType("INTEGER");
					}
					model.endUpdate();
				}
			});

			jrb4.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb4) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							 cell.setDataType("BIGINT");
						}
					//	else
					//	cell.setDataType("INTEGER");
					}
					model.endUpdate();
				}
			});

			jrb5.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb5) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							 cell.setDataType("REAL");
						}
					//	else
					//	cell.setDataType("INTEGER");
					}
					model.endUpdate();
				}
			});

			jrb6.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb6) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							 cell.setDataType("DOUBLE PRECISION");
						}
						else
						cell.setDataType("INTEGER");
					}
					model.endUpdate();
				}
			});

			jrb7.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb7) {
						model.beginUpdate();
						int nInput1 = 8;
						int nInput2 = 2;
						if (e.getStateChange() == 1){
							String input1 = JOptionPane.showInputDialog("Please enter a value for precision i:  ", 8);
							String input2 = JOptionPane.showInputDialog("Please enter a value for precision i:  ", 2);
							if(input1!=null && input2!=null) {
								 nInput1 = (int) Math.floor(Double.parseDouble(input1));
								 nInput2 = (int) Math.floor(Double.parseDouble(input2));
								if (nInput1<=0) {
									nInput1=8;
								}
								if(nInput2<=0) {
									nInput2=2;
								}
							}
							cell.setDataType("DECIMAL(" +nInput1+","+nInput2+")"); 	
						}
					}
					model.endUpdate();
				}
			});

			jrb8.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb8) {
						model.beginUpdate();
						int nInput = 10;
						if (e.getStateChange() == 1){
							String input = JOptionPane.showInputDialog("Please enter a value for n:  ", 10);
							if(input!=null) {
								 nInput = (int) Math.floor(Double.parseDouble(input));
								if (nInput<=0) {
									nInput=10;
								}
							}
							cell.setDataType("CHAR(" +nInput+")"); 	
						}
					}
					model.endUpdate();
				}
			});

			jrb9.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb9) {
						model.beginUpdate();
						int nInput = 10;
						if (e.getStateChange() == 1){
							String input = JOptionPane.showInputDialog("Please enter a value for n:  ", 10);
							if(input!=null) {
								 nInput = (int) Math.floor(Double.parseDouble(input));
								if (nInput<=0) {
									nInput=10;
								}
							}
							cell.setDataType("VARCHAR(" +nInput+")"); 	
						}
					}
					model.endUpdate();
				}
			});

			jrb10.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb10) {
						model.beginUpdate();
						int nInput = 10;
						if (e.getStateChange() == 1){
							String input = JOptionPane.showInputDialog("Please enter a value for n:  ", 10);
							if(input!=null) {
								 nInput = (int) Math.floor(Double.parseDouble(input));
								if (nInput<=0) {
									nInput=10;
								}
							}
							cell.setDataType("BIT(" +nInput+")"); 	
						}
					}
					model.endUpdate();
				}
			});

			jrb11.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb11) {
						model.beginUpdate();
						int nInput = 10;
						if (e.getStateChange() == 1){
							String input = JOptionPane.showInputDialog("Please enter a value for n:  ", 10);
							if(input!=null) {
								 nInput = (int) Math.floor(Double.parseDouble(input));
								if (nInput<=0) {
									nInput=10;
								}
							}
							cell.setDataType("BIT VARYING(" +nInput+")"); 	
						}
			
					}
					model.endUpdate();
				}
			});

			jrb12.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb12) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							 cell.setDataType("DATE");
						}
					}
					model.endUpdate();
				}
			});

			jrb13.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb13) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							 cell.setDataType("TIME");
						}
					}
					model.endUpdate();
				}
			});

			jrb14.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb14) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							 cell.setDataType("TIMESTAMP");
						}
					}
					model.endUpdate();
				}
			});

			jrb15.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb15) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							 cell.setDataType("INTERVAL");
						}
			
					}
					model.endUpdate();
				}
			});

			jrb16.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb16) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							 cell.setDataType("XML");
						}
					}
					model.endUpdate();
				}
			});

			jrb17.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == jrb17) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							String input = JOptionPane.showInputDialog("Add type:  ");
							if(input!=null) {	
								cell.setDataType(input);
							}
							
						}
					}
					model.endUpdate();
				}
			});

			addSeparator();	

		}		

		if (cellStyle.contains("rhombus")  || cellStyle.contains("doubleRh")) {
			addSeparator();	
			JMenu weak = new JMenu("Set Weak RelationShip");
			JCheckBox weakE = new JCheckBox("Weak RelationShip");
			if(cellStyle.contains("doubleRh")) {
				weakE.setSelected(true); 
			}
			weak.add(weakE);
			weakE.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();
				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == weakE) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
						model.setStyle(cell, "doubleRh");
						}
						else
						model.setStyle(cell, "rhombus");
					}
					model.endUpdate();
				}
			});
			add(weak);

			addSeparator();
			// connection points: Top, Left, Right, Bottom
			JMenu tMenu = new JMenu("Top");
			JMenu tmenuItem = new JMenu("set");
			JRadioButton tTotal = new JRadioButton("set total");
			JRadioButton tTotal1 = new JRadioButton("set total 1");
			JRadioButton tTotalN = new JRadioButton("set total N");
			JRadioButton tTotalM = new JRadioButton("set total M");
			JRadioButton tTotalK = new JRadioButton("set total K");
			JRadioButton tTotalL = new JRadioButton("set total L");

			JRadioButton tPartial = new JRadioButton("set partial");
			JRadioButton tPartial1 = new JRadioButton("set partial 1");
			JRadioButton tPartialN = new JRadioButton("set partial N");
			JRadioButton tPartialM = new JRadioButton("set partial M");
			JRadioButton tPartialK = new JRadioButton("set partial K");
			JRadioButton tPartialL = new JRadioButton("set partial L");
			ButtonGroup group = new ButtonGroup();
			group.add(tTotal1);group.add(tTotalN);group.add(tTotalM);group.add(tTotalK);group.add(tTotalL);
			group.add(tPartial1);group.add(tPartialN);group.add(tPartialM);group.add(tPartialK);group.add(tPartialL);
			group.add(tTotal);group.add(tTotal);

			JMenuItem tmenuItem1 = new JMenuItem("remove");

			JMenu lMenu = new JMenu("Left");
			JMenu lmenuItem = new JMenu("set");
			JRadioButton lTotal = new JRadioButton("set total");
			JRadioButton lTotal1 = new JRadioButton("set total 1");
			JRadioButton lTotalN = new JRadioButton("set total N");
			JRadioButton lTotalM = new JRadioButton("set total M");
			JRadioButton lTotalK = new JRadioButton("set total K");
			JRadioButton lTotalL = new JRadioButton("set total L");

			JRadioButton lPartial = new JRadioButton("set partial");
			JRadioButton lPartial1 = new JRadioButton("set partial 1");
			JRadioButton lPartialN = new JRadioButton("set partial N");
			JRadioButton lPartialM = new JRadioButton("set partial M");
			JRadioButton lPartialK = new JRadioButton("set partial K");
			JRadioButton lPartialL = new JRadioButton("set partial L");
			ButtonGroup group1 = new ButtonGroup();
			group1.add(tTotal1);group1.add(tTotalN);group1.add(tTotalM);group1.add(tTotalK);group1.add(tTotalL);
			group1.add(tPartial1);group1.add(tPartialN);group1.add(tPartialM);group1.add(tPartialK);group1.add(tPartialL);
			group1.add(tTotal);group1.add(tTotal);

			JMenuItem lmenuItem1 = new JMenuItem("remove");

			JMenu rMenu = new JMenu("Right");
			JMenu rmenuItem = new JMenu("set");
			JRadioButton rTotal = new JRadioButton("set total");
			JRadioButton rTotal1 = new JRadioButton("set total 1");
			JRadioButton rTotalN = new JRadioButton("set total N");
			JRadioButton rTotalM = new JRadioButton("set total M");
			JRadioButton rTotalK = new JRadioButton("set total K");
			JRadioButton rTotalL = new JRadioButton("set total L");

			JRadioButton rPartial = new JRadioButton("set partial");
			JRadioButton rPartial1 = new JRadioButton("set partial 1");
			JRadioButton rPartialN = new JRadioButton("set partial N");
			JRadioButton rPartialM = new JRadioButton("set partial M");
			JRadioButton rPartialK = new JRadioButton("set partial K");
			JRadioButton rPartialL = new JRadioButton("set partial L");
			ButtonGroup group2 = new ButtonGroup();
			group2.add(rTotal1);group2.add(rTotalN);group2.add(rTotalM);group2.add(rTotalK);group2.add(rTotalL);
			group2.add(rPartial1);group2.add(rPartialN);group2.add(rPartialM);group2.add(rPartialK);group2.add(rPartialL);
			group2.add(rTotal);group2.add(rTotal);

			JMenuItem rmenuItem1 = new JMenuItem("remove");

			JMenu bMenu = new JMenu("Bottom");
			JMenu bmenuItem = new JMenu("set");
			JRadioButton bTotal = new JRadioButton("set total");
			JRadioButton bTotal1 = new JRadioButton("set total 1");
			JRadioButton bTotalN = new JRadioButton("set total N");
			JRadioButton bTotalM = new JRadioButton("set total M");
			JRadioButton bTotalK = new JRadioButton("set total K");
			JRadioButton bTotalL = new JRadioButton("set total L");

			JRadioButton bPartial = new JRadioButton("set partial");
			JRadioButton bPartial1 = new JRadioButton("set partial 1");
			JRadioButton bPartialN = new JRadioButton("set partial N");
			JRadioButton bPartialM = new JRadioButton("set partial M");
			JRadioButton bPartialK = new JRadioButton("set partial K");
			JRadioButton bPartialL = new JRadioButton("set partial L");
			ButtonGroup group3 = new ButtonGroup();
			group3.add(bTotal1);group3.add(bTotalN);group3.add(bTotalM);group3.add(bTotalK);group3.add(bTotalL);
			group3.add(bPartial1);group3.add(bPartialN);group3.add(bPartialM);group3.add(bPartialK);group3.add(bPartialL);
			group3.add(bTotal);group3.add(bTotal);

			JMenuItem bmenuItem1 = new JMenuItem("remove");
			
			tPartial.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					//new mxConnectionConstraint(new mxPoint(WIDTH/2, 0), true);
					//set connect to 2 meaning relationship top connection
					//editor.removeTopOwner(cell);
					editor.setConnect(2);
					editor.setSource(cell);				
				}		
			});
			tTotal.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//new mxConnectionConstraint(new mxPoint(WIDTH/2, 0), true);
					//set connect to 2 meaning relationship top connection
					editor.setConnect(6);
					editor.setSource(cell);				
				}		
			});

			tPartial1.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getTopOwner();
					editor.removeTopOwner(cell);
					editor.setConnect(2);
					editor.setSource(cell);	
					cell.setTopOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});
			tTotal1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getTopOwner();
					editor.removeTopOwner(cell);
					editor.setConnect(6);
					editor.setSource(cell);	
					cell.setTopOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});

			tPartialN.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getTopOwner();
					editor.removeTopOwner(cell);	
					editor.setConnect(2.1);
					editor.setSource(cell);	
					cell.setTopOwner(ownerC);
					editor.ConnectShapes(ownerC);	
						
				}		
			});
			tTotalN.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getTopOwner();
					editor.removeTopOwner(cell);
					editor.setConnect(6.1);
					editor.setSource(cell);		
					cell.setTopOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});

			tPartialM.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getTopOwner();
					editor.removeTopOwner(cell);
					editor.setConnect(2.2);
					editor.setSource(cell);		
					cell.setTopOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});
			tTotalM.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getTopOwner();
					editor.removeTopOwner(cell);
					editor.setConnect(6.2);
					editor.setSource(cell);	
					cell.setTopOwner(ownerC);
					editor.ConnectShapes(ownerC);				
				}		
			});

			tPartialK.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getTopOwner();
					editor.removeTopOwner(cell);
					editor.setConnect(2.3);
					editor.setSource(cell);		
					cell.setTopOwner(ownerC);
					editor.ConnectShapes(ownerC);		
				}		
			});
			tTotalK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getTopOwner();
					editor.removeTopOwner(cell);
					editor.setConnect(6.3);
					editor.setSource(cell);	
					cell.setTopOwner(ownerC);
					editor.ConnectShapes(ownerC);				
				}		
			});

			tPartialL.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getTopOwner();
					editor.removeTopOwner(cell);
					editor.setConnect(2.4);
					editor.setSource(cell);		
					cell.setTopOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});
			tTotalL.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getTopOwner();
					editor.removeTopOwner(cell);
					editor.setConnect(6.4);
					editor.setSource(cell);		
					cell.setTopOwner(ownerC);	
					editor.ConnectShapes(ownerC);		
				}		
			});

			tmenuItem1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//editor.setSource(cell);
					editor.removeTopOwner(cell);
				}
			});


			lPartial.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					//new mxConnectionConstraint(new mxPoint(WIDTH/2, 0), true);
					//set connect to 2 meaning relationship top connection
					//editor.removeTopOwner(cell);
					editor.setConnect(3);
					editor.setSource(cell);				
				}		
			});

			lTotal.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//new mxConnectionConstraint(new mxPoint(WIDTH/2, 0), true);
					//set connect to 2 meaning relationship top connection
					editor.setConnect(7);
					editor.setSource(cell);				
				}		
			});

			lPartial1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getLeftOwner();
					editor.removeLeftOwner(cell);
					editor.setConnect(3);
					editor.setSource(cell);
					cell.setLeftOwner(ownerC);
					editor.ConnectShapes(ownerC);	
				}
			});


			lTotal1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getLeftOwner();
					editor.removeLeftOwner(cell);
					editor.setConnect(7);
					editor.setSource(cell);	
					cell.setLeftOwner(ownerC);
					editor.ConnectShapes(ownerC);				
				}		
			});

			lPartialN.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getLeftOwner();
					editor.removeLeftOwner(cell);
					editor.setConnect(3.1);
					editor.setSource(cell);
					cell.setLeftOwner(ownerC);	
					editor.ConnectShapes(ownerC);				
				}		
			});
			lTotalN.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getLeftOwner();
					editor.removeLeftOwner(cell);
					editor.setConnect(7.1);
					editor.setSource(cell);		
					cell.setLeftOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});

			lPartialM.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getLeftOwner();
					editor.removeLeftOwner(cell);
					editor.setConnect(3.2);
					editor.setSource(cell);		
					cell.setLeftOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});
			lTotalM.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getLeftOwner();
					editor.removeLeftOwner(cell);
					editor.setConnect(7.2);
					editor.setSource(cell);		
					cell.setLeftOwner(ownerC);	
					editor.ConnectShapes(ownerC);		
				}		
			});

			lPartialK.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getLeftOwner();
					editor.removeLeftOwner(cell);
					editor.setConnect(3.3);
					editor.setSource(cell);		
					cell.setLeftOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});
			lTotalK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getLeftOwner();
					editor.removeLeftOwner(cell);
					editor.setConnect(7.3);
					editor.setSource(cell);	
					cell.setLeftOwner(ownerC);	
					editor.ConnectShapes(ownerC);			
				}		
			});

			lPartialL.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getLeftOwner();
					editor.removeLeftOwner(cell);
					editor.setConnect(3.4);
					editor.setSource(cell);	
					cell.setLeftOwner(ownerC);	
					editor.ConnectShapes(ownerC);			
				}		
			});
			lTotalL.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getLeftOwner();
					editor.removeLeftOwner(cell);
					editor.setConnect(7.4);
					editor.setSource(cell);	
					cell.setLeftOwner(ownerC);	
					editor.ConnectShapes(ownerC);			
				}		
			});

			lmenuItem1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//editor.setSource(cell);
					editor.removeLeftOwner(cell);
				}
			});


			rPartial.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					//new mxConnectionConstraint(new mxPoint(WIDTH/2, 0), true);
					//set connect to 2 meaning relationship top connection
					//editor.removeTopOwner(cell);
					editor.setConnect(4);
					editor.setSource(cell);				
				}		
			});
			rTotal.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//new mxConnectionConstraint(new mxPoint(WIDTH/2, 0), true);
					//set connect to 2 meaning relationship top connection
					editor.setConnect(8);
					editor.setSource(cell);				
				}		
			});

			rPartial1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getRightOwner();
					editor.removeRightOwner(cell);
					editor.setConnect(4);
					editor.setSource(cell);
					cell.setRightOwner(ownerC);
					editor.ConnectShapes(ownerC);	
				}
			});

			rTotal1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getRightOwner();
					editor.removeRightOwner(cell);
					editor.setConnect(8);
					editor.setSource(cell);	
					cell.setRightOwner(ownerC);
					editor.ConnectShapes(ownerC);				
				}		
			});

			rPartialN.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getRightOwner();
					editor.removeRightOwner(cell);
					editor.setConnect(4.1);
					editor.setSource(cell);		
					cell.setRightOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});
			rTotalN.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getRightOwner();
					editor.removeRightOwner(cell);
					editor.setConnect(8.1);
					editor.setSource(cell);
					cell.setRightOwner(ownerC);		
					editor.ConnectShapes(ownerC);			
				}		
			});

			rPartialM.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getRightOwner();
					editor.removeRightOwner(cell);
					editor.setConnect(4.2);
					editor.setSource(cell);		
					cell.setRightOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});
			rTotalM.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getRightOwner();
					editor.removeRightOwner(cell);
					editor.setConnect(8.2);
					editor.setSource(cell);	
					cell.setRightOwner(ownerC);
					editor.ConnectShapes(ownerC);				
				}		
			});

			rPartialK.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getRightOwner();
					editor.removeRightOwner(cell);
					editor.setConnect(4.3);
					editor.setSource(cell);	
					cell.setRightOwner(ownerC);
					editor.ConnectShapes(ownerC);				
				}		
			});
			rTotalK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getRightOwner();
					editor.removeRightOwner(cell);
					editor.setConnect(8.3);
					editor.setSource(cell);		
					cell.setRightOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});

			rPartialL.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getRightOwner();
					editor.removeRightOwner(cell);
					editor.setConnect(4.4);
					editor.setSource(cell);	
					cell.setRightOwner(ownerC);
					editor.ConnectShapes(ownerC);				
				}		
			});
			rTotalL.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getRightOwner();
					editor.removeRightOwner(cell);
					editor.setConnect(8.4);
					editor.setSource(cell);	
					cell.setRightOwner(ownerC);	
					editor.ConnectShapes(ownerC);			
				}		
			});

			rmenuItem1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//editor.setSource(cell);
					editor.removeRightOwner(cell);
				}
			});

			bPartial.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					//new mxConnectionConstraint(new mxPoint(WIDTH/2, 0), true);
					//set connect to 2 meaning relationship top connection
					//editor.removeTopOwner(cell);
					editor.setConnect(5);
					editor.setSource(cell);				
				}		
			});
			bTotal.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//new mxConnectionConstraint(new mxPoint(WIDTH/2, 0), true);
					//set connect to 2 meaning relationship top connection
					editor.setConnect(9);
					editor.setSource(cell);				
				}		
			});

			bPartial1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getBottomOwner();
					editor.removeBottomOwner(cell);
					editor.setConnect(5);
					editor.setSource(cell);
					cell.setBottomOwner(ownerC);
					editor.ConnectShapes(ownerC);	
				}
			});

			bTotal1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getBottomOwner();
					editor.removeBottomOwner(cell);
					editor.setConnect(9);
					editor.setSource(cell);	
					cell.setBottomOwner(ownerC);
					editor.ConnectShapes(ownerC);				
				}		
			});

			bPartialN.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getBottomOwner();
					editor.removeBottomOwner(cell);
					editor.setConnect(5.1);
					editor.setSource(cell);
					cell.setBottomOwner(ownerC);
					editor.ConnectShapes(ownerC);					
				}		
			});
			bTotalN.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getBottomOwner();
					editor.removeBottomOwner(cell);
					editor.setConnect(9.1);
					editor.setSource(cell);		
					cell.setBottomOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});

			bPartialM.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getBottomOwner();
					editor.removeBottomOwner(cell);
					editor.setConnect(5.2);
					editor.setSource(cell);		
					cell.setBottomOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});
			bTotalM.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getBottomOwner();
					editor.removeBottomOwner(cell);
					editor.setConnect(9.2);
					editor.setSource(cell);	
					cell.setBottomOwner(ownerC);
					editor.ConnectShapes(ownerC);				
				}		
			});

			bPartialK.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getBottomOwner();
					editor.removeBottomOwner(cell);
					editor.setConnect(5.3);
					editor.setSource(cell);		
					cell.setBottomOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});
			bTotalK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getBottomOwner();
					editor.removeBottomOwner(cell);
					editor.setConnect(9.3);
					editor.setSource(cell);		
					cell.setBottomOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});

			bPartialL.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getBottomOwner();
					editor.removeBottomOwner(cell);
					editor.setConnect(5.4);
					editor.setSource(cell);		
					cell.setBottomOwner(ownerC);
					editor.ConnectShapes(ownerC);			
				}		
			});
			bTotalL.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxCell ownerC = (mxCell) cell.getBottomOwner();
					editor.removeBottomOwner(cell);
					editor.setConnect(9.4);
					editor.setSource(cell);	
					cell.setBottomOwner(ownerC);
					editor.ConnectShapes(ownerC);				
				}		
			});

			bmenuItem1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//editor.setSource(cell);
					editor.removeBottomOwner(cell);
				}
			});

			if (!cell.hasTopOwner()) {
			tmenuItem.add(tPartial);
			tmenuItem.add(tTotal);
			}
			else if (cell.hasTopOwner()) {
				tmenuItem.add(tPartial1);
				tmenuItem.add(tPartialN);tmenuItem.add(tPartialM);tmenuItem.add(tPartialK);tmenuItem.add(tPartialL);
				tmenuItem.add(tTotal1);tmenuItem.add(tTotalN);tmenuItem.add(tTotalM);tmenuItem.add(tTotalK);tmenuItem.add(tTotalL);
			}
			tMenu.add(tmenuItem);
			tMenu.add(tmenuItem1);add(tMenu);

			if (!cell.hasLeftOwner()) {
				lmenuItem.add(lPartial);
				lmenuItem.add(lTotal);
			}

			else if (cell.hasLeftOwner()) {
				lmenuItem.add(lPartial1);
				lmenuItem.add(lPartialN);lmenuItem.add(lPartialM);lmenuItem.add(lPartialK);lmenuItem.add(lPartialL);
				lmenuItem.add(lTotal1);lmenuItem.add(lTotalN);lmenuItem.add(lTotalM);lmenuItem.add(lTotalK);lmenuItem.add(lTotalL);
			}
			lMenu.add(lmenuItem);
			lMenu.add(lmenuItem1);add(lMenu);

			if (!cell.hasRightOwner()) {
				rmenuItem.add(rPartial);
				rmenuItem.add(rTotal);
			}

			else if (cell.hasRightOwner()) {
				rmenuItem.add(rPartial1);
				rmenuItem.add(rPartialN);rmenuItem.add(rPartialM);rmenuItem.add(rPartialK);rmenuItem.add(rPartialL);
				rmenuItem.add(lTotal1);rmenuItem.add(rTotalN);rmenuItem.add(rTotalM);rmenuItem.add(rTotalK);rmenuItem.add(rTotalL);
			}
			rMenu.add(rmenuItem);
			rMenu.add(rmenuItem1);add(rMenu);

			if (!cell.hasBottomOwner()) {
				bmenuItem.add(bPartial);
				bmenuItem.add(bTotal);
			}

			else if (cell.hasBottomOwner()) {
				bmenuItem.add(bPartial1);
				bmenuItem.add(bPartialN);bmenuItem.add(bPartialM);bmenuItem.add(bPartialK);bmenuItem.add(bPartialL);
				bmenuItem.add(bTotal1);bmenuItem.add(bTotalN);bmenuItem.add(bTotalM);bmenuItem.add(bTotalK);bmenuItem.add(bTotalL);
			}
			bMenu.add(bmenuItem);
			bMenu.add(bmenuItem1);add(bMenu);
		}

		if ( cellStyle.contains("rectangle")||cellStyle.contains("doubleRect")){
			addSeparator();	
			
			JMenu weak = new JMenu("Set Weak Entity");
			JCheckBox weakE = new JCheckBox("Weak Entity");
			if (cellStyle.contains("doubleRect")){
				weakE.setSelected(true);
			}
			
			weak.add(weakE);
			weakE.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == weakE) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
						model.setStyle(cell, "doubleRect");
						}
						else
						model.setStyle(cell, "rectangle");
					}
					model.endUpdate();
				}
			});
			add(weak);
		}

		if ( cellStyle.contains("rectangle")) {
			JMenu set = new JMenu("Set");
			JMenuItem setItem = new JMenuItem("Super");
			JMenuItem setItem1 = new JMenuItem("remove super");

			setItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//set connect to 1.3 meaning superclass connection
					editor.setConnect(1.3);
					editor.setSource(cell);
				}
			});
			setItem1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//editor.setSource(cell);
					editor.removeOwner(cell);
				}
			});


			set.add(setItem);
			set.add(setItem1);
			add(set);
		}

		if ( cellStyle.contains("ellipse") ||cellStyle.contains("primary" )
		||cellStyle.contains("unique") ||cellStyle.contains("partial") ||cellStyle.contains("ellipse;fontSize") ) {
			addSeparator();	
			
			JMenu key = new JMenu("Key");
			JRadioButton keyU = new JRadioButton("Unique");
			JRadioButton keyP = new JRadioButton("Primary key");
			JRadioButton keyPa = new JRadioButton("Partial key");
			JRadioButton keyNo = new JRadioButton("Remove key");

			ButtonGroup group1 = new ButtonGroup();
			group1.add(keyU);group1.add(keyP);group1.add(keyPa);group1.add(keyNo);

			if (cellStyle.contains("primary")){
				keyP.setSelected(true);
			}
			
			if (cellStyle.contains("unique")){
				keyU.setSelected(true);
			}

			if (cellStyle.contains("partial")){
				keyPa.setSelected(true);
			}

			key.add(keyU);
			key.add(keyP);
			key.add(keyPa);
			key.add(keyNo);

			keyP.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == keyP) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
						model.setStyle(cell, "primary");
						}
						else
						model.setStyle(cell, "ellipse");
					}
					model.endUpdate();
				}
			});

			keyU.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == keyU) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
						model.setStyle(cell, "unique");
						}
						else
						model.setStyle(cell, "ellipse");
					}
					model.endUpdate();
				}
			});
			
			keyPa.addItemListener(new ItemListener() {
			 	mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == keyPa) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
							 new FontStyleAction(0, true,cell);
						model.setStyle(cell, "partial");
						}
						else
						model.setStyle(cell, "ellipse");
					}
					model.endUpdate();
				}
			});

			keyNo.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == keyNo) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
						model.setStyle(cell, "ellipse");
						}
						else
						return;
					}
					model.endUpdate();
				}
			});

			add(key);
		}

		if(cellStyle.contains("ellipse") ||cellStyle.contains("ellipse;fontSize")||cellStyle.contains("ellipse;fontStyle=0")) {
			addSeparator();
			JMenu notNull = new JMenu("Set not Null");
			JCheckBox nNull = new JCheckBox("not Null");
			if(cell.getNotNull()==true){
				nNull.setSelected(true);
			}
			notNull.add(nNull);
			nNull.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == nNull) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
						cell.setNotNull(true);
						}
						else
						cell.setNotNull(false);
					}
					model.endUpdate();
				}
			});
			add(notNull);

		}
		
		if (cellStyle.contains("doubleEl")||cellStyle.contains("un_dl")) {
			addSeparator();
				JMenu Unique = new JMenu("Set Unique");
				JCheckBox unique = new JCheckBox("Unique");
				if (cellStyle.contains("un_dl")){
					unique.setSelected(true);
				}
				
				Unique.add(unique);
				unique.addItemListener(new ItemListener() {
					mxIGraphModel model = graphComponent.getGraph().getModel();
	
					@Override
					public void itemStateChanged(ItemEvent e) {
						// if the state of checkbox is changed
						if (e.getSource() == unique) {
							model.beginUpdate();
							if (e.getStateChange() == 1){
							model.setStyle(cell, "un_dl");
							}
							else
							model.setStyle(cell, "doubleEl");
						}
						model.endUpdate();
					}
				});
				add(Unique);
		}

		if (cellStyle.contains("U_circle")) {
			addSeparator();	
			JMenu bMenu = new JMenu("sub");
			JMenu menuItem = new JMenu("set sub");
			JRadioButton sTotal = new JRadioButton("sub total");
			JRadioButton sPartial = new JRadioButton("sub partial");
			JMenuItem menuItem1 = new JMenuItem("remove sub");
			JMenu Usuper = new JMenu("super");
			JMenuItem superSet = new JMenuItem("set super");
			JMenuItem superRemove = new JMenuItem("remove super");
			ButtonGroup sGroup = new ButtonGroup();
			sGroup.add(sPartial);sGroup.add(sTotal);
			bMenu.add(menuItem); menuItem.add(sPartial); menuItem.add(sTotal);bMenu.add(menuItem1); add(bMenu);
			Usuper.add(superSet); 
			if(cell.ssList.size() > 0) { 
				Usuper.add(superRemove);
			}
			add(Usuper);

			sPartial.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//set connect to 1 meaning attribute-entity connection
					editor.setConnect(11);
					editor.setSource(cell);
				}
			});
			sTotal.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//set connect to 1 meaning attribute-entity connection
					editor.setConnect(11.1);
					editor.setSource(cell);
				}
			});

			menuItem1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//editor.setSource(cell);
					editor.removeOwner(cell);
				}
			});

			superSet.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//set connect to 1 meaning attribute-entity connection
					editor.setConnect(10);
					editor.setSource(cell);
				}
			});
			superRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editor.setConnect(10.1);
					editor.setSource(cell);
				}
			});
		}

		if (cellStyle.contains("d_circle")||cellStyle.contains("o_circle")) {
			addSeparator();	
			JMenu overl = new JMenu("set overlapping");
			JCheckBox overla = new JCheckBox("overlapping");
			if (cellStyle.contains("o_circle")){
				overla.setSelected(true);
			}
			
			overl.add(overla);
			overla.addItemListener(new ItemListener() {
				mxIGraphModel model = graphComponent.getGraph().getModel();

				@Override
				public void itemStateChanged(ItemEvent e) {
					// if the state of checkbox is changed
					if (e.getSource() == overla) {
						model.beginUpdate();
						if (e.getStateChange() == 1){
						model.setStyle(cell, "o_circle");
						model.setValue(cell, "o");
						}
						else if(e.getStateChange() == 2){
						model.setStyle(cell, "d_circle");
						model.setValue(cell, "d");
						}
					}
					model.endUpdate();
				}
			});
			add(overl);
			addSeparator();	
			JMenu bMenu = new JMenu("super");
			JMenuItem  menuItem = new JMenuItem ("set super");
			JMenuItem menuItem1 = new JMenuItem("remove super");
			JMenu defattr = new JMenu("defining attribute");
			JMenu type = new JMenu("type");
			JMenuItem typePartial = new JMenuItem("partial");
			JMenuItem typeTotal = new JMenuItem("total");
			type.add(typePartial); type.add(typeTotal);

			JMenu Usub = new JMenu("sub");
			JMenuItem subSet = new JMenuItem("set sub");
			JMenuItem defCrit = new JMenuItem("defining criteria");
			if(cell.ssList.size() > 0) { 
				Usub.add(defCrit);
			}
			JMenuItem subRemove = new JMenuItem("remove sub");
						
			bMenu.add(menuItem); add(bMenu);
			Usub.add(subSet);
			if(cell.ssList.size() > 0) { 
				Usub.add(subRemove);
			}
			add(Usub);

			if(cell.hasOwner()) {
				mxCell owner = (mxCell) cell.getOwner();
				Object[] currentEdge =  graph.getEdgesBetween(cell,owner); 
				// get the edge between the shapes 
				mxCell currEdge = (mxCell) currentEdge[0];
				Object[] currentEdges =  graph.getEdges(owner); 
				for(Object cE:currentEdges){
					mxAnalysisGraph aGraph = new mxAnalysisGraph();
					aGraph.setGraph(graph);
					mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
					String sourceStyle = edgeSource.getStyle();
					String sourceName =(String) edgeSource.getValue();
					if(sourceStyle.contains ("ellipse")||sourceStyle.contains("ellipse;fontSize")||sourceStyle.contains("ellipse;fontStyle=0")||sourceStyle.contains ("unique")
						||sourceStyle.contains ("primary")||sourceStyle.contains ("doubleEl")||sourceStyle.contains ("un_dl")){
							JMenuItem defA = new JMenuItem(sourceName);
							defattr.add(defA);
							bMenu.add(defattr);
							defA.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
										currEdge.setValue(sourceName);
										graph.refresh();
								}
							});
					}
				}	
				bMenu.add(type);
				bMenu.add(menuItem1);
				typePartial.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
							currEdge.setStyle("strokeWidth=1;");
							graph.refresh();
					}
				});
				typeTotal.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
							currEdge.setStyle("strokeWidth=5;");
							graph.refresh();
					}
				});

				
			}
			
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editor.setConnect(1.1);
					editor.setSource(cell);
				}
			});
			
			

			menuItem1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//editor.setSource(cell);
					editor.removeOwner(cell);
				}
			});

			subSet.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editor.setConnect(10.2);
					editor.setSource(cell);
				}
			});

			defCrit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editor.setConnect(10.4);
					editor.setSource(cell);
				}
			});
				
			subRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editor.setConnect(10.1);
					editor.setSource(cell);
				}
			});
		}

			

		addSeparator();
		add(editor.bind("Delete", mxGraphActions.getDeleteAction(),
					"/com/mxgraph/thesis/swing/images/delete.gif")).setEnabled(selected);
					
   }  
   
}


