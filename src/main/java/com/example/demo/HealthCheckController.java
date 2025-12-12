package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.example.demo
 * FileName    : HealthCheckController
 * Author      : oldolgol331
 * Date        : 25. 12. 11.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 11.   oldolgol331          Initial creation
 */
@RestController
public class HealthCheckController {

    @GetMapping("/")
    public String healthCheck(){
        return "배포 성공! Hello World!";
    }

}
