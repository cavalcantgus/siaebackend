package com.siae.mappers;

import com.siae.entities.Produto;
import com.siae.entities.Produtor;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProduct(@MappingTarget Produto target, Produto source);
}
