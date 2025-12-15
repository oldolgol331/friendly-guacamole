package com.example.demo.common.model.generator;

import com.github.f4b6a3.ulid.UlidCreator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * PackageName : com.example.demo.common.model.generator
 * FileName    : UlidGenerator
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 엔티티 ID 대상 ULID 타입 생성기
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
public final class UlidGenerator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        return UlidCreator.getUlid().toUuid();
    }

}
