package org.libsmith.anvil.net;

import org.libsmith.anvil.UncheckedException;
import org.libsmith.anvil.time.ImmutableDate;
import org.libsmith.anvil.time.TimePeriod;

import javax.annotation.Nonnull;
import javax.mail.Message;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 26.02.2015 21:19
 */
public interface MailDelivery {

    @Nonnull DeliveryResult send(@Nonnull Message message);
    @Nonnull CompletableFuture<DeliveryResult> sendAsync(@Nonnull Message message);

    //<editor-fold desc="class DeliveryResult and his Builder">
    class DeliveryResult implements Serializable {

        private static final long serialVersionUID = 730210939224224199L;

        enum Status {
            SENT, DELIVERED, IGNORED, ERROR
        }

        private final Message message;
        private final Status status;
        private final Date queuedDate;
        private final Date processedDate;
        private final Date deliveredDate;
        private final TimePeriod transportTime;
        private final Throwable throwable;

        protected DeliveryResult(Message message, Status status, Date queuedDate, Date processedDate,
                              Date deliveredDate, TimePeriod transportTime, Throwable throwable) {
            this.message = message;
            this.status = status;
            this.queuedDate = queuedDate;
            this.processedDate = processedDate;
            this.deliveredDate = deliveredDate;
            this.transportTime = transportTime;
            this.throwable = throwable;
        }

        public DeliveryResult throwExceptionOnError() {
            if (getThrowable() != null) {
                throw UncheckedException.rethrow(getThrowable());
            }
            return this;
        }

        //<editor-fold desc="Getters">
        public Message getMessage() {
            return message;
        }

        public Status getStatus() {
            return status;
        }

        public Date getQueuedDate() {
            return queuedDate;
        }

        public Date getProcessedDate() {
            return processedDate;
        }

        public Date getDeliveredDate() {
            return deliveredDate;
        }

        public TimePeriod getTransportTime() {
            return transportTime;
        }

        public Throwable getThrowable() {
            return throwable;
        }
        //</editor-fold>

        //<editor-fold desc="Builder">
        public static class Builder implements Serializable {

            private static final long serialVersionUID = 7577528367072016185L;

            private final Message message;
            private final Date queuedDate;
            private volatile long sendingStartTimestamp;
            private volatile Date processedDate;
            private volatile TimePeriod transportTime;
            private volatile Date deliveredDate;

            protected Builder(Message message, Date queuedDate) {
                this.message = message;
                this.queuedDate = queuedDate;
            }

            public static Builder queued(Message message) {
                return new Builder(message, ImmutableDate.now());
            }

            public Builder process() {
                this.sendingStartTimestamp = System.currentTimeMillis();
                return this;
            }

            public DeliveryResult sent() {
                this.processedDate = ImmutableDate.now();
                this.transportTime = TimePeriod.betweenMillis(sendingStartTimestamp, processedDate.getTime());
                return new DeliveryResult(message, Status.SENT, queuedDate, processedDate,
                                          deliveredDate, transportTime, null);
            }

            public DeliveryResult delivered() {
                this.deliveredDate = ImmutableDate.now();
                return new DeliveryResult(message, Status.DELIVERED, queuedDate, processedDate,
                                          deliveredDate, transportTime, null);
            }

            public DeliveryResult ignored() {
                return new DeliveryResult(message, Status.IGNORED, queuedDate, processedDate,
                                          deliveredDate, transportTime, null);
            }

            public DeliveryResult error(Throwable throwable) {
                this.processedDate = ImmutableDate.now();
                this.transportTime = TimePeriod.betweenMillis(sendingStartTimestamp, processedDate.getTime());
                return new DeliveryResult(message, Status.ERROR, queuedDate, processedDate,
                                          deliveredDate, transportTime, throwable);
            }
        }
        //</editor-fold>
    }
    //</editor-fold>
}
