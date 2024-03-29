Cấu hình máy chủ và thuộc tính lưu trữ tệp
Điều đầu tiên Đầu tiên! Hãy cấu hình ứng dụng Spring Boot của tôi để cho phép tải lên tệp Multipart và xác định kích thước tệp tối đa có thể được tải lên. tôi cũng sẽ định cấu hình thư mục mà tất cả các tệp đã tải lên sẽ được lưu trữ.

Mở src/main/resources/application.propertiestệp và thêm các thuộc tính sau vào nó:

## MULTIPART (MultipartProperties)
# Enable multipart uploads
spring.servlet.multipart.enabled=true
# Threshold after which files are written to disk.
spring.servlet.multipart.file-size-threshold=2KB
# Max file size.
spring.servlet.multipart.max-file-size=200MB
# Max Request Size
spring.servlet.multipart.max-request-size=215MB

## File Storage Properties
# All files uploaded through the REST API will be stored in this directory
file.upload-dir=/Users/callicoder/uploads
Lưu ý: Vui lòng thay đổi thuộc file.upload-dir tính thành đường dẫn bạn muốn lưu trữ các tệp đã tải lên.

Tự động liên kết các thuộc tính với một lớp POJO
Spring Boot có một tính năng tuyệt vời được gọi là @ConfigurationProperties sử dụng nó mà bạn có thể tự động liên kết các thuộc tính được xác định trong application.propertiestệp với một lớp POJO.

Hãy xác định một lớp POJO được gọi là gói FileStoragePropertiesbên trong com.example.filedemo.property để liên kết tất cả các thuộc tính lưu trữ tệp -


```java


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
```

Các @ConfigurationProperties(prefix = "file") chú thích làm công việc của mình khi khởi động ứng dụng và liên kết tất cả các thuộc tính với tiền tố file để các trường tương ứng của lớp POJO.

Nếu xác định các file thuộc tính bổ sung trong tương lai, bạn có thể chỉ cần thêm một trường tương ứng trong lớp trên và khởi động sẽ tự động liên kết trường với giá trị thuộc tính.

Bật thuộc tính cấu hình

Bây giờ, để bật ConfigurationPropertiestính năng này, bạn cần thêm @EnableConfigurationPropertieschú thích vào bất kỳ lớp cấu hình nào.

Mở lớp chính src/main/java/com/example/filedemo/FileDemoApplication.javavà thêm @EnableConfigurationPropertieschú thích vào nó như vậy -

```java


import com.example.filedemo.property.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class FileDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileDemoApplication.class, args);
    }
}
```


Viết API để Tải lên và Tải xuống Tệp
Bây giờ chúng ta hãy viết các API REST để tải lên và tải xuống tệp. Tạo một lớp điều khiển mới được gọi là gói FileControllerbên trong com.example.filedemo.controller.

Đây là mã hoàn chỉnh cho FileController-

```java


import com.example.filedemo.payload.UploadFileResponse;
import com.example.filedemo.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;
    
    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
```

Các FileController lớp học sử dụng FileStorageServiceđể lưu trữ các tập tin trong hệ thống tập tin và lấy chúng. Nó trả về một loại trọng tải UploadFileResponsesau khi tải lên hoàn tất. Hãy xác định từng lớp một.

UploadFileResponse
Như tên cho thấy, lớp này được sử dụng để trả về phản hồi từ /uploadFilevà /uploadMultipleFilesAPI.

Tạo UploadFileResponselớp bên trong com.example.filedemo.model gói với nội dung sau:

```java


public class UploadFileResponse {
    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private long size;

    public UploadFileResponse(String fileName, String fileDownloadUri, String fileType, long size) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileType = fileType;
        this.size = size;
    }

	// Getters and Setters (Omitted for brevity)
}
```
Dịch vụ lưu trữ tệp trong hệ thống tệp và truy xuất chúng
Bây giờ chúng ta hãy viết dịch vụ lưu trữ tệp trong hệ thống tệp và truy xuất chúng. Tạo một lớp mới được gọi là FileStorageService.java bên trong com.example.filedemo.service gói với nội dung sau:


```java


import com.example.filedemo.exception.FileStorageException;
import com.example.filedemo.exception.MyFileNotFoundException;
import com.example.filedemo.property.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
}
```

Các lớp ngoại lệ
Cả FileStorageService lớp ném một số ngoại lệ trong trường hợp có tình huống bất ngờ. Sau đây là các định nghĩa của các lớp ngoại lệ đó (Tất cả các lớp ngoại lệ đi bên trong gói com.example.filedemo.exception).

1. FileStorageException
Nó được ném ra khi một tình huống không mong muốn xảy ra trong khi lưu trữ một tệp trong hệ thống tệp -

```java


public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
```
2. MyFileNotFoundException
Nó được ném ra khi không tìm thấy tệp mà người dùng đang cố tải xuống.
```java


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MyFileNotFoundException extends RuntimeException {
    public MyFileNotFoundException(String message) {
        super(message);
    }

    public MyFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
```
Lưu ý rằng, tôi đã chú thích lớp ngoại lệ ở trên với @ResponseStatus(HttpStatus.NOT_FOUND). Điều này đảm bảo rằng Spring boot phản hồi với một 404 Not Foundtrạng thái khi ngoại lệ này được ném ra.