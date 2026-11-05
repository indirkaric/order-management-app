package com.sqa.oms.storage.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        name = "application.cloud-provider",
        havingValue = "AWS"
)
public class S3BucketStorageConfig {

    public static final String DEV_ENVIRONMENT = "DEV";

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String accessSecret;

    @Value("${cloud.aws.credentials.s3.endpoint}")
    private String url;

    @Value("${cloud.aws.region.static}")
    private String zone;

    @Value("${environment}")
    private String environment;

    /**
     * Builds the S3 client used by the service.
     *
     * @return {@link AmazonS3} - instance of AmazonS3
     */
    @Bean
    public AmazonS3 generateS3Client() {

        // LocalStack needs static credentials, plain HTTP and path-style access because it is
        // reached through a single host alias instead of per-bucket virtual-hosted DNS names.
        if (environment.equals(DEV_ENVIRONMENT)) {

            AWSCredentials credentials = new BasicAWSCredentials(accessKey, accessSecret);

            return AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withPathStyleAccessEnabled(true)
                    .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                    .withEndpointConfiguration(new AwsClientBuilder
                            .EndpointConfiguration(url, Regions.EU_WEST_2.getName()))
                    .build();
        }

        return AmazonS3ClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion(Regions.fromName(zone))
                .build();
    }
}
