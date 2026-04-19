package com.nam.assetmanager.repositories;

import com.nam.assetmanager.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // This manually teaches the database how to search your assets
    @Query("SELECT a FROM Asset a WHERE a.assetName LIKE %:keyword% OR a.serialNumber LIKE %:keyword%")
    List<Asset> searchAssets(@Param("keyword") String keyword);

    // Fetch assets assigned to a specific employee
    List<Asset> findByAssignedEmployeeId(Long employeeId);
}