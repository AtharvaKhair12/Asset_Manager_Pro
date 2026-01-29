package com.nam.assetmanager.service;

import com.nam.assetmanager.model.Asset;
// Updated these two lines to 'repositories' to match your folder name
import com.nam.assetmanager.repositories.AssetRepository;
import com.nam.assetmanager.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveAsset(Asset asset, String username) {
        // We use the full path to your User model to avoid confusion with Spring's User
        com.nam.assetmanager.model.User user = userRepository.findByUsername(username);
        asset.setUser(user);
        assetRepository.save(asset);
    }

    public List<Asset> getAssetsByUsername(String username) {
        com.nam.assetmanager.model.User user = userRepository.findByUsername(username);
        return user.getAssets();
    }

    // This method handles the 'Delete' button request from the Controller
    public void deleteAsset(Long id) {
        assetRepository.deleteById(id);
    }
}