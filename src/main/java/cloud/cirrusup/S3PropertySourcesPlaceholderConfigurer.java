package cloud.cirrusup;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Sources Placeholder Configurer that reads properties from an AWS S3 properties file.
 */
public class S3PropertySourcesPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(S3PropertySourcesPlaceholderConfigurer.class);

    private final AmazonS3 amazonS3;
    private final String bucket;
    private final String fileName;

    /**
     * Constructor.
     *
     * @param amazonS3 AWS S3 client
     * @param bucket   S3 bucket
     * @param fileName file name
     */
    public S3PropertySourcesPlaceholderConfigurer(AmazonS3 amazonS3, String bucket, String fileName) {

        Preconditions.checkNotNull(amazonS3, "AWS S3 client cannot be null.");
        Preconditions.checkNotNull(StringUtils.trimToNull(bucket), "Bucket cannot be null or empty.");
        Preconditions.checkNotNull(StringUtils.trimToNull(fileName), "File name cannot be null or empty.");

        this.amazonS3 = amazonS3;
        this.bucket = bucket;
        this.fileName = fileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory) throws BeansException {

        try {

            LOG.info("Loading properties from S3 {} bucket, file {}.", bucket, fileName);
            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(getObjectContentFromS3()));

            S3PropertySource s3PropertySource = new S3PropertySource(properties);
            MutablePropertySources sources = new MutablePropertySources();
            sources.addLast(s3PropertySource);

            setPropertySources(sources);
            super.postProcessBeanFactory(beanFactory);
            LOG.info("Successfully loaded properties from S3.");
        } catch (IOException e) {

            LOG.warn("Exception on loading properties from S3 ", e);
            Throwables.propagate(e);
        }
    }

    private byte[] getObjectContentFromS3() throws IOException {

        S3Object object = null;
        try {

            GetObjectRequest request = new GetObjectRequest(bucket, fileName);
            object = amazonS3.getObject(request);
            return IOUtils.toByteArray(object.getObjectContent());
        } catch (AmazonS3Exception e) {

            if (e.getErrorCode().equals("NoSuchBucket")) {

                throw new IOException("Bucket doesn't exist.");
            }

            if (e.getErrorCode().equals("NoSuchKey")) {

                throw new IOException("Document with key " + fileName + " not found.");
            }

            throw new IOException(e);
        } catch (AmazonClientException e) {

            throw new IOException(e);
        } finally {

            IOUtils.closeQuietly(object.getObjectContent());
        }
    }
}
