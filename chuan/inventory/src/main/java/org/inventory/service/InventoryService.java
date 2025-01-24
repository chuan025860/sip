package org.inventory.service;

import org.inventory.bean.Inventory;
import org.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {
    @Autowired
    InventoryRepository inventoryRepository;

    public Boolean insertInventoryList(List<Inventory> inventoryList) {
        try {
            inventoryRepository.saveAll(inventoryList);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<Inventory> insertInventory(Inventory inventory) {
        try {
            Inventory savedInventory = inventoryRepository.save(inventory);
            return Optional.of(savedInventory);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Inventory getInventoryById(String id) {
        Optional<Inventory> optional = inventoryRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }else {
            return null;
        }
    }
    public List<Inventory> findByLocation(String location) {
        List<Inventory> inventoryList = inventoryRepository.findByLocation(location);
        if (inventoryList.isEmpty()) {
            return null;
        }else {
            return inventoryList;
        }
    }
    public List<Inventory> findByLocationAndState(String location,Boolean state) {
        List<Inventory> inventoryList = inventoryRepository.findByLocationAndState(location,state);
        if (inventoryList.isEmpty()) {
            return null;
        }else {
            return inventoryList;
        }
    }

    public List<Inventory> findAll() {
        List<Inventory> inventoryList = inventoryRepository.findAll();
        if (inventoryList.isEmpty()) {
            return null;
        }else {
            return inventoryList;
        }
    }
}
