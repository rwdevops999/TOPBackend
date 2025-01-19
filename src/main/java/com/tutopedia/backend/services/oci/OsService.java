package com.tutopedia.backend.services.oci;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.Bucket;
import com.oracle.bmc.objectstorage.model.CreateBucketDetails;
import com.oracle.bmc.objectstorage.requests.CreateBucketRequest;
import com.oracle.bmc.objectstorage.requests.DeleteBucketRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreateBucketResponse;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest;
import com.tutopedia.backend.persistence.model.Setting;
import com.tutopedia.backend.services.CommandService;
import com.tutopedia.backend.services.QueryService;

import enums.SettingKeys;

@Service
public class OsService {
	// Path to OCI configuration file
    final String configurationFilePath = "~/.oci/config";
    final String profile = "DEFAULT";

    @Autowired
    CommandService commandService;
    
    @Autowired
    QueryService queryService;
    
    public void initialize() throws IOException {
        final ConfigFileReader.ConfigFile 
        configFile = ConfigFileReader
        .parse(configurationFilePath, profile);

        System.out.println("===== INIT OCI =====");
        Setting setting;
        Optional<Setting> dbSetting = queryService.findSettingByKeyAndType(SettingKeys.TENANT.getKey(), SettingKeys.TENANT.getType());
        if (dbSetting.isEmpty()) {
        	setting = new Setting(SettingKeys.TENANT.getKey(), null, SettingKeys.TENANT.getType());
        } else {
        	setting = dbSetting.get();
        }
        setting.setValue(configFile.get("tenancy"));
        System.out.println("===== PERSIST TENANCY =====");
        System.out.println(configFile.get("tenancy"));
    	commandService.persistSetting(setting);
        
        dbSetting = queryService.findSettingByKeyAndType(SettingKeys.REGION.getKey(), SettingKeys.REGION.getType());
        if (dbSetting.isEmpty()) {
            setting = new Setting(SettingKeys.REGION.getKey(), null, SettingKeys.REGION.getType());
        } else {
        	setting = dbSetting.get();
        }
    	setting.setValue(configFile.get("region"));
        System.out.println("===== PERSIST REGION =====");
        System.out.println(configFile.get("region"));
        commandService.persistSetting(setting);
    }
    
    public ObjectStorage getObjectStorage() throws IOException {
        
        // load configuration file
        final ConfigFileReader.ConfigFile 
        configFile = ConfigFileReader
        .parse(configurationFilePath, profile);

        final ConfigFileAuthenticationDetailsProvider provider =
            new ConfigFileAuthenticationDetailsProvider(configFile);

        //build and return client
        return ObjectStorageClient.builder().build(provider);
    }
    
    public String createBucket(String compartmentId, String bucketName) throws IOException {
    	ObjectStorage client = getObjectStorage();
    	
    	// get the namespace
        GetNamespaceResponse namespaceResponse = client.getNamespace(GetNamespaceRequest.builder().build());
        String namespaceName = namespaceResponse.getValue();

        CreateBucketDetails createSourceBucketDetails =
                CreateBucketDetails.builder().compartmentId(compartmentId).name(bucketName).build();
        CreateBucketRequest createSourceBucketRequest =
                CreateBucketRequest.builder()
                        .namespaceName(namespaceName)
                        .createBucketDetails(createSourceBucketDetails)
                        .build();
        CreateBucketResponse response = client.createBucket(createSourceBucketRequest);
        Bucket bucket = response.getBucket();
        
        return bucket.getName();
        
    }

    public void deleteBucket(String bucketName) throws IOException {
    	ObjectStorage client = getObjectStorage();
    	
    	// get the namespace
        GetNamespaceResponse namespaceResponse = client.getNamespace(GetNamespaceRequest.builder().build());
        String namespaceName = namespaceResponse.getValue();
        
        DeleteBucketRequest deleteSourceBucketRequest = DeleteBucketRequest.builder()
                .namespaceName(namespaceName)
                .bucketName(bucketName).build();
        		
        client.deleteBucket(deleteSourceBucketRequest);
    }

    private static File convertMultipartToFile(MultipartFile multipartFile, String fileName) throws IllegalStateException, IOException {
    	File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
    	multipartFile.transferTo(convFile);
    	return convFile;
    }
    
    public void uploadFile(String bucketName, MultipartFile file, String fileName) {
        try {
        	ObjectStorage client = getObjectStorage();

            GetNamespaceResponse namespaceResponse = client.getNamespace(GetNamespaceRequest.builder().build());
            String namespaceName = namespaceResponse.getValue();

            String contentType = file.getContentType();
            File body = convertMultipartToFile(file, fileName);

            UploadConfiguration uploadConfiguration = UploadConfiguration.builder()
                    .allowMultipartUploads(true)
                    .allowParallelUploads(true)
                    .lengthPerUploadPart(16)
                    .build();

            UploadManager uploadManager = new UploadManager(client, uploadConfiguration);
            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .bucketName(bucketName)
                            .namespaceName(namespaceName)
                            .objectName(fileName)
                            .contentType(contentType).build();

            UploadRequest uploadRequest = UploadRequest.builder(body)
                    .allowOverwrite(true)
                    .build(putObjectRequest);
            
            uploadManager.upload(uploadRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OciStorageException();
        }
    }

    public void deleteFile(String bucketName, String fileName) {
        try {
        	ObjectStorage client = getObjectStorage();

            GetNamespaceResponse namespaceResponse = client.getNamespace(GetNamespaceRequest.builder().build());
            String namespaceName = namespaceResponse.getValue();

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            		.bucketName(bucketName)
            		.namespaceName(namespaceName)
            		.objectName(fileName)
            		.build();

            client.deleteObject(deleteObjectRequest);
            
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new OciStorageException();
        }
    }
}
