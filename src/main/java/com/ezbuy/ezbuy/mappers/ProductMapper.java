package com.ezbuy.ezbuy.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.ezbuy.ezbuy.dtos.request.ProductRequest;
import com.ezbuy.ezbuy.dtos.response.AdminProductDetailResponse;
import com.ezbuy.ezbuy.dtos.response.ProductDetailResponse;
import com.ezbuy.ezbuy.dtos.response.ProductResponse;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.entities.User;

@Mapper(componentModel = "spring"
        // nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)

public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "manufacturer", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Product toProduct(ProductRequest productRequest);
    
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "manufacturer.name", target = "manufacturerName")
    ProductResponse toProductResponse(Product product);

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "manufacturer.name", target = "manufacturerName")
    ProductDetailResponse toProductDetailResponse(Product product);

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "manufacturer.name", target = "manufacturerName")
    AdminProductDetailResponse toAdminProductDetailResponse(Product product);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "manufacturer", ignore = true)
    @Mapping(target = "slug", ignore = true)
    void updateProductFromRequest(@MappingTarget Product product, ProductRequest productRequest);
}