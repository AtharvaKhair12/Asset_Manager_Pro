package com.nam.assetmanager.controller;

import com.nam.assetmanager.model.Asset;
import com.nam.assetmanager.repositories.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private AssetRepository assetRepository;

    /**
     * Landing page for scanning a QR code.
     * URL: /public/asset/{id}
     */
    @GetMapping("/asset/{id}")
    public String viewPublicAsset(@PathVariable Long id, Model model) {
        Asset asset = assetRepository.findById(id).orElse(null);

        if (asset == null) {
            return "error/404"; // Or a simple "Asset not found" page
        }

        model.addAttribute("asset", asset);
        return "public-asset";
    }

    /**
     * Logic to handle "Report Fault" from a scanned mobile phone.
     */
    @PostMapping("/report-issue/{id}")
    public String reportIssue(@PathVariable Long id) {
        Asset asset = assetRepository.findById(id).orElse(null);

        if (asset != null) {
            // Automatically update status to trigger the Admin's Repair count
            asset.setStatus("In Repair");
            assetRepository.save(asset);
        }

        return "redirect:/public/asset/" + id + "?reported=true";
    }
}