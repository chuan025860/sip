package org.inventory.bean;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
@Data
@Entity
@Table(name = "inventory_equipment_files")
@EqualsAndHashCode(exclude = {"inventoryEquipment"})
public class InventoryEquipmentFile {
    @Id
    @Column(name = "fileId", length = 50, nullable = false)
    private String fileId;

    @Column(name = "filePath", length = 255, nullable = false)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_property_id")
    private Inventory_Equipment inventoryEquipment;
}
