package knapp.util;

import knapp.table.Frequency;
import knapp.table.UnevenTable;
import knapp.table.values.GetMethod;
import knapp.table.Table;
import knapp.table.TableImpl;
import knapp.table.util.TableParser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

public class InputLoader {

    public static String loadTextFromClasspath(String cp) {
        InputStream in = InputLoader.class.getResourceAsStream(cp);
        if (in == null) {
//            System.out.println("The resource does not exist: "+cp);
            return "";
        }
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
        if (text == null || text.isEmpty()) {
            return null;
        }
        return TableParser.parse(text,true,Frequency.Monthly);
    }

    public static Table loadInputsTableFromClasspath(List<String> series, LocalDate start, LocalDate end, Frequency frequency) {
        return loadTableFromClasspath(series,start,end,frequency,new String[]{"/best-inputs/","/good-inputs/","/inputs/"},0);
    }

    public static Table loadMarketTableFromClasspath(String market, LocalDate start, LocalDate end, Frequency frequency) {
        return loadTableFromClasspath(Arrays.asList(market),start,end,frequency,new String[]{"/market/"},5);
    }

    public static Table loadTableFromClasspath(List<String> series, LocalDate start, LocalDate end, Frequency frequency,
                                               String[] prefixes, int column) {
        Map<String,Table> tables = new HashMap<>();
        TableImpl.TableBuilder tableBuilder = TableImpl.newBuilder().frequency(frequency);
        for (String s : series) {
            Table table = null;
            for (String prefix : prefixes) {
                table = loadTableFromClasspath(prefix + s + ".csv");
                if (table != null) {
                    tables.put(s, table);
                    tableBuilder.column(s);
                    break;
                }
            }
            if (table == null) {
                System.out.println("The table "+s+" DNE");
                throw new RuntimeException("The table " + s + " DNE");
            }
        }

        GetMethod tgm = GetMethod.INTERPOLATE;
        if (frequency == Frequency.Weekly || frequency == Frequency.Daily) {
            tgm = GetMethod.LAST_KNOWN_VALUE;
        }
        GetMethod gm = tgm;
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

    public static Table loadExactTableFromClasspath(Collection<String> series, Collection<String> prefixes) {
        UnevenTable.UnevenTableBuilder builder = UnevenTable.defineTable();
        for (String symbol : series) {
            for (String prefix : prefixes) {
                String cp = prefix+"/"+symbol + ".csv";
                String text = loadTextFromClasspath(cp);
                if (text == null || text.isEmpty()){
                    continue;
                }
                String[] header = null;
                boolean first = true;
                for (String line : text.split("\n")) {
                    String[] parts = line.split(",");
                    if (first) {
                        header = parts;
                        first = false;
                    } else {
                        LocalDate date = LocalDate.parse(parts[0]);
                        for (int i = 1; i < parts.length; i++) {
                            String v = parts[i];
                            String col = header[i];
                            if (".".equals(v)){
                                continue;
                            }
                            if (!v.matches("-?[\\d\\.]+")) {
                                continue;
                            }
                            builder.add(header[i],date,Double.parseDouble(v));
                        }
                    }
                }
                break;
            }
        }
        return builder.exact(true).build();
    }
}
