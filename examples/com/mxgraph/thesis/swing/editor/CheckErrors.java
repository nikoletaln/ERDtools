package com.mxgraph.thesis.swing.editor;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.Dimension;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.view.mxGraph;
import com.mxgraph.analysis.mxAnalysisGraph;

public class CheckErrors extends JFrame {

    JFrame frame;
    JTable table;
    DefaultTableModel model ;
    mxCell cell;
    boolean hasErrors = false;

    public CheckErrors(mxGraphComponent graphComponent, mxGraph graph) {

        frame = new JFrame("List of Errors");
        model = new DefaultTableModel(); 
        table = new JTable(model); 

        // Create a couple of columns 
        model.addColumn("Type"); 
        model.addColumn("Name"); 
        model.addColumn("Description"); 

        table.setPreferredScrollableViewportSize(new Dimension(600, 500));
        table.setFillsViewportHeight(true);
        //Set up column sizes.
        initColumnSizes(table);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        mxAnalysisGraph aGraph = new mxAnalysisGraph();
        aGraph.setGraph(graph);

        //return the Errors List
        Object[] cells = graph.getChildVertices(graph.getDefaultParent());
        for (Object c : cells) {
            mxCell cell = (mxCell) c;
            //get every cell's value
           String getName =  (String) cell.getValue(); 
            //get every cell's style
           String cellStyle = cell.getStyle();

           //entities
           if( cellStyle.contains("rectangle")||cellStyle.contains("doubleRect")) {     
               Object[] currentEdges =  graph.getEdges(cell); 
               for (Object cc : cells) {
                mxCell cellC = (mxCell) cc;
                if( !cellC.equals(cell) && (cellC.getStyle().contains("rectangle")||cellC.getStyle().contains("doubleRect"))  && cellC.getValue().equals(cell.getValue())) {
                  AddRow("Entity",getName,"Entity has duplicate name with another"); 
                }
              }
              Boolean hasWeakS = false;
              Boolean hasSuper = false;
              String ownerCName=null;
              if (cell.hasOwner()){
                  mxCell ownerC = (mxCell) cell.getOwner();
                  ownerCName = (String) ownerC.getValue();
                  if(ownerC.getStyle().contains ("doubleRect") ) {
                    hasWeakS= true;
                  }
                  if(ownerC.getStyle().contains ("rectangle")) {
                    hasSuper=true;
                  }
              }

              if (hasWeakS==true){
                  AddRow("Weak entity",ownerCName,"Weak entities cannot be superclasses"); 
              }

               Boolean hasAtrribute = false;
               Integer hasUnique=0;
               Integer hasPrimary=0;
               Integer hasPartial=0;
               
                for(Object cE:currentEdges){
                  mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
                  String sourceStyle = edgeSource.getStyle(); 

                  if(sourceStyle.contains ("ellipse")||cellStyle.contains("ellipse;fontSize")||cellStyle.contains("ellipse;fontStyle=0")||sourceStyle.contains ("doubleEl")) {
                    hasAtrribute= true;
                  }
                  if(sourceStyle.contains ("unique")||sourceStyle.contains ("un_dl")) {
                    hasUnique++;
                    hasAtrribute= true;
                  }
                  if(sourceStyle.contains ("primary")) {
                    hasPrimary++;
                    hasAtrribute= true;
                  }
                  if(sourceStyle.contains ("partial")) {
                    hasPartial++;
                    hasAtrribute= true;
                  }
                  if(sourceStyle.contains ("U_circle")) {
                    //if entity is a sub 
                    if(edgeSource.getOwner()==cell){
                      //if a weak entity is a sub
                      if(cellStyle.contains("doubleRect") ) {
                        AddRow("Weak entity",getName,"Weak entities cannot have superclasses"); 
                      }
                      else {
                        hasAtrribute = true;
                      }
                    } 
                    // if entity is super
                    if(edgeSource.getOwner()!=cell) {
                      // if entity is a weak one
                      if(cellStyle.contains("doubleRect") ) { 
                        AddRow("Weak entity",getName,"Weak entities cannot be superclasses"); 
                      }
                    }
                  }

                  if(sourceStyle.contains ("o_circle")||sourceStyle.contains ("d_circle")) {
                    //if entity is not a super
                    if(edgeSource.getOwner()!=cell) {
                      //if a weak entity is a sub
                      if(cellStyle.contains("doubleRect") ) {
                        AddRow("Weak entity",getName,"Weak entities cannot have superclasses"); 
                      }
                      else { //it is a sub
                        if (cell.hasOwner()){ 
                          hasAtrribute=true;
                         // hasSuper=false;
                        }else{
                           hasSuper=true;
                        }
                      }
                    } 
                    //if a weak entity is a super
                    if(((mxCell) edgeSource.getOwner()).getStyle().contains("doubleRect")) {
                      if(cell==edgeSource.getOwner()) {
                        AddRow("Weak entity",getName,"Weak entities cannot be superclasses"); 
                      }
                    }
                  }
                } 

                if (hasAtrribute==false && hasSuper==false){
                   AddRow((cellStyle.contains("rectangle") ? "Entity" : "Weak entity"),getName,"Entity has no Attributes"); 
                }
                if (hasUnique==0 && (hasPrimary==0&&hasPartial==0) && hasSuper==false){
                  AddRow((cellStyle.contains("rectangle") ? "Entity" : "Weak entity"),getName,"Entity has no Unique Attributes to be used as " + (cellStyle.contains("rectangle") ? "primary" : "partial") + " key"); 
                }
                else if (cellStyle.contains("rectangle") && hasUnique>1 && hasPrimary==0 && hasSuper==false) {
                  AddRow("Entity",getName,"Entity has multiple unique attributes, please specify the primary key"); 
                }
                else if (cellStyle.contains("doubleRect") && hasUnique>1 && hasPartial==0 ) {
                  AddRow("Weak entity",getName,"Weak entity has multiple unique attributes, please specify the partial key"); 
                }
                if (cellStyle.contains("rectangle") && hasPrimary>1 && hasSuper==false) {
                  AddRow("Entity",getName,"Entity has multiple primary attributes, please specify only one primary key"); 
                }
                if (cellStyle.contains("doubleRect") && hasPartial>1) {
                  AddRow("Weak entity",getName,"Weak entity has multiple partial attributes, please specify only one partial key"); 
                }
                if (cellStyle.contains("rectangle") && hasPartial!=0 && hasSuper==false) {
                  AddRow("Entity",getName,"Entity has partial key"); 
                }
                if (cellStyle.contains("doubleRect") && hasPrimary!=0) {
                  AddRow("Weak entity",getName,"Weak entity has primary key"); 
                }
           }

            //weak entities
            if( cellStyle.contains("doubleRect")) { 
               Boolean hasWS = false;
               Boolean noIdent = true;
               Object[]  currentEdges1 =  graph.getEdges(cell); 
            
              
             for(Object cE:currentEdges1){
              mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
              String sourceStyle = edgeSource.getStyle();
              if(sourceStyle.contains("doubleRh")) {
                noIdent = false;
              }
              else noIdent=true;
             }

              if(noIdent==true) {
                AddRow("Weak entity",getName,"Weak entity does not have identifying relationship"); 
              }

              if (cell.hasOwner()){
                  mxCell ownerC = (mxCell) cell.getOwner();
                  if(ownerC.getStyle().contains ("rectangle")||ownerC.getStyle().contains ("doubleRect") ) {
                    hasWS= true;
                  }
              } 
              if (hasWS==true){
                  AddRow("Weak entity",getName,"Weak entities cannot have superclasses"); 
              }
            } 

           

           //attributes 
           else if(cellStyle.contains("ellipse")||cellStyle.contains("ellipse;fontSize")||(cellStyle.contains("partial"))|| 
             cellStyle.contains("primary")||(cellStyle.contains("unique"))||
             cellStyle.contains("doubleEl")||cellStyle.contains("un_doubleEl")|| 
             cellStyle.contains("dashed")||cellStyle.contains("ellipse;fontStyle=0")) {
                
                for (Object cc : cells) {
                  mxCell cellC = (mxCell) cc;
                 
                  if(cell.hasOwner()==true && cell.getOwner().equals(cellC.getOwner()) && !cellC.equals(cell) && cellC.getValue().equals(cell.getValue())) {
                    AddRow("Attribute",getName,"Attributes belonging to the same element cannot have the same name"); 
                  }
                }
                if (cell.hasOwner()==false){
                  AddRow("Attribute",getName,"Attribute does not belong to anything"); 
              }
               if (cell.hasOwner()==true) {
                      mxCell owner = (mxCell) cell.getOwner();
                      if(owner.getStyle().contains("primary") || owner.getStyle().contains("unique") ) {
                        owner.setNotNull(true);
                      }
                      if (owner.getStyle().contains("llipse")) {
                        if( owner.getNotNull()==true && cell.getNotNull()==false ) {
                          AddRow("Attribute",getName,"  Attribute must be set to NOT NULL as the one that it belongs to"); 
                        }
                      }                
               }
          }
          
          //relationships
          if(cellStyle.contains("rhombus")||cellStyle.contains("doubleRh")) { 
            for (Object cc : cells) {
             mxCell cellC = (mxCell) cc;
             if( !cellC.equals(cell) && (cellC.getStyle().contains("rhombus")||cellC.getStyle().contains("doubleRh"))  && cellC.getValue().equals(cell.getValue())) {
               AddRow("Relationship",getName,"Relationship has duplicate name with another"); 
             }
           }
            int i=0;
            int strongCon=0;
            int weakCon=0;
            Boolean hasTotal= false;
            Boolean hasStr1= false;
            if(cellStyle.contains("rhombus")) {
              hasTotal=true;
              hasStr1=true;
            }

            if(cell.hasTopOwner()) {
              i++;
              if(cellStyle.contains("doubleRh")&& ((mxCell)cell.getTopOwner()).getStyle().contains("rectangle")){
                strongCon++;
                Object[] currentEdge =  graph.getEdgesBetween(cell,cell.getTopOwner()); 
                // get the edge between the shapes 
                  Object currEdge = currentEdge[0];
                  String edgeValue = (String) ((mxCell) currEdge).getValue();
                  if(edgeValue.equals("1")) {
                    hasStr1=true;
                  }
              } 
              else if(cellStyle.contains("doubleRh")&&((mxCell)cell.getTopOwner()).getStyle().contains("doubleRect")){
                weakCon++;
                Object[] currentEdge =  graph.getEdgesBetween(cell,cell.getTopOwner()); 
			        // get the edge between the shapes 
			          Object currEdge = currentEdge[0];
                String edgeStyle = ((mxCell) currEdge).getStyle();
               
                if (edgeStyle.equals("strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=0;exitPerimeter=1")) {
                  hasTotal = true;
              
                }
              } 
            }
            if(cell.hasLeftOwner()) {
              i++;
              if(cellStyle.contains("doubleRh")&&((mxCell) cell.getLeftOwner()).getStyle().contains("rectangle")){
                strongCon++;
                Object[] currentEdge =  graph.getEdgesBetween(cell,cell.getLeftOwner()); 
                // get the edge between the shapes 
                  Object currEdge = currentEdge[0];
                  String edgeValue = (String) ((mxCell) currEdge).getValue();
                  if(edgeValue.equals("1")) {
                    hasStr1=true;
                  }
              } 
              else if(cellStyle.contains("doubleRh")&&((mxCell) cell.getLeftOwner()).getStyle().contains("doubleRect")){
                weakCon++;

                Object[] currentEdge =  graph.getEdgesBetween(cell,cell.getLeftOwner()); 
                // get the edge between the shapes 
                  Object currEdge = currentEdge[0];
                  String edgeStyle = ((mxCell) currEdge).getStyle();
                  if (edgeStyle.equals("strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0;exitY=0.5;exitPerimeter=1")) {
                    hasTotal = true;
                
                  }
              } 
            }
            if(cell.hasRightOwner()) {
              i++;
              if(cellStyle.contains("doubleRh")&&((mxCell) cell.getRightOwner()).getStyle().contains("rectangle")){
                strongCon++;
                Object[] currentEdge =  graph.getEdgesBetween(cell,cell.getRightOwner()); 
                // get the edge between the shapes 
                  Object currEdge = currentEdge[0];
                  String edgeValue = (String) ((mxCell) currEdge).getValue();
                  if(edgeValue.equals("1")) {
                    hasStr1=true;
                  }
              } 
              else if(cellStyle.contains("doubleRh")&&((mxCell) cell.getRightOwner()).getStyle().contains("doubleRect")){
                weakCon++;
                Object[] currentEdge =  graph.getEdgesBetween(cell,cell.getRightOwner()); 
                // get the edge between the shapes 
                  Object currEdge = currentEdge[0];
                  String edgeStyle = ((mxCell) currEdge).getStyle();
                  if (edgeStyle.equals("strokeWidth=5;"+"spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=1;exitY=0.5;exitPerimeter=1")) {
                    hasTotal = true;
                 
                  }
              } 
            }
            if(cell.hasBottomOwner()) {
              i++;
              if(cellStyle.contains("doubleRh")&&((mxCell) cell.getBottomOwner()).getStyle().contains("rectangle")){
                strongCon++;
                Object[] currentEdge =  graph.getEdgesBetween(cell,cell.getBottomOwner()); 
                // get the edge between the shapes 
                  Object currEdge = currentEdge[0];
                  String edgeValue = (String) ((mxCell) currEdge).getValue();
                  if(edgeValue.equals("1")) {
                    hasStr1=true;
                  }
              } 
              else if(cellStyle.contains("doubleRh")&&((mxCell) cell.getBottomOwner()).getStyle().equals("doubleRect")){
                weakCon++;
                Object[] currentEdge =  graph.getEdgesBetween(cell,cell.getBottomOwner()); 
                // get the edge between the shapes 
                  Object currEdge = currentEdge[0];
                  String edgeStyle = ((mxCell) currEdge).getStyle();
                  if (edgeStyle.equals("strokeWidth=5;"+ "spacingLeft=20;"+"textPosition=right;"+"fontSize=20;"+"exitX=0.5;exitY=1;exitPerimeter=1")) {
                    hasTotal = true;
                  
                  }
              } 
            }
            
            if(i==0) {
              AddRow((cellStyle.contains("rhombus") ? "Relationship" : "Identifying relationship"),getName,"Relationship does not connect to an entity"); 
            }
            if(i==1) {
              AddRow((cellStyle.contains("rhombus") ? "Relationship" : "Identifying relationship"),getName,"Relationship must be connected to at least 2 entities"); 
            }
            if(cellStyle.contains("doubleRh") && weakCon!=1) {
              AddRow("Identifying relationship",getName,"Identifying relationship must be connected to exactly one weak entity"); 
            }
            if(cellStyle.contains("doubleRh")&& strongCon<1) {
              AddRow("Identifying relationship",getName,"Identifying relationship must be connected to at least one strong entity"); 
            }

            if (hasTotal==false){
              AddRow("Identifying relationship",getName,"Identifying relationship must be connected to a weak entity through total participation"); 
            }

            if (hasStr1==false){
              AddRow("Identifying relationship",getName,"Identifying relationship must be connected to all owner entities using '1' cardinality ratio"); 
            }

          }

          //weak relationships
          if(cellStyle.contains("doubleRh")) {
            Object[] currentEdges =  graph.getEdges(cell); 
            Boolean hasAttri = false;
            for(Object cE:currentEdges){
              mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
              String sourceStyle = edgeSource.getStyle();

              if(sourceStyle.contains("ellipse")||cellStyle.contains("ellipse;fontSize")||(sourceStyle.contains("partial"))|| 
              sourceStyle.contains("primary")||(sourceStyle.contains("unique"))||
              sourceStyle.contains("doubleEl")||sourceStyle.contains("un_dl")|| 
              sourceStyle.contains("dashed")||sourceStyle.contains("ellipse;fontStyle=0")) {
                hasAttri=true;               
              }
            }
            
            if (hasAttri==true){
              AddRow("Identifying relationship",getName,"Identifying relationships cannot have attributes"); 
            }
          }

          //specialization
          if (cellStyle.contains("d_circle")||cellStyle.contains("o_circle")) {
            if (cell.hasOwner()==false){
              AddRow("Specialization",getName,"Specialization has no superclass"); 
            }
            if(cell.ssList.size() < 2) {
              AddRow("Specialization",getName,"Specializations must have at least 2 subclasses"); 
            }
            
          }

           //union
           if (cellStyle.contains("U_circle")) {
            if (cell.hasOwner()==false){
              AddRow("Union",getName,"Union has no subclass"); 
            }
            if (cell.hasOwner()==true){
              mxCell ownerU = (mxCell) cell.getOwner();
              String ownerName =  (String) ownerU.getValue();
              if (ownerU.getStyle().contains("doubleRect")){
                AddRow("Weak Entity",ownerName,"Weak entities cannot have superclasses"); 
              }
          }

            if(cell.ssList.size() < 2) {
              AddRow("Union",getName,"Unions must have at least 2 superclasses"); 
            }
            if(cell.ssList.size() >0) {
              if(cell.ssList.contains("doubleRect")){
                mxCell wname = (mxCell) cell.getSS("doubleRect");
                String wValue =  (String) wname.getValue();
                AddRow("Weak Entity",wValue,"Weak entities cannot be superclasses"); 
              }
            }
           
          }

        }
        
        //check if the graph has errors
         countRows();
         if (hasErrors==false) {
          JOptionPane.showMessageDialog(null, "No errors found"); 
         }
         
       
        
     //   graph.setMultiplicities(multiplicities);
        //graph.setAllowDanglingEdges(false);
        graphComponent.setConnectable(true);
		   graphComponent.setToolTips(true);
        
        // Enables rubberband selection
        new mxRubberband(graphComponent);
        //graph.setMultigraph(true);


        
        //Add the scroll pane to this panel.
        frame.add(scrollPane);   
       // frame.add(table);
        //frame.setSize(700,80);
       frame.setLocation(550 , 10);  
       // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Display the window.
        frame.pack();
       // frame.setVisible(true);
    }

    //Add a row to the table of errors
    public void AddRow(String str1, String str2, String str3){
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addRow(new Object[]{str1, str2, str3});
    }


    private void initColumnSizes(JTable table) {
         TableColumn column = null;      
        for (int i = 0; i < 3; i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 2) {
                column.setPreferredWidth(500); //third column is bigger
            } else {
                column.setPreferredWidth(100);
            }
        }
    }
    
    boolean countRows(){
      int rows =  table.getRowCount(); 
      if (rows>=1){
        hasErrors = true;
        frame.setVisible(true);
      }
      return hasErrors;
    }
  
}
