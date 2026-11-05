package com.sqa.oms.order.modules.product.mapper;

import com.sqa.oms.order.entity.Product;
import com.sqa.oms.order.entity.ProductDocument;
import com.sqa.oms.order.modules.product.dto.ProductDTO;
import com.sqa.oms.order.modules.product.dto.ProductDocumentDTO;
import com.sqa.oms.order.modules.product.model.ProductDocumentResponse;
import com.sqa.oms.order.modules.product.model.ProductResponse;
import com.sqa.oms.order.modules.product.repository.projection.ProductProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IProductMapper {

    /**
     * Maps a product entity to its API representation, used only to build the domain event
     * payload off the just persisted entity.
     *
     * @param product product entity
     * @return ProductResponse
     */
    ProductResponse toResponse(Product product);

    /**
     * Maps a product projection to its DTO.
     *
     * @param projection projection to map
     * @return ProductDTO
     */
    ProductDTO toDto(ProductProjection projection);

    /**
     * Maps a single product.
     *
     * @param dto source product dto
     * @return ProductResponse
     */
    ProductResponse toResponse(ProductDTO dto);

    /**
     * Maps a product document entity to its API representation.
     *
     * @param document    product document entity
     * @param presignedUrl freshly generated presigned URL for the document's storage key
     * @return ProductDocumentResponse
     */
    @Mapping(target = "productEntityId", source = "document.product.entityId")
    @Mapping(target = "s3Url", source = "presignedUrl")
    ProductDocumentResponse toResponse(ProductDocument document, String presignedUrl);

    /**
     * Combines a document DTO with its freshly generated presigned URL into the final API
     * representation.
     *
     * @param dto          source document dto
     * @param presignedUrl freshly generated presigned URL for the document's storage key
     * @return ProductDocumentResponse
     */
    @Mapping(target = "s3Url", source = "presignedUrl")
    ProductDocumentResponse toResponse(ProductDocumentDTO dto, String presignedUrl);

    /**
     * Maps a product document entity to its DTO.
     *
     * @param document product document entity
     * @return ProductDocumentDTO
     */
    @Mapping(target = "productEntityId", source = "product.entityId")
    ProductDocumentDTO toDto(ProductDocument document);
}
