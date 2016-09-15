package edu.cmu.iccb.services;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@Service
public class ImageServiceS3Impl implements ImageService {
	
	@Value("${cloud.aws.s3.bucket}")
	private String bucket;
	
	@Value("${cloud.aws.credentials.accessKey}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secretKey}")
	private String secretKey;
	
	@Autowired
	private AmazonS3Client amazonS3Client;
	

	@Override
	public byte[] getImageData(String id) throws IOException {
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, id);

		S3Object s3Object = amazonS3Client.getObject(getObjectRequest);

		S3ObjectInputStream objectInputStream = s3Object.getObjectContent();

		byte[] bytes = IOUtils.toByteArray(objectInputStream);
		
		return bytes;
	}

	@Override
	public void saveImage(String imageName, InputStream inputStream) throws IOException {
		String imageId = Long.toString(System.currentTimeMillis());
				
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, imageId, inputStream, new ObjectMetadata());

		//putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
	//ObjectMetadata objectMetadata = new ObjectMetadata();

	//objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION); 

		PutObjectResult putObjectResult = amazonS3Client.putObject(putObjectRequest);

		IOUtils.closeQuietly(inputStream);

	}

	@Override
	public byte[] getThumbnailData(String id) throws IOException {
		return getImageData(id);
	}

	@Override
	public List<String> getUploadedImages() {
		ObjectListing objectListing = amazonS3Client.listObjects(new ListObjectsRequest().withBucketName(bucket));

		List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();
		
        List<String> fileNames = s3ObjectSummaries.stream()
                .map(f -> f.getKey())
                .collect(Collectors.toList());   

		return fileNames;
	}

}
