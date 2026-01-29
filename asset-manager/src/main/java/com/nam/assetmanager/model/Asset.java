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

    private LocalDate purchaseDate; // The new field for Lifecycle tracking

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBillDescription() { return billDescription; }
    public void setBillDescription(String billDescription) { this.billDescription = billDescription; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}