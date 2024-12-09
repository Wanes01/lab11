package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadedSumMatrix implements SumMatrix {

    private final int nWorkers;

    public MultiThreadedSumMatrix(final int nWorkers) {
        this.nWorkers = nWorkers;
    }

    @Override
    public double sum(double[][] matrix) {
        final List<Worker> workers = new ArrayList<>(this.nWorkers);
        final int matSize = matrix.length * matrix[0].length;
        // The number of items each worker will sum (except possibly the last worker)
        final int items = matSize / this.nWorkers;
        for (int i = 0; (items * i) < matSize; i++) {
            int start = i * items;
            int end = (i + 1) * items;
            // Each worker will sum all elements in range [start, end)
            workers.add(new Worker(matrix, start, Math.min(end - 1, matSize - 1), i));
        }
        /*
         * Starting the workers
         */
        for (final var worker : workers) {
            worker.start();
        }
        /*
         * Waiting for the workers to finish
         */
        double sum = 0;
        for (final var worker : workers) {
            try {
                worker.join();
                sum += worker.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return sum;
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int endRow;
        private final int endCol;
        private final int id;
        private int row;
        private int col;
        private double res;

        public Worker(double[][] matrix, int startItem, int endItem, int id) {
            this.id = id;
            this.matrix = matrix;
            /*
             * Retrivies the row and column in the matrix
             * of the first and last items to sum
             */
            this.row = computeRow(startItem);
            this.col = computeCol(startItem);
            this.endRow = computeRow(endItem);
            this.endCol = computeCol(endItem);
        }

        public void run() {
            System.out.println(
                    String.format("[WORKER %d] Working from matrix[%d][%d] to matrix[%d][%d]",
                            id, row, col, endRow, endCol));
            /* Sums all elements in the given range */
            while (row != endRow || col != endCol) {
                res += matrix[row][col];
                if (col + 1 >= matrix[0].length) {
                    col = 0;
                    row++;
                } else {
                    col++;
                }
            }
            res += matrix[row][col];
        }

        public double getResult() {
            return this.res;
        }

        private int computeRow(int item) {
            return item / matrix[0].length;
        }

        private int computeCol(int item) {
            return item % matrix[0].length;
        }
    }
}
