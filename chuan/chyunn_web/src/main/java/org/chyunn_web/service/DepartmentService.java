package org.chyunn_web.service;

import org.chyunn_web.bean.User.User;
import org.chyunn_web.bean.admin.Department;
import org.chyunn_web.bean.admin.Position;
import org.chyunn_web.bean.admin.SubSubUnit;
import org.chyunn_web.bean.admin.SubUnit;
import org.chyunn_web.repository.DepartmentRepo;
import org.chyunn_web.repository.PositionRepo;
import org.chyunn_web.repository.SubSubUnitRepo;
import org.chyunn_web.repository.SubUnitRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DepartmentService {
    @Autowired
    DepartmentRepo departmentRepo;
    @Autowired
    SubUnitRepo subUnitRepo;
    @Autowired
    SubSubUnitRepo subSubUnitRepo;
    @Autowired
    PositionRepo positionRepo;
    public Department findByIdByDepartment(Integer id) {
        Optional<Department> optional = departmentRepo.findById(id);
        if (optional.isPresent()) {
            Department department = optional.get();
            return department;
        } else {
            return null;
        }
    }
    public SubUnit findByIdBySubUnit(Integer id) {
        Optional<SubUnit> optional = subUnitRepo.findById(id);
        if (optional.isPresent()) {
            SubUnit subUnit = optional.get();
            return subUnit;
        } else {
            return null;
        }
    }
    public SubSubUnit findByIdBySubSubUnit(Integer id) {
        Optional<SubSubUnit> optional = subSubUnitRepo.findById(id);
        if (optional.isPresent()) {
            SubSubUnit subSubUnit = optional.get();
            return subSubUnit;
        } else {
            return null;
        }
    }
    public Position findByIdByPosition(Integer id) {
        Optional<Position> optional = positionRepo.findById(id);
        if (optional.isPresent()) {
            Position position = optional.get();
            return position;
        } else {
            return null;
        }
    }
}
