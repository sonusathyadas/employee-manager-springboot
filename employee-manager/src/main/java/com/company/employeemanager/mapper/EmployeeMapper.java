package com.company.employeemanager.mapper;

import com.company.employeemanager.dto.request.CreateEmployeeRequest;
import com.company.employeemanager.dto.request.UpdateEmployeeRequest;
import com.company.employeemanager.dto.response.EmployeeResponse;
import com.company.employeemanager.entity.Employee;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between {@link Employee} entities and DTOs.
 * Null fields in update requests are ignored — only non-null values are applied.
 */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    /**
     * Maps an {@link Employee} entity to an {@link EmployeeResponse} DTO.
     *
     * @param employee the source entity.
     * @return the mapped response DTO.
     */
    EmployeeResponse toResponse(Employee employee);

    /**
     * Maps a {@link CreateEmployeeRequest} DTO to a new {@link Employee} entity.
     * The {@code id}, {@code createdAt}, {@code updatedAt} fields are not mapped
     * and will be set by the persistence layer and JPA auditing.
     *
     * @param request the incoming creation request.
     * @return a new, unmapped-id employee entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Employee toEntity(CreateEmployeeRequest request);

    /**
     * Applies non-null fields from an {@link UpdateEmployeeRequest} onto an existing
     * {@link Employee} entity (partial update / PATCH semantics).
     *
     * @param request  the update request containing fields to apply.
     * @param employee the existing entity to modify in place.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateEmployeeRequest request, @MappingTarget Employee employee);

    /**
     * Maps a list of {@link Employee} entities to a list of {@link EmployeeResponse} DTOs.
     *
     * @param employees the source list.
     * @return the mapped response list.
     */
    List<EmployeeResponse> toResponseList(List<Employee> employees);
}
