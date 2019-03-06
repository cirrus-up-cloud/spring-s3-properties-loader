package cloud.cirrusup;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.io.ByteArrayInputStream;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link S3PropertySourcesPlaceholderConfigurer} class.
 */
public class S3PropertySourcesPlaceholderConfigurerTest {

    private S3PropertySourcesPlaceholderConfigurer configurer;
    private ConfigurableListableBeanFactory bean;
    private AmazonS3 amazonS3;

    @Before
    public void init() {

        amazonS3 = Mockito.mock(AmazonS3.class);
        bean = Mockito.mock(ConfigurableListableBeanFactory.class);
        configurer = new S3PropertySourcesPlaceholderConfigurer(amazonS3, "bucket", "file");
    }

    @Test(expected = NullPointerException.class)
    public void testNullS3Client() {

        //call
        new S3PropertySourcesPlaceholderConfigurer(null, "bucket", "file");
    }

    @Test(expected = NullPointerException.class)
    public void testEmptyBucket() {

        //call
        new S3PropertySourcesPlaceholderConfigurer(amazonS3, "  ", "file");
    }

    @Test(expected = NullPointerException.class)
    public void testEmptyKey() {

        //call
        new S3PropertySourcesPlaceholderConfigurer(amazonS3, "bucket", "  ");
    }

    @Test(expected = RuntimeException.class)
    public void testNoSuchBucket() {

        //setup
        AmazonS3Exception exception = Mockito.mock(AmazonS3Exception.class);
        when(amazonS3.getObject(anyObject())).thenThrow(exception);
        when(exception.getErrorCode()).thenReturn("NoSuchBucket");

        //call
        configurer.postProcessBeanFactory(bean);
    }

    @Test(expected = RuntimeException.class)
    public void testNoSuchKey() {

        //setup
        AmazonS3Exception exception = Mockito.mock(AmazonS3Exception.class);
        when(amazonS3.getObject(anyObject())).thenThrow(exception);
        when(exception.getErrorCode()).thenReturn("NoSuchKey");

        //call
        configurer.postProcessBeanFactory(bean);
    }

    @Test(expected = RuntimeException.class)
    public void testOtherS3Exception() {

        //setup
        AmazonS3Exception exception = Mockito.mock(AmazonS3Exception.class);
        when(amazonS3.getObject(anyObject())).thenThrow(exception);
        when(exception.getErrorCode()).thenReturn("unknown");

        //call
        configurer.postProcessBeanFactory(bean);
    }

    @Test(expected = RuntimeException.class)
    public void testAmazonClientException() {

        //setup
        AmazonClientException exception = Mockito.mock(AmazonClientException.class);
        when(amazonS3.getObject(anyObject())).thenThrow(exception);

        //call
        configurer.postProcessBeanFactory(bean);
    }

    @Test
    public void testComplete() {

        //setup
        S3Object object = Mockito.mock(S3Object.class);
        S3ObjectInputStream stream = new S3ObjectInputStream(new ByteArrayInputStream("key=value".getBytes()), mock(HttpRequestBase.class));

        when(amazonS3.getObject(anyObject())).thenReturn(object);
        when(object.getObjectContent()).thenReturn(stream);
        when(bean.getBeanDefinitionNames()).thenReturn(new String[]{});

        //call
        configurer.postProcessBeanFactory(bean);

        //verify
        verify(amazonS3, times(1)).getObject(anyObject());
        verifyNoMoreInteractions(amazonS3);
        verify(object, times(2)).getObjectContent();
        verifyNoMoreInteractions(object);
        verify(bean, times(1)).getBeanDefinitionNames();
        verify(bean, times(1)).resolveAliases(anyObject());
        verify(bean, times(1)).addEmbeddedValueResolver(anyObject());
        verifyNoMoreInteractions(bean);
    }
}
