package com.xavor.agile.exports;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.agile.api.AgileSessionFactory;
import com.agile.api.ExportConstants;
import com.agile.api.IAgileSession;
import com.agile.api.IChange;
import com.agile.api.IDataObject;
import com.agile.api.IExportManager;
import com.agile.api.INode;
import com.agile.px.ActionResult;
import com.agile.px.EventActionResult;
import com.agile.px.IEventAction;
import com.agile.px.IEventInfo;
import com.agile.px.IObjectEventInfo;
import com.xavor.agile.Utils;

public class AxmlExporter implements IEventAction {

	private Properties props = null;
	private volatile static Logger log = null;
	
	/**
	 * initializes properties and gets logger
	 * @param oChange
	 * @throws Exception
	 */
	private void initPropsAndLog()throws Exception{
		// see if the properties are not available then load
		//if(props == null){
			props = Utils.getProjectProperties(true);
			if(props == null){
				log.info("XAVOR:Cannot initialize Properties.");
				throw new Exception("PX fails: Cannot inistialize properties please see logs");
			}
		//}
		log = Utils.getLogger(props);
		log.info("Properties loaded, logger initialized");
	}
	
	public EventActionResult doAction(IAgileSession session, INode node,IEventInfo req) {

		EventActionResult eventActionResult = null;
		ActionResult actionResult = null;
		IDataObject dataObject = null;
		try {
			initPropsAndLog();
			IObjectEventInfo objectInfo = (IObjectEventInfo) req;
			dataObject = objectInfo.getDataObject();
			log.info("request recieved to export ["+dataObject+"]");
			String exportMsg = exportAndAttach(session, dataObject);
			log.info(exportMsg);
			actionResult = new ActionResult(ActionResult.STRING, exportMsg);
		} catch (Throwable t) {
			log.error("Error in exporting ["+dataObject+"]",t);
			actionResult = new ActionResult(ActionResult.EXCEPTION, t);
		}
		eventActionResult = new EventActionResult(req, actionResult);
		return eventActionResult;
	}
	
	private String exportAndAttach(IAgileSession session, IDataObject dataObject) throws Exception{
		IDataObject[] expObjs = { dataObject };
		String exportFilters = Utils.getPropertyValueWithDefault(props, "export.filters", "AG Leader Changes;AG Leader Items");
		log.info("Export Filters ["+exportFilters+"]");
		String[] filters = exportFilters.split(";");
		IExportManager eMgr = (IExportManager) session.getManager(IExportManager.class);
		byte[] exportData = eMgr.exportData(expObjs,ExportConstants.EXPORT_FORMAT_AXML, filters);
		String msg = "";
		log.info("exportData:"+exportData);
		if (exportData != null) {
			log.info("exportData byte length:"+exportData.length);
			String exportDir = Utils.getPropertyValueWithDefault(props, "export.directory", "D:/exports");
			log.info("Export Directory ["+exportDir+"]");
			if(!exportDir.endsWith(File.separator)){
				exportDir += File.separator;
			}
			String fileName = exportDir+""+dataObject + "_"+ System.currentTimeMillis()+".xml";
			log.info("Export path and file Name ["+fileName+"]");
			ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(exportData));
			ZipEntry entry = null;
			File unzippedFile = null;
			if ((entry = zipStream.getNextEntry()) != null) {
			    String entryName = entry.getName();
			    log.info("Zipped File Name:"+entryName);
			    unzippedFile = new File(fileName);
			    OutputStream out = null;
				try {
					out = new FileOutputStream(unzippedFile);
					byte[] byteBuff = new byte[4096];
					int bytesRead = 0;
					while ((bytesRead = zipStream.read(byteBuff)) != -1) {
					    out.write(byteBuff, 0, bytesRead);
					}
					log.info("Data exported to file: " + fileName);
					msg = dataObject+" has been exported to Folder ["+fileName+"]";
				} catch (Exception e) {
					log.error("Error in writing file ",e);
					throw e;
				}finally{
					try {
						if(out != null) {
							out.flush();
							out.close();
						}
						zipStream.closeEntry();
						zipStream.close();
					} catch (Exception e) {
						log.error("Error in finalizing streams");
					} 
				}
			}			
		}
		return msg;
	}
	
	public static void main(String[] args) {
		System.setProperty("disable.agile.sessionID.generation", "true"); 
		Map<Object, Object> params = new HashMap<Object, Object>(); 
		params.put(AgileSessionFactory.USERNAME, "aansari"); 
		params.put(AgileSessionFactory.PASSWORD, "*****"); 
		IAgileSession session = null;
		try {
			AxmlExporter exporter = new AxmlExporter();
			exporter.initPropsAndLog();
			log.info("connecting.....!");
			AgileSessionFactory factory = AgileSessionFactory.getInstance("http://agile933.xavor.com:7001/Agile/"); 
			session = factory.createSession(params);
			log.info("connected!");
			IChange chng = (IChange) session.getObject(IChange.OBJECT_TYPE, "C02106");
			log.info("Exporting ["+chng+"]");
			 
			exporter.exportAndAttach(session, chng);
		} catch (Exception e) {
			
			e.printStackTrace();
		}finally{
			if(session != null){
				session.close();
			}
		}
	}

}
