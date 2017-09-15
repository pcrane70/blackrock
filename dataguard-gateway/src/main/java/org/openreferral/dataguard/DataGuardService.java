package org.openreferral.dataguard;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value="/dataguard/api")
@Api(value="dataguard", description="Operations pertaining to Data Steward Gateway")
public class DataGuardService {
	
	@Autowired
	private EmailService emailService;

	
    //@Value("${conflictdir}")
	private String conflictDirectory;
	
	public DataGuardService() {
		conflictDirectory= System.getProperty("conflictdir");
		conflictDirectory = conflictDirectory.replace("-noverify", ""); //bug on spring boot version
	}
	
	@ApiOperation(value = "View a list of available conflicts", response = Iterable.class)
	@RequestMapping(value="/conflicts",method = RequestMethod.GET,produces = "application/json")
	public List<String>  getConflictsList(){
		List<String> conflictsList = new ArrayList<String>();
		try {
			Files.newDirectoryStream(Paths.get(conflictDirectory), path -> path.toFile().isDirectory())
	           .forEach(p-> {
	        	   conflictsList.add(p.getFileName().toString());
			        	
			        });
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return conflictsList;
	}
	
	@ApiOperation(value = "View a details of available conflicts for an entity", response = Iterable.class)
	@RequestMapping(value="/conflicts/{id}",method = RequestMethod.GET,produces = "application/json")
	public Map<String,Object> getConflictsDetails(@PathVariable String id) throws Exception {
		Map<String,Object> conflictsMap = new HashMap<String,Object>();
		try {
			Files.newDirectoryStream(Paths.get(conflictDirectory+"/"+id),
			        path -> path.toString().endsWith(".hsds"))
			        .forEach(p-> {
			        	try {
			        		ObjectMapper objectMapper = new ObjectMapper();
			        		Object json = objectMapper.readValue(p.toFile(), Object.class);
							conflictsMap.put(p.getFileName().toString(),json);
			        	}catch (Exception e) {
							// TODO: handle exception
			        		e.printStackTrace();
						}
			        	
//						        	String content;
//									try {
//										content = new String(Files.readAllBytes(p));
//										conflictsMap.put(p.getFileName().toString(),content);
//									} catch (IOException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
			        					        	
				        });
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return conflictsMap;
	}
	
	@ApiOperation(value = "Create new or Update an entity after resolving any conflicts for an entity", response = String.class)
	@RequestMapping(value = "/updateEntity/{id}/{sourceId:.+}", method = RequestMethod.PUT)
    public String updateEntity(@PathVariable String id, @PathVariable String sourceId,@RequestBody String hsdsJSON){
        //rename the old file to previous version
		Path source  = Paths.get(conflictDirectory + "/"+id+"/"+sourceId);
		// if a files exists create a version of it
		if ( source.toFile().exists()) {
			
			int maxCount = 20;
			int currentVersion=1;
			Path target = Paths.get(source.toAbsolutePath().toString() + ".V" + currentVersion++  );
			
			while ((target.toFile().exists()) && currentVersion < maxCount ) {			
				target  = Paths.get(source.toAbsolutePath().toString() + ".V" + currentVersion++  );
			}
			
			try {
			    Files.move(source, target);
			} catch(FileAlreadyExistsException fae) {
			    fae.printStackTrace();
			} catch (IOException e) {
			    // something else went wrong
			    e.printStackTrace();
			}
		}
		
		//persist the new version
		//Use try-with-resource to get auto-closeable writer instance
		try (BufferedWriter writer = Files.newBufferedWriter(source))
		{
		    writer.write(hsdsJSON);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        return "SUCCESS";
    }
	
	
	@ApiOperation(value = "Archive an entity after resolving any conflicts for an entity", response = String.class)
	@RequestMapping(value = "/archiveEntity/{id}/{sourceId:.+}", method = RequestMethod.PUT)
    public String archiveEntity(@PathVariable String id, @PathVariable String sourceId){
        //rename the old file to previous version
		Path source  = Paths.get(conflictDirectory + "/"+id+"/"+sourceId);
		// if a files exists create a version of it
		if ( source.toFile().exists()) {
			Path target = Paths.get(source.toAbsolutePath().toString() + ".ARCH"  );
			try {
			    Files.move(source, target);
			} catch(FileAlreadyExistsException fae) {
			    fae.printStackTrace();
			} catch (IOException e) {
			    // something else went wrong
			    e.printStackTrace();
			}
		}
		
	
        return "SUCCESS";
    }
	
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Increase Confidence Approval", response = String.class)
	@RequestMapping(value = "/increaseConfidence/{id}/{sourceId:.+}", method = RequestMethod.PUT)
    public String increaseConfidence(@PathVariable String id, @PathVariable String sourceId){
      
		Path source  = Paths.get(conflictDirectory + "/"+id+"/"+sourceId);
		// check if a file exists
		if ( source.toFile().exists()) {
			try {
			ObjectMapper objectMapper = new ObjectMapper();
           List<Object> updatedJsonList = new ArrayList<Object>();
			Object json = objectMapper.readValue(source.toFile(), Object.class);
			if ( json != null && json instanceof List) {
				((List<LinkedHashMap<String,Object>>)json).forEach( hsdsMap -> {
					try {
						if ( hsdsMap != null ) {
							hsdsMap.put("approvalCount", String.valueOf( Integer.parseInt((String)hsdsMap.getOrDefault("approvalCount","0")) + 1));
							updatedJsonList.add(hsdsMap);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				});
			}
			
			if ( updatedJsonList != null && updatedJsonList.size() > 0 ) {
				objectMapper.writeValue(source.toFile(), updatedJsonList);
			}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			return "IGNORED";
		}
        return "SUCCESS";
    }

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Decrease Confidence Approval", response = String.class)
	@RequestMapping(value = "/decreaseConfidence/{id}/{sourceId:.+}", method = RequestMethod.PUT)
    public String decreaseConfidence(@PathVariable String id, @PathVariable String sourceId){
		
		Path source  = Paths.get(conflictDirectory + "/"+id+"/"+sourceId);
		// check if a file exists
		if ( source.toFile().exists()) {
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				List<Object> updatedJsonList = new ArrayList<Object>();
				Object json = objectMapper.readValue(source.toFile(), Object.class);
				if ( json != null && json instanceof List) {
					((List<LinkedHashMap<String,Object>>)json).forEach( hsdsMap -> {
					try {
						if ( hsdsMap != null ) {
							hsdsMap.put("disApprovalCount", String.valueOf( Integer.parseInt((String)hsdsMap.getOrDefault("disApprovalCount","0")) + 1));
							updatedJsonList.add(hsdsMap);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				});
			}
			
			if ( updatedJsonList != null && updatedJsonList.size() > 0 ) {
				objectMapper.writeValue(source.toFile(), updatedJsonList);
			}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			return "IGNORED";
		}
        return "SUCCESS";
    }

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Notify to orgs all the sources part of conflicts list", response = String.class)
	@RequestMapping(value = "/notifyOrgsByEmail/{id}", method = RequestMethod.POST)
    public String notifyOrgsByEmail(@PathVariable String id){
		
		try {
			Files.newDirectoryStream(Paths.get(conflictDirectory+"/"+id),
			        path -> path.toString().endsWith(".hsds"))
			        .forEach(p-> {		   
				// get email and update if a file and contact exists 
				if ( p.toFile().exists()) {
						
						try {
							ObjectMapper objectMapper = new ObjectMapper();
				           List<Object> updatedJsonList = new ArrayList<Object>();
							Object json = objectMapper.readValue(p.toFile(), Object.class);
							if ( json != null && json instanceof List) {
								((List<LinkedHashMap<String,Object>>)json).forEach( hsdsMap -> {
									try {
										if ( hsdsMap != null ) {
											String email = (String) hsdsMap.get("email");
										       if (null !=email){
										    	   //email send
										    	   emailService.sendMail(email,"Open Referral","Watchdog mail");
										    	   hsdsMap.put("lastContacted-Org" + new Date(), email );
												   updatedJsonList.add(hsdsMap);
										       }
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
								});
							}
							
							if ( updatedJsonList != null && updatedJsonList.size() > 0 ) {
								objectMapper.writeValue(p.toFile(), updatedJsonList);
							}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	
				}
			    	
			   });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        return "SUCCESS";
    }

	
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Notify to Contacts all the sources part of conflicts list", response = String.class)
	@RequestMapping(value = "/notifyContactsByEmail/{id}", method = RequestMethod.POST)
    public String notifyContactsByEmail(@PathVariable String id){
		
		try {
			Files.newDirectoryStream(Paths.get(conflictDirectory+"/"+id),
			        path -> path.toString().endsWith(".hsds"))
			        .forEach(p-> {		   
				// get email and update if a file and contact exists 
				if ( p.toFile().exists()) {
						
						try {
							ObjectMapper objectMapper = new ObjectMapper();
				           List<Object> updatedJsonList = new ArrayList<Object>();
							Object json = objectMapper.readValue(p.toFile(), Object.class);
							if ( json != null && json instanceof List) {
								((List<LinkedHashMap<String,Object>>)json).forEach( hsdsMap -> {
									try {
										List<String> contactEmailList = new ArrayList<String>();
										if ( hsdsMap != null ) {
										
											List<Map<String, String>> contactList = (List<Map<String, String>>) hsdsMap.get("contacts");
											contactList.forEach(c -> {
												String email = c.get("email");
											
											       if (null !=email){
											    	   //email send
											    	   emailService.sendMail(email,"Open Referral","Watchdog mail");
											    	   contactEmailList.add(email);											    	 
											       }
											});
											
											if (contactEmailList != null && contactEmailList.size() > 0) {
												 hsdsMap.put("lastContacted-Contact -"+ new Date(), contactEmailList  );//TODO
												 updatedJsonList.add(hsdsMap);
											}
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
								});
							}
							
							if ( updatedJsonList != null && updatedJsonList.size() > 0 ) {
								objectMapper.writeValue(p.toFile(), updatedJsonList);
							}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	
				}
			    	
			   });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        return "SUCCESS";
    }
	

}
