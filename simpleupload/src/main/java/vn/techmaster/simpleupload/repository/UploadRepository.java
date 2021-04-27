package vn.techmaster.simpleupload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import vn.techmaster.simpleupload.payload.UploadFileResponse;

@Repository
public interface UploadRepository extends JpaRepository<UploadFileResponse, Long>{
  
}
