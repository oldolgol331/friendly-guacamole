package com.example.demo.common.model.generator;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.hibernate.annotations.IdGeneratorType;

/**
 * PackageName : com.example.demo.common.model.generator
 * FileName    : UlidGeneratorDef
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : ULID 타입 엔티티 ID에 ULID 생성기 연결 어노테이션
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@IdGeneratorType(UlidGenerator.class)
@Target({PACKAGE, TYPE})
@Retention(RUNTIME)
public @interface UlidGeneratorDef {

    String name();

}
