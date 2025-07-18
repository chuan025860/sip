package org.chyunn_web.repository;

import org.chyunn_web.bean.Asset.FixedAssetFile;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedAssetFileRepository extends JpaRepository<FixedAssetFile, String> {
}
