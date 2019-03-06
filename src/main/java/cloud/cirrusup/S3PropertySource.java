package cloud.cirrusup;

import org.springframework.core.env.PropertySource;
import org.springframework.lang.Nullable;

import java.util.Enumeration;
import java.util.Properties;

/**
 * S3 property source.
 */
public class S3PropertySource extends PropertySource<Object> {

    private static final String S3_PROPERTY_SOURCE_NAME = "s3PropertySource";

    private final Properties properties;

    public S3PropertySource(Properties properties) {
        super(S3_PROPERTY_SOURCE_NAME);
        this.properties = properties;
    }

    @Nullable
    public Object getProperty(String name) {

        return this.properties.get(name);
    }

    public Enumeration<?> keys() {

        return this.properties.propertyNames();
    }
}