package com.nam.assetmanager.controller;

import com.nam.assetmanager.model.Asset;
import com.nam.assetmanager.model.User;
import com.nam.assetmanager.repositories.AssetRepository;
import com.nam.assetmanager.service.AssetService;
import com.nam.assetmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetRepository assetRepository;

    // --- PUBLIC ACCESS ROUTES ---

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {
        userService.saveUser(user);
        return "redirect:/login?success";
    }

    // --- PROTECTED DASHBOARD & SEARCH ---

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String username = principal.getName();
        List<Asset> assets = assetService.getAssetsByUsername(username);

        // 1. Core Analytics
        long hwCount = assets.stream().filter(a -> "Hardware".equalsIgnoreCase(a.getCategory())).count();
        long swCount = assets.stream().filter(a -> "Software".equalsIgnoreCase(a.getCategory())).count();
        long repairCount = assets.stream().filter(a -> "In Repair".equalsIgnoreCase(a.getStatus())).count();

        // 2. Proactive Maintenance (EOL Check - 3 Year Limit)
        LocalDate expiryLimit = LocalDate.now().minusYears(3);
        List<Asset> expiredAssets = assets.stream()
                .filter(a -> a.getPurchaseDate() != null && a.getPurchaseDate().isBefore(expiryLimit))
                .toList();

        model.addAttribute("assets", assets);
        model.addAttribute("expiredAssets", expiredAssets);
        model.addAttribute("hwCount", hwCount);
        model.addAttribute("swCount", swCount);
        model.addAttribute("repairCount", repairCount);
        model.addAttribute("newAsset", new Asset());

        return "dashboard";
    }

    @GetMapping("/dashboard/search")
    public String searchAssets(@RequestParam("keyword") String keyword, Model model, Principal principal) {
        List<Asset> results = assetRepository.searchAssets(keyword);
        model.addAttribute("assets", results);
        model.addAttribute("newAsset", new Asset());
        return "dashboard";
    }

    @GetMapping("/dashboard/export")
    public void exportToCSV(jakarta.servlet.http.HttpServletResponse response, Principal principal) throws java.io.IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; file=Inventory_Report.csv");

        List<Asset> assets = assetService.getAssetsByUsername(principal.getName());
        java.io.PrintWriter writer = response.getWriter();

        // CSV Header
        writer.println("Asset Name,Serial Number,Category,Status,Description");

        // CSV Data Rows
        for (Asset asset : assets) {
            writer.println(String.format("%s,%s,%s,%s,%s",
                    asset.getAssetName(),
                    asset.getSerialNumber(),
                    asset.getCategory(),
                    asset.getStatus(),
                    asset.getBillDescription().replace(",", ";"))); // Prevent comma breaking CSV
        }
    }

    // --- ASSET OPERATIONS (CRUD) ---

    @PostMapping("/add-asset")
    public String addAsset(@ModelAttribute("newAsset") Asset asset, Principal principal, RedirectAttributes ra) {
        assetService.saveAsset(asset, principal.getName());
        ra.addFlashAttribute("message", "Asset assigned successfully!");
        return "redirect:/dashboard";
    }

    @PostMapping("/update-asset")
    public String updateAsset(@ModelAttribute("asset") Asset asset, Principal principal, RedirectAttributes ra) {
        // JPA save() performs an update if the ID is already present
        assetService.saveAsset(asset, principal.getName());
        ra.addFlashAttribute("message", "Asset updated successfully!");
        return "redirect:/dashboard";
    }

    @GetMapping("/delete-asset/{id}")
    public String deleteAsset(@PathVariable Long id, RedirectAttributes ra) {
        assetService.deleteAsset(id);
        ra.addFlashAttribute("message", "Asset removed from inventory.");
        return "redirect:/dashboard";
    }
}