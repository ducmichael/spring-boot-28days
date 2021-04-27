package vn.techmaster.simpleupload.controller;



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

import vn.techmaster.simpleupload.payload.UploadFileResponse;
import vn.techmaster.simpleupload.service.FileStorageService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
public class FileController {
    @Autowired
    private FileStorageService fileStorageService;

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

  
    
    @PostMapping("/photo")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("description") String description) {
        String fileName = fileStorageService.storeFile(file,description);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/static/")
                .path(fileName)
                .toUriString();
        String fileDescription = description;
        
 
        return new UploadFileResponse(fileName, fileDownloadUri, fileDescription , file.getContentType(), file.getSize());
    }

    // @PostMapping("/uploadMultipleFiles")
    // public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
    //     return Arrays.asList(files)
    //             .stream()
    //             .map(file -> uploadFile(file))
    //             .collect(Collectors.toList());
    // }

    
    @GetMapping("/static/{fileName:.+}")
    public ResponseEntity<Resource> Static(@PathVariable String fileName, HttpServletRequest request) {
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