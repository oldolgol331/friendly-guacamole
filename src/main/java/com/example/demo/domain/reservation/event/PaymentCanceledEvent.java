package com.example.demo.domain.reservation.event;

import com.example.demo.domain.reservation.model.Payment;
import lombok.Getter;

/**
 * PackageName : com.example.demo.domain.reservation.event
 * FileName    : PaymentCanceledEvent
 * Author      : oldolgol331
 * Date        : 25. 12. 21.
 * Description : 결제 취소 이벤트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 21.   oldolgol331          Initial creation
 */
@Getter
public class PaymentCanceledEvent extends PaymentEvent {

    private final String cancelReason;

    public PaymentCanceledEvent(final Object source, final Payment payment, final String cancelReason) {
        super(source, payment);
        this.cancelReason = cancelReason;
    }

}
