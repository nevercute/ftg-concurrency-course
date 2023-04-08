package course.concurrency.m2_async.cf.report;

import course.concurrency.m2_async.cf.LoadGenerator;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class ReportServiceCF {

    private ExecutorService executor = Executors.newFixedThreadPool(2);

    private LoadGenerator loadGenerator = new LoadGenerator();

    public Others.Report getReport() {
        CompletableFuture<Others.Report> reportTask = CompletableFuture.supplyAsync(() -> getItems(), executor)
                .thenCombine(
                        CompletableFuture.supplyAsync(() -> getActiveCustomers(), executor),
                        (items, customers) -> combineResults(items, customers)
                );
        return reportTask.join();
    }

    private Others.Report combineResults(Collection<Others.Item> items, Collection<Others.Customer> customers) {
        return new Others.Report();
    }

    private Collection<Others.Customer> getActiveCustomers() {
        loadGenerator.work();
        loadGenerator.work();
        return List.of(new Others.Customer(), new Others.Customer());
    }

    private Collection<Others.Item> getItems() {
        loadGenerator.work();
        return List.of(new Others.Item(), new Others.Item());
    }

    public void shutdown() {
        executor.shutdown();
    }
}
