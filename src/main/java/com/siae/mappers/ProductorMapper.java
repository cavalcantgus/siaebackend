package com.siae.mappers;

import com.siae.entities.Produtor;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductorMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductor(@MappingTarget Produtor target, Produtor source);
}
