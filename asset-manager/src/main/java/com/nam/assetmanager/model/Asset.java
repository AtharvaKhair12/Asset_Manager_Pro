package com.nam.assetmanager.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assetName;
    private String serialNumber;
    private String category;
    private String status;

    @Column(length = 1000)
    private String billDescription;

    // Lifecycle tracking field
    private LocalDate purchaseDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "assigned_employee_id")
    private User assignedEmployee;

    /**
     * The @Transient annotation is key. It allows us to hold the
     * QR code string in memory for the UI without storing it in MySQL.
     */
    @Transient
    private String qrCodeBase64;

    // --- FINANCIAL METRICS (PHASE 3) ---
    @Column(name = "purchase_price", precision = 10, scale = 2)
    private java.math.BigDecimal purchasePrice;

    @Column(name = "salvage_value", precision = 10, scale = 2)
    private java.math.BigDecimal salvageValue;

    @Column(name = "lifespan_months")
    private Integer lifespanMonths;

    @Transient
    private java.math.BigDecimal currentValue;

    // --- PREDICTIVE MAINTENANCE (PHASE 5) ---
    @Column(name = "repair_count", nullable = false)
    private Integer repairCount = 0;

    @Column(name = "health_status")
    private String healthStatus = "OPTIMAL";

    // --- PHASE 6 (GEO-TRACKING & IMAGE APIS) ---
    private Double latitude;
    
    private Double longitude;

    @Column(length = 1000)
    private String imageUrl;

    // --- GETTERS AND SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBillDescription() {
        return billDescription;
    }

    public void setBillDescription(String billDescription) {
        this.billDescription = billDescription;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(User assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }

    public java.math.BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(java.math.BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public java.math.BigDecimal getSalvageValue() {
        return salvageValue;
    }

    public void setSalvageValue(java.math.BigDecimal salvageValue) {
        this.salvageValue = salvageValue;
    }

    public Integer getLifespanMonths() {
        return lifespanMonths;
    }

    public void setLifespanMonths(Integer lifespanMonths) {
        this.lifespanMonths = lifespanMonths;
    }

    public java.math.BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(java.math.BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public Integer getRepairCount() {
        return repairCount;
    }

    public void setRepairCount(Integer repairCount) {
        this.repairCount = repairCount;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}