package com.example.demo.domain.reservation.event;

import com.example.demo.domain.reservation.model.Payment;

/**
 * PackageName : com.example.demo.domain.reservation.event
 * FileName    : PaymentCompletedEvent
 * Author      : oldolgol331
 * Date        : 25. 12. 21.
 * Description : 결제 완료 이벤트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 21.   oldolgol331          Initial creation
 */
public class PaymentCompletedEvent extends PaymentEvent {
    public PaymentCompletedEvent(final Object source, final Payment payment) {
        super(source, payment);
    }
}
