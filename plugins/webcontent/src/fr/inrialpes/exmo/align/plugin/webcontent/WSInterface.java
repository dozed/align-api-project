/*
 * $Id: WSInterface.java 1704 2012-03-10 16:24:07Z euzenat $
 *
 * Copyright (C) INRIA, 2007-2008, 2010, 2012
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.plugin.webcontent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
 
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import java.util.Properties;
import java.util.zip.ZipInputStream;
import java.lang.StringBuffer;

import javax.swing.JOptionPane;
 
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class WSInterface {
		
		//public AlignmentClient ws = null;
		public  String HOST = null;
		public  String PORT = null;
		public  String WSDL = "7777";
		public  boolean connected = false;
		URL SOAPUrl = null;
		String SOAPAction = null;
		String uploadFile = null;
		private static DocumentBuilder BUILDER = null;
		final DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		
	    public WSInterface( String htmlPort, String host)  {
	    	try {
	    		HOST = host;
	    		PORT = htmlPort;
	    		
	    		SOAPUrl = new URL( "http://" + host + ":" + htmlPort + "/aserv" );
	    		
	    	} catch ( Exception ex ) { ex.printStackTrace(); };
	    	
			fac.setValidating(false);
			fac.setNamespaceAware(false);
			try { BUILDER = fac.newDocumentBuilder(); }
			catch (ParserConfigurationException e) { };
	    }
	    
	    public String uploadAlign(String alignId) {
	    	
			String answer = null;
			try {
				Properties params = new Properties();
				params.setProperty( "host", HOST );
				//params.setProperty( "http", PORT );
				//params.setProperty( "wsdl", WSDL );
				params.setProperty( "command","load");
				//params.setProperty( "arg1", alignId);
				
				uploadFile = alignId;
				//System.out.println("Load file= "+ uploadFile);
					
				// Create the SOAP message
				String message = createMessage( params );
				  
				//System.out.println("HOST= "+ HOST + ", PORT=  " + PORT + ",  Action = "+ SOAPAction);
				//System.out.println("Message for load file :"+ message);
				
				// Send message
				//answer = sendMessage( message, params );
				answer = sendFile( message, params );
				//System.out.println("SOAP loaded align=" + answer );
				
			} catch ( Exception ex ) { ex.printStackTrace(); };
			if(! connected ) return null;
			
			Document domMessage = null;
			try {
			    domMessage = BUILDER.parse( new ByteArrayInputStream( answer.getBytes()) );
			    
			} catch  ( IOException ioex ) {
			    ioex.printStackTrace();
			} catch  ( SAXException saxex ) {
			    saxex.printStackTrace();
			}
			
				   
			String[] result = getTagFromSOAP( domMessage,  "loadResponse" );
			//System.out.println("Loaded Align="+ result[0]);
			
			return result[0];
			
	    }
	    
	    public String trimAlign(String alignId, String thres) {
	    	
			String answer = null;
			try {
				// Read parameters
				 
				Properties params = new Properties();
				params.setProperty( "host", HOST );
				 
				params.setProperty( "command","trim");
				params.setProperty( "arg1",alignId);
				params.setProperty( "arg2",thres);
					
				// Create the SOAP message
				String message = createMessage( params );
				  
				//System.out.println("HOST= :"+ HOST + ", PORT=  " + PORT + ",  Action = "+ SOAPAction);
				//System.out.println("Message :"+ message);
				
				// Send message
				answer = sendMessage( message, params );
				
				
			}
			catch ( Exception ex ) { ex.printStackTrace(); };
			if(! connected ) return null; 
			
			Document domMessage = null;
			try {
			    domMessage = BUILDER.parse( new ByteArrayInputStream( answer.getBytes()) );
			    
			} catch  ( IOException ioex ) {
			    ioex.printStackTrace();
			} catch  ( SAXException saxex ) {
			    saxex.printStackTrace();
			}
			
			String[] result = getTagFromSOAP( domMessage,  "cutResponse" );
			
			//System.out.println("Trim Align="+ result[0]);
			
			return result[0];
			
			 
			
	    }
	    
	    public String[] getMethods() {
	    	
			String answer = null;
		     
			try {
				// Read parameters
				 
				Properties params = new Properties();
				params.setProperty( "host", HOST );
				 
				params.setProperty( "command","list");
				params.setProperty( "arg1","methods");
					
				// Create the SOAP message
				String message = createMessage( params );
				  
				//System.out.println("HOST= :"+ HOST + ", PORT=  " + PORT + ",  Action = "+ SOAPAction);
				//System.out.println("Message :"+ message);
				
				// Send message
				answer = sendMessage( message, params );
			}
			catch ( Exception ex ) { ex.printStackTrace(); };
			if(! connected ) return null; 
			
			Document domMessage = null;
			try {
				    domMessage = BUILDER.parse( new ByteArrayInputStream( answer.getBytes()) );
				    
				} catch  ( IOException ioex ) {
				    ioex.printStackTrace();
				} catch  ( SAXException saxex ) {
				    saxex.printStackTrace();
				}
				
				 
			String[] result = getTagFromSOAP( domMessage,  "listmethodsResponse/classList/method" ) ;
			//for(int i=0; i< result.length;i++) //System.out.println("methods=" + result[i]);
			return result;
		 
	    }
	    
	    public String[] findAlignForOntos(String onto1, String onto2) {
	    	
			String answer = null;
		     
			try {
				// Read parameters 
				Properties params = new Properties();
				params.setProperty( "host", HOST );
				 
				params.setProperty( "command","find");
				params.setProperty( "arg1", onto1);
				params.setProperty( "arg2", onto2);	
				// Create the SOAP message
				String message = createMessage( params );
				  	
				// Send message
				answer = sendMessage( message, params );
			}
			catch ( Exception ex ) { ex.printStackTrace(); };
			if(! connected ) return null; 
				   
			Document domMessage = null;
				try {
				    domMessage = BUILDER.parse( new ByteArrayInputStream( answer.getBytes()) );
				    
				} catch  ( IOException ioex ) {
				    ioex.printStackTrace();
				} catch  ( SAXException saxex ) {
				    saxex.printStackTrace();
				}
				
			String[] result = getTagFromSOAP( domMessage,  "findResponse/alignmentList/alid" );
				
		        return result; 
			 
	    }
	    
	    public String[] getAllAlign() {
	    	
			String answer = null;
		     
			try {
				// Read parameters
				 
				Properties params = new Properties();
				params.setProperty( "host", HOST );
				//params.setProperty( "http", PORT );
				//params.setProperty( "wsdl", WSDL );
				params.setProperty( "command","list");
				params.setProperty( "arg1","alignments");
					
				// Create the SOAP message
				String message = createMessage( params );
				  
				//System.out.println("HOST= :"+ HOST + ", PORT=  " + PORT + ",  Action = "+ SOAPAction);
				//System.out.println("Message :"+ message);
				
				// Send message
				answer = sendMessage( message, params );
			}
			catch ( Exception ex ) { ex.printStackTrace(); };
			
			if(! connected ) return null; 
			
			// Cut SOAP header
			//answer =  "<?xml version='1.0' encoding='utf-8' standalone='no'?>" + answer ; 
		    Document domMessage = null;
			try {
			    domMessage = BUILDER.parse( new ByteArrayInputStream( answer.getBytes()) );
			    
			} catch  ( IOException ioex ) {
			    ioex.printStackTrace();
			} catch  ( SAXException saxex ) {
			    saxex.printStackTrace();
			}
			
			
			String[] result = getTagFromSOAP( domMessage,  "listalignmentsResponse/alignmentList/alid" );
			//for(int i=0; i< result.length;i++) System.out.println("aligns=" + result[i]);
			
			return result;
			 
				 
			
	    }
	    
	    public String getAlignId(String method, String onto1, String onto2) {
	    	
	    	String[] aservArgAlign = new String[6];		
	    	String answer = null ;
				
				//System.out.println("Uri 1="+ onto1);
			    //System.out.println("Uri 2="+ onto2);
			    
			    Properties params = new Properties();
				params.setProperty( "host", HOST );
				//params.setProperty( "http", PORT );
				//params.setProperty( "wsdl", WSDL );
				params.setProperty( "command","match");
				params.setProperty( "arg1", method);
				params.setProperty( "arg2", onto1);
				params.setProperty( "arg3", onto2);
				
			    try {
			    	// Read parameters
			    	// Create the SOAP message
			    	String message = createMessage( params );
			  
			    	//System.out.println("URL SOAP :"+ SOAPUrl+ ",  Action:"+ SOAPAction);
			    	//System.out.println("Message :"+ message);
			
			    	// Send message
			    	answer = sendMessage( message, params );
			 
			    	//System.out.println("SOAP Match align=" + answer );
			    	 
			    }
			    
			    catch ( Exception ex ) { ex.printStackTrace(); };
			    if(! connected ) return null; 
			    
			    // Cut SOAP header
				//answer =  "<?xml version='1.0' encoding='utf-8' standalone='no'?>" + answer ; 
			    Document domMessage = null;
				try {
				    domMessage = BUILDER.parse( new ByteArrayInputStream( answer.getBytes()) );
				    
				} catch  ( IOException ioex ) {
				    ioex.printStackTrace();
				} catch  ( SAXException saxex ) {
				    saxex.printStackTrace();
				}
				
				 
				String result[] = getTagFromSOAP( domMessage,  "matchResponse" );
				
				//System.out.println("Match align Id=" + result[0]);
				
			    return result[0];
			 
	    }
	    
		
	    public String getOWLAlignment(String alignId) {
		
		//retrieve alignment for storing in OWL file
		
		 
		Properties params = new Properties();
		params.setProperty( "host", HOST );
		//params.setProperty( "http", PORT );
		//params.setProperty( "wsdl", WSDL );
		params.setProperty( "command","retrieve");
		params.setProperty( "arg1", alignId);
		params.setProperty( "arg2", "fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor");
		
		String answer=null;
	     
		try {
			// Read parameters
			//Properties params = ws.readParameters( aservArgRetrieve );
			
			// Create the SOAP message
			String message = createMessage( params );

			//System.out.println("URL SOAP :"+ SOAPUrl + ",  Action:"+  SOAPAction);
			//System.out.println("Message :" + message);
			
			// Send message
			answer = sendMessage( message, params );
			if(! connected ) return null; 
			
			
			
		} catch ( Exception ex ) { ex.printStackTrace();  };
			 
			 
		// Cut SOAP header
		//answer =  "<?xml version='1.0' encoding='utf-8' standalone='no'?>" + answer ; 
		answer = answer.replace("<?xml version='1.0' encoding='utf-8' standalone='no'?>", "");
		 
		
		Document domMessage = null;
		try {
		    domMessage = BUILDER.parse( new ByteArrayInputStream( answer.getBytes()) );
		    
		} catch  ( IOException ioex ) {
		    ioex.printStackTrace();
		} catch  ( SAXException saxex ) {
		    saxex.printStackTrace();
		}
		
		
		
		String result[] = getTagFromSOAP( domMessage,  "retrieveResponse/result/RDF" );
	 	 
		 
		
		//System.out.println("OWLAlign="+ result[0]);
		return result[0];
	    }
	    
	    public String getRDFAlignment(String alignId) {
			
			//retrieve alignment for storing in OWL file
			
			 
			Properties params = new Properties();
			params.setProperty( "host", HOST );
			//params.setProperty( "http", PORT );
			//params.setProperty( "wsdl", WSDL );
			params.setProperty( "command","retrieve");
			params.setProperty( "arg1", alignId);
			params.setProperty( "arg2", "fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor");
			
			String answer=null;
		     
			try {
				// Read parameters
				//Properties params = ws.readParameters( aservArgRetrieve );
				
				// Create the SOAP message
				String message = createMessage( params );

				//System.out.println("URL SOAP :"+ SOAPUrl + ",  Action:"+  SOAPAction);
				//System.out.println("Message :" + message);
				
				// Send message
				answer = sendMessage( message, params );
				if(! connected ) return null; 
				
				
				
			} catch ( Exception ex ) { ex.printStackTrace();  };
				 
			Document domMessage = null;
			try {
			    domMessage = BUILDER.parse( new ByteArrayInputStream( answer.getBytes()) );
			    
			} catch  ( IOException ioex ) {
			    ioex.printStackTrace();
			} catch  ( SAXException saxex ) {
			    saxex.printStackTrace();
			}
			
			 
			String result[] = getTagFromSOAP( domMessage,  "retrieveResponse/result/RDF" );
			 	
			return result[0];
		}
	    
	    public String render(String alignId, String method) {
 
			Properties params = new Properties();
			params.setProperty( "host", HOST );
			//params.setProperty( "http", PORT );
			//params.setProperty( "wsdl", WSDL );
			params.setProperty( "command","retrieve");
			params.setProperty( "arg1", alignId);
			params.setProperty( "arg2", method);
			
			String answer=null;
		     
			try {
				// Read parameters
				//Properties params = ws.readParameters( aservArgRetrieve );
				
				// Create the SOAP message
				String message = createMessage( params );

				// Send message
				answer = sendMessage( message, params );
				if(! connected ) return null; 
				
				
				
			} catch ( Exception ex ) { ex.printStackTrace();  };
			
			//there is a problem when trying to remove SOAP header from message for this case			
			/*	 
			Document domMessage = null;
			try {
			    domMessage = BUILDER.parse( new ByteArrayInputStream( answer.getBytes()) );
			    
			} catch  ( IOException ioex ) {
			    ioex.printStackTrace();
			} catch  ( SAXException saxex ) {
			    saxex.printStackTrace();
			}
			
			 
			String result[] = getTagFromSOAP( domMessage,  "retrieveResponse/result" );
			*/
			 	
			return answer;
		}
	    
	    public String storeAlign(String alignId) {
			
	    	//retrieve alignment for displaying
			
			Properties params = new Properties();
			params.setProperty( "host", HOST );
			//params.setProperty( "http", PORT );
			//params.setProperty( "wsdl", WSDL );
			params.setProperty( "command","store");
			params.setProperty( "arg1", alignId);
			 
			String answer = null;
			
			try {
				// Read parameters
				 
				//Properties params = ws.readParameters( aservArgRetrieve );
				
				// Create the SOAP message
				String message = createMessage( params );
				  
				//System.out.println("URL SOAP :"+ SOAPUrl+ ",  Action:"+ SOAPAction);
				//System.out.println("Message :"+ message);
				
				// Send message
				answer = sendMessage( message, params );
				 
			
				//corrList = getCorresFromAnswer( answer, "tr", "#" );
		    	
			}
			catch ( Exception ex ) { ex.printStackTrace() ;};
			
			if(! connected ) return null; 
			
			Document domMessage = null;
			try {
			    domMessage = BUILDER.parse( new ByteArrayInputStream( answer.getBytes()) );
			    
			} catch  ( IOException ioex ) {
			    ioex.printStackTrace();
			} catch  ( SAXException saxex ) {
			    saxex.printStackTrace();
			}
			
			 
			String result[] = getTagFromSOAP( domMessage,  "storeResponse" );
			 
			 
			//System.out.println("Stored Align="+ result[0]); 
				
			return result[0];
			
	    }
	    
	    public String loadStringAsAlignment( String message  )   {
	    	// Create the connection
	        
	        byte[] b = message.getBytes();

	        String answer = "";
	        // Create HTTP Request
	        try {
	        	 
	    	    URLConnection connection = SOAPUrl.openConnection();
	            HttpURLConnection httpConn = (HttpURLConnection) connection;
	        	
	            httpConn.setRequestProperty("SOAPAction","loadRequest");
	            httpConn.setRequestMethod( "POST" );
	            httpConn.setDoOutput( true );
	            httpConn.setDoInput( true );
	            
	            // Don't use a cached version of URL connection.
	            httpConn.setUseCaches ( false );
	            httpConn.setDefaultUseCaches (false);
	            
	            //File f = new File("/home/exmo/cleduc/Desktop/align.rdf");
		    //FileInputStream fi = new FileInputStream(f);
	            // set headers and their values.
	            httpConn.setRequestProperty("Content-Type",
	                                         "application/octet-stream");
	            httpConn.setRequestProperty("Content-Length",
	                                        Long.toString(b.length ));
	           
	            // create file stream and write stream to write file data.
	            
	            OutputStream os =  httpConn.getOutputStream();
		    
                    try {
		    	os.write( b, 0, b.length );
	                os.flush();
			os.close();
	            } catch (Exception ex) {}
		    
	            /*
		 
	            try
	            {
	               // transfer the file in 4K chunks.
	               byte[] buffer = new byte[4096];
	               //long byteCnt = 0;
	               int bytes=0;
	               while (true)
	               {
	                  bytes = fi.read(buffer);
	                  System.out.println("line1="+ buffer.toString() );
	                  if (bytes < 0)  break;
	                  
	                  os.write( buffer, 0, bytes );
	 
	               }
	                  
	               os.flush();
	            } catch (Exception ex) {}
	            */	 
	            // Read the response  
	            InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
	            BufferedReader in = new BufferedReader(isr);
	        
	            String line;
		    StringBuffer strBuff = new StringBuffer();
		    while ((line = in.readLine()) != null) {
	            	 strBuff.append( line + "\n");
	            }
	            if (in != null) in.close();
	            answer = strBuff.toString();

		     
	        } catch  (Exception ex) {
	        	connected= false; ex.printStackTrace() ; return null;
	        	}
	        
	        connected = true;
		
	        Document domMessage = null;
			try {
			    domMessage = BUILDER.parse( new ByteArrayInputStream( answer.getBytes()) );
			    
			} catch  ( IOException ioex ) {
			    ioex.printStackTrace();
			} catch  ( SAXException saxex ) {
			    saxex.printStackTrace();
			}
			
		
		String[] result = getTagFromSOAP( domMessage,  "loadResponse" );

	    	return result[0];
	    }

	    public String[] getTagFromSOAP( Document dom,  String tag ){
	    	XPath XPATH = XPathFactory.newInstance().newXPath();
	    	String[] result = null;
	    	Node n = null;
	    	NodeList nl = null;
	    	try {
	    	    // The two first elements are prefixed by: "SOAP-ENV:"
	    		if(tag.equals("listmethodsResponse/classList/method") || tag.equals("listalignmentsResponse/alignmentList/alid") 
	    		|| tag.equals("findResponse/alignmentList/alid") ) {
	    			nl = (NodeList)(XPATH.evaluate("/Envelope/Body/" + tag, dom, XPathConstants.NODESET));
	    			result = new String[nl.getLength()];
	    			
	    			 for (int i=0; i< nl.getLength(); i++) {
	    	 		      Node method = nl.item(i);
 
	    	 		      Node firstnode = method.getFirstChild();
	    	   		      String nm = firstnode.getNodeValue();
	    	 		      
	    	 		      if(nm!=null) result[i] = nm; 
	    	 		             
	    	 		 }
	    		} else  if (tag.equals("retrieveResponse/result/RDF") ) {
	    			  n =  (Node)(XPATH.evaluate("/Envelope/Body/" + tag, dom, XPathConstants.NODE));
	    			  ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    			  try {
	    				  Transformer tf = TransformerFactory.newInstance().newTransformer();
	    				  tf.setOutputProperty(OutputKeys.ENCODING,"utf-8");
	    		          tf.setOutputProperty(OutputKeys.INDENT,"yes");
	    		           
	    		          tf.transform(new DOMSource(n),new StreamResult(stream));
	    			  }
	    			  catch (Exception e){}
	    			  //Node firstnode = n.getFirstChild();
	    			  
	    	   		  String nm = stream.toString();
		    		  result = new String[1];
	    	   		  result[0] = nm; 
	    	   		  
	    		} else {
	    		  Node nn =  (Node)(XPATH.evaluate("/Envelope/Body/" + tag, dom, XPathConstants.NODE));
	    		  result = new String[1];
	    		  
    	 		  NodeList ns = nn.getChildNodes();
    	 		  
    	 		  //tag "alid" is third
    	 		  Node n3 = ns.item(3);
    	 		  Node nx  = n3.getFirstChild();
    	   		  String nm = nx.getNodeValue();
    	   		   
    	   		  result[0] = nm; 
	    		}  
	    	     
	    	} catch (XPathExpressionException e) {
	    	} catch (NullPointerException e) {
	    	}
	    	
	    	return result;  
	    }

	    
	    public String createMessage( Properties params ) throws Exception {
	        String messageBegin = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\'http://schemas.xmlsoap.org/soap/envelope/\' " +
				                  "xmlns:xsi=\'http://www.w3.org/1999/XMLSchema-instance\' " + 
				                  "xmlns:xsd=\'http://www.w3.org/1999/XMLSchema\'>" +
				                  "<SOAP-ENV:Body>";
		String messageBody = "";
		String cmd = params.getProperty( "command" );
		if ( cmd.equals("list" ) ) {
		    String arg = params.getProperty( "arg1" );
		    if ( arg.equals("methods" ) ){  
			SOAPAction = "listmethodsRequest";
		    } else if ( arg.equals("renderers" ) ){
			SOAPAction = "listrenderersRequest";
		    } else if ( arg.equals("services" ) ){
			SOAPAction = "listservicesRequest";
		    } else if ( arg.equals("alignments" ) ){
			SOAPAction = "listalignmentsRequest";
		    } else {
			//usage();
			System.exit(-1);
		    }
		} else if ( cmd.equals("wsdl" ) ) {
		    SOAPAction = "wsdlRequest";
		} else if ( cmd.equals("find" ) ) {
		    SOAPAction = "findRequest";
		    String uri1 = params.getProperty( "arg1" );
		    String uri2 = params.getProperty( "arg2" );
		    if ( uri2 == null ){
			//usage();
			System.exit(-1);
		    }
		    messageBody = "<url1>"+uri1+"</url1><url2>"+uri2+"</url2>";
		} else if ( cmd.equals("match" ) ) {
		    SOAPAction = "matchRequest";
		    String uri1 = params.getProperty( "arg1" );
		    String uri2 = params.getProperty( "arg2" );
		    if ( uri2 == null ){
			//usage();
			System.exit(-1);
		    }
		    String method = null;
		    String arg3 = params.getProperty( "arg3" );
		    if ( arg3 != null ) {
			method = uri1; uri1 = uri2; uri2 = arg3;
		    }
		    arg3 = params.getProperty( "arg4" );
		    messageBody = "<url1>"+uri1+"</url1><url2>"+uri2+"</url2>";
		    if ( method != null )
			messageBody += "<method>"+method+"</method>";
		    //fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment
		    if ( arg3 != null )
			messageBody += "<force>"+arg3+"</force>";
		} else if ( cmd.equals("trim" ) ) {
		    SOAPAction = "cutRequest";
		    String id = params.getProperty( "arg1" );
		    String thres = params.getProperty( "arg2" );
		    if ( thres == null ){
			//usage();
			//System.exit(-1);
		    }
		    String method = null;
		    String arg3 = params.getProperty( "arg3" );
		    if ( arg3 != null ) {
			method = thres; thres = arg3;
		    }
		    messageBody = "<alid>"+id+"</alid><threshold>"+thres+"</threshold>";
		    if ( method != null )
			messageBody += "<method>"+method+"</method>";
		} else if ( cmd.equals("invert" ) ) {
		    SOAPAction = "invertRequest";
		    String uri = params.getProperty( "arg1" );
		    if ( uri == null ){
			//usage();
			//System.exit(-1);
		    }
		    messageBody = "<alid>"+uri+"</alid>";
		} else if ( cmd.equals("store" ) ) {
		    SOAPAction = "storeRequest";
		    String uri = params.getProperty( "arg1" );
		    if ( uri == null ) {
			//usage();
			//System.exit(-1);
		    }
		    messageBody = "<alid>"+uri+"</alid>";
		} else if ( cmd.equals("load" ) ) {
		    String url = params.getProperty( "arg1" );
		    if ( url == null ){
			SOAPAction = "loadRequest";
			/* 
			BufferedReader in = new BufferedReader(new FileReader( new File(uploadFile) ));
			String line;
			String content = "";
			while ((line = in.readLine()) != null) {
			    content += line + "\n";
			}
			if (in != null) in.close();
			*/
			String content = fileToString(new File(uploadFile));
			
			return  content;
			
			//messageBody = "<content>"+content+"</content>";
		    } else {
			SOAPAction = "loadfileRequest";
			messageBody = "<url>"+url+"</url>";
		    }
		    /* This may read the input stream!
				// Most likely Web service request
				int length = request.getContentLength();
				char [] mess = new char[length+1];
				try { 
				    new BufferedReader(new InputStreamReader(request.getInputStream())).read( mess, 0, length);
				} catch (Exception e) {
				    e.printStackTrace(); // To clean up
				}
				params.setProperty( "content", new String( mess ) );
		    */
		} else if ( cmd.equals("retrieve" ) ) {
		    SOAPAction = "retrieveRequest";
		    String uri = params.getProperty( "arg1" );
		    String method = params.getProperty( "arg2" );
		    if ( method == null ){
			//usage();
			//System.exit(-1);
		    }
		    messageBody = "<alid>"+uri+"</alid><method>"+method+"</method>";
		} else if ( cmd.equals("metadata" ) ) {
		    SOAPAction = "metadata";
		    String uri = params.getProperty( "arg1" );
		    String key = params.getProperty( "arg2" );
		    if ( key == null ){
			//usage();
			//System.exit(-1);
		    }
		    messageBody = "<alid>"+uri+"</alid><key>"+key+"</key>";
		} else {
		    //usage();
		    //System.exit(-1);
		}
			// Create input message and URL
		String messageEnd = "</SOAP-ENV:Body>"+"</SOAP-ENV:Envelope>";
		String message = messageBegin + messageBody + messageEnd;
		return message;
	    }
	    
	    public String sendMessage( String message, Properties param )   {
	    	// Create the connection
	        	 
	        byte[] b = message.getBytes();
	        
	        String answer = "";
	        // Create HTTP Request
	        try {
	        	 
	    		URLConnection connection = SOAPUrl.openConnection();
	        	HttpURLConnection httpConn = (HttpURLConnection) connection;
	        	 
	            httpConn.setRequestProperty( "Content-Length",
	                                         String.valueOf( b.length ) );
	            httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
	            
	            
	            httpConn.setRequestProperty("SOAPAction",SOAPAction);
	            httpConn.setRequestMethod( "POST" );
	            httpConn.setDoOutput(true);
	            httpConn.setDoInput(true);

	            // Send the request through the connection
	            OutputStream out = httpConn.getOutputStream();
	            
	            //System.out.println("ResponseMessage= "+httpConn.getResponseMessage());
	           
	            out.write( b );
	        	out.close();
	        	
	        	//System.out.println("Message Length= "+String.valueOf( b.length ));
	        	
	            // Read the response and write it to standard output
	            InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
	            BufferedReader in = new BufferedReader(isr);
	        
	            String line;
	            StringBuffer strBuff = new StringBuffer();
		    while ((line = in.readLine()) != null) {
	            	 strBuff.append( line + "\n");
	            }
	            if (in != null) in.close();
	            answer = strBuff.toString();
	            
	            if(httpConn.HTTP_REQ_TOO_LONG == httpConn.getResponseCode()) System.err.println("Request too long");
	            
	            //if(httpConn.HTTP_OK == httpConn.getResponseCode()) System.out.println("Request OK");
	        	
	        } catch  (Exception ex) {
	        	connected= false; ex.printStackTrace() ; return null;
	        	}
	        
	        connected = true;
	    	return answer;
	    }
	    
	    public String sendFile( String message, Properties param )   {
	    	// Create the connection
	        	 
	        //byte[] b = message.getBytes();
	        
	        String answer = "";
	        // Create HTTP Request
	        try {
	        	 
	    		URLConnection connection = SOAPUrl.openConnection();
	        	HttpURLConnection httpConn = (HttpURLConnection) connection;
	        	
	            httpConn.setRequestProperty("SOAPAction",SOAPAction);
	            httpConn.setRequestMethod( "POST" );
	            httpConn.setDoOutput( true );
	            httpConn.setDoInput( true );
	            
	            // Don't use a cached version of URL connection.
	            httpConn.setUseCaches ( false );
	            httpConn.setDefaultUseCaches (false);
	            
	            File f = new File(uploadFile);
		    FileInputStream fi = new FileInputStream(f);
	            // set headers and their values.
	            httpConn.setRequestProperty("Content-Type",
	                                         "application/octet-stream");
	            httpConn.setRequestProperty("Content-Length",
	                                        Long.toString(f.length()));
	           
	            // create file stream and write stream to write file data.
	            
	            OutputStream os =  httpConn.getOutputStream();
		        String str ="";
	            try
	            {
	               // transfer the file in 4K chunks.
	               byte[] buffer = new byte[4096];
	               //long byteCnt = 0;
	               int bytes=0;
	               while (true)
	               {
	                  bytes = fi.read(buffer);
	                  
	                  if (bytes < 0)  break;
	                  
	                  os.write(buffer, 0, bytes );
	                  //String st =  new String( buffer );
					  //str = str + st.substring(0, bytes);
					  //System.out.println("st="+st.substring(0, bytes));
	               }
	               
	               
	               os.flush();
	            } catch (Exception ex) {}
	            
	            os.close();
	            fi.close();
				//System.out.println("Upload Read done.");
	        	
				
	            // Read the response  
	            InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
	            BufferedReader in = new BufferedReader(isr);
	        
	            String line;
	            while ((line = in.readLine()) != null) {
	            	answer += line + "\n";
	            }
	            if (in != null) in.close();
	            
	        	
	        } catch  (Exception ex) {
	        	connected= false; ex.printStackTrace() ; return null;
	        	}
	        
	        connected = true;
	    	return answer;
	    }

	public static String fileToString(File f){
	String texto = "";
	int i=0;
	try{
	   
	   FileReader rd = new FileReader(f);
	   i = rd.read();
	    
	     while(i!=-1){
	          texto = texto+(char)i;
	          i = rd.read();
	     }
	 

	   }catch(IOException e){
	    System.err.println(e.getMessage());
	     }
	   
	return texto;
	}
}
