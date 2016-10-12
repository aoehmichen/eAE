package eae.plugin

import grails.transaction.Transactional
import grails.util.Environment
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Transactional
class EaeDataService {

    def DEBUG =  Environment.current == Environment.DEVELOPMENT
    def DEBUG_TMP_DIR = '/tmp/'
    private static final String SOURCE_FOLDER = "/tmp/eae/";

    def grailsApplication = Holders.grailsApplication
    def springSecurityService
    def i2b2HelperService
    def dataQueryService

    /**
     *  The method retrieves the selected data from tranSMART database.
     * @param params
     * @return {dict}
     */
    def queryData(params) {

        def parameterMap = createParameterMap(params)
        def data_cohort1 = [:]
        def data_cohort2 = [:]

        def rIID1 = parameterMap['result_instance_id1'].toString()
        def rIID2 = parameterMap['result_instance_id2'].toString()

        def patientIDs_cohort1 = rIID1 ? i2b2HelperService.getSubjectsAsList(rIID1).collect { it.toLong() } : []
        def size_cohort1 = patientIDs_cohort1.size();
        def patientIDs_cohort2 = rIID2 ? i2b2HelperService.getSubjectsAsList(rIID2).collect { it.toLong() } : []
        def size_cohort2 = patientIDs_cohort2.size();

        parameterMap['patientids_cohort1'] = patientIDs_cohort1.sort().join(' ')
        parameterMap['patientids_cohort2'] = patientIDs_cohort2.sort().join(' ')
        parameterMap['size_cohort1'] = size_cohort1
        parameterMap['size_cohort2'] = size_cohort2

        parameterMap['conceptBoxes'].each { conceptBox ->
            conceptBox.cohorts.each { cohort ->
                def rIID
                def data
                def patientIDs

                if (cohort == 1) {
                    rIID = rIID1
                    patientIDs = patientIDs_cohort1
                    data = data_cohort1
                } else {
                    rIID = rIID2
                    patientIDs = patientIDs_cohort2
                    data = data_cohort2
                }

                if (! rIID || ! patientIDs) {
                    return
                }

                if (conceptBox.concepts.size() == 0) {
                    data[conceptBox.name] = [:]
                } else if (conceptBox.type == 'valueicon' || conceptBox.type == 'alphaicon') {
                    data[conceptBox.name] = dataQueryService.getAllData(conceptBox.concepts, patientIDs)
                } else if (conceptBox.type == 'hleaficon') {
                    def rawData = dataQueryService.exportHighDimData(
                            conceptBox.concepts,
                            patientIDs,
                            rIID as Long)
                    data[conceptBox.name] = rawData
                } else {
                    throw new IllegalArgumentException()
                }
            }
        }

        parameterMap['data_cohort1'] = new JsonBuilder(data_cohort1).toString()
        parameterMap['data_cohort2'] = new JsonBuilder(data_cohort2).toString()

        if (DEBUG) {
            new File(DEBUG_TMP_DIR + 'data1.json').write(parameterMap['data_cohort1'])
            new File(DEBUG_TMP_DIR + 'data2.json').write(parameterMap['data_cohort2'])
        }

        return parameterMap
    }


    def createParameterMap(params){
        def parameterMap = [:]
        parameterMap['result_instance_id1'] = params.result_instance_id1
        parameterMap['result_instance_id2'] = params.result_instance_id2
        parameterMap['conceptBoxes'] = new JsonSlurper().parseText(params.conceptBoxes)
        parameterMap['DEBUG'] = DEBUG
        return parameterMap
    }

    /**
     * Intermediary method for the transfer of the data files to HDFS
     * @param username
     * @param mongoDocumentID
     * @param workflow
     * @param data
     * @param scriptDir
     * @param sparkURL
     * @param typeOfFile
     * @return
     */
    def sendToHDFS(String username, String mongoDocumentID, String workflow, data, String scriptDir, String sparkURL, String typeOfFile){
        def fileToTransfer = "";
        switch (typeOfFile) {
            case "data":
                fileToTransfer = sendDataToHDFS( username, mongoDocumentID, workflow, data,  scriptDir, sparkURL);
                break;
            case "additional":
                fileToTransfer = sendAdditionalToHDFS( username, mongoDocumentID, workflow, data,  scriptDir, sparkURL);
                break;
        }
        return fileToTransfer;
    }

    /**
     * This method exectues on the flight curation for the data to fit the required format by the workflow.
     * @param username
     * @param mongoDocumentID
     * @param workflow
     * @param data
     * @param scriptDir
     * @param sparkURL
     * @return
     */
    def  sendDataToHDFS (String username, String mongoDocumentID, String workflow, data, String scriptDir, String sparkURL) {
//        def script = scriptDir +'transferToHDFS.sh';
        def fileToTransfer = workflow + "-" + username + "-" + mongoDocumentID + ".txt";
        String fp;

//        def scriptFile = new File(script);
//        if (scriptFile.exists()) {
//            if(!scriptFile.canExecute()){
//                scriptFile.setExecutable(true)
//            }
//        }else {
//            log.error('The Script file to transfer to HDFS wasn\'t found')
//        }

        File f = new File("/tmp/eae/",fileToTransfer);
        if(f.exists()){
            f.delete();
        }

        switch (workflow) {
            case "GeneralTesting":
                fp = writeGTFile(f, data);
                break;
            case "CrossValidation":
                int size_cohort1 = (int)data['size_cohort1'];
                int size_cohort2 = (int)data['size_cohort2'];
                def data_cohort1 = data['data_cohort1'];
                def data_cohort2 = data['data_cohort2'];
                f = writeCVFile(f, size_cohort1, data_cohort1, "0");
                f = writeCVFile(f, size_cohort2, data_cohort2, "1");
                f.createNewFile()
                fp = f.getAbsolutePath()
                break;
            case "LabelPropagation":
                fp = writeLPFile(f, data);
                break;
        }

//        def executeCommand = script + " " + fp + " "  + fileToTransfer + " " + sparkURL;
//        executeCommand.execute().waitFor();
//
//        // We cleanup
//        f.delete();

        return fileToTransfer
    }

    /**
     * This method writes the retrived values to file.
     * @param f
     * @param size_cohort
     * @param data_cohort
     * @param k
     * @return {file}
     */
    def writeCVFile(File f, int size_cohort, data_cohort, String k){
        def JSONcohort = new JsonSlurper().parseText(data_cohort);
        def data_value = JSONcohort.highDimDataCV.VALUE as Float[];
        def data_size = data_value.size();
        int chunkSize = data_size/size_cohort;

        for (int i=0; i<size_cohort; i++){
            Float[] subArray = data_value[i*chunkSize..(i+1)*chunkSize-1];
            String line = k + ' ' + subArray.join(' ');
            f.withWriterAppend('utf-8') { writer ->
                writer.writeLine line
            }
        }

        return f
    }

    /**
     * This method exectues on the flight curation for the additional data to fit the required format by the workflow.
     * @param username
     * @param mongoDocumentID
     * @param workflow
     * @param data
     * @param scriptDir
     * @param sparkURL
     * @return
     */
    def  sendAdditionalToHDFS(String username, String mongoDocumentID, String workflow, data, String scriptDir, String sparkURL){
//        def script = scriptDir +'transferToHDFS.sh';
        def fileToTransfer = workflow + "-additional" + "-" + username + "-" + mongoDocumentID + ".txt";
        String fp;

//        def scriptFile = new File(script);
//        if (scriptFile.exists()) {
//            if(!scriptFile.canExecute()){
//                scriptFile.setExecutable(true)
//            }
//        }else {
//            log.error('The Script file to transfer to HDFS wasn\'t found')
//        }

        File f = new File("/tmp/eae/",fileToTransfer);
        if(f.exists()){
            f.delete();
        }

        switch (workflow) {
            case "GeneralTesting":
                fp = writeGTFile(f, data);
                break;
            case "CrossValidation":
                int size_cohort = (int)data['size_cohort1'];
                def JSONcohort = new JsonSlurper().parseText(data['data_cohort1']);
                def data_value = JSONcohort.highDimDataCV.PROBE as String[];
                def data_size = data_value.size();
                int chunkSize = data_size/size_cohort;
                String[] subArray = data_value[0..chunkSize-1];
                String line = subArray.join(' ');
                f.withWriterAppend('utf-8') { writer ->
                    writer.writeLine line
                }
                break;
            case "LabelPropagation":
                fp = writeLPFile(f, data);
                break;
        }

        f.createNewFile()
        //fp = f.getAbsolutePath()
//        def executeCommand = script + " " + fp + " "  + fileToTransfer + " " + sparkURL;
//        println(executeCommand)
//        executeCommand.execute().waitFor();
//
//        // We cleanup
//        f.delete();

        return fileToTransfer
    }


    def zipFiles(ArrayList<String> filesToZip, String zipName){
        byte[] buffer = new byte[1024];

        try{

            FileOutputStream fos = new FileOutputStream(zipName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            System.out.println("Output to Zip : " + zipName);

            for(String file : filesToZip){

                System.out.println("File Added : " + file);
                ZipEntry ze= new ZipEntry(file);
                zos.putNextEntry(ze);

                FileInputStream fileIn = new FileInputStream(SOURCE_FOLDER + file);

                int len;
                while ((len = fileIn.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                fileIn.close();
            }

            zos.closeEntry();
            //remember close it
            zos.close();

            System.out.println("Done");
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return "OK"
    }
}
