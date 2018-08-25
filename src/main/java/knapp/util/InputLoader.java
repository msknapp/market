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

    public static Table loadInputsTableFromClasspath(List<String> series) {
        return loadTableFromClasspath(series,Arrays.asList("/best-inputs/","/good-inputs/","/inputs/"));
    }

    public static Table loadMarketTableFromClasspath(String market) {
        return loadTableFromClasspath(Arrays.asList(market),Arrays.asList("/market/"));
    }

    public static Table loadTableFromClasspath(Collection<String> series, Collection<String> prefixes) {
        return loadExactTableFromClasspath(series, prefixes);
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
