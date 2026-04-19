package com.nam.assetmanager.service;

import com.nam.assetmanager.model.Asset;
import com.nam.assetmanager.repositories.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class FinancialService {

    @Autowired
    private AssetRepository assetRepository;

    /**
     * Dynamically calculates straight-line depreciation for a given asset.
     */
    public void calculateTransientValue(Asset asset) {
        if (asset.getPurchasePrice() == null || asset.getSalvageValue() == null || asset.getLifespanMonths() == null || asset.getLifespanMonths() == 0 || asset.getPurchaseDate() == null) {
            asset.setCurrentValue(asset.getPurchasePrice());
            return;
        }

        long monthsElapsed = ChronoUnit.MONTHS.between(asset.getPurchaseDate(), LocalDate.now());
        if (monthsElapsed < 0) monthsElapsed = 0;
        if (monthsElapsed > asset.getLifespanMonths()) monthsElapsed = asset.getLifespanMonths();

        BigDecimal depreciableBase = asset.getPurchasePrice().subtract(asset.getSalvageValue());
        BigDecimal monthlyDepreciation = depreciableBase.divide(new BigDecimal(asset.getLifespanMonths()), 2, RoundingMode.HALF_UP);
        BigDecimal accumulatedDepreciation = monthlyDepreciation.multiply(new BigDecimal(monthsElapsed));

        BigDecimal currentValue = asset.getPurchasePrice().subtract(accumulatedDepreciation);
        
        // Ensure it hasn't somehow dropped below salvage value due to rounding errors
        if (currentValue.compareTo(asset.getSalvageValue()) < 0) {
            currentValue = asset.getSalvageValue();
        }

        asset.setCurrentValue(currentValue);
    }

    /**
     * Executes daily at Midnight to crunch fleet-wide total metrics for analytics/reporting
     * and triggers any ROI lifecycle events.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyFleetDepreciationAudit() {
        System.out.println("==== [FinancialService] RUNNING DAILY DEPRECIATION AUDIT ====");
        List<Asset> fleet = assetRepository.findAll();
        
        BigDecimal totalPurchaseValue = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;

        for (Asset a : fleet) {
            calculateTransientValue(a);
            if (a.getPurchasePrice() != null) {
                totalPurchaseValue = totalPurchaseValue.add(a.getPurchasePrice());
            }
            if (a.getCurrentValue() != null) {
                totalCurrentValue = totalCurrentValue.add(a.getCurrentValue());
            }
        }
        
        System.out.println("Total Fleet Initial Value: $" + totalPurchaseValue);
        System.out.println("Total Fleet Current Depreciated Value: $" + totalCurrentValue);
        System.out.println("Total Accumulated Depreciation: $" + totalPurchaseValue.subtract(totalCurrentValue));
        System.out.println("=============================================================");
    }
}
