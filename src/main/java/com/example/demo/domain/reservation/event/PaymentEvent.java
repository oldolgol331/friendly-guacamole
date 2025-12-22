package com.example.demo.domain.reservation.event;

import com.example.demo.domain.reservation.model.Payment;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * PackageName : com.example.demo.domain.reservation.event
 * FileName    : PaymentEvent
 * Author      : oldolgol331
 * Date        : 25. 12. 21.
 * Description : 결제 이벤트 상위 클래스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 21.   oldolgol331          Initial creation
 */
@Getter
public abstract class PaymentEvent extends ApplicationEvent {

    protected final Payment       payment;
    protected final LocalDateTime occurredAt;

    public PaymentEvent(final Object source, final Payment payment) {
        super(source);
        this.payment = payment;
        this.occurredAt = LocalDateTime.now();
    }

}
