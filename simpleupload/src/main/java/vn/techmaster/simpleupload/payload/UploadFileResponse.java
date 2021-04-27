package vn.techmaster.simpleupload.payload;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "UploadFileResponse")
@Table(name = "UploadFileResponse")
@NoArgsConstructor


public class UploadFileResponse {
    @Id  @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String fileName;
    private String fileDownloadUri;
    private String fileDescription;
    private String fileType;
    private long size;

    public UploadFileResponse(String fileName, String fileDownloadUri, String fileDiscription, String fileType, long size) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileDescription = fileDescription;
        this.fileType = fileType;
        this.size = size;
    }

	// Getters and Setters (Omitted for brevity)
}