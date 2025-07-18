package org.chyunn_web.bean.Resource;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.chyunn_web.bean.Asset.General_Catalog;

@Data
@Entity
@Table(name = "bulletin_file")
@EqualsAndHashCode(exclude = {"bulletinPost"})
public class BulletinFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String fileId;

    @Column(name = "fileName", length = 255, nullable = false)
    private String fileName;

    private String filePath; // 檔案存放路徑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bulletin_post_id")
    private BulletinPost bulletinPost;
}
