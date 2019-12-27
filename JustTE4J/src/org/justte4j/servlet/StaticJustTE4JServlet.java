package org.justte4j.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.justte4j.core.JustTE4J;
 
 
public class StaticJustTE4JServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    
    public StaticJustTE4JServlet() {
        super();
           
    }     
      

    //Context for parameters
	 ServletContext context; 
  	//Data for template retrieval
 	String DATA_FILE_EXTENTION=".ted";
	String TE4J_FILE_FOLDER="/template";
	String DATA_FILE_FOLDER="/data";
	String FORM_FILE_FOLDER="/form";
	String FORM_FILE_NAME="/templateTE4J.html";
		 	
	 
	 //
	public void init(ServletConfig config) throws ServletException {

		super.init();
	 	 
      
		//Get servlet context
   	    context=config.getServletContext();
	    
   	    //Get initial parameters. Get from web.xml or JustTE4J
   	    String param=context.getInitParameter("DATA_FILE_EXTENTION");
   	    if(param!=null)
   	    	DATA_FILE_EXTENTION=param;
   	    else
   	    	DATA_FILE_EXTENTION=JustTE4J.getDefaultDataFileExtention();
   	    
   	    param=context.getInitParameter("TE4J_FILE_FOLDER");
  	    if(param!=null)
  	    	TE4J_FILE_FOLDER=param;
  	    else
  	    	TE4J_FILE_FOLDER=JustTE4J.getDefaultTE4JFileFolder() ;
  	 
  	    param=context.getInitParameter("DATA_FILE_FOLDER");
  	    if(param!=null)
  	    	DATA_FILE_FOLDER=param;
  	    else
  	    	DATA_FILE_FOLDER=JustTE4J.getDefaultDataFileFolder();
  	 
  	    param=context.getInitParameter("FORM_FILE_FOLDER");
  	    if(param!=null)
  	    	FORM_FILE_FOLDER=param;
  	    else
  	    	FORM_FILE_FOLDER=JustTE4J.getDefaultFormFileFolder();
  	 
  	    param=context.getInitParameter("FORM_FILE_NAME");
  	    if(param!=null)
  	    	FORM_FILE_NAME=param;
  	    else
  	    	FORM_FILE_NAME=JustTE4J.getDefaultFormFileName();
  	    
  	       
   	    //Test logs
   	    if(JustTE4J.isLogged())
           {
   	    	System.out.println("INITIAL PARAMS[DATA_FILE_EXTENTION]"+DATA_FILE_EXTENTION);
   	    	System.out.println("INITIAL PARAMS[TE4J_FILE_FOLDER]"+TE4J_FILE_FOLDER);
   	    	System.out.println("INITIAL PARAMS[DATA_FILE_FOLDER]"+DATA_FILE_FOLDER);
   	    	System.out.println("INITIAL PARAMS[FORM_FILE_FOLDER]"+FORM_FILE_FOLDER);
   	    	System.out.println("INITIAL PARAMS[FORM_FILE_NAME]"+FORM_FILE_NAME);
   	        }
	
	}
 	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		         
		          //Test logs
		      	  if(JustTE4J.isLogged())
		      	     System.out.println("Start doGet");
		           
				  //Get requested path to evaluate
				  String reqestedPath=request.getServletPath();
				 
				  //Test logs
		      	  if(JustTE4J.isLogged())
		      	     System.out.println("reqestedPath="+reqestedPath);
		       
				  
				  //Requested file name
				  String reqestedFileName=JustTE4J.extractFileName(reqestedPath);
				 
				   //Test logs
		      	  if(JustTE4J.isLogged())
		      	     System.out.println("reqestedFileName="+reqestedFileName);
		       
				  
				  //No file name = 404
				  if(reqestedFileName==null)
				    {
					  response.sendError(HttpServletResponse.SC_NOT_FOUND);
					  return;
				    }
				   
				  //Get context for file lookup
				  String contextPath=context.getRealPath("/");
				  
				  //Test logs
		      	  if(JustTE4J.isLogged())
		      	     System.out.println("contextPath="+contextPath);
		         
				  //Data for template
				  File dataFile=new File(contextPath+ TE4J_FILE_FOLDER+DATA_FILE_FOLDER+reqestedFileName+DATA_FILE_EXTENTION);
				  
				  //Check if data file exists - otherwise 404
				  if(!JustTE4J.exists(dataFile))
				    {
					  response.sendError(HttpServletResponse.SC_NOT_FOUND);
					  return;
				    }
			 	 
				  ///////////////JUST TEMPLATE ENGINE FOR JAVA ////////////////////
				  JustTE4J justTE4J=new JustTE4J();
				    
				  //INPUT - Template file -   html file
				  FileInputStream templateIs=new FileInputStream(  new File(contextPath+ TE4J_FILE_FOLDER+FORM_FILE_FOLDER+FORM_FILE_NAME));
				   
				  //Test logs
		      	  if(JustTE4J.isLogged())
		      	     System.out.println("templateIs="+contextPath+ TE4J_FILE_FOLDER+FORM_FILE_FOLDER+FORM_FILE_NAME);
		       
				  //INPUT - requested
		      	   InputStream dataIs=new FileInputStream(dataFile);
				  //INPUT - Dummy data
				  //InputStream dataIs=justTE4J.getDummyData() ;
				   
				  /////////////////////PROCESS//////////
			 	  //Execute templating process
				   justTE4J.doTemplate(  templateIs,   dataIs);
				   ////////////// //////
  
				  //Close streams
				  templateIs.close();
				  dataIs.close();
				  
			     //PRINT AND RETURN
				 response.setContentType("text/html");
    			 PrintWriter writer=response.getWriter();
    			 //Write to output
    			 justTE4J.writeAllTo(writer);
    			 
    			 //
    		     writer.flush();
    		     writer.close();
    		     return;
							 

	}
	
	 
 
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,   response);
	}

}
