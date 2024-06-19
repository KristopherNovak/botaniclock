package com.krisnovak.springboot.demo.planttracker.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.krisnovak.springboot.demo.planttracker.entity.Plant;
import com.krisnovak.springboot.demo.planttracker.entity.Session;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

/**
 * Class used to represent the functionality of an S3 bucket
 */
public class S3Bucket {
    //The client being used to connect to S3
    private AmazonS3 s3Client;
    //The name of the bucket that is being connected to
    private String bucketName;

    //S3 key size in characters
    private static final int S3_KEY_SIZE = 100;

    //Expiration time in milliseconds
    private static final int DURATION_BEFORE_EXPIRATION_IN_MILLISECONDS = 60*1000;

    /**
     * Function that initializes the S3 bucket according to the properties in the provided file.
     * @param s3PropertiesFile The file that includes the s3 properties (should be in a .properties format)
     * and should include an "accessKey" field, a "secretKey" field, and a "bucketName" field
     * @throws IOException Thrown if the file is not located or if the file is in the wrong format
     */
    public S3Bucket(File s3PropertiesFile) throws IOException{

        Properties s3Properties = new Properties();
        s3Properties.load(new FileReader(s3PropertiesFile));

        String accessKey = s3Properties.getProperty("accessKey");
        String secretKey = s3Properties.getProperty("secretKey");
        bucketName = s3Properties.getProperty("bucketName");

        AWSCredentials credentials = new BasicAWSCredentials(
                accessKey,
                secretKey
        );

        s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_2)
                .build();
    }

    public AmazonS3 getS3Client() {
        return s3Client;
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * Function to delete an image in an S3 bucket
     * @param imageKey The s3 key used to find the image to delete
     */
    public void deleteImage(String imageKey){
        //Create the request to delete an image
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, imageKey);

        //Delete the image
        if(imageKey != null){
            s3Client.deleteObject(deleteObjectRequest);
        }
    }

    /**
     * Function to add an image to an S3 bucket
     * @param image the image to add to the S3 bucket
     * @return The image key generated for the image successfully added to the S3 bucket
     */
    public String addImage(BufferedImage image) throws IOException{

        //Create an image key to put in the database
        String imageKey = RandomString.generateRandomString(S3_KEY_SIZE);

        //Create an output stream to write the image to
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        //Attempt to write the image as a jpeg to the output stream
        ImageIO.write(image, "jpg", outputStream);

        //Transform the output stream into an input stream
        byte [] imageBuffer = outputStream.toByteArray();
        InputStream inputStream = new ByteArrayInputStream(imageBuffer);

        //Generate metadata for the image and include the length of the image buffer in the metadata
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(imageBuffer.length);

        //Put the image in the s3 bucket
        PutObjectRequest request = new PutObjectRequest(bucketName, imageKey, inputStream, metadata);
        s3Client.putObject(request);

        //Return the generated image key
        return imageKey;
    }

    /**
     * Function to generate a presigned image URL for a given key
     * @param imageKey The S3 key to be used to look up the image in the s3 bucket for which to generate a URL
     * @return A presigned URL to the image associated with the S3 key
     */
    public String generateImageURL(String imageKey){

        //Set an expiration time for the image URL
        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + DURATION_BEFORE_EXPIRATION_IN_MILLISECONDS);

        //Generate the presigned URL request to send for the s3 bucket
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, imageKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        //Generate the presigned URL
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

        //Return the presigned URL
        return url.toString();
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(!o.getClass().equals(S3Bucket.class)) return false;

        S3Bucket otherS3Bucket = (S3Bucket) o;

        if(!bothNullOrEqual(this.s3Client, otherS3Bucket.getS3Client())) return false;
        if(!bothNullOrEqual(this.bucketName, otherS3Bucket.getBucketName())) return false;

        return true;

    }

    private boolean bothNullOrEqual(Object o1, Object o2){
        if(o1 == null && o2 == null) return true;
        if(o1 == null || o2 == null) return false;
        return o1.equals(o2);
    }

    @Override
    public int hashCode(){
        return this.s3Client.hashCode();
    }

}
