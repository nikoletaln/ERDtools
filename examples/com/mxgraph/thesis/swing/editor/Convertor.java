package com.mxgraph.thesis.swing.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.cert.X509CRLEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
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


            //makes the relational model
            if(convertToSQL==false) { 
              JFrame frame = new JFrame("Relational Schema");
              frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
              //frame.setSize(600, 500);
           
              //gui
              JPanel contentPanel = new JPanel();
              contentPanel.setLayout(new BoxLayout(contentPanel,  BoxLayout.Y_AXIS));
              // Create a panel for each table and add it to the main frame
              JPanel mainPanel = new JPanel(new GridBagLayout());
              GridBagConstraints gbc = new GridBagConstraints();
              gbc.weightx = 0.0;
              gbc.weighty = 0.0;
              gbc.fill = GridBagConstraints.HORIZONTAL;
              gbc.insets = new Insets(30, 5, 30, 5);
              gbc.gridx = 0;
              gbc.gridy = GridBagConstraints.RELATIVE;
              gbc.anchor = GridBagConstraints.NORTHWEST;
              // Set font size and style for labels in table panels
              Font labelFont = new Font("Arial", Font.PLAIN, 16);

              // Create a HashMap to hold the JLabels for the attributes
              HashMap<String, JLabel> attributeLabels = new HashMap<>();
              // Create a map to store the JLabel instances for each mxCell
              HashMap<mxCell, JLabel> cellToLabelMap = new HashMap<>();
              HashMap<JLabel, Point> labelLocations = new HashMap<>();

              // Add each table panel to the main panel
              for (Table table : tables) {
                JPanel tablePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                // Create a titled border with a larger font size
                Font titleFont = new Font("Arial", Font.BOLD, 18);
                // border to be titled but invisible
                TitledBorder  titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 0)),table.name,TitledBorder.LEFT,TitledBorder.ABOVE_TOP);
                titledBorder.setTitleFont(titleFont);
                tablePanel.setBorder(titledBorder);

                // Create a line border with a thickness of 1
                Border lineBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
                 // Create an empty border with a padding 
                Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
                // Combine the two borders using a compound border
                Border compoundBorder = BorderFactory.createCompoundBorder(lineBorder, emptyBorder); 

                for (mxCell pk : table.primaryKey) {
                  String lab = (String) pk.getValue();
                  JLabel pkLabel = new JLabel(lab);
                  pkLabel.setBorder(compoundBorder);
                  pkLabel.setHorizontalAlignment(JLabel.CENTER);
                  String htmlString = "<html><u style='text-underline-offset: 8px'>" + lab + "</u></html>";
                  pkLabel.setText(htmlString);
                  tablePanel.add(pkLabel);
                  Point labelLocation = pkLabel.getLocation();
                  SwingUtilities.convertPoint(pkLabel, labelLocation, pkLabel.getParent());
                  labelLocations.put(pkLabel, labelLocation);
                  cellToLabelMap.put(pk, pkLabel);
                }

                for (mxCell attribute : table.attributes) {
                  String lab = (String) attribute.getValue();
                  JLabel attributeLabel = new JLabel(lab);
                  // Add the attribute JLabel to the HashMap
                  attributeLabels.put(lab,attributeLabel);
                
                  for(mxCell pri:table.primaryKey) {
                    if( attribute != pri ){
                      attributeLabel.setBorder(compoundBorder);
                      attributeLabel.setHorizontalAlignment(JLabel.CENTER);
                      tablePanel.add(attributeLabel);
                      // Store the label's location
                      Point labelLocation = attributeLabel.getLocation();
                      SwingUtilities.convertPoint(attributeLabel, labelLocation, attributeLabel.getParent());
                      labelLocations.put(attributeLabel, labelLocation);                      
                      cellToLabelMap.put(attribute, attributeLabel);
                    }
                  }
                
                }

                // Set font for labels in table panel
                for (Component component : tablePanel.getComponents()) {
                    if (component instanceof JLabel) {
                      JLabel label = (JLabel) component;
                      label.setFont(labelFont);
                    }
                }
            
               mainPanel.add(tablePanel, gbc);
               gbc.insets = new Insets(1, 5, 1, 5);
              }

               // Add a scroll pane to the main panel
               JScrollPane scrollPane = new JScrollPane(mainPanel);
               scrollPane.setPreferredSize(new Dimension(600, 500));
               contentPanel.add(scrollPane);
               add(contentPanel);

               setSize(600, 500);
               // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
               setVisible(true);
               frame.setLocation(550 , 10);  
               frame.pack();

               // Create a new panel for the arrowed lines
JPanel linePanel = new JPanel() {
  @Override
  public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;

      // Iterate over the foreign keys and draw arrowed lines
      for (Table table : tables) {
          if (table.foreignKey.size() > 0) {
              for (Foreign foreignKey : table.foreignKey) {
                  for (mxCell cell : foreignKey.List) {
                      mxCell referredCell = foreignKey.refersTo;
                      Table referredTable = getTableByCell(referredCell, tables);
                      for (mxCell referredAttribute : referredTable.attributes) {
                          String referredAttributeName = (String) referredAttribute.getValue();
                          String sName = (String) cell.getValue();
                          if (sName.contains("_")) {
                              String[] parts = sName.split("_");
                              sName = parts[1];
                              if (sName.equals(referredAttributeName)) {
                                  // Get coordinates of labels relative to content panel
                                  Point sourceLoc = labelLocations.get(cellToLabelMap.get(cell));
                                  Point targetLoc = labelLocations.get(cellToLabelMap.get(referredAttribute));

                                  // Draw arrowed line
                                 // g2.setColor(Color.BLACK);
                                  drawArrowLine(g2, sourceLoc, targetLoc, 10, 2);

                                 // drawArrowLine(g2, sourceLoc.x + 20, sourceLoc.y + 10, targetLoc.x + 20, targetLoc.y + 10, 10, 5);
                              }
                          }
                      }
                  }
              }
          }
      }
  }
};
linePanel.setPreferredSize(new Dimension(600, 500));
scrollPane.setRowHeaderView(linePanel);
             
              // Create a button for saving the relational model as an image
              JButton saveButton = new JButton("Save as Image");
              saveButton.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
              // Create an image of the relational model
              BufferedImage image = new BufferedImage(mainPanel.getWidth(), mainPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
              Graphics2D g = image.createGraphics();
              mainPanel.paint(g);
              g.dispose();
    
              // Show a dialog for selecting the file to save the image to
              JFileChooser chooser = new JFileChooser();
              FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
              chooser.setFileFilter(filter);
              int returnVal = chooser.showSaveDialog(frame);
              if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File file = chooser.getSelectedFile();
                 String filePath = file.getPath();
                 if (!filePath.toLowerCase().endsWith(".png")) {
                   file = new File(filePath + ".png");
                 }
                 if (file.exists()) { // check if the file already exists
                  int result = JOptionPane.showConfirmDialog(frame, "The file already exists. Do you want to overwrite it?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                  if (result != JOptionPane.YES_OPTION) {
                    return; // user chose not to overwrite, do not save the file
                  }
                }
                try {
                ImageIO.write(image, "png", file);
                } catch (IOException ex) {
                ex.printStackTrace();
                }
              }
            }
            });
            contentPanel.add(saveButton);
              
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



    public static void drawArrowLine(Graphics g, Point source, Point target, int arrowSize, int stroke) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setStroke(new BasicStroke(stroke));
      g2d.setColor(Color.BLACK);
  
      int x1 = source.x;
      int y1 = source.y;
      int x2 = target.x;
      int y2 = target.y;
  
      int dx = x2 - x1;
      int dy = y2 - y1;
      double theta = Math.atan2(dy, dx);
      double rad = Math.toRadians(arrowSize);
      int len = (int) Math.sqrt(dx*dx + dy*dy) - 10;
  
      int x3 = (int) (x2 - len * Math.cos(theta));
      int y3 = (int) (y2 - len * Math.sin(theta));
  
      int x4 = (int) (x3 + arrowSize * Math.cos(theta + rad));
      int y4 = (int) (y3 + arrowSize * Math.sin(theta + rad));
  
      int x5 = (int) (x3 + arrowSize * Math.cos(theta - rad));
      int y5 = (int) (y3 + arrowSize * Math.sin(theta - rad));
  
      g2d.drawLine(x1, y1, x2, y2);
      g2d.drawLine(x2, y2, x4, y4);
      g2d.drawLine(x2, y2, x5, y5);
  }
  
  
  
  
    public static void drawArrowedLine(JPanel panel, int x1, int y1, int x2, int y2) {
      System.out.println("drawArrowedLine called");
      panel.add(new ArrowedLine(x1, y1, x2, y2)); 
  }
  
  private static class ArrowedLine extends JComponent {
      private final int x1, y1, x2, y2;
      
      public ArrowedLine(int x1, int y1, int x2, int y2) {
          this.x1 = x1;
          this.y1 = y1;
          this.x2 = x2;
          this.y2 = y2;
      }
      
      @Override
      public void paintComponent(Graphics g) {
          super.paintComponent(g);
          Graphics2D g2d = (Graphics2D) g;
          g2d.setColor(Color.RED);
          g2d.drawLine(x1, y1, x2, y2);
          int ARR_SIZE = 10;
          double dx = x2 - x1;
          double dy = y2 - y1;
          double angle = Math.atan2(dy, dx);
          int len = (int) Math.sqrt(dx*dx + dy*dy);
          AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
          at.concatenate(AffineTransform.getRotateInstance(angle));
          g2d.transform(at);
          g2d.drawLine(0, 0, len, 0);
          g2d.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len},
                  new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
      }
  }
  

    private void createLineBetweenLabels(JComponent panel, JLabel label1, JLabel label2) {
      Graphics g = panel.getGraphics();
      Point p1 = getLabelCenter(label1);
      Point p2 = getLabelCenter(label2);
      g.drawLine(p1.x, p1.y, p2.x, p2.y);
  }
  
  private Point getLabelCenter(JLabel label) {
      int x = label.getX() + label.getWidth() / 2;
      int y = label.getY() + label.getHeight() / 2;
      return new Point(x, y);
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