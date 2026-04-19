package com.nam.assetmanager.service;

import com.nam.assetmanager.model.Asset;
import com.nam.assetmanager.model.AuditLog;
import com.nam.assetmanager.repositories.AssetRepository;
import com.nam.assetmanager.repositories.AuditLogRepository;
import com.nam.assetmanager.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void saveAsset(Asset asset, String username, Long assignedEmployeeId) {
        // Only override the creator/user if it's completely new or absent.
        if (asset.getId() == null || asset.getUser() == null) {
            com.nam.assetmanager.model.User user = userRepository.findByUsername(username);
            asset.setUser(user);
        }

        if (assignedEmployeeId != null) {
            com.nam.assetmanager.model.User employee = userRepository.findById(assignedEmployeeId).orElse(null);
            asset.setAssignedEmployee(employee);
        } else {
            asset.setAssignedEmployee(null); // Optional: clear assignment if passed null
        }

        boolean isNew = (asset.getId() == null);

        // --- PHASE 6 ENHANCEMENTS: API Image Defaults & Geo-Mapping ---
        if (isNew) {
            if (asset.getImageUrl() == null || asset.getImageUrl().isEmpty()) {
                String cat = asset.getCategory() != null ? asset.getCategory() : "Hardware";
                // Unsplash professional tech images (Randomized query)
                if ("Hardware".equalsIgnoreCase(cat)) {
                    asset.setImageUrl("https://images.unsplash.com/photo-1518770660439-4636190af475?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80"); // Circuit board
                } else if ("Software".equalsIgnoreCase(cat)) {
                    asset.setImageUrl("https://images.unsplash.com/photo-1555066931-4365d14bab8c?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80"); // Code / Screen
                } else {
                    asset.setImageUrl("https://images.unsplash.com/photo-1587202372634-32705e3bf49c?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80"); // Peripherals
                }
            }

            // Pseudo-random geo mapping around NYC Headquarters for demonstration
            if (asset.getLatitude() == null) {
                asset.setLatitude(40.7128 + (Math.random() - 0.5) * 0.1); 
            }
            if (asset.getLongitude() == null) {
                asset.setLongitude(-74.0060 + (Math.random() - 0.5) * 0.1);
            }
        }

        String oldStatus = null;
        if (!isNew) {
            Asset oldAsset = assetRepository.findById(asset.getId()).orElse(null);
            if (oldAsset != null) {
                oldStatus = oldAsset.getStatus();
                
                // Preserve hidden values
                if (asset.getRepairCount() == null) {
                    asset.setRepairCount(oldAsset.getRepairCount() != null ? oldAsset.getRepairCount() : 0);
                }
                if (asset.getHealthStatus() == null) {
                    asset.setHealthStatus(oldAsset.getHealthStatus());
                }
                if (asset.getLatitude() == null) {
                    asset.setLatitude(oldAsset.getLatitude());
                }
                if (asset.getLongitude() == null) {
                    asset.setLongitude(oldAsset.getLongitude());
                }
                if (asset.getImageUrl() == null || asset.getImageUrl().isEmpty()) {
                    asset.setImageUrl(oldAsset.getImageUrl());
                }
            }
        } else {
            if (asset.getRepairCount() == null) asset.setRepairCount(0);
        }

        // Automatic Repair Counter (Phase 5)
        if ("In Repair".equals(asset.getStatus()) && !"In Repair".equals(oldStatus)) {
            asset.setRepairCount(asset.getRepairCount() + 1);
        }

        // Save asset first to get/maintain ID for foreign keys
        Asset savedAsset = assetRepository.save(asset);

        // Immutable Audit Trail Generation
        if (isNew) {
            AuditLog log = new AuditLog(savedAsset, username, "CREATED", null, savedAsset.getStatus());
            auditLogRepository.save(log);
        } else if (oldStatus != null && !oldStatus.equals(savedAsset.getStatus())) {
            AuditLog log = new AuditLog(savedAsset, username, "STATUS_CHANGE", oldStatus, savedAsset.getStatus());
            auditLogRepository.save(log);
        }

        broadcastStats();
    }

    public List<Asset> getAssetsByUsername(String username) {
        com.nam.assetmanager.model.User user = userRepository.findByUsername(username);
        return user.getAssets();
    }

    // This method handles the 'Delete' button request from the Controller
    public void deleteAsset(Long id) {
        assetRepository.deleteById(id);
        broadcastStats();
    }

    public Asset getAssetById(Long id) {
        return assetRepository.findById(id).orElse(null);
    }

    private void broadcastStats() {
        List<Asset> assets = assetRepository.findAll();
        long hwCount = assets.stream().filter(a -> "Hardware".equalsIgnoreCase(a.getCategory())).count();
        long swCount = assets.stream().filter(a -> "Software".equalsIgnoreCase(a.getCategory())).count();
        long repairCount = assets.stream().filter(a -> "In Repair".equalsIgnoreCase(a.getStatus())).count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("hwCount", hwCount);
        stats.put("swCount", swCount);
        stats.put("repairCount", repairCount);

        messagingTemplate.convertAndSend("/topic/dashboard-stats", stats);
    }
}