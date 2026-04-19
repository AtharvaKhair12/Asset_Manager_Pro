package com.nam.assetmanager.service;

import com.nam.assetmanager.model.Asset;
import com.nam.assetmanager.repositories.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PredictiveMaintenanceService {

    @Autowired
    private AssetRepository assetRepository;

    /**
     * Dynamically flags assets AT RISK based on their current tracking conditions.
     */
    public void analyzeTransientHealth(Asset asset) {
        String newHealth = "OPTIMAL";

        // Rule 1: High repair incidence
        if (asset.getRepairCount() != null && asset.getRepairCount() >= 3) {
            newHealth = "AT RISK";
        }

        // Rule 2: Approaching End of Life (80% through lifespan)
        if (asset.getPurchaseDate() != null && asset.getLifespanMonths() != null && asset.getLifespanMonths() > 0) {
            long monthsElapsed = ChronoUnit.MONTHS.between(asset.getPurchaseDate(), LocalDate.now());
            double lifeElapsedRatio = (double) monthsElapsed / asset.getLifespanMonths();
            
            if (lifeElapsedRatio >= 0.8) {
                newHealth = "AT RISK";
            }
        }

        // Rule 3: Current Status override
        if ("In Repair".equals(asset.getStatus()) && !"AT RISK".equals(newHealth)) {
            newHealth = "NEEDS ATTENTION";
        }

        asset.setHealthStatus(newHealth);
    }

    /**
     * Executes daily at 1:00 AM to persist health status checks to the database
     * directly for long-term analytics logs.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void dailyFleetHealthAudit() {
        System.out.println("==== [PredictiveMaintenanceService] RUNNING DAILY AI HEALTH SCAN ====");
        List<Asset> fleet = assetRepository.findAll();
        
        for (Asset asset : fleet) {
            String originalHealth = asset.getHealthStatus();
            analyzeTransientHealth(asset);
            
            if (!asset.getHealthStatus().equals(originalHealth)) {
                assetRepository.save(asset);
                System.out.println("-> Flagged Asset #" + asset.getId() + " (" + asset.getSerialNumber() + ") as " + asset.getHealthStatus());
            }
        }
    }
}
