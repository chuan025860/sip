package org.chyunn_web.service;

import jakarta.transaction.Transactional;
import org.chyunn_web.bean.Asset.Inventory_Equipment;
import org.chyunn_web.bean.Resource.BulletinFile;
import org.chyunn_web.bean.Resource.BulletinPost;
import org.chyunn_web.bean.incident.Incident;
import org.chyunn_web.bean.incident.IncidentFile;
import org.chyunn_web.dto.BulletinFileDto;
import org.chyunn_web.dto.BulletinPostDto;
import org.chyunn_web.dto.Inventory_EquipmentDto;
import org.chyunn_web.repository.BulletinPostRepository;
import org.chyunn_web.repository.EditorImageUsageRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Service
public class BulletinPostService {
    @Autowired
    private BulletinPostRepository bulletinPostRepository;
    @Autowired
    private EditorImageUsageRepository editorImageUsageRepository;

    @Transactional
    public BulletinPost insert_bulletinPost_and_files(BulletinPost bulletinPost, List<MultipartFile> files) throws IOException {
        // 儲存事件資料
        BulletinPost newbulletinPost = bulletinPostRepository.save(bulletinPost);  // 插入事件並獲取 ID
        if (files == null) {
            //無附件直接回傳
            return newbulletinPost;
        }
        String uploadDirBase = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\Bulletin_file\\";
        // 上傳檔案（若有）
        List<BulletinFile> bulletinFiles = new ArrayList<>();
        String eventFolderPath = uploadDirBase + "BulletinPost_" + bulletinPost.getId();
        // 用來追蹤已儲存的臨時檔案
        List<File> tempFiles = new ArrayList<>();
        File dir = new File(eventFolderPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("無法建立事件資料夾");
        }
        // 儲存檔案並創建檔案記錄
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                if (originalFilename != null) {
                    // 儲存檔案到事件資料夾
                    String filePath = eventFolderPath + File.separator + originalFilename;
                    File storedFile = new File(filePath);
                    file.transferTo(storedFile);
                    tempFiles.add(storedFile);  // 將已儲存的檔案加入列表

                    // 創建 IncidentFiles 物件並設定檔案路徑
                    BulletinFile bulletinFile = new BulletinFile();
                    bulletinFile.setFileName(originalFilename);
                    bulletinFile.setFilePath(filePath);
                    bulletinFiles.add(bulletinFile);
                }
            }
        }
        try {
            for (BulletinFile file : bulletinFiles) {
                file.setBulletinPost(bulletinPost);  // Incident 關聯
            }
            newbulletinPost.setBulletinFiles(bulletinFiles);
            return bulletinPostRepository.save(bulletinPost);
        } catch (Exception e) {
            e.printStackTrace();
            // **刪除整個資料夾（包含所有檔案）**
            deleteFolder(dir);
            throw new RuntimeException(e); // 重新拋出異常
        }
    }


    public List<BulletinPost> findAll() {
        return bulletinPostRepository.findAll();
    }

    public BulletinPost findById(Integer id) {
        Optional<BulletinPost> bulletinPost = bulletinPostRepository.findById(id);
        if (bulletinPost.isPresent()) {
            return bulletinPost.get();
        } else {
            return null;
        }
    }

    private ModelMapper modelMapper = new ModelMapper();

    public BulletinPostDto convertBulletinPostDto(BulletinPost bulletinPost) {
        // 自動映射
        return modelMapper.map(bulletinPost, BulletinPostDto.class);
    }

    public BulletinFileDto convertBulletinFileDto(BulletinFile bulletinFile) {
        // 自動映射
        return modelMapper.map(bulletinFile, BulletinFileDto.class);
    }

    public List<BulletinPost> findTop5ByOrderByPublish_timeDesc() {
        return bulletinPostRepository.findTop5ByOrderByPublishTimeDesc();
    }

    public Page<BulletinPost> findAllOrderByCreatedAtDesc(Pageable pageable) {
        return bulletinPostRepository.findAllOrderByCreatedAtDesc(pageable);
    }

    public BulletinPost update_bulletinPost_and_files(BulletinPost bulletinPost, List<BulletinFile> bulletinFiles) {
        try {
            if (bulletinFiles != null && bulletinFiles.size() > 0) {
                for (BulletinFile file : bulletinFiles) {
                    file.setBulletinPost(bulletinPost);
                }
                bulletinPost.setBulletinFiles(bulletinFiles);
                return bulletinPostRepository.save(bulletinPost);
            } else {
                System.out.println("-------------------");
                return bulletinPostRepository.save(bulletinPost);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e); // 重新拋出異常
        }
    }

    @Transactional
    public void deleteByIds(List<Integer> ids) {
        for (Integer id : ids) {
            bulletinPostRepository.deleteById(id);
        }
    }

    @Scheduled(cron = "0 0 20 * * ?") // 每天 20:00 執行
    public void cleanUnusedImages() {
        // 共用資料夾的實體目錄（注意雙斜線轉義）
        Path imageDir = Paths.get("\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\bulletin\\ckeditor_images\\");
        Path baseDir = Paths.get("\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\Bulletin_file");

        // 從資料庫取得所有使用中圖片的相對路徑（/chyunn/uploads/ckeditor_images/xxx.png）
        List<String> usedImagePaths = editorImageUsageRepository.findAllImagePaths();  // 你需實作這個方法
        // 從資料庫取得所有存在的 bulletin_post_id
        List<Integer> existingPostIds = bulletinPostRepository.findAllPostIds();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(imageDir)) {
            for (Path file : stream) {
                String filename = file.getFileName().toString();
                if (!usedImagePaths.contains(filename)) {
                    Files.delete(file);
                    System.out.println("刪除未使用圖片：" + filename);
                }
            }

        } catch (IOException e) {
            System.err.println("刪除圖片時發生錯誤：" + e.getMessage());
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir)) {
            for (Path folder : stream) {
                String dirName = folder.getFileName().toString();

                if (dirName.startsWith("BulletinPost_")) {
                    String idStr = dirName.substring("BulletinPost_".length());
                    try {
                        Integer postId = Integer.valueOf(idStr);
                        if (!existingPostIds.contains(postId)) {
                            // 該資料夾所對應的 bulletin_post 已不存在 → 整個資料夾刪除
                            deleteDirectoryRecursively(folder);
                            System.out.println("刪除孤兒資料夾：" + folder);
                        }
                    } catch (NumberFormatException e) {
                        // 非合法格式略過
                        System.err.println("略過非預期資料夾：" + dirName);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("清理 bulletin_file 發生錯誤：" + e.getMessage());
        }
    }


    //刪除整個資料夾及其內容
    public void deleteFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete(); // 刪除資料夾內所有檔案
                }
            }
            folder.delete(); // 最後刪除資料夾本身
        }
    }

    // 工具方法：遞迴刪除資料夾
    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> files = Files.list(path)) {
                files.forEach(p -> {
                    try {
                        deleteDirectoryRecursively(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        Files.deleteIfExists(path);
    }
}
