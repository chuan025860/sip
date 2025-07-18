package org.chyunn_web.bean.Resource;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "editor_image_usage")
@Data
public class EditorImageUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_path", nullable = false, length = 500)
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bulletin_post_id")
    private BulletinPost bulletinPost;
}
