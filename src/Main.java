import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static final Lock lock = new ReentrantLock();
    private static final Condition equationSolved = lock.newCondition();

    public static void main(String[] args) {
        String inputFileName = "equations.txt";
        String outputFileName = "results.txt";

        try {
            BufferedReader br = new BufferedReader(new FileReader(inputFileName));
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));

            int equationCount = 0;

            ExecutorService executorService = Executors.newFixedThreadPool(5);

            String line;
            while ((line = br.readLine()) != null) {
                equationCount++;

                Callable<Double> equationTask = new EquationTask(line);

                Future<Double> futureResult = executorService.submit(equationTask);

                lock.lock();
                try {
                    double result = futureResult.get();
                    bw.write("Equation: " + line + " = " + result);
                    bw.newLine();
                } finally {
                    lock.unlock();
                }
            }
            executorService.shutdown();

            br.close();
            bw.close();

            System.out.println("Counted and saved results for " + equationCount + " equations.");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void signalEquationSolved() {
        lock.lock();
        try {
            equationSolved.signal();
        } finally {
            lock.unlock();
        }
    }
    private static void awaitEquationSolved() {
        lock.lock();
        try {
            equationSolved.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }
}
