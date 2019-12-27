package org.justte4j.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
 
public class JustTE4J   {

	//Indicated if data in logged
	static boolean isLogged=false;
	//Main document
	Document document;
	
	public JustTE4J()
	  {
		
	  }
	
	  
	//CONTS
		final char TARGET_TYPE_TAG='T';
		final char TARGET_TYPE_CLASS='C';
		final char TARGET_TYPE_ATTRIBUTE='A';
		final char TARGET_TYPE_ID='I';
		//
		final char COMMAND_TYPE_CONTENT='C';
		final char COMMAND_TYPE_ATTRIBUTE='A';
		//
		final char SUBCOMMAND_TYPE_REPLACE='R';
		final char SUBCOMMAND_TYPE_INSERT='I';
		final char SUBCOMMAND_TYPE_APPEND='A';
		final char SUBCOMMAND_TYPE_PREPEND='P';
		final char SUBCOMMAND_TYPE_NAME='N';
		final char SUBCOMMAND_TYPE_DELETE='D';
		final char SUBCOMMAND_TYPE_FORMER='F';
		final char SUBCOMMAND_TYPE_LATTER='L';
		
		 
		 //
	 	final static String DEFAULT_DATA_FILE_EXTENTION=".ted";
		final static String DEFAULT_TE4J_FILE_FOLDER="/template";
		final static String DEFAULT_DATA_FILE_FOLDER="/data";
		final static String DEFAULT_FORM_FILE_FOLDER="/form";
		final static String DEFAULT_FORM_FILE_NAME="/templateTE4J.html";
		 //Separates key from value
		 final static String DEFAULT_KV_SEPARATOR = "!=" ;
		 //Denotes end of line
		 final static String DEFAULT_LINE_DELIMITER =  ";\r\n" ;
	    
	
 public InputStream getDummyData()
		{
		  //Test
		  StringBuffer testSB=new StringBuffer();
		  testSB.append(TARGET_TYPE_TAG+":"+COMMAND_TYPE_ATTRIBUTE+":"+SUBCOMMAND_TYPE_NAME+":span:data-custom"+DEFAULT_KV_SEPARATOR+"data-new"+DEFAULT_LINE_DELIMITER);
		  testSB.append(TARGET_TYPE_CLASS+":"+COMMAND_TYPE_CONTENT+":"+SUBCOMMAND_TYPE_INSERT+":title:"+DEFAULT_KV_SEPARATOR+"Welcome+DEFAULT_LINE_DELIMITER");
		  testSB.append(TARGET_TYPE_TAG+":"+COMMAND_TYPE_CONTENT+":"+SUBCOMMAND_TYPE_APPEND+":span:"+DEFAULT_KV_SEPARATOR+"new data+DEFAULT_LINE_DELIMITER");
		  ByteArrayInputStream dataIs=new ByteArrayInputStream(testSB.toString().getBytes());
		 
		  return dataIs;
		}
	
	
	   //Execute templating process
  public void doTemplate(InputStream templateIs, InputStream dataIs) throws IOException
		{
			  ////////////////////////////////////////READ DATA////////////////////////////
		    
			  //Define maps for separate targets
			  // Lines with Id as target
			  LinkedHashMap<String,String> dataLinesForIds =new LinkedHashMap<String,String>( );
			  // Lines with Attribute as target
			  LinkedHashMap<String,String> dataLinesForAttrs =new LinkedHashMap<String,String>( );
			  // Lines with Classes as target
			  LinkedHashMap<String,String> dataLinesForClss =new LinkedHashMap<String,String>( );
			  // Lines with Tags as target
			  LinkedHashMap<String,String> dataLinesForTgs =new LinkedHashMap<String,String>( );

			 //Reads lines from data file and puts it into maps
			 separateData(  dataIs, dataLinesForIds,  dataLinesForAttrs,  dataLinesForClss,  dataLinesForTgs   );

		     ///////////////////////////////////////END READ DATA////////////////////////////
					
			 ///////////////////////////////READ TEMPLATE//////////////////////////		 
			 //Read predefined template
		  	 Document doc = Jsoup.parse(templateIs, "UTF-8", "/");
		  	 //Outer reference
		  	 this.document=doc;
			 ///////////////////////////////END READ TEMPLATE//////////////////////////		 
		 
					 
			/////////////////////////////////////COMMANDS////////////////////////
			 //Processes comands from data file
 		     processCommands(  doc,  dataLinesForIds,  dataLinesForAttrs,  dataLinesForClss,  dataLinesForTgs );
			 /////////////////////////////////////END COMMANDS////////////////////
		       
		     return;
		}
		
		
		//Reads lines from data file and puts it into maps
		public void separateData(InputStream dataIs,LinkedHashMap<String,String> dataLinesForIds,LinkedHashMap<String,String> dataLinesForAttrs, LinkedHashMap<String,String> dataLinesForClss,LinkedHashMap<String,String> dataLinesForTgs   )
		{
			
			     //Define file reader and delimiter
			     Scanner dataTemplate=new Scanner(dataIs);
			     dataTemplate.useDelimiter(Pattern.compile(DEFAULT_LINE_DELIMITER));
		   
			     if(isLogged)
					  System.out.println("Use DEFAULT_LINE_DELIMITER="+DEFAULT_LINE_DELIMITER);
			     
		 	     
				//Get every line until end 
				while(true)
						{
						  //Read from 
						  String dataLine;
						  try {
							   dataLine = dataTemplate.next();
						       } 
						  catch (NoSuchElementException e)
						        {
							    break;
						        } 
						  
						   if(isLogged)
							  System.out.println("dataLine="+dataLine);
						 			   
						  //Split by DEFAULT_KV_SEPARATOR 
						  String[] data=dataLine.split(DEFAULT_KV_SEPARATOR); 
						   
						  //Separate by target and input into map
						  switch(data[0].charAt(0))
						  {
						  
							  //Comment - skip
							  case '#':
							    	   
							  break;
								  
							  //Tags
						      case TARGET_TYPE_TAG:
						    	   dataLinesForTgs.put(data[0],data[1]);
							  break;
							   
							  //Classes
						      case TARGET_TYPE_CLASS:
						     	  dataLinesForClss.put(data[0],data[1]);
							  break;
							 
							  //Attributes
						      case TARGET_TYPE_ATTRIBUTE:
						     	  dataLinesForAttrs.put(data[0],data[1]);
							  break;
							  
							  //IDs
						      case TARGET_TYPE_ID:
						     	  dataLinesForIds.put(data[0],data[1]);
						  	  break;
							   
						  }
						   
					 } 
	 	}
		
	 
		
	  //Processes commands from data file
	  public void processCommands(Document doc,LinkedHashMap<String,String> dataLinesForIds,LinkedHashMap<String,String> dataLinesForAttrs, LinkedHashMap<String,String> dataLinesForClss,LinkedHashMap<String,String> dataLinesForTgs )
				{
					 		
		  
		  					/////////HANDLE TAGS///////////
							 for(String key: dataLinesForTgs.keySet())
							   {
						 		 //Separate elements of keys
								 String[] keyEles= key.split(":");
							 	 //ParamI - main parameter - tag name
								 String tagName =keyEles[3].toLowerCase();
								 //Retrieve tag by tagName
								 Elements tagEles = doc.getElementsByTag(tagName);  
								 //Iterator
								 ListIterator< Element> iterator= tagEles.listIterator(); 
								 //Browse elements
								 while(	iterator.hasNext() )
								     {
									 //Get this element
									 Element tagElement=iterator.next();
								     //Process 
								 	 processInputs(  keyEles,      tagElement,   key,  dataLinesForTgs);
									 }
							   }
							 
						 
							 ////////////////HANDLE CLASSES////////////
							 for(String key: dataLinesForClss.keySet())
							    {
								  //Separate elements of keys
								 String[] keyEles= key.split(":");
								  //ParamI - main parameter - class value
								 String classValue =keyEles[3].toLowerCase();
							 	 //Check for match - classValue
								 Elements tagEles = doc.getElementsByClass(classValue);
								 //Iterator
								 ListIterator< Element> iterator= tagEles.listIterator(); 
								 //Browse elements
								 while(	iterator.hasNext() )
								   {
									//Get this element
									 Element tagElement=iterator.next();
									 //Process 
								 	 processInputs(  keyEles,    tagElement,   key,  dataLinesForClss);
								   }
						 	    }
						 
							 ////////////////HANDLE ATTRIBUTES//////////
							 for(String key: dataLinesForAttrs.keySet())
							    {
								  //Separate elements of keys
								 String[] keyEles= key.split(":");
								  //ParamI - main parameter - attrbute name
								 String attrName =keyEles[3].toLowerCase();
							 	 //Check for match - attributeName
								 Elements tagEles = doc.getElementsByAttribute(attrName);
								 //Iterator
								 ListIterator< Element> iterator= tagEles.listIterator(); 
								//Browse elements
								 while(	iterator.hasNext() )
								   {
									//Get this element
									 Element tagElement=iterator.next();
									    //Process 
								 	 processInputs(  keyEles,    tagElement,   key,  dataLinesForIds);
								   }
						 	    }
							  
							 ////////////////HANDLE IDS/////////////
							 for(String key: dataLinesForIds.keySet())
							    {
								
								  //Separate elements of keys
								 String[] keyEles= key.split(":");
								  //ParamI - main parameter - id value
								 String idValue =keyEles[3];
						    	 //Check for match - idValue
								 Element tagElement= doc.getElementById(idValue);
							 	 if(tagElement!=null  )  
								   {
							 	    //Process 
									processInputs(  keyEles,    tagElement,   key,  dataLinesForIds);
								   }
						 	    }
						     
				}
				
		 
		//Execute command and subcommand on html 
	 public boolean processInputs(String[] keyEles,   Element tagElement, String key, LinkedHashMap<String,String> dataLines)
				{
					 //String buffer for data
					 StringBuilder sb;
					
					 //Command - command to process
					 String command =keyEles[1];
					 //Subcommand - subcommand to process
					 String subcommand =keyEles[2];
					
					 if(isLogged)
					    System.out.println("Command="+command+" subcommand="+subcommand);
					  
					//Change tag content or attribute 
				 	 switch(command.charAt(0))
					     {
				  	 
					       //Tag content
					       case COMMAND_TYPE_CONTENT:
					    	     
					    	   switch(subcommand.charAt(0))
							     {
							       //Replace whole TAG along with start-tag and end-tag
							       case SUBCOMMAND_TYPE_REPLACE:
							    	    //Puts new after 
							    	   tagElement.after(  dataLines.get(key));
							    	   //Deletes this one
							            tagElement.remove();
							    	 	 
							    	   
								    break;
								    
								    //Insert TAG's content - does not affect start and end tags
							       case SUBCOMMAND_TYPE_INSERT:
							    	     //Input
							    	     tagElement.html( dataLines.get(key));
								    break;
								    
								    //Append data after existing content
							       case SUBCOMMAND_TYPE_APPEND:
							    	     //Input
							    	     tagElement.append( dataLines.get(key));
								    break;
								    
								  //Prepend data before existing content
							       case SUBCOMMAND_TYPE_PREPEND:
							    	     //Input
							    	   tagElement.prepend( dataLines.get(key));
								    break;
								    
								    //Renames the  tag
							       case SUBCOMMAND_TYPE_NAME:
							    	      //Input
						 	    		 tagElement.tagName(dataLines.get(key));
								    break;
								    
								    //Delete the tag
							       case SUBCOMMAND_TYPE_DELETE:
							    	     
							    	     //Input
							    	     tagElement.remove();
								    break;
								    
								    //Place before the tag
							       case SUBCOMMAND_TYPE_FORMER:
							    	     //Input
							     	     tagElement.before( dataLines.get(key) );
							       break;
								    
								    //Place after the tag
							       case SUBCOMMAND_TYPE_LATTER:
							    	   //Input
							    	   tagElement.after(  dataLines.get(key));
							  	    break;
								 
								    
						        }
					    	 
					    	   
						    break;
						    
						    //Tag attribute
					       case COMMAND_TYPE_ATTRIBUTE:
					    	   
					    	
					    	   //Get extra param - attribute name
					    	     String attrName =keyEles[4].toLowerCase();
					    	     //Empty value
					    	     String attrValue;
					    		     
					    	   switch(subcommand.charAt(0))
							     {
							       //Replace whole attribute. Replaces name and value with another attribute
							       case SUBCOMMAND_TYPE_REPLACE:
							         
							    	    //Check for syntax
							    	   String[] attrKV=dataLines.get(key).split("=");
							    	   
							    	   //Only if proper syntax
							    	   if(attrKV.length==2)
							    	      {
							    	       //Delete attr
							    	       tagElement.removeAttr(attrName);
							    	       //New one
							    	       tagElement.attr(attrKV[0],attrKV[1]);
							    	      }
								    break;
								    
								    //Insert attributes's value - does not affect attr name
							       case SUBCOMMAND_TYPE_INSERT:
							    	   
							     	   //Input
							    	   tagElement.attr(attrName,dataLines.get(key));
										  
								    break;
								    
								    //Append attribute data after existing content
							       case SUBCOMMAND_TYPE_APPEND:
							    	   
							    	   //Input
							    	   attrValue=tagElement.attr(attrName);
							    	   //append
							    	   tagElement.attr(attrName,attrValue+" "+dataLines.get(key));
							    	   
							    	break;
								    
								  //Prepend data before existing content
							       case SUBCOMMAND_TYPE_PREPEND:
							    	   //Input
							    	   attrValue=tagElement.attr(attrName);
							    	   //append
							    	   tagElement.attr(attrName,dataLines.get(key)+" "+attrValue);
							    	   
								    break;
								    
								    //Renames the attribute
							       case SUBCOMMAND_TYPE_NAME:
							    	    
							    	   //Input
							    	   attrValue=tagElement.attr(attrName);
							    	   //Delete attr
							    	   tagElement.removeAttr(attrName);
							    	   //Add with new name
							    	   tagElement.attr(dataLines.get(key),attrValue );
							    	   
								    break;
								    
								    //Delete the attribute
							       case SUBCOMMAND_TYPE_DELETE:
							    	   //Delete attr
							    	   tagElement.removeAttr(attrName);
							    	   
								    break;
								    
								    //Place before the attributes - same as SUBCOMMAND_TYPE_PREPEND
							       case SUBCOMMAND_TYPE_FORMER:
							    	   //Input
							    	   attrValue=tagElement.attr(attrName);
							    	   //prepend
							    	   tagElement.attr(attrName,dataLines.get(key)+" "+attrValue);
							       break;
								    
								    //Place after the attributes - same as  SUBCOMMAND_TYPE_APPEND
							       case SUBCOMMAND_TYPE_LATTER:
							    	   //Input
							    	   attrValue=tagElement.attr(attrName);
							    	   //append
							    	   tagElement.attr(attrName,attrValue+" "+dataLines.get(key));
							    	   
							  	    break;
								    
						        }
					    	   
					       break;
						    
						   
				        }
				 	 
				 	 return true;
				}
		
		//Extract file name (without path and suffix) from file name with path and suffix.
		public static String extractFileName( String filePathName )
		  {
		    if ( filePathName == null )
		      return null;

		    int dotPos = filePathName.lastIndexOf( '.' );
		    int slashPos = filePathName.lastIndexOf( '\\' );
		    if ( slashPos == -1 )
		      slashPos = filePathName.lastIndexOf( '/' );

		    if ( dotPos > slashPos )
		    {
		      return filePathName.substring( slashPos > 0 ? slashPos + 1 : 0,
		          dotPos );
		    }

		    return filePathName.substring( slashPos > 0 ? slashPos + 1 : 0 );
		  }

		 

//Default servlet values 	
 public static String getDefaultDataFileExtention()
		{
		 return DEFAULT_DATA_FILE_EXTENTION;
		}
		
 public static String getDefaultTE4JFileFolder()
		{
		return DEFAULT_TE4J_FILE_FOLDER;
		}		
 
 public static String getDefaultDataFileFolder()
		{
		return DEFAULT_DATA_FILE_FOLDER;
		}
 
 public  static String getDefaultFormFileFolder()
		{
		return DEFAULT_FORM_FILE_FOLDER;
		}
 public  static String getDefaultFormFileName()
		{
		return DEFAULT_FORM_FILE_NAME;
		}
 
 public  static String getDefaultKVSeparator()
		{
		return DEFAULT_KV_SEPARATOR;
		} 
 
 public  static String getDefaultLineDelimiter()
		{
		return DEFAULT_LINE_DELIMITER;
		} 
 

 
public static void setLogged(boolean isLoggedNewValue)
		{
      	isLogged=isLoggedNewValue;	
		}
 
public static boolean isLogged( )
		{
		return isLogged;	
		}

//Verifies existance of a file
public static boolean exists(File f)
	{
		 
		 if(f.exists() && !f.isDirectory()) 
	       return true;
		 else
			 return false;
	}

//Reads content of a file as String
public String readFile(String filename)
	{
	  String content = null;
	  File file = new File(filename);  
	  try {
	      FileReader reader = new FileReader(file);
	      char[] chars = new char[(int) file.length()];
	      reader.read(chars);
	      content = new String(chars);
	      reader.close();
	  } catch (IOException e) {
	      e.printStackTrace();
	  }
	  return content;
	}

 /////////////////OUTPUT DOCUMENT//////////////////
 
	 //Writes modified source to the writer
	public void writeAllTo(  Writer writer) throws IOException
	     {
		  if(document!=null)
		   writer.write(document.toString());
	     }
 /////////////////OUTPUT DOCUMENT//////////////////

}
