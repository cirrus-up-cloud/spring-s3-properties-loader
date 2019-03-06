# Spring S3 Properties Loader
The aim of this library is to provide a simple mechanism to load configuration properties from a properties file stored in AWS S3.

# How to use it
In your Spring XML files, simply define a ```PropertySourcesPlaceholderConfigurer```, as it follows:

```
<bean id="s3PropertyPlaceholder" class="cloud.cirrusup.S3PropertySourcesPlaceholderConfigurer">
        <constructor-arg name="amazonS3" ref="amazonS3"/>
        <constructor-arg name="bucket" value="your-bucket"/>
        <constructor-arg name="fileName" value="path/to/file.properties"/>
</bean>
```

In order to get the maximum flexibility, you can set values for bucket and filename using Java system properties.

```
<bean id="s3PropertyPlaceholder" class="cloud.cirrusup.S3PropertySourcesPlaceholderConfigurer">
        <constructor-arg name="amazonS3" ref="amazonS3"/>
        <constructor-arg name="bucket" value="#{systemProperties['s3.config.bucket']}"/>
        <constructor-arg name="fileName" value="#{systemProperties['s3.config.filename']}"/>
</bean>
```