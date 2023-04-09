package course.concurrency.m2_async.cf.min_price;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        var executor = Executors.newCachedThreadPool();
        var futureTasks = shopIds.stream()
                .map(it -> CompletableFuture.supplyAsync(
                                        () -> priceRetriever.getPrice(itemId, it), executor
                                ).completeOnTimeout(Double.NaN, 2800, TimeUnit.MILLISECONDS)
                                .handle((i, e) -> {
                                    if (e != null) {
                                        System.out.println("Задача в потоке " + Thread.currentThread().getName()
                                                + " завершилась с ошибкой");
                                        return Double.NaN;
                                    } else {
                                        return i;
                                    }
                                })
                )
                .collect(Collectors.toList());
        CompletableFuture.allOf(futureTasks.toArray(new CompletableFuture[]{})).join();
        return futureTasks.stream()
                .filter(cf -> cf.isDone() && !cf.isCancelled())
                .map(it -> {
                    try {
                        return it.get();
                    } catch (InterruptedException | ExecutionException e) {
                        System.out.println("Задача завершилась с ошибкой: " + e.getMessage() +
                                ", поток: " + Thread.currentThread().getName());
                        return Double.NaN;
                    }
                })
                .min(Double::compareTo)
                .orElseGet(() -> Double.NaN);
    }
}
