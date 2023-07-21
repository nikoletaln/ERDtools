package com.mxgraph.thesis.swing.editor;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.cert.X509CRLEntry;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.thesis.swing.editor.Convertor.Foreign;
import com.mxgraph.thesis.swing.editor.Convertor.LineComponent;
import com.mxgraph.thesis.swing.editor.Convertor.Table;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;


public class Convertor extends JFrame {
  
   ArrayList<Table> tables = new ArrayList<>();

   public Convertor(mxGraphComponent graphComponent, mxGraph graph, boolean convertToSQL ) {

        if (new CheckErrors(graphComponent,graph).hasErrors == false) {
            mxAnalysisGraph aGraph = new mxAnalysisGraph();
            aGraph.setGraph(graph);

            //for every cell
            Object[] cells = graph.getChildVertices(graph.getDefaultParent());
            for (Object c : cells) {
               mxCell cell = (mxCell) c;
                //get every cell's value
               String getName =  (String) cell.getValue(); 
                //get every cell's style
               String cellStyle = cell.getStyle();
             
               //entities
               if( cellStyle.contains("rectangle")) {   
                Table table = new Table(getName, cell);
                Object[] currentEdges =  graph.getEdges(cell); 
                for(Object cE:currentEdges){
                  mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
                  String sourceStyle = edgeSource.getStyle();
                  //if entity is a sub of a specialization
                  if(sourceStyle.contains ("o_circle")||sourceStyle.contains ("d_circle")) {
                    if(edgeSource.getOwner()!=cell  && cell.hasOwner()==false) {
                      //get the specialization's superclass
                      mxCell subOwner = (mxCell) edgeSource.getOwner();
                      //and make it our current entity's owner
                      cell.setOwner(subOwner);
                    }
                  }
                }

                for(Object cE:currentEdges){
                  mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
                  String sourceStyle = edgeSource.getStyle();
                  //to see if the attribute is combined 
                  Object[] getEdges =  graph.getEdges(edgeSource);
                
                  //find the simple attributes
                  if(sourceStyle.contains ("ellipse")||sourceStyle.contains("ellipse;fontSize")||cellStyle.contains("ellipse;fontStyle=0")) {
                    //add to the attribute table
                    table.attributes.add(edgeSource);
                    for(Object cEdge : getEdges){
                      mxCell eSource =  (mxCell) aGraph.getTerminal(cEdge, true); 
                      String eStyle = eSource.getStyle();
                      //if attribute is composite
                      if(eSource.getOwner()==edgeSource &&(eStyle.contains("ellipse")||eStyle.contains("ellipse;fontSize")||eStyle.contains("ellipse;fontStyle=0"))) {
                        //remove the attribute
                         table.attributes.remove(edgeSource);
                         //and add the simple attributes
                         table.attributes.add(eSource);
                      }
                    }
                  }
                   
                  //find the uniques
                  if(sourceStyle.contains ("unique")) { 
                    edgeSource.setNotNull(true);
                    table.attributes.add(edgeSource);
                    table.Unique.add(edgeSource);
                    for(Object cEdge : getEdges){
                      mxCell eSource =  (mxCell) aGraph.getTerminal(cEdge, true); 
                      String eStyle = eSource.getStyle();
                      if( eSource.getOwner()==edgeSource &&(eStyle.contains("ellipse")||eStyle.contains("ellipse;fontSize")||eStyle.contains("ellipse;fontStyle=0"))) {
                        eSource.setNotNull(true);
                        table.attributes.remove(edgeSource);
                        table.Unique.remove(edgeSource);
                        table.attributes.add(eSource);
                        table.Unique.add(eSource);
                      }
                    }
                  }
                }
                
                //Primary key 
                ArrayList<mxCell> PK = null;
                //if entity has a superclass
                if (cell.hasOwner()){
                  mxCell ownerC = (mxCell) cell.getOwner();
                  //find the primary key of the superclass
                  PK = PrimaryKey(ownerC, graph, aGraph);
                  for (mxCell object : PK) {
                   // mxCell newpk = new mxCell();
                    object.setNotNull(true);
                  //  newpk.setValue(object.getValue());
                   // newpk.setDataType(object.getDataType());
                  //  newpk.setStyle(object.getStyle());
                    table.primaryKey.add(object);

                    //add it to the attribute list if it's not multivalued
                    if(!object.getStyle().contains("un_dl")){
                      table.attributes.add(object);
                    }
                  }
 
                  Foreign f = new Foreign();
                  f.List.addAll(table.primaryKey);
                  //refer to the superclass
                  f.refersTo = ownerC;
                  //referential integrity constraint
                  f.constraint = "ON DELETE CASCADE ON UPDATE CASCADE" ;
                  table.foreignKey.add(f);  
                }

                //no superclass found
                else{
                  PK = PrimaryKey(cell, graph, aGraph);
                  for (mxCell object : PK) {
                    object.setNotNull(true);
                    table.primaryKey.add(object);
                    //add it to the attribute list if it's not multivalued
                    if(!object.getStyle().contains("un_dl")){
                      table.attributes.add(object);
                    }
                  }
                }

                //if current entity is a superclass of a union
                for(Object cE:currentEdges){
                 mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
                 String sourceStyle = edgeSource.getStyle();
                 if(sourceStyle.contains ("U_circle") && edgeSource.getOwner()!=cell) {
                    Foreign f = new Foreign();
                    mxCell unionSub =  (mxCell) edgeSource.getOwner();
                    PK = PrimaryKey(unionSub, graph, aGraph);
                    for (mxCell object : PK) {
                      mxCell fk = new mxCell();
                      fk.setNotNull(false);
                      String pkv = (String) object.getValue(); 
                      fk.setValue("FK"+ (table.foreignKey.size() + 1) + "_"+ pkv);
                      fk.setDataType(object.getDataType());
                      f.List.add(fk);
                    }
                    f.refersTo = unionSub;
                    f.constraint =  "ON DELETE SET NULL ON UPDATE CASCADE" ;
                    table.attributes.addAll(f.List);
                    table.foreignKey.add(f);
                  }
                }
                
                //if key is in the unique list remove it
                for(mxCell priCell : table.primaryKey){
                  if(table.Unique.contains(priCell)){
                    table.Unique.remove(priCell);
                    table.attributes.remove(priCell);
                  }
                }
                //add the entity to the table list
                tables.add(table);
              }
            }//end of entities 
            

            //weak entities
            for (Object c : cells) {
              mxCell cell = (mxCell) c;
               //get every cell's value
              String getName =  (String) cell.getValue(); 
               //get every cell's style
              String cellStyle = cell.getStyle();

              if(cellStyle.contains("doubleRect")) {   
                Boolean hasPartial = false;
                Table table = new Table(getName, cell);
                Object[] currentEdges =  graph.getEdges(cell); 
                for(Object cE:currentEdges){
                  mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
                  String sourceStyle = edgeSource.getStyle();
                  //to see if the attribute is combined 
                  Object[] getEdges =  graph.getEdges(edgeSource);

                  //find the simple attributes
                  if(sourceStyle.contains ("ellipse")||sourceStyle.contains("ellipse;fontSize")||cellStyle.contains("ellipse;fontStyle=0")) {
                    //add to the attribute table
                    table.attributes.add(edgeSource);
                    for(Object cEdge : getEdges){
                      mxCell eSource =  (mxCell) aGraph.getTerminal(cEdge, true); 
                      String eStyle = eSource.getStyle();
                      //if attribute is composite
                      if(eSource.getOwner()==edgeSource &&(eStyle.contains("ellipse")||eStyle.contains("ellipse;fontSize")||eStyle.contains("ellipse;fontStyle=0"))) {
                        //remove the attribute
                         table.attributes.remove(edgeSource);
                         //and add the simple attributes
                         table.attributes.add(eSource);
                      }
                    }
                  }
                    
                  //find the uniques
                  if(sourceStyle.contains ("unique")) { 
                    edgeSource.setNotNull(true);
                    table.attributes.add(edgeSource);
                    table.Unique.add(edgeSource);
                    for(Object cEdge : getEdges){
                      mxCell eSource =  (mxCell) aGraph.getTerminal(cEdge, true); 
                      String eStyle = eSource.getStyle();
                      if( eSource.getOwner()==edgeSource &&(eStyle.contains("ellipse")||eStyle.contains("ellipse;fontSize")||eStyle.contains("ellipse;fontStyle=0"))) {
                        eSource.setNotNull(true);
                        table.attributes.remove(edgeSource);
                        table.Unique.remove(edgeSource);
                        table.attributes.add(eSource);
                        table.Unique.add(eSource);
                      }
                    }
                  }

                  //partial key
                  if(sourceStyle.contains ("partial")) { 
                    hasPartial=true;
                    edgeSource.setNotNull(true);
                    table.primaryKey.add(edgeSource);
                    table.attributes.add(edgeSource);
                  } 
                }

                //if no partial then unique
                if(hasPartial==false) {
                  for(mxCell uniCell : table.Unique){
                    //remove it from the uniques
                    table.Unique.remove(uniCell);
                    //and add it to the primaries
                    table.primaryKey.add(uniCell);
                  }
                }

                //key of strong entity
                for(Object cE:currentEdges){
                  mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
                  String sourceStyle = edgeSource.getStyle();
                  //find the weak relationship
                  if(sourceStyle.contains ("doubleRh")) {
                    //find the primary keys of every strong entity
                    Object[] getEdges =  graph.getEdges(edgeSource);
                    for(Object cEdge : getEdges){
                      mxCell eSource =  (mxCell) aGraph.getTerminal(cEdge, false); 
                      String eStyle = eSource.getStyle();
                      //strong entities
                      if(eStyle.contains ("rectangle")) {
                        Table table1 = getTableByCell(eSource, tables);
                        Foreign f = new Foreign();
                        for(mxCell pri:table1.primaryKey) { 
                            mxCell newpk = new mxCell();
                            newpk.setNotNull(true);
                            newpk.setValue("FK"+ (table.foreignKey.size() + 1) + "_" +pri.getValue());
                            newpk.setDataType(pri.getDataType());
                            newpk.setStyle(pri.getStyle());
                           // table.primaryKey.add(newpk);
                            f.List.add(newpk);
                        }
                        f.refersTo = eSource;
                        f.constraint =  "ON DELETE CASCADE ON UPDATE CASCADE" ;
                        table.attributes.addAll(f.List);
                        table.primaryKey.addAll(f.List);
                        table.foreignKey.add(f);
                     }
                   }
                 }

                }//end of key of strong entities

                //add the weak entity to the table list
                tables.add(table);
              }

            }//end of weak entities

            //relationships 1:1 or 1:N type
            for (Object c : cells) {
              mxCell cell = (mxCell) c;
              //get every cell's style
              String cellStyle = cell.getStyle();
              if (cellStyle.contains("rhombus")) { 
                Boolean type1= false;
                Object[] currentEdges =  graph.getEdges(cell); 
                for (Object cE:currentEdges) {
                  String edgeValue = (String) ((mxCell) cE).getValue();
                  //1:1 or 1:N type
                  if (edgeValue.equals("1")) {
                    type1=true;
                  }
                }

                if (currentEdges.length==2 && type1==true) { 

                  Table table = null;
                  mxCell entity = null;
                  Boolean totalOrN = false;
                  for (Object cE:currentEdges) {
                    String edgeStyle = ((mxCell) cE).getStyle();
                    String edgeValue = (String) ((mxCell) cE).getValue();
                    //N or total 
                    if (edgeStyle.contains("strokeWidth=5;") || !edgeValue.equals("1")) { 
                      //find the entity
                      entity =  (mxCell) aGraph.getTerminal(cE, false); 
                      //find the entity's table 
                      table = getTableByCell(entity, tables);
                      totalOrN=true;
                    }   
                  } 
                  if (totalOrN==false) {
                    entity =  (mxCell) aGraph.getTerminal(currentEdges[0], false);
                    table = getTableByCell(entity, tables);
                  }

                  //attributes of the relationship
                  for (Object cE:currentEdges) {
                    mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
                    String sourceStyle = edgeSource.getStyle();
                   
                    if(sourceStyle.contains ("ellipse")||cellStyle.contains("ellipse;fontSize")||cellStyle.contains("ellipse;fontStyle=0")) {
                      int counter = 0;
                      Boolean check = false;
                      while (!check) {
                      counter++;
                      check = true;
                      for(mxCell atr : table.attributes) { 
                        if (((String) edgeSource.getValue()).equalsIgnoreCase(atr.getValue() + ((counter > 1) ? Integer.toString(counter) : "") )) {
                          check = false;
                          break;
                        }
                      }
                     }
                     mxCell temp = new mxCell();
                     temp.setDataType(edgeSource.getDataType());
                     temp.setStyle(edgeSource.getStyle());
                     temp.setNotNull(edgeSource.getNotNull());
                     temp.setValue(edgeSource.getValue() + ((counter > 1) ? Integer.toString(counter) : ""));
                     //add the attributes of the relationship to the entity's table 
                     table.attributes.add(temp);
                    }
                  }

                  Foreign f = new Foreign();
                  int counter1 = table.foreignKey.size() + 1;
                  Table table1 = null;
                  mxCell entity1 = null;
                  if (totalOrN==true) {
                    for (Object cE:currentEdges) {
                      
                      String edgeValue = (String) ((mxCell) cE).getValue();
                      //find the other entity
                      if (edgeValue.equals("1")) { 
                        entity1 =  (mxCell) aGraph.getTerminal(cE, false); 
                        table1 = getTableByCell(entity1, tables);
                      }
                    }
                  }
                  else if (totalOrN==false) {
                    entity1 =  (mxCell) aGraph.getTerminal(currentEdges[1], false);
                    table1 = getTableByCell(entity1, tables);
                  }

                  for(mxCell pri:table1.primaryKey) { 
                    mxCell newpk = new mxCell();
                    newpk.setNotNull(totalOrN);
                    newpk.setValue("FK"+ counter1 + "_" +pri.getValue());
                    newpk.setDataType(pri.getDataType());
                    newpk.setStyle(pri.getStyle());
                    f.List.add(newpk);
                  }
                  f.refersTo = entity1;
                  f.constraint =  "ON DELETE SET NULL ON UPDATE CASCADE" ;
                  table.attributes.addAll(f.List);
                  table.foreignKey.add(f);
                }
              }
            } //end of relationships 1:1 or 1:N type

            //relationships N:M type
            for (Object c : cells) {
              mxCell cell = (mxCell) c;
              //get every cell's style
              String cellStyle = cell.getStyle();
              if (cellStyle.contains("rhombus")) { 
                Boolean type1= false;
                Object[] currentEdges =  graph.getEdges(cell); 
                for (Object cE:currentEdges) {
                  String edgeValue = (String) ((mxCell) cE).getValue();
                  //1:1 or 1:N type
                  if (edgeValue.equals("1")) {
                    type1=true;
                  }
                }
                //N:M or 1:N:M or K:N:M etc
                if (currentEdges.length!=2 || type1==false) { 
                  Table table = null;
                  int counter = 0;
                  Boolean check = false;
                  while (!check) {
                    counter++;
                    check = true;
                    for(Table t : tables) { 
                      if (((String) cell.getValue()).equalsIgnoreCase(t.name + ((counter > 1) ? Integer.toString(counter) : "") )) {
                        check = false;
                        break;
                      }
                    }
                  }
                  table = new Table( (String)(cell.getValue() + ((counter > 1) ? Integer.toString(counter) : "")), cell);
                  //add the attributes
                  for(Object cE:currentEdges) {
                    mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
                    String sourceStyle = edgeSource.getStyle();
                    Object[] getEdges =  graph.getEdges(edgeSource);

                    //find the simple attributes
                    if(sourceStyle.contains ("ellipse")||sourceStyle.contains("ellipse;fontSize")||cellStyle.contains("ellipse;fontStyle=0")) {
                      //add to the attribute table
                      table.attributes.add(edgeSource);
                      for(Object cEdge : getEdges) {
                        mxCell eSource =  (mxCell) aGraph.getTerminal(cEdge, true); 
                        String eStyle = eSource.getStyle();
                        //if attribute is composite
                        if(eSource.getOwner()==edgeSource &&(eStyle.contains("ellipse")||eStyle.contains("ellipse;fontSize")||eStyle.contains("ellipse;fontStyle=0"))) {
                          //remove the attribute
                           table.attributes.remove(edgeSource);
                           //and add the simple attributes
                           table.attributes.add(eSource);
                        }
                      }
                    }

                    //find the uniques/primaries
                    if(sourceStyle.contains ("unique") ||(sourceStyle.contains ("primary"))) { 
                      edgeSource.setNotNull(true);
                      table.attributes.add(edgeSource);
                      table.Unique.add(edgeSource);
                      for(Object cEdge : getEdges) {
                        mxCell eSource =  (mxCell) aGraph.getTerminal(cEdge, true); 
                        String eStyle = eSource.getStyle();
                        if( eSource.getOwner()==edgeSource &&(eStyle.contains("ellipse")||eStyle.contains("ellipse;fontSize")||eStyle.contains("ellipse;fontStyle=0"))) {
                          eSource.setNotNull(true);
                          table.attributes.remove(edgeSource);
                          table.Unique.remove(edgeSource);
                          table.attributes.add(eSource);
                          table.Unique.add(eSource);
                        }
                      }
                    }
                  }
                  
                  ArrayList<mxCell> list = new ArrayList<mxCell>();
                  for (Object cE:currentEdges) {
                    Foreign f = new Foreign();
                    int counter1 = table.foreignKey.size() + 1;
                    Table table1 = null;
                    mxCell entity = null;
                    String edgeValue = (String) ((mxCell) cE).getValue();
                    mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, false); 
                    String sourceStyle = edgeSource.getStyle();
                    if (sourceStyle.contains("rectangle")) {
                      entity =  (mxCell) aGraph.getTerminal(cE, false); 
                      table1 = getTableByCell(entity, tables);
                      for(mxCell pri:table1.primaryKey) { 
                        mxCell newpk = new mxCell();
                        newpk.setNotNull(true);
                        newpk.setValue("FK"+ counter1 + "_" +pri.getValue());
                        newpk.setDataType(pri.getDataType());
                        newpk.setStyle(pri.getStyle());
                        f.List.add(newpk);
                      }
                      f.refersTo = entity;
                      f.constraint =  "ON DELETE CASCADE ON UPDATE CASCADE" ;
                      table.attributes.addAll(f.List);
                      if (!edgeValue.equals("1")) {
                        table.primaryKey.addAll(f.List);
                      }
                      list.addAll(f.List);
                      table.foreignKey.add(f);
                    }
                  } 
                  //if no primary keys found
                  if ( table.primaryKey.size() == 0) {
                    Boolean b = true;
                    for (mxCell l : list){
                      if(b) {
                        table.primaryKey.add(l);
                        b=false;
                      }
                      else {
                        table.Unique.add(l);
                      }
                    }
                  }

                tables.add(table);  
                }                   
              }
            } //end of relationships N:M type

            //multivalued
            for (Object c : cells) {
              mxCell cell = (mxCell) c;
              String getName =  (String) cell.getValue(); 
              String cellStyle = cell.getStyle();
              Object[] currentEdges =  graph.getEdges(cell);
              if(cellStyle.contains("doubleEl")||cellStyle.contains ("un_dl")) {  
                cell.setNotNull(true);
                for(Object cE:currentEdges){
                  mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
                  String sourceStyle = edgeSource.getStyle();
                  if(sourceStyle.contains ("ellipse")||sourceStyle.contains("ellipse;fontSize")||sourceStyle.contains("ellipse;fontStyle=0")) {
                    edgeSource.setNotNull(true);
                  }
                }
                Table table = null;
                int counter = 0;
                Boolean check = false;
                while (!check) {
                  counter++;
                  check = true;
                  for(Table t : tables) { 
                    if (((String) cell.getValue()).equalsIgnoreCase(t.name + ((counter > 1) ? Integer.toString(counter) : "") )) {
                      check = false;
                      break;
                    }
                  }
                }
                table = new Table( (String)(cell.getValue() + ((counter > 1) ? Integer.toString(counter) : "")), cell);
                //add the attributes
                for(Object cE:currentEdges) {
                  mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
                  String sourceStyle = edgeSource.getStyle();
                  Object[] getEdges =  graph.getEdges(edgeSource);

                  //find the simple attributes
                  if(sourceStyle.contains ("ellipse")||sourceStyle.contains("ellipse;fontSize")||sourceStyle.contains("ellipse;fontStyle=0")) {
                    //add to the attribute table
                    table.attributes.add(edgeSource);
                    for(Object cEdge : getEdges) {
                      mxCell eSource =  (mxCell) aGraph.getTerminal(cEdge, true); 
                      String eStyle = eSource.getStyle();
                      //if attribute is composite
                      if(eSource.getOwner()==edgeSource &&(eStyle.contains("ellipse")||eStyle.contains("ellipse;fontSize")||eStyle.contains("ellipse;fontStyle=0"))) {
                        //remove the attribute
                         table.attributes.remove(edgeSource);
                         //and add the simple attributes
                         table.attributes.add(eSource);
                      }
                    }
                  }
                  //find the uniques/primaries
                  if(sourceStyle.contains ("unique") ||(sourceStyle.contains ("primary"))) { 
                    edgeSource.setNotNull(true);
                    table.attributes.add(edgeSource);
                    table.Unique.add(edgeSource);
                    for(Object cEdge : getEdges) {
                      mxCell eSource =  (mxCell) aGraph.getTerminal(cEdge, true); 
                      String eStyle = eSource.getStyle();
                      if( eSource.getOwner()==edgeSource &&(eStyle.contains("ellipse")||eStyle.contains("ellipse;fontSize")||eStyle.contains("ellipse;fontStyle=0"))) {
                        eSource.setNotNull(true);
                        table.attributes.remove(edgeSource);
                        table.Unique.remove(edgeSource);
                        table.attributes.add(eSource);
                        table.Unique.add(eSource);
                      }
                    }
                  }
                }
                if (table.attributes.size()==0) {
                  table.attributes.add(cell);
                }
                table.primaryKey.addAll(table.attributes);
                Foreign f = new Foreign();
                int counter1 = table.foreignKey.size() + 1;
                mxCell ref = null;
                Table tab = null;
                for (Object cE:currentEdges) { 
                  mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, false); 
                  String sourceStyle = edgeSource.getStyle();
                  //if multivalued belongs to an entity
                  if (sourceStyle.contains("rectangle")||sourceStyle.contains("doubleRect")) {
                    ref =  edgeSource; 
                    tab = getTableByCell(ref, tables);
                    for(mxCell pri:tab.primaryKey) { 
                      mxCell newpk = new mxCell();
                      newpk.setNotNull(true);
                      newpk.setValue("FK"+ counter1 + "_" +pri.getValue());
                      newpk.setDataType(pri.getDataType());
                      newpk.setStyle(pri.getStyle());
                      f.List.add(newpk);
                    }
                    f.refersTo = ref;
                    f.constraint =  "ON DELETE CASCADE ON UPDATE CASCADE" ;
                    table.attributes.addAll(f.List);
                  }

                  //if multivalued belongs to a relationship 
                  else if (sourceStyle.contains("rhombus")) {
                    Boolean type1= false;
                    Object[] cEdges =  graph.getEdges(edgeSource); 
                    for (Object cEdge:cEdges) {
                      String edgeValue = (String) ((mxCell) cEdge).getValue();
                      //1:1 or 1:N type
                      if (edgeValue.equals("1")) {
                        type1=true;
                      }
                    }
                    //1:1 or 1:N type
                    if (currentEdges.length==2 && type1==true) { 
                      Boolean totalOrN = false;
                      for (Object cEdge:cEdges) {
                        String edgeStyle = ((mxCell) cEdge).getStyle();
                        String edgeValue = (String) ((mxCell) cEdge).getValue();
                        //N or total 
                        if (edgeStyle.contains("strokeWidth=5;") || !edgeValue.equals("1")) { 
                          //find the entity
                          ref =  (mxCell) aGraph.getTerminal(cEdge, false); 
                          //find the entity's table
                          tab = getTableByCell(ref, tables);
                          totalOrN=true;
                        }   
                      } 
                      if (totalOrN==false) {
                        ref =  (mxCell) aGraph.getTerminal(currentEdges[0], false);
                        tab = getTableByCell(ref, tables);
                      }
                      for(mxCell pri:tab.primaryKey) { 
                        mxCell newpk = new mxCell();
                        newpk.setNotNull(true);
                        newpk.setValue("FK"+ counter1 + "_" +pri.getValue());
                        newpk.setDataType(pri.getDataType());
                        newpk.setStyle(pri.getStyle());
                        f.List.add(newpk);
                      }
                      f.refersTo = ref;
                      f.constraint =  "ON DELETE CASCADE ON UPDATE CASCADE" ;
                      table.attributes.addAll(f.List);
                    }

                    //N:M or 1:N:M or K:N:M etc
                    else if (currentEdges.length!=2 || type1==false) { 
                      ref = edgeSource;
                      tab = getTableByCell(ref, tables);
                      for(mxCell pri:tab.primaryKey) { 
                        mxCell newpk = new mxCell();
                        newpk.setNotNull(true);
                        newpk.setValue("FK"+ counter1 + "_" +pri.getValue());
                        newpk.setDataType(pri.getDataType());
                        newpk.setStyle(pri.getStyle());
                        f.List.add(newpk);
                      }
                      f.refersTo = ref;
                      
                      f.constraint =  "ON DELETE CASCADE ON UPDATE CASCADE" ;
                    }
                  }
               }
               table.attributes.addAll(f.List);
               table.primaryKey.addAll(f.List);
               table.foreignKey.add(f);
               tables.add(table); 
              }   
            }//end of multivalued


            if (convertToSQL == false) {
              // Create a new JFrame for the relational schema
              JFrame schemaFrame = new JFrame("Relational Schema");
              schemaFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
              schemaFrame.setLayout(new BorderLayout());
          
              // Create a custom panel for drawing the schema
              JPanel schemaPanel = new JPanel() {
                  @Override
                  protected void paintComponent(Graphics g) {
                      super.paintComponent(g);
                      Graphics2D g2d = (Graphics2D) g;
                      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          
                      Map<String, Point> tablePositions = new HashMap<>();
                      Map<String, List<String>> tableAttribute = new HashMap<>();
                      int spacing = 100;
                      int x = spacing;
                      int y = spacing;
          
                      // Iterate over the tables and draw them
                      for (Table table : tables) {
                          // Draw the table rectangle
                          g2d.setColor(Color.LIGHT_GRAY);
                          int tableWidth = 200 + (table.attributes.size() * 20);
                          int tableHeight = 55 ;//+ (table.attributes.size() * 20);
                          g2d.fillRect(x, y+23, tableWidth, tableHeight-23);
                          g2d.setColor(Color.BLACK);
                          g2d.drawRect(x, y, tableWidth, tableHeight);
          
                         
                          // Draw the table name
                          g2d.setFont(new Font("Arial", Font.BOLD, 14));
                          g2d.drawString(table.name, x + 10, y + 20);
          
                          // Draw the primary key attributes and table attributes in the same line
                          g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                          int attributeX = x + 10; // Starting X position for attributes
                          int attributeY = y + 40; // Starting Y position for attributes
          
                          for (mxCell pk : table.primaryKey) {
                              String attributeName = (String) pk.getValue();
                              AttributedString attributedString = new AttributedString(attributeName);
                              attributedString.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                              attributedString.addAttribute(TextAttribute.FONT, g2d.getFont());
          
                              // Draw the underlined text
                              g2d.drawString(attributedString.getIterator(), attributeX, attributeY);
                              attributeX += g2d.getFontMetrics().stringWidth(attributeName) + 20; // Adjust spacing
                              tableAttribute.computeIfAbsent(table.name, k -> new ArrayList<>()).add(attributeName);
                               
                          }
          
                          // Draw the table attributes
                          for (mxCell attribute : table.attributes) {
                            boolean isForeignKey = false;
                            for (Foreign foreignKey : table.foreignKey) {
                              for (mxCell cell : foreignKey.List){
                                if (cell == attribute) {
                                 isForeignKey = true;
                                 break;
                                }
                              }
                            }

                            if (!isForeignKey) {
                              for (mxCell pri : table.primaryKey) {
                                if (attribute != pri) {
                                  String attributeName1 = (String) attribute.getValue();
                                  g2d.drawString(attributeName1, attributeX, attributeY);
                                  attributeX += g2d.getFontMetrics().stringWidth(attributeName1) + 20; // Adjust spacing
                                }
                              }
                            }
                          }
          
                          // Store the position of the table
                          tablePositions.put(table.name, new Point(x , y ));
                                   
                           // Update the position for the next table
                           y += tableHeight + spacing;
                      }
          
                      // Draw the relationship lines
                      g2d.setStroke(new BasicStroke(2.0f));
                      g2d.setColor(Color.BLACK);

                      for (Table table : tables) {
                          for (Foreign foreignKey : table.foreignKey) {
                              mxCell sourceCell = foreignKey.refersTo;
                              Table referencedTable = getTableByCell(sourceCell, tables);
                              if (referencedTable != null) {
                                  Point source = tablePositions.get(table.name);
                                  Point target = tablePositions.get(referencedTable.name);
                                             
                                  int sourceY = source.y + (55 / 2); // Middle Y-coordinate of the source table
                                  int sourceX = source.x +   200 + (table.attributes.size() * 20); // Right X-coordinate of the source table
                                  int targetY = target.y + (55 / 2); // Middle Y-coordinate of the target table
                                  int targetX = target.x + 200 + (referencedTable.attributes.size() * 20); // Right X-coordinate of the target table

                                  int dx = targetX - sourceX; // Difference in X-coordinates
                                  int dy = targetY - sourceY; // Difference in Y-coordinates
                                  int controlX1, controlY1, controlX2, controlY2;
                                  int distance = Math.max(Math.abs(dx), Math.abs(dy));
                                  controlX1 = sourceX + distance -100;
                                  controlY1 = sourceY -100;
                                  controlX2 = sourceX  + distance -100;
                                  controlY2 = sourceY -100;
                        
                                  // Draw curved line using cubic Bezier curve
                                  g2d.draw(new CubicCurve2D.Double(sourceX, sourceY, controlX1, controlY1, controlX2, controlY2, targetX, targetY));
                                }
                              }
                            }
                          }
                        };       
              
                // Set the preferred size of the schema panel
                schemaPanel.setPreferredSize(new Dimension(600, 600));
    
                // Create a JScrollPane to contain the schema panel
                JScrollPane scrollPane = new JScrollPane(schemaPanel);
                // Add the scroll pane to the schema frame
                schemaFrame.add(scrollPane, BorderLayout.CENTER);

                // Create a "Save" button
                JButton saveButton = new JButton("Save");
                saveButton.addActionListener(new ActionListener() {
                  @Override
                  public void actionPerformed(ActionEvent e) {
                      // Prompt the user to choose the save location
                      JFileChooser fileChooser = new JFileChooser();
                      fileChooser.setDialogTitle("Save as PNG");
                      int userSelection = fileChooser.showSaveDialog(schemaFrame);

                      if (userSelection == JFileChooser.APPROVE_OPTION) {
                          // Get the selected file
                          File fileToSave = fileChooser.getSelectedFile();
                          // Ensure the file has ".png" extension
                          if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".png")) {
                            fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
                          }

                          // Check if the file already exists
                          if (fileToSave.exists()) {
                            int response = JOptionPane.showConfirmDialog(schemaFrame, "The file already exists. Do you want to overwrite it?", "File Exists", JOptionPane.YES_NO_OPTION);
                            if (response != JOptionPane.YES_OPTION) {
                                return; // Cancel saving
                            }
                          }

                          try {
                            // Create a BufferedImage to store the schema panel content
                            BufferedImage image = new BufferedImage(schemaFrame.getWidth(), schemaFrame.getHeight(), BufferedImage.TYPE_INT_RGB);
                            Graphics2D g2d = image.createGraphics();
                            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                            // Fill the background with white to match the panel
                            g2d.setColor(Color.WHITE);
                            g2d.fillRect(0, 0, schemaPanel.getWidth(), schemaPanel.getHeight());
                            // Scale the drawing to match the window size
                            double scaleX = (double) image.getWidth() / (double) schemaPanel.getWidth();
                            double scaleY = (double) image.getHeight() / (double) schemaPanel.getHeight();
                            g2d.scale(scaleX, scaleY);
                            // Draw the panel content onto the BufferedImage
                            schemaPanel.print(g2d);
                            g2d.dispose();

                            // Save the image as PNG
                            ImageIO.write(image, "png", fileToSave);
                            JOptionPane.showMessageDialog(schemaFrame, "Schema saved as PNG successfully.", "Save", JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(schemaFrame, "Failed to save schema as PNG.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

              // Add the "Save" button to the bottom of the JFrame
              schemaFrame.add(saveButton, BorderLayout.SOUTH);          


              // Set the size and visibility of the schema frame
              schemaFrame.pack();
              schemaFrame.setVisible(true);
            }
          
            // makes the sql code
            if(convertToSQL==true) {

              String sqlString = "";
              for (Table t : tables){
               // sqlString += table.toString();
                StringBuilder sb = new StringBuilder();
                sb.append("CREATE TABLE " + t.name).append(" (\n");

                for (mxCell attribute : t.attributes) {
                  sb.append("\t").append((String) attribute.getValue()+ "\t" + attribute.getDataType() +
                   (attribute.getNotNull()==true ? ("\t"+"NOT NULL") : "") ).append(",\n");
                }
                if(t.primaryKey.size()>0){
                  sb.append("PRIMARY KEY (");
                  for (int i = 0; i < t.primaryKey.size(); i++) {
                    String val = (String) t.primaryKey.get(i).getValue();
                    sb.append(val);
                    if (i < t.primaryKey.size() - 1) {
                     sb.append(", ");
                    }
                  }
                 sb.append("),\n");
                }

                for (mxCell uk : t.Unique) {
                 sb.append("UNIQUE(").append((String)uk.getValue()).append("),\n");
                }
    
               sb.delete(sb.length() - 2, sb.length());
               sb.append(" "+ ");\n"+ "\n");
               sqlString += sb.toString();
              }

              //after declarining the tables we declare their foreign keys
              //in order not to generate errors referring to tables that have not been declared
              for (Table t : tables){
                StringBuilder s = new StringBuilder();
                if (t.foreignKey.size() > 0){
                  for (Foreign frg : t.foreignKey){
                      s.append("ALTER TABLE " + t.name + " ADD FOREIGN KEY (") ;
                     for(mxCell f : frg.List){
                       s.append(f.getValue() + ", ");
                     }
                      s.delete(s.length() - 2, s.length());
                      String ref = (String) frg.refersTo.getValue(); 
                      s.append( ") REFERENCES " + ref +" (");
        
                     for(mxCell f : frg.List){
                       String fv = (String) f.getValue() ;
                       if (fv.contains("_")) {
                         String[] parts = fv.split("_");
                         fv = parts[1];
                       }
                        s.append( fv  + ", ");
                      }
                      s.delete(s.length() - 2, s.length());
                      s.append(  ") " + frg.constraint + ";" + "\n" + "\n");  
                  }
                }
                sqlString += s.toString();
                System.out.println(sqlString);
              }
              
              //save the sql code 
              JFileChooser chooser = new JFileChooser();
              FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Document (*.txt, *.sql)", "txt", "sql");
              chooser.setFileFilter(filter);
              chooser.setDialogTitle("Save SQL");
              int returnVal = chooser.showSaveDialog(null);
              if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                String filePath = file.getPath();
                String extension = "";
                if(!filePath.toLowerCase().endsWith(".txt") && !filePath.toLowerCase().endsWith(".sql")){
                  int option = JOptionPane.showOptionDialog(null, "Do you want to save as a .txt or .sql file?", "Save as", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] {"txt", "sql"}, "txt");
                  if(option == JOptionPane.YES_OPTION) {
                      extension = ".txt";
                  } else if(option == JOptionPane.NO_OPTION) {
                      extension = ".sql";
                  }
                }
                file = new File(filePath + extension);
                if (file.exists()) {
                  int choice = JOptionPane.showConfirmDialog(null, "The file already exists. Do you want to overwrite it?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                  if (choice != JOptionPane.YES_OPTION) {
                    return;
                  }
                }
                try {
                    FileWriter writer = new FileWriter(file);
                    writer.write(sqlString);
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
              } 
            }    
          }  
    }


    public class LineComponent extends JComponent {
      private Point source;
      private Point target;
  
      public LineComponent(Point source, Point target) {
          this.source = source;
          this.target = target;
      }
  
      @Override
      protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          Graphics2D g2d = (Graphics2D) g;
          g2d.setColor(Color.BLACK);
          g2d.setStroke(new BasicStroke(2.0f));
          g2d.drawLine(source.x, source.y, target.x, target.y);
      }
  }
  

   
  

  

      //find a cell's primary key
      public ArrayList<mxCell> PrimaryKey(mxCell cell, mxGraph graph, mxAnalysisGraph aGraph) {
        ArrayList<mxCell> pKey = new ArrayList<mxCell>();
        Boolean hasPrimary=false;
        Object[] currentEdges =  graph.getEdges(cell); 
        for(Object cE:currentEdges){
         mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
         String sourceStyle = edgeSource.getStyle();
         //if the superclass is a sub of a specialization
         if(sourceStyle.contains ("o_circle")||sourceStyle.contains ("d_circle")) {
            if(edgeSource.getOwner()!=cell && cell.hasOwner()==false) {
               //get the specialization's superclass
               mxCell subOwner = (mxCell) edgeSource.getOwner();
                //and make it our current entity's owner
                cell.setOwner(subOwner);
            }
          }
        }
        //if the superclass has a superclass
        if (cell.hasOwner()){
          mxCell ownerC = (mxCell) cell.getOwner();
          //find the primary key of that superclass
          pKey.addAll(PrimaryKey(ownerC, graph, aGraph));
        }
        // else find the primary key
        else{
          for(Object cE:currentEdges){
            mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
            String sourceStyle = edgeSource.getStyle();
            //to see if the attribute is combined 
            Object[] getEdges =  graph.getEdges(edgeSource);
            if(sourceStyle.contains ("primary")) { 
              hasPrimary = true;
              //add the key to the pKey 
              pKey.add(edgeSource);
              for(Object cEdge : getEdges){
                mxCell eSource =  (mxCell) aGraph.getTerminal(cEdge, true); 
                String eStyle = eSource.getStyle();
                //if attribute is composite
                if(eSource.getOwner()==edgeSource &&(eStyle.contains("ellipse")||eStyle.contains("ellipse;fontSize")||eStyle.contains("ellipse;fontStyle=0"))) {
                  //remove the attribute from pKey
                  pKey.remove(edgeSource);
                  //and add the simple attributes
                  pKey.add(eSource);
                }
              }
            }
          } 
        }

        //if no primary key was found 
        if(hasPrimary==false){
          for(Object cE:currentEdges){
            mxCell edgeSource =  (mxCell) aGraph.getTerminal(cE, true); 
            String sourceStyle = edgeSource.getStyle();
            //to see if the attribute is combined 
            Object[] getEdges =  graph.getEdges(edgeSource);
            if(sourceStyle.contains ("unique")||sourceStyle.contains ("un_dl")) {
              hasPrimary = true;
              //add the key to the pKey 
              pKey.add(edgeSource);
              for(Object cEdge : getEdges){
                mxCell eSource =  (mxCell) aGraph.getTerminal(cEdge, true); 
                String eStyle = eSource.getStyle();
                //if attribute is composite
                if(eSource.getOwner()==edgeSource &&(eStyle.contains("ellipse")||eStyle.contains("ellipse;fontSize")||eStyle.contains("ellipse;fontStyle=0"))) {
                  //remove the attribute from pKey
                  pKey.remove(edgeSource);
                  //and add the simple attributes
                  pKey.add(eSource);
                }
              }
            }
          }
        }
        return pKey;
      }
      
   

    public class Table {
      public String name;
      public mxCell cell;
      public String Inherits = "";
      public ArrayList<mxCell> attributes;
      public ArrayList<mxCell> primaryKey;
      public ArrayList<mxCell> Unique; 
      public ArrayList<Foreign> foreignKey;
      public JPanel tableSquare;

      public Table(String name, mxCell cell) {
        this.name = name;
        this.cell = cell;
        attributes = new ArrayList<mxCell>();
        primaryKey = new ArrayList<mxCell>();
        Unique = new ArrayList<mxCell>(); 
        foreignKey = new ArrayList<Foreign>();
      }
    }

    public class Foreign{
        public ArrayList<mxCell> List = new ArrayList<mxCell>();
        public mxCell refersTo = null;
        public String constraint = "";
    }   
  
    public Table getTableByCell(mxCell cell, ArrayList<Table> tables) {
      for (Table table : tables) {
          if (table.cell == cell) {
              return table;
          }
      }
      return null;
  }
}