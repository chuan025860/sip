package org.chyunn_web.bean.Resource;

import jakarta.persistence.*;
import lombok.Data;
import org.chyunn_web.bean.Asset.GeneralCatalogFile;
import org.chyunn_web.bean.User.User;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "bulletin_post")
public class BulletinPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;                  // 自動編號主鍵
    private String title;               // 文章標題
    private String content;             // 文章內容（含 CKEditor HTML）
    @Column(name = "publish_time")
    private LocalDateTime publishTime;
    @Column(name = "end_time")
    private LocalDateTime endTime;
    private LocalDateTime created_at;    // 建立時間
    private LocalDateTime updated_at;    // 更新時間
    @Transient
    private String publishTimeStr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator", referencedColumnName = "loginId", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "bulletinPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EditorImageUsage> imageUsages;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bulletinPost", cascade = CascadeType.ALL)
    private List<BulletinFile> bulletinFiles = new ArrayList<>();

}
