package com.complaintiq.department;
import com.complaintiq.complaint.enums.ComplaintCategory;
import com.complaintiq.department.dto.DepartmentDTO;
import com.complaintiq.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j @Service @RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    @Transactional public DepartmentDTO createDepartment(DepartmentDTO request) {
        if (departmentRepository.existsByName(request.getName())) throw new DuplicateResourceException("Department","name",request.getName());
        if (departmentRepository.existsByCategory(request.getCategory())) throw new DuplicateResourceException("Department","category",request.getCategory().name());
        Department dept = Department.builder().name(request.getName().trim()).category(request.getCategory()).description(request.getDescription()).headAgentId(request.getHeadAgentId()).build();
        Department saved = departmentRepository.save(dept);
        log.info("Department created: name={} category={}", saved.getName(), saved.getCategory()); return mapToDTO(saved);
    }
    @Transactional(readOnly=true) public List<DepartmentDTO> getAllDepartments() { return departmentRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList()); }
    @Transactional(readOnly=true) public DepartmentDTO getById(Long id) { return mapToDTO(departmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Department","id",id))); }
    @Transactional(readOnly=true) public DepartmentDTO getByCategory(ComplaintCategory category) { return mapToDTO(departmentRepository.findByCategory(category).orElseThrow(() -> new ResourceNotFoundException("Department","category",category.name()))); }
    @Transactional public DepartmentDTO updateDepartment(Long id, DepartmentDTO request) {
        Department dept = departmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Department","id",id));
        if (request.getName() != null && !request.getName().isBlank()) dept.setName(request.getName().trim());
        if (request.getDescription() != null) dept.setDescription(request.getDescription());
        if (request.getHeadAgentId() != null) dept.setHeadAgentId(request.getHeadAgentId());
        return mapToDTO(departmentRepository.save(dept));
    }
    public DepartmentDTO mapToDTO(Department dept) { return DepartmentDTO.builder().id(dept.getId()).name(dept.getName()).category(dept.getCategory()).description(dept.getDescription()).headAgentId(dept.getHeadAgentId()).build(); }
    @Transactional(readOnly=true) public Department getEntityById(Long id) { return departmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Department","id",id)); }
}
