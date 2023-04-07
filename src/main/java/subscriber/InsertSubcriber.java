package subscriber;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class InsertSubcriber<T> implements Subscriber<T> {

    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(T t) {
        System.out.println("insert success");
    }

    @Override
    public void onError(Throwable t) {
        System.err.println("insert failed: "+ t.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println("connect finished");
    }
}
