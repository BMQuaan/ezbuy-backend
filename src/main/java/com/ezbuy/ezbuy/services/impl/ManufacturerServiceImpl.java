package com.ezbuy.ezbuy.services.impl;

import com.ezbuy.ezbuy.dtos.response.ManufacturerResponse;
import com.ezbuy.ezbuy.entities.Manufacturer;
import com.ezbuy.ezbuy.mappers.ManufacturerMapper;
import com.ezbuy.ezbuy.repositories.ManufacturerRepository;
import com.ezbuy.ezbuy.services.ManufacturerService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Thêm import

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final ManufacturerMapper manufacturerMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "manufacturers", key = "'all'", sync = true)
    public List<ManufacturerResponse> getAllManufacturers() {
        List<Manufacturer> manufacturers = manufacturerRepository.findAll();
        
        return manufacturers.stream()
                .map(manufacturerMapper::toManufacturerResponse)
                .collect(Collectors.toList());
    }
}