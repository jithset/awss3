package io.github.awss3.utils;

import org.springframework.context.annotation.Configuration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

@Configuration
public class AwsS3Utils {
    private final Logger log = LoggerFactory.getLogger(AwsS3Utils.class);

    @Autowired
    private AmazonS3 s3Client;

    @Value("${cloud.aws.bucket-name}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    /*public String saveFile(String fileName, File fileObj) {
        s3Client.putObject(bucketName, fileName, fileObj);
        fileObj.delete();
        return createPublicUrl(fileName);
    }

    public String saveFile(String baseFolder, MultipartFile file) {
        File fileObj = convertMultiPartFileToFile(file);
        String fileName = baseFolder + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Client.putObject(bucketName, fileName, fileObj);
        fileObj.delete();
        return createPublicUrl(fileName);
    }*/

    public String updateFile(String oldPath, String newPath, MultipartFile file) {
        deleteObject(oldPath);
        saveFile(newPath, file);
        return newPath;
    }

    public String saveFile(String fullPath, MultipartFile file) {
        File fileObj = convertMultiPartFileToFile(file);
        s3Client.putObject(bucketName, fullPath, fileObj);
        fileObj.delete();
        return fullPath;
    }

    public String saveFile(String rootFolder, String fileName, MultipartFile file) {
        String path = rootFolder + "/" + fileName;
        return saveFile(path, file);
    }

    public CopyObjectResult copyObject(String source, String destination) {
        CopyObjectRequest req = new CopyObjectRequest(bucketName, source, bucketName, destination);
        return s3Client.copyObject(req);
    }

    public void deleteObject(String source) {
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, source);
        s3Client.deleteObject(deleteObjectRequest);
    }

    public void moveObject(String source, String destination) {
        copyObject(source, destination);
        deleteObject(source);
    }

    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String createPublicUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }
}
