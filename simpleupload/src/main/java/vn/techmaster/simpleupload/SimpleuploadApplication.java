package vn.techmaster.simpleupload;


import vn.techmaster.simpleupload.property.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class SimpleuploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleuploadApplication.class, args);
    }
}