package com.ezbuy.ezbuy.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ezbuy.ezbuy.dtos.response.CategoryDetailResponse;
import com.ezbuy.ezbuy.dtos.response.CategoryResponse;
import com.ezbuy.ezbuy.dtos.response.CategoryTreeResponse;
import com.ezbuy.ezbuy.entities.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    
    @Mapping(source = "parent.id", target = "parentId")
    CategoryResponse toCategoryResponse(Category category);

    @Mapping(source = "parent.id", target = "parentId")
    CategoryDetailResponse toCategoryDetailResponse(Category category);

    CategoryTreeResponse toCategoryTreeResponse(Category category);
}