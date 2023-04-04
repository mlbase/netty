package utils;

import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoTimeoutException;
import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class SubscribeHelper {

    public abstract static class ObservableSubscriber<T> implements Subscriber<T> {
        private final List<T> received;
        private final List<RuntimeException> errors;
        private final CountDownLatch latch;
        private volatile Subscription subscription;
        private volatile boolean completed;

        public ObservableSubscriber() {
            this.received = new ArrayList<>();
            this.errors = new ArrayList<>();
            this.latch = new CountDownLatch(1);
        }

        @Override
        public void onSubscribe(final Subscription s) {
            subscription = s;
        }

        @Override
        public void onNext(final T t) {
            received.add(t);
        }

        @Override
        public void onError(final Throwable t) {
            if (t instanceof RuntimeException) {
                errors.add((RuntimeException) t);
            } else {
                errors.add(new RuntimeException("Unexpected exception", t));
            }
            onComplete();
        }

        @Override
        public void onComplete() {
            completed = true;
            latch.countDown();
        }

        public Subscription getSubscription() {
            return subscription;
        }

        public List<T> getReceived() {
            return received;
        }

        public RuntimeException getErrors() {
            if (errors.size() > 0) {
                return errors.get(0);
            }
            return null;
        }

        public List<T> get() {
            return await().received;
        }

        public List<T> get(final long timeout, final TimeUnit unit) {
            return await(timeout, unit).received;
        }

        public T first() {
            List<T> received = await().getReceived();
            return received.size() > 0 ? received.get(0) : null;
        }

        public ObservableSubscriber<T> await() {
            return await(60, TimeUnit.SECONDS);
        }

        public ObservableSubscriber<T> await(final long timeout, final TimeUnit unit) {
            subscription.request(Integer.MAX_VALUE);
            try {
                if (!latch.await(timeout, unit)) {
                    throw new MongoTimeoutException("Publisher onComplete timed out");
                }
            } catch (InterruptedException e) {
                throw new MongoInterruptedException("Interrupted waiting for observeration", e);
            }
            if (!errors.isEmpty()) {
                throw errors.get(0);
            }
            return this;
        }
    }

    public static class OperationSubscriber<T> extends ObservableSubscriber<T> {

        @Override
        public void onSubscribe(final Subscription s) {
            super.onSubscribe(s);
            s.request(Integer.MAX_VALUE);
        }
    }

    public static class PrintSubscriber<T> extends OperationSubscriber<T> {
        private final String message;

        /**
         * A Subscriber that outputs a message onComplete.
         *
         * @param message the message to output onComplete
         */
        public PrintSubscriber(final String message) {
            this.message = message;
        }

        @Override
        public void onComplete() {
            System.out.printf((message) + "%n", getReceived());
            super.onComplete();
        }
    }

    public static class PrintDocumentSubscriber extends ConsumerSubscriber<Document> {
        /**
         * Construct a new instance
         */
        public PrintDocumentSubscriber() {
            super(t -> System.out.println(t.toJson()));
        }
    }

    public static class PrintToStringSubscriber<T> extends ConsumerSubscriber<T> {
        /**
         * Construct a new instance
         */
        public PrintToStringSubscriber() {
            super(System.out::println);
        }
    }

    public static class ConsumerSubscriber<T> extends OperationSubscriber<T> {
        private final Consumer<T> consumer;

        /**
         * Construct a new instance
         * @param consumer the consumer
         */
        public ConsumerSubscriber(final Consumer<T> consumer) {
            this.consumer = consumer;
        }


        @Override
        public void onNext(final T document) {
            super.onNext(document);
            consumer.accept(document);
        }
    }

    private SubscribeHelper() {
    }
}
