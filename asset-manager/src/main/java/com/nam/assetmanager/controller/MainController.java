package com.nam.assetmanager.controller;

import com.nam.assetmanager.model.Asset;
import com.nam.assetmanager.model.User;
import com.nam.assetmanager.repositories.AssetRepository;
import com.nam.assetmanager.repositories.UserRepository;
import com.nam.assetmanager.service.AssetService;
import com.nam.assetmanager.service.UserService;
import com.nam.assetmanager.service.FinancialService;
import com.nam.assetmanager.service.PredictiveMaintenanceService;
import com.nam.assetmanager.util.QRCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.nam.assetmanager.model.AuditLog;
import com.nam.assetmanager.repositories.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import org.springframework.data.repository.query.Param;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private FinancialService financialService;

    @Autowired
    private PredictiveMaintenanceService predictiveMaintenanceService;

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
    public String registerUser(@ModelAttribute("user") User user, HttpServletRequest request) {
        try {
            userService.saveUser(user, getSiteURL(request));
            return "redirect:/verify?sent=true";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/register?error";
        }
    }

    @GetMapping("/employee/register")
    public String employeeRegisterPage(Model model) {
        model.addAttribute("employee", new User());
        return "employee-register";
    }

    @GetMapping("/employee/login")
    public String employeeLoginPage() {
        return "employee-login";
    }

    @PostMapping("/employee/register")
    public String registerEmployee(@ModelAttribute("employee") User employee, HttpServletRequest request) {
        try {
            userService.saveEmployee(employee, getSiteURL(request));
            return "redirect:/verify?sent=true";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/employee/register?error";
        }
    }

    @GetMapping("/verify")
    public String verifyPage() {
        return "verify";
    }

    @PostMapping("/verify")
    public String verifyUser(@RequestParam("code") String code) {
        if (userService.verify(code)) {
            return "redirect:/login?verify_success";
        } else {
            return "redirect:/verify?error=true";
        }
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }

    // --- LOGS API ---
    @GetMapping("/public/api/asset/{id}/logs")
    @ResponseBody
    public List<AuditLog> getAssetLogs(@PathVariable Long id) {
        return auditLogRepository.findByAssetIdOrderByTimestampDesc(id);
    }

    @GetMapping("/public/api/asset/{id}")
    @ResponseBody
    public java.util.Map<String, Object> getAssetData(@PathVariable Long id) {
        Asset asset = assetService.getAssetById(id);
        if (asset == null) return null;
        
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", asset.getId());
        map.put("assetName", asset.getAssetName());
        map.put("serialNumber", asset.getSerialNumber());
        map.put("status", asset.getStatus());
        map.put("billDescription", asset.getBillDescription());
        map.put("purchaseDate", asset.getPurchaseDate());

        financialService.calculateTransientValue(asset);
        if (asset.getCurrentValue() != null) {
            map.put("currentValue", asset.getCurrentValue());
        }
        if (asset.getPurchasePrice() != null) {
            map.put("purchasePrice", asset.getPurchasePrice());
        }
        if (asset.getSalvageValue() != null) {
            map.put("salvageValue", asset.getSalvageValue());
        }
        if (asset.getLifespanMonths() != null) {
            map.put("lifespanMonths", asset.getLifespanMonths());
        }

        if (asset.getAssignedEmployee() != null) {
            java.util.Map<String, Object> emp = new java.util.HashMap<>();
            emp.put("id", asset.getAssignedEmployee().getId());
            map.put("assignedEmployee", emp);
        }
        return map;
    }

    // --- PROTECTED DASHBOARD & SEARCH ---

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        
        if (user != null && "ROLE_EMPLOYEE".equals(user.getRole())) {
            List<Asset> assignedAssets = assetRepository.findByAssignedEmployeeId(user.getId());
            assignedAssets.forEach(asset -> {
                String secureUrl = "http://localhost:8080/public/asset/" + asset.getId();
                asset.setQrCodeBase64(QRCodeGenerator.getQRCodeImage(secureUrl, 250, 250));
                financialService.calculateTransientValue(asset);
                predictiveMaintenanceService.analyzeTransientHealth(asset);
            });
            model.addAttribute("assets", assignedAssets);
            return "employee-dashboard";
        }

        // --- ADMIN DASHBOARD LOGIC ---
        List<Asset> assets = assetRepository.findAll();

        // 1. Core Analytics
        long hwCount = assets.stream().filter(a -> "Hardware".equalsIgnoreCase(a.getCategory())).count();
        long swCount = assets.stream().filter(a -> "Software".equalsIgnoreCase(a.getCategory())).count();
        long repairCount = assets.stream().filter(a -> "In Repair".equalsIgnoreCase(a.getStatus())).count();

        // 2. Proactive Maintenance (EOL Check - 3 Year Limit)
        LocalDate expiryLimit = LocalDate.now().minusYears(3);
        List<Asset> expiredAssets = assets.stream()
                .filter(a -> a.getPurchaseDate() != null && a.getPurchaseDate().isBefore(expiryLimit))
                .toList();

        List<Asset> inRepairAssets = assets.stream()
                .filter(a -> "In Repair".equalsIgnoreCase(a.getStatus()))
                .toList();

        // 3. QR Code Logic & Financial Evaluation & Predictive AI
        assets.forEach(asset -> {
            String secureUrl = "http://localhost:8080/public/asset/" + asset.getId();
            asset.setQrCodeBase64(QRCodeGenerator.getQRCodeImage(secureUrl, 250, 250));
            financialService.calculateTransientValue(asset);
            predictiveMaintenanceService.analyzeTransientHealth(asset);
        });

        // 4. Fetch Employees purely for Dropdown lists
        List<User> employees = userRepository.findByRole("ROLE_EMPLOYEE");

        model.addAttribute("assets", assets);
        model.addAttribute("expiredAssets", expiredAssets);
        model.addAttribute("inRepairAssets", inRepairAssets);
        model.addAttribute("hwCount", hwCount);
        model.addAttribute("swCount", swCount);
        model.addAttribute("repairCount", repairCount);
        model.addAttribute("newAsset", new Asset());
        model.addAttribute("employees", employees);

        return "dashboard";
    }

    @GetMapping("/dashboard/search")
    public String searchAssets(@RequestParam("keyword") String keyword, Model model, Principal principal) {
        List<Asset> results = assetRepository.searchAssets(keyword);

        results.forEach(asset -> {
            String secureUrl = "http://localhost:8080/public/asset/" + asset.getId();
            asset.setQrCodeBase64(QRCodeGenerator.getQRCodeImage(secureUrl, 250, 250));
            financialService.calculateTransientValue(asset);
            predictiveMaintenanceService.analyzeTransientHealth(asset);
        });

        model.addAttribute("assets", results);
        model.addAttribute("newAsset", new Asset());
        return "dashboard";
    }

    @GetMapping("/dashboard/export")
    public void exportToCSV(jakarta.servlet.http.HttpServletResponse response, Principal principal)
            throws java.io.IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; file=Inventory_Report.csv");

        // FIX: Export the Full Global Inventory, not just personal items
        List<Asset> assets = assetRepository.findAll();

        java.io.PrintWriter writer = response.getWriter();

        writer.println("Asset Name,Serial Number,Category,Status,Description");

        for (Asset asset : assets) {
            writer.println(String.format("%s,%s,%s,%s,%s",
                    asset.getAssetName(),
                    asset.getSerialNumber(),
                    asset.getCategory(),
                    asset.getStatus(),
                    asset.getBillDescription() != null ? asset.getBillDescription().replace(",", ";") : ""));
        }
    }

    // --- ASSET OPERATIONS (CRUD) ---

    @PostMapping("/add-asset")
    public String addAsset(@ModelAttribute("newAsset") Asset asset,
            @RequestParam(value = "assignedEmployeeId", required = false) Long assignedEmployeeId,
            Principal principal, RedirectAttributes ra) {
        // We still use saveAsset with principal to record WHO created it (Audit Trail),
        // but thanks to the changes above, EVERYONE will be able to see it.
        assetService.saveAsset(asset, principal.getName(), assignedEmployeeId);
        ra.addFlashAttribute("message", "Asset assigned successfully!");
        return "redirect:/dashboard";
    }

    @PostMapping("/update-asset")
    public String updateAsset(@ModelAttribute("asset") Asset asset,
            @RequestParam(value = "assignedEmployeeId", required = false) Long assignedEmployeeId,
            Principal principal, RedirectAttributes ra) {
        assetService.saveAsset(asset, principal.getName(), assignedEmployeeId);
        ra.addFlashAttribute("message", "Asset updated successfully!");
        return "redirect:/dashboard";
    }

    @GetMapping("/delete-asset/{id}")
    public String deleteAsset(@PathVariable Long id, RedirectAttributes ra) {
        assetService.deleteAsset(id);
        ra.addFlashAttribute("message", "Asset removed from inventory.");
        return "redirect:/dashboard";
    }

    // --- EMPLOYEE SPECIFIC ACTIONS ---
    @PostMapping("/employee/report-issue/{id}")
    public String employeeReportIssue(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        User user = userRepository.findByUsername(principal.getName());
        Asset asset = assetRepository.findById(id).orElse(null);

        // Security check: Only allow reporting if the asset is actually assigned to this employee
        if (asset != null && asset.getAssignedEmployee() != null && asset.getAssignedEmployee().getId().equals(user.getId())) {
            asset.setStatus("In Repair");
            assetService.saveAsset(asset, principal.getName(), asset.getAssignedEmployee().getId());
            ra.addAttribute("reported", "true");
        }
        
        return "redirect:/dashboard";
    }
}