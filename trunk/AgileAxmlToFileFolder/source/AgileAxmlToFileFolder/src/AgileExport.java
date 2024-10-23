import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class AgileExport {

    public static Logger logger = Logger.getLogger(AgileExport.class);

    public Properties prop = null;
    public Map<String, String> bomIdMap = null;

    public static void main(String[] args) {

        try {
            AgileExport ae = new AgileExport();
            ae.runMe();
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            e.printStackTrace();
        }

    }

    public void runMe() throws Exception {

        prop = loadPropertyFile("config\\app.properties");
        //prop = loadPropertyFile("F:/Projects/AG Leader/CSV Export/ExportUtility/config/app.properties");
        PropertyConfigurator.configure(prop);

        String inputFolderPath = prop.getProperty("inputFolder");
        String tempFolderPath = prop.getProperty("tempFolder");
        String archiveFolderPath = prop.getProperty("archiveFolder");
        String extension = prop.getProperty("extension");

        logger.info("inputFolder: " + inputFolderPath);
        logger.info("tempFolder: " + tempFolderPath);
        logger.info("archiveFolder: " + archiveFolderPath);
        

        File rtempFolder = new File(tempFolderPath);
        if (!rtempFolder.exists()) {
            rtempFolder.mkdirs();
        }
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }

        File inputFolder = new File(inputFolderPath);
        File[] inputFiles = inputFolder.listFiles();
        for (File inputFile : inputFiles) {
            if (inputFile.isDirectory()) {
                continue;
            }

            try {
                
                String filename = inputFile.getName();
                if (filename.contains(".")) {
                    filename = filename.substring(0, filename.lastIndexOf("."));
                }
                filename += extension;

                logger.info("File found: " + filename);

                File tempFolder = new File(rtempFolder.getAbsolutePath() + "\\" + filename);
                if (tempFolder.exists()) {
                    for (File file : tempFolder.listFiles()) {
                        file.delete();
                    }
                    tempFolder.delete();
                }
                tempFolder.mkdir();

                String extractedFilePath = unZipIt(inputFile, tempFolder.getAbsolutePath());
                logger.info("Extracted to " + extractedFilePath);

                String exportFilepath = tempFolder.getAbsolutePath() + "\\" + filename;
                File tempExportFile = new File(exportFilepath);
                OutputStream outStream = new FileOutputStream(tempExportFile);

                String xslFilePath = prop.getProperty("xslFilepath");
                logger.info("XSL File Path: " + xslFilePath);
                Source xsl = new StreamSource(new File(xslFilePath));
                Source xml = new StreamSource(new File(extractedFilePath));
                Result out = new StreamResult(outStream);

                TransformerFactory fact = new net.sf.saxon.TransformerFactoryImpl();
                Transformer transformer = fact.newInstance().newTransformer(xsl);
                transformer.transform(xml, out);
                logger.info("Transformed");
                logger.info("Profile completed");
                
                outStream.flush();
                outStream.close();
                
                File targetFile = new File(prop.getProperty("outputFolder") + "/" + filename);

                if (targetFile.exists()) {
                    targetFile.delete();
                }

                FileUtils.copyFile(tempExportFile, targetFile);

                for (File file : tempFolder.listFiles()) {
                    file.delete();
                }
                tempFolder.delete();
                

                SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMdd_HHmmss");
                String timestamp = sdf.format(new Date());
                File archive = new File(archiveFolderPath + "/" + inputFile.getName() + timestamp);
                FileUtils.moveFile(inputFile, archive);

                logger.info("file processing completed");
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error processing file " + inputFile, e);
            }
        }

        long purgeTime = System.currentTimeMillis() - (60L * 60L * 1000L);
        cleanup(rtempFolder, purgeTime);

        logger.info("Execution completed");
    }

    public static void cleanup(File file, long purgeTime) {
        if (file.isDirectory()) {
            for (File childFiles : file.listFiles()) {
                cleanup(childFiles, purgeTime);
            }

        }
        if (file.lastModified() < purgeTime) {
            if (!file.delete()) {
                logger.warn("Could not delete " + file.getAbsolutePath());
            }
        }
    }

    public static String unZipIt(File zipFile, String outputFolder) {

        String outputFile = null;

        byte[] buffer = new byte[1024];

        try {

            // get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            // get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                logger.info("file unzip : " + newFile.getAbsoluteFile());

                // create all non exists folders
                // else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();

                outputFile = newFile.getAbsolutePath();
            }

            zis.closeEntry();
            zis.close();

            logger.info("Done");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return outputFile;
    }

    public static Properties loadPropertyFile(String path) {
        System.out.println("Loading properties file: " + path);
        Properties properties = new Properties();
        InputStream stream = null;
        try {
            stream = new FileInputStream(path);
            properties.load(stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // Ignoring exception
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }
}
