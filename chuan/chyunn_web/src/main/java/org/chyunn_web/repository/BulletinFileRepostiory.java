package org.chyunn_web.repository;

import org.chyunn_web.bean.Resource.BulletinFile;
import org.chyunn_web.bean.Resource.BulletinPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BulletinFileRepostiory  extends JpaRepository<BulletinFile,String> {
}
