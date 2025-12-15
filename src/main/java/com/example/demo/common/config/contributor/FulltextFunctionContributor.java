package com.example.demo.common.config.contributor;

import static org.hibernate.type.StandardBasicTypes.DOUBLE;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * PackageName : com.example.demo.common.config.contributor
 * FileName    : FulltextFunctionContributor
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : Fulltext 인덱스 사용 시 필요한 함수 등록
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
public class FulltextFunctionContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        TypeConfiguration typeConfig = functionContributions.getTypeConfiguration();

        functionContributions.getFunctionRegistry()
                             .registerPattern("fulltext_boolean_search_param_2",
                                              "MATCH (?1, ?2) AGAINST (?3 IN BOOLEAN MODE)",
                                              typeConfig.getBasicTypeRegistry().resolve(DOUBLE));
        functionContributions.getFunctionRegistry()
                             .registerPattern("fulltext_boolean_search_param_3",
                                              "MATCH (?1, ?2, ?3) AGAINST (?4 IN BOOLEAN MODE)",
                                              typeConfig.getBasicTypeRegistry().resolve(DOUBLE));
    }

}
