package moe.tristan.kmdah;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@Order(10000)
@ControllerAdvice
public class GlobalControllerAdvice {

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields(
            "class.*",
            "Class.*",
            "*.class.*",
            "*.Class.*"
        );
    }

}

