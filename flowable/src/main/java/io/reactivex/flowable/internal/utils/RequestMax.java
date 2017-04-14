
public static final class MaxRequestSubscription implements Consumer<Subscription> {
    public static final Consumer<Subscription> REQUEST_MAX = new MaxRequestSubscription();
    @Override
    public void accept(Subscription t) throws Exception {
        t.request(Long.MAX_VALUE);
    }
}
