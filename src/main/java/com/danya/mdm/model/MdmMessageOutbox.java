package com.danya.mdm.model;

import com.danya.mdm.dto.ResponseDataDto;
import com.danya.mdm.enums.DeliveryStatus;
import com.danya.mdm.enums.ServiceTarget;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MdmMessageOutbox extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID mdmMessageId;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @Enumerated(EnumType.STRING)
    private ServiceTarget target;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private ResponseDataDto responseData;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MdmMessageOutbox mdmMessageOutbox = (MdmMessageOutbox) o;
        return Objects.equals(id, mdmMessageOutbox.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
