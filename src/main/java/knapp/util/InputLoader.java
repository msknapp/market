package knapp.util;

import knapp.history.Frequency;
import knapp.table.Table;
import knapp.table.TableImpl;
import knapp.table.TableParser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

public class InputLoader {

    public static String loadTextFromClasspath(String cp) {
        InputStream in = InputLoader.class.getResourceAsStream(cp);
        String text = "";
        try {
            text = IOUtils.toString(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        IOUtils.closeQuietly(in);
        return text;
    }

    public static Table loadTableFromClasspath(String cp) {
        String text = loadTextFromClasspath(cp);
        return TableParser.parse(text,true,Frequency.Monthly);
    }

    public static Table loadInputsTableFromClasspath(List<String> series, LocalDate start, LocalDate end, Frequency frequency) {
        return loadTableFromClasspath(series,start,end,frequency,"/inputs/",0);
    }

    public static Table loadMarketTableFromClasspath(String market, LocalDate start, LocalDate end, Frequency frequency) {
        return loadTableFromClasspath(Arrays.asList(market),start,end,frequency,"/market/",5);
    }

    public static Table loadTableFromClasspath(List<String> series, LocalDate start, LocalDate end, Frequency frequency,
                                               String prefix, int column) {
        Map<String,Table> tables = new HashMap<>();
        TableImpl.TableBuilder tableBuilder = TableImpl.newBuilder().frequency(frequency);
        for (String s : series) {
            Table table = loadTableFromClasspath(prefix+s+".csv");
            tables.put(s,table);
            tableBuilder.column(s);
        }

        TableImpl.GetMethod tgm = TableImpl.GetMethod.INTERPOLATE;
        if (frequency == Frequency.Weekly || frequency == Frequency.Daily) {
            tgm = TableImpl.GetMethod.LAST_KNOWN_VALUE;
        }
        TableImpl.GetMethod gm = tgm;
        Util.doWithDate(start,end,frequency,date -> {
            double[] values = new double[series.size()];
            int i = 0;
            for (String s : series) {
                Table t = tables.get(s);
                double v = t.getValue(date,0,gm);
                values[i++] = v;
            }
            tableBuilder.addRow(date,values);
        });
        return tableBuilder.build();
    }
}
