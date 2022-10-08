package swlee.logiclist.utils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class S3UploaderService {

//    private static final Logger log = org.slf4j.LoggerFactory.getLogger(S3UploaderService.class);
    // local, development 등 현재 프로파일
    @Value("${spring.environment}")
    private String environment;

    // 파일이 저장되는 경로
    @Value("${spring.file-dir}")
    private String rootDir;
    private String fileDir;

    private final AmazonS3Client amazonS3Client;

    public S3UploaderService(AmazonS3Client amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    /**
     * 서버가 시작할 때 프로파일에 맞는 파일 경로를 설정해줌
     */
    @PostConstruct
    private void init(){
        System.out.println("environment = " + environment);
        if(environment.equals("local")){
            this.fileDir = System.getProperty("user.dir") + this.rootDir;
        }
        else if(environment.equals("development")){
            this.fileDir = this.rootDir;
        }

    }

    public String upload(MultipartFile multipartFile, String bucket, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)  // 파일 변환할 수 없으면 에러
                .orElseThrow(() -> new IllegalArgumentException("error: MultipartFile -> File convert fail"));

        return upload(uploadFile, bucket, dirName);
    }

    // S3로 파일 업로드하기
    private String upload(File uploadFile, String bucket, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + uploadFile.getName();   // S3에 저장된 파일 이름
        String uploadImageUrl = putS3(uploadFile, bucket, fileName); // s3로 업로드
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    // S3로 업로드
    private String putS3(File uploadFile, String bucket, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // 로컬에 저장된 이미지 지우기
    private void removeNewFile(File targetFile) {

        if (targetFile.delete()) {
            log.info("File delete success");
            return;
        }


        log.info("File delete fail");
    }



    public String removeS3File(String[] fileNames,String bucket){
        if(fileNames.length == 0){
            log.error("fileNames is empty");
            return "failed";
        }
        for(String file : fileNames){
            log.info("file::{}",file);
            String fileName=file.substring(file.indexOf("image"));
            //File  not Exist
            if(!amazonS3Client.doesObjectExist("logiclist", fileName)){
                log.error("AWS S3 Error!!! :: File not Exist!!");
                return "failed";
            }
            log.info("file name : "+ fileName);
            try {
                amazonS3Client.deleteObject(bucket, fileName);
            } catch (AmazonServiceException e) {
                log.error(e.getErrorMessage());
                return "failed";
            }
        }
        return "success";


    }
    /**
     * @param multipartFile
     * 로컬에 파일 저장하기
     */
    private Optional<File> convert(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return Optional.empty();
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);

        //파일 업로드
        File file = new File(fileDir+storeFileName);
        multipartFile.transferTo(file);

        return Optional.of(file);
    }

    /**
     * @description 파일 이름이 이미 업로드된 파일들과 겹치지 않게 UUID를 사용한다.
     * @param originalFilename 원본 파일 이름
     * @return 파일 이름
     */
    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    /**
     * @description 사용자가 업로드한 파일에서 확장자를 추출한다.
     *
     * @param originalFilename 원본 파일 이름
     * @return 파일 확장자
     */
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

}
