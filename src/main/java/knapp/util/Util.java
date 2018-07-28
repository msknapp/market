package knapp.util;

import knapp.history.Frequency;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class Util {


    public static void doWithDate(String start, String end, Frequency frequency, ExceptionalConsumer<LocalDate> consumer) {
        LocalDate s = LocalDate.parse(start);
        LocalDate e = LocalDate.parse(end);
        doWithDate(s,e,frequency,consumer);
    }

    public static void doWithDate(LocalDate start, LocalDate end, Frequency frequency, ExceptionalConsumer<LocalDate> consumer) {
        doWithDate(start,end,frequency,d -> {
            consumer.accept(d);
            return false;
        });
    }

    public static void doWithDate(LocalDate start, LocalDate end, Frequency frequency, ExceptionalFunction<LocalDate, Boolean> consumer) {
        LocalDate d = start;
        while (!d.isAfter(end)) {
            try {
                boolean stop = consumer.accept(d);
                if (stop) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
            if (frequency.equals(Frequency.Annual)) {
                d = d.plusYears(1);
            } else if (frequency.equals(Frequency.Quarterly)) {
                d = d.plusMonths(3);
            } else if (frequency.equals(Frequency.Monthly)) {
                d = d.plusMonths(1);
            } else if (frequency.equals(Frequency.Daily)) {
                d = d.plusDays(1);
            } else if (frequency.equals(Frequency.Weekly)) {
                d = d.plusWeeks(1);
            }
        }
    }

    public static interface ExceptionalConsumer<T> {
        void accept(T t) throws Exception;
    }

    public static interface ExceptionalFunction<T,S> {
        S accept(T t) throws Exception;
    }

    public static String doWithWriter(ExceptionalConsumer<Writer> writerConsumer) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(byteArrayOutputStream);
        try {
            writerConsumer.accept(writer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(byteArrayOutputStream);
        }
        String out = new String(byteArrayOutputStream.toByteArray());
        return out;
    }

    public static void writeToFile(ExceptionalConsumer<Writer> writerConsumer,File file) {
        String tmp = doWithWriter(writerConsumer);
        try {
            FileUtils.writeStringToFile(file,tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double[][] toDoubleRows(int[] xColumns, LocalDate start, LocalDate end,
                                          Frequency frequency,
                                          BiFunction<LocalDate,Integer,String> valueProvider) {
        List<double[]> values = new ArrayList<>();
        LocalDate cd = start;
        while (!cd.isAfter(end)) {
            double[] t = new double[xColumns.length];
            int curCol = 0;
            for (int i : xColumns) {
                String v = valueProvider.apply(cd,i);
                if (v.isEmpty()) {
                    t[curCol] = 0.0;
                } else if (!v.matches("[\\d\\.]+")) {
                    t[curCol] = 0.0;
                } else if (".".equals(v)) {
                    t[curCol] = 0.0;
                } else {
                    double d = Double.parseDouble(v);
                    t[curCol] = d;
                }
                curCol++;
            }
            values.add(t);
            if (frequency == Frequency.Annual) {
                cd = cd.plusYears(1);
            } else if (frequency == Frequency.Quarterly) {
                cd = cd.plusMonths(3);
            } else if (frequency == Frequency.Monthly) {
                cd = cd.plusMonths(1);
            } else if (frequency == Frequency.Daily) {
                cd = cd.plusDays(1);
            }

        }
        double[][] x = new double[values.size()][];
        for (int i = 0;i<x.length;i++) {
            x[i] = values.get(i);
        }

        return x;
    }

    public static double[][] toDoubleColumns(int[] xColumns, LocalDate start, LocalDate end,
                                             Frequency frequency,
                                             BiFunction<LocalDate,Integer,String> valueProvider) {
        Map<Integer,List<Double>> values = new HashMap<>(xColumns.length);
        for (int i : xColumns) {
            values.put(i,new ArrayList<>());
        }
        LocalDate cd = start;
        while (!cd.isAfter(end)) {
            for (int i : xColumns) {
                List<Double> ds = values.get(i);
                String v = valueProvider.apply(cd,i);
                if (v.isEmpty()) {
                    ds.add(0.0);
                } else if (!v.matches("[\\d\\.]+")) {
                    ds.add(0.0);
                } else if (".".equals(v)) {
                    ds.add(0.0);
                } else {
                    double d = Double.parseDouble(v);
                    ds.add(d);
                }
            }
            if (frequency == Frequency.Annual) {
                cd = cd.plusYears(1);
            } else if (frequency == Frequency.Quarterly) {
                cd = cd.plusMonths(3);
            } else if (frequency == Frequency.Monthly) {
                cd = cd.plusMonths(1);
            } else if (frequency == Frequency.Daily) {
                cd = cd.plusDays(1);
            }

        }
        double[][] x = new double[xColumns.length][];
        int colNum = 0;
        for (int i : xColumns) {
            double[] colVals = new double[values.get(i).size()];
            List<Double> l = values.get(i);
            int row = 0;
            for (Double d : l) {
                colVals[row++] = d;
            }
            x[colNum] = colVals;
            colNum++;
        }

        return x;
    }
}
