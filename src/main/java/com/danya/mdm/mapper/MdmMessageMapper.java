package com.danya.mdm.mapper;

import com.danya.mdm.dto.ChangePhoneDto;
import com.danya.mdm.model.MdmMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MdmMessageMapper {

    @Mapping(target = "payload",
            expression = "java(getPayload(dto.phone()))")
    @Mapping(target = "externalId", source = "id")
    MdmMessage toMdmMessage(ChangePhoneDto dto);

    default JsonNode getPayload(String phone) {
        return JsonNodeFactory
                .instance.objectNode()
                .put("phone", phone);
    }
}
